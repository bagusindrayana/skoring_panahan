package com.potadev.skoring_panahan.excel

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import com.potadev.skoring_panahan.adapters.RankingAdapter
import com.potadev.skoring_panahan.data.AppDatabase
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.Score
import com.potadev.skoring_panahan.data.entity.ScoreWithParticipant
import com.potadev.skoring_panahan.data.repository.RoundRepository
import com.potadev.skoring_panahan.data.repository.ScoreRepository
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ExcelManager(private val context: Context) {

    companion object {
        private const val TAG = "ExcelManager"
    }

    /**
     * Creates a sample Excel file with data
     * @param fileName Name of the Excel file to create
     * @return Path to the created file, or null if creation failed
     */
    fun createExcelFile(fileName: String): String? {
        val workbook: Workbook = XSSFWorkbook()

        try {
            // Create a sheet
            val sheet: Sheet = workbook.createSheet("Sample Sheet")

            // Create header row
            val headerRow: Row = sheet.createRow(0)
            val headers = arrayOf("ID", "Name", "Email", "Phone", "Salary")

            // Add headers
            for (i in headers.indices) {
                val cell: Cell = headerRow.createCell(i)
                cell.setCellValue(headers[i])
            }

            // Add sample data rows
            val sampleData = arrayOf(
                arrayOf("1", "John Doe", "john@example.com", "123-456-7890", "50000"),
                arrayOf("2", "Jane Smith", "jane@example.com", "987-654-3210", "55000"),
                arrayOf("3", "Mike Johnson", "mike@example.com", "555-123-4567", "48000"),
                arrayOf("4", "Sarah Williams", "sarah@example.com", "555-987-6543", "62000"),
                arrayOf("5", "Robert Brown", "robert@example.com", "555-444-3333", "58000")
            )

            // Create data rows
            for (i in sampleData.indices) {
                val row: Row = sheet.createRow(i + 1)
                for (j in sampleData[i].indices) {
                    val cell: Cell = row.createCell(j)
                    cell.setCellValue(sampleData[i][j])
                }
            }

            // Auto size columns
//            for (i in headers.indices) {
//                sheet.autoSizeColumn(i)
//            }

            // Write the workbook to a file
            val file = File(context.getExternalFilesDir(null), "$fileName.xlsx")
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }

            Log.d(TAG, "Excel file created successfully at: ${file.absolutePath}")
            return file.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error creating Excel file", e)
            return null
        } finally {
            try {
                workbook.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing workbook", e)
            }
        }
    }

    /**
     * Reads data from an existing Excel file
     * @param filePath Path to the Excel file
     * @return List of rows as lists of cell values
     */
    fun readExcelFile(filePath: String): List<List<String>> {
        val data = mutableListOf<List<String>>()

        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: $filePath")
                return data
            }

            val workbook = XSSFWorkbook(file)
            val sheet = workbook.getSheetAt(0)

            for (row in sheet) {
                val rowData = mutableListOf<String>()
                for (cell in row) {
                    rowData.add(cell.toString())
                }
                data.add(rowData)
            }

            workbook.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading Excel file", e)
        }

        return data
    }


    fun exportSkor(fileName: String, round: Round, scores : List<ScoreWithParticipant>):String? {
        val workbook: Workbook = XSSFWorkbook()

        try {




            var rankingItems = scores.groupBy { it.score.participantId }
                ?.map { (id, sc) ->
                    val totalScore = sc.sumOf { it.score.score }
                    RankingAdapter.RankingItem(
                        id = id,
                        rank = 0, // Rank will be assigned after sorting
                        name = sc.first().participant.name, // Placeholder name, should be replaced with actual participant name
                        score = totalScore
                    )
                }
                ?.sortedByDescending { it.score }
                ?.mapIndexed { index, item ->
                    item.copy(rank = index + 1)
                }


            //sheet 1
            val leaderBoardheet: Sheet = workbook.createSheet("Leaderboard")

            //merge cells for the title
            leaderBoardheet.createRow(0).createCell(0).setCellValue("Leaderboard - ${round.name}")
            leaderBoardheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4))

            var rowIndex = 2
            rankingItems?.forEach { item ->
                val row: Row = leaderBoardheet.createRow(rowIndex++)
                val participant = item.name

                row.createCell(0).setCellValue(participant)


                var headerRow: Row = leaderBoardheet.createRow(rowIndex++)
                headerRow.createCell(0).setCellValue("Sesi")
                for (i in 1..round.shootsPerEnd) {
                    val cell: Cell = headerRow.createCell(i)
                    cell.setCellValue("Shoot $i")
                }
                headerRow.createCell(round.shootsPerEnd).setCellValue("Total")
                headerRow.createCell(round.shootsPerEnd+1).setCellValue("End")

                val scoresByEnd = scores.groupBy { it.score.endNumber }

                var grandTotal = 0
                for (endNumber in 1..round.numberOfEnds) {
                    val endScores = scoresByEnd[endNumber] ?: emptyList()
                    var endTotal = 0
                    var contentRow: Row = leaderBoardheet.createRow(rowIndex++)
                    contentRow.createCell(0).setCellValue("$endNumber")
                    for (shootNumber in 1..round.shootsPerEnd) {
                        val score = endScores.find { it.score.shootNumber == shootNumber && it.score.participantId == item.id }?.score?.score ?: 0
                        endTotal += score
                        val cell: Cell = contentRow.createCell(shootNumber)
                        cell.setCellValue("$score")
                    }
                    grandTotal += endTotal
                    contentRow.createCell(round.shootsPerEnd).setCellValue("$endTotal")
                    contentRow.createCell(round.shootsPerEnd+1).setCellValue("$grandTotal")

                }
                
            }


            //sheet 2
            val skorSheet: Sheet = workbook.createSheet("Score")
            skorSheet.createRow(0).createCell(0).setCellValue("Score - ${round.name}")
            skorSheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4))

            val row: Row = skorSheet.createRow(1)
            row.createCell(0).setCellValue("ID")
            row.createCell(1).setCellValue("Nama")
            for (i in 1..round.shootsPerEnd) {
                val cell: Cell = row.createCell(i+1)
                cell.setCellValue("Rambahan $i")
            }
            row.createCell(round.shootsPerEnd+1).setCellValue("Total")
            var rowIndex2 = 2
            rankingItems?.forEach { item ->

                val scoresByEnd = scores.groupBy { it.score.endNumber }
                val contentRow: Row = skorSheet.createRow(rowIndex2++)
                contentRow.createCell(0).setCellValue("${item.id}")
                contentRow.createCell(1).setCellValue(item.name)

                var grandTotal = 0
                for (endNumber in 1..round.numberOfEnds) {
                    var total = 0
                    for (shootNumber in 1..round.shootsPerEnd) {
                        val endScores = scoresByEnd[endNumber] ?: emptyList()
                        val score = endScores.find { it.score.shootNumber == shootNumber && it.score.participantId == item.id }?.score?.score ?: 0
                        total += score
                    }

                    grandTotal += total


                    val cell: Cell = contentRow.createCell(endNumber+1)
                    cell.setCellValue("$total")
                }

                contentRow.createCell(round.shootsPerEnd+1).setCellValue("$grandTotal")





            }


            // Write the workbook to a file
            val file = File(context.getExternalFilesDir(null), "$fileName.xlsx")
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }

            Log.d(TAG, "Excel file created successfully at: ${file.absolutePath}")
            return file.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error creating Excel file", e)
            return null
        } finally {
            try {
                workbook.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing workbook", e)
            }
        }
    }
}