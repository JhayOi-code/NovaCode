# Linq Studio - Native Android Multi-Language IDE

Linq Studio is a full-featured native Android IDE built with Kotlin and Jetpack Compose. It features a complete code editor, multi-tab file management, syntax highlighting, smart autocomplete, integrated terminal, Git version control, local web dev server, and a **genuine 3-tier multi-language code execution architecture**.

---

## 🚀 Code Execution Architecture

Linq Studio completely replaces mock/regex execution engines with a robust, production-grade, streaming runner pipeline. Code is never faked or simulated:

```
[EditorScreen / Run Button / Ctrl+Enter]
                   │
                   ▼
       [EditorViewModel.runActiveFile()]
                   │ (Dispatchers.IO)
                   ▼
         [RunnerRegistry.resolveRunner()]
  ┌────────────────┼────────────────┐
  │ Tier 1         │ Tier 2         │ Tier 3
  ▼                ▼                ▼
[Embedded Runner] [Termux Runner]  [Remote Piston API]
  - SQLite (.sql)   - Native Clang   - 40+ Languages
  - V8 JS (.js)     - OpenJDK Java   - Rust, Go, C#
  - HTML Preview    - Rustc, Go, PHP - Swift, Fortran, etc.
  - JSON/MD/CSV     - Python/Node
  └────────────────┼────────────────┘
                   │
                   ▼
         Flow<RunEvent> live stream
  (Started -> Stdout/Stderr -> Exit code)
                   │
                   ▼
      [ExecutionOutputSheet UI]
  (Live output, tappable line jump, stdin bar)
```

### Execution Tiers

1. **Tier 1: Embedded (Offline, Zero Extra Apps)**
   - **SQLite (`.sql`)**: Executes queries on an in-memory Android SQLite database (`SQLiteDatabase.create(null)`). Displays table results in formatted ASCII tables with row counts and column names.
   - **JavaScript (`.js`)**: Executes real ECMAScript code using Android's V8 WebView engine with console redirection (`console.log`, `warn`, `error`, `info`), JSON parsing, and error stack trace extraction.
   - **HTML/CSS (`.html`, `.css`)**: Renders in WebView with live console message capture via `WebChromeClient.onConsoleMessage` and previewing.
   - **Structure & Formatters (`.json`, `.md`, `.xml`, `.csv`, `.yaml`)**: Parses and validates syntax, displaying formatted structure trees and line-accurate error diagnostics.

2. **Tier 2: Termux Integration (Offline Native Toolchains)**
   - Executes real local compilers and interpreters on-device via Termux (`com.termux.permission.RUN_COMMAND`).
   - Supports GCC/Clang (C/C++), OpenJDK 21 (Java), Kotlinc (Kotlin), Rustc (Rust), Go, Python 3, Node.js, PHP, Ruby, Perl, Lua, R, and Bash.
   - Captures stdout/stderr via PendingIntent broadcasts and streams results directly into the Linq Studio output panel.

3. **Tier 3: Remote Execution (Piston / Judge0 CE)**
   - Connects to Piston CE (`https://emkc.org/api/v2/piston`) or Judge0 instances supporting 40+ programming languages.
   - Sends code, command-line arguments, and stdin.
   - Streams compilation errors, standard output, standard error, execution duration, and exit codes.
   - Gracefully handles rate limits (HTTP 429), timeouts, and authentication keys.

---

## 🛠️ Setup Instructions

### 1. Termux Setup (for Tier 2 Local Offline Compilers)
To enable real offline compiled execution for C, C++, Java, Kotlin, Rust, and Go:
1. Install **Termux** from [F-Droid](https://f-droid.org/en/packages/com.termux/) (do not use the deprecated Google Play version).
2. Open Termux and enable external app execution by running:
   ```bash
   echo "allow-external-apps=true" >> ~/.termux/termux.properties
   ```
3. Install required toolchains:
   ```bash
   pkg update
   pkg install clang openjdk-21 rust golang python nodejs
   ```
4. In Linq Studio Settings > **Termux Setup**, tap **Check Termux** to verify permissions and toolchain availability.

### 2. Remote API Configuration (for Tier 3 Cloud Execution)
- Default instance: `https://emkc.org/api/v2/piston` (public, free).
- If using a self-hosted Piston or Judge0 instance, or an instance requiring an API key, enter the Base URL and API Key in Linq Studio Settings > **Code Execution & Runners**.

### 3. Chaquopy Setup (Embedded Python)
To add Chaquopy for embedded Python without Termux:
1. In `build.gradle.kts` (Project root), add the Chaquopy plugin:
   ```kotlin
   plugins {
       id("com.chaquo.python") version "15.0.1" apply false
   }
   ```
2. In `app/build.gradle.kts`:
   ```kotlin
   plugins {
       id("com.chaquo.python")
   }
   chaquopy {
       defaultConfig {
           pip {
               install("numpy")
               install("requests")
           }
       }
   }
   ```

---

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| `Ctrl + Enter` | Run active code immediately |
| `Ctrl + S` | Save active file |
| `Ctrl + F` | Toggle Find & Replace overlay |
| `Ctrl + B` | Toggle Explorer sidebar |
| `Ctrl + Z` | Undo edit |
| `Ctrl + Y` / `Ctrl + Shift + Z` | Redo edit |
| `Ctrl + W` | Close active tab |
| `Ctrl + N` | Create new file |
| `Ctrl + /` | Open keyboard shortcuts help |

---

## 🧪 Testing and Verification

Run local unit tests for the execution registry, error parser, and language rules:
```bash
gradle :app:testDebugUnitTest
```
Verify app compilation:
```bash
gradle assembleDebug
```
