package com.gasperpintar.smokingtracker.ui.view.chart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.withRotation
import com.gasperpintar.smokingtracker.R
import com.google.android.material.color.MaterialColors
import kotlin.math.ceil

abstract class Base @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Entry(
        val label: String,
        val value: Float
    )

    data class Data(
        val title: String = "",
        val entries: List<Entry>
    )

    protected data class ChartPoint(
        val x: Float,
        val y: Float
    )

    var showGrid = true
    var showLabels = true
    var showValues = true
    var animated = true

    var chartColor: Int = MaterialColors.getColor(this, R.attr.appChartColor)
    var gridColor: Int = MaterialColors.getColor(this, R.attr.appChartGridColor)
    var textColor: Int = MaterialColors.getColor(this, R.attr.appChartTextColor)

    var lineWidth: Float = 3f
    var pointRadius: Float = 5f

    protected var chartData: Data? = null
    protected val chartArea = RectF()
    protected val density = resources.displayMetrics.density

    protected var animationProgress = 1f
    private var animator: ValueAnimator? = null

    protected val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 11f * density
        textAlign = Paint.Align.RIGHT
    }

    protected val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = gridColor
        strokeWidth = 1f * density
        style = Paint.Style.STROKE
    }

    protected val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 16f * density
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    protected val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 14f * density
        textAlign = Paint.Align.CENTER
    }

    private val leftOffset = 45f * density
    private val topOffset = 45f * density
    private val rightOffset = 15f * density
    private val bottomOffset = 65f * density

    @Override
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val data = chartData ?: return
        val width = width.toFloat()
        val height = height.toFloat()

        chartArea.set(leftOffset, topOffset, width - rightOffset, height - bottomOffset)
        if (chartArea.width() <= 0f || chartArea.height() <= 0f) return
        drawTitle(canvas, data.title)

        val visibleEntries = data.entries.filter { it.value > 0f }
        val maxValue = data.entries.maxOfOrNull { it.value } ?: 0f

        if (visibleEntries.isEmpty() || maxValue <= 0f) {
            drawEmptyState(canvas)
            return
        }

        if (showGrid) drawGrid(canvas, maxValue)
        if (showLabels) drawX(canvas, visibleEntries)
        if (showValues) drawY(canvas, maxValue)
        drawChart(canvas, visibleEntries)
    }

    @Override
    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    open fun setData(
        data: Data
    ) {
        chartData = data
        invalidate()
    }

    protected open fun drawTitle(
        canvas: Canvas,
        title: String
    ) {
        if (title.isBlank()) return
        canvas.drawText(title, chartArea.centerX(), 25f * density, titlePaint)
    }

    protected open fun drawGrid(
        canvas: Canvas,
        maxValue: Float
    ) {
        val maxDataValue = ceil(x = maxValue).toInt()
        if (maxDataValue <= 0) return

        val stepIncrement = ceil(x = (maxDataValue + 1) / 5f).toInt().coerceAtLeast(minimumValue = 1)
        var value = maxDataValue

        while (value >= 0) {
            val fraction = value.toFloat() / maxDataValue
            val y = chartArea.bottom - fraction * chartArea.height()
            canvas.drawLine(chartArea.left, y, chartArea.right, y, gridPaint)
            value -= stepIncrement
        }
    }

    protected open fun drawY(
        canvas: Canvas,
        maxValue: Float
    ) {
        val maxDataValue = ceil(x = maxValue).toInt()
        if (maxDataValue <= 0) return

        textPaint.color = textColor
        textPaint.textAlign = Paint.Align.RIGHT

        val stepIncrement = ceil(x = (maxDataValue + 1) / 5f).toInt().coerceAtLeast(minimumValue = 1)

        val labels = generateSequence(seed = maxDataValue) { value ->
            (value - stepIncrement).takeIf { it >= 0 }
        }

        labels.forEach { value ->
            val fraction = value.toFloat() / maxDataValue
            val y = chartArea.bottom - fraction * chartArea.height()
            canvas.drawText(value.toString(), chartArea.left - 8f * density, y + 4f * density, textPaint)
        }
    }


    protected open fun drawX(canvas: Canvas, visibleEntries: List<Entry>) {
        if (visibleEntries.isEmpty()) return

        textPaint.color = textColor
        textPaint.textAlign = Paint.Align.CENTER

        val labelStep = if (visibleEntries.size > 15) 2 else 1
        visibleEntries.forEachIndexed { index, entry ->
            if (index % labelStep != 0) return@forEachIndexed
            val xPosition = if (visibleEntries.size > 1) index.toFloat() / (visibleEntries.size - 1) else 0f
            val x = chartArea.left + xPosition * chartArea.width()
            val labelY = chartArea.bottom + 22f * density
            canvas.withRotation(degrees = -45f, pivotX = x, pivotY = labelY) {
                drawText(entry.label, x, labelY, textPaint)
            }
        }

        textPaint.textAlign = Paint.Align.RIGHT
    }

    protected fun animateChart(duration: Long = 400L) {
        animator?.cancel()
        animationProgress = 0f
        invalidate()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            repeatCount = 0
            addUpdateListener { animation ->
                animationProgress = animation.animatedValue as Float
                postInvalidateOnAnimation()
            }
            start()
        }
    }

    protected abstract fun drawChart(
        canvas: Canvas,
        entries: List<Entry>
    )

    protected open fun drawEmptyState(canvas: Canvas) {
        canvas.drawText(
            context.getString(R.string.graph_view_info),
            chartArea.centerX(),
            chartArea.centerY() - (emptyPaint.ascent() + emptyPaint.descent()) / 2f,
            emptyPaint
        )
    }
}