package com.robinsonindustries.samtracker.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.robinsonindustries.samtracker.R
import com.robinsonindustries.samtracker.other.Constants
import com.robinsonindustries.samtracker.other.Constants.ACTION_SHOW_MAIN_ACTIVITY
import com.robinsonindustries.samtracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.robinsonindustries.samtracker.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped //only one instance of FusedLocationProviderClient
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = ACTION_SHOW_MAIN_ACTIVITY
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )



    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(
        app, NOTIFICATION_CHANNEL_ID
    ).setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_run)
        .setContentTitle("Sam Tracker")
        .setContentText("Munch on Deez Nuts")
        .setContentIntent(pendingIntent)

}