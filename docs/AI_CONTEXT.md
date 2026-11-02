# Linq Studio Code Execution Architecture - AI Context & Reference

## System Identity & Core Philosophy
Linq Studio is an advanced mobile IDE for Android developed in Kotlin with Jetpack Compose.
**Rule #1 of this codebase: NEVER MOCK CODE EXECUTION.** Never use regex or string matches to guess program output. Never print fake "Completed in 18ms" messages without a real execution process. Every output line, exit code, and execution duration must originate from a real running interpreter, compiler, or database engine.

---

## Architecture Topology

```
┌────────────────────────────────────────────────────────┐
│ UI Layer (Jetpack Compose)                             │
│ - EditorScreen.kt (Run button, Ctrl+Enter, Line jumps) │
│ - ExecutionOutputSheet.kt (Live stream LazyColumn)     │
│ - SettingsScreen.kt (Tier prefs, Termux, Test suite)   │
└──────────────────────────┬─────────────────────────────┘
                           │ StateFlow & Callbacks
┌──────────────────────────▼─────────────────────────────┐
│ ViewModel & State (MVI/MVVM)                           │
│ - EditorViewModel.kt                                   │
│   • runActiveFile()                                    │
│   • stopExecution()                                    │
│   • sendStdin(input)                                   │
│   • LiveExecutionState (lines, status, durationMs)     │
└──────────────────────────┬─────────────────────────────┘
                           │ Coroutines (Dispatchers.IO)
┌──────────────────────────▼─────────────────────────────┐
│ Runner Abstraction Layer (com.example.runner)          │
│ - CodeRunner interface: run(RunRequest): Flow<RunEvent>│
│ - RunnerRegistry.resolveRunner(...)                    │
│ - RemoteLanguageConfig (40+ language catalog)          │
│ - LanguageValidationRules (compiled entrypoints check) │
│ - ErrorLineParser (stacktrace file:line extraction)    │
└────────────┬──────────────────┬──────────────────┬─────┘
             │                  │                  │
    ┌────────▼────────┐┌────────▼────────┐┌────────▼────────┐
    │ Tier 1 Embedded ││ Tier 2 Termux   ││ Tier 3 Remote   │
    │ - SQLite DB     ││ - RUN_COMMAND   ││ - Piston API    │
    │ - V8 WebView JS ││ - Tool checker  ││ - Judge0 CE     │
    │ - DOM & Console ││ - PendingIntent ││ - 40+ runtimes  │
    └─────────────────┘└─────────────────┘└─────────────────┘
```

---

## File Directory Reference
- `app/src/main/java/com/example/runner/`:
  - `CodeRunner.kt`: Interface definition, `RunRequest`, `RunEvent`
  - `RunnerRegistry.kt`: Resolution and ordering (Embedded -> Termux -> Remote)
  - `LiveExecutionState.kt`: UI models and `ErrorLineParser`
  - `LanguageValidationRules.kt`: Structural compiler syntax verification
  - `RemoteLanguageConfig.kt`: Canonical mapping for 40+ languages
- `app/src/main/java/com/example/runner/embedded/`:
  - `EmbeddedSqlRunner.kt`: Real Android SQLite table engine
  - `EmbeddedJsRunner.kt`: V8 Chromium JS execution
  - `EmbeddedHtmlRunner.kt`: WebView DOM console intercepts
  - `EmbeddedFormatRunner.kt`: JSON / Markdown / XML / CSV validator
- `app/src/main/java/com/example/runner/termux/`:
  - `TermuxRunner.kt`: Intent dispatcher for `com.termux.RUN_COMMAND`
  - `TermuxToolChecker.kt`: Tool detection and package install mapping
  - `TermuxResultReceiver.kt`: PendingIntent broadcast receiver
- `app/src/main/java/com/example/runner/remote/`:
  - `RemotePistonRunner.kt`: Piston / Judge0 HTTP client
- `app/src/main/java/com/example/ui/components/ExecutionOutputSheet.kt`:
  - Live streaming bottom sheet with stdin bar, action buttons, and tappable hyperlinks
- `app/src/main/java/com/example/viewmodel/EditorViewModel.kt`:
  - Central orchestrator binding execution state to Compose UI

---

## Key Invariants
1. `ExecutionOutputSheet` is controlled via `EditorUiState.isExecutionOutputOpen` and `EditorUiState.liveExecutionState`.
2. All execution operations execute asynchronously on `Dispatchers.IO` and emit events sequentially over a Kotlin `Flow`.
3. Execution jobs can be cancelled immediately via `viewModel.stopExecution()`.
4. Multi-file imports: when running a file, sibling workspace files are synced to the local cache directory first so sibling imports (e.g. Python modules, Java classes) resolve cleanly.
