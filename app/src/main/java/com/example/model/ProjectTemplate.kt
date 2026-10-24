package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

enum class ProjectTemplateType(
    val title: String,
    val description: String,
    val iconVector: ImageVector,
    val defaultFiles: List<Pair<String, String>>
) {
    BLANK_FOLDER(
        title = "Blank Folder",
        description = "Clean empty folder to organize files and assets",
        iconVector = Icons.Default.Folder,
        defaultFiles = emptyList()
    ),
    BLANK_PROJECT(
        title = "Blank Project",
        description = "Clean project root with README.md markdown",
        iconVector = Icons.Default.FolderZip,
        defaultFiles = listOf(
            "README.md" to """
# Project

Created with Linq Studios Workspace.

## Getting Started
- Add your source files in this folder.
- Run scripts or host with Local Dev Server.
""".trimIndent()
        )
    ),
    PYTHON_PROJECT(
        title = "Python Project",
        description = "Complete Python project with main.py, requirements, and docs",
        iconVector = Icons.Default.Terminal,
        defaultFiles = listOf(
            "main.py" to """
import sys

def main():
    print("=== Linq Studios Python Project ===")
    print("[INIT] Python Runtime initialized successfully")
    print(f"Platform: {sys.platform}")
    
    tasks = ["Initialize components", "Process data pipeline", "Ready for deployment"]
    for i, task in enumerate(tasks, start=1):
        print(f"  [{i}/{len(tasks)}] {task} [DONE]")
    
    print("\nReady! Press Ctrl+Enter to re-run at any time.")

if __name__ == "__main__":
    main()
""".trimIndent(),
            "requirements.txt" to """
# Project dependencies
# Add required libraries here (e.g. requests, numpy)
""".trimIndent(),
            "README.md" to """
# Python Project

Created in Linq Studios.

### How to Run:
- Press **Ctrl+Enter** (or tap the **Run** button) in the editor.
- Open **Terminal** and execute: `python3 main.py`
""".trimIndent()
        )
    ),
    WEB_PROJECT(
        title = "Web / Localhost Project",
        description = "HTML5 website with live preview, CSS3 styling, and JS interactivity",
        iconVector = Icons.Default.Language,
        defaultFiles = listOf(
            "index.html" to """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Linq Studios Web Project</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="card">
    <div class="logo">⚡ LINQ STUDIOS</div>
    <h1>Local Dev Server Ready</h1>
    <p>Live preview active on http://localhost:8080</p>
    <div class="stat-badge">HTTP 200 OK</div>
    <div class="actions">
      <button id="counterBtn">Click Count: 0</button>
      <button id="timeBtn" class="secondary">Show Time</button>
    </div>
    <div id="output"></div>
  </div>
  <script src="app.js"></script>
</body>
</html>
""".trimIndent(),
            "style.css" to """
body {
  margin: 0;
  padding: 24px;
  background: #0B0F17;
  color: #F8FAFC;
  font-family: system-ui, -apple-system, sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 85vh;
}
.card {
  background: #1E293B;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 28px 24px;
  max-width: 440px;
  width: 100%;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
}
.logo {
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 2px;
  color: #00E5FF;
  margin-bottom: 8px;
}
h1 {
  margin: 0 0 8px;
  font-size: 22px;
}
p {
  color: #94A3B8;
  font-size: 14px;
}
.stat-badge {
  display: inline-block;
  background: #064E3B;
  color: #6EE7B7;
  font-size: 11px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 99px;
  margin: 12px 0;
}
.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 14px;
}
button {
  background: #00E5FF;
  color: #0B0F17;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  font-size: 13px;
}
button.secondary {
  background: #334155;
  color: #F8FAFC;
}
#output {
  margin-top: 16px;
  font-family: monospace;
  font-size: 13px;
  color: #38BDF8;
}
""".trimIndent(),
            "app.js" to """
// Linq Studios Project Script
let count = 0;
const counterBtn = document.getElementById('counterBtn');
const timeBtn = document.getElementById('timeBtn');
const output = document.getElementById('output');

if (counterBtn) {
  counterBtn.addEventListener('click', () => {
    count++;
    counterBtn.textContent = `Click Count: ${'$'}{count}`;
    if (output) output.textContent = `⚡ Action logged at count ${'$'}{count}`;
  });
}

if (timeBtn) {
  timeBtn.addEventListener('click', () => {
    const now = new Date().toLocaleTimeString();
    if (output) output.textContent = `🕒 Current device time: ${'$'}{now}`;
  });
}
""".trimIndent(),
            "README.md" to """
# Web Project

Interactive HTML5, CSS3, and JavaScript project.

### Preview:
- Use the **Web Preview** / Local Dev Server to view changes instantly.
- Select this folder in the Dev Server host folder menu!
""".trimIndent()
        )
    ),
    JAVA_PROJECT(
        title = "Java Project",
        description = "Standard Java console application with Main.java entry point",
        iconVector = Icons.Default.Coffee,
        defaultFiles = listOf(
            "Main.java" to """
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Linq Studios Java Project ===");
        System.out.println("⚡ Java Runtime initialized [OK]");
        
        int total = 0;
        for (int i = 1; i <= 5; i++) {
            total += i;
            System.out.println("Step " + i + " processed, running sum = " + total);
        }
        
        System.out.println("Execution finished cleanly. Press Ctrl+Enter to re-run.");
    }
}
""".trimIndent(),
            "README.md" to """
# Java Project

Created in Linq Studios.

### How to Run:
- Press **Ctrl+Enter** (or tap **Run**) in the editor to run `Main.java`.
- Output displays in the live terminal execution drawer.
""".trimIndent()
        )
    ),
    NODE_PROJECT(
        title = "Node.js Project",
        description = "JavaScript / Node backend project with package.json and index.js",
        iconVector = Icons.Default.DataObject,
        defaultFiles = listOf(
            "index.js" to """
// Linq Studios Node.js Project
console.log("=== Linq Studios Node.js Project ===");
console.log("⚡ JavaScript engine initialized [OK]");

const info = {
  env: "Linq Mobile IDE",
  timestamp: new Date().toISOString(),
  status: "Online"
};

console.log("Runtime info:", JSON.stringify(info, null, 2));
console.log("Press Ctrl+Enter to execute.");
""".trimIndent(),
            "package.json" to """
{
  "name": "linq-node-project",
  "version": "1.0.0",
  "description": "Created with Linq Studios",
  "main": "index.js",
  "scripts": {
    "start": "node index.js"
  },
  "keywords": ["linq", "ide", "mobile"],
  "author": "Developer",
  "license": "MIT"
}
""".trimIndent(),
            "README.md" to """
# Node.js Project

Created in Linq Studios.

### How to Run:
- Press **Ctrl+Enter** or run `node index.js` in the Terminal.
""".trimIndent()
        )
    )
}
