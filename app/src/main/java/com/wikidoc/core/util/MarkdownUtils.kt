package com.wikidoc.core.util

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import java.util.regex.Pattern

object MarkdownUtils {

    private val parser = Parser.builder()
        .extensions(listOf(TablesExtension.create(), HeadingAnchorExtension.create()))
        .build()

    private val renderer = HtmlRenderer.builder()
        .extensions(listOf(TablesExtension.create(), HeadingAnchorExtension.create()))
        .build()

    fun parseToHtml(markdown: String): String {
        if (markdown.isBlank()) return ""

        val document = parser.parse(markdown)
        val htmlBody = renderer.render(document)

        return wrapWithHtmlTemplate(htmlBody)
    }

    private fun wrapWithHtmlTemplate(body: String): String {
        val processedBody = body
            .replace("<pre><code class=\"language-mermaid\">", "<div class=\"mermaid\">")
            .replace("</code></pre>", "</div>")
            .replace("<pre><code class=\"mermaid\">", "<div class=\"mermaid\">")
            .replace("&lt;div class=\"mermaid\"&gt;", "<div class=\"mermaid\">")
            .replace("&lt;/div&gt;", "</div>")

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
                <style>
                    ${getMarkdownStyles()}
                </style>
            </head>
            <body>
                $processedBody
            </body>
            <script>
                mermaid.initialize({
                    startOnLoad: true,
                    theme: 'default',
                    securityLevel: 'loose',
                    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif',
                    flowchart: {
                        useMaxWidth: true,
                        htmlLabels: true
                    }
                });
            </script>
            </html>
        """.trimIndent()
    }

    private fun getMarkdownStyles(): String = """
        :root {
            --text-color: #1a1a1a;
            --bg-color: #ffffff;
            --code-bg: #f6f8fa;
            --border-color: #e1e4e8;
            --link-color: #0366d6;
        }
        
        @media (prefers-color-scheme: dark) {
            :root {
                --text-color: #c9d1d9;
                --bg-color: #0d1117;
                --code-bg: #161b22;
                --border-color: #30363d;
                --link-color: #58a6ff;
            }
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
            font-size: 16px;
            line-height: 1.6;
            color: var(--text-color);
            background-color: var(--bg-color);
            padding: 16px;
            margin: 0;
            word-wrap: break-word;
        }
        
        h1, h2, h3, h4, h5, h6 {
            margin-top: 24px;
            margin-bottom: 16px;
            font-weight: 600;
            line-height: 1.25;
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 8px;
        }
        
        h1 { font-size: 2em; }
        h2 { font-size: 1.5em; }
        h3 { font-size: 1.25em; }
        h4 { font-size: 1em; }
        h5 { font-size: 0.875em; }
        h6 { font-size: 0.85em; color: #6a737d; }
        
        p {
            margin-top: 0;
            margin-bottom: 16px;
        }
        
        a {
            color: var(--link-color);
            text-decoration: none;
        }
        
        a:hover {
            text-decoration: underline;
        }
        
        code {
            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
            font-size: 85%;
            padding: 0.2em 0.4em;
            background-color: var(--code-bg);
            border-radius: 6px;
        }
        
        pre {
            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
            font-size: 85%;
            padding: 16px;
            overflow: auto;
            background-color: var(--code-bg);
            border-radius: 6px;
            line-height: 1.45;
        }
        
        pre code {
            padding: 0;
            background-color: transparent;
            border-radius: 0;
        }
        
        blockquote {
            padding: 0 1em;
            color: #6a737d;
            border-left: 0.25em solid var(--border-color);
            margin: 0 0 16px 0;
        }
        
        ul, ol {
            padding-left: 2em;
            margin-top: 0;
            margin-bottom: 16px;
        }
        
        li {
            margin-top: 0.25em;
        }
        
        li + li {
            margin-top: 0.25em;
        }
        
        table {
            border-spacing: 0;
            border-collapse: collapse;
            margin-bottom: 16px;
            width: 100%;
            overflow: auto;
        }
        
        table th, table td {
            padding: 6px 13px;
            border: 1px solid var(--border-color);
        }
        
        table th {
            font-weight: 600;
            background-color: var(--code-bg);
        }
        
        table tr:nth-child(2n) {
            background-color: var(--bg-color);
        }
        
        hr {
            height: 0.25em;
            padding: 0;
            margin: 24px 0;
            background-color: var(--border-color);
            border: 0;
        }
        
        img {
            max-width: 100%;
            box-sizing: border-box;
        }
        
        .task-list-item {
            list-style-type: none;
        }
        
        .task-list-item input[type="checkbox"] {
            margin-right: 8px;
        }
        
        .mermaid {
            background-color: var(--bg-color);
            border-radius: 8px;
            padding: 16px;
            margin: 16px 0;
            text-align: center;
        }
        
        .mermaid svg {
            max-width: 100%;
            height: auto;
        }
    """.trimIndent()

    fun insertMarkdownSyntax(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        syntax: MarkdownSyntax
    ): Pair<String, Int> {
        val before = text.substring(0, selectionStart)
        val selected = text.substring(selectionStart, selectionEnd)
        val after = text.substring(selectionEnd)

        val (prefix, suffix, cursorOffset) = when (syntax) {
            MarkdownSyntax.BOLD -> Triple("**", "**", 2)
            MarkdownSyntax.ITALIC -> Triple("*", "*", 1)
            MarkdownSyntax.STRIKETHROUGH -> Triple("~~", "~~", 2)
            MarkdownSyntax.CODE -> Triple("`", "`", 1)
            MarkdownSyntax.CODE_BLOCK -> Triple("```\n", "\n```", 4)
            MarkdownSyntax.HEADING_1 -> Triple("# ", "\n", 2)
            MarkdownSyntax.HEADING_2 -> Triple("## ", "\n", 3)
            MarkdownSyntax.HEADING_3 -> Triple("### ", "\n", 4)
            MarkdownSyntax.BULLET_LIST -> Triple("- ", "\n", 2)
            MarkdownSyntax.NUMBERED_LIST -> Triple("1. ", "\n", 3)
            MarkdownSyntax.TASK_LIST -> Triple("- [ ] ", "\n", 6)
            MarkdownSyntax.QUOTE -> Triple("> ", "\n", 2)
            MarkdownSyntax.LINK -> Triple("[", "](url)", 1)
            MarkdownSyntax.IMAGE -> Triple("![alt](", ")", 2)
            MarkdownSyntax.TABLE -> Triple("| Column 1 | Column 2 |\n|----------|----------|\n| Cell 1   | Cell 2   |\n", "\n", 0)
            MarkdownSyntax.HORIZONTAL_RULE -> Triple("\n---\n", "", 0)
            MarkdownSyntax.MERMAID -> Triple("```mermaid\ngraph TD\n    A[Start] --> B{Decision}\n    B -->|Yes| C[Result 1]\n    B -->|No| D[Result 2]\n", "\n```\n", 0)
        }

        val newText = before + prefix + selected + suffix + after
        val newCursorPos = if (selectionStart == selectionEnd) {
            selectionStart + prefix.length
        } else {
            selectionStart + prefix.length + selected.length + suffix.length
        }

        return Pair(newText, newCursorPos)
    }
}

enum class MarkdownSyntax {
    BOLD,
    ITALIC,
    STRIKETHROUGH,
    CODE,
    CODE_BLOCK,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    BULLET_LIST,
    NUMBERED_LIST,
    TASK_LIST,
    QUOTE,
    LINK,
    IMAGE,
    TABLE,
    HORIZONTAL_RULE,
    MERMAID
}
