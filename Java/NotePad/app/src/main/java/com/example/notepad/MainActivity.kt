package com.example.notepad

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.PushPinOutlined
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notepad.data.Note
import com.example.notepad.ui.NoteViewModel
import com.example.notepad.ui.editor.EditorViewModel
import com.example.notepad.ui.browser.PrivateBrowserActivity
import android.os.SystemClock

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NotePadApp() }
    }
}

@Composable
private fun NotePadApp() {
    val vm: NoteViewModel = viewModel()
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    var showEditor by remember { mutableStateOf<Long?>(null) }

    // Triple tap detection variables
    var tapTimes by remember { mutableStateOf(listOf<Long>()) }

    fun registerTap() {
        val now = System.currentTimeMillis()
        tapTimes = (tapTimes + now).takeLast(3)
        if (tapTimes.size == 3 && (tapTimes.last() - tapTimes.first()) < 600) {
            tapTimes = emptyList()
            ctx.startActivity(Intent(ctx, PrivateBrowserActivity::class.java))
        }
    }

    MaterialTheme { // Simple theme; dynamic theming placeholder removed for stability
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showEditor = 0 }) { Text("+") }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::setQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { registerTap() },
                    placeholder = { Text("Search notes") },
                    singleLine = true
                )
                if (state.filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize().clickable { registerTap() }) {
                        Text(
                            "No notes yet. Triple tap anywhere to open Private Browser",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    NoteList(notes = state.filtered, onTap = { showEditor = it.id }, onPin = vm::togglePin, onDelete = vm::delete, registerTap = { registerTap() })
                }
            }
        }
    }

    if (showEditor != null) {
        NoteEditorDialog(noteId = showEditor!!, onClose = { showEditor = null })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteList(notes: List<Note>, onTap: (Note) -> Unit, onPin: (Note) -> Unit, onDelete: (Note) -> Unit, registerTap: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().clickable { registerTap() }) {
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
                        if (note.pinned) Text("📌")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(note.content.take(200), style = MaterialTheme.typography.bodyMedium)
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
                OutlinedTextField(
                    value = state.content,
                    onValueChange = vm::setContent,
                    label = { Text("Content") },
                    modifier = Modifier.heightIn(min = 200.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { vm.save { onClose() } }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Close") } }
    )
}

// Removed dynamicColorScheme indirection to avoid accessing MaterialTheme before it's provided.
