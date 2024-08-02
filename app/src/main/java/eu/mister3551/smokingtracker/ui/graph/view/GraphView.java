package eu.mister3551.smokingtracker.ui.graph.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import eu.mister3551.smokingtracker.R;
import eu.mister3551.smokingtracker.record.DataPoint;
import eu.mister3551.smokingtracker.ui.graph.GraphInterface;

public class GraphView extends View {

    private List<DataPoint> dataPoints;
    private List<Integer> pointColors;
    private List<Integer> lineColors;
    private Paint graphPaint;
    private Paint pointPaint;
    private Paint linePaint;
    private Paint textPaint;
    private Rect textBounds;
    private float maxDataPoint;
    private boolean show;
    private int graphColor;
    private Paint.Style paintStyle;
    private DateTypes dateType;

    public enum DateTypes {
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    private GraphInterface graphInterface;

    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dataPoints = new ArrayList<>();
        pointColors = new ArrayList<>();
        lineColors = new ArrayList<>();

        textBounds = new Rect();

        show = false;
        graphColor = Color.BLUE;
        paintStyle = Paint.Style.STROKE;
        dateType = DateTypes.WEEKLY;

        graphPaint = new Paint();
        graphPaint.setColor(graphColor);
        graphPaint.setStrokeWidth(5);
        graphPaint.setStyle(paintStyle);

        pointPaint = new Paint();
        pointPaint.setStyle(paintStyle);

