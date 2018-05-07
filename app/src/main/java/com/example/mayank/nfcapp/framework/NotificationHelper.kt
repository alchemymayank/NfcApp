package com.example.mayank.nfcapp.framework

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import com.example.mayank.nfcapp.R
import java.util.ArrayList

/**
 * Created by Mayank on 26/03/2018.
 */
object NotificationHelper {
    val ERROR = Notification.CATEGORY_ERROR
    val STATUS = Notification.CATEGORY_STATUS
    private var notifyId = 0

    @Synchronized
    fun notify(context: Context, subText: String, notificationCategory: String) {
        val builder = Notification.Builder(context)
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(subText)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(notificationCategory)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(Notification.PRIORITY_MAX)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(++notifyId, builder.build())
    }

    @Synchronized
    fun notifyGroupedError(context: Context, subText: String, summaryText: String, messages: ArrayList<String>) {
        val builder = Notification.Builder(context)
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(subText)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_ERROR)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(Notification.PRIORITY_MAX)

        val inboxStyle = Notification.InboxStyle()
        inboxStyle.setBigContentTitle(subText)
                .setSummaryText(summaryText)

        for (message in messages) {
            inboxStyle.addLine(message)
        }

        builder.setStyle(inboxStyle)
                .setGroupSummary(true)
                .setGroup("OLMS")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(++notifyId, builder.build())
    }
}