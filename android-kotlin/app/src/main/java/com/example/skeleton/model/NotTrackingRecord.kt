package com.example.skeleton.model

data class NotTrackingRecord(
        val appName: String,
        val packageName: String,
        var isIgnored: Boolean = false
)
