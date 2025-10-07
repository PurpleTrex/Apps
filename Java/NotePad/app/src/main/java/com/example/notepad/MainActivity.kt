package com.example.notepad

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notepad.data.Note
import com.example.notepad.security.AppDestructionManager
import com.example.notepad.ui.NoteViewModel
import com.example.notepad.ui.browser.PrivateBrowserActivity
import com.example.notepad.ui.editor.EditorViewModel
import com.example.notepad.ui.vault.SecretVaultActivity
import com.example.notepad.ui.setup.SetupActivity
import com.example.notepad.ui.auth.PinLockScreen
import com.example.notepad.security.SecurityManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if setup is needed
        if (!SecurityManager.hasCompletedSetup(this)) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }
        
        setContent { NotePadRoot() }
    }
}

@Composable
private fun NotePadRoot() {
    val vm: NoteViewModel = viewModel()
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showEditor by remember { mutableStateOf<Long?>(null) }
    var pendingLockedNote by remember { mutableStateOf<Long?>(null) }
    var showPinLock by remember { mutableStateOf(SecurityManager.isLockEnabled(ctx)) }
    var isAuthenticated by remember { mutableStateOf(!SecurityManager.isLockEnabled(ctx)) }
    var showLockedNotePinPrompt by remember { mutableStateOf(false) }
    
    // Show PIN lock screen on app start if enabled
    if (!isAuthenticated && showPinLock) {
        PinLockScreen(
            title = "Unlock PurplePad",
            onAuthenticated = { 
                isAuthenticated = true
                showPinLock = false
            },
            onDistressCode = {
                // Distress code handles its own destruction
            }
        )
        return
    }
    
    // Show PIN lock for accessing locked note
    if (showLockedNotePinPrompt && pendingLockedNote != null) {
        PinLockScreen(
            title = "Unlock Note",
            onAuthenticated = { 
                showEditor = pendingLockedNote
                pendingLockedNote = null
                showLockedNotePinPrompt = false
            },
            onDistressCode = {
                // Distress code handles its own destruction
            }
        )
        return
    }

    // Triple tap detection for private browser
    var tapTimes by remember { mutableStateOf(listOf<Long>()) }
    var pendingBrowserLaunch by remember { mutableStateOf<Long?>(null) }
    
    // Simple tap counter for gesture detection
    var lastTapTime by remember { mutableStateOf(0L) }
    var consecutiveTaps by remember { mutableStateOf(0) }
    
    // Long press menu state
    var showLongPressMenu by remember { mutableStateOf(false) }
    
    // Long press destruction tracking (4 seconds silent, then confirm)
    var longPressStartTime by remember { mutableStateOf<Long?>(null) }
    var longPressProgress by remember { mutableStateOf(0f) }
    var showDestructionConfirm by remember { mutableStateOf(false) }

    fun registerTap() {
        val now = System.currentTimeMillis()
        tapTimes = (tapTimes + now).takeLast(4) // Keep last 4 taps
        
        // Check for 4 taps first (secret vault)
        if (tapTimes.size == 4 && (tapTimes.last() - tapTimes.first()) < 800) {
            tapTimes = emptyList()
            pendingBrowserLaunch = null
            ctx.startActivity(Intent(ctx, SecretVaultActivity::class.java))
            return
        }
        
        // Check for 3 taps (private browser) - but wait a bit to see if 4th tap comes
        if (tapTimes.size >= 3) {
            val last3 = tapTimes.takeLast(3)
            if ((last3.last() - last3.first()) < 600) {
                // Wait 300ms to see if a 4th tap comes
                pendingBrowserLaunch = now
                scope.launch {
                    delay(300)
                    if (pendingBrowserLaunch == now && tapTimes.size == 3) {
                        tapTimes = emptyList()
                        ctx.startActivity(Intent(ctx, PrivateBrowserActivity::class.java))
                    }
                }
            }
        }
    }
    
    // Monitor long press progress - NO visual warning until 4 seconds
    LaunchedEffect(longPressStartTime) {
        longPressStartTime?.let { startTime ->
            Log.d("MainActivity", "Long press timer STARTED")
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                longPressProgress = (elapsed / 4000f).coerceIn(0f, 1f)
                
                if (elapsed >= 4000) {
                    // 4 seconds reached - show confirmation dialog
                    Log.d("MainActivity", "4 SECONDS REACHED - showing dialog")
                    longPressStartTime = null
                    longPressProgress = 0f
                    showDestructionConfirm = true
                    break
                }
                
                delay(50)
            }
        }
        
        if (longPressStartTime == null) {
            longPressProgress = 0f
        }
    }

    val dark = state.darkTheme
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) { // simple toggle
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(onClick = { showEditor = 0 }) { Text("+") }
                }
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    // Only handle taps on the background, not on components
                                    val tapTime = System.currentTimeMillis()
                                    if (tapTime - lastTapTime < 1000) {
                                        consecutiveTaps++
                                    } else {
                                        consecutiveTaps = 1
                                    }
                                    lastTapTime = tapTime
                                    
                                    // 3 taps opens browser
                                    if (consecutiveTaps == 3) {
                                        val intent = Intent(ctx, com.example.notepad.ui.browser.PrivateBrowserActivity::class.java)
                                        ctx.startActivity(intent)
                                        consecutiveTaps = 0
                                    }
                                },
                                onLongPress = { offset ->
                                    // Long press shows menu
                                    showLongPressMenu = true
                                }
                            )
                        }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(onClick = { vm.toggleTheme() }, label = { Text(if (dark) "Light" else "Dark") })
                        AssistChip(
                            onClick = { 
                                val intent = Intent(ctx, com.example.notepad.ui.ai.AIChattActivity::class.java)
                                ctx.startActivity(intent)
                            }, 
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🤖 AI Chat")
                                }
                            }
                        )
                    }
                    if (state.allTags.isNotEmpty()) {
                        LazyRow(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                            item { AssistChip(onClick = { vm.setTagFilter(null) }, label = { Text("All") }) }
                            items(state.allTags) { tag ->
                                val selected = state.tagFilter == tag
                                AssistChip(onClick = { vm.setTagFilter(if (selected) null else tag) }, label = { Text(if (selected) "#$tag" else tag) })
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = vm::setQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search notes...") },
                        singleLine = true
                        )
                    if (state.filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize()) {
                            Text(
                                "No notes yet. Tap + to create your first note.",
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    } else {
                        NoteList(notes = state.filtered, onTap = { note ->
                            if (note.locked) {
                                // ALWAYS require PIN for locked notes, even if already authenticated
                                pendingLockedNote = note.id
                                showLockedNotePinPrompt = true
                            } else {
                                showEditor = note.id
                            }
                        }, onPin = vm::togglePin, onDelete = vm::delete, tagsMap = state.noteTags)
                    }
                }
            }
            

            // Long press menu dialog
            if (showLongPressMenu) {
                AlertDialog(
                    onDismissRequest = { showLongPressMenu = false },
                    title = { 
                        Text(
                            "🔒 Secret Options",
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    text = {
                        Text("Choose an action:")
                    },
                    confirmButton = {
                        Row {
                            TextButton(
                                onClick = {
                                    showLongPressMenu = false
                                    val intent = Intent(ctx, com.example.notepad.ui.vault.SecretVaultActivity::class.java)
                                    ctx.startActivity(intent)
                                }
                            ) {
                                Text("🗄️ Open Vault")
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    showLongPressMenu = false
                                    showDestructionConfirm = true
                                }
                            ) {
                                Text("💥 Purge", color = Color.Red)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLongPressMenu = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            // Destruction confirmation dialog (appears AFTER 4 seconds)
            if (showDestructionConfirm) {
                AlertDialog(
                    onDismissRequest = { showDestructionConfirm = false },
                    title = { 
                        Text(
                            "⚠️ DESTROY ALL DATA?",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    text = {
                        Column {
                            Text(
                                "This will:",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("• Corrupt and overwrite all databases")
                            Text("• Purge all notes and cached data")
                            Text("• Clear all preferences and files")
                            Text("• Uninstall the app from device")
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "⚠️ THIS CANNOT BE UNDONE! ⚠️",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDestructionConfirm = false
                                scope.launch {
                                    AppDestructionManager.executeDestruction(ctx)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text("DESTROY ALL DATA")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDestructionConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        if (showEditor != null) {
            NoteEditorDialog(noteId = showEditor!!, onClose = { showEditor = null })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteList(notes: List<Note>, onTap: (Note) -> Unit, onPin: (Note) -> Unit, onDelete: (Note) -> Unit, tagsMap: Map<Long, List<String>>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(notes, key = { it.id }) { note ->
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .fillMaxWidth()
                    .clickable { onTap(note) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(note.title.ifBlank { "(Untitled)" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row { if (note.pinned) Text("📌") ; if (note.locked) Text("🔒") }
                    }
                    Spacer(Modifier.height(4.dp))
                    val display = if (note.locked) "🔒 Locked Note - Content Hidden" else renderChecklistPreview(note.content)
                    Text(display.take(200), style = MaterialTheme.typography.bodyMedium)
                    val tags = tagsMap[note.id]
                    if (!tags.isNullOrEmpty()) {
                        FlowRow(modifier = Modifier.fillMaxWidth()) {
                            tags.forEach { t -> AssistChip(onClick = {}, label = { Text(t) }); Spacer(Modifier.width(4.dp)) }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onPin(note) }) { Text(if (note.pinned) "Unpin" else "Pin") }
                        TextButton(onClick = { onDelete(note) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(noteId: Long, onClose: () -> Unit) {
    val vm: EditorViewModel = viewModel()
    val state by vm.state.collectAsState()
    val isLoaded = remember { mutableStateOf(false) }
    if (noteId != 0L && !isLoaded.value) {
        LaunchedEffect(noteId) { vm.load(noteId); isLoaded.value = true }
    }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(if (state.isNew && noteId == 0L) "New Note" else "Edit Note") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(value = state.title, onValueChange = vm::setTitle, label = { Text("Title") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                if (!state.markdownPreview) {
                    OutlinedTextField(
                        value = state.content,
                        onValueChange = vm::setContent,
                        label = { Text("Content / Markdown / Checklists") },
                        modifier = Modifier.heightIn(min = 200.dp)
                    )
                } else {
                    val plain = state.htmlRendered.replace(Regex("<[^>]*>"), "")
                    Box(Modifier.heightIn(min = 200.dp).fillMaxWidth().padding(4.dp)) { Text(plain) }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = state.tags, onValueChange = vm::setTags, label = { Text("Tags (comma separated)") })
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Locked")
                    Switch(checked = state.locked, onCheckedChange = { vm.toggleLocked() })
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { vm.toggleMarkdown() }, label = { Text(if (state.markdownPreview) "Edit" else "Preview") })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { vm.save { onClose() } }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Close") } }
    )
}

@Composable
private fun renderChecklistPreview(content: String): String {
    return content.lines().joinToString("\n") { line ->
        val trimmed = line.trim()
        when {
            trimmed.startsWith("- [ ]") -> "☐ " + trimmed.removePrefix("- [ ]").trim()
            trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]") -> "☑ " + trimmed.removePrefix("- [x]").removePrefix("- [X]").trim()
            else -> line
        }
    }
}

@Composable
private fun FlowRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier) { content() }
}

// Removed dynamicColorScheme indirection to avoid accessing MaterialTheme before it's provided.
