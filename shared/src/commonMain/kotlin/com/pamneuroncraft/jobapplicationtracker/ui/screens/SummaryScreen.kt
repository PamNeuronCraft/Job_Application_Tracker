package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobAnalytics
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.ui.components.EmptyState
import com.pamneuroncraft.jobapplicationtracker.ui.theme.getJobStatusColor
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.SummaryViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import com.pamneuroncraft.jobapplicationtracker.shared.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onBack: (() -> Unit)? = null,
    viewModel: SummaryViewModel = koinViewModel()
) {
    val analytics by viewModel.analytics.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 4 }
    
    val tabs = listOf(
        stringResource(Res.string.tab_overview),
        stringResource(Res.string.tab_financials),
        stringResource(Res.string.tab_timeline),
        stringResource(Res.string.tab_distribution)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.summary)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp,
                    divider = {} // Divider handled by Surface/Column
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { 
                                scope.launch { 
                                    pagerState.animateScrollToPage(index) 
                                } 
                            },
                            text = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        )
                    }
                }
            }

            if (analytics == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    userScrollEnabled = true
                ) { page ->
                    if (analytics!!.totalApps == 0) {
                        EmptyState(
                            imageVector = Icons.Default.QueryStats,
                            title = stringResource(Res.string.empty_summary_title),
                            description = stringResource(Res.string.empty_summary_desc)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            when (page) {
                                0 -> SummaryOverviewTab(analytics!!)
                                1 -> SummaryFinancialsTab(analytics!!)
                                2 -> SummaryTimelineTab(analytics!!)
                                3 -> SummaryDistributionTab(analytics!!)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryOverviewTab(analytics: JobAnalytics) {
    val isDark = isSystemInDarkTheme()
    
    Text(
        text = stringResource(Res.string.status_breakdown),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().height(240.dp)
    ) {
        DonutChart(analytics.statusCounts, analytics.totalApps)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = analytics.totalApps.toString(),
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            label = stringResource(Res.string.status_interview),
            count = analytics.statusCounts[JobStatus.INTERVIEW] ?: 0,
            color = getJobStatusColor(JobStatus.INTERVIEW, isDark),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = stringResource(Res.string.status_offered),
            count = analytics.statusCounts[JobStatus.OFFER] ?: 0,
            color = getJobStatusColor(JobStatus.OFFER, isDark),
            modifier = Modifier.weight(1f)
        )
    }

    HorizontalDivider()

    Text(
        text = stringResource(Res.string.conversion_funnel),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatRow(
            label = stringResource(Res.string.response_rate),
            value = "${analytics.responseRate.format(1)}%",
            icon = Icons.Default.ThumbUp,
            color = MaterialTheme.colorScheme.primary
        )
        StatRow(
            label = stringResource(Res.string.offer_rate),
            value = "${analytics.offerRate.format(1)}%",
            icon = Icons.Default.EmojiEvents,
            color = Color(0xFF4CAF50)
        )
    }
}

@Composable
fun SummaryFinancialsTab(analytics: JobAnalytics) {
    Text(
        text = stringResource(Res.string.salary_stats),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FinancialCard(
            label = stringResource(Res.string.avg_salary),
            value = analytics.averageAnnualSalary?.formatCurrency() ?: "-",
            color = MaterialTheme.colorScheme.primaryContainer
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FinancialCard(
                label = stringResource(Res.string.max_salary),
                value = analytics.maxAnnualSalary?.formatCurrency() ?: "-",
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
            FinancialCard(
                label = stringResource(Res.string.min_salary),
                value = analytics.minAnnualSalary?.formatCurrency() ?: "-",
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = stringResource(Res.string.annual_est_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    HorizontalDivider()

    Text(
        text = stringResource(Res.string.hourly_annual_ratio),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RatioBar(
            labels = listOf(stringResource(Res.string.compensation_hourly), stringResource(Res.string.compensation_annual)),
            counts = listOf(analytics.hourlyCount, analytics.annualCount),
            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
        )
    }
}

@Composable
fun SummaryTimelineTab(analytics: JobAnalytics) {
    Text(
        text = stringResource(Res.string.application_velocity),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VelocityCard(
                label = stringResource(Res.string.this_week),
                count = analytics.appsThisWeek,
                trend = analytics.appsThisWeek - analytics.appsLastWeek,
                modifier = Modifier.weight(1f)
            )
            VelocityCard(
                label = stringResource(Res.string.last_week),
                count = analytics.appsLastWeek,
                modifier = Modifier.weight(1f)
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(stringResource(Res.string.avg_per_week), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${analytics.averageAppsPerWeek.format(1)} apps/week",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryDistributionTab(analytics: JobAnalytics) {
    Text(
        text = stringResource(Res.string.job_type_distribution),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    val jobTypes = analytics.jobTypeCounts.keys.toList()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        jobTypes.forEach { type ->
            val count = analytics.jobTypeCounts[type] ?: 0
            val percentage = if (analytics.totalApps > 0) (count.toFloat() / analytics.totalApps) else 0f
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(type.labelRes), style = MaterialTheme.typography.bodyMedium)
                    Text("$count", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    HorizontalDivider()

    Text(
        text = stringResource(Res.string.top_companies_pipeline),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (analytics.topCompanies.isEmpty()) {
                Text(stringResource(Res.string.no_jobs_found), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            analytics.topCompanies.forEach { (company, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(company.take(1).uppercase(), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(company, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                        Text("$count", modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}

// UI Components

@Composable
fun StatRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun FinancialCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VelocityCard(label: String, count: Int, trend: Int? = null, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                if (trend != null && trend != 0) {
                    Spacer(Modifier.width(4.dp))
                    val color = if (trend > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    val icon = if (trend > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
                        Text("${kotlin.math.abs(trend)}", style = MaterialTheme.typography.labelSmall, color = color)
                    }
                }
            }
        }
    }
}

@Composable
fun RatioBar(labels: List<String>, counts: List<Int>, colors: List<Color>) {
    val total = counts.sum().toFloat()
    if (total == 0f) return
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            counts.forEachIndexed { index, count ->
                if (count > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(count.toFloat())
                            .background(colors[index])
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            labels.forEachIndexed { index, label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(colors[index]))
                    Spacer(Modifier.width(4.dp))
                    Text("$label (${counts[index]})", style = MaterialTheme.typography.labelSmall)
                }
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

// Extensions
fun Double.format(digits: Int) = this.toString().substringBefore(".") + "." + this.toString().substringAfter(".").take(digits)

fun Double.formatCurrency(): String {
    // Simple placeholder for multiplatform currency formatting
    return if (this >= 1000) "${(this / 1000).format(1)}k" else this.format(0)
}


