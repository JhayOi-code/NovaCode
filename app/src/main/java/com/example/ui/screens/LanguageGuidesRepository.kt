package com.example.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Individual Code Guide Section with explanation, runnable code snippet, and search tags.
 */
data class GuideSectionItem(
    val id: String,
    val title: String,
    val category: String,
    val explanation: String,
    val codeSnippet: String,
    val isRunnable: Boolean = true,
    val tags: List<String> = emptyList()
)

/**
 * Compact Quick Reference entry for syntax tables.
 */
data class QuickRefRow(
    val topic: String,
    val syntax: String,
    val description: String
)

/**
 * Full, production-grade Language Guide representation with comprehensive topics.
 */
data class ComprehensiveLanguageGuide(
    val id: String,
    val language: String,
    val iconVector: ImageVector,
    val description: String,
    val badgeColor: Color,
    val fileExtension: String,
    val version: String,
    val overview: String,
    val quickRef: List<QuickRefRow>,
    val sections: List<GuideSectionItem>,
    val starterCode: String,
    val tips: List<String>
)

object LanguageGuidesRepository {

    val allGuides: List<ComprehensiveLanguageGuide> by lazy {
        listOf(
            createJavaGuide(),
            createPythonGuide(),
            createJavaScriptGuide(),
            createTypeScriptGuide(),
            createKotlinGuide(),
            createBashGuide(),
            createWebGuide(),
            createCGuide(),
            createCppGuide(),
            createRustGuide(),
            createGoGuide()
        )
    }

    fun getGuideById(id: String): ComprehensiveLanguageGuide? {
        return allGuides.find { it.id.equals(id, ignoreCase = true) || it.language.equals(id, ignoreCase = true) }
    }

