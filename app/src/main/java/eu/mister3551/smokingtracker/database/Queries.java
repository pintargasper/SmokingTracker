package eu.mister3551.smokingtracker.database;

import android.graphics.Paint;

public class Queries {

    public static String createMain = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (" +
                    "id_%1$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT (DATETIME('now', 'localtime')));",
            Database.MAIN_TABLE
    );

    public static String createHistory = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (" +
                    "id_%1$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "is_lent INTEGER NOT NULL CHECK (is_lent IN (0,1)), " +
                    "created_at TIMESTAMP NOT NULL);",
            Database.HISTORY_TABLE
    );

    public static String createSettings = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (" +
                    "id_%1$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "language CHAR(5) NOT NULL DEFAULT 'EN');",
            Database.SETTINGS_TABLE
    );

    public static String createGraph = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (" +
                    "id_%1$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "color INTEGER NOT NULL DEFAULT 0xFF888888, " +
                    "point_color TEXT NOT NULL DEFAULT 0xFF888888, " +
                    "line_color TEXT NOT NULL DEFAULT 0xFF888888, " +
                    "paint_style TEXT NOT NULL DEFAULT 'FILL'" +
                    ");",
            Database.GRAPH_TABLE
    );

    public static String dropMain = String.format(
            "DROP TABLE IF EXISTS %s",
            Database.MAIN_TABLE
    );

    public static String dropHistory = String.format(
            "DROP TABLE IF EXISTS %s",
            Database.HISTORY_TABLE
    );

    public static String dropSettings = String.format(
            "DROP TABLE IF EXISTS %s",
            Database.SETTINGS_TABLE
    );

    public static String dropGraph = String.format(
            "DROP TABLE IF EXISTS %s",
            Database.GRAPH_TABLE
    );

    public static String fetchCurrentDate(String currentDate) {
        return String.format("SELECT strftime('%%d.%%m.%%Y', '%s', 'localtime') AS current_date, " +
                        "'false' AS start_timer;",
                currentDate
        );
    }

    public static String fetchAll(String currentDate) {
        return String.format("SELECT " +
                        "m.id_main, " +
                        "(strftime('%%s', 'now', 'localtime') - strftime('%%s', m.created_at)) AS time_passed, " +
                        "(SELECT COUNT(*) FROM %2$s h WHERE date(h.created_at) = date('%3$s')) AS daily, " +
                        "(SELECT COUNT(*) FROM %2$s h WHERE strftime('%%Y-%%W', h.created_at) = strftime('%%Y-%%W', '%3$s')) AS weekly, " +
                        "(SELECT COUNT(*) FROM %2$s h WHERE strftime('%%Y-%%m', h.created_at) = strftime('%%Y-%%m', '%3$s')) AS monthly " +
                        "FROM %1$s m;",
                Database.MAIN_TABLE,
                Database.HISTORY_TABLE,
                currentDate
        );
    }

    public static String fetchHistory(String currentDate) {
        return String.format(
                "SELECT h.id_%1$s, " +
                        "h.is_lent, " +
                        "strftime('%%H:%%M:%%S', h.created_at) AS created_at " +
                        "FROM %1$s h " +
                        "WHERE strftime('%%Y-%%m-%%d', h.created_at) = '%2$s' " +
                        "ORDER BY h.created_at DESC;",
                Database.HISTORY_TABLE,
                currentDate
        );
    }

    public static String updateTimer = String.format(
            "INSERT INTO %1$s (id_%1$s, created_at) VALUES (1, DATETIME('now', 'localtime')) " +
                    "ON CONFLICT(id_%1$s) DO UPDATE SET created_at = EXCLUDED.created_at;",
            Database.MAIN_TABLE
    );

    public static String insertRecord(String createDate) {
        return String.format(
                "INSERT INTO %1$s (created_at, is_lent) VALUES (DATETIME('%2$s'), 0);",
                Database.HISTORY_TABLE,
                createDate
        );
    }

    public static String deleteRecord(Long id) {
        return String.format(
                "DELETE FROM %1$s " +
                        "WHERE id_%1$s = '%2$s';",
                Database.HISTORY_TABLE,
                id
        );
    }

    public static String updateRecord(Long id, int isLent, String customDate) {
        return String.format(
                "UPDATE %1$s " +
                        "SET created_at = '%4$s', " +
                        "is_lent = '%3$s' " +
                        "WHERE id_%1$s = %2$s;",
                Database.HISTORY_TABLE,
                id,
                isLent,
                customDate
        );
    }

    public static String fetchById(Long id) {
        return String.format(
                "SELECT h.created_at, h.is_lent " +
                        "FROM %1$s h " +
                        "WHERE h.id_%1$s = '%2$s';",
                Database.HISTORY_TABLE,
                id
        );
    }

    public static String fetchByWeekAndMonth(String minDate, String maxDate) {
        return String.format(
                "SELECT " +
                        "DATE(h.created_at) AS date, " +
                        "COUNT(*) AS value " +
                        "FROM %1$s h " +
                        "WHERE " +
                        "DATE(h.created_at) >= '%2$s' AND DATE(h.created_at) <= '%3$s' " +
                        "GROUP BY " +
                        "DATE(h.created_at);",
                Database.HISTORY_TABLE,
                minDate,
                maxDate
        );
    }

    public static String fetchByYear(String minDate, String maxDate) {
        return String.format(
                "SELECT " +
                        "STRFTIME('%%Y-%%m', h.created_at) AS date, " +
                        "COUNT(*) AS value " +
                        "FROM %1$s h " +
                        "WHERE " +
                        "h.created_at BETWEEN '%2$s' AND '%3$s' " +
                        "GROUP BY " +
                        "STRFTIME('%%Y-%%m', h.created_at);",
                Database.HISTORY_TABLE,
                minDate,
                maxDate
        );
    }

    public static String fetchSettings = String.format(
            "SELECT s.language, g.color, g.point_color, g.line_color, g.paint_style " +
                    "FROM %1$s s " +
                    "JOIN %2$s g ON g.id_%2$s = s.id_%1$s",
            Database.SETTINGS_TABLE,
            Database.GRAPH_TABLE
    );

    public static String updateSettings(String language) {
        return String.format(
                "INSERT INTO %1$s (id_%1$s, language) " +
                        "VALUES (1, '%2$s') " +
                        "ON CONFLICT(id_%1$s) DO UPDATE SET " +
                        "language = '%2$s';",
                Database.SETTINGS_TABLE,
                language
        );
    }

    public static String insertGraph() {
        return String.format(
                "INSERT INTO %1$s (id_%1$s, color, point_color, line_color, paint_style) " +
                        "VALUES (1, 0xFF888888, 0xFF888888, 0xFF888888, 'FILL') ",
                Database.GRAPH_TABLE
        );
    }

    public static String updateGraph(int color, String pointColor, String lineColor) {
        return String.format(
                "INSERT INTO %1$s (id_%1$s, color, point_color, line_color) " +
                        "VALUES (1, 0xFF888888, 0xFF888888, 0xFF888888) " +
                        "ON CONFLICT(id_%1$s) DO UPDATE SET " +
                        "color = %2$s, point_color = '%3$s', line_color = '%4$s';",
                Database.GRAPH_TABLE,
                color,
                pointColor,
                lineColor
        );
    }

    public static String updatePaintStyle(Paint.Style paintStyle) {
        return String.format(
                "UPDATE %1$s SET " +
                        "paint_style = '%2$s'",
                Database.GRAPH_TABLE,
                paintStyle
        );
    }

    public static String getMainData() {
        return String.format(
                "SELECT * FROM %s",
                Database.MAIN_TABLE
        );
    }

    public static String getSettingsData() {
        return String.format(
                "SELECT * FROM %s",
                Database.SETTINGS_TABLE
        );
    }

    public static String getGraphData() {
        return String.format(
                "SELECT * FROM %s",
                Database.GRAPH_TABLE
        );
    }

    public static String getHistoryData() {
        return String.format(
                "SELECT * FROM %s",
                Database.HISTORY_TABLE
        );
    }

    public static String newMainRecord(StringBuilder stringBuilder) {
        return String.format(
                "INSERT INTO %1$s (id_main, created_at) VALUES (%2$s)",
                Database.MAIN_TABLE,
                stringBuilder
        );
    }

    public static String newSettingsRecord(StringBuilder stringBuilder) {
        return String.format(
                "INSERT INTO %1$s (id_settings, language) VALUES (%2$s)",
                Database.SETTINGS_TABLE,
                stringBuilder
        );
    }

    public static String newGraphRecord(StringBuilder stringBuilder) {
        return String.format(
                "INSERT INTO %1$s (id_graph, color, point_color, line_color, paint_style) VALUES (%2$s)",
                Database.GRAPH_TABLE,
                stringBuilder
        );
    }

    public static String newHistoryRecord(StringBuilder stringBuilder) {
        return String.format(
                "INSERT INTO %1$s (id_history, is_lent, created_at) VALUES (%2$s)",
                Database.HISTORY_TABLE,
                stringBuilder
        );
    }

    public static String truncateMain() {
        return String.format(
                "DELETE FROM %s",
                Database.MAIN_TABLE
        );
    }

    public static String truncateSettings() {
        return String.format(
                "DELETE FROM %s",
                Database.SETTINGS_TABLE
        );
    }

    public static String truncateGraph() {
        return String.format(
                "DELETE FROM %s",
                Database.GRAPH_TABLE
        );
    }

    public static String truncateHistory() {
        return String.format(
                "DELETE FROM %s",
                Database.HISTORY_TABLE
        );
    }

    public static String resetMainSequence() {
        return String.format(
                "DELETE FROM sqlite_sequence WHERE name='%s';",
                Database.MAIN_TABLE
        );
    }

    public static String resetSettingsSequence() {
        return String.format(
                "DELETE FROM sqlite_sequence WHERE name='%s';",
                Database.SETTINGS_TABLE
        );
    }

    public static String resetGraphSequence() {
        return String.format(
                "DELETE FROM sqlite_sequence WHERE name='%s';",
                Database.GRAPH_TABLE
        );
    }

    public static String resetHistorySequence() {
        return String.format(
                "DELETE FROM sqlite_sequence WHERE name='%s';",
                Database.HISTORY_TABLE
        );
    }
}