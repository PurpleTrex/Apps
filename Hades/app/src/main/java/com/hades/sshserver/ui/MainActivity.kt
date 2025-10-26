package com.hades.sshserver.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.hades.sshserver.ui.screens.FileBrowserScreen
import com.hades.sshserver.ui.screens.ServerStatusScreen
import com.hades.sshserver.ui.theme.HadesTheme
import com.hades.sshserver.ui.viewmodel.FileBrowserViewModel
import com.hades.sshserver.ui.viewmodel.ServerViewModel

class MainActivity : ComponentActivity() {
    private lateinit var fileBrowserViewModel: FileBrowserViewModel
    private lateinit var serverViewModel: ServerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModels
        fileBrowserViewModel = FileBrowserViewModel(this)
        serverViewModel = ServerViewModel(this)

        setContent {
            HadesTheme {
                MainScreen(
                    fileBrowserViewModel = fileBrowserViewModel,
                    serverViewModel = serverViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    fileBrowserViewModel: FileBrowserViewModel,
    serverViewModel: ServerViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("File Browser", "Server")

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Folder, "File Browser") },
                    label = { Text("Files") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, "Server") },
                    label = { Text("Server") }
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.padding(padding)
        ) {
            when (selectedTab) {
                0 -> FileBrowserScreen(viewModel = fileBrowserViewModel)
                1 -> ServerStatusScreen(viewModel = serverViewModel)
            }
        }
    }
}
