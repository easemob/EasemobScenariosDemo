package com.hyphenate.scenarios.callkit.widget

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import coil.size.Size
import coil.transform.Transformation

class RoundedCornersTransformation(private val radius: Float) : Transformation {

    override val cacheKey: String
        get() = "rounded_corners_$radius"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val output = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(input, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        val rectF = RectF(0f, 0f, input.width.toFloat(), input.height.toFloat())
        canvas.drawRoundRect(rectF, radius, radius, paint)
        return output
    }

}