        linePaint = new Paint();
        linePaint.setStrokeWidth(5);
        linePaint.setStyle(paintStyle);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!show) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        drawYAxisLabels(canvas, height);
        drawXAxisLabels(canvas, width, height);
        drawAxes(canvas, width, height);
        drawDataPoints(canvas, width, height);
    }

    private void drawYAxisLabels(Canvas canvas, int height) {
        List<Float> yPositions = new ArrayList<>();
        int labelsNumber = 5;
        float stepY = 1;

        if (maxDataPoint != 0) {
            for (float i = 0; i <= maxDataPoint; i += stepY) {
                float y = height - 100 - i * (height - 210) / maxDataPoint;
                yPositions.add(y);
            }

            int stepIncrement = (int) Math.ceil(maxDataPoint / labelsNumber);

            if (stepIncrement != 0) {
                for (int i = yPositions.size() - 1; i > 0; i -= stepIncrement) {
                    float y = yPositions.get(i);
                    canvas.drawText(String.valueOf(i), 20, y, textPaint);
                }
            }
        }
    }

    private void drawXAxisLabels(Canvas canvas, int width, int height) {
        float totalWidth = width - 200;
        float stepX = dataPoints.size() > 1 ? totalWidth / (dataPoints.size() - 1) : totalWidth;
        int step = dataPoints.size() > 12 ? 2 : 1;
        for (int i = 0; i < dataPoints.size(); i += step) {
            float x = 140 + i * stepX;
            String label = getLabelForDate(dataPoints.get(i).date());
            if (label != null) {
                textPaint.getTextBounds(label, 0, label.length(), textBounds);
                float textWidth = textBounds.width();
                canvas.save();
                canvas.rotate(-45, x, height - 10);
                canvas.drawText(label, x - textWidth / 2, height - 50, textPaint);
                canvas.restore();
            }
        }
    }

    private void drawAxes(Canvas canvas, int width, int height) {
        canvas.drawLine(100, height - 100, width - 100, height - 100, graphPaint);
        canvas.drawLine(100, height - 100, 100, 100, graphPaint);
    }

    private void drawDataPoints(Canvas canvas, int width, int height) {
        float totalWidth = width - 200;
        float stepX = dataPoints.size() > 1 ? totalWidth / (dataPoints.size() - 1) : totalWidth;

        for (int i = 0; i < dataPoints.size(); i++) {
            float x = 100 + i * stepX;
            float y = height - 100 - dataPoints.get(i).value() * (height - 200) / maxDataPoint;

            int pointColor = pointColors.isEmpty() ? graphColor : pointColors.get(i % pointColors.size());
            pointPaint.setColor(pointColor);
            canvas.drawCircle(x, y, 10, pointPaint);

            if (i > 0) {
                float prevX = 100 + (i - 1) * stepX;
                float prevY = height - 100 - dataPoints.get(i - 1).value() * (height - 200) / maxDataPoint;

                int lineColor = lineColors.isEmpty() ? graphColor : lineColors.get((i - 1) % lineColors.size());
                linePaint.setColor(lineColor);
                canvas.drawLine(prevX, prevY, x, y, linePaint);
            }
        }
    }

    private float getMaxDataPoint() {
        float max = Float.MIN_VALUE;
        for (DataPoint dataPoint : dataPoints) {
            if (dataPoint.value() > max) {
                max = dataPoint.value();
            }
        }
        return max;
    }

    private String getLabelForDate(String date) {
        return switch (dateType) {
            case WEEKLY -> getDayOfWeek(date);
            case MONTHLY -> date;
            case YEARLY -> getMonth(getContext(), date);
        };
    }

    private String getDayOfWeek(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(date));
            return switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SUNDAY -> getContext().getString(R.string.str_sunday);
                case Calendar.MONDAY -> getContext().getString(R.string.str_monday);
                case Calendar.TUESDAY -> getContext().getString(R.string.str_tuesday);
                case Calendar.WEDNESDAY -> getContext().getString(R.string.str_wednesday);
                case Calendar.THURSDAY -> getContext().getString(R.string.str_thursday);
                case Calendar.FRIDAY -> getContext().getString(R.string.str_friday);
                case Calendar.SATURDAY -> getContext().getString(R.string.str_saturday);
                default -> "None";
            };
        } catch (ParseException e) {
            return "None";
        }
    }

    public static String getMonth(Context context, String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                calendar.setTime(simpleDateFormat.parse(date));
                return switch (calendar.get(Calendar.MONTH)) {
                    case Calendar.JANUARY -> context.getString(R.string.str_january);
                    case Calendar.FEBRUARY -> context.getString(R.string.str_february);
                    case Calendar.MARCH -> context.getString(R.string.str_march);
                    case Calendar.APRIL -> context.getString(R.string.str_april);
                    case Calendar.MAY -> context.getString(R.string.str_may);
                    case Calendar.JUNE -> context.getString(R.string.str_june);
                    case Calendar.JULY -> context.getString(R.string.str_july);
                    case Calendar.AUGUST -> context.getString(R.string.str_august);
                    case Calendar.SEPTEMBER -> context.getString(R.string.str_september);
                    case Calendar.OCTOBER -> context.getString(R.string.str_october);
                    case Calendar.NOVEMBER -> context.getString(R.string.str_november);
                    case Calendar.DECEMBER -> context.getString(R.string.str_december);
                    default -> "None";
                };
            } catch (ParseException e) {
                System.out.println(e.getMessage());
            }
        }
        return "None";
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
        maxDataPoint = getMaxDataPoint();
        invalidate();
    }

    public void setColor(int color) {
        this.graphColor = color;
        graphPaint.setColor(color);
        invalidate();
    }

    public void setPointColor(List<Integer> colors) {
        this.pointColors = colors;
        invalidate();
    }

    public void setLineColor(List<Integer> colors) {
        this.lineColors = colors;
        invalidate();
    }

    public void setPaintStyle(Paint.Style paintStyle) {
        this.paintStyle = paintStyle;
        graphPaint.setStyle(paintStyle);
        pointPaint.setStyle(paintStyle);
        linePaint.setStyle(paintStyle);
        invalidate();
    }

    public void show() {
        show = true;
        invalidate();
    }

    public void setDateType(DateTypes dateType) {
        this.dateType = dateType;
        invalidate();
    }

    public void clickable(GraphInterface graphInterface) {
        this.graphInterface = graphInterface;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && graphInterface != null) {
            float x = event.getX();
            float y = event.getY();
            for (int i = 0; i < dataPoints.size(); i++) {
                float px = 100 + i * ((float) (getWidth() - 200) / (dataPoints.size() > 1 ? dataPoints.size() - 1 : 1));
                float py = getHeight() - 100 - dataPoints.get(i).value() * ((getHeight() - 200) / maxDataPoint);
                if (Math.abs(x - px) <= 20 && Math.abs(y - py) <= 20) {
                    graphInterface.onPointClick(dataPoints.get(i), px, py);
                    graphInterface.onPointClick(dataPoints.get(i), px, py);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}