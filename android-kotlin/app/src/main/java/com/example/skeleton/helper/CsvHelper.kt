package com.example.skeleton.helper

import android.util.Log
import com.csvreader.CsvReader
import com.csvreader.CsvWriter
import java.io.File
import java.io.FileWriter

@Suppress("MemberVisibilityCanBePrivate")
object CsvHelper {
    val usageHeader = arrayOf("packageName", "startTime", "duration")

    data class UsageRecord(
            val packageName: String = "",
            val starTime: Long = 0,
            val duration: Long = 0
    ) {
        fun toArray(): Array<String> {
            return arrayOf(packageName, starTime.toString(), duration.toString())
        }

        override fun toString(): String {
            return "packageName: $packageName  startTime: ${CalanderHelper.getDate(starTime)}  duration: ${duration / 1000}s"
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
            writer.close()
            true
        } catch (e: Exception) {
            Log.e("CsvHelper", e.message + ": " + e.localizedMessage)
            false
        }
    }

    fun read(file: File): Array<UsageRecord> {
        try {
            if (!file.canRead()) {
                Log.d("CsvHelper", "can't read file ${file.path}")
                return arrayOf()
            }
            val records = arrayListOf<UsageRecord>()
            val reader = CsvReader(file.path)
            reader.readHeaders()
            while (reader.readRecord()) {
                val record = UsageRecord(reader.get(0), reader.get(1).toLong(), reader.get(2).toLong())
                records.add(record)
                Log.d("CsvHelper", "read $record")
            }
            reader.close()
            return records.toTypedArray()
        } catch (e: Exception) {
            Log.e("CsvHelper", e.message + ": " + e.localizedMessage)
            return arrayOf()
        }
    }
}
