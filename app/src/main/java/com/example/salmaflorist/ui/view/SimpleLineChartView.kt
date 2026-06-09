package com.example.salmaflorist.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.salmaflorist.R

class SimpleLineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataPoints: List<Int> = emptyList()
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary_light)
        alpha = 50
        style = Paint.Style.FILL
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    fun setData(data: List<Int>) {
        this.dataPoints = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val padding = 60f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        val maxVal = dataPoints.maxOrNull()?.coerceAtLeast(1) ?: 1
        val stepX = if (dataPoints.size > 1) chartWidth / (dataPoints.size - 1) else 0f

        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = height - padding - (value.toFloat() / maxVal * chartHeight)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height - padding)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == dataPoints.size - 1) {
                fillPath.lineTo(x, height - padding)
                fillPath.close()
            }
        }

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)

        dataPoints.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = height - padding - (value.toFloat() / maxVal * chartHeight)
            canvas.drawCircle(x, y, 10f, pointPaint)
            canvas.drawText(value.toString(), x, y - 20f, textPaint)
        }
    }
}