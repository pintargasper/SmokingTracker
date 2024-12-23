package eu.mister3551.smokingtracker.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;

import java.sql.SQLDataException;
import java.util.List;

import eu.mister3551.smokingtracker.R;

public class Manager {

    private Database database;
    private final Context context;
    private SQLiteDatabase sqLiteDatabase;

    public Manager(Context context) {
        this.context = context;
    }

    public void open() throws SQLDataException {
        database = new Database(context);
        sqLiteDatabase = database.getReadableDatabase();
    }

    public void close() {
        database.close();
    }

    public void insertRecord(String currentDate) {
        sqLiteDatabase.execSQL(Queries.updateTimer);
        sqLiteDatabase.execSQL(Queries.insertRecord(currentDate));
    }
    
    public void delete(Long id) {
        sqLiteDatabase.execSQL(Queries.deleteRecord(id));
    }

    public void update(Long id, int isLent, String customDate) {
        sqLiteDatabase.execSQL(Queries.updateRecord(id, isLent, customDate));
    }

    public Cursor fetchCurrentDate(String currentDate) {
        return sqLiteDatabase.rawQuery(Queries.fetchCurrentDate(currentDate), null);
    }

    public Cursor fetchAll(String currentDate) {
        return sqLiteDatabase.rawQuery(Queries.fetchAll(currentDate), null);
    }

    public Cursor fetchHistory(String currentDate) {
        return sqLiteDatabase.rawQuery(Queries.fetchHistory(currentDate), null);
    }

    public Cursor fetchById(Long id) {
        return sqLiteDatabase.rawQuery(Queries.fetchById(id), null);
    }

    public Cursor fetchByWeek(String minDate, String maxDate) {
        return sqLiteDatabase.rawQuery(Queries.fetchByWeekAndMonth(minDate, maxDate), null);
    }

    public Cursor fetchByMonth(String minDate, String maxDate) {
        return sqLiteDatabase.rawQuery(Queries.fetchByWeekAndMonth(minDate, maxDate), null);
    }

    public Cursor fetchByYear(String minDate, String maxDate) {
        return sqLiteDatabase.rawQuery(Queries.fetchByYear(minDate, maxDate), null);
    }

    public Cursor fetchSettings() {
        return sqLiteDatabase.rawQuery(Queries.fetchSettings, null);
    }

    public void update(String language) {
        sqLiteDatabase.execSQL(Queries.updateSettings(language));
    }

    public void update(int color, String pointColors, String lineColors) {
        sqLiteDatabase.execSQL(Queries.updateGraph(color, pointColors, lineColors));
    }

    public void update(Paint.Style paintStyle) {
        sqLiteDatabase.execSQL(Queries.updatePaintStyle(paintStyle));
    }

    public Cursor getMainData() {
        return sqLiteDatabase.rawQuery(Queries.getMainData(), null);
    }

    public Cursor getSettingsData() {
        return sqLiteDatabase.rawQuery(Queries.getSettingsData(), null);
    }

    public Cursor getGraphData() {
        return sqLiteDatabase.rawQuery(Queries.getGraphData(), null);
    }

    public Cursor getHistoryData() {
        return sqLiteDatabase.rawQuery(Queries.getHistoryData(), null);
    }

    public String insertFromFile(List<String> mainQueries, List<String> settingsQueries, List<String> graphQueries, List<String> historyQueries) {
        sqLiteDatabase.execSQL(Queries.truncateMain());
        sqLiteDatabase.execSQL(Queries.truncateSettings());
        sqLiteDatabase.execSQL(Queries.truncateGraph());
        sqLiteDatabase.execSQL(Queries.truncateHistory());

        sqLiteDatabase.execSQL(Queries.resetMainSequence());
        sqLiteDatabase.execSQL(Queries.resetSettingsSequence());
        sqLiteDatabase.execSQL(Queries.resetGraphSequence());
        sqLiteDatabase.execSQL(Queries.resetHistorySequence());

        for (String query : mainQueries) {
            sqLiteDatabase.execSQL(query);
        }

        for (String query : settingsQueries) {
            sqLiteDatabase.execSQL(query);
        }

        for (String query : graphQueries) {
            sqLiteDatabase.execSQL(query);
        }

        for (String query : historyQueries) {
            sqLiteDatabase.execSQL(query);
        }
        return context.getString(R.string.str_data_imported);
    }
}