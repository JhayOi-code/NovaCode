# Architecture: Linq Studio Execution Engine

This document outlines the software design, sequence diagrams, and concurrency model for the code execution subsystem in Linq Studio.

---

## 1. High-Level Pipeline

```
  [User Action]
  - Tap "Run" Button
  - Press Ctrl+Enter
         │
         ▼
  [EditorViewModel.runActiveFile()]
         │ 1. Saves active file to disk
         │ 2. Copies multi-file project files to cache workspace
         │ 3. Performs LanguageValidationRules checks (e.g. Java class name)
         │ 4. Calls RunnerRegistry.resolveRunner()
         │
         ▼
  [RunnerRegistry]
  - Evaluates file extension & shebang
  - Checks candidate tiers based on User Tier Preference (Embedded -> Termux -> Remote)
  - Returns RunnerResolution.Found(runner) OR RunnerResolution.Unavailable(reason)
         │
         ▼
  [CodeRunner.run(RunRequest)]  <-- Dispatchers.IO
  - Emits Flow<RunEvent>
      - RunEvent.Started
      - RunEvent.Stdout
      - RunEvent.Stderr
      - RunEvent.Exit
      - RunEvent.Failure
         │
         ▼
  [EditorViewModel]
  - Collects Flow<RunEvent>
  - Appends lines to LiveExecutionState
  - Runs ErrorLineParser to extract tappable file:line error targets
  - Enforces max output buffer truncation (default 5,000 lines)
         │
         ▼
  [ExecutionOutputSheet (Compose UI)]
  - Header: Language, Runner Tier Badge, Status, Elapsed Time, Exit Code
  - Live streamed LazyColumn with automatic auto-scroll
  - Tappable error hyperlinks jumping to editor line & highlighting
  - Interactive stdin input field for live interactive prompts
  - Controls: Run, Stop, Clear, Copy, Share, Open in Terminal, Dismiss
```

---

## 2. Core Contracts (`com.example.runner`)

### `CodeRunner` Interface
```kotlin
interface CodeRunner {
    val id: String
    val displayName: String
    val supportedLanguages: Set<String>
    suspend fun isAvailable(): Boolean
    fun run(request: RunRequest): Flow<RunEvent>
    fun stop()
    fun sendInput(input: String) {}
}
```

### `RunRequest`
```kotlin
data class RunRequest(
    val language: String,
    val code: String,
    val filePath: String,
    val fileName: String,
    val stdin: String? = null,
    val args: List<String> = emptyList(),
    val timeoutMs: Long = 30_000
)
```

### `RunEvent`
```kotlin
sealed class RunEvent {
    data class Started(val language: String, val runner: String) : RunEvent()
    data class Stdout(val text: String) : RunEvent()
    data class Stderr(val text: String) : RunEvent()
    data class Exit(val code: Int, val durationMs: Long) : RunEvent()
    data class Failure(val message: String) : RunEvent()
}
```

---

## 3. Concurrency & Android System Constraints

### W^X Android Security Policy
Since Android 10 (API 29) with targetSdk 35, the kernel enforces the `W^X` policy on internal storage (`/data/data/...`). Android applications cannot download and directly execute arbitrary ELF binaries (`Runtime.getRuntime().exec("/data/.../clang")` results in `EACCES: Permission denied`).

### Why the 3-Tier Model Solves This
1. **Tier 1 Embedded**: Runs entirely within JVM memory and system APIs (Android SQLite C library, Android V8 Chromium WebView runtime).
2. **Tier 2 Termux**: Uses Android IPC (`com.termux.permission.RUN_COMMAND`) to request the Termux app process (which is compiled as a terminal emulator with native execution rights) to compile and run binaries, returning stdout/stderr via PendingIntent broadcasts.
3. **Tier 3 Remote**: Relies on containerized sandboxes (Piston / Judge0) over HTTPS, requiring only network access and providing sandboxing against crashes.

---

## 4. Error Navigation (`ErrorLineParser`)
The output panel scans stdout and stderr using regex patterns for stack traces across languages:
- **Python**: `File "([^"]+)", line (\d+)`
- **GCC / Clang**: `([^:\s]+):(\d+):(?:\d+:)? (?:error|warning):`
- **Java**: `at [^\(]+\(([^:]+):(\d+)\)`
- **Rust**: `--> ([^:\s]+):(\d+):(\d+)`
- **Go**: `([^\s:]+\.go):(\d+)`

When the user taps an error line, `ExecutionOutputSheet` calls `onJumpToLine(fileName, line)`, which calculates character offset in `textFieldValue`, scrolls into view, and highlights the error line.
