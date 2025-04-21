package com.tuk.searchble.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@SuppressLint("ObsoleteSdkInt")
@Composable
fun PermissionRequestScreen(
    onPermissionGranted: @Composable () -> Unit
) {
    val context = LocalContext.current

    // 요청할 권한 목록 결정
    val permissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_FINE_LOCATION  // 앱에서 위치가 꼭 필요하다면
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        else -> emptyArray()
    }

    var allGranted by remember { mutableStateOf(false) }

    // 초기 권한 체크
    LaunchedEffect(Unit) {
        allGranted = permissions.all { p ->
            ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 권한 요청 Launcher
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        allGranted = results.values.all { it }
    }

    if (!allGranted) {
        // 권한이 없으면 다이얼로그로 요청 혹은 설정 유도
        AlertDialog(
            onDismissRequest = { /* 닫기 방지 */ },
            title = { Text("권한 필요") },
            text = {
                Text(
                    """
                    이 앱은 BLE 스캔·연결을 위해 다음 권한이 필요합니다:
                    • 블루투스 스캔/연결
                    • 위치 (앱에서 필요 시)
                    """.trimIndent()
                )
            },
            confirmButton = {
                Button(onClick = { launcher.launch(permissions) }) {
                    Text("권한 요청")
                }
            },
            dismissButton = {
                Button(onClick = {
                    // 설정 화면으로 이동
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("설정 열기")
                }
            }
        )
    } else {
        onPermissionGranted()
    }
}