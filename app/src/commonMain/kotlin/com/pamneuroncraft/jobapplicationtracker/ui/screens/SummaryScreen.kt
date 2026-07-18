package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.Canvas
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
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.SummaryViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SummaryScreen(
    onBack: () -> Unit,
    viewModel: SummaryViewModel = koinViewModel()
) {
    val statusCounts by viewModel.statusCounts.collectAsState()
    val totalJobs by viewModel.totalJobs.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Job Summary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                text = "Status Breakdown",
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
                        text = "Total",
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
                    label = "Applied",
                    count = statusCounts[JobStatus.APPLIED] ?: 0,
                    color = getChartStatusColor(JobStatus.APPLIED),
                    modifier = Modifier.weight(1f).height(100.dp)
                )
                SummaryCard(
                    label = "Interview",
                    count = statusCounts[JobStatus.INTERVIEW] ?: 0,
                    color = getChartStatusColor(JobStatus.INTERVIEW),
                    modifier = Modifier.weight(1f).height(100.dp)
                )
                SummaryCard(
                    label = "Offered",
                    count = statusCounts[JobStatus.OFFER] ?: 0,
                    color = getChartStatusColor(JobStatus.OFFER),
                    modifier = Modifier.weight(1f).height(100.dp)
                )
                SummaryCard(
                    label = "Rejected",
                    count = statusCounts[JobStatus.NO_OFFER] ?: 0,
                    color = getChartStatusColor(JobStatus.NO_OFFER),
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
                    color = getChartStatusColor(status),
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

private fun getChartStatusColor(status: JobStatus): Color {
    return when (status) {
        JobStatus.APPLIED -> Color(0xFF2196F3) // Blue
        JobStatus.INTERVIEW -> Color(0xFFFFC107) // Yellow/Amber
        JobStatus.OFFER -> Color(0xFF4CAF50) // Green
        JobStatus.NO_OFFER -> Color(0xFFF44336) // Red
    }
}
