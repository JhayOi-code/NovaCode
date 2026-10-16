package com.example.autocomplete

object AutocompleteEngine {

    fun getSuggestions(
        code: String,
        cursorPosition: Int,
        language: String
    ): List<AutocompleteSuggestion> {
        val safeCursor = cursorPosition.coerceIn(0, code.length)
        val textBeforeCursor = code.substring(0, safeCursor)
        val lastLine = textBeforeCursor.substringAfterLast('\n', textBeforeCursor)
        
        // Extract current word/token being typed
        val wordRegex = Regex("[a-zA-Z0-9_$.<\\-]+$")
        val match = wordRegex.find(lastLine)
        val currentWord = match?.value ?: ""

        val suggestions = mutableListOf<AutocompleteSuggestion>()

        // 1. First, check if AI Copilot completion is suitable (e.g. after function definition, trigger word, or typing)
        val aiSuggestion = generateAiCompletion(code, safeCursor, lastLine, language)
        if (aiSuggestion != null) {
            suggestions.add(aiSuggestion)
        }

        // 2. Language-specific context suggestions with function signatures
        val normalizedLang = language.lowercase().trim()
        val langSuggestions = when {
            normalizedLang.contains("javascript") || normalizedLang.contains("typescript") || normalizedLang == "js" || normalizedLang == "ts" ->
                getJavaScriptSuggestions(currentWord, lastLine)
            normalizedLang.contains("python") || normalizedLang == "py" ->
                getPythonSuggestions(currentWord, lastLine)
            normalizedLang.contains("html") || normalizedLang.contains("web") ->
                getHtmlSuggestions(currentWord, lastLine)
            normalizedLang.contains("css") ->
                getCssSuggestions(currentWord, lastLine)
            normalizedLang.contains("kotlin") || normalizedLang == "kt" ->
                getKotlinSuggestions(currentWord, lastLine)
            normalizedLang.contains("java") ->
                getJavaSuggestions(currentWord, lastLine)
            normalizedLang.contains("rust") ->
                getRustSuggestions(currentWord, lastLine)
            normalizedLang.contains("go") ->
                getGoSuggestions(currentWord, lastLine)
            normalizedLang.contains("sql") ->
                getSqlSuggestions(currentWord, lastLine)
            normalizedLang.contains("bash") || normalizedLang.contains("shell") ->
                getBashSuggestions(currentWord, lastLine)
            else ->
                getGenericSuggestions(currentWord)
        }

        suggestions.addAll(langSuggestions)

        // Filter and rank based on currentWord if not empty
        return if (currentWord.isEmpty()) {
            suggestions.take(8)
        } else {
            suggestions.filter {
                it.label.contains(currentWord, ignoreCase = true) ||
                it.insertText.contains(currentWord, ignoreCase = true) ||
                it.isAi
            }.sortedByDescending {
                if (it.isAi) 2
                else if (it.label.startsWith(currentWord, ignoreCase = true)) 1
                else 0
            }.take(8)
        }
    }

