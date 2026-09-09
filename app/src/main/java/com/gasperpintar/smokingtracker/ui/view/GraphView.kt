package com.gasperpintar.smokingtracker.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.withRotation
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.GraphEntry
import com.gasperpintar.smokingtracker.type.GraphInterval
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import java.time.LocalDateTime
import kotlin.math.ceil
import kotlin.math.roundToInt

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private fun Float.dp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)
    private fun Float.sp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, resources.displayMetrics)

    private val themeColor by lazy {
        TypedValue().also { context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, it, true) }.data
    }

    private val paddingLeft = 36f.dp()
    private val paddingRight = 24f.dp()
    private val paddingTop = 24f.dp()
    private val paddingBottom = 48f.dp()
    private val pointRadius = 4f.dp()

    private var combinedEntries = emptyList<GraphEntry>()
    private var validMainCount = 0
    private var maxDataValue = 1
    private var labelsCount = 5
    private var currentGraphInterval = GraphInterval.WEEKLY
    private var isForecastGraph = false

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f.sp() }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 2f.dp(); style = Paint.Style.STROKE }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val fillPath = Path()
    private val dashEffect = DashPathEffect(floatArrayOf(6f.dp(), 4f.dp()), 0f)

    init {
        if (isInEditMode) {
            val now = LocalDateTime.now()
            setData(listOf(GraphEntry(quantity = 5, date = now.minusDays(3)), GraphEntry(quantity = 12, date = now)), graphInterval = GraphInterval.WEEKLY)
        }
    }

    @Override
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateGradientShader()
    }

    @Override
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textPaint.color = themeColor

        if (combinedEntries.isEmpty()) {
            val message = context.getString(R.string.graph_view_info)
            canvas.drawText(message, (width - textPaint.measureText(message)) / 2f, height / 2f, textPaint)
            return
        }

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        val yLabels = getYAxisLabels(maxDataValue)
        yLabels.forEachIndexed { _, labelValue ->
            val yPosition = paddingTop + graphHeight * (1f - labelValue.toFloat() / maxDataValue)
            canvas.drawText(labelValue.toString(), paddingLeft - 28f.dp(), yPosition + 4f.dp(), textPaint)
        }

        val stepX = graphWidth / (combinedEntries.size - 1).coerceAtLeast(minimumValue = 1)
        val labelStep = if (combinedEntries.size > 10) 2 else 1

        fillPath.reset()
        fillPath.moveTo(paddingLeft, paddingTop + graphHeight)

        var previousX = -1f
        var previousY = -1f

        combinedEntries.forEachIndexed { index, entry ->
            val xPosition = paddingLeft + index * stepX
            val yPosition = paddingTop + graphHeight * (1 - entry.quantity.toFloat() / maxDataValue)
            val isForecastItem = index >= validMainCount

            fillPath.lineTo(xPosition, yPosition)

            if (previousX >= 0) {
                linePaint.color = if (isForecastItem) themeColor else Color.GRAY
                linePaint.pathEffect = if (isForecastItem) dashEffect else null
                canvas.drawLine(previousX, previousY, xPosition, yPosition, linePaint)
            }

            fillPaint.shader = null
            fillPaint.color = if (isForecastItem) themeColor else Color.GRAY
            canvas.drawCircle(xPosition, yPosition, pointRadius, fillPaint)

            if (index % labelStep == 0) drawXLabel(canvas, xPosition, graphHeight, entry)
            previousX = xPosition
            previousY = yPosition
        }

        fillPath.lineTo(previousX, paddingTop + graphHeight)
        fillPath.close()
        updateGradientShader()
        canvas.drawPath(fillPath, fillPaint)
    }

    fun setData(
        data: List<GraphEntry>,
        forecast: List<GraphEntry> = emptyList(),
        graphInterval: GraphInterval,
        labels: Int = 5,
        isForecast: Boolean = false
    ) {
        this.currentGraphInterval = graphInterval
        this.labelsCount = labels
        this.isForecastGraph = isForecast

        val validMain = data.filter { it.quantity > 0 }
        val validForecast = forecast.filter { it.quantity > 0 }

        validMainCount = validMain.size
        combinedEntries = validMain + validForecast
        maxDataValue = maxOf(
            a = validMain.maxOfOrNull { it.quantity } ?: 0,
            b = validForecast.maxOfOrNull { it.quantity } ?: 0
        ).coerceAtLeast(minimumValue = 1)

        updateGradientShader()
        invalidate()
    }

    private fun getYAxisLabels(maxDataValue: Int): List<Int> {
        if (maxDataValue == 0) return emptyList()
        val stepIncrement = ceil(x = (maxDataValue + 1) / 5f).roundToInt().coerceAtLeast(minimumValue = 1)
        return generateSequence(seed = maxDataValue) { index ->
            (index - stepIncrement).takeIf { it >= 0 }
        }.toList()
    }

    private fun updateGradientShader() {
        val graphHeight = height - paddingTop - paddingBottom
        if (graphHeight > 0) {
            val semiTransparentThemeColor = (themeColor and 0x00FFFFFF) or 0x28000000
            fillPaint.shader = LinearGradient(
                0f, paddingTop, 0f, paddingTop + graphHeight,
                semiTransparentThemeColor, Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
        }
    }

    private fun drawXLabel(canvas: Canvas, xPosition: Float, graphHeight: Float, entry: GraphEntry) {
        val date = entry.date
        val labelText = when (currentGraphInterval) {
            GraphInterval.HOURLY -> "%02d:00".format(date.hour)
            GraphInterval.DAILY -> if (isForecastGraph) "${date.dayOfMonth}.${date.monthValue}" else "%02d:00".format(date.hour)
            GraphInterval.WEEKLY -> if (isForecastGraph) "${date.dayOfMonth}.${date.monthValue}" else LocalizationHelper.getDayOfWeekName(context, date.dayOfWeek).take(n = 3)
            GraphInterval.MONTHLY -> if (isForecastGraph) LocalizationHelper.getMonthName(context, date.month).take(n = 3) else "${date.dayOfMonth}.${date.monthValue}"
            else -> LocalizationHelper.getMonthName(context, date.month).take(n = 3)
        }

        val yPosition = paddingTop + graphHeight + 20f.dp()
        canvas.withRotation(-45f, xPosition, yPosition) {
            drawText(labelText, xPosition - textPaint.measureText(labelText) / 2f, yPosition, textPaint)
        }
    }
}