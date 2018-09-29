package com.example.skeleton.helper

import android.util.Log
import com.csvreader.CsvReader
import com.csvreader.CsvWriter
import java.io.File
import java.io.FileWriter

@Suppress("MemberVisibilityCanBePrivate")
object CsvHelper {
    val usageHeader = arrayOf("appName", "startTime", "duration")
    data class UsageRecord(
            val appName: String = "",
            val starTime: Long = 0,
            val duration: Long = 0
    ) {
        fun toArray(): Array<String> {
            return arrayOf(appName, starTime.toString(), duration.toString())
        }
    }

    fun write(file: File, records: List<UsageRecord>): Boolean {
        val alreadyExist = File(file.path).exists()
        return try {
            val writer = CsvWriter(FileWriter(file, true), ',')
            if (!alreadyExist) {
                writer.writeRecord(usageHeader)
            }
            for (record in records) {
                writer.writeRecord(record.toArray())
            }
            Log.d("CsvHelper", "write to file ${file.path}  exist = $alreadyExist")
            writer.close()
            true
        } catch (e: Exception) {
            Log.e("CsvHelper", e.message + ": " + e.localizedMessage)
            false
        }
    }

    fun read(file: File): Boolean {
        return try {
            if (!file.canRead()) {
                return false
            }
            val reader = CsvReader(file.path)
            reader.readHeaders()
            while (reader.readRecord()) {
                Log.d("CsvHelper", reader.rawRecord)
            }
            reader.close()
            true
        } catch (e: Exception) {
            Log.e("CsvHelper", e.message + ": " + e.localizedMessage)
            false
        }
    }
}
