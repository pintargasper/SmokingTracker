package eu.mister3551.smokingtracker.database.download;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.mister3551.smokingtracker.R;
import eu.mister3551.smokingtracker.database.Manager;
import eu.mister3551.smokingtracker.database.Queries;
import eu.mister3551.smokingtracker.database.interface_.Callback;

public class Data {

    public final String XLSX = "xlsx";
    private final Manager manager;
    private final Context context;

    public Data(Manager manager, Context context) {
        this.manager = manager;
        this.context = context;
    }

    public void download(String format, Callback callback) {
        if (format.equals(XLSX)) {
            callback.onSuccess(exportToExcel());
        } else {
            callback.onError(context.getString(R.string.str_something_went_wrong));
        }
    }

    public void upload(Uri uri, String format, Callback callback) {
        if (format.equals(XLSX)) {
            callback.onSuccess(importFromExcel(uri));
        } else {
            callback.onError(context.getString(R.string.str_something_went_wrong));
        }
    }

    private String exportToExcel() {
        Cursor mainCursor = manager.getMainData();
        Cursor settingsCursor = manager.getSettingsData();
        Cursor graphCursor = manager.getGraphData();
        Cursor historyCursor = manager.getHistoryData();

        if (mainCursor == null || settingsCursor == null || graphCursor == null || historyCursor == null) {
            return context.getString(R.string.str_no_data);
        }

        Workbook workbook = new XSSFWorkbook();

        writeCursorToSheet(workbook, mainCursor, "Main");
        writeCursorToSheet(workbook, settingsCursor, "Settings");
        writeCursorToSheet(workbook, graphCursor, "Graph");
        writeCursorToSheet(workbook, historyCursor, "History");

        try {
            String fileName = String.format("%s.xlsx", context.getString(R.string.str_file_name));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = context.getContentResolver();
                Uri fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (fileUri != null) {
                    OutputStream fileOut = resolver.openOutputStream(fileUri);
                    if (fileOut != null) {
                        workbook.write(fileOut);
                        fileOut.close();
                    }
                    workbook.close();
                    return context.getString(R.string.str_excel_file);
                } else {
                    return context.getString(R.string.str_file_error);
                }
            } else {
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!directory.exists() && !directory.mkdirs()) {
                    return context.getString(R.string.str_something_went_wrong);
                }
                File file = new File(directory, fileName);
                FileOutputStream fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();
            }
        } catch (Exception exception) {
            return context.getString(R.string.str_something_went_wrong);
        }
        return context.getString(R.string.str_something_went_wrong);
    }

    private void writeCursorToSheet(Workbook workbook, Cursor cursor, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cursor.getColumnName(i));
        }

        int rowIndex = 1;
        while (cursor.moveToNext()) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                Cell cell = row.createCell(i);
                switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_INTEGER -> cell.setCellValue(cursor.getInt(i));
                    case Cursor.FIELD_TYPE_FLOAT -> cell.setCellValue(cursor.getDouble(i));
                    case Cursor.FIELD_TYPE_STRING -> cell.setCellValue(cursor.getString(i));
                    case Cursor.FIELD_TYPE_BLOB -> cell.setCellValue("BLOB Data");
                    case Cursor.FIELD_TYPE_NULL -> cell.setCellValue("NULL");
                    default -> throw new IllegalArgumentException("Unknown cursor field type");
                }
            }
        }
        cursor.close();
    }

    public String importFromExcel(Uri uri) {
        List<String> mainQueries = new ArrayList<>();
        List<String> settingsQueries = new ArrayList<>();
        List<String> graphQueries = new ArrayList<>();
        List<String> historyQueries = new ArrayList<>();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            if (inputStream != null) {
                Workbook workbook = new XSSFWorkbook(inputStream);

                Sheet mainSheet = workbook.getSheet("Main");
                if (mainSheet != null) {
                    readSheetData(mainSheet, mainQueries);
                }

                Sheet settingsSheet = workbook.getSheet("Settings");
                if (settingsSheet != null) {
                    readSheetData(settingsSheet, settingsQueries);
                }

                Sheet graphSheet = workbook.getSheet("Graph");
                if (graphSheet != null) {
                    readSheetData(graphSheet, graphQueries);
                }

                Sheet historySheet = workbook.getSheet("History");
                if (historySheet != null) {
                    readSheetData(historySheet, historyQueries);
                }

                workbook.close();
                inputStream.close();

                return manager.insertFromFile(mainQueries, settingsQueries, graphQueries, historyQueries);
            } else {
                return context.getString(R.string.str_excel_open_file);
            }
        } catch (IOException e) {
            return context.getString(R.string.str_excel_open_file);
        }
    }

    private void readSheetData(Sheet sheet, List<String> queries) {
        String sheetName = sheet.getSheetName();

        for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (row != null) {
                StringBuilder values = new StringBuilder();

                for (int colIndex = 0; colIndex < row.getPhysicalNumberOfCells(); colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    String cellValue = "";

                    if (cell != null) {
                        cellValue = switch (cell.getCellType()) {
                            case STRING -> "'" + cell.getStringCellValue().replace("'", "''") + "'";
                            case NUMERIC -> {
                                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                    yield "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(cell.getDateCellValue()) + "'";
                                } else {
                                    yield String.valueOf(cell.getNumericCellValue());
                                }
                            }
                            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                            case FORMULA -> "'" + cell.getCellFormula() + "'";
                            default -> "NULL";
                        };
                    }

                    if (colIndex > 0) {
                        values.append(", ");
                    }
                    values.append(cellValue);
                }
                queries.add(
                        switch (sheetName) {
                            case "Main" -> Queries.newMainRecord(values);
                            case "Settings" -> Queries.newSettingsRecord(values);
                            case "Graph" -> Queries.newGraphRecord(values);
                            case "History" -> Queries.newHistoryRecord(values);
                            default -> null;
                        }
                );
            }
        }
    }
}