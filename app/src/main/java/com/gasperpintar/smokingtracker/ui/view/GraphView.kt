package com.gasperpintar.smokingtracker.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.withRotation
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.model.GraphEntry
import com.gasperpintar.smokingtracker.type.GraphInterval
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var dataList: List<GraphEntry> = emptyList()
    private var forecastList: List<GraphEntry> = emptyList()
    private var labelsNumber: Int = 5
    private var currentGraphInterval: GraphInterval = GraphInterval.WEEKLY

    private val paddingLeft: Float = 80f
    private val paddingRight: Float = 80f
    private val paddingTop: Float = 60f
    private val paddingBottom: Float = 120f
    private val pointRadius: Float = 12f

    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        color = typedValue.data
        textSize = 30f
    }

    private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val pointPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.FILL
    }

    private val forecastLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        color = typedValue.data
        strokeWidth = 6f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(15f, 10f), 0f)
    }

    private val forecastPointPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        color = typedValue.data
        style = Paint.Style.FILL
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        if (dataList.isEmpty() && forecastList.isEmpty()) {
            drawEmptyMessage(canvas)
            return
        }

        val graphWidth: Float = width - paddingLeft - paddingRight
        val graphHeight: Float = height - paddingTop - paddingBottom

        val maxMain = dataList.maxOfOrNull { it.quantity } ?: 0
        val maxForecast = forecastList.maxOfOrNull { it.quantity } ?: 0
        val maxDataValue: Int = maxOf(maxMain, maxForecast).coerceAtLeast(1)

        drawAxes(canvas, graphWidth, graphHeight)
        drawYAxisLabels(canvas, graphHeight, maxDataValue)
        drawAllData(canvas, graphHeight, maxDataValue)
    }

    fun setData(
        data: List<GraphEntry>,
        forecast: List<GraphEntry> = emptyList(),
        graphInterval: GraphInterval,
        labels: Int = labelsNumber
    ) {
        dataList = data
        forecastList = forecast
        currentGraphInterval = graphInterval
        labelsNumber = labels
        invalidate()
    }

    private fun drawAxes(
        canvas: Canvas,
        graphWidth: Float,
        graphHeight: Float
    ) {
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + graphHeight, linePaint)
        canvas.drawLine(paddingLeft, paddingTop + graphHeight, paddingLeft + graphWidth, paddingTop + graphHeight, linePaint)
    }

    private fun drawAllData(
        canvas: Canvas,
        graphHeight: Float,
        maxDataValue: Int
    ) {
        val validMain = dataList.filter { it.quantity > 0 }
        val validForecast = forecastList.filter { it.quantity > 0 }
        val combinedList = validMain + validForecast

        if (combinedList.isEmpty()) return

        val stretchedStepX: Float = (width - paddingLeft - paddingRight) /
                (combinedList.size - 1).coerceAtLeast(1)

        var previousX: Float? = null
        var previousY: Float? = null

        combinedList.forEachIndexed { index, entry ->
            val x = paddingLeft + index * stretchedStepX
            val y = paddingTop + graphHeight * (1 - entry.quantity.toFloat() / maxDataValue)

            val isForecast = index >= validMain.size

            val currentLinePaint = if (isForecast) forecastLinePaint else linePaint
            val currentPointPaint = if (isForecast) forecastPointPaint else pointPaint

            if (previousX != null && previousY != null) {
                canvas.drawLine(previousX, previousY, x, y, currentLinePaint)
            }

            canvas.drawCircle(x, y, pointRadius, currentPointPaint)
            drawXLabel(canvas, x, graphHeight, entry, index, combinedList.size)

            previousX = x
            previousY = y
        }
    }

    private fun drawXLabel(
        canvas: Canvas,
        x: Float,
        graphHeight: Float,
        entry: GraphEntry,
        index: Int,
        totalSize: Int
    ) {
        val step = if (totalSize > 10) 2 else 1
        if (index % step != 0) {
            return
        }

        val labelText: String = when (currentGraphInterval) {
            GraphInterval.DAILY -> String.format(Locale.getDefault(), "%02d:00", entry.date.hour)
            GraphInterval.WEEKLY -> LocalizationHelper.getDayOfWeekName(context, entry.date.dayOfWeek).take(3)
            GraphInterval.MONTHLY -> String.format(Locale.getDefault(), "%02d.%02d", entry.date.dayOfMonth, entry.date.monthValue)
            GraphInterval.YEARLY -> LocalizationHelper.getMonthName(context, entry.date.month).take(3)
        }

        val textWidth: Float = textPaint.measureText(labelText)
        canvas.withRotation(-45f, x, paddingTop + graphHeight + 60f) {
            drawText(labelText, x - textWidth / 2, paddingTop + graphHeight + 60f, textPaint)
        }
    }

    private fun drawYAxisLabels(
        canvas: Canvas,
        graphHeight: Float,
        maxDataValue: Int
    ) {
        if (maxDataValue == 0) {
            return
        }

        val yPositions: MutableList<Float> = mutableListOf()
        val stepY = 1f
        var value = 0f

        while (value <= maxDataValue) {
            yPositions.add(paddingTop + graphHeight * (1 - value / maxDataValue))
            value += stepY
        }

        val stepIncrement: Int = ceil(yPositions.size / labelsNumber.toFloat()).roundToInt().coerceAtLeast(1)
        var index: Int = yPositions.size - 1

        repeat(times = labelsNumber) {
            val y: Float = yPositions.getOrNull(index) ?: return@repeat
            val labelValue: Int = ((maxDataValue.toFloat() / (yPositions.size - 1)) * index).roundToInt()

            canvas.drawText(labelValue.toString(), paddingLeft - 70f, y + 10f, textPaint)
            index -= stepIncrement

            if (index < 0) {
                return@repeat
            }
        }
    }

    private fun drawEmptyMessage(
        canvas: Canvas
    ) {
        val message = context.getString(R.string.graph_view_info)
        val textWidth = textPaint.measureText(message)
        canvas.drawText(message, (width - textWidth) / 2, height / 2f, textPaint)
    }
}