    private fun generateAiCompletion(
        code: String,
        cursor: Int,
        lastLine: String,
        language: String
    ): AutocompleteSuggestion? {
        val trimmed = lastLine.trim()
        val normLang = language.lowercase()

        // If user typed a function header or common pattern, generate AI continuation
        return when {
            normLang.contains("javascript") || normLang.contains("typescript") -> {
                when {
                    trimmed.startsWith("function ") && trimmed.endsWith("{") -> {
                        val fnName = trimmed.removePrefix("function ").substringBefore("(").trim()
                        AutocompleteSuggestion(
                            label = "✨ AI: Complete $fnName implementation",
                            insertText = "\n  try {\n    console.log(\"Executing $fnName\");\n    return true;\n  } catch (error) {\n    console.error(\"Error in $fnName:\", error);\n    throw error;\n  }\n}",
                            kind = SuggestionKind.AI_COMPLETION,
                            signature = "$fnName(...args): Promise<boolean> | boolean",
                            detail = "Context-aware error handled function body",
                            isAi = true
                        )
                    }
                    trimmed.contains("fetch(") -> {
                        AutocompleteSuggestion(
                            label = "✨ AI: .then(res => res.json()) pipeline",
                            insertText = ".then(res => res.json())\n  .then(data => {\n    console.log(\"Response:\", data);\n  })\n  .catch(err => console.error(err));",
                            kind = SuggestionKind.AI_COMPLETION,
                            signature = "Promise.then(onfulfilled, onrejected)",
                            detail = "Standard async API response handling",
                            isAi = true
                        )
                    }
                    trimmed.endsWith("addEventListener(") -> {
                        AutocompleteSuggestion(
                            label = "✨ AI: ('click', (event) => { ... })",
                            insertText = "'click', (event) => {\n  event.preventDefault();\n  console.log('Clicked element:', event.target);\n});",
                            kind = SuggestionKind.AI_COMPLETION,
                            signature = "addEventListener(type: string, listener: EventListener)",
                            detail = "Click handler with event prevention",
                            isAi = true
                        )
                    }
                    else -> null
                }
            }

            normLang.contains("python") -> {
                when {
                    trimmed.startsWith("def ") && trimmed.endsWith(":") -> {
                        val fnName = trimmed.removePrefix("def ").substringBefore("(").trim()
                        AutocompleteSuggestion(
                            label = "✨ AI: Docstring and return for $fnName",
                            insertText = "\n    \"\"\"Execute $fnName operation.\"\"\"\n    try:\n        # Implementation logic\n        return True\n    except Exception as e:\n        print(f\"Error in $fnName: {e}\")\n        return None",
                            kind = SuggestionKind.AI_COMPLETION,
                            signature = "def $fnName(*args, **kwargs) -> Any",
                            detail = "Production error handled python function",
                            isAi = true
                        )
                    }
                    trimmed.startsWith("if __name__") -> {
                        AutocompleteSuggestion(
                            label = "✨ AI: if __name__ == '__main__': main()",
                            insertText = " == '__main__':\n    print(\"Starting application...\")\n    main()",
                            kind = SuggestionKind.AI_COMPLETION,
                            signature = "Python entry point boilerplate",
                            detail = "Standard Python module execution guard",
                            isAi = true
                        )
                    }
                    else -> null
                }
            }

            normLang.contains("html") || normLang.contains("web") -> {
                when {
                    trimmed == "<button" || trimmed.startsWith("<button") -> {
                        AutocompleteSuggestion(
                            label = "✨ AI: <button class='btn btn-primary'>",
                            insertText = " class=\"px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition\">\n  Click Here\n</button>",
                            kind = SuggestionKind.AI_COMPLETION,
                            signature = "<button class=\"...\" onclick=\"...\">",
                            detail = "Modern styled interactive button",
                            isAi = true
                        )
                    }
                    trimmed == "<form" || trimmed.startsWith("<form") -> {
                        AutocompleteSuggestion(
                            label = "✨ AI: Complete interactive form snippet",
                            insertText = " class=\"space-y-4 p-4 border rounded-lg\">\n  <input type=\"text\" placeholder=\"Name\" class=\"w-full p-2 border rounded\" required />\n  <button type=\"submit\" class=\"px-4 py-2 bg-emerald-600 text-white rounded\">Submit</button>\n</form>",
                            kind = SuggestionKind.AI_COMPLETION,
                            signature = "<form onsubmit=\"...\">",
                            detail = "Complete form with styled input and submit",
                            isAi = true
                        )
                    }
                    else -> null
                }
            }

            normLang.contains("kotlin") -> {
                when {
                    trimmed.startsWith("fun ") && trimmed.endsWith("{") -> {
                        AutocompleteSuggestion(
                            label = "✨ AI: Complete Kotlin method block",
                            insertText = "\n    println(\"Executing method...\")\n}",
                            kind = SuggestionKind.AI_COMPLETION,
                            signature = "fun <T> execute(): T",
                            detail = "Standard Kotlin function body",
                            isAi = true
                        )
                    }
                    else -> null
                }
            }

            else -> null
        }
    }

