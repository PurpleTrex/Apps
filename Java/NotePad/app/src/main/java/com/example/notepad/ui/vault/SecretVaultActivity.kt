package com.example.notepad.ui.vault

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.notepad.security.SecretVaultManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SecretVaultActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SecretVaultScreen(onClose = { finish() })
            }
        }
    }
    
    override fun onBackPressed() {
        // Clear any temporary files
        cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("vault_temp_")) {
                file.delete()
            }
        }
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretVaultScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vaultManager = remember { SecretVaultManager(context) }
    
    var items by remember { mutableStateOf<List<SecretVaultManager.VaultItem>>(emptyList()) }
    var stats by remember { mutableStateOf<SecretVaultManager.VaultStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<SecretVaultManager.VaultItem?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isLoading = true
                
                // Get file name from URI
                val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                } ?: "unknown_file"
                
                vaultManager.importFile(uri, fileName, deleteSource = true)
                
                // Refresh list
                items = vaultManager.getAllItems()
                stats = vaultManager.getVaultStats()
                isLoading = false
            }
        }
    }
    
    // Load items on start
    LaunchedEffect(Unit) {
        items = vaultManager.getAllItems()
        stats = vaultManager.getVaultStats()
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Secure",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Secret Vault")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Clear All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("*/*") },
                containerColor = Color(0xFF6A34D9)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add File", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0D0D0D))
        ) {
            // Stats bar
            stats?.let { s ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1A1A1A)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Files", s.totalItems.toString())
                        StatItem("Images", s.imageCount.toString())
                        StatItem("Videos", s.videoCount.toString())
                        StatItem("Size", formatSize(s.totalSize))
                    }
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6A34D9))
                }
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No files in vault",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap + to add files",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        VaultItemCard(
                            item = item,
                            vaultManager = vaultManager,
                            onClick = { selectedItem = it },
                            onDelete = {
                                selectedItem = it
                                showDeleteConfirm = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirm && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete File?") },
            text = { Text("Are you sure you want to delete ${selectedItem?.fileName}? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            selectedItem?.let { vaultManager.deleteFile(it.id) }
                            items = vaultManager.getAllItems()
                            stats = vaultManager.getVaultStats()
                            isLoading = false
                            showDeleteConfirm = false
                            selectedItem = null
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Clear vault confirmation dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Entire Vault?") },
            text = { Text("⚠️ This will permanently delete ALL files in the vault. This cannot be undone!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            vaultManager.clearVault()
                            items = vaultManager.getAllItems()
                            stats = vaultManager.getVaultStats()
                            isLoading = false
                            showClearConfirm = false
                        }
                    }
                ) {
                    Text("DELETE ALL", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // View/Export item dialog
    if (selectedItem != null && !showDeleteConfirm) {
        VaultItemDialog(
            item = selectedItem!!,
            vaultManager = vaultManager,
            onDismiss = { selectedItem = null },
            onDelete = {
                showDeleteConfirm = true
            }
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6A34D9)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun VaultItemCard(
    item: SecretVaultManager.VaultItem,
    vaultManager: SecretVaultManager,
    onClick: (SecretVaultManager.VaultItem) -> Unit,
    onDelete: (SecretVaultManager.VaultItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    var thumbnail by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    LaunchedEffect(item.id) {
        thumbnail = vaultManager.getThumbnail(item)
    }
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick(item) },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = item.fileName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (item.type) {
                            SecretVaultManager.VaultItemType.IMAGE -> Icons.Default.Image
                            SecretVaultManager.VaultItemType.VIDEO -> Icons.Default.VideoLibrary
                            SecretVaultManager.VaultItemType.DOCUMENT -> Icons.Default.Description
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            // Overlay with file info
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    item.fileName.take(15) + if (item.fileName.length > 15) "..." else "",
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun VaultItemDialog(
    item: SecretVaultManager.VaultItem,
    vaultManager: SecretVaultManager,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.fileName) },
        text = {
            Column {
                Text("Type: ${item.type}")
                Text("Size: ${formatSize(item.size)}")
                Text("Added: ${dateFormat.format(Date(item.dateAdded))}")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        val result = vaultManager.exportFile(item.id)
                        result.onSuccess { file ->
                            // Open file with external app
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, context.contentResolver.getType(uri))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Open with"))
                        }
                    }
                }
            ) {
                Text("Open")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = Color.Red)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
