package com.example.notepad.ui.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notepad.ai.LocalAIManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AIChattActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AIChattScreen(onClose = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChattScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aiManager = remember { LocalAIManager(context) }
    
    var messages by remember { mutableStateOf<List<LocalAIManager.ChatMessage>>(emptyList()) }
    var currentInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var currentConversation by remember { mutableStateOf<LocalAIManager.Conversation?>(null) }
    var showModelDialog by remember { mutableStateOf(false) }
    var availableModels by remember { mutableStateOf<List<LocalAIManager.AIModel>>(emptyList()) }
    var downloadedModels by remember { mutableStateOf<List<LocalAIManager.AIModel>>(emptyList()) }
    var downloadProgress by remember { mutableStateOf<LocalAIManager.DownloadProgress?>(null) }
    
    val listState = rememberLazyListState()
    
    // Initialize conversation and load models
    LaunchedEffect(Unit) {
        currentConversation = aiManager.createNewConversation("AI Chat")
        messages = currentConversation?.messages ?: emptyList()
        availableModels = aiManager.getAvailableModels()
        downloadedModels = aiManager.getDownloadedModels()
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = "AI",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Local AI Chat", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "🔒 Private • Offline • Unrestricted", 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showModelDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = "Download Models")
                    }
                    IconButton(onClick = { 
                        // Clear conversation
                        scope.launch {
                            currentConversation = aiManager.createNewConversation("New Chat")
                            messages = emptyList()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "New Chat")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        placeholder = { Text("Ask me anything...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        enabled = !isLoading
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    FloatingActionButton(
                        onClick = {
                            if (currentInput.isNotBlank() && !isLoading) {
                                val input = currentInput
                                currentInput = ""
                                isLoading = true
                                
                                scope.launch {
                                    aiManager.sendMessage(input, currentConversation?.id)
                                        .onSuccess { response ->
                                            messages = currentConversation?.messages ?: emptyList()
                                            isLoading = false
                                        }
                                        .onFailure { error ->
                                            isLoading = false
                                            // Handle error - could show snackbar
                                        }
                                }
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = if (currentInput.isNotBlank() && !isLoading) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Send, 
                                contentDescription = "Send",
                                tint = if (currentInput.isNotBlank()) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (messages.isEmpty()) {
                // Welcome screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = "AI",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Your Personal AI Assistant",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "Completely private and unrestricted",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "✨ Features:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            
                            val features = listOf(
                                "🔒 100% offline and private",
                                "🚀 No content restrictions", 
                                "💬 Natural conversations",
                                "📝 Writing and coding help",
                                "🧠 Analysis and research",
                                "🎨 Creative assistance"
                            )
                            
                            features.forEach { feature ->
                                Text(
                                    feature,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Start by asking me anything!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Chat messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageItem(message = message)
                    }
                }
            }
        }
        
        // Model Download Dialog
        if (showModelDialog) {
            AlertDialog(
                onDismissRequest = { showModelDialog = false },
                title = { Text("🤖 AI Models") },
                text = {
                    Column(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        Text(
                            "Download models for advanced AI capabilities:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        LazyColumn {
                            items(availableModels) { model ->
                                val isDownloaded = downloadedModels.any { it.filename == model.filename }
                                val isDownloading = downloadProgress != null
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    model.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    model.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    "${model.size / 1_000_000} MB",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            
                                            when {
                                                isDownloaded -> {
                                                    Text(
                                                        "✅ Downloaded",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                isDownloading -> {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(20.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                                else -> {
                                                    Button(
                                                        onClick = {
                                                            scope.launch {
                                                                aiManager.downloadModel(model) { progress ->
                                                                    downloadProgress = progress
                                                                    if (progress.isComplete) {
                                                                        downloadProgress = null
                                                                        // Refresh downloaded models
                                                                        scope.launch {
                                                                            downloadedModels = aiManager.getDownloadedModels()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.width(80.dp).height(32.dp)
                                                    ) {
                                                        Text("Download", style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // Download progress bar
                                        downloadProgress?.let { progress ->
                                            Spacer(Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { progress.percentage / 100f },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Text(
                                                "${progress.percentage.toInt()}% (${progress.bytesDownloaded / 1_000_000}/${progress.totalBytes / 1_000_000} MB)",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showModelDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: LocalAIManager.ChatMessage) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            // AI avatar
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = "AI",
                    modifier = Modifier.padding(6.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isUser) 16.dp else 4.dp,
                    topEnd = if (message.isUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (message.isUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    color = if (message.isUser) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Text(
                text = timeFormat.format(Date(message.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        
        if (message.isUser) {
            Spacer(Modifier.width(8.dp))
            // User avatar
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "User",
                    modifier = Modifier.padding(6.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}