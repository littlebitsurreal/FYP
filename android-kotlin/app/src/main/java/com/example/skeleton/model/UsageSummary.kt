package com.example.skeleton.model

data class UsageSummary(
        val appName: String,
        val packageName: String,
        val useTimeTotal: Long,
        val useTimeAverage: Long
)
