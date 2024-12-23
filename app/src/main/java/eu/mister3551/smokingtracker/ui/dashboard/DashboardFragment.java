package eu.mister3551.smokingtracker.ui.dashboard;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import eu.mister3551.smokingtracker.R;
import eu.mister3551.smokingtracker.Utils;
import eu.mister3551.smokingtracker.adapter.HistoryAdapter;
import eu.mister3551.smokingtracker.database.Database;
import eu.mister3551.smokingtracker.database.Manager;
import eu.mister3551.smokingtracker.databinding.FragmentDashboardBinding;
import eu.mister3551.smokingtracker.record.History;

public class DashboardFragment extends Fragment implements HistoryInterface {

    private FragmentDashboardBinding binding;
    private RecyclerView recyclerView;
    private Manager manager;
    private TextView text_view_timer_text;
    private TextView text_view_daily_value;
    private TextView text_view_weekly_value;
    private TextView text_view_monthly_value;
    private TextView text_view_current_date_text;
    private Handler handler;
    private Runnable runnable;
    private long timePassed;
    private String currentFinalDate;
    private String currentDate;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        manager = Utils.getManager().get();
        handler = new Handler();

        recyclerView = root.findViewById(R.id.recyclerview_history);

        text_view_timer_text = root.findViewById(R.id.text_view_timer_text);
        text_view_daily_value = root.findViewById(R.id.text_view_daily_value);
        text_view_weekly_value = root.findViewById(R.id.text_view_weekly_value);
        text_view_monthly_value = root.findViewById(R.id.text_view_monthly_value);
        text_view_current_date_text = root.findViewById(R.id.text_view_current_date_text);
        ImageButton image_button_add = root.findViewById(R.id.image_button_add);
        ImageButton image_button_previous_day = root.findViewById(R.id.image_button_previous_day);
        ImageButton image_button_next_day = root.findViewById(R.id.image_button_next_day);

        image_button_add.setOnClickListener(view -> insert());
        image_button_previous_day.setOnClickListener(view -> {
            updateData(updateDate(currentDate, false));
        });
        image_button_next_day.setOnClickListener(view -> {
           updateData(updateDate(currentDate, true));
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentFinalDate = LocalDate.now().toString();
            currentDate = currentFinalDate;
        }
        updateData(currentDate);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        stopTimer();
    }

    @Override
    public void onItemClick(Context context, int position) {

    }

    @Override
    public void onEditClick(Context context, Long id) {
        showPopup(context, id,true);
    }

    @Override
    public void onDeleteClick(Context context, Long id) {
        showPopup(context, id, false);
    }

    private void setParameters(String currentDate) {
        boolean startTimer = true;
        Cursor cursor = manager.fetchAll(currentDate);
        if (cursor.moveToFirst()) {
            do {
                timePassed = cursor.getLong(cursor.getColumnIndexOrThrow("time_passed"));
                text_view_daily_value.setText(cursor.getString(cursor.getColumnIndexOrThrow("daily")));
                text_view_weekly_value.setText(cursor.getString(cursor.getColumnIndexOrThrow("weekly")));
                text_view_monthly_value.setText(cursor.getString(cursor.getColumnIndexOrThrow("monthly")));
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Cursor cursor1 = manager.fetchCurrentDate(currentDate);
            if (cursor1.moveToFirst()) {
                do {
                    startTimer = Boolean.parseBoolean(cursor1.getString(cursor1.getColumnIndexOrThrow("start_timer")));
                } while (cursor1.moveToNext());
                cursor1.close();
            }
        }
        text_view_current_date_text.setText(formattedDate(currentDate));
        text_view_timer_text.setText(formattedDate(timePassed));

        if (startTimer) {
            startTimer();
        }
    }

    private void showHistory(String currentDate) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        HistoryAdapter historyAdapter = new HistoryAdapter(requireContext(), getHistoryList(currentDate), this);
        recyclerView.setAdapter(historyAdapter);
    }

