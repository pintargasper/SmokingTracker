package com.gasperpintar.smokingtracker.utils.manager

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Extensions {

    fun <T> XSSFWorkbook.create(
        name: String,
        headers: List<String>,
        data: List<T>?,
        onStepProgress: (Int) -> Unit = {},
        mapper: (T) -> List<Any?>
    ) {
        onStepProgress(0)
        val sheet = createSheet(name)
        sheet.createRow(0).apply { headers.forEachIndexed { index, h -> createCell(index).setCellValue(h) } }

        val list = data.orEmpty()
        if (list.isEmpty()) {
            onStepProgress(100)
            return
        }

        list.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            mapper(item).forEachIndexed { colIndex, value ->
                val cell = row.createCell(colIndex)
                when (value) {
                    is Boolean -> cell.setCellValue(value)
                    is Number -> cell.setCellValue(value.toDouble())
                    else -> cell.setCellValue(value?.toString().orEmpty())
                }
            }
            onStepProgress(((index + 1) * 100) / list.size)
        }
        onStepProgress(100)
    }

    fun <T> XSSFWorkbook.import(
        sheetName: String,
        requiredHeaders: List<String>,
        onStepProgress: (Int) -> Unit = {},
        rowParser: (Row, Map<String, Int>, Int) -> T?
    ): List<T> {
        onStepProgress(0)
        val sheet = getSheet(sheetName) ?: run { onStepProgress(100); return emptyList() }
        val columnMap = sheet.getHeaderColumnMap() ?: run { onStepProgress(100); return emptyList() }

        if (!requiredHeaders.all(predicate = columnMap::containsKey)) { onStepProgress(100); return emptyList() }

        val rows = sheet.drop(n = 1)
        if (rows.isEmpty()) { onStepProgress(100); return emptyList() }

        return buildList(capacity = rows.size) {
            rows.forEachIndexed { index, row ->
                rowParser(row, columnMap, index)?.let(block = ::add)
                onStepProgress(((index + 1) * 100) / rows.size)
            }
        }.also { onStepProgress(100) }
    }

    inline fun XSSFWorkbook.importSingleRow(
        sheetName: String,
        onStepProgress: (Int) -> Unit,
        block: (Row, Map<String, Int>) -> Unit
    ) {
        val sheet = getSheet(sheetName) ?: run { onStepProgress(100); return }
        val col = sheet.getHeaderColumnMap() ?: run { onStepProgress(100); return }
        val row = sheet.getRow(1) ?: run { onStepProgress(100); return }
        block(row, col)
        onStepProgress(100)
    }

    fun Sheet.getHeaderColumnMap(): Map<String, Int>? {
        return getRow(0)?.mapNotNull { cell ->
            runCatching { cell.stringCellValue.trim() }.getOrNull()
                ?.takeIf { it.isNotEmpty() }
                ?.let { it to cell.columnIndex }
        }?.toMap()
    }

    fun Row.parseDateTime(
        column: Int,
        formatter: DateTimeFormatter
    ): LocalDateTime? {
        return getCell(column)?.stringCellValue
            ?.takeIf { it.isNotEmpty() }
            ?.let { runCatching { LocalDateTime.parse(it, formatter) }.getOrNull() }
    }

    fun XSSFWorkbook.getRowCount(sheetName: String): Int {
        return (getSheet(sheetName)?.physicalNumberOfRows?.minus(other = 1) ?: 1).coerceAtLeast(minimumValue = 1)
    }
}