package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Hand-drawn style vector linework illustrations matching the Opporrs editorial direction:
 * 1. Paper sheet, pen and folder (Welcome / Auth screen)
 * 2. Hand holding / presenting a document (Career & Template discovery)
 * 3. Pen drawing a signature (Signature Manager / Editor)
 * 4. Folder containing documents (Empty state / Folder management)
 * 5. Document scanner illustration (Scanner screen)
 */

@Composable
fun OpporrsBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val secondary = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawRoundRect(
            color = primary,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.26f, w * 0.26f)
        )

        // Document sheet outline
        val sheetLeft = w * 0.26f
        val sheetTop = h * 0.20f
        val sheetRight = w * 0.74f
        val sheetBottom = h * 0.80f
        val fold = w * 0.16f

        val docPath = Path().apply {
            moveTo(sheetLeft, sheetTop)
            lineTo(sheetRight - fold, sheetTop)
            lineTo(sheetRight, sheetTop + fold)
            lineTo(sheetRight, sheetBottom)
            lineTo(sheetLeft, sheetBottom)
            close()
        }
        drawPath(docPath, color = surface)

        // Folded corner
        val foldPath = Path().apply {
            moveTo(sheetRight - fold, sheetTop)
            lineTo(sheetRight - fold, sheetTop + fold)
            lineTo(sheetRight, sheetTop + fold)
            close()
        }
        drawPath(foldPath, color = secondary)

        // Subtle lines inside sheet
        val stroke = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
        drawLine(
            color = primary,
            start = Offset(w * 0.35f, h * 0.44f),
            end = Offset(w * 0.58f, h * 0.44f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = primary,
            start = Offset(w * 0.35f, h * 0.56f),
            end = Offset(w * 0.65f, h * 0.56f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = primary,
            start = Offset(w * 0.35f, h * 0.68f),
            end = Offset(w * 0.52f, h * 0.68f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PaperPenFolderIllustration(
    modifier: Modifier = Modifier
) {
    val inkColor = MaterialTheme.colorScheme.onBackground
    val accentColor = MaterialTheme.colorScheme.primary
    val mutedGreen = MaterialTheme.colorScheme.secondary
    val paperFill = MaterialTheme.colorScheme.surface
    val folderFill = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val lineStroke = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Soft organic desk shadow line
        drawLine(
            color = inkColor.copy(alpha = 0.18f),
            start = Offset(w * 0.12f, h * 0.86f),
            end = Offset(w * 0.88f, h * 0.86f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Back folder tab & body
        val folderPath = Path().apply {
            moveTo(w * 0.18f, h * 0.34f)
            lineTo(w * 0.38f, h * 0.34f)
            lineTo(w * 0.43f, h * 0.40f)
            lineTo(w * 0.72f, h * 0.40f)
            lineTo(w * 0.72f, h * 0.82f)
            lineTo(w * 0.18f, h * 0.82f)
            close()
        }
        drawPath(folderPath, color = folderFill)
        drawPath(folderPath, color = inkColor.copy(alpha = 0.75f), style = lineStroke)

        // Tilted paper sheet with folded corner emerging from folder
        withTransform({
            rotate(degrees = -4f, pivot = Offset(w * 0.48f, h * 0.52f))
        }) {
            val pLeft = w * 0.28f
            val pTop = h * 0.16f
            val pRight = w * 0.68f
            val pBottom = h * 0.78f
            val fold = w * 0.08f

            val sheetPath = Path().apply {
                moveTo(pLeft, pTop)
                lineTo(pRight - fold, pTop)
                lineTo(pRight, pTop + fold)
                lineTo(pRight, pBottom)
                lineTo(pLeft, pBottom)
                close()
            }
            drawPath(sheetPath, color = paperFill)
            drawPath(sheetPath, color = inkColor, style = lineStroke)

            val foldCorner = Path().apply {
                moveTo(pRight - fold, pTop)
                lineTo(pRight - fold, pTop + fold)
                lineTo(pRight, pTop + fold)
            }
            drawPath(foldCorner, color = inkColor, style = lineStroke)

            // CV badge box inside sheet
            drawRoundRect(
                color = accentColor.copy(alpha = 0.14f),
                topLeft = Offset(pLeft + w * 0.05f, pTop + h * 0.08f),
                size = Size(w * 0.14f, h * 0.09f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Hand-inked editorial text lines
            val lineYs = listOf(0.34f, 0.42f, 0.50f, 0.58f)
            lineYs.forEachIndexed { index, ratioY ->
                val endRatio = if (index % 2 == 0) 0.61f else 0.54f
                drawLine(
                    color = if (index == 0) accentColor else inkColor.copy(alpha = 0.65f),
                    start = Offset(pLeft + w * 0.05f, h * ratioY),
                    end = Offset(w * endRatio, h * ratioY),
                    strokeWidth = if (index == 0) 4.5f else 3f,
                    cap = StrokeCap.Round
                )
            }

            // Hand-drawn signature squiggle at bottom of sheet
            val sigPath = Path().apply {
                moveTo(pLeft + w * 0.06f, h * 0.69f)
                cubicTo(
                    pLeft + w * 0.12f, h * 0.63f,
                    pLeft + w * 0.16f, h * 0.73f,
                    pLeft + w * 0.22f, h * 0.66f
                )
                cubicTo(
                    pLeft + w * 0.25f, h * 0.63f,
                    pLeft + w * 0.28f, h * 0.70f,
                    pLeft + w * 0.33f, h * 0.67f
                )
            }
            drawPath(sigPath, color = accentColor, style = Stroke(width = 3.2f, cap = StrokeCap.Round))
        }

        // Fountain pen resting diagonally on the right
        withTransform({
            rotate(degrees = 32f, pivot = Offset(w * 0.74f, h * 0.54f))
        }) {
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(w * 0.71f, h * 0.24f),
                size = Size(w * 0.055f, h * 0.46f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = inkColor,
                topLeft = Offset(w * 0.71f, h * 0.24f),
                size = Size(w * 0.055f, h * 0.46f),
                cornerRadius = CornerRadius(8f, 8f),
                style = lineStroke
            )
            // Pen nib
            val nibPath = Path().apply {
                moveTo(w * 0.71f, h * 0.70f)
                lineTo(w * 0.7375f, h * 0.80f)
                lineTo(w * 0.765f, h * 0.70f)
                close()
            }
            drawPath(nibPath, color = mutedGreen)
            drawPath(nibPath, color = inkColor, style = lineStroke)
        }
    }
}

@Composable
fun EditorialDocumentIllustration(
    modifier: Modifier = Modifier
) {
    val ink = MaterialTheme.colorScheme.onBackground
    val accent = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Document sheet with folded corner
        val left = w * 0.20f
        val top = h * 0.10f
        val right = w * 0.80f
        val bottom = h * 0.90f
        val fold = w * 0.14f

        val path = Path().apply {
            moveTo(left, top)
            lineTo(right - fold, top)
            lineTo(right, top + fold)
            lineTo(right, bottom)
            lineTo(left, bottom)
            close()
        }
        drawPath(path, color = surface)
        drawPath(path, color = ink, style = stroke)

        val foldLine = Path().apply {
            moveTo(right - fold, top)
            lineTo(right - fold, top + fold)
            lineTo(right, top + fold)
        }
        drawPath(foldLine, color = ink, style = stroke)

        // Checkmark seal badge
        drawCircle(
            color = accent,
            radius = w * 0.14f,
            center = Offset(w * 0.74f, h * 0.72f)
        )
        val checkPath = Path().apply {
            moveTo(w * 0.68f, h * 0.72f)
            lineTo(w * 0.725f, h * 0.765f)
            lineTo(w * 0.80f, h * 0.675f)
        }
        drawPath(checkPath, color = surface, style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Lines
        drawLine(accent, Offset(w * 0.30f, h * 0.28f), Offset(w * 0.56f, h * 0.28f), strokeWidth = 4f, cap = StrokeCap.Round)
        drawLine(ink.copy(alpha = 0.6f), Offset(w * 0.30f, h * 0.42f), Offset(w * 0.68f, h * 0.42f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(ink.copy(alpha = 0.6f), Offset(w * 0.30f, h * 0.54f), Offset(w * 0.64f, h * 0.54f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(ink.copy(alpha = 0.6f), Offset(w * 0.30f, h * 0.66f), Offset(w * 0.52f, h * 0.66f), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}
