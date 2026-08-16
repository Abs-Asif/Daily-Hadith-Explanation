package dly.hdth.xpl.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dly.hdth.xpl.data.model.Hadith
import dly.hdth.xpl.ui.components.SimpleMarkdownViewer
import dly.hdth.xpl.ui.theme.EkusheyLalsaluFontFamily
import dly.hdth.xpl.ui.theme.ScheherazadeFontFamily
import dly.hdth.xpl.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithDetailScreen(
    dateCode: String,
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hadith by remember { mutableStateOf<Hadith?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(dateCode) {
        hadith = viewModel.getHadithByDate(dateCode)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "হাদীসের ব্যাখ্যা",
                        fontFamily = EkusheyLalsaluFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val currentHadith = hadith
            if (currentHadith == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "হাদীস পাওয়া যায়নি",
                        fontFamily = EkusheyLalsaluFontFamily
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (currentHadith.arabic.isNotBlank()) {
                        Text(
                            text = currentHadith.arabic,
                            fontFamily = ScheherazadeFontFamily,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 36.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (currentHadith.bangla.isNotBlank()) {
                        Text(
                            text = currentHadith.bangla,
                            fontFamily = EkusheyLalsaluFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    SimpleMarkdownViewer(
                        markdownText = currentHadith.explanationMarkdown
                    )
                }
            }
        }
    }
}
