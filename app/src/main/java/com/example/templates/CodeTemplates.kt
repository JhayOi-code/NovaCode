package com.example.templates

data class CodeSnippetTemplate(
    val id: String,
    val title: String,
    val category: String,
    val language: String,
    val defaultFileName: String,
    val description: String,
    val snippet: String
)

object CodeTemplates {

    val allTemplates = listOf(
        // ==================== WEB & FRONTEND ====================
        CodeSnippetTemplate(
            id = "html5_starter",
            title = "HTML5 Modern Boilerplate",
            category = "Web & Frontend",
            language = "HTML / Web",
            defaultFileName = "index.html",
            description = "Standard modern HTML5 document structure with viewport meta, CSS reset, and clean responsive layout.",
            snippet = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Modern Web App</title>
  <style>
    :root {
      --primary: #6366f1;
      --bg: #0f172a;
      --card: #1e293b;
      --text: #f8fafc;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: system-ui, -apple-system, sans-serif;
      background: var(--bg);
      color: var(--text);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      padding: 24px;
    }
    .card {
      background: var(--card);
      border-radius: 16px;
      padding: 32px;
      max-width: 440px;
      width: 100%;
      text-align: center;
      box-shadow: 0 10px 25px rgba(0,0,0,0.4);
      border: 1px solid rgba(255,255,255,0.1);
    }
    h1 { margin-bottom: 12px; font-size: 26px; color: #a5b4fc; }
    p { margin-bottom: 24px; color: #94a3b8; line-height: 1.5; }
    button {
      background: var(--primary);
      color: white;
      border: none;
      padding: 12px 24px;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      transition: transform 0.1s, opacity 0.2s;
    }
    button:active { transform: scale(0.96); }
  </style>
</head>
<body>
  <div class="card">
    <h1>Hello from Linq Studio!</h1>
    <p>This is a complete responsive HTML5 starter with custom CSS styling.</p>
    <button onclick="handleClick()">Click Me</button>
  </div>

  <script>
    let count = 0;
    function handleClick() {
      count++;
      console.log('Button clicked! Total:', count);
      alert('Hello from HTML5! Taps: ' + count);
    }
  </script>
</body>
</html>"""
        ),

        CodeSnippetTemplate(
            id = "react_cdn",
            title = "React 18 CDN Boilerplate",
            category = "Web & Frontend",
            language = "HTML / Web",
            defaultFileName = "react_app.html",
            description = "Interactive React 18 single-file application with Babel Standalone, hooks, and reactive state.",
            snippet = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>React 18 App</title>
  <!-- React 18 & ReactDOM UMD -->
  <script crossorigin src="https://unpkg.com/react@18/umd/react.development.js"></script>
  <script crossorigin src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
  <!-- Babel Standalone for in-browser JSX transformation -->
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <style>
    body {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: #090d16;
      color: #ffffff;
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .app-container {
      background: #151c2c;
      padding: 32px;
      border-radius: 16px;
      border: 1px solid #2d3748;
      text-align: center;
      width: 360px;
    }
    .count-display {
      font-size: 48px;
      font-weight: bold;
      color: #61dafb;
      margin: 20px 0;
    }
    .btn-row {
      display: flex;
      gap: 12px;
      justify-content: center;
    }
    .btn {
      padding: 10px 20px;
      border-radius: 8px;
      border: none;
      font-weight: 600;
      cursor: pointer;
      background: #61dafb;
      color: #090d16;
    }
    .btn-sec {
      background: #2d3748;
      color: #ffffff;
    }
  </style>
</head>
<body>
  <div id="root"></div>

  <script type="text/babel">
    const { useState } = React;

    function App() {
      const [count, setCount] = useState(0);

      return (
        <div className="app-container">
          <h2>React 18 in Linq Studio</h2>
          <p style={{ color: '#a0aec0', fontSize: '14px' }}>Fast single-file reactive component</p>
          <div className="count-display">{count}</div>
          <div className="btn-row">
            <button className="btn" onClick={() => setCount(count + 1)}>Increment</button>
            <button className="btn btn-sec" onClick={() => setCount(0)}>Reset</button>
          </div>
        </div>
      );
    }

    const root = ReactDOM.createRoot(document.getElementById('root'));
    root.render(<App />);
  </script>
</body>
</html>"""
        ),

        CodeSnippetTemplate(
            id = "tailwind_starter",
            title = "Tailwind CSS 3 Starter",
            category = "Web & Frontend",
            language = "HTML / Web",
            defaultFileName = "tailwind_demo.html",
            description = "Modern Tailwind Play CDN starter with responsive grid, glassmorphism card, and gradient buttons.",
            snippet = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Tailwind CSS 3 Starter</title>
  <!-- Tailwind CSS 3 CDN -->
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-900 text-slate-100 min-h-screen flex items-center justify-center p-6">
  <div class="max-w-md w-full bg-slate-800/80 backdrop-blur-md border border-slate-700 rounded-2xl p-8 shadow-2xl space-y-6">
    <div class="flex items-center space-x-3">
      <div class="w-10 h-10 bg-cyan-500 rounded-xl flex items-center justify-center font-bold text-slate-950 text-xl shadow-lg shadow-cyan-500/30">
        ⚡
      </div>
      <div>
        <h1 class="text-xl font-bold text-white">Tailwind CSS 3</h1>
        <p class="text-xs text-cyan-400">Utility-First Styling in Linq</p>
      </div>
    </div>

    <p class="text-slate-300 text-sm leading-relaxed">
      Rapidly build responsive UI designs directly inside your markup without leaving your HTML editor.
    </p>

    <div class="grid grid-cols-2 gap-3">
      <div class="bg-slate-900/60 p-3 rounded-lg border border-slate-700/50">
        <span class="text-xs text-slate-400">Flex & Grid</span>
        <div class="font-semibold text-emerald-400">Ready</div>
      </div>
      <div class="bg-slate-900/60 p-3 rounded-lg border border-slate-700/50">
        <span class="text-xs text-slate-400">Colors</span>
        <div class="font-semibold text-cyan-400">Full Palette</div>
      </div>
    </div>

    <button onclick="console.log('Tailwind button clicked!')" class="w-full bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-medium py-3 rounded-xl shadow-lg transition active:scale-95">
      Launch App
    </button>
  </div>
</body>
</html>"""
        ),

        CodeSnippetTemplate(
            id = "vue3_starter",
            title = "Vue 3 CDN Starter",
            category = "Web & Frontend",
            language = "HTML / Web",
            defaultFileName = "vue_app.html",
            description = "Vue 3 Composition API boilerplate with reactive ref, list rendering, and interactive UI.",
            snippet = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Vue 3 App</title>
  <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
  <style>
    body { background: #121824; color: #fff; font-family: sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
    #app { background: #1e2638; padding: 28px; border-radius: 12px; max-width: 360px; width: 100%; text-align: center; }
    .badge { background: #42b883; color: #000; padding: 4px 10px; border-radius: 20px; font-weight: bold; font-size: 12px; }
    button { margin-top: 16px; padding: 10px 20px; background: #42b883; border: none; border-radius: 8px; font-weight: bold; cursor: pointer; }
  </style>
</head>
<body>
  <div id="app">
    <span class="badge">Vue 3</span>
    <h2>{{ title }}</h2>
    <p>Counter: <strong>{{ count }}</strong></p>
    <button @click="increment">Add +1</button>
  </div>

  <script>
    const { createApp, ref } = Vue;
    createApp({
      setup() {
        const title = ref('Reactive Vue 3 App');
        const count = ref(0);
        const increment = () => count.value++;
        return { title, count, increment };
      }
    }).mount('#app');
  </script>
</body>
</html>"""
        ),

        CodeSnippetTemplate(
            id = "css_styles",
            title = "CSS3 Modern Variables & Reset",
            category = "Web & Frontend",
            language = "CSS",
            defaultFileName = "styles.css",
            description = "Clean modern CSS stylesheet with design tokens, CSS custom properties, and reset rules.",
            snippet = """:root {
  --color-primary: #6366f1;
  --color-accent: #10b981;
  --color-bg: #0f172a;
  --color-surface: #1e293b;
  --color-text: #f8fafc;
  --color-muted: #94a3b8;
  --radius-sm: 6px;
  --radius-md: 12px;
  --radius-lg: 20px;
}

*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  background-color: var(--color-bg);
  color: var(--color-text);
  line-height: 1.6;
}

.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
}

.card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}"""
        ),

        // ==================== JAVASCRIPT & TYPESCRIPT ====================
        CodeSnippetTemplate(
            id = "js_starter",
            title = "JavaScript ES6+ Utility",
            category = "JavaScript",
            language = "JavaScript",
            defaultFileName = "script.js",
            description = "ES6+ script with async/await, Array methods, Math functions, and structured console output.",
            snippet = """// JavaScript ES6+ Runner in Linq Studio
console.log("=== JavaScript Runner Initialized ===");

const numbers = [12, 45, 78, 23, 56, 89];
console.log("Input array:", numbers);

// Calculate sum and average
let sum = 0;
for (let i = 0; i < numbers.length; i++) {
  sum += numbers[i];
}
const average = sum / numbers.length;

console.log("Total Sum: " + sum);
console.log("Average: " + average);
console.log("Execution finished successfully!");"""
        ),

        CodeSnippetTemplate(
            id = "ts_starter",
            title = "TypeScript Interface & Service",
            category = "JavaScript",
            language = "TypeScript",
            defaultFileName = "main.ts",
            description = "TypeScript data contracts, interfaces, and typed calculation service.",
            snippet = """// TypeScript Typed Module
interface UserProfile {
  id: number;
  name: string;
  role: string;
  isActive: boolean;
}

const currentUser: UserProfile = {
  id: 101,
  name: "Linq Developer",
  role: "Full Stack Engineer",
  isActive: true
};

console.log("Active User:", currentUser.name);
console.log("Role:", currentUser.role);
console.log("Status:", currentUser.isActive ? "Online" : "Offline");"""
        ),

        // ==================== PYTHON ====================
        CodeSnippetTemplate(
            id = "python_starter",
            title = "Python 3 Application Starter",
            category = "Python",
            language = "Python",
            defaultFileName = "main.py",
            description = "Python 3 script with functions, loops, conditionals, list operations, and formatted prints.",
            snippet = """# Python 3 Application in Linq Studio
import math

def calculate_stats(data):
    total = sum(data)
    count = len(data)
    avg = total / count
    return total, avg

print("=== Python 3 Analysis Tool ===")

scores = [88, 92, 79, 95, 84, 91]
print("Scores list:", scores)

total_score, avg_score = calculate_stats(scores)
print("Total Score:", total_score)
print("Average Score:", avg_score)

# Loop demonstration
for i in range(3):
    print("Step", i + 1, "completed successfully")

print("Finished execution with code 0")"""
        ),

        // ==================== SHELL & AUTOMATION ====================
        CodeSnippetTemplate(
            id = "shell_starter",
            title = "Bash Automation Script",
            category = "Shell & Systems",
            language = "Shell / Bash",
            defaultFileName = "build.sh",
            description = "Shell automation script with environment variables, status messages, and directory operations.",
            snippet = """#!/usr/bin/env bash
# Automation Script in Linq Studio
echo "=== Starting Build Pipeline ==="

USER_NAME="Developer"
APP_NAME="LinqStudio"
VERSION="2.4.0"

echo "Deploying ${'$'}APP_NAME v${'$'}VERSION for ${'$'}USER_NAME..."
date
pwd
whoami

echo "Build succeeded with 0 errors!" """
        ),

        // ==================== JVM & NATIVE ====================
        CodeSnippetTemplate(
            id = "kotlin_starter",
            title = "Kotlin Application Starter",
            category = "Shell & Systems",
            language = "Kotlin",
            defaultFileName = "Main.kt",
            description = "Kotlin application file with fun main(), data class, and string formatting.",
            snippet = """// Kotlin Application in Linq Studio
data class Project(val name: String, val stars: Int)

fun main() {
    println("=== Kotlin Runner Initialized ===")
    val linq = Project("Linq Studio", 1450)
    println("Project: " + linq.name)
    println("Rating Stars: " + linq.stars)
    println("All systems operational!")
}"""
        ),

        CodeSnippetTemplate(
            id = "cpp_starter",
            title = "C++ 20 Modern Main",
            category = "Shell & Systems",
            language = "C++",
            defaultFileName = "main.cpp",
            description = "Modern C++ main with iostream, vectors, and std::cout.",
            snippet = """#include <iostream>
#include <vector>

int main() {
    std::cout << "=== Modern C++20 Program ===" << std::endl;
    std::vector<int> numbers = {10, 20, 30, 40, 50};
    
    std::cout << "Vector elements loaded: " << numbers.size() << std::endl;
    std::cout << "Execution completed successfully!" << std::endl;
    return 0;
}"""
        ),

        CodeSnippetTemplate(
            id = "rust_starter",
            title = "Rust Application Starter",
            category = "Shell & Systems",
            language = "Rust",
            defaultFileName = "main.rs",
            description = "Rust source file with fn main() and println! macro.",
            snippet = """// Rust in Linq Studio
fn main() {
    println!("=== Rust Application Started ===");
    let app_name = "Linq Studio";
    let version = "2.4.0";
    println!("Running {} v{}", app_name, version);
    println!("Cargo execution complete.");
}"""
        ),

        CodeSnippetTemplate(
            id = "sql_starter",
            title = "SQL Schema & Seed Table",
            category = "Data & Config",
            language = "SQL",
            defaultFileName = "schema.sql",
            description = "SQL table creation, insert queries, and select statement.",
            snippet = """-- SQLite / SQL Schema & Data Seed
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    status TEXT DEFAULT 'ACTIVE',
    role TEXT
);

INSERT INTO users (name, status, role) VALUES ('Linq Developer', 'ACTIVE', 'ADMIN');
INSERT INTO users (name, status, role) VALUES ('Sarah Connor', 'VERIFIED', 'USER');
INSERT INTO users (name, status, role) VALUES ('Neo Anderson', 'ACTIVE', 'LEAD');

SELECT * FROM users WHERE status = 'ACTIVE';"""
        ),

        CodeSnippetTemplate(
            id = "json_starter",
            title = "package.json Template",
            category = "Data & Config",
            language = "JSON",
            defaultFileName = "package.json",
            description = "Standard npm project configuration file with scripts and metadata.",
            snippet = """{
  "name": "linq-project",
  "version": "1.0.0",
  "description": "Mobile code project created with Linq Studio",
  "main": "index.html",
  "scripts": {
    "start": "serve .",
    "build": "echo 'Building...'",
    "test": "echo 'Testing...'"
  },
  "keywords": ["mobile", "ide", "editor", "linq"],
  "author": "Developer",
  "license": "MIT"
}"""
        ),

        CodeSnippetTemplate(
            id = "markdown_starter",
            title = "Project README.md",
            category = "Data & Config",
            language = "Markdown",
            defaultFileName = "README.md",
            description = "Structured GitHub-ready README with badges, features, and setup instructions.",
            snippet = """# Linq Studio Project

A fast, mobile-first code editor and development environment.

## 🚀 Features
- Real-time **Prism.js** color-coded syntax highlighting
- Instant **Web Preview** with responsive frame controls
- Built-in **JavaScript & CSS Linter** for syntax diagnostics
- Multi-file tab management and project file explorer
- Multi-language execution engine (Python, JS, Bash, C++, Rust, Kotlin, SQL)

## 🛠 Usage
1. Open or create files via the **Files** sidebar
2. Tap **Templates** to insert common boilerplates
3. Tap **Run / Preview** for instant output!"""
        )
    )

    val categories = listOf("All", "Web & Frontend", "JavaScript", "Python", "Shell & Systems", "Data & Config")

    fun getTemplatesForCategory(category: String): List<CodeSnippetTemplate> {
        if (category == "All") return allTemplates
        return allTemplates.filter { it.category == category }
    }
}
