package com.gasperpintar.smokingtracker.ui.view.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import androidx.core.graphics.withClip

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Base(context, attrs) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pointBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    @Override
    override fun setData(data: Data) {
        super.setData(data)
        if (animated) animateChart()
        else {
            animationProgress = 1f
            invalidate()
        }
    }

    @Override
    override fun drawChart(
        canvas: Canvas,
        entries: List<Entry>
    ) {
        val maxValue = entries.maxOfOrNull { it.value } ?: 0f

        if (maxValue <= 0f) return
        val visibleEntries = entries.filter { it.value > 0f }

        if (visibleEntries.isEmpty()) return
        val points = visibleEntries.mapIndexed { index, entry ->
            val xPosition = if (visibleEntries.size > 1) index.toFloat() / (visibleEntries.size - 1) else 0f
            ChartPoint(
                x = chartArea.left + xPosition * chartArea.width(),
                y = chartArea.bottom - (entry.value / maxValue) * chartArea.height()
            )
        }

        if (points.isEmpty()) return
        val revealTop = chartArea.bottom - chartArea.height() * animationProgress
        canvas.withClip(left = chartArea.left, top = revealTop, right = chartArea.right, bottom = chartArea.bottom) {
            drawArea(canvas, points)
            drawLine(canvas, points)
            drawPoints(canvas, points)
        }
    }

    @Override
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    private fun drawArea(
        canvas: Canvas,
        points: List<ChartPoint>
    ) {
        if (points.size < 2) return
        val fillPath = Path()
        fillPath.moveTo(points.first().x, chartArea.bottom)
        fillPath.lineTo(points.first().x, points.first().y)

        for (i in 0 until points.size - 1) {
            val current = points[i]
            val next = points[i + 1]
            val controlX = (current.x + next.x) / 2f
            fillPath.cubicTo(controlX, current.y, controlX, next.y, next.x, next.y)
        }

        fillPath.lineTo(points.last().x, chartArea.bottom)
        fillPath.close()

        fillPaint.shader = LinearGradient(
            0f,
            chartArea.top,
            0f,
            chartArea.bottom,
            chartColor,
            chartColor and 0x00FFFFFF,
            Shader.TileMode.CLAMP
        )

        canvas.drawPath(fillPath, fillPaint)
        fillPaint.shader = null
    }

    private fun drawLine(
        canvas: Canvas,
        points: List<ChartPoint>
    ) {
        if (points.isEmpty()) return
        val linePath = Path()

        linePath.moveTo(points.first().x, points.first().y)
        for (i in 0 until points.size - 1) {
            val current = points[i]
            val next = points[i + 1]

            val controlX = (current.x + next.x) / 2f
            linePath.cubicTo(controlX, current.y, controlX, next.y, next.x, next.y)
        }

        linePaint.color = chartColor
        linePaint.strokeWidth = lineWidth * density
        linePaint.shader = null

        canvas.drawPath(linePath, linePaint)
    }

    private fun drawPoints(
        canvas: Canvas,
        points: List<ChartPoint>
    ) {
        points.forEach { point ->
            val radius = pointRadius * density
            canvas.drawCircle(point.x, point.y, radius + 2f * density, pointBorderPaint)
            pointPaint.color = chartColor
            canvas.drawCircle(point.x, point.y, radius, pointPaint)
        }
    }
}