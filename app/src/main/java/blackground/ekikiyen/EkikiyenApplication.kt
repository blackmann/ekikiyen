package blackground.ekikiyen

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import blackground.ekikiyen.home.HomeActivity
import com.google.firebase.messaging.RemoteMessage
import com.pusher.android.PusherAndroid
import java.util.*
import kotlin.collections.HashSet

class EkikiyenApplication : Application() {

    private val notificationId = 1021
    private val notificationChannelId = "newCodeChannel"

    override fun onCreate() {
        super.onCreate()

        val sharedPreference = getSharedPreferences("push_notif", Context.MODE_PRIVATE)
        val shouldShowPush = sharedPreference.getBoolean("show", true)

        if (shouldShowPush) {
            val pusherAndroid = PusherAndroid("4d02fef51f5006126d3c")
            val pusher = pusherAndroid.nativePusher()

            pusher.registerFCM(this)
            pusher.setFCMListener { showNotification(it) }

            pusher.subscribe("new_codes")
        }
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val cardsLoadedToday = getSharedPreferences(Date().getDateString(), Context.MODE_PRIVATE)
                .getStringSet("today", HashSet<String>()) as HashSet<String>

        if (cardsLoadedToday.size < 2) {
            if (remoteMessage.notification == null) return
            createNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
        }
    }

    private fun createNotification(title: String?, body: String?) {
        // this is required for devices from N
        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(body)
        notificationBuilder.setSmallIcon(R.drawable.ekikiyen_icon)
        notificationBuilder.setVibrate(longArrayOf(1000, 1000))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, 12, intent, 0)

        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setAutoCancel(true)
        val notification = notificationBuilder.build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(notificationChannelId,
                    "Ekiki Yen",
                    NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    // util
    private fun Date.getDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.time = this

        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val date = calendar.get(Calendar.DAY_OF_MONTH)

        return "$date-$month-$year"
    }
}