package com.example.autocomplete

object AutocompleteEngine {

    private val javaSuggestions = listOf(
        AutocompleteSuggestion("System.out.println()", "System.out.println();", "method", "Prints message to standard output"),
        AutocompleteSuggestion("System.out.print()", "System.out.print();", "method", "Prints message without newline"),
        AutocompleteSuggestion("public static void main", "public static void main(String[] args) {\n    \n}", "snippet", "Java main entry point"),
        AutocompleteSuggestion("String", "String ", "type", "Java String class"),
        AutocompleteSuggestion("StringBuilder", "StringBuilder sb = new StringBuilder();", "snippet", "Mutable sequence of characters"),
        AutocompleteSuggestion("ArrayList<>()", "List<String> list = new ArrayList<>();", "snippet", "Resizable list implementation"),
        AutocompleteSuggestion("for loop (int)", "for (int i = 0; i < 10; i++) {\n    \n}", "snippet", "Standard counted for loop"),
        AutocompleteSuggestion("try-catch", "try {\n    \n} catch (Exception e) {\n    e.printStackTrace();\n}", "snippet", "Exception handling block")
    )

    private val pythonSuggestions = listOf(
        AutocompleteSuggestion("print()", "print()", "function", "Prints text to console"),
        AutocompleteSuggestion("def function()", "def my_function():\n    pass\n", "snippet", "Defines a function"),
        AutocompleteSuggestion("if __name__ == '__main__'", "if __name__ == \"__main__\":\n    main()\n", "snippet", "Main guard block"),
        AutocompleteSuggestion("import json", "import json\n", "module", "JSON encoder and decoder"),
        AutocompleteSuggestion("for item in list:", "for item in items:\n    pass\n", "snippet", "Iterate over collection")
    )

    private val jsSuggestions = listOf(
        AutocompleteSuggestion("console.log()", "console.log();", "method", "Outputs message to console"),
        AutocompleteSuggestion("function()", "function myFunction() {\n    \n}", "snippet", "Defines a JS function"),
        AutocompleteSuggestion("const [state, setState]", "const [state, setState] = useState(initial);", "hook", "React useState hook"),
        AutocompleteSuggestion("fetch()", "const res = await fetch(url);\nconst data = await res.json();", "snippet", "Fetch API request"),
        AutocompleteSuggestion("addEventListener()", "addEventListener('click', (e) => {\n    \n});", "method", "Attach event listener")
    )

    fun getSuggestions(text: String, cursor: Int, language: String): List<AutocompleteSuggestion> {
        val wordBeforeCursor = getWordBeforeCursor(text, cursor)
        val list = when (language) {
            "java" -> javaSuggestions
            "python" -> pythonSuggestions
            "javascript", "typescript" -> jsSuggestions
            else -> javaSuggestions + jsSuggestions
        }

        if (wordBeforeCursor.isEmpty()) {
            return list.take(4)
        }

        return list.filter {
            it.displayText.contains(wordBeforeCursor, ignoreCase = true) ||
            it.insertText.contains(wordBeforeCursor, ignoreCase = true)
        }.take(5)
    }

    private fun getWordBeforeCursor(text: String, cursor: Int): String {
        if (cursor <= 0 || cursor > text.length) return ""
        var start = cursor - 1
        while (start >= 0 && (text[start].isLetterOrDigit() || text[start] == '.' || text[start] == '_')) {
            start--
        }
        return text.substring(start + 1, cursor)
    }
}
