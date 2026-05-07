package com.wikidoc.core.util

import org.junit.Assert.*
import org.junit.Test

class MarkdownUtilsTest {

    @Test
    fun `parseToHtml returns empty string for empty input`() {
        val result = MarkdownUtils.parseToHtml("")
        assertEquals("", result)
    }

    @Test
    fun `parseToHtml returns empty string for whitespace input`() {
        val result = MarkdownUtils.parseToHtml("   ")
        assertEquals("", result)
    }

    @Test
    fun `parseToHtml correctly parses headings`() {
        val markdown = "# Test Heading"
        val html = MarkdownUtils.parseToHtml(markdown)

        assertTrue(html.contains("<h1"))
        assertTrue(html.contains("Test Heading"))
    }

    @Test
    fun `parseToHtml correctly parses bold text`() {
        val markdown = "This is **bold text**"
        val html = MarkdownUtils.parseToHtml(markdown)

        assertTrue(html.contains("<strong>bold text</strong>"))
    }

    @Test
    fun `parseToHtml correctly parses italic text`() {
        val markdown = "This is *italic text*"
        val html = MarkdownUtils.parseToHtml(markdown)

        assertTrue(html.contains("<em>italic text</em>"))
    }

    @Test
    fun `parseToHtml correctly parses links`() {
        val markdown = "[Link Text](https://example.com)"
        val html = MarkdownUtils.parseToHtml(markdown)

        assertTrue(html.contains("href=\"https://example.com\""))
        assertTrue(html.contains("Link Text"))
    }

    @Test
    fun `parseToHtml correctly parses code blocks`() {
        val markdown = """
            ```kotlin
            fun hello() {
                println("Hello")
            }
            ```
        """.trimIndent()
        val html = MarkdownUtils.parseToHtml(markdown)

        assertTrue(html.contains("<pre><code"))
        assertTrue(html.contains("fun hello()"))
    }

    @Test
    fun `parseToHtml correctly parses lists`() {
        val markdown = """
            - Item 1
            - Item 2
            - Item 3
        """.trimIndent()
        val html = MarkdownUtils.parseToHtml(markdown)

        assertTrue(html.contains("<ul>"))
        assertTrue(html.contains("<li>"))
        assertTrue(html.contains("Item 1"))
    }

    @Test
    fun `parseToHtml correctly parses tables`() {
        val markdown = """
            | Col1 | Col2 |
            |------|------|
            | Cell1 | Cell2 |
        """.trimIndent()
        val html = MarkdownUtils.parseToHtml(markdown)

        assertTrue(html.contains("<table>"))
        assertTrue(html.contains("<th>"))
        assertTrue(html.contains("<td>"))
    }

    @Test
    fun `parseToHtml includes HTML template structure`() {
        val markdown = "# Heading"
        val html = MarkdownUtils.parseToHtml(markdown)

        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("<html>"))
        assertTrue(html.contains("<head>"))
        assertTrue(html.contains("<body>"))
        assertTrue(html.contains("mermaid.min.js"))
    }

    @Test
    fun `parseToHtml correctly handles mermaid code blocks`() {
        val markdown = """
            ```mermaid
            graph TD
                A --> B
            ```
        """.trimIndent()
        val html = MarkdownUtils.parseToHtml(markdown)

        assertTrue(html.contains("class=\"mermaid\""))
        assertTrue(html.contains("graph TD"))
    }

    @Test
    fun `insertMarkdownSyntax inserts bold syntax`() {
        val text = "Hello World"
        val (result, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
            text = text,
            selectionStart = 0,
            selectionEnd = 5,
            syntax = MarkdownSyntax.BOLD
        )

        assertTrue(result.contains("**Hello**"))
        assertEquals(9, cursorPos)
    }

    @Test
    fun `insertMarkdownSyntax inserts italic syntax`() {
        val text = "Hello World"
        val (result, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
            text = text,
            selectionStart = 6,
            selectionEnd = 11,
            syntax = MarkdownSyntax.ITALIC
        )

        assertTrue(result.contains("Hello *World*"))
        assertEquals(13, cursorPos)
    }

    @Test
    fun `insertMarkdownSyntax inserts link syntax`() {
        val text = "click here"
        val (result, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
            text = text,
            selectionStart = 0,
            selectionEnd = 5,
            syntax = MarkdownSyntax.LINK
        )

        assertTrue(result.contains("[click](url)"))
        assertTrue(result.contains(" here"))
        assertEquals(12, cursorPos)
    }

    @Test
    fun `insertMarkdownSyntax inserts mermaid template`() {
        val text = ""
        val (result, _) = MarkdownUtils.insertMarkdownSyntax(
            text = text,
            selectionStart = 0,
            selectionEnd = 0,
            syntax = MarkdownSyntax.MERMAID
        )

        assertTrue(result.contains("```mermaid"))
        assertTrue(result.contains("graph TD"))
        assertTrue(result.contains("A[Start]"))
    }

    @Test
    fun `insertMarkdownSyntax inserts heading syntax`() {
        val text = "Heading text"
        val (result, _) = MarkdownUtils.insertMarkdownSyntax(
            text = text,
            selectionStart = 0,
            selectionEnd = 0,
            syntax = MarkdownSyntax.HEADING_1
        )

        assertTrue(result.startsWith("# "))
    }

    @Test
    fun `insertMarkdownSyntax inserts table syntax`() {
        val text = ""
        val (result, _) = MarkdownUtils.insertMarkdownSyntax(
            text = text,
            selectionStart = 0,
            selectionEnd = 0,
            syntax = MarkdownSyntax.TABLE
        )

        assertTrue(result.contains("| Column 1 |"))
        assertTrue(result.contains("|----------|"))
        assertTrue(result.contains("| Cell 1   |"))
    }

    @Test
    fun `insertMarkdownSyntax cursor position correct for empty selection`() {
        val text = "Hello"
        val (result, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
            text = text,
            selectionStart = 2,
            selectionEnd = 2,
            syntax = MarkdownSyntax.BOLD
        )

        assertEquals(4, cursorPos)
        assertEquals("He****llo", result)
    }
}
