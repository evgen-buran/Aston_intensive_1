package com.buranchikov.audioplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.buranchikov.audioplayer.utils.ACTION_NEXT
import com.buranchikov.audioplayer.utils.ACTION_PAUSE
import com.buranchikov.audioplayer.utils.ACTION_PLAY
import com.buranchikov.audioplayer.utils.ACTION_PREVIOUS
import com.buranchikov.audioplayer.utils.SERVICE_STOP

class PlayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_PLAY ->MainActivity.startServiceWithAction(ACTION_PLAY)
            ACTION_PAUSE ->MainActivity.startServiceWithAction(ACTION_PAUSE)
            ACTION_NEXT ->MainActivity.startServiceWithAction(ACTION_NEXT)
            ACTION_PREVIOUS ->MainActivity.startServiceWithAction(ACTION_PREVIOUS)
        }
    }
}