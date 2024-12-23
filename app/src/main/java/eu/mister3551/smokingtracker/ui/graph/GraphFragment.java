package eu.mister3551.smokingtracker.ui.graph;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.mister3551.smokingtracker.R;
import eu.mister3551.smokingtracker.Utils;
import eu.mister3551.smokingtracker.database.Manager;
import eu.mister3551.smokingtracker.databinding.FragmentGraphBinding;
import eu.mister3551.smokingtracker.record.DataPoint;
import eu.mister3551.smokingtracker.ui.graph.view.GraphView;

public class GraphFragment extends Fragment implements GraphInterface {

    private FragmentGraphBinding binding;
    private Manager manager;
    private LocalDate weeklyDate;
    private LocalDate monthlyDate;
    private LocalDate yearlyDate;
    private TextView text_view_graph_view_weekly;
    private TextView text_view_graph_view_monthly;
    private TextView text_view_graph_view_yearly;
    private TextView text_view_current_date_text_weekly;
    private TextView text_view_current_date_text_monthly;
    private TextView text_view_current_date_text_yearly;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        manager = Utils.getManager().get();

        text_view_graph_view_weekly = root.findViewById(R.id.text_view_graph_view_weekly);
        text_view_graph_view_monthly = root.findViewById(R.id.text_view_graph_view_monthly);
        text_view_graph_view_yearly = root.findViewById(R.id.text_view_graph_view_yearly);
        text_view_current_date_text_weekly = root.findViewById(R.id.text_view_current_date_text_weekly);
        text_view_current_date_text_monthly = root.findViewById(R.id.text_view_current_date_text_monthly);
        text_view_current_date_text_yearly = root.findViewById(R.id.text_view_current_date_text_yearly);
        ImageButton image_button_previous_day_weekly = root.findViewById(R.id.image_button_previous_day_weekly);
        ImageButton image_button_next_day_weekly = root.findViewById(R.id.image_button_next_day_weekly);
        ImageButton image_button_previous_day_monthly = root.findViewById(R.id.image_button_previous_day_monthly);
        ImageButton image_button_next_day_monthly = root.findViewById(R.id.image_button_next_day_monthly);
        ImageButton image_button_previous_day_yearly = root.findViewById(R.id.image_button_previous_day_yearly);
        ImageButton image_button_next_day_yearly = root.findViewById(R.id.image_button_next_day_yearly);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate now = LocalDate.now();
            weeklyDate = now;
            monthlyDate = now;
            yearlyDate = now;
        }

        image_button_previous_day_weekly.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                weeklyDate = weeklyDate.minusWeeks(1);
            }
            showData();
        });

        image_button_next_day_weekly.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                weeklyDate = weeklyDate.plusWeeks(1);
            }
            showData();
        });

        image_button_previous_day_monthly.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                monthlyDate = monthlyDate.minusMonths(1);
            }
            showData();
        });

        image_button_next_day_monthly.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                monthlyDate = monthlyDate.plusMonths(1);
            }
            showData();
        });

        image_button_previous_day_yearly.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                yearlyDate = yearlyDate.minusYears(1);
            }
            showData();
        });

        image_button_next_day_yearly.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                yearlyDate = yearlyDate.plusYears(1);
            }
            showData();
        });

        showData();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPointClick(DataPoint dataPoint, float x, float y) {
        showPopup(String.valueOf((int) dataPoint.value()));
    }

    private void showData() {
        if (Utils.getSettings() == null) {
            return;
        }

        updateTextViewAndGraph(
                getWeekData(weeklyDate),
                R.string.text_view_weekly,
                text_view_graph_view_weekly,
                GraphView.DateTypes.WEEKLY
        );

        updateTextViewAndGraph(
                getMonthData(monthlyDate),
                R.string.text_view_monthly,
                text_view_graph_view_monthly,
                GraphView.DateTypes.MONTHLY
        );

        updateTextViewAndGraph(
                getYearData(yearlyDate),
                R.string.text_view_yearly,
                text_view_graph_view_yearly,
                GraphView.DateTypes.YEARLY
        );
    }

    private List<DataPoint> getWeekData(LocalDate localDate) {
        List<DataPoint> dataPoints = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startOfWeek = localDate.with(DayOfWeek.MONDAY);
            LocalDate endOfWeek = localDate.with(DayOfWeek.SUNDAY);

            String weekName = startOfWeek.format(DateTimeFormatter.ofPattern("dd.MM")) + "/" + endOfWeek.format(DateTimeFormatter.ofPattern("dd.MM")) + " " + localDate.getYear();
            text_view_current_date_text_weekly.setText(weekName);

            Cursor cursor = manager.fetchByWeek(startOfWeek.format(formatter), endOfWeek.format(formatter));

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    dataPoints.add(new DataPoint(
                            cursor.getString(cursor.getColumnIndexOrThrow("date")),
                            cursor.getFloat(cursor.getColumnIndexOrThrow("value"))
                    ));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return dataPoints;
    }

    private List<DataPoint> getMonthData(LocalDate date) {
        List<DataPoint> dataPoints = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate startDateOfMonth = date.withDayOfMonth(1);
            LocalDate endDateOfMonth = date.withDayOfMonth(date.lengthOfMonth());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String monthName = GraphView.getMonth(getContext(), date.toString()) + " " + date.getYear();
            text_view_current_date_text_monthly.setText(monthName);

            Cursor cursor = manager.fetchByMonth(startDateOfMonth.format(dateTimeFormatter), endDateOfMonth.format(dateTimeFormatter));

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String[] dateParts = cursor.getString(cursor.getColumnIndexOrThrow("date")).split("-");
                    float value = cursor.getFloat(cursor.getColumnIndexOrThrow("value"));
                    dataPoints.add(new DataPoint(dateParts[2] + "." + dateParts[1], value));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return dataPoints;
    }

    private List<DataPoint> getYearData(LocalDate date) {
        List<DataPoint> dataPoints = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate startOfYear = date.withDayOfYear(1);
            LocalDate endOfYear = date.withDayOfYear(date.lengthOfYear());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            text_view_current_date_text_yearly.setText(String.valueOf(date.getYear()));

            Cursor cursor = manager.fetchByYear(startOfYear.format(dateTimeFormatter), endOfYear.format(dateTimeFormatter));

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String dateString = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    float value = cursor.getFloat(cursor.getColumnIndexOrThrow("value"));
                    dataPoints.add(new DataPoint(dateString, value));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return dataPoints;
    }

    private void showGraph(View view, List<DataPoint> dataPointList, GraphView.DateTypes dateType) {
        if (Utils.getSettings() == null) {
            return;
        }

        GraphView graphView = null;
        switch (dateType) {
            case WEEKLY -> graphView = view.findViewById(R.id.graph_view_weekly);
            case MONTHLY -> graphView = view.findViewById(R.id.graph_view_monthly);
            case YEARLY ->  graphView = view.findViewById(R.id.graph_view_yearly);
        }

        if (graphView != null) {
            graphView.setDataPoints(dataPointList);
            graphView.setColor(Utils.getSettings().getGraph().getColor());
            graphView.setPointColor(Utils.settings.getGraph().getPointColors());
            graphView.setLineColor(Utils.getSettings().getGraph().getLineColors());
            graphView.setPaintStyle(Utils.getSettings().getGraph().getPaintStyle());
            graphView.setDateType(dateType);
            graphView.clickable(this);
            graphView.show();
        }
    }

    private void updateTextViewAndGraph(List<DataPoint> data, int stringResId, TextView textView, GraphView.DateTypes dateType) {
        float value = data.stream().map(DataPoint::value).reduce(0f, Float::sum);
        String text = String.format(Locale.getDefault(), "%s (%d)", getString(stringResId), (int) value);
        textView.setText(text);
        showGraph(binding.getRoot(), data, dateType);
    }

    private void showPopup(String value) {
        if (getContext() == null) {
            return;
        }
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_graph_popup, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textViewValue = popupView.findViewById(R.id.text_view_value);

        String valueText = String.format(Locale.getDefault(), "%s: %s", getString(R.string.text_view_value), value);

        textViewValue.setText(valueText);

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(popupWindow::dismiss, 1500);
    }
}