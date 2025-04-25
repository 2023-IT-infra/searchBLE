package com.tuk.searchble.ui


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tuk.searchble.model.Beacon
import com.tuk.searchble.service.BeaconScanService
import com.tuk.searchble.viewmodel.IBeaconViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeaconListScreen(
    viewModel: IBeaconViewModel,
    onBeaconSelected: (Beacon) -> Unit
) {
    val beaconList by viewModel.beaconListFlow.collectAsState()
    val context = LocalContext.current
    var serviceBinder by remember { mutableStateOf<BeaconScanService.LocalBinder?>(null) }

    // 1) 서비스에 바인딩
    DisposableEffect(Unit) {
        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
                serviceBinder = binder as BeaconScanService.LocalBinder
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBinder = null
            }
        }
        context.bindService(Intent(context, BeaconScanService::class.java), conn, Context.BIND_AUTO_CREATE)
        onDispose {
            context.unbindService(conn)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "비콘",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // 스캔 시작/중지 버튼 영역
                if (serviceBinder?.isScanning() != true) {
                    ElevatedButton(
                        onClick = {
                            viewModel.startScan()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "스캔 시작",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                } else {
                    ElevatedButton(
                        onClick = {
                            viewModel.stopScan() // 스캔 중지 함수, 구현되어 있다면
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "스캔 중지",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 비콘 목록이 없는 경우 빈 상태 UI 표시
                if (beaconList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "검색된 비콘이 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(beaconList) { beacon ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onBeaconSelected(beacon) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                // 각 Beacon 항목에 대해 MAC 주소와 추가 정보를 표시할 수 있습니다.
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "비콘 MAC: ${beacon.id}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    // 필요하면 추가 정보(예: 이름, RSSI 등)를 더 추가할 수 있습니다.
                                    Text(
                                        text = "이름: ${beacon.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}