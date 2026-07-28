package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.ui.theme.getJobStatusColor
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.SummaryViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import jobapplicationtracker.app.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SummaryScreen(
    onBack: () -> Unit,
    viewModel: SummaryViewModel = koinViewModel()
) {
    val statusCounts by viewModel.statusCounts.collectAsState()
    val totalJobs by viewModel.totalJobs.collectAsState()
    val isDark = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.summary)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(Res.string.status_breakdown),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                DonutChart(statusCounts, totalJobs)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalJobs.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.total),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                SummaryCard(
                    label = stringResource(Res.string.status_applied),
                    count = totalJobs,
                    color = getJobStatusColor(JobStatus.APPLIED, isDark),
                    modifier = Modifier.weight(1f).height(100.dp)
                )
                SummaryCard(
                    label = stringResource(Res.string.status_interview),
                    count = statusCounts[JobStatus.INTERVIEW] ?: 0,
                    color = getJobStatusColor(JobStatus.INTERVIEW, isDark),
                    modifier = Modifier.weight(1f).height(100.dp)
                )
                SummaryCard(
                    label = stringResource(Res.string.status_offered),
                    count = statusCounts[JobStatus.OFFER] ?: 0,
                    color = getJobStatusColor(JobStatus.OFFER, isDark),
                    modifier = Modifier.weight(1f).height(100.dp)
                )
                SummaryCard(
                    label = stringResource(Res.string.status_rejected),
                    count = statusCounts[JobStatus.NO_OFFER] ?: 0,
                    color = getJobStatusColor(JobStatus.NO_OFFER, isDark),
                    modifier = Modifier.weight(1f).height(100.dp)
                )
            }
        }
    }
}

@Composable
fun DonutChart(
    statusCounts: Map<JobStatus, Int>,
    total: Int
) {
    val isDark = isSystemInDarkTheme()
    if (total == 0) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color.LightGray.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        return
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        var startAngle = -90f
        
        JobStatus.entries.forEach { status ->
            val count = statusCounts[status] ?: 0
            if (count > 0) {
                val sweepAngle = (count.toFloat() / total) * 360f
                drawArc(
                    color = getJobStatusColor(status, isDark),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


