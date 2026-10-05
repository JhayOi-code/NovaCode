package com.example

import com.example.server.NanoHttpDevServer
import com.example.server.ServerStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class NanoHttpDevServerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var server: NanoHttpDevServer
    private lateinit var workspace: File

    @Before
    fun setup() {
        server = NanoHttpDevServer()
        workspace = tempFolder.newFolder("web_workspace")
    }

    @After
    fun tearDown() {
        server.stop()
    }

    @Test
    fun testServerServesHtmlAndCss() {
        // 1. Create index.html and style.css in workspace
        val indexHtml = File(workspace, "index.html")
        indexHtml.writeText("<!DOCTYPE html><html><body><h1>Hello Web Preview</h1></body></html>")

        val styleCss = File(workspace, "style.css")
        styleCss.writeText("body { background: #000; color: #fff; }")

        // 2. Start server
        val result = server.start(workspace, preferredPort = 8080)
        assertTrue(server.status.value.isRunning)

        val port = server.status.value.portOrNull
        assertNotNull(port)
        assertTrue(port!! > 0)

        // 3. Request index.html via HTTP
        val url = URL("http://127.0.0.1:$port/index.html")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        conn.requestMethod = "GET"

        val statusCode = conn.responseCode
        assertEquals(200, statusCode)
        val contentType = conn.contentType
        assertTrue(contentType.contains("text/html"))
        val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
        assertTrue(responseBody.contains("Hello Web Preview"))
        conn.disconnect()

        // 4. Request style.css
        val cssUrl = URL("http://127.0.0.1:$port/style.css")
        val cssConn = cssUrl.openConnection() as HttpURLConnection
        cssConn.connectTimeout = 3000
        cssConn.readTimeout = 3000
        assertEquals(200, cssConn.responseCode)
        assertTrue(cssConn.contentType.contains("text/css"))
        cssConn.disconnect()

        // 5. Verify request logs
        val logs = server.requestLogs.value
        assertTrue(logs.isNotEmpty())
        assertTrue(logs.any { it.uri == "/index.html" && it.statusCode == 200 })

        // 6. Stop server
        server.stop()
        assertFalse(server.status.value.isRunning)
        assertEquals(ServerStatus.Stopped, server.status.value)
    }

    @Test
    fun testServerReturns404ForMissingFile() {
        server.start(workspace, preferredPort = 8080)
        val port = server.status.value.portOrNull!!

        val url = URL("http://127.0.0.1:$port/non_existent.html")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000

        val statusCode = conn.responseCode
        assertEquals(404, statusCode)
        conn.disconnect()
    }
}
