/**
 * Linq Studio - Comprehensive Language Guides & Cheat Sheets
 * Covers Java, Python, JavaScript, Kotlin, Bash, Web (HTML/CSS/DOM), TypeScript, C, C++, Rust, Go.
 * Compliant with Part 3 specifications:
 * - Quick reference tables
 * - Full collapsible sections
 * - Runnable snippets with "Run" and "Open in editor"
 * - Original, high-density practical cheat sheets
 */

(function (root, factory) {
  if (typeof module === 'object' && module.exports) {
    module.exports = factory();
  } else {
    root.LinqLanguageGuides = factory();
  }
}(typeof self !== 'undefined' ? self : this, function () {

  const GUIDES = {
    python: {
      id: 'python',
      name: 'Python',
      version: '3.11+',
      extension: '.py',
      badge: 'PY',
      tagline: 'Readable, expressive, general-purpose powerhouse',
      quickRef: [
        { topic: 'Print & Format', syntax: 'print(f"Count: {x}")' },
        { topic: 'List Comp', syntax: '[x * 2 for x in items if x > 0]' },
        { topic: 'Dict Comp', syntax: '{k: v for k, v in pairs}' },
        { topic: 'Type Annotations', syntax: 'def add(a: int, b: int) -> int:' },
        { topic: 'Context Mgr', syntax: 'with open("file.txt") as f:' },
        { topic: 'Async Task', syntax: 'async def fetch(): await asyncio.sleep(1)' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `Python is a high-level, dynamically typed multi-paradigm programming language celebrated for its clean, English-like syntax and vast ecosystem. It powers backend web services (FastAPI, Django), artificial intelligence and machine learning (PyTorch, TensorFlow), data engineering (Pandas), scripting, and automation.`
        },
        {
          id: 'linq-runtime',
          title: 'Running Python in Linq Studio',
          content: `Files end in <code>.py</code>. In Linq Studio, the terminal runner evaluates Python scripts using the WebAssembly Pyodide / terminal runner environment. Standard I/O (<code>print()</code>, <code>input()</code>), math, string operations, JSON, and standard libraries are fully supported.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `def main():\n    print("Hello, Linq Studio!")\n\nif __name__ == "__main__":\n    main()`,
          runnable: true
        },
        {
          id: 'variables-types',
          title: 'Variables & Built-in Types',
          code: `# Dynamic typing with optional type hints\nage: int = 21\nname: str = "Alice"\nprice: float = 19.99\nis_ready: bool = True\ntags: list[str] = ["student", "developer"]\nmetadata: dict[str, str] = {"role": "owner", "env": "prod"}\ncoords: tuple[float, float] = (37.7749, -122.4194)\nunique_ids: set[int] = {101, 102, 103}`,
          runnable: true
        },
        {
          id: 'control-flow',
          title: 'Control Flow & Pattern Matching',
          code: `status = 200\n\nmatch status:\n    case 200:\n        print("Success")\n    case 404:\n        print("Not Found")\n    case 500 | 502:\n        print("Server Error")\n    case _:\n        print("Unknown Status")\n\nfor i in range(3):\n    print(f"Index: {i}")`,
          runnable: true
        },
        {
          id: 'functions',
          title: 'Functions, Lambdas & Decorators',
          code: `from functools import wraps\nimport time\n\ndef time_it(func):\n    @wraps(func)\n    def wrapper(*args, **kwargs):\n        start = time.perf_counter()\n        res = func(*args, **kwargs)\n        print(f"{func.__name__} took {time.perf_counter() - start:.6f}s")\n        return res\n    return wrapper\n\n@time_it\ndef compute_squares(n: int) -> list[int]:\n    return [x**2 for x in range(n)]\n\nprint(compute_squares(5))`,
          runnable: true
        },
        {
          id: 'mini-examples',
          title: 'Practical Mini Examples',
          examples: [
            {
              title: '1. JSON Parsing & File I/O',
              code: `import json\n\ndata = {"project": "Linq Studio", "features": ["IDE", "AI", "Terminal"]}\nencoded = json.dumps(data, indent=2)\nparsed = json.loads(encoded)\nprint("Project:", parsed["project"])\nprint("Feature count:", len(parsed["features"]))`,
              runnable: true
            },
            {
              title: '2. Frequency Counter via collections.Counter',
              code: `from collections import Counter\n\nwords = "apple banana apple cherry date banana apple".split()\ncounts = Counter(words)\nfor word, count in counts.most_common():\n    print(f"{word}: {count}")`,
              runnable: true
            },
            {
              title: '3. Data Validation with Dataclasses',
              code: `from dataclasses import dataclass\n\n@dataclass\nclass User:\n    uid: str\n    email: str\n    is_owner: bool = False\n\nu = User(uid="usr_001", email="dev@linqstudio.com", is_owner=True)\nprint(u)`,
              runnable: true
            }
          ]
        }
      ]
    },

    javascript: {
      id: 'javascript',
      name: 'JavaScript',
      version: 'ES2024 / Node 20+',
      extension: '.js',
      badge: 'JS',
      tagline: 'Ubiquitous language of modern web and runtime systems',
      quickRef: [
        { topic: 'Async / Await', syntax: 'const data = await fetch(url).then(r => r.json());' },
        { topic: 'Destructuring', syntax: 'const { id, name, ...rest } = payload;' },
        { topic: 'Optional Chaining', syntax: 'const city = user?.address?.city ?? "Unknown";' },
        { topic: 'Array Transform', syntax: 'items.map(x => x * 2).filter(x => x > 10)' },
        { topic: 'Event Dispatch', syntax: 'window.dispatchEvent(new CustomEvent("refresh"));' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `JavaScript is the core scripting language of web browsers and universal server environments via Node.js, Bun, and Deno. It features first-class functions, prototypal inheritance, an asynchronous event loop, and extensive standard Web APIs.`
        },
        {
          id: 'linq-runtime',
          title: 'Running JavaScript in Linq Studio',
          content: `Files end in <code>.js</code>. Linq Studio evaluates JavaScript directly in the browser runtime sandbox or through the integrated Node.js terminal runner with full console I/O.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `function main() {\n  const greeting = "Hello, Linq Studio!";\n  console.log(greeting);\n}\nmain();`,
          runnable: true
        },
        {
          id: 'async-patterns',
          title: 'Async/Await & Promises',
          code: `async function fetchMockUser(id) {\n  return new Promise(resolve => {\n    setTimeout(() => resolve({ id, name: "Student Dev", role: "Developer" }), 100);\n  });\n}\n\nasync function run() {\n  const user = await fetchMockUser(42);\n  console.log("Loaded user:", user.name, "(Role: " + user.role + ")");\n}\nrun();`,
          runnable: true
        },
        {
          id: 'mini-examples',
          title: 'Practical Mini Examples',
          examples: [
            {
              title: '1. Debounce Utility',
              code: `function debounce(fn, delayMs) {\n  let timer = null;\n  return function (...args) {\n    clearTimeout(timer);\n    timer = setTimeout(() => fn.apply(this, args), delayMs);\n  };\n}\n\nconst log = debounce((msg) => console.log("Debounced:", msg), 50);\nlog("A");\nlog("B");\nlog("Final");`,
              runnable: true
            },
            {
              title: '2. Deep Object Clone via structuredClone',
              code: `const original = { studio: "Linq", settings: { theme: "dark", tabs: 4 } };\nconst cloned = structuredClone(original);\ncloned.settings.theme = "dracula";\nconsole.log("Original theme:", original.settings.theme);\nconsole.log("Cloned theme:", cloned.settings.theme);`,
              runnable: true
            }
          ]
        }
      ]
    },

    typescript: {
      id: 'typescript',
      name: 'TypeScript',
      version: '5.4+',
      extension: '.ts',
      badge: 'TS',
      tagline: 'JavaScript with syntax for types, interfaces & generics',
      quickRef: [
        { topic: 'Type Definition', syntax: 'type Result<T> = { ok: true; data: T } | { ok: false; err: string };' },
        { topic: 'Interface', syntax: 'interface DevProfile { id: string; skills: string[]; }' },
        { topic: 'Generics', syntax: 'function wrap<T>(val: T): [T] { return [val]; }' },
        { topic: 'Utility Types', syntax: 'type PartialConfig = Partial<DevProfile>;' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `TypeScript is a typed superset of JavaScript developed by Microsoft. It compiles down to plain JavaScript, providing compile-time type checking, auto-completion, refactoring, and interface contracts for enterprise applications.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `interface AppGreeting {\n  appName: string;\n  version: string;\n}\n\nfunction greet(app: AppGreeting): string {\n  return \`Welcome to \${app.appName} (v\${app.version})\`;\n}\n\nconsole.log(greet({ appName: "Linq Studio", version: "2.5.0" }));`,
          runnable: true
        }
      ]
    },

    java: {
      id: 'java',
      name: 'Java',
      version: '21 LTS',
      extension: '.java',
      badge: 'JAVA',
      tagline: 'Robust, object-oriented language for enterprise and Android',
      quickRef: [
        { topic: 'Main Method', syntax: 'public static void main(String[] args) { ... }' },
        { topic: 'Record', syntax: 'public record User(String id, String email) {}' },
        { topic: 'Streams', syntax: 'list.stream().filter(s -> s.length() > 3).toList();' },
        { topic: 'Pattern Match', syntax: 'if (obj instanceof String s) { ... }' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `Java is a class-based, object-oriented programming language designed to have as few implementation dependencies as possible ("Write Once, Run Anywhere"). It powers large-scale backend systems, cloud microservices, and native Android development.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello from Java in Linq Studio!");\n    }\n}`,
          runnable: true
        }
      ]
    },

    kotlin: {
      id: 'kotlin',
      name: 'Kotlin',
      version: '1.9 / 2.0',
      extension: '.kt',
      badge: 'KT',
      tagline: 'Concise, null-safe, modern language for JVM and Android',
      quickRef: [
        { topic: 'Null Safety', syntax: 'val name: String? = null; val len = name?.length ?: 0' },
        { topic: 'Data Class', syntax: 'data class User(val id: String, val name: String)' },
        { topic: 'Extension Func', syntax: 'fun String.isEmail(): Boolean = contains("@")' },
        { topic: 'When Expr', syntax: 'when (state) { is Loading -> ...; is Ready -> ... }' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `Kotlin is a modern, statically typed language developed by JetBrains and officially championed by Google for Android development. It is 100% interoperable with Java and eliminates null-pointer exceptions via first-class nullability.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `fun main() {\n    println("Hello from Kotlin in Linq Studio!")\n}`,
          runnable: true
        }
      ]
    },

    bash: {
      id: 'bash',
      name: 'Bash',
      version: '5.x',
      extension: '.sh',
      badge: 'SH',
      tagline: 'Standard Unix command shell and scripting language',
      quickRef: [
        { topic: 'Variables', syntax: 'NAME="Linq"; echo "Welcome $NAME"' },
        { topic: 'Conditionals', syntax: 'if [ -f "$FILE" ]; then echo "Exists"; fi' },
        { topic: 'Loops', syntax: 'for item in a b c; do echo "$item"; done' },
        { topic: 'Pipes & Filters', syntax: 'cat logs.txt | grep "ERROR" | wc -l' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `Bash (Bourne-Again SHell) is the ubiquitous Unix shell and command language for Linux, macOS, and container environments. It is indispensable for system administration, CI/CD pipelines, DevOps automation, and file management.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `#!/bin/bash\necho "Hello from Bash shell in Linq Studio!"\necho "Current date: $(date)"`,
          runnable: true
        }
      ]
    },

    web: {
      id: 'web',
      name: 'Web (HTML, CSS, DOM)',
      version: 'HTML5 / CSS3 / ES2024',
      extension: '.html',
      badge: 'WEB',
      tagline: 'Semantic markup, modern styling, and responsive DOM APIs',
      quickRef: [
        { topic: 'Semantic HTML5', syntax: '<header>, <nav>, <main>, <article>, <footer>' },
        { topic: 'CSS Flexbox', syntax: 'display: flex; align-items: center; gap: 12px;' },
        { topic: 'CSS Grid', syntax: 'display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));' },
        { topic: 'DOM Event', syntax: 'el.addEventListener("click", (e) => { ... });' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `The foundation of the World Wide Web. HTML describes structure and content semantics, CSS dictates layout, typography, animations, and color theming, and JavaScript manipulates the DOM in real time.`
        },
        {
          id: 'hello-world',
          title: 'Interactive Web Card',
          code: `<!DOCTYPE html>\n<html>\n<head>\n  <style>\n    body { font-family: sans-serif; background: #0B0F17; color: #FFF; padding: 20px; }\n    .card { background: #1E293B; border-radius: 12px; padding: 16px; border: 1px solid #334155; }\n    button { background: #00E5FF; border: none; padding: 8px 16px; border-radius: 6px; font-weight: bold; cursor: pointer; }\n  </style>\n</head>\n<body>\n  <div class="card">\n    <h2>⚡ Linq Studio Web Runner</h2>\n    <p>Live preview rendered in real time.</p>\n    <button onclick="alert('Hello from Linq Studio!')">Click Me</button>\n  </div>\n</body>\n</html>`,
          runnable: true
        }
      ]
    },

    c: {
      id: 'c',
      name: 'C',
      version: 'C17 / C23',
      extension: '.c',
      badge: 'C',
      tagline: 'Foundational systems programming language',
      quickRef: [
        { topic: 'Main', syntax: 'int main(int argc, char *argv[]) { return 0; }' },
        { topic: 'Pointers', syntax: 'int x = 10; int *p = &x; printf("%d", *p);' },
        { topic: 'Structs', syntax: 'typedef struct { int x; int y; } Point;' },
        { topic: 'Memory', syntax: 'int *arr = malloc(sizeof(int) * 10); free(arr);' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `C is one of the most influential programming languages ever created. It forms the backbone of operating system kernels (Linux, Windows), database engines, embedded microcontrollers, and graphics drivers.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `#include <stdio.h>\n\nint main(void) {\n    printf("Hello from C in Linq Studio!\\n");\n    return 0;\n}`,
          runnable: true
        }
      ]
    },

    cpp: {
      id: 'cpp',
      name: 'C++',
      version: 'C++20 / C++23',
      extension: '.cpp',
      badge: 'C++',
      tagline: 'High-performance language with zero-cost abstractions & STL',
      quickRef: [
        { topic: 'Streams', syntax: 'std::cout << "Value: " << x << std::endl;' },
        { topic: 'Vectors', syntax: 'std::vector<int> nums = {1, 2, 3}; nums.push_back(4);' },
        { topic: 'Smart Pointers', syntax: 'auto ptr = std::make_unique<MyClass>();' },
        { topic: 'Lambdas', syntax: 'auto add = [](int a, int b) { return a + b; };' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `C++ extends C with object-oriented, generic, and functional capabilities while maintaining ultra-low-level performance. It dominates game engines (Unreal), high-frequency financial trading, browser engines (Chromium), and graphics software.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `#include <iostream>\n#include <string>\n\nint main() {\n    std::string appName = "Linq Studio";\n    std::cout << "Hello from C++ in " << appName << "!" << std::endl;\n    return 0;\n}`,
          runnable: true
        }
      ]
    },

    rust: {
      id: 'rust',
      name: 'Rust',
      version: '1.75+',
      extension: '.rs',
      badge: 'RS',
      tagline: 'Memory safety without garbage collection & concurrency',
      quickRef: [
        { topic: 'Ownership & Borrow', syntax: 'let s = String::from("hello"); print_ref(&s);' },
        { topic: 'Pattern Matching', syntax: 'match res { Ok(val) => println!("{}", val), Err(e) => eprintln!("{}", e) }' },
        { topic: 'Enums & Option', syntax: 'let opt: Option<i32> = Some(42);' },
        { topic: 'Struct & Impl', syntax: 'struct User { name: String } impl User { fn new(n: &str) -> Self { ... } }' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `Rust is a systems programming language focusing on performance, type safety, and memory safety enforced at compile time through an innovative borrow checker. It is used in Linux kernel modules, web backends, WebAssembly, and cloud infrastructure.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `fn main() {\n    println!("Hello from Rust in Linq Studio!");\n}`,
          runnable: true
        }
      ]
    },

    go: {
      id: 'go',
      name: 'Go',
      version: '1.22+',
      extension: '.go',
      badge: 'GO',
      tagline: 'Simple, fast, and concurrent systems language by Google',
      quickRef: [
        { topic: 'Goroutines', syntax: 'go func() { fmt.Println("Async task") }()' },
        { topic: 'Channels', syntax: 'ch := make(chan int); ch <- 42; val := <-ch' },
        { topic: 'Error Handling', syntax: 'val, err := doWork(); if err != nil { return err }' },
        { topic: 'Structs & Slices', syntax: 'items := []string{"a", "b"}; type Config struct { Port int }' }
      ],
      sections: [
        {
          id: 'overview',
          title: 'What it is & Where it is used',
          content: `Go (Golang) is an open-source programming language created at Google that makes it easy to build simple, reliable, and efficient software. It powers Docker, Kubernetes, Terraform, and high-throughput microservices.`
        },
        {
          id: 'hello-world',
          title: 'Hello World',
          code: `package main\n\nimport "fmt"\n\nfunc main() {\n    fmt.Println("Hello from Go in Linq Studio!")\n}`,
          runnable: true
        }
      ]
    }
  };

  return {
    GUIDES,
    getGuideList: () => Object.values(GUIDES),
    getGuide: (id) => GUIDES[id] || null
  };
}));
