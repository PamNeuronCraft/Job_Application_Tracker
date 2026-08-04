package com.pamneuroncraft.jobapplicationtracker.util

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.data.local.JobDao
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlinx.coroutines.flow.first

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
