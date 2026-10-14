package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ProfileAvatarPresets
import com.example.model.UserAccount
import com.example.plugin.DiagnosticSeverity
import com.example.plugin.PluginCategory
import com.example.plugin.PluginManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PluginSystemTest {

    private lateinit var context: Context
    private lateinit var pluginManager: PluginManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        pluginManager = PluginManager(context)
    }

    @Test
    fun testDefaultPluginsCatalogLoaded() {
        val plugins = pluginManager.plugins.value
        assertTrue("Plugins should not be empty", plugins.isNotEmpty())
        assertTrue("Prettier should exist", plugins.any { it.id == "plugin_prettier" })
        assertTrue("ESLint should exist", plugins.any { it.id == "plugin_eslint" })
        assertTrue("Emmet should exist", plugins.any { it.id == "plugin_emmet" })
        assertTrue("Prettier should be active by default", pluginManager.isPluginActive("plugin_prettier"))
    }

    @Test
    fun testPrettierFormatting() {
        val messyCode = """
function test() {
const x = 10;
if (x > 5) {
console.log("hello");
}
}
""".trimIndent()

        val formatted = pluginManager.formatCode(messyCode, "js")
        assertTrue(formatted.contains("    const x = 10;"))
        assertTrue(formatted.contains("        console.log(\"hello\");"))
    }

    @Test
    fun testESLintDiagnostics() {
        val unclosedCode = """
function broken() {
    val str = "unclosed string;
}
""".trimIndent()

        val diags = pluginManager.lintCode(unclosedCode, "js")
        assertTrue("Should detect unclosed quote", diags.any { it.message.contains("Unclosed") })
    }

    @Test
    fun testEmmetAbbreviationExpander() {
        val htmlSnippet = pluginManager.expandSnippet("div.container")
        assertEquals("<div class=\"container\"></div>", htmlSnippet)

        val buttonSnippet = pluginManager.expandSnippet("btn")
        assertEquals("<button class=\"btn btn-primary\">Click Me</button>", buttonSnippet)
    }

    @Test
    fun testCodeStatisticsCalculation() {
        val sampleCode = """
line 1
line 2
line 3
""".trimIndent()

        val stats = pluginManager.calculateStats(sampleCode, "kotlin")
        assertEquals(3, stats.lines)
        assertTrue(stats.words >= 6)
        assertTrue(stats.characters > 10)
    }

    @Test
    fun testTodoTaskExtractor() {
        val codeWithTodos = """
// TODO: Implement cloud sync
val x = 10
/* FIXME: Handle null pointer */
""".trimIndent()

        val todos = pluginManager.extractTodos(codeWithTodos)
        assertEquals(2, todos.size)
        assertTrue(todos[0].contains("TODO"))
        assertTrue(todos[1].contains("FIXME"))
    }

    @Test
    fun testAddCustomPlugin() {
        val added = pluginManager.addCustomPlugin(
            name = "My Custom Linter",
            author = "Tester",
            description = "Custom testing plugin",
            category = PluginCategory.CUSTOM,
            customScript = "// script"
        )

        assertNotNull(added.id)
        assertTrue(pluginManager.plugins.value.any { it.id == added.id })
        assertTrue(pluginManager.isPluginActive(added.id))
    }

    @Test
    fun testInstallFromUrl() {
        val (success, msg) = pluginManager.installFromUrl("https://example.com/extensions/my-formatter.json")
        assertTrue("Install from URL should succeed", success)
        assertTrue(pluginManager.plugins.value.any { it.downloadUrl?.contains("my-formatter") == true })
    }

    @Test
    fun testUserProfilePresets() {
        val presets = ProfileAvatarPresets.presets
        assertEquals(8, presets.size)
        assertTrue(presets.any { it.id == "avatar_terminal" })
        assertTrue(presets.any { it.id == "avatar_ninja" })

        val account = UserAccount(name = "Alex", customTitle = "Lead Architect", avatarId = "avatar_terminal")
        assertEquals("Alex", account.name)
        assertEquals("Lead Architect", account.customTitle)
    }
}
