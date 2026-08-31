package com.rohit.jobtracker.android.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rohit.jobtracker.shared.api.JobTrackerApi
import com.rohit.jobtracker.shared.model.Status
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FollowUpReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val api: JobTrackerApi by inject()

    override suspend fun doWork(): Result {
        return try {
            val applications = api.getApplications()
            val now = Clock.System.now()

            val activeStatuses = setOf(Status.APPLIED, Status.SCREENING, Status.INTERVIEW)

            applications
                .filter { it.status in activeStatuses }
                .filter { it.reminderDays != null && it.reminderDays!! > 0 }
                .forEach { app ->
                    val daysSinceUpdate = (now - app.lastUpdated).inWholeDays
                    if (daysSinceUpdate >= app.reminderDays!!) {
                        ReminderNotificationHelper.showFollowUpNotification(
                            context = appContext,
                            application = app,
                            daysSinceUpdate = daysSinceUpdate
                        )
                    }
                }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
