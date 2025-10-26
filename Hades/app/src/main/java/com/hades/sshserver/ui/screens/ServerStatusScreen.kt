package com.hades.sshserver.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hades.sshserver.data.ServerStatus
import com.hades.sshserver.service.SshServerService
import com.hades.sshserver.ui.viewmodel.ServerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerStatusScreen(
    viewModel: ServerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val serverStatus by viewModel.serverStatus.collectAsState()
    val serverConfig by viewModel.serverConfig.collectAsState()
    val localIpAddress by viewModel.localIpAddress.collectAsState()
    val isWifiConnected by viewModel.isWifiConnected.collectAsState()

    var sshService by remember { mutableStateOf<SshServerService?>(null) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as SshServerService.SshServerBinder
                sshService = binder.getService()
                
                // Update status from service
                LaunchedEffect(sshService) {
                    sshService?.serverStatus?.collect { status ->
                        viewModel.updateServerStatus(status)
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                sshService = null
            }
        }
    }

    DisposableEffect(Unit) {
        val intent = Intent(context, SshServerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateNetworkInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server Status") }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server Status Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (serverStatus) {
                                ServerStatus.RUNNING -> Icons.Default.CheckCircle
                                ServerStatus.STOPPED -> Icons.Default.Cancel
                                ServerStatus.STARTING, ServerStatus.STOPPING -> Icons.Default.Refresh
                                ServerStatus.ERROR -> Icons.Default.Error
                            },
                            contentDescription = null,
                            tint = when (serverStatus) {
                                ServerStatus.RUNNING -> MaterialTheme.colorScheme.primary
                                ServerStatus.STOPPED -> MaterialTheme.colorScheme.onSurfaceVariant
                                ServerStatus.ERROR -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            text = when (serverStatus) {
                                ServerStatus.RUNNING -> "Server Running"
                                ServerStatus.STOPPED -> "Server Stopped"
                                ServerStatus.STARTING -> "Starting Server..."
                                ServerStatus.STOPPING -> "Stopping Server..."
                                ServerStatus.ERROR -> "Server Error"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Divider()

                    // Connection Info
                    if (serverStatus == ServerStatus.RUNNING) {
                        InfoRow("Port", serverConfig.port.toString())
                        InfoRow("Bind Address", serverConfig.bindAddress)
                        localIpAddress?.let {
                            InfoRow("IP Address", it)
                            InfoRow(
                                "Connection Command",
                                "ssh admin@$it -p ${serverConfig.port}"
                            )
                        }
                    }

                    // Network Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isWifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (isWifiConnected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                        Text(
                            text = if (isWifiConnected) "WiFi Connected" else "No WiFi Connection",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, SshServerService::class.java).apply {
                            action = SshServerService.ACTION_START_SERVER
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = serverStatus == ServerStatus.STOPPED
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Server")
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, SshServerService::class.java).apply {
                            action = SshServerService.ACTION_STOP_SERVER
                        }
                        context.startService(intent)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = serverStatus == ServerStatus.RUNNING
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Stop Server")
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Default Credentials",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = "Username: admin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Password: admin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Note: Change these credentials in a production environment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
