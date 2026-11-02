# Adding a New Language to Linq Studio

Adding support for a new language in Linq Studio takes only three quick steps.

---

## Step 1: Register in `RemoteLanguageConfig.kt`

Open `app/src/main/java/com/example/runner/RemoteLanguageConfig.kt` and add your language entry to `REMOTE_LANGUAGES`:

```kotlin
"zig" to RemoteLanguageSpec(
    pistonLanguage = "zig",
    defaultVersion = "0.10.1",
    aliases = listOf("zig"),
    fileExtension = "zig",
    compileCommand = "zig build-exe file.zig && ./file",
    termuxBinary = "zig",
    termuxPackage = "zig"
)
```

Also add the extension mapping inside `resolveLanguageForFile()`:

```kotlin
"zig" -> "zig"
```

---

## Step 2: (Optional) Add Structural Validation Rules

If your language has strict entry point requirements (like Java `main`, C `int main()`, or Go `package main`), add a rule in `app/src/main/java/com/example/runner/LanguageValidationRules.kt`:

```kotlin
"zig" -> {
    if (!code.contains("pub fn main(")) {
        ValidationResult.Invalid("Zig file must declare 'pub fn main(...)'.")
    } else {
        ValidationResult.Valid
    }
}
```

---

## Step 3: (Optional) Add Termux Tool Checker Mapping

If the compiler is installable inside Termux via `pkg install`, add it to `app/src/main/java/com/example/runner/termux/TermuxToolChecker.kt`:

```kotlin
TermuxToolDefinition(
    binaryName = "zig",
    language = "Zig",
    pkgInstallCommand = "pkg install zig"
)
```

---

## Step 4: Verify with Unit Tests

Add a test case in `app/src/test/java/com/example/runner/RunnerRegistryTest.kt`:

```kotlin
@Test
fun testZigResolution() {
    assertEquals("zig", RemoteLanguageConfig.resolveLanguageForFile("main.zig"))
}
```

Run test suite:
```bash
gradle :app:testDebugUnitTest
```
Your new language is now completely hooked into the editor, syntax highlighter, Termux runner, and Remote Piston runner!
