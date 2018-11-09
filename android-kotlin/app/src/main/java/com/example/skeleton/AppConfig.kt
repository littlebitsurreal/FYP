package com.example.skeleton

@Suppress("unused")
object AppConfig {
    const val SPLASH_DURATION = 300L // splash screen duration in ms, 0 to disable
    const val TIMER_CHECK_PERIOD = 3000L
    const val TIMER_UPDATE_SERVER_PERIOD = 120000L // 2 minutes
    const val MAIN_SCREEN_UPDATEVIEW_TIMER = 2500L
    val SERVER_ADDRESS: String
    const val SERVER_PORT = 8080

    init {
        when (BuildConfig.FLAVOR) {
            "staging" -> {
                SERVER_ADDRESS = "http://192.168.40.1:8080/"
            }
            else -> {
                SERVER_ADDRESS = "http://192.168.40.1:8080/"
            }
        }
    }
}
