/*
 * Author: Matthew Zhang
 * Created on: 5/9/19 10:02 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.widgets

import android.content.Context
import android.graphics.*
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import com.qint.pt1.base.extension.dp2px
import com.qint.pt1.base.extension.sp2px

/*
 * @param context
 * @param radius radius in DP
 * @param color background or border color
 * @param textColor textColor
 * @param textSize textSize in DP
 * @param backgroundStyle background fill/stroke style
 * @param drawable resource id of drawable, the drawable will be draw at the begining of Span
 * @param drawableSize size in DP of drawable
 */
class RoundedBackgroundSpan(val context: Context,
                            val radius: Int, //dp
                            @ColorInt val color: Int,
                            @ColorInt val textColor: Int,
                            val textSize: Int, //sp
                            val backgroundStyle: Paint.Style = Paint.Style.FILL,
                            val drawable: Int? = null,
                            val drawableSize: Int = 0 //dp
): ReplacementSpan() {

    val horizontalPadding = context.dp2px(4)
    val verticalPadding = context.dp2px(2)
    val borderWidth = context.dp2px(1)
    val textSizeInPx = context.sp2px(textSize)
    val drawableWidth = if(drawable != null) context.dp2px(drawableSize).toInt() else 0
    val drawableHeight = if(drawable != null) context.dp2px(drawableSize).toInt() else 0

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ) = Math.round(measureText(paint, text, start, end))

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        paint.textSize = textSizeInPx

        val boxHeight = textSizeInPx + verticalPadding * 2
        val verticalMargin = (bottom - top - boxHeight) / 2
        val boxOffset = if(drawable != null) context.dp2px(2).toInt() else 0

        val rect = RectF(x + boxOffset,
            top.toFloat() + verticalMargin,
            x + measureText(paint, text, start, end),
            bottom.toFloat() - verticalMargin)

        paint.color = color
        paint.style = backgroundStyle
        paint.strokeWidth = borderWidth
        val radiusInPx = context.dp2px(radius)
        canvas.drawRoundRect(rect, radiusInPx, radiusInPx, paint)

        if(drawable != null){
            val bitmap = BitmapFactory.decodeResource(context.resources, drawable)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, drawableWidth, drawableHeight, true)
            val originFilter = paint.colorFilter
            //paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.ADD) //FIXME, 实现图标变色
            canvas.drawBitmap(scaledBitmap, x, top.toFloat() + verticalMargin, paint)
            paint.colorFilter = originFilter
        }

        paint.style = Paint.Style.FILL
        paint.color = textColor
        val desent = paint.fontMetricsInt.descent
        val textOffset = Math.max(horizontalPadding, drawableWidth.toFloat())
        //绘制文字时如果垂直方向使用y参数作为基线，在设置了行间距的时候标签文字的垂直位置会有比较大的偏差，故使用bottom再减去垂直margin和padding及desent作为文字基线位置
        //FIXME: 在设置了行间距且文字有多行时，标签框的垂直位置会比同一行的文字略低一些
        //FIXME：在设置了文字为单行时，如果文字长度超出textview宽度，则这里的文字会显示为小方框
        canvas.drawText(text, start, end, x + textOffset, bottom.toFloat() - verticalMargin - verticalPadding/2 - desent, paint)
    }

    private fun measureText(paint: Paint, text: CharSequence?, start: Int, end: Int) =
            paint.measureText(text, start, end) + horizontalPadding * 2 + drawableWidth
}