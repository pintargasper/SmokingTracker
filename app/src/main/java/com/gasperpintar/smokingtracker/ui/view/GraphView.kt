package com.gasperpintar.smokingtracker.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
    private var labelsNumber: Int = 5
    private var currentGraphInterval: GraphInterval = GraphInterval.WEEKLY

    private val paddingLeft: Float = 80f
    private val paddingRight: Float = 80f
    private val paddingTop: Float = 60f
    private val paddingBottom: Float = 90f
    private val pointRadius: Float = 12f

    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorOnSurface,
            typedValue,
            true
        )
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

    fun setData(data: List<GraphEntry>, graphInterval: GraphInterval, labels: Int = labelsNumber) {
        dataList = data
        currentGraphInterval = graphInterval
        labelsNumber = labels
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataList.isEmpty()) {
            val message: String = context.getString(R.string.graph_view_info)
            val textWidth: Float = textPaint.measureText(message)
            val textHeight: Float = textPaint.descent() - textPaint.ascent()
            val x: Float = (width - textWidth) / 2
            val y: Float = (height + textHeight) / 2 - textPaint.descent()
            canvas.drawText(message, x, y, textPaint)
            return
        }

        val graphWidth: Float = width - paddingLeft - paddingRight
        val graphHeight: Float = height - paddingTop - paddingBottom
        val maxDataValue: Int = dataList.maxOf { entry: GraphEntry -> entry.quantity }.coerceAtLeast(1)

        drawAxes(canvas, graphWidth, graphHeight)
        drawYAxisLabels(canvas, graphHeight, maxDataValue)
        drawDataPoints(canvas, graphHeight, maxDataValue)
    }

    private fun drawAxes(canvas: Canvas, graphWidth: Float, graphHeight: Float) {
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + graphHeight, linePaint)
        canvas.drawLine(paddingLeft, paddingTop + graphHeight, paddingLeft + graphWidth, paddingTop + graphHeight, linePaint)
    }

    private fun drawDataPoints(
        canvas: Canvas,
        graphHeight: Float,
        maxDataValue: Int
    ) {
        var previousX: Float? = null
        var previousY: Float? = null

        val validEntries: List<Pair<Int, GraphEntry>> =
            dataList.withIndex()
                .filter { it.value.quantity > 0 }
                .map { it.index to it.value }

        if (validEntries.isEmpty()) {
            return
        }

        val stretchedStepX: Float =
            (width - paddingLeft - paddingRight) /
                    (validEntries.size - 1).coerceAtLeast(1)

        val step: Int = when {
            currentGraphInterval == GraphInterval.DAILY && validEntries.size <= 15 -> 1
            validEntries.size > 15 -> 2
            else -> 1
        }

        validEntries.forEachIndexed { visibleIndex: Int, pair: Pair<Int, GraphEntry> ->

            val originalIndex: Int = pair.first
            val entry: GraphEntry = pair.second

            val x: Float = paddingLeft + visibleIndex * stretchedStepX
            val y: Float = paddingTop + graphHeight * (1 - entry.quantity.toFloat() / maxDataValue)

            if (previousX != null && previousY != null) {
                canvas.drawLine(previousX, previousY, x, y, linePaint)
            }

            canvas.drawCircle(x, y, pointRadius, pointPaint)

            if (visibleIndex % step == 0) {
                val labelText: String = when (currentGraphInterval) {
                    GraphInterval.DAILY -> String.format(Locale.getDefault(), "%02d:00", originalIndex)
                    GraphInterval.WEEKLY -> LocalizationHelper.getDayOfWeekName(context, entry.date.dayOfWeek).take(3)
                    GraphInterval.MONTHLY ->
                        String.format(
                            Locale.getDefault(),
                            "%02d.%02d",
                            entry.date.dayOfMonth,
                            entry.date.monthValue
                        )
                    GraphInterval.YEARLY -> LocalizationHelper.getMonthName(context, entry.date.month).take(3)
                }

                val textWidth: Float = textPaint.measureText(labelText)
                canvas.withRotation(-45f, x, paddingTop + graphHeight + 60f) {
                    drawText(
                        labelText,
                        x - textWidth / 2,
                        paddingTop + graphHeight + 60f,
                        textPaint
                    )
                }
            }
            previousX = x
            previousY = y
        }
    }

    private fun drawYAxisLabels(canvas: Canvas, graphHeight: Float, maxDataValue: Int) {
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
            val labelValue: Int = if (index == yPositions.size - 1) {
                maxDataValue
            } else {
                ((maxDataValue / yPositions.size.toFloat()) * index).roundToInt()
            }

            canvas.drawText(labelValue.toString(), paddingLeft - 80f, y + 15f, textPaint)

            index -= stepIncrement
            if (index < 0) {
                return@repeat
            }
        }
    }
}