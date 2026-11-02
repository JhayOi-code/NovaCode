# Troubleshooting Guide: Code Execution in Linq Studio

This guide addresses common questions, error codes, and configuration issues.

---

## 1. "Setup Required: Termux RUN_COMMAND permission missing"
- **Cause**: Termux is either not installed or external app command execution is disabled in Termux configuration.
- **Solution**:
  1. Make sure Termux was installed from **F-Droid**, not Google Play (Google Play version is unmaintained and blocks background intents).
  2. Open Termux and run:
     ```bash
     mkdir -p ~/.termux
     echo "allow-external-apps=true" >> ~/.termux/termux.properties
     ```
  3. Close and restart the Termux app.
  4. In Linq Studio Settings > **Termux Setup**, tap **Check Termux**. Both badges should now display green.

---

## 2. "HTTP 429 Too Many Requests" on Remote Runner
- **Cause**: The public Piston instance (`https://emkc.org/api/v2/piston`) is rate-limited when multiple requests are sent rapidly.
- **Solution**:
  1. Wait 15 seconds before re-running.
  2. If you run frequently, consider pointing Linq Studio to a self-hosted Piston instance or a Judge0 instance:
     - In Linq Studio Settings > **Code Execution & Runners**, change **Remote API URL** to your endpoint (e.g. `https://piston.mycompany.com/api/v2/piston`).
     - Enter your API Key if authentication is enabled.

---

## 3. "Java source file must declare a public class matching the file name"
- **Cause**: The Java language specification requires public top-level classes to match the filename. For example, `Example.java` must declare `public class Example`.
- **Solution**:
  - Rename the file in the sidebar to match the class, or change `public class Main` to `public class Example` inside `Example.java`.

---

## 4. "No process or timeout exceeded 30 seconds"
- **Cause**: The program entered an infinite loop (e.g. `while(true) {}`) or waited for input without interactive input provided.
- **Solution**:
  - Tap the red **Stop** button in the output panel to terminate the process immediately.
  - You can increase the timeout limit in Settings > **Code Execution & Runners** > **Timeout (s)** (up to 120 seconds).
  - If the program expects input, use the **stdin** field in the output sheet while it is running.

---

## 5. Offline Compiled Languages (C, C++, Rust, Go) Failing Without Internet
- **Cause**: Android 10+ prevents executing native ELF binaries from inside app sandboxes (`/data/data/com.aistudio...`). Therefore, compiled languages cannot be bundled inside the APK.
- **Solution**:
  - Install Termux and run `pkg install clang rust golang`.
  - In Settings > **Preferred Execution Tier**, select **Termux First**. Linq Studio will execute the code locally on your device completely offline!
