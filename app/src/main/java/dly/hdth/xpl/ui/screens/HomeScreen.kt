package dly.hdth.xpl.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dly.hdth.xpl.ui.components.AppHeader
import dly.hdth.xpl.ui.components.PreviousHadithTile
import dly.hdth.xpl.ui.components.SearchBarComponent
import dly.hdth.xpl.ui.components.TodayHadithTile
import dly.hdth.xpl.ui.theme.EkusheyLalsaluFontFamily
import dly.hdth.xpl.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onHadithClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Request notification permission once on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val pref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            val asked = pref.getBoolean("notification_asked", false)
            if (!asked) {
                pref.edit().putBoolean("notification_asked", true).apply()
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            AppHeader()

            SearchBarComponent(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            val query = uiState.searchQuery.trim()

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (query.isEmpty()) {
                    item {
                        TodayHadithTile(
                            hadith = uiState.todayHadith,
                            onClick = {
                                uiState.todayHadith?.dateCode?.let { onHadithClick(it) }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "পূর্বের হাদীস সমূহ",
                            fontFamily = EkusheyLalsaluFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(uiState.previousHadiths) { hadith ->
                        PreviousHadithTile(
                            hadith = hadith,
                            onClick = { onHadithClick(hadith.dateCode) }
                        )
                    }
                } else {
                    // Search results mode
                    val filteredPrevious = uiState.previousHadiths.filter {
                        it.bangla.contains(query, ignoreCase = true) ||
                                it.arabic.contains(query, ignoreCase = true) ||
                                it.explanationMarkdown.contains(query, ignoreCase = true)
                    }

                    val todayMatches = uiState.todayHadith?.let {
                        if (!it.isPlaceholder && (
                                    it.bangla.contains(query, ignoreCase = true) ||
                                            it.arabic.contains(query, ignoreCase = true) ||
                                            it.explanationMarkdown.contains(query, ignoreCase = true)
                                    )) it else null
                    }

                    if (todayMatches != null) {
                        item {
                            Text(
                                text = "আজকের হাদীস (ফলাফল)",
                                fontFamily = EkusheyLalsaluFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            TodayHadithTile(
                                hadith = todayMatches,
                                onClick = { onHadithClick(todayMatches.dateCode) }
                            )
                        }
                    }

                    if (filteredPrevious.isNotEmpty()) {
                        item {
                            Text(
                                text = "পূর্বের হাদীস সমূহ (ফলাফল)",
                                fontFamily = EkusheyLalsaluFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(filteredPrevious) { hadith ->
                            PreviousHadithTile(
                                hadith = hadith,
                                onClick = { onHadithClick(hadith.dateCode) }
                            )
                        }
                    }

                    if (todayMatches == null && filteredPrevious.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "কোনো হাদীস পাওয়া যায়নি",
                                    fontFamily = EkusheyLalsaluFontFamily,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