    private List<History> getHistoryList(String currentDate) {
        List<History> historyList = new ArrayList<>();
        Cursor cursor = manager.fetchHistory(currentDate);
        if (cursor.moveToFirst()) {
            do {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow("id_" + Database.HISTORY_TABLE));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                int isLent = cursor.getInt(cursor.getColumnIndexOrThrow("is_lent"));
                historyList.add(new History(id, createdAt, isLent == 1));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return historyList;
    }

    private String formattedDate(Long timePassed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Duration duration = Duration.ofSeconds(timePassed);

            String dayString = duration.toDays() == 1 ? getString(R.string.text_day_1) : getString(R.string.text_day_2);

            return duration.toDays() > 0
                    ? String.format(Locale.getDefault(), "%d " + dayString + " %02d:%02d:%02d", duration.toDays(), duration.toHours() % 24, duration.toMinutes() % 60, duration.getSeconds() % 60)
                    : String.format(Locale.getDefault(), "%02d:%02d:%02d", duration.toHours(), duration.toMinutes() % 60, duration.getSeconds() % 60);
        }
        return "00:00:00";
    }

    private String formattedDate(String currentDate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return LocalDate.parse(currentDate).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }
        return "dd.MM.yyyy";
    }

    private String updateDate(String currentDate, boolean nextDay) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate date = LocalDate.parse(currentDate);
            return this.currentDate = nextDay ? date.plusDays(1).toString() : date.minusDays(1).toString();
        }
        return "dd.MM.yyyy";
    }

    private void insert() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!LocalDate.now().toString().equals(currentFinalDate)) {
                currentFinalDate = LocalDate.now().toString();
                currentDate = currentFinalDate;
                text_view_current_date_text.setText(formattedDate(currentDate));
            } else {
                currentDate = currentFinalDate;
            }
            String fullDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            manager.insertRecord(fullDate);
        }
        setParameters(currentFinalDate);
        showHistory(currentFinalDate);
    }

    private void startTimer() {
        if (handler != null) {
            stopTimer();
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                text_view_timer_text.setText(formattedDate(timePassed));
                timePassed++;
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void stopTimer() {
        handler.removeCallbacks(runnable);
    }

    private void updateData(String currentDate) {
        setParameters(currentDate);
        showHistory(currentDate);
    }

    private void showPopup(Context context, Long id, boolean edit) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.fragment_dashboard_popup, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        HorizontalScrollView horizontal_scroll_view = bottomSheetView.findViewById(R.id.horizontal_scroll_view);
        LinearLayout linear_layout_given_cigarette = bottomSheetView.findViewById(R.id.linear_layout_given_cigarette);
        DatePicker date_picker = bottomSheetView.findViewById(R.id.date_picker);
        TimePicker time_picker = bottomSheetView.findViewById(R.id.time_picker);
        CheckBox check_box_given_cigarette = bottomSheetView.findViewById(R.id.check_box_given_cigarette);
        TextView text_view_message = bottomSheetView.findViewById(R.id.text_view_message);
        Button button_confirm = bottomSheetView.findViewById(R.id.button_confirm);
        Button button_close = bottomSheetView.findViewById(R.id.button_close);

        if (edit) {
            text_view_message.setText(R.string.text_view_message_text_1);
            time_picker.setIs24HourView(true);

            Pair<Calendar, Boolean> pair = fetchById(id);
            Calendar calendar = pair.first;

            date_picker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
            time_picker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            time_picker.setMinute(calendar.get(Calendar.MINUTE));

            check_box_given_cigarette.setChecked(pair.second);

            horizontal_scroll_view.setVisibility(View.VISIBLE);
            linear_layout_given_cigarette.setVisibility(View.VISIBLE);
        }

        button_confirm.setOnClickListener(view -> {
            if (edit) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(date_picker.getYear(), date_picker.getMonth(), date_picker.getDayOfMonth(), time_picker.getHour(), time_picker.getMinute());
                String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.getTime());

                int isLent = check_box_given_cigarette.isChecked() ? 1 : 0;
                manager.update(id, isLent, datetime);
            } else {
                manager.delete(id);
            }
            updateData(currentDate);
            bottomSheetDialog.hide();
        });
        button_close.setOnClickListener(view -> bottomSheetDialog.hide());
        bottomSheetDialog.show();
    }

    private Pair<Calendar, Boolean> fetchById(Long id) {
        Cursor cursor = manager.fetchById(id);

        String createdAt = null;
        int isLent = 0;
        if (cursor.moveToFirst()) {
            do {
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                isLent = cursor.getInt(cursor.getColumnIndexOrThrow("is_lent"));
            } while (cursor.moveToNext());
            cursor.close();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date dateTime;
        try {
            dateTime = simpleDateFormat.parse(createdAt);
        } catch (ParseException parseException) {
            dateTime = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);

        return new Pair<>(calendar, isLent == 1);
    }
}