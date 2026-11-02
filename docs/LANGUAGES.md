# Supported Languages Guide

Linq Studio supports code execution across 40+ programming languages through a 3-tier execution model:
1. **Tier 1: Embedded** (built-in offline SQLite, V8 JavaScript, HTML preview, structured formatters)
2. **Tier 2: Termux** (local native compilers/interpreters on-device)
3. **Tier 3: Remote** (Piston CE / Judge0 cloud API)

---

## Language Specifications

### 1. Python (`.py`, `.pyw`)
- **Preferred Route**: Embedded (Chaquopy) -> Termux -> Remote (Piston)
- **Termux Command**: `python3 file.py`
- **Termux Package**: `pkg install python`
- **Remote Runtime**: `python` (3.10.0+)
- **Features**: Interactive `stdin` / `input()`, `sys.stdout.write()`, `sys.stderr`, `time.sleep()` background execution.
- **Example**:
  ```python
  import sys, time
  name = input("Enter name: ")
  print(f"Hello, {name} from Python!")
  ```

### 2. JavaScript (`.js`, `.mjs`, `.cjs`)
- **Preferred Route**: Embedded (V8 WebView) -> Termux -> Remote
- **Termux Command**: `node file.js`
- **Termux Package**: `pkg install nodejs`
- **Remote Runtime**: `javascript` (Node.js 18+)
- **Features**: `console.log`, `console.warn`, `console.error`, JSON serialization, full ES2022 syntax.
- **Example**:
  ```javascript
  const greeting = "Hello, World!";
  console.log(greeting.toUpperCase());
  ```

### 3. TypeScript (`.ts`, `.mts`)
- **Preferred Route**: Termux -> Remote
- **Termux Command**: `npx tsx file.ts` (or `tsc file.ts && node file.js`)
- **Termux Package**: `pkg install nodejs`
- **Remote Runtime**: `typescript` (5.0.3)
- **Example**:
  ```typescript
  interface User { name: string; id: number }
  const user: User = { name: "Alice", id: 1 };
  console.log(`User: ${user.name} (#${user.id})`);
  ```

### 4. Java (`.java`)
- **Preferred Route**: Termux -> Remote
- **Rules**: Public class name must match file name (e.g. `Main.java` -> `public class Main`). Must contain `public static void main(String[] args)`.
- **Termux Command**: `javac Main.java && java Main`
- **Termux Package**: `pkg install openjdk-21`
- **Remote Runtime**: `java` (OpenJDK 15+)
- **Example**:
  ```java
  public class Main {
      public static void main(String[] args) {
          System.out.println("Hello from Java!");
      }
  }
  ```

### 5. Kotlin (`.kt`, `.kts`)
- **Preferred Route**: Termux -> Remote
- **Rules**: Must declare a top-level `fun main()` or `fun main(args: Array<String>)`.
- **Termux Command**: `kotlinc file.kt -include-runtime -d out.jar && java -jar out.jar`
- **Termux Package**: `pkg install kotlin openjdk-21`
- **Remote Runtime**: `kotlin` (1.8+)
- **Example**:
  ```kotlin
  fun main() {
      val langs = listOf("Kotlin", "Android", "Linq Studio")
      println("Langs: ${langs.joinToString()}")
  }
  ```

### 6. C (`.c`, `.h`)
- **Preferred Route**: Termux -> Remote
- **Rules**: Must contain `int main()`.
- **Termux Command**: `clang file.c -o a.out && ./a.out`
- **Termux Package**: `pkg install clang`
- **Remote Runtime**: `c` (GCC 10.2 / Clang)
- **Example**:
  ```c
  #include <stdio.h>
  int main(void) {
      printf("Hello from C!\n");
      return 0;
  }
  ```

### 7. C++ (`.cpp`, `.cc`, `.cxx`, `.hpp`)
- **Preferred Route**: Termux -> Remote
- **Rules**: Must contain `int main()`.
- **Termux Command**: `clang++ -std=c++17 file.cpp -o a.out && ./a.out`
- **Termux Package**: `pkg install clang`
- **Remote Runtime**: `c++` (GCC 10.2)
- **Example**:
  ```cpp
  #include <iostream>
  #include <vector>
  int main() {
      std::vector<int> nums = {1, 2, 3, 4};
      std::cout << "Sum elements: " << nums.size() << std::endl;
      return 0;
  }
  ```

### 8. C# (`.cs`)
- **Preferred Route**: Remote
- **Rules**: Must define `class` with a `Main` entry point.
- **Remote Runtime**: `csharp` (.NET 6.0+)
- **Example**:
  ```csharp
  using System;
  class Program {
      static void Main() {
          Console.WriteLine("Hello from C# on mobile!");
      }
  }
  ```

### 9. Rust (`.rs`)
- **Preferred Route**: Termux -> Remote
- **Rules**: Must contain `fn main()`.
- **Termux Command**: `rustc file.rs -o a.out && ./a.out`
- **Termux Package**: `pkg install rust`
- **Remote Runtime**: `rust` (1.68+)
- **Example**:
  ```rust
  fn main() {
      let message = "Hello from Rust!";
      println!("{}", message);
  }
  ```

### 10. Go (`.go`)
- **Preferred Route**: Termux -> Remote
- **Rules**: Must declare `package main` and `func main()`.
- **Termux Command**: `go run file.go`
- **Termux Package**: `pkg install golang`
- **Remote Runtime**: `go` (1.16+)
- **Example**:
  ```go
  package main
  import "fmt"
  func main() {
      fmt.Println("Hello from Go!")
  }
  ```

### 11. SQLite / SQL (`.sql`)
- **Preferred Route**: Embedded (Android SQLite) -> Remote
- **Features**: Executes all DDL (`CREATE`, `ALTER`) and DML (`INSERT`, `SELECT`, `UPDATE`, `DELETE`) on real in-memory SQLite database. Formats results into clean ASCII tables.
- **Example**:
  ```sql
  CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, role TEXT);
  INSERT INTO users VALUES (1, 'Alice', 'Engineer'), (2, 'Bob', 'Designer');
  SELECT * FROM users;
  ```

### 12. HTML / CSS (`.html`, `.htm`, `.css`)
- **Preferred Route**: Embedded (Android WebView & Local Dev Server)
- **Features**: Full DOM execution, CSS styling, and JavaScript runtime. Console messages automatically intercept via `WebChromeClient.onConsoleMessage` and forward to the execution output panel.

### 13. Additional Supported Runtimes (Remote Tier 3 & Termux)
- **PHP (`.php`)**: `pkg install php` | Remote `php 8.2`
- **Ruby (`.rb`)**: `pkg install ruby` | Remote `ruby 3.0`
- **Perl (`.pl`)**: `pkg install perl` | Remote `perl 5.36`
- **Lua (`.lua`)**: `pkg install lua54` | Remote `lua 5.4`
- **Bash / Shell (`.sh`)**: Built-in Termux `bash` | Remote `bash`
- **Dart (`.dart`)**: Remote `dart 2.19`
- **Swift (`.swift`)**: Remote `swift 5.3`
- **R (`.r`, `.R`)**: `pkg install r-base` | Remote `r 4.1`
- **Scala (`.scala`)**: Remote `scala 3.2`
- **Haskell (`.hs`)**: Remote `haskell 9.0`
- **Elixir (`.ex`, `.exs`)**: Remote `elixir 1.14`
- **Julia (`.jl`)**: Remote `julia 1.8`
- **Fortran (`.f90`, `.f`)**: Remote `fortran 10.2`
- **Pascal (`.pas`)**: Remote `pascal 3.2`
- **Groovy (`.groovy`)**: Remote `groovy 3.0`
