package com.example.skeleton

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.skeleton.helper.Logger
import com.example.skeleton.helper.ScreenUnlockHelper

class ScreenUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context, p1: Intent) {
        if (p1.action == Intent.ACTION_USER_PRESENT) {
            Logger.d("ScreenUnlockReceiver", "onReceive")
            ScreenUnlockHelper.countScreenUnlock(p0)
        }
    }
}
