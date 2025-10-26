package com.hades.sshserver.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.hades.sshserver.data.ServerConfig
import com.hades.sshserver.data.ServerStatus
import com.hades.sshserver.data.SshSession
import com.hades.sshserver.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ServerViewModel(private val context: Context) : ViewModel() {

    private val _serverStatus = MutableStateFlow(ServerStatus.STOPPED)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    private val _serverConfig = MutableStateFlow(ServerConfig())
    val serverConfig: StateFlow<ServerConfig> = _serverConfig.asStateFlow()

    private val _activeSessions = MutableStateFlow<List<SshSession>>(emptyList())
    val activeSessions: StateFlow<List<SshSession>> = _activeSessions.asStateFlow()

    private val _localIpAddress = MutableStateFlow<String?>(null)
    val localIpAddress: StateFlow<String?> = _localIpAddress.asStateFlow()

    private val _isWifiConnected = MutableStateFlow(false)
    val isWifiConnected: StateFlow<Boolean> = _isWifiConnected.asStateFlow()

    init {
        updateNetworkInfo()
    }

    fun updateServerStatus(status: ServerStatus) {
        _serverStatus.value = status
    }

    fun updateConfig(config: ServerConfig) {
        _serverConfig.value = config
    }

    fun updateActiveSessions(sessions: List<SshSession>) {
        _activeSessions.value = sessions
    }

    fun updateNetworkInfo() {
        _localIpAddress.value = NetworkUtils.getLocalIpAddress()
        _isWifiConnected.value = NetworkUtils.isWifiConnected(context)
    }

    fun getConnectionInfo(): String {
        val ip = _localIpAddress.value ?: "N/A"
        val port = _serverConfig.value.port
        return "ssh username@$ip -p $port"
    }
}