    // =========================================================================
    // 1. JAVA GUIDE (Java 21 LTS)
    // =========================================================================
    private fun createJavaGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "java",
            language = "Java",
            iconVector = Icons.Default.Coffee,
            description = "Robust, object-oriented, strongly typed language for backend systems and Android.",
            badgeColor = Color(0xFFEF4444),
            fileExtension = ".java",
            version = "Java 21 LTS",
            overview = "Java is a class-based, object-oriented language known for write-once-run-anywhere portability. Linq Studio compiles and runs Java classes with stdout/stderr redirection, full bracket pairing, and interactive input.",
            starterCode = """
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello from Java 21 on Linq Studio!");
        int sum = 0;
        for (int i = 1; i <= 5; i++) {
            sum += i;
        }
        System.out.println("Sum 1..5 = " + sum);
    }
}
""".trimIndent(),
            tips = listOf(
                "Public class name must match the .java file name exactly (e.g. Main.java).",
                "Every statement ends with a semicolon ';'.",
                "Use 'public static void main(String[] args)' as the primary execution entry point.",
                "Java 21 introduces record patterns and virtual threads for modern backend engineering."
            ),
            quickRef = listOf(
                QuickRefRow("Entry Point", "public static void main(String[] args)", "Standard application entry"),
                QuickRefRow("Output", "System.out.println(\"text\");", "Print with newline to standard output"),
                QuickRefRow("For Loop", "for (int i = 0; i < n; i++) { ... }", "Standard counted iteration"),
                QuickRefRow("Enhanced For", "for (Type item : collection) { ... }", "Iterate over arrays or iterables"),
                QuickRefRow("While Loop", "while (condition) { ... }", "Loop while condition is true"),
                QuickRefRow("Switch Expr", "int res = switch(x) { case 1 -> 10; default -> 0; };", "Java 14+ switch expression"),
                QuickRefRow("Record", "public record User(String name, int age) {}", "Immutable data carrier (Java 16+)"),
                QuickRefRow("Try-Catch", "try { ... } catch (Exception e) { ... }", "Structured exception handling")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "java_entry",
                    title = "Hello World & Main Entry Point",
                    category = "Basics",
                    explanation = "In Java, every application begins execution inside a public static void main method within a matching class.",
                    codeSnippet = """
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        System.out.printf("Version: %s%n", System.getProperty("java.version"));
    }
}
""".trimIndent(),
                    tags = listOf("main", "hello", "entry", "start", "print", "basics")
                ),
                GuideSectionItem(
                    id = "java_variables",
                    title = "Variables, Types & Constants",
                    category = "Variables",
                    explanation = "Java is strongly typed. Primitive types include int, double, boolean, and char. Use 'final' to declare constants, and 'var' for local type inference.",
                    codeSnippet = """
public class VariablesDemo {
    public static void main(String[] args) {
        int age = 25;
        double price = 19.99;
        boolean isActive = true;
        char grade = 'A';
        final String APP_NAME = "Linq Studio";
        var inferredGreeting = "Welcome, Developer!";

        System.out.println(inferredGreeting + " App: " + APP_NAME + ", Age: " + age);
    }
}
""".trimIndent(),
                    tags = listOf("variable", "type", "int", "double", "string", "var", "final", "constant")
                ),
                GuideSectionItem(
                    id = "java_control_flow",
                    title = "Conditionals: If-Else & Switch Expressions",
                    category = "Conditionals",
                    explanation = "Use standard if-else blocks or modern Java switch expressions with arrow syntax (->) for concise pattern matching.",
                    codeSnippet = """
public class ControlFlowDemo {
    public static void main(String[] args) {
        int score = 85;
        if (score >= 90) {
            System.out.println("Grade: A");
        } else if (score >= 80) {
            System.out.println("Grade: B");
        } else {
            System.out.println("Grade: C or below");
        }

        // Modern Switch Expression (Java 14+)
        int day = 3;
        String dayName = switch (day) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            default -> "Other Day";
        };
        System.out.println("Day 3 is: " + dayName);
    }
}
""".trimIndent(),
                    tags = listOf("if", "else", "switch", "condition", "branch", "control")
                ),
                GuideSectionItem(
                    id = "java_loops",
                    title = "Loops & Iteration (For, While, Do-While, Streams)",
                    category = "Loops",
                    explanation = "Java provides counted for loops, enhanced for-each loops over arrays and collections, while loops, do-while loops, and functional Stream iteration.",
                    codeSnippet = """
import java.util.List;

public class LoopsDemo {
    public static void main(String[] args) {
        // 1. Standard Counted For Loop
        System.out.println("--- Standard For Loop ---");
        for (int i = 1; i <= 4; i++) {
            System.out.println("Count: " + i);
        }

        // 2. Enhanced For-Each Loop
        System.out.println("--- Enhanced For-Each Loop ---");
        String[] fruits = {"Apple", "Banana", "Orange"};
        for (String fruit : fruits) {
            System.out.println("Fruit: " + fruit);
        }

        // 3. While Loop
        System.out.println("--- While Loop ---");
        int count = 3;
        while (count > 0) {
            System.out.println("Countdown: " + count);
            count--;
        }

        // 4. Do-While Loop (runs at least once)
        System.out.println("--- Do-While Loop ---");
        int num = 0;
        do {
            System.out.println("Executed once, num = " + num);
            num++;
        } while (num < 1);

        // 5. Streams forEach (Functional Iteration)
        System.out.println("--- Stream Loop ---");
        List.of("Alpha", "Beta", "Gamma").forEach(item -> System.out.println("Stream item: " + item));
    }
}
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "while", "do-while", "for-each", "iteration", "iterate", "stream", "repeat")
                ),
                GuideSectionItem(
                    id = "java_methods",
                    title = "Methods, Parameters & Overloading",
                    category = "Functions",
                    explanation = "Methods encapsulate logic within classes. Java supports method overloading (same name, differing parameter types or count) and variable arguments (varargs).",
                    codeSnippet = """
public class MethodsDemo {
    public static int add(int a, int b) {
        return a + b;
    }

    public static double add(double a, double b) {
        return a + b;
    }

    public static int sumAll(int... numbers) {
        int total = 0;
        for (int n : numbers) total += n;
        return total;
    }

    public static void main(String[] args) {
        System.out.println("add(5, 10): " + add(5, 10));
        System.out.println("add(2.5, 3.5): " + add(2.5, 3.5));
        System.out.println("sumAll(1, 2, 3, 4, 5): " + sumAll(1, 2, 3, 4, 5));
    }
}
""".trimIndent(),
                    tags = listOf("method", "function", "overload", "parameter", "return", "varargs")
                ),
                GuideSectionItem(
                    id = "java_collections",
                    title = "Collections: List, Set, Map & Arrays",
                    category = "Collections",
                    explanation = "The Java Collections Framework provides ArrayList for ordered lists, HashSet for unique items, and HashMap for key-value mappings.",
                    codeSnippet = """
import java.util.*;

public class CollectionsDemo {
    public static void main(String[] args) {
        // List (Ordered, duplicates allowed)
        List<String> list = new ArrayList<>(List.of("Kotlin", "Java", "Python"));
        list.add("Rust");
        System.out.println("List: " + list);

        // Set (Unique elements only)
        Set<Integer> uniqueNums = new HashSet<>(List.of(1, 2, 2, 3, 3, 4));
        System.out.println("Set size: " + uniqueNums.size() + " elements: " + uniqueNums);

        // Map (Key-Value pairs)
        Map<String, Integer> wordCounts = new HashMap<>();
        wordCounts.put("code", 42);
        wordCounts.put("build", 100);
        System.out.println("Word count for 'code': " + wordCounts.get("code"));
    }
}
""".trimIndent(),
                    tags = listOf("collection", "list", "set", "map", "array", "arraylist", "hashmap", "data structures")
                ),
                GuideSectionItem(
                    id = "java_oop",
                    title = "Object-Oriented Programming & Records",
                    category = "OOP",
                    explanation = "Java is fully object-oriented with encapsulation, inheritance, interfaces, and immutable Record data carriers.",
                    codeSnippet = """
interface Identifiable {
    String getId();
}

public class OopDemo {
    // Immutable Record (Java 16+)
    public record Developer(String name, String role) implements Identifiable {
        @Override
        public String getId() {
            return name.toLowerCase() + "-" + role.toLowerCase();
        }
    }

    public static void main(String[] args) {
        Developer dev = new Developer("Ljhay", "Lead Engineer");
        System.out.println("Developer: " + dev.name() + " (" + dev.role() + ")");
        System.out.println("Dev ID: " + dev.getId());
    }
}
""".trimIndent(),
                    tags = listOf("class", "object", "oop", "interface", "record", "inheritance", "polymorphism")
                ),
                GuideSectionItem(
                    id = "java_exceptions",
                    title = "Exception Handling: Try-Catch-Finally",
                    category = "Error Handling",
                    explanation = "Handle runtime exceptions and ensure resources are closed properly using try-catch blocks and try-with-resources.",
                    codeSnippet = """
public class ExceptionDemo {
    public static void main(String[] args) {
        try {
            int result = 10 / 2;
            System.out.println("10 / 2 = " + result);

            int bad = Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            System.err.println("Caught NumberFormatException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
        } finally {
            System.out.println("Finally block always executes.");
        }
    }
}
""".trimIndent(),
                    tags = listOf("error", "exception", "try", "catch", "finally", "throw", "handling")
                )
            )
        )
    }

    // =========================================================================
    // 2. PYTHON GUIDE (Python 3.11+)
    // =========================================================================
    private fun createPythonGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "python",
            language = "Python",
            iconVector = Icons.Default.Terminal,
            description = "Expressive, clean syntax for scripting, backend APIs, data science, and AI pipelines.",
            badgeColor = Color(0xFF3B82F6),
            fileExtension = ".py",
            version = "Python 3.11+",
            overview = "Python combines readable syntax with dynamic typing and extensive standard libraries. Linq Studio runs Python scripts with interactive input(), instant terminal output, and full library support.",
            starterCode = """
def greet(name: str) -> str:
    return f"Hello, {name}!"

if __name__ == "__main__":
    print(greet("Linq Developer"))
    for i in range(1, 6):
        print(f"Step {i}: square = {i**2}")
""".trimIndent(),
            tips = listOf(
                "Indentation defines code blocks (use standard 4 spaces).",
                "Lines do not require semicolons.",
                "Use f-strings (f'value: {var}') for fast, clean string formatting.",
                "Define executable entry points with 'if __name__ == \"__main__\":'."
            ),
            quickRef = listOf(
                QuickRefRow("Output", "print(\"text\", val)", "Standard console printing"),
                QuickRefRow("For Loop", "for item in iterable:", "Loop over list, range, or string"),
                QuickRefRow("Range Loop", "for i in range(start, stop, step):", "Counted numeric iteration"),
                QuickRefRow("While Loop", "while condition:", "Iterate while condition evaluates truthy"),
                QuickRefRow("List Comp", "[x**2 for x in nums if x > 0]", "Compact collection transformation"),
                QuickRefRow("Function", "def func(arg1, arg2='default'):", "Function declaration"),
                QuickRefRow("Dictionary", "d = {'key': 'val', 'count': 42}", "Key-value hash map"),
                QuickRefRow("Try-Except", "try: ... except Exception as e: ...", "Exception handling")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "py_entry",
                    title = "Hello World & Main Guard",
                    category = "Basics",
                    explanation = "Python executes scripts top-to-bottom. The __name__ == '__main__' guard ensures code only runs when executed directly.",
                    codeSnippet = """
def main():
    print("Hello from Python on Linq Studio!")

if __name__ == "__main__":
    main()
""".trimIndent(),
                    tags = listOf("main", "hello", "print", "entry", "start", "basics")
                ),
                GuideSectionItem(
                    id = "py_loops",
                    title = "Loops & Iteration (For, Range, While, Comprehensions)",
                    category = "Loops",
                    explanation = "Python uses 'for in' over any iterable, 'range()' for counted loops, 'enumerate()' for indices, 'zip()' to combine lists, and list/dict comprehensions.",
                    codeSnippet = """
# 1. Range-based For Loop
print("--- Range Loop ---")
for i in range(1, 5):
    print(f"Index {i}")

# 2. Iterating a List with Enumerate
print("--- Enumerate Loop ---")
languages = ["Python", "JavaScript", "Rust", "Go"]
for idx, lang in enumerate(languages, start=1):
    print(f"{idx}. {lang}")

# 3. While Loop with Break/Continue
print("--- While Loop ---")
counter = 3
while counter > 0:
    print(f"Counter: {counter}")
    counter -= 1

# 4. List & Dict Comprehensions
squares = [x**2 for x in range(1, 6)]
print("Squares comprehension:", squares)

word_lengths = {word: len(word) for word in languages}
print("Word lengths:", word_lengths)
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "while", "range", "enumerate", "comprehension", "iterate", "iteration")
                ),
                GuideSectionItem(
                    id = "py_functions",
                    title = "Functions, *args, **kwargs & Lambdas",
                    category = "Functions",
                    explanation = "Functions support positional arguments, default values, arbitrary positional arguments (*args), keyword arguments (**kwargs), and anonymous lambda expressions.",
                    codeSnippet = """
def calculate_summary(title, *values, multiplier=1.0):
    total = sum(values) * multiplier
    return f"{title}: {total}"

# Lambda function
square = lambda x: x * x

print(calculate_summary("Total Score", 10, 20, 30, multiplier=1.5))
print("Lambda square of 8:", square(8))
""".trimIndent(),
                    tags = listOf("function", "def", "args", "kwargs", "lambda", "return")
                ),
                GuideSectionItem(
                    id = "py_datastructures",
                    title = "Data Structures: Lists, Tuples, Dictionaries, Sets",
                    category = "Collections",
                    explanation = "Python features rich built-in data structures: mutable lists, immutable tuples, hash-mapped dictionaries, and unique-element sets.",
                    codeSnippet = """
# List (Mutable ordered sequence)
items = ["code", "debug", "ship"]
items.append("test")

# Dictionary (Key-Value)
user = {"name": "Ljhay", "role": "Owner", "score": 98}
user["verified"] = True

# Set (Unique elements)
unique_ids = {101, 102, 102, 103}

print("Items:", items)
print("User name:", user["name"])
print("Unique count:", len(unique_ids))
""".trimIndent(),
                    tags = listOf("list", "dict", "tuple", "set", "collection", "data structures")
                ),
                GuideSectionItem(
                    id = "py_classes",
                    title = "Classes & Modern Dataclasses",
                    category = "OOP",
                    explanation = "Use standard classes with __init__ or modern dataclasses (@dataclass) to create clean, readable data containers.",
                    codeSnippet = """
from dataclasses import dataclass

@dataclass
class Project:
    name: str
    language: str
    active: bool = True

    def summary(self) -> str:
        status = "Active" if self.active else "Paused"
        return f"{self.name} ({self.language}) - {status}"

proj = Project("Linq Studio", "Kotlin/JS")
print(proj.summary())
""".trimIndent(),
                    tags = listOf("class", "dataclass", "oop", "object", "self", "init")
                )
            )
        )
    }

    // =========================================================================
    // 3. JAVASCRIPT GUIDE (ES2024 / Node 20+)
    // =========================================================================
    private fun createJavaScriptGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "javascript",
            language = "JavaScript",
            iconVector = Icons.Default.Bolt,
            description = "High-performance dynamic language powering the modern web, servers, and scripting.",
            badgeColor = Color(0xFFFBBF24),
            fileExtension = ".js",
            version = "ES2024 / Node 20+",
            overview = "JavaScript is ubiquitous across frontend web browsers and backend Node.js engines. Linq Studio supports ES6+ modules, Promises, async/await, and full live web preview.",
            starterCode = """
const languages = ['JavaScript', 'TypeScript', 'Node.js'];
languages.forEach((lang, idx) => {
    console.log(`[${'$'}{idx + 1}] Language: ${'$'}{lang}`);
});

async function compute() {
    return new Promise(resolve => setTimeout(() => resolve('Done!'), 100));
}
compute().then(console.log);
""".trimIndent(),
            tips = listOf(
                "Prefer 'const' by default, use 'let' only when reassigning.",
                "Use strict equality '===' instead of loose '=='.",
                "Take advantage of template literals (`hello ${'$'}{name}`).",
                "Async functions return Promises and handle asynchronous I/O cleanly."
            ),
            quickRef = listOf(
                QuickRefRow("Output", "console.log('text', obj)", "Standard console log"),
                QuickRefRow("For Loop", "for (let i = 0; i < n; i++) { ... }", "Counted loop"),
                QuickRefRow("For...of", "for (const item of array) { ... }", "Iterate over iterable values"),
                QuickRefRow("For...in", "for (const key in object) { ... }", "Iterate object keys"),
                QuickRefRow("Array Methods", "arr.map(x => x * 2).filter(x => x > 5)", "Functional transformation"),
                QuickRefRow("Async/Await", "const res = await fetch(url);", "Asynchronous promise resolution"),
                QuickRefRow("Destructuring", "const { name, age } = user;", "Unpack object properties")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "js_loops",
                    title = "Loops & Iteration (For, For...of, For...in, Array Methods)",
                    category = "Loops",
                    explanation = "JavaScript provides counted for loops, for...of for iterable items, for...in for object keys, while/do-while, and high-order methods like forEach, map, filter, and reduce.",
                    codeSnippet = """
// 1. Traditional Counted For Loop
console.log('--- Traditional For Loop ---');
for (let i = 1; i <= 3; i++) {
    console.log('Index:', i);
}

// 2. For...of (Iterating Values)
console.log('--- For...of Loop ---');
const frameworks = ['React', 'Vue', 'Compose'];
for (const fw of frameworks) {
    console.log('Framework:', fw);
}

// 3. For...in (Iterating Object Keys)
console.log('--- For...in Loop ---');
const config = { host: 'localhost', port: 8080, secure: true };
for (const key in config) {
    console.log(`Key ${'$'}{key} = ${'$'}{config[key]}`);
}

// 4. Functional Array Iteration (map, filter, reduce)
console.log('--- Functional Loop ---');
const numbers = [1, 2, 3, 4, 5];
const doubledOdds = numbers
    .filter(n => n % 2 !== 0)
    .map(n => n * 2);
console.log('Doubled odds:', doubledOdds);
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "while", "for-of", "for-in", "map", "filter", "forEach", "iteration")
                ),
                GuideSectionItem(
                    id = "js_async",
                    title = "Async / Await & Promises",
                    category = "Async",
                    explanation = "Handle asynchronous workflows cleanly with Promises, async functions, and the await operator without callback nesting.",
                    codeSnippet = """
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function runPipeline() {
    console.log('Fetching data...');
    await delay(100);
    console.log('Data processed successfully.');
    return { status: 200, items: 3 };
}

runPipeline().then(result => console.log('Result:', result));
""".trimIndent(),
                    tags = listOf("async", "await", "promise", "concurrency", "then", "fetch")
                )
            )
        )
    }

    // =========================================================================
    // 4. TYPESCRIPT GUIDE (TypeScript 5.4+)
    // =========================================================================
    private fun createTypeScriptGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "typescript",
            language = "TypeScript",
            iconVector = Icons.Default.DataObject,
            description = "Typed superset of JavaScript adding interfaces, generics, and compile-time type safety.",
            badgeColor = Color(0xFF38BDF8),
            fileExtension = ".ts",
            version = "TypeScript 5.4+",
            overview = "TypeScript adds static typing to JavaScript. It enables robust refactoring, catches bugs at build time, and compiles to clean JavaScript.",
            starterCode = """
interface User {
    id: number;
    username: string;
    roles: string[];
}

function printUser(user: User): void {
    console.log(`User #${'$'}{user.id}: ${'$'}{user.username} (${'$'}{user.roles.join(', ')})`);
}

const dev: User = { id: 1, username: 'ljhay', roles: ['admin', 'owner'] };
printUser(dev);
""".trimIndent(),
            tips = listOf(
                "Use interfaces or type aliases to define domain contracts.",
                "Leverage discriminated unions for clean pattern matching.",
                "Avoid 'any' whenever possible; prefer 'unknown' with type narrowing."
            ),
            quickRef = listOf(
                QuickRefRow("Interface", "interface Props { title: string; }", "Object shape contract"),
                QuickRefRow("Union Type", "type Status = 'loading' | 'success' | 'error';", "Restricted string union"),
                QuickRefRow("Generics", "function getFirst<T>(arr: T[]): T { ... }", "Reusable type parameter"),
                QuickRefRow("Loops", "for (const item of list as string[])", "Type-safe iteration")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "ts_loops",
                    title = "Type-Safe Loops & Iteration",
                    category = "Loops",
                    explanation = "TypeScript enforces type safety across all standard JavaScript loop structures including arrays, tuples, and custom iterators.",
                    codeSnippet = """
interface Task {
    id: number;
    title: string;
    completed: boolean;
}

const tasks: Task[] = [
    { id: 1, title: 'Write Tests', completed: true },
    { id: 2, title: 'Refactor UI', completed: false }
];

// Typed for...of loop
for (const task of tasks) {
    const status = task.completed ? '✓' : '✗';
    console.log(`[${'$'}{status}] Task #${'$'}{task.id}: ${'$'}{task.title}`);
}
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "while", "iteration", "types", "interface")
                )
            )
        )
    }

    // =========================================================================
    // 5. KOTLIN GUIDE (Kotlin 2.0+)
    // =========================================================================
    private fun createKotlinGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "kotlin",
            language = "Kotlin",
            iconVector = Icons.Default.DataObject,
            description = "Modern, expressive multiplatform language with first-class null-safety and coroutines.",
            badgeColor = Color(0xFFA78BFA),
            fileExtension = ".kt",
            version = "Kotlin 2.0+",
            overview = "Kotlin is concise, safe, and seamlessly interoperable with Java. It powers Android development and multiplatform backends with elegant coroutines and functional collections.",
            starterCode = """
data class Developer(val name: String, val role: String)

fun main() {
    val dev = Developer("Ljhay", "System Owner")
    println("Welcome to Linq Studio, ${'$'}{dev.name}!")
    
    val items = listOf("Compose", "Coroutines", "Flow")
    items.forEachIndexed { idx, item ->
        println("${'$'}{idx + 1}. Learning ${'$'}item")
    }
}
""".trimIndent(),
            tips = listOf(
                "Null-safety is enforced at compile time (? vs !!)",
                "val declares immutable read-only references; var declares mutable variables.",
                "Top-level 'fun main()' requires no enclosing class.",
                "Use 'when' expressions instead of traditional switch statements."
            ),
            quickRef = listOf(
                QuickRefRow("Output", "println(\"text\")", "Standard print with newline"),
                QuickRefRow("For Loop", "for (i in 1..10 step 2) { ... }", "Closed range iteration"),
                QuickRefRow("Until Loop", "for (i in 0 until count) { ... }", "Half-open range iteration"),
                QuickRefRow("DownTo Loop", "for (i in 10 downTo 1) { ... }", "Reverse range iteration"),
                QuickRefRow("When Expr", "val res = when(x) { 1 -> \"A\"; else -> \"B\" }", "Pattern matching expression"),
                QuickRefRow("Data Class", "data class User(val id: String)", "Auto equals/hashcode/copy")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "kt_loops",
                    title = "Loops & Iteration (Ranges, Steps, DownTo, Collections)",
                    category = "Loops",
                    explanation = "Kotlin provides expressive range operators (.. and until), downTo, step, repeat(n), and rich collection iterators like forEach and map.",
                    codeSnippet = """
fun main() {
    // 1. Closed Range Loop (1..5 inclusive)
    println("--- 1..5 Closed Range ---")
    for (i in 1..5) {
        print("${'$'}i ")
    }
    println()

    // 2. Until Loop (0 until 5 exclusive of 5)
    println("--- 0 until 5 ---")
    for (i in 0 until 5) {
        print("${'$'}i ")
    }
    println()

    // 3. Reverse with step
    println("--- 10 downTo 2 step 2 ---")
    for (i in 10 downTo 2 step 2) {
        print("${'$'}i ")
    }
    println()

    // 4. Collection Iteration with Index
    println("--- Collection Iteration ---")
    val tools = listOf("Git", "Gradle", "Clang", "Cargo")
    for ((index, tool) in tools.withIndex()) {
        println("Tool #${'$'}{index + 1}: ${'$'}tool")
    }

    // 5. repeat(count)
    println("--- repeat(3) ---")
    repeat(3) { runIdx ->
        println("Pulse ${'$'}{runIdx + 1}")
    }
}
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "while", "ranges", "step", "downto", "repeat", "iteration")
                )
            )
        )
    }

    // =========================================================================
    // 6. BASH / SHELL GUIDE (Bash 5.2)
    // =========================================================================
    private fun createBashGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "bash",
            language = "Bash",
            iconVector = Icons.Default.Terminal,
            description = "Unix shell scripting for task automation, pipelines, and terminal operations.",
            badgeColor = Color(0xFF10B981),
            fileExtension = ".sh",
            version = "Bash 5.2 / POSIX Shell",
            overview = "Bash is the industry standard for command-line automation and process orchestration. Linq Studio executes shell scripts inside the integrated terminal.",
            starterCode = """
#!/bin/bash
set -euo pipefail

echo "=== Linq Studio Shell Automation ==="
echo "Date: $(date)"
echo "Current directory: $(pwd)"

for item in "Source" "Build" "Test"; do
    echo "Processing stage: ${'$'}item"
done
""".trimIndent(),
            tips = listOf(
                "Always start with #!/bin/bash shebang.",
                "Use 'set -euo pipefail' for safe error handling.",
                "Wrap variables in double quotes (\"${'$'}VAR\") to prevent word splitting.",
                "Use (( i++ )) for arithmetic and [[ condition ]] for string tests."
            ),
            quickRef = listOf(
                QuickRefRow("For in", "for f in *.txt; do ... done", "Glob pattern iteration"),
                QuickRefRow("C-Style For", "for ((i=0; i<n; i++)); do ... done", "Counted arithmetic loop"),
                QuickRefRow("While Read", "while IFS= read -r line; do ... done", "Line-by-line file reading"),
                QuickRefRow("If Test", "if [[ -f \"${'$'}file\" ]]; then ... fi", "File existence check")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "bash_loops",
                    title = "Loops & Iteration (For In, C-Style For, While Read)",
                    category = "Loops",
                    explanation = "Bash supports iterating over list items, traditional C-style arithmetic loops, and while loops reading file streams.",
                    codeSnippet = """
#!/bin/bash

# 1. Standard List Iteration
echo "--- For In Loop ---"
for lang in "Java" "Python" "Rust" "Go"; do
    echo "Compiled language: ${'$'}lang"
done

# 2. C-Style Arithmetic Loop
echo "--- C-Style For Loop ---"
for ((i=1; i<=4; i++)); do
    echo "Step ${'$'}i of 4"
done

# 3. While Loop with Condition
echo "--- While Loop ---"
count=3
while [ ${'$'}count -gt 0 ]; do
    echo "Timer countdown: ${'$'}count"
    count=$((count - 1))
done
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "while", "iteration", "bash", "shell", "read")
                )
            )
        )
    }

    // =========================================================================
    // 7. WEB GUIDE (HTML5, CSS3, DOM JS)
    // =========================================================================
    private fun createWebGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "web",
            language = "Web (HTML/CSS/JS)",
            iconVector = Icons.Default.Language,
            description = "Full-stack frontend web architecture with live dev server hosting and preview.",
            badgeColor = Color(0xFF00E5FF),
            fileExtension = ".html",
            version = "HTML5 & CSS3",
            overview = "Build responsive web applications with semantic HTML5, modern flexbox/grid layouts, CSS custom properties, and interactive JavaScript. Linq Studio hosts folders on localhost:8080 with live reload.",
            starterCode = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Linq Studio Web Demo</title>
  <style>
    body { font-family: sans-serif; background: #0F172A; color: #FFF; padding: 20px; }
    .btn { background: #00E5FF; color: #000; border: none; padding: 10px 18px; border-radius: 8px; font-weight: bold; cursor: pointer; }
  </style>
</head>
<body>
  <h1>Interactive Web Interface</h1>
  <button class="btn" onclick="alert('Hello from Web Preview!')">Click Me</button>
</body>
</html>
""".trimIndent(),
            tips = listOf(
                "Use the Local Dev Server card on Home to serve any folder on localhost:8080.",
                "CSS Grid and Flexbox make responsive layouts effortless.",
                "Preview works both online and offline with full console logging."
            ),
            quickRef = listOf(
                QuickRefRow("Doctype", "<!DOCTYPE html>", "HTML5 declaration"),
                QuickRefRow("Viewport", "<meta name=\"viewport\" content=\"width=device-width\">", "Mobile responsive scaling"),
                QuickRefRow("Flexbox", "display: flex; justify-content: center;", "1D flexible alignment"),
                QuickRefRow("CSS Grid", "display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));", "2D layout")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "web_dom_loops",
                    title = "DOM Manipulation & Dynamic Rendering Loops",
                    category = "Loops",
                    explanation = "Use JavaScript within HTML documents to dynamically render UI elements and cards using loops.",
                    codeSnippet = """
<ul id="itemList"></ul>

<script>
  const items = ['Dashboard', 'Editor', 'Terminal', 'Settings'];
  const listEl = document.getElementById('itemList');

  // Looping to generate dynamic DOM elements
  items.forEach(item => {
    const li = document.createElement('li');
    li.textContent = `Nav: ${'$'}{item}`;
    listEl.appendChild(li);
  });
</script>
""".trimIndent(),
                    tags = listOf("loop", "looping", "dom", "html", "javascript", "render", "iteration")
                )
            )
        )
    }

    // =========================================================================
    // 8. C GUIDE (C17 / Clang)
    // =========================================================================
    private fun createCGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "c",
            language = "C",
            iconVector = Icons.Default.Memory,
            description = "Low-level systems programming language with explicit memory control and high speed.",
            badgeColor = Color(0xFF64748B),
            fileExtension = ".c",
            version = "C17 / Clang 18",
            overview = "C is the foundation of operating systems, kernels, and embedded platforms. Linq Studio compiles C source with clang/lld, showing real compiler diagnostics and memory structures.",
            starterCode = """
#include <stdio.h>

int main(void) {
    printf("Hello from C17 on Linq Studio!\n");
    for (int i = 1; i <= 5; i++) {
        printf("Index %d: Square = %d\n", i, i * i);
    }
    return 0;
}
""".trimIndent(),
            tips = listOf(
                "Every C program must define 'int main(void)' or 'int main(int argc, char *argv[])'.",
                "Explicitly manage heap memory allocated via malloc() using free().",
                "Format specifiers: %d for int, %f for double, %s for string, %p for pointer."
            ),
            quickRef = listOf(
                QuickRefRow("Entry", "int main(void) { return 0; }", "Program entry"),
                QuickRefRow("Output", "printf(\"fmt %d\\n\", val);", "Formatted stdout output"),
                QuickRefRow("For Loop", "for (int i = 0; i < n; i++) { ... }", "Counted iteration"),
                QuickRefRow("While Loop", "while (condition) { ... }", "Condition-driven loop"),
                QuickRefRow("Pointers", "int *ptr = &val; *ptr = 10;", "Direct address referencing")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "c_loops",
                    title = "Loops & Iteration in C (For, While, Do-While, Break, Continue)",
                    category = "Loops",
                    explanation = "C provides counted for loops with index initializers, while loops, and do-while loops with break/continue control.",
                    codeSnippet = """
#include <stdio.h>

int main(void) {
    // 1. Counted For Loop
    printf("--- For Loop ---\n");
    for (int i = 1; i <= 4; i++) {
        printf("Count: %d\n", i);
    }

    // 2. While Loop
    printf("--- While Loop ---\n");
    int counter = 3;
    while (counter > 0) {
        printf("Decrement: %d\n", counter);
        counter--;
    }

    // 3. Do-While Loop
    printf("--- Do-While Loop ---\n");
    int val = 0;
    do {
        printf("Do-while executed, val = %d\n", val);
        val++;
    } while (val < 1);

    return 0;
}
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "while", "do-while", "iteration", "c")
                )
            )
        )
    }

    // =========================================================================
    // 9. C++ GUIDE (C++20)
    // =========================================================================
    private fun createCppGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "cpp",
            language = "C++",
            iconVector = Icons.Default.Speed,
            description = "Powerful object-oriented and generic systems programming language with modern RAII.",
            badgeColor = Color(0xFF0284C7),
            fileExtension = ".cpp",
            version = "C++20",
            overview = "C++ pairs high performance with rich abstractions: classes, templates, STL algorithms, and smart pointers. Linq Studio compiles C++20 with full standard library support.",
            starterCode = """
#include <iostream>
#include <vector>
#include <string>

int main() {
    std::cout << "Hello from Modern C++20 on Linq Studio!\n";
    std::vector<std::string> tools = {"Clang", "LLD", "CMake", "Ninja"};
    for (const auto& tool : tools) {
        std::cout << "Tool: " << tool << "\n";
    }
    return 0;
}
""".trimIndent(),
            tips = listOf(
                "Use std::vector and std::string instead of raw C-style arrays/pointers.",
                "Embrace RAII and smart pointers (std::unique_ptr) to eliminate memory leaks.",
                "Use 'auto' and range-based for loops for cleaner, safer iteration."
            ),
            quickRef = listOf(
                QuickRefRow("Output", "std::cout << \"text\" << std::endl;", "Standard stream output"),
                QuickRefRow("Range-For", "for (const auto& item : container)", "Range-based for loop"),
                QuickRefRow("Vector", "std::vector<int> nums = {1, 2, 3};", "Dynamic contiguous array"),
                QuickRefRow("Lambda", "auto fn = [](int x) { return x * 2; };", "Anonymous closure")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "cpp_loops",
                    title = "Loops & Range-Based Iteration in C++",
                    category = "Loops",
                    explanation = "C++ supports modern range-based for loops, index-based for loops, while loops, and STL algorithm iteration.",
                    codeSnippet = """
#include <iostream>
#include <vector>

int main() {
    std::vector<int> numbers = {10, 20, 30, 40, 50};

    // 1. Range-based For Loop (By const reference)
    std::cout << "--- Range-based For ---\n";
    for (const auto& num : numbers) {
        std::cout << "Value: " << num << "\n";
    }

    // 2. Traditional Index For Loop
    std::cout << "--- Index For ---\n";
    for (size_t i = 0; i < numbers.size(); ++i) {
        std::cout << "Index [" << i << "] = " << numbers[i] << "\n";
    }

    return 0;
}
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "while", "range-for", "iteration", "stl", "cpp")
                )
            )
        )
    }

    // =========================================================================
    // 10. RUST GUIDE (Rust 1.78+)
    // =========================================================================
    private fun createRustGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "rust",
            language = "Rust",
            iconVector = Icons.Default.Memory,
            description = "Memory-safe systems programming language with zero-cost abstractions and no garbage collection.",
            badgeColor = Color(0xFFF97316),
            fileExtension = ".rs",
            version = "Rust 1.78+",
            overview = "Rust ensures memory safety and thread safety at compile time through ownership, borrowing, and lifetimes. Linq Studio compiles Rust programs via rustc/cargo with real error diagnostics.",
            starterCode = """
fn main() {
    println!("Hello from Rust on Linq Studio!");
    let languages = ["Rust", "Kotlin", "TypeScript"];
    for (idx, lang) in languages.iter().enumerate() {
        println!("{}. Language: {}", idx + 1, lang);
    }
}
""".trimIndent(),
            tips = listOf(
                "Variables are immutable by default; use 'let mut' to make them mutable.",
                "Ownership guarantees eliminate use-after-free and data races.",
                "Pattern matching with 'match' must be exhaustive."
            ),
            quickRef = listOf(
                QuickRefRow("Output", "println!(\"fmt {}\", val);", "Standard macro print"),
                QuickRefRow("For Loop", "for item in &vec { ... }", "Iterate over borrowed collection"),
                QuickRefRow("Range Loop", "for i in 0..10 { ... }", "Half-open range iteration"),
                QuickRefRow("Infinite Loop", "loop { if done { break; } }", "Unconditional loop with break value"),
                QuickRefRow("Match", "match val { Ok(v) => v, Err(e) => ... }", "Pattern matching")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "rust_loops",
                    title = "Loops & Iteration in Rust (For, While, Loop, Iterators)",
                    category = "Loops",
                    explanation = "Rust features three looping constructs: 'loop' (infinite loop with return expression), 'while', and 'for .. in' over iterators.",
                    codeSnippet = """
fn main() {
    // 1. Range-based For Loop (0..4)
    println!("--- Range For Loop ---");
    for i in 0..4 {
        println!("Index: {}", i);
    }

    // 2. Iterating a Vector
    println!("--- Borrowed Vector Loop ---");
    let fruits = vec!["Apple", "Banana", "Orange"];
    for fruit in &fruits {
        println!("Fruit: {}", fruit);
    }

    // 3. Loop with Return Value
    println!("--- Loop Expression ---");
    let mut counter = 0;
    let result = loop {
        counter += 1;
        if counter == 5 {
            break counter * 10;
        }
    };
    println!("Loop break value: {}", result);
}
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "while", "iterator", "ranges", "rust")
                )
            )
        )
    }

    // =========================================================================
    // 11. GO GUIDE (Go 1.22)
    // =========================================================================
    private fun createGoGuide(): ComprehensiveLanguageGuide {
        return ComprehensiveLanguageGuide(
            id = "go",
            language = "Go",
            iconVector = Icons.Default.Speed,
            description = "Clean, compiled language engineered by Google for concurrent distributed network services.",
            badgeColor = Color(0xFF00ADD8),
            fileExtension = ".go",
            version = "Go 1.22",
            overview = "Go delivers lightning-fast compilation, built-in garbage collection, and lightweight goroutines with channels. Linq Studio compiles and runs Go applications with live output streaming.",
            starterCode = """
package main

import (
    "fmt"
)

func main() {
    fmt.Println("Hello from Go 1.22 on Linq Studio!")
    services := []string{"Auth", "Compiler", "Telemetry"}
    for idx, s := range services {
        fmt.Printf("[%d] Service: %s\n", idx+1, s)
    }
}
""".trimIndent(),
            tips = listOf(
                "Go has only ONE looping keyword: 'for'.",
                "Unused imports and variables trigger compile errors.",
                "Goroutines are spawned trivially with 'go myFunction()'.",
                "Explicit error checks ('if err != nil') are standard Go idiom."
            ),
            quickRef = listOf(
                QuickRefRow("Output", "fmt.Println(\"text\")", "Print to standard out"),
                QuickRefRow("Counted For", "for i := 0; i < n; i++ { ... }", "Traditional 3-component loop"),
                QuickRefRow("While-style For", "for condition { ... }", "Condition-only while loop"),
                QuickRefRow("Range For", "for idx, val := range slice { ... }", "Iterate slices, arrays, or maps"),
                QuickRefRow("Integer Range", "for i := range 10 { ... }", "Go 1.22 integer loop"),
                QuickRefRow("Goroutine", "go doWork()", "Spawn lightweight thread")
            ),
            sections = listOf(
                GuideSectionItem(
                    id = "go_loops",
                    title = "Loops & Iteration in Go (The Unified 'for' Keyword)",
                    category = "Loops",
                    explanation = "Go replaces while, do-while, and foreach with a single, highly flexible 'for' keyword: 3-component, while-style condition, range over slices/maps, and Go 1.22 integer range.",
                    codeSnippet = """
package main

import "fmt"

func main() {
    // 1. Traditional 3-Component Loop
    fmt.Println("--- 3-Component For Loop ---")
    for i := 1; i <= 3; i++ {
        fmt.Printf("Count: %d\n", i)
    }

    // 2. Condition-Only Loop (Go's 'while')
    fmt.Println("--- Condition-Only Loop ---")
    countdown := 3
    for countdown > 0 {
        fmt.Printf("Countdown: %d\n", countdown)
        countdown--
    }

    // 3. Slice Range Loop
    fmt.Println("--- Range Loop ---")
    servers := []string{"web-01", "api-02", "db-03"}
    for idx, s := range servers {
        fmt.Printf("Server #%d: %s\n", idx+1, s)
    }

    // 4. Go 1.22 Integer Range Loop
    fmt.Println("--- Go 1.22 range 4 ---")
    for i := range 4 {
        fmt.Printf("Integer range: %d\n", i)
    }
}
""".trimIndent(),
                    tags = listOf("loop", "looping", "for", "range", "while", "iteration", "go", "golang")
                )
            )
        )
    }
}
