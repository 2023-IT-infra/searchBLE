package com.tuk.searchble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tuk.searchble.ui.BeaconDetailScreen
import com.tuk.searchble.ui.BeaconListScreen
import com.tuk.searchble.ui.PermissionRequestScreen
import com.tuk.searchble.viewmodel.BeaconViewModel
import com.tuk.searchble.viewmodel.FakeBeaconViewModel
import com.tuk.searchble.viewmodel.IBeaconViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BeaconViewModel = hiltViewModel()
            Main(viewModel)
        }
    }
}


@Composable
fun Main(viewModel: IBeaconViewModel = hiltViewModel<BeaconViewModel>()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "beaconList") {
        composable("beaconList") {
            // 권한이 승인된 경우에만 비콘 목록을 보여주고,
            // 항목을 터치하면 onBeaconSelected 콜백에서 navController.navigate() 호출
            PermissionRequestScreen {
                BeaconListScreen(
                    viewModel = viewModel,
                    onBeaconSelected = { beacon ->
                        // beacon.id(또는 beacon.device.address)를 인자로 전달합니다.
                        navController.navigate("beaconDetail/${beacon.id}")
                    }
                )
            }
        }
        composable(
            route = "beaconDetail/{macAddress}",
            arguments = listOf(navArgument("macAddress") { type = NavType.StringType })
        ) { backStackEntry ->
            val macAddress = backStackEntry.arguments?.getString("macAddress") ?: ""
            BeaconDetailScreen(macAddress = macAddress)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    // 권한이 승인된 경우에만 BeaconScreen 표시 (BLE 스캔, GATT 연결, 데이터 수신)
    val fakeViewModel = FakeBeaconViewModel()
    Main(fakeViewModel)
}