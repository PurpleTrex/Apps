package com.hades.sshserver.data

/**
 * SSH Server configuration
 */
data class ServerConfig(
    val port: Int = 2222,
    val bindAddress: String = "0.0.0.0",
    val autoStartOnBoot: Boolean = false,
    val maxConnections: Int = 5,
    val sessionTimeout: Int = 300, // seconds
    val allowPasswordAuth: Boolean = true,
    val allowPublicKeyAuth: Boolean = true,
    val enableLogging: Boolean = true
)

/**
 * User credentials for SSH authentication
 */
data class UserCredentials(
    val username: String,
    val passwordHash: String, // Never store plain text
    val authorizedKeys: List<AuthorizedKey> = emptyList()
)

/**
 * Authorized SSH public key
 */
data class AuthorizedKey(
    val keyType: String, // "rsa", "ecdsa", "ed25519"
    val publicKey: String,
    val fingerprint: String,
    val comment: String = ""
)

/**
 * Active SSH session information
 */
data class SshSession(
    val sessionId: String,
    val username: String,
    val clientIp: String,
    val connectedAt: Long,
    val lastActivity: Long,
    val isActive: Boolean = true
)

/**
 * Connection log entry
 */
data class ConnectionLog(
    val timestamp: Long,
    val event: ConnectionEvent,
    val username: String?,
    val clientIp: String,
    val details: String
)

enum class ConnectionEvent {
    CONNECT_ATTEMPT,
    AUTH_SUCCESS,
    AUTH_FAILURE,
    DISCONNECT,
    FILE_ACCESS,
    ERROR
}

/**
 * Server status
 */
enum class ServerStatus {
    STOPPED,
    STARTING,
    RUNNING,
    STOPPING,
    ERROR
}
