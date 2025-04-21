package com.tuk.searchble.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tuk.searchble.model.toHexString
import com.tuk.searchble.model.toIBeaconData
import com.tuk.searchble.util.formatTimestamp
import com.tuk.searchble.ui.theme.SearchBLETheme
import com.tuk.searchble.util.getFileName
import com.tuk.searchble.viewmodel.BeaconViewModel
import com.tuk.searchble.viewmodel.IBeaconViewModel
import com.tuk.searchble.viewmodel.FakeBeaconViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeaconDetailScreen(
    macAddress: String,
    viewModel: IBeaconViewModel = hiltViewModel<BeaconViewModel>()
) {
    // 스캔된 Beacon 목록 구독
    val beaconList by viewModel.beaconListFlow.collectAsState()
    // 전달받은 MAC 주소에 해당하는 Beacon 검색
    val beacon = beaconList.find { it.id == macAddress }
    // 스크롤 상태
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    // 자동 스크롤 토글 상태
    var autoScrollState by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // BeaconViewModel에 기록된 major 이벤트 중 해당 Beacon 로그 필터링 및 정렬
    val majorEvents = (viewModel as? BeaconViewModel)
        ?.majorChangeEvents
        ?.filter { it.beaconId == macAddress }
        ?.sortedBy { it.timestamp }
        ?: emptyList()

    // CSV 저장 결과를 표시할 상태 변수
    var savedUri by remember { mutableStateOf<Uri?>(null) }
    var savedFileName by remember { mutableStateOf<String?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }

    // ActivityResultLauncher를 사용하여 사용자가 파일 생성 위치(및 이름)를 선택하게 함.
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val result = (viewModel as? BeaconViewModel)?.saveMajorChangeEventsToUri(uri, macAddress)
                if (result?.isSuccess == true) {
                    savedUri = uri
                    // 저장된 파일 이름을 읽어옵니다.
                    savedFileName = getFileName(context, uri) ?: "알 수 없음"
                    showSaveDialog = true
                } else {
                    // 저장 실패 처리 (원하는 경우 토스트 메시지 등으로 사용자에게 알릴 수 있음)
                    showSaveDialog = false
                }
            }
        }
    )

    // autoScrollEnabled 가 켜져 있을 때만 majorEvents.size 변경 시 자동으로 스크롤
    LaunchedEffect(majorEvents.size, autoScrollState) {
        if (autoScrollState) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("비콘 상세 정보", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    // "맨 아래로" 버튼
                    TextButton(onClick = {
                        autoScrollState = !autoScrollState
                    }) {
                        Text(
                            text = if (autoScrollState) "자동 스크롤 ✔" else "자동 스크롤 ✘",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            )
        },
        bottomBar = {
            // CSV 저장 버튼 고정 (시스템 내비게이션 영역 위로 표시)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                ElevatedButton(
                    onClick = { createDocumentLauncher.launch("major_log.csv") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "CSV 저장", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (beacon != null) {
                    // Beacon 기본 정보 Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "비콘 정보",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "MAC 주소: ${beacon.id}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "비콘 이름: ${beacon.name}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            if (beacon.manufacturerSpecificData != null) {
                                val iBeaconData = beacon.manufacturerSpecificData.toIBeaconData()
                                if (iBeaconData != null) {
                                    Text(
                                        text = "제조사 데이터",
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    DetailRow(label = "회사 ID", value = iBeaconData.companyId)
                                    DetailRow(label = "비콘 타입", value = iBeaconData.beaconType)
                                    DetailRow(label = "데이터 길이", value = iBeaconData.length.toString())
                                    DetailRow(label = "UUID", value = iBeaconData.uuid)
                                    DetailRow(label = "Major", value = iBeaconData.major.toString())
                                    DetailRow(label = "Minor", value = iBeaconData.minor.toString())
                                    DetailRow(label = "TX 파워", value = iBeaconData.txPower.toString())
                                } else {
                                    Text(
                                        text = "제조사 특정 데이터 (파싱 실패): ${beacon.manufacturerSpecificData.toHexString()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "제조사 특정 데이터: 없음",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    // Major 로그를 표 형식으로 표시하는 Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Major 로그",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // 테이블 헤더
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Timestamp",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Major",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (majorEvents.isNotEmpty()) {
                                majorEvents.forEach { event ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatTimestamp(event.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = event.major.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Major 로그 없음",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Beacon 정보를 찾지 못한 경우
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        elevation = CardDefaults.elevatedCardElevation()
                    ) {
                        Text(
                            text = "선택한 비콘 정보를 찾을 수 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // CSV 저장 완료 AlertDialog
    if (showSaveDialog && savedUri != null) {
        // 파일 이름을 가져옵니다.
        val fileName = getFileName(context, savedUri!!) ?: "알 수 없음"
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("CSV 저장 완료") },
            text = { Text("CSV 파일이 저장되었습니다\n${savedUri!!.path + "/" + fileName}") },
            confirmButton = {
                ElevatedButton(
                    onClick = {
                        // "파일 열기" 버튼: ACTION_VIEW로 해당 URI를 연다.
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(savedUri, "text/csv")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                        showSaveDialog = false
                    }
                ) {
                    Text("파일 열기")
                }
            },
            dismissButton = {
                ElevatedButton(
                    onClick = { showSaveDialog = false }
                ) {
                    Text("닫기")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BeaconDetailScreenPreview() {
    SearchBLETheme {
        BeaconDetailScreen(
            macAddress = "10:06:1C:9F:36:C2",
            viewModel = FakeBeaconViewModel()
        )
    }
}