    private fun getJavaScriptSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "console.log",
                insertText = "console.log()",
                kind = SuggestionKind.FUNCTION,
                signature = "console.log(message?: any, ...optionalParams: any[]): void",
                detail = "Prints to debugging console"
            ),
            AutocompleteSuggestion(
                label = "console.error",
                insertText = "console.error()",
                kind = SuggestionKind.FUNCTION,
                signature = "console.error(message?: any, ...optionalParams: any[]): void",
                detail = "Prints error to console"
            ),
            AutocompleteSuggestion(
                label = "document.getElementById",
                insertText = "document.getElementById(\"\")",
                kind = SuggestionKind.FUNCTION,
                signature = "document.getElementById(elementId: string): HTMLElement | null",
                detail = "Returns element reference by ID"
            ),
            AutocompleteSuggestion(
                label = "document.querySelector",
                insertText = "document.querySelector(\"\")",
                kind = SuggestionKind.FUNCTION,
                signature = "document.querySelector<E extends Element = Element>(selectors: string): E | null",
                detail = "Returns first element matching CSS selector"
            ),
            AutocompleteSuggestion(
                label = "document.querySelectorAll",
                insertText = "document.querySelectorAll(\"\")",
                kind = SuggestionKind.FUNCTION,
                signature = "document.querySelectorAll<E extends Element = Element>(selectors: string): NodeListOf<E>",
                detail = "Returns all elements matching CSS selector"
            ),
            AutocompleteSuggestion(
                label = "addEventListener",
                insertText = "addEventListener(\"click\", (e) => {\n  \n})",
                kind = SuggestionKind.METHOD,
                signature = "addEventListener(type: string, listener: EventListenerOrEventListenerObject): void",
                detail = "Attaches an event handler"
            ),
            AutocompleteSuggestion(
                label = "fetch",
                insertText = "fetch(\"\").then(res => res.json())",
                kind = SuggestionKind.FUNCTION,
                signature = "fetch(input: RequestInfo | URL, init?: RequestInit): Promise<Response>",
                detail = "Starts HTTP request"
            ),
            AutocompleteSuggestion(
                label = "setTimeout",
                insertText = "setTimeout(() => {\n  \n}, 1000)",
                kind = SuggestionKind.FUNCTION,
                signature = "setTimeout(handler: TimerHandler, timeout?: number): number",
                detail = "Sets a timer to execute after delay"
            ),
            AutocompleteSuggestion(
                label = "JSON.stringify",
                insertText = "JSON.stringify()",
                kind = SuggestionKind.FUNCTION,
                signature = "JSON.stringify(value: any, replacer?: any, space?: string | number): string",
                detail = "Converts JavaScript value to JSON string"
            ),
            AutocompleteSuggestion(
                label = "JSON.parse",
                insertText = "JSON.parse()",
                kind = SuggestionKind.FUNCTION,
                signature = "JSON.parse(text: string, reviver?: any): any",
                detail = "Parses JSON string into object"
            ),
            AutocompleteSuggestion(
                label = "Array.map",
                insertText = "map(item => item)",
                kind = SuggestionKind.METHOD,
                signature = "map<U>(callbackfn: (value: T, index: number, array: T[]) => U): U[]",
                detail = "Transforms each element"
            ),
            AutocompleteSuggestion(
                label = "Array.filter",
                insertText = "filter(item => Boolean(item))",
                kind = SuggestionKind.METHOD,
                signature = "filter(predicate: (value: T, index: number, array: T[]) => boolean): T[]",
                detail = "Filters elements matching condition"
            ),
            AutocompleteSuggestion(
                label = "const",
                insertText = "const  = ",
                kind = SuggestionKind.KEYWORD,
                signature = "const variableName: Type = value",
                detail = "Declares a block-scoped constant"
            ),
            AutocompleteSuggestion(
                label = "function",
                insertText = "function name() {\n  \n}",
                kind = SuggestionKind.KEYWORD,
                signature = "function name(...params): ReturnType",
                detail = "Declares a function"
            ),
            AutocompleteSuggestion(
                label = "async function",
                insertText = "async function name() {\n  \n}",
                kind = SuggestionKind.KEYWORD,
                signature = "async function name(...params): Promise<ReturnType>",
                detail = "Declares an asynchronous function"
            )
        )
    }

    private fun getPythonSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "print",
                insertText = "print()",
                kind = SuggestionKind.FUNCTION,
                signature = "print(*values: object, sep: str = ' ', end: str = '\\n', file: IO = None) -> None",
                detail = "Prints values to standard output"
            ),
            AutocompleteSuggestion(
                label = "len",
                insertText = "len()",
                kind = SuggestionKind.FUNCTION,
                signature = "len(__obj: Sized) -> int",
                detail = "Return number of items in a container"
            ),
            AutocompleteSuggestion(
                label = "range",
                insertText = "range()",
                kind = SuggestionKind.FUNCTION,
                signature = "range(stop: int) -> range | range(start: int, stop: int, step: int = 1)",
                detail = "Generates a sequence of numbers"
            ),
            AutocompleteSuggestion(
                label = "def",
                insertText = "def function_name():\n    ",
                kind = SuggestionKind.KEYWORD,
                signature = "def name(*args: Any, **kwargs: Any) -> Any:",
                detail = "Define a new function"
            ),
            AutocompleteSuggestion(
                label = "class",
                insertText = "class ClassName:\n    def __init__(self):\n        pass",
                kind = SuggestionKind.KEYWORD,
                signature = "class Name(BaseClass):",
                detail = "Define a new class"
            ),
            AutocompleteSuggestion(
                label = "open",
                insertText = "with open(\"\", \"r\") as f:\n    content = f.read()",
                kind = SuggestionKind.FUNCTION,
                signature = "open(file: str, mode: str = 'r', encoding: str = 'utf-8') -> IO",
                detail = "Open file context manager"
            ),
            AutocompleteSuggestion(
                label = "import json",
                insertText = "import json",
                kind = SuggestionKind.KEYWORD,
                signature = "import json # json.loads(), json.dumps()",
                detail = "Standard JSON serialization library"
            ),
            AutocompleteSuggestion(
                label = "list comprehension",
                insertText = "[x for x in items if x]",
                kind = SuggestionKind.SNIPPET,
                signature = "[expression for item in iterable if condition]",
                detail = "Constructs a new list"
            )
        )
    }

    private fun getHtmlSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "<div class=\"\">",
                insertText = "<div class=\"\">\n  \n</div>",
                kind = SuggestionKind.TAG,
                signature = "<div class=\"...\" id=\"...\">...</div>",
                detail = "Generic container element"
            ),
            AutocompleteSuggestion(
                label = "<button>",
                insertText = "<button class=\"btn\">\n  Click\n</button>",
                kind = SuggestionKind.TAG,
                signature = "<button type=\"button | submit\" onclick=\"...\">",
                detail = "Clickable button element"
            ),
            AutocompleteSuggestion(
                label = "<section>",
                insertText = "<section class=\"py-8\">\n  \n</section>",
                kind = SuggestionKind.TAG,
                signature = "<section id=\"...\">...</section>",
                detail = "Thematic grouping of content"
            ),
            AutocompleteSuggestion(
                label = "<input>",
                insertText = "<input type=\"text\" placeholder=\"\" class=\"\" />",
                kind = SuggestionKind.TAG,
                signature = "<input type=\"text | email | password\" value=\"...\" />",
                detail = "Form input element"
            ),
            AutocompleteSuggestion(
                label = "<script>",
                insertText = "<script>\n  \n</script>",
                kind = SuggestionKind.TAG,
                signature = "<script type=\"text/javascript\" src=\"...\">",
                detail = "Client-side script"
            ),
            AutocompleteSuggestion(
                label = "<style>",
                insertText = "<style>\n  \n</style>",
                kind = SuggestionKind.TAG,
                signature = "<style>/* CSS rules */</style>",
                detail = "Embedded CSS stylesheet"
            )
        )
    }

    private fun getCssSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "display: flex",
                insertText = "display: flex;\njustify-content: center;\nalign-items: center;",
                kind = SuggestionKind.PROPERTY,
                signature = "display: flex | inline-flex | grid | block | none",
                detail = "Flexbox layout container"
            ),
            AutocompleteSuggestion(
                label = "display: grid",
                insertText = "display: grid;\ngrid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\ngap: 1rem;",
                kind = SuggestionKind.PROPERTY,
                signature = "display: grid; grid-template-columns: ...",
                detail = "CSS Grid layout container"
            ),
            AutocompleteSuggestion(
                label = "background-color",
                insertText = "background-color: #;",
                kind = SuggestionKind.PROPERTY,
                signature = "background-color: <color>",
                detail = "Sets background color"
            ),
            AutocompleteSuggestion(
                label = "border-radius",
                insertText = "border-radius: 8px;",
                kind = SuggestionKind.PROPERTY,
                signature = "border-radius: <length-percentage>",
                detail = "Rounds the corners of an element's outer border"
            ),
            AutocompleteSuggestion(
                label = "box-shadow",
                insertText = "box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);",
                kind = SuggestionKind.PROPERTY,
                signature = "box-shadow: none | <shadow>#",
                detail = "Adds shadow effects around an element's frame"
            )
        )
    }

    private fun getKotlinSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "println",
                insertText = "println()",
                kind = SuggestionKind.FUNCTION,
                signature = "println(message: Any?): Unit",
                detail = "Prints the given message to the standard output stream"
            ),
            AutocompleteSuggestion(
                label = "fun",
                insertText = "fun name(): Unit {\n    \n}",
                kind = SuggestionKind.KEYWORD,
                signature = "fun <T> name(param: Type): ReturnType",
                detail = "Defines a function"
            ),
            AutocompleteSuggestion(
                label = "data class",
                insertText = "data class Name(val id: String)",
                kind = SuggestionKind.KEYWORD,
                signature = "data class Name(val property: Type)",
                detail = "Defines a class whose main purpose is to hold data"
            ),
            AutocompleteSuggestion(
                label = "listOf",
                insertText = "listOf()",
                kind = SuggestionKind.FUNCTION,
                signature = "listOf<T>(vararg elements: T): List<T>",
                detail = "Returns a new read-only list of given elements"
            )
        )
    }

    private fun getJavaSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "System.out.println",
                insertText = "System.out.println();",
                kind = SuggestionKind.FUNCTION,
                signature = "System.out.println(String x): void",
                detail = "Prints a string to stdout"
            ),
            AutocompleteSuggestion(
                label = "public static void main",
                insertText = "public static void main(String[] args) {\n    \n}",
                kind = SuggestionKind.SNIPPET,
                signature = "public static void main(String[] args)",
                detail = "Java application entry point"
            )
        )
    }

    private fun getRustSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "println!",
                insertText = "println!(\"{}\", );",
                kind = SuggestionKind.FUNCTION,
                signature = "println!(format_string, args...)",
                detail = "Prints to standard output with newline"
            ),
            AutocompleteSuggestion(
                label = "fn main",
                insertText = "fn main() {\n    \n}",
                kind = SuggestionKind.SNIPPET,
                signature = "fn main() -> ()",
                detail = "Rust program entry point"
            ),
            AutocompleteSuggestion(
                label = "let mut",
                insertText = "let mut  = ",
                kind = SuggestionKind.KEYWORD,
                signature = "let mut variable_name: Type = value;",
                detail = "Declares a mutable binding"
            )
        )
    }

    private fun getGoSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "fmt.Println",
                insertText = "fmt.Println()",
                kind = SuggestionKind.FUNCTION,
                signature = "fmt.Println(a ...any) (n int, err error)",
                detail = "Formats and writes to standard output"
            ),
            AutocompleteSuggestion(
                label = "func main",
                insertText = "func main() {\n    \n}",
                kind = SuggestionKind.SNIPPET,
                signature = "func main()",
                detail = "Go package entry function"
            )
        )
    }

    private fun getSqlSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "SELECT * FROM",
                insertText = "SELECT * FROM  WHERE 1=1;",
                kind = SuggestionKind.SNIPPET,
                signature = "SELECT column1, column2 FROM table WHERE condition",
                detail = "Queries rows from a table"
            ),
            AutocompleteSuggestion(
                label = "CREATE TABLE",
                insertText = "CREATE TABLE  (\n  id INTEGER PRIMARY KEY,\n  name TEXT NOT NULL\n);",
                kind = SuggestionKind.SNIPPET,
                signature = "CREATE TABLE table_name (column_name type constraints)",
                detail = "Creates a new database table"
            ),
            AutocompleteSuggestion(
                label = "INSERT INTO",
                insertText = "INSERT INTO  (col1) VALUES ('val');",
                kind = SuggestionKind.SNIPPET,
                signature = "INSERT INTO table (columns) VALUES (values)",
                detail = "Inserts records into a table"
            )
        )
    }

    private fun getBashSuggestions(word: String, line: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion(
                label = "echo",
                insertText = "echo \"\"",
                kind = SuggestionKind.FUNCTION,
                signature = "echo [-n] [string...]",
                detail = "Displays a line of text"
            ),
            AutocompleteSuggestion(
                label = "#!/bin/bash",
                insertText = "#!/bin/bash\nset -euo pipefail\n",
                kind = SuggestionKind.SNIPPET,
                signature = "#!/bin/bash (strict mode shebang)",
                detail = "Standard Bash shebang with strict mode"
            )
        )
    }

    private fun getGenericSuggestions(word: String): List<AutocompleteSuggestion> {
        return listOf(
            AutocompleteSuggestion("function", "function () {\n  \n}", SuggestionKind.KEYWORD, "function(...): void"),
            AutocompleteSuggestion("return", "return ", SuggestionKind.KEYWORD, "return value;"),
            AutocompleteSuggestion("if", "if () {\n  \n}", SuggestionKind.KEYWORD, "if (condition)"),
            AutocompleteSuggestion("for", "for () {\n  \n}", SuggestionKind.KEYWORD, "for (initializer; condition; increment)")
        )
    }
}
