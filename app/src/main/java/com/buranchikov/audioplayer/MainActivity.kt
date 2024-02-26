package com.buranchikov.audioplayer

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import com.buranchikov.audioplayer.databinding.ActivityMainBinding
import com.buranchikov.audioplayer.utils.ACTION_NEXT
import com.buranchikov.audioplayer.utils.ACTION_PAUSE
import com.buranchikov.audioplayer.utils.ACTION_PLAY
import com.buranchikov.audioplayer.utils.ACTION_PREVIOUS
import com.buranchikov.audioplayer.utils.APP_ACTIVITY
class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var playerReceiver: PlayerReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        APP_ACTIVITY = this
        binding.btnPlay.setOnClickListener(this)
        binding.btnPause.setOnClickListener(this)
        binding.btnPrevTrack.setOnClickListener(this)
        binding.btnNextTrack.setOnClickListener(this)

        playerReceiver = PlayerReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_PLAY)
        intentFilter.addAction(ACTION_PAUSE)
        intentFilter.addAction(ACTION_NEXT)
        intentFilter.addAction(ACTION_PREVIOUS)

        ContextCompat.registerReceiver(this, playerReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)
    }
    override fun onClick(v: View?) {
        when (v) {
            binding.btnPlay -> {
                sendBroadcast(Intent(ACTION_PLAY))
            }

            binding.btnPause -> {
                sendBroadcast(Intent(ACTION_PAUSE))
            }

            binding.btnNextTrack -> {
                sendBroadcast(Intent(ACTION_NEXT))
            }

            binding.btnPrevTrack -> {
                sendBroadcast(Intent(ACTION_PREVIOUS))
            }
        }
    }
    companion object {
        fun startServiceWithAction(action: String) {


            val serviceIntent = Intent(APP_ACTIVITY, AudioService::class.java)
            serviceIntent.action = action
            Log.d("df", "startServiceWithAction: ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                APP_ACTIVITY.startForegroundService(serviceIntent)
            } else {
                APP_ACTIVITY.startService(serviceIntent)
            }
        }

    }
}