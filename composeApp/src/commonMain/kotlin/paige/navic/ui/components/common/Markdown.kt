// very limited markdown parser
// since this is literally just used once i didn't want to add another dependency

package paige.navic.ui.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun Markdown(
	text: String,
	modifier: Modifier = Modifier,
	color: Color = Color.Unspecified
) {
	val lines = text.split("\n")
	Column(modifier = modifier) {
		lines.forEach { line ->
			if (line.isBlank()) {
				Spacer(Modifier.height(8.dp))
			} else {
				MarkdownLine(line, color)
			}
		}
	}
}

@Composable
private fun MarkdownLine(line: String, color: Color) {
	val trimmed = line.trimStart()
	val orderedListMatch = Regex("""^(\d+\.)\s+(.*)""").find(trimmed)

	when {
		trimmed.startsWith("# ") -> {
			Text(
				text = parseInlineMarkdown(trimmed.substring(2)),
				style = MaterialTheme.typography.headlineLarge,
				color = color,
				modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
			)
		}

		trimmed.startsWith("## ") -> {
			Text(
				text = parseInlineMarkdown(trimmed.substring(3)),
				style = MaterialTheme.typography.headlineMedium,
				color = color,
				modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
			)
		}

		trimmed.startsWith("### ") -> {
			Text(
				text = parseInlineMarkdown(trimmed.substring(4)),
				style = MaterialTheme.typography.headlineSmall,
				color = color,
				modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
			)
		}

		trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
			Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
				Text(
					text = "• ",
					color = color.takeIf { it != Color.Unspecified } ?: LocalContentColor.current,
					style = MaterialTheme.typography.bodyMedium
				)
				MarkdownText(parseInlineMarkdown(trimmed.substring(2)), color)
			}
		}

		orderedListMatch != null -> {
			val number = orderedListMatch.groupValues[1]
			val content = orderedListMatch.groupValues[2]
			Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
				Text(
					text = "$number ",
					color = color.takeIf { it != Color.Unspecified } ?: LocalContentColor.current,
					style = MaterialTheme.typography.bodyMedium
				)
				MarkdownText(parseInlineMarkdown(content), color)
			}
		}

		else -> {
			MarkdownText(
				parseInlineMarkdown(line),
				color,
				modifier = Modifier.padding(vertical = 2.dp)
			)
		}
	}
}

@Composable
private fun MarkdownText(
	annotatedString: AnnotatedString,
	color: Color,
	modifier: Modifier = Modifier
) {
	val style = MaterialTheme.typography.bodyMedium.copy(
		color = if (color != Color.Unspecified) color else LocalContentColor.current
	)
	Text(
		text = annotatedString,
		modifier = modifier,
		style = style
	)
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
	return buildAnnotatedString {
		appendMarkdown(text)
	}
}

private fun AnnotatedString.Builder.appendMarkdown(text: String) {
	var remaining = text
	while (remaining.isNotEmpty()) {
		val boldMatch = Regex("""\*\*(.*?)\*\*""").find(remaining)
		val italicMatch = Regex("""\*(.*?)\*""").find(remaining)
		val strikeoutMatch = Regex("""~~(.*?)~~""").find(remaining)

		val matches = listOfNotNull(boldMatch, italicMatch, strikeoutMatch)
		if (matches.isEmpty()) {
			append(remaining)
			remaining = ""
			continue
		}

		val nextMatch = matches.minWithOrNull(compareBy({ it.range.first }, { -it.value.length }))!!

		append(remaining.substring(0, nextMatch.range.first))

		val content = nextMatch.groupValues[1]
		when (nextMatch) {
			boldMatch -> {
				withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
					appendMarkdown(content)
				}
			}

			italicMatch -> {
				withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
					appendMarkdown(content)
				}
			}

			strikeoutMatch -> {
				withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
					appendMarkdown(content)
				}
			}
		}

		remaining = remaining.substring(nextMatch.range.last + 1)
	}
}
