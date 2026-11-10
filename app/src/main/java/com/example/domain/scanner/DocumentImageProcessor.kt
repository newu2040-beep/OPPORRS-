package com.example.domain.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import com.example.data.model.ScanFilterMode
import com.example.data.model.ScannedPage
import java.io.File
import java.io.FileOutputStream

object DocumentImageProcessor {

    /**
     * Estimates document boundary box (normalized 0f..1f) by sampling luminance contrast
     * from the edges toward the center of the captured bitmap.
     */
    fun detectDocumentBounds(bitmap: Bitmap): RectF {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 20 || h < 20) return RectF(0.06f, 0.06f, 0.94f, 0.94f)

        var minX = w
        var minY = h
        var maxX = 0
        var maxY = 0
        val stepX = (w / 24).coerceAtLeast(1)
        val stepY = (h / 24).coerceAtLeast(1)

        for (y in stepY until (h - stepY) step stepY) {
            for (x in stepX until (w - stepX) step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val lum = (Color.red(pixel) * 0.299f + Color.green(pixel) * 0.587f + Color.blue(pixel) * 0.114f)
                if (lum > 145f) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }

        return if (maxX > minX + w / 4 && maxY > minY + h / 4) {
            RectF(
                (minX.toFloat() / w).coerceIn(0.03f, 0.25f),
                (minY.toFloat() / h).coerceIn(0.03f, 0.25f),
                (maxX.toFloat() / w).coerceIn(0.75f, 0.97f),
                (maxY.toFloat() / h).coerceIn(0.75f, 0.97f)
            )
        } else {
            RectF(0.06f, 0.06f, 0.94f, 0.94f)
        }
    }

    /**
     * Applies crop, rotation, brightness, contrast, and color mode filter (COLOR, GRAYSCALE, BW)
     * to a source bitmap.
     */
    fun processScannedBitmap(source: Bitmap, page: ScannedPage): Bitmap {
        val w = source.width
        val h = source.height
        val left = (page.cropLeft.coerceIn(0f, 0.9f) * w).toInt()
        val top = (page.cropTop.coerceIn(0f, 0.9f) * h).toInt()
        val right = (page.cropRight.coerceIn(left / w.toFloat() + 0.1f, 1f) * w).toInt()
        val bottom = (page.cropBottom.coerceIn(top / h.toFloat() + 0.1f, 1f) * h).toInt()

        val cropW = (right - left).coerceAtLeast(1)
        val cropH = (bottom - top).coerceAtLeast(1)

        val cropped = Bitmap.createBitmap(source, left, top, cropW, cropH)

        val rotated = if (page.rotationDegrees % 360 != 0) {
            val matrix = Matrix().apply { postRotate(page.rotationDegrees.toFloat()) }
            Bitmap.createBitmap(cropped, 0, 0, cropped.width, cropped.height, matrix, true)
        } else {
            cropped
        }

        val output = Bitmap.createBitmap(rotated.width, rotated.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colorMatrix = ColorMatrix()
        when (page.filterMode) {
            ScanFilterMode.COLOR -> {
                // Slight document clarity boost
            }
            ScanFilterMode.GRAYSCALE -> {
                colorMatrix.setSaturation(0f)
            }
            ScanFilterMode.BW -> {
                colorMatrix.setSaturation(0f)
                val bwContrast = 2.6f
                val bwTranslate = -180f
                val bwMatrix = ColorMatrix(
                    floatArrayOf(
                        bwContrast, 0f, 0f, 0f, bwTranslate,
                        0f, bwContrast, 0f, 0f, bwTranslate,
                        0f, 0f, bwContrast, 0f, bwTranslate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                colorMatrix.postConcat(bwMatrix)
            }
        }

        val c = page.contrast.coerceIn(0.5f, 2.5f)
        val b = page.brightness.coerceIn(-100f, 100f)
        val translate = (-.5f * c + .5f) * 255f + b
        val bcMatrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, translate,
                0f, c, 0f, 0f, translate,
                0f, 0f, c, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        colorMatrix.postConcat(bcMatrix)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(rotated, 0f, 0f, paint)
        return output
    }

    fun loadAndProcessPageBitmap(context: Context, page: ScannedPage, maxDimension: Int = 1200): Bitmap? {
        return try {
            val uri = Uri.parse(page.imageUri)
            val inputStream = if (uri.scheme == "file") {
                File(uri.path ?: return null).inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
            } ?: return null

            val raw = inputStream.use { BitmapFactory.decodeStream(it) } ?: return null
            val scale = (maxDimension.toFloat() / maxOf(raw.width, raw.height)).coerceAtMost(1f)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    raw,
                    (raw.width * scale).toInt().coerceAtLeast(1),
                    (raw.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                raw
            }
            processScannedBitmap(scaled, page)
        } catch (e: Exception) {
            null
        }
    }

    fun saveCapturedBitmap(context: Context, bitmap: Bitmap): Uri {
        val scansDir = File(context.filesDir, "scans").apply { mkdirs() }
        val file = File(scansDir, "scan_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return Uri.fromFile(file)
    }
}
