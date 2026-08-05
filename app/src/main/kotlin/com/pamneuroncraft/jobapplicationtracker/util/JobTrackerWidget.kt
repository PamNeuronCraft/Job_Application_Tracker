package com.pamneuroncraft.jobapplicationtracker.util

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.pamneuroncraft.jobapplicationtracker.data.local.JobDao
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class JobTrackerWidget : GlanceAppWidget(), KoinComponent {
    private val jobDao: JobDao by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val jobsCount = jobDao.getAllJobs().first().size

        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Job Tracker",
                    modifier = GlanceModifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Total Applications: $jobsCount"
                )
            }
        }
    }
}
