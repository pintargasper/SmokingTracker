package com.gasperpintar.smokingtracker.utils.manager

import com.gasperpintar.smokingtracker.utils.manager.Extensions.create
import com.gasperpintar.smokingtracker.utils.manager.Extensions.getHeaderColumnMap
import com.gasperpintar.smokingtracker.utils.manager.Extensions.getRowCount
import com.gasperpintar.smokingtracker.utils.manager.Extensions.import
import com.gasperpintar.smokingtracker.utils.manager.Extensions.importSingleRow
import com.gasperpintar.smokingtracker.utils.manager.Extensions.parseDateTime
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ExtensionsTest {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private lateinit var workbook: XSSFWorkbook

    @Before
    fun setup() {
        workbook = XSSFWorkbook()
    }

    @After
    fun teardown() {
        workbook.close()
    }

    @Test
    fun createWritesHeadersAndTypedCells() {
        workbook.create(name = "Data", headers = listOf("Flag", "Number", "Text", "Empty"), data = listOf(1)) {
            listOf(true, 5, "text", null)
        }

        val sheet = workbook.getSheet("Data")
        assertEquals(listOf("Flag", "Number", "Text", "Empty"), sheet.getRow(0).map { it.stringCellValue })

        val row = sheet.getRow(1)
        assertEquals(CellType.BOOLEAN, row.getCell(0).cellType)
        assertTrue(row.getCell(0).booleanCellValue)
        assertEquals(5.0, row.getCell(1).numericCellValue, 0.0)
        assertEquals("text", row.getCell(2).stringCellValue)
        assertEquals("", row.getCell(3).stringCellValue)
    }

    @Test
    fun createReportsProgressUntilCompletion() {
        val progress = mutableListOf<Int>()

        workbook.create(name = "Data", headers = listOf("Value"), data = listOf(1, 2, 3, 4), onStepProgress = { progress += it }) {
            listOf(it)
        }

        assertEquals(listOf(0, 25, 50, 75, 100, 100), progress)
    }

    @Test
    fun createWithEmptyDataWritesOnlyHeaders() {
        val progress = mutableListOf<Int>()

        workbook.create(name = "Data", headers = listOf("Value"), data = emptyList<Int>(), onStepProgress = { progress += it }) {
            listOf(it)
        }

        assertEquals(1, workbook.getSheet("Data").physicalNumberOfRows)
        assertEquals(listOf(0, 100), progress)
    }

    @Test
    fun importReturnsEmptyListWhenSheetIsMissing() {
        assertTrue(importValues(sheetName = "Missing").isEmpty())
    }

    @Test
    fun importReturnsEmptyListWhenRequiredHeaderIsMissing() {
        sheet(name = "Data", headers = listOf("Other"), rows = listOf(listOf(1)))

        assertTrue(importValues(sheetName = "Data").isEmpty())
    }

    @Test
    fun importReturnsEmptyListWhenSheetHasOnlyHeaders() {
        sheet(name = "Data", headers = listOf("Value"), rows = emptyList())

        assertTrue(importValues(sheetName = "Data").isEmpty())
    }

    @Test
    fun importSkipsRowsWhenParserReturnsNull() {
        sheet(name = "Data", headers = listOf("Value"), rows = listOf(listOf(1), listOf(-1), listOf(3)))

        val actual = workbook.import(sheetName = "Data", requiredHeaders = listOf("Value")) { row, col, _ ->
            row.getCell(col.getValue("Value")).numericCellValue.toInt().takeIf { it > 0 }
        }

        assertEquals(listOf(1, 3), actual)
    }

    @Test
    fun importSkipsRowsWhenParserThrows() {
        sheet(name = "Data", headers = listOf("Value"), rows = listOf(listOf(1), listOf("abc"), listOf(3)))

        assertEquals(listOf(1, 3), importValues(sheetName = "Data"))
    }

    @Test
    fun getHeaderColumnMapTrimsAndSkipsInvalidHeaders() {
        workbook.createSheet("Data").createRow(0).apply {
            createCell(0).setCellValue(" Name ")
            createCell(1).setCellValue("")
            createCell(2).setCellValue(5.0)
            createCell(3).setCellValue("Other")
        }

        assertEquals(mapOf("Name" to 0, "Other" to 3), workbook.getSheet("Data").getHeaderColumnMap())
    }

    @Test
    fun parseDateTimeParsesFormattedText() {
        val row = row(value = "2026-01-01 12:30:15")

        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 30, 15), row.parseDateTime(column = 0, formatter = formatter))
    }

    @Test
    fun parseDateTimeReturnsNullForInvalidText() {
        assertNull(row(value = "").parseDateTime(column = 0, formatter = formatter))
        assertNull(row(value = "not a date").parseDateTime(column = 0, formatter = formatter))
        assertNull(row(value = "").parseDateTime(column = 5, formatter = formatter))
    }

    @Test
    fun parseDateTimeReturnsNullForNonDateCells() {
        assertNull(row(value = true).parseDateTime(column = 0, formatter = formatter))
        assertNull(row(value = 46023.5).parseDateTime(column = 0, formatter = formatter))
    }

    @Test
    fun parseDateTimeReadsDateFormattedCells() {
        val row = workbook.createSheet("Data").createRow(0)
        row.createCell(0).apply {
            setCellValue(LocalDateTime.of(2026, 1, 1, 12, 30, 15))
            cellStyle = workbook.createCellStyle().apply {
                dataFormat = workbook.creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss")
            }
        }

        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 30, 15), row.parseDateTime(column = 0, formatter = formatter))
    }

    @Test
    fun getRowCountReturnsNumberOfDataRows() {
        sheet(name = "Headers", headers = listOf("Value"), rows = emptyList())
        sheet(name = "Data", headers = listOf("Value"), rows = listOf(listOf(1), listOf(2), listOf(3)))

        assertEquals(1, workbook.getRowCount(sheetName = "Missing"))
        assertEquals(1, workbook.getRowCount(sheetName = "Headers"))
        assertEquals(3, workbook.getRowCount(sheetName = "Data"))
    }

    @Test
    fun importSingleRowSkipsBlockWithoutDataRow() {
        sheet(name = "Settings", headers = listOf("Theme"), rows = emptyList())
        val progress = mutableListOf<Int>()
        var called = false

        workbook.importSingleRow(sheetName = "Settings", onStepProgress = { progress += it }) { _, _ -> called = true }

        assertFalse(called)
        assertEquals(listOf(100), progress)
    }

    private fun importValues(sheetName: String): List<Int> {
        return workbook.import(sheetName = sheetName, requiredHeaders = listOf("Value")) { row, col, _ ->
            row.getCell(col.getValue("Value")).numericCellValue.toInt()
        }
    }

    private fun sheet(
        name: String,
        headers: List<String>,
        rows: List<List<Any>>
    ) {
        workbook.createSheet(name).apply {
            createRow(0).apply { headers.forEachIndexed { index, header -> createCell(index).setCellValue(header) } }
            rows.forEachIndexed { rowIndex, values ->
                createRow(rowIndex + 1).apply {
                    values.forEachIndexed { index, value ->
                        when (value) {
                            is Number -> createCell(index).setCellValue(value.toDouble())
                            else -> createCell(index).setCellValue(value.toString())
                        }
                    }
                }
            }
        }
    }

    private fun row(value: Any): Row {
        return workbook.createSheet().createRow(0).apply {
            when (value) {
                is Boolean -> createCell(0).setCellValue(value)
                is Number -> createCell(0).setCellValue(value.toDouble())
                else -> createCell(0).setCellValue(value.toString())
            }
        }
    }
}
