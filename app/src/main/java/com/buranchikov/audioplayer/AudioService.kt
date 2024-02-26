package com.buranchikov.audioplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.buranchikov.audioplayer.utils.ACTION_NEXT
import com.buranchikov.audioplayer.utils.ACTION_PAUSE
import com.buranchikov.audioplayer.utils.ACTION_PLAY
import com.buranchikov.audioplayer.utils.ACTION_PREVIOUS
import com.buranchikov.audioplayer.utils.SERVICE_STOP
import com.buranchikov.audioplayer.utils.AUDIO_CHANNEL

class AudioService : Service() {
    private lateinit var player: MediaPlayer
    private val retriever = MediaMetadataRetriever()
    private val NOTIFICATION_ID = 101
    private lateinit var notificationManager: NotificationManager
    private lateinit var notification: Notification
    private var isPause = true
    private var metadata: String? = null
    private var uri: Uri? = null

    private val arrayMP3 = arrayOf(
        R.raw.chi_mai,
        R.raw.hotel_california,
        R.raw.house_of_the_rising_sun
    )
    private var currentFileIndex = 0


    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        player = MediaPlayer.create(this, arrayMP3[currentFileIndex])
        autoPlayNext()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        uri = Uri.parse(
            getString(R.string.android_resource)
                    + packageName
                    + "/"
                    + arrayMP3[currentFileIndex]
        )
        metadata = getMetaDatum(uri)
        clickPlayButtons(intent)
        clickNotificationButtons(intent)
        startForegroundService()

        return START_STICKY
    }

    private fun getMetaDatum(uri: Uri?): String {
        retriever.setDataSource(this, uri)
        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()
        val minutes: Int? = duration?.div(60000)
        val seconds: Int? = duration?.mod(60)
        return "$title  -  $artist  -  $minutes:$seconds"
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            startForeground(
                NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        }
    }

    private fun clickNotificationButtons(intent: Intent?) {
        when (intent?.action) {
            SERVICE_STOP -> {
                stopAudioService()
            }

            ACTION_PAUSE -> {
                pause()
            }

            ACTION_PLAY -> {
                play()
            }

            ACTION_NEXT -> {
                notification = createNotification(ACTION_PLAY)
                notificationManager.notify(NOTIFICATION_ID, notification)
                next()
            }

            ACTION_PREVIOUS -> {
                notification = createNotification(ACTION_PLAY)
                notificationManager.notify(NOTIFICATION_ID, notification)
                previous()
            }

        }
    }

    private fun clickPlayButtons(intent: Intent?) {
        when (intent?.action) {
            ACTION_PLAY -> {
                notification = createNotification(ACTION_PLAY)
                notificationManager.notify(NOTIFICATION_ID, notification)
                play()
            }

            ACTION_PAUSE -> {
                notification = createNotification(ACTION_PAUSE)
                notificationManager.notify(NOTIFICATION_ID, notification)
                pause()
            }
        }
    }

    private fun stopAudioService() {
        player.stop()
        notificationManager.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    private fun createNotification(action: String): Notification {
        var notification: Notification? = null

        val intentStop = Intent(this, AudioService::class.java)
        intentStop.action = SERVICE_STOP
        val pIntentStop =
            PendingIntent.getService(this, 0, intentStop, PendingIntent.FLAG_IMMUTABLE)

        val intentPlay = Intent(this, AudioService::class.java)
        intentPlay.action = ACTION_PAUSE
        val pIntentPlay =
            PendingIntent.getService(this, 0, intentPlay, PendingIntent.FLAG_IMMUTABLE)

        val intentPause = Intent(this, AudioService::class.java)
        intentPause.action = ACTION_PLAY
        val pIntentPause =
            PendingIntent.getService(this, 0, intentPause, PendingIntent.FLAG_IMMUTABLE)

        val intentNext = Intent(this, AudioService::class.java)
        intentNext.action = ACTION_NEXT
        val pIntentNext =
            PendingIntent.getService(this, 0, intentNext, PendingIntent.FLAG_IMMUTABLE)

        val intentPrev = Intent(this, AudioService::class.java)
        intentPrev.action = ACTION_PREVIOUS
        val pIntentPrev =
            PendingIntent.getService(this, 0, intentPrev, PendingIntent.FLAG_IMMUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AUDIO_CHANNEL,
                getString(R.string.foreground_audio_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        when (action) {
            ACTION_PLAY -> {
                notification = NotificationCompat.Builder(this, AUDIO_CHANNEL)
                    .setContentTitle(
                        getString(R.string.app_name) + getString(R.string.empty_string) + getString(
                            R.string.close_notif_symbol
                        )
                    )
                    .setContentText(metadata)
                    .setSmallIcon(R.drawable.icon_small_play)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(resources, R.mipmap.icon_player_round)
                    )
                    .addAction(
                        R.drawable.baseline_skip_previous_gray,
                        getString(R.string.prev), pIntentPrev
                    )
                    .addAction(
                        R.drawable.baseline_pause_gray,
                        getString(R.string.pause),
                        pIntentPlay
                    )
                    .addAction(
                        R.drawable.baseline_skip_next_gray,
                        getString(R.string.next),
                        pIntentNext
                    )
                    .setContentIntent(pIntentStop)
                    .setShowWhen(false)
                    .build()
            }

            ACTION_PAUSE -> {
                notification = NotificationCompat.Builder(this, AUDIO_CHANNEL)
                    .setContentTitle(
                        getString(R.string.app_name) + getString(R.string.empty_string) + getString(
                            R.string.close_notif_symbol
                        )
                    )
                    .setContentText(metadata)
                    .setSmallIcon(R.drawable.icon_small_pause)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(resources, R.mipmap.icon_player_round)
                    ).addAction(
                        R.drawable.baseline_skip_previous_gray,
                        getString(R.string.prev),
                        pIntentPrev
                    )
                    .addAction(
                        R.drawable.baseline_play_arrow_gray,
                        getString(R.string.play),
                        pIntentPause
                    )
                    .addAction(
                        R.drawable.baseline_skip_next_gray,
                        getString(R.string.next),
                        pIntentNext
                    )
                    .setContentIntent(pIntentStop)
                    .setShowWhen(false)
                    .build()
            }
        }
        return notification!!
    }
    private fun previous() {
        if (currentFileIndex > 0) {
            currentFileIndex--
            player.reset()
            player.setDataSource(
                this,
                Uri.parse(getString(R.string.android_resource) + packageName + "/" + arrayMP3[currentFileIndex])
            )
            player.prepare()
            player.start()
            updateNotification(ACTION_PLAY)
        }
    }


    private fun next() {
        if (currentFileIndex < arrayMP3.size - 1) {
            currentFileIndex++
            player.reset()
            player.setDataSource(
                this,
                Uri.parse(getString(R.string.android_resource) + packageName + "/" + arrayMP3[currentFileIndex])
            )
            player.prepare()
            player.start()
            updateNotification(ACTION_PLAY)
        }
    }

    private fun pause() {
        if (player.isPlaying) {
            player.pause()
            isPause = true
        }
    }

    private fun play() {
        if (!player.isPlaying) {
            player.start()
        }
    }

    private fun autoPlayNext() {
        player.setOnCompletionListener {
            next()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
    private fun updateNotification(action: String) {
        val newNotification = createNotification(action)
        notificationManager.notify(NOTIFICATION_ID, newNotification)
    }
}