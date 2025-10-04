package com.example.notepad.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.notepad.security.AppDestructionManager
import com.example.notepad.security.SecurityManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinLockScreen(
    title: String = "Enter PIN",
    onAuthenticated: () -> Unit,
    onDistressCode: suspend () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var showFakeCrash by remember { mutableStateOf(false) }
    
    if (showFakeCrash) {
        // Fake crash screen during distress code execution
        FakeCrashScreen()
        return
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A34D9)
                )
                
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it; errorMessage = "" },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isProcessing
                )
                
                if (errorMessage.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            when (SecurityManager.verifyPin(context, pin)) {
                                1 -> {
                                    // Correct PIN
                                    onAuthenticated()
                                }
                                2 -> {
                                    // DISTRESS CODE - Trigger destruction
                                    showFakeCrash = true
                                    onDistressCode()
                                    delay(1000) // Show fake crash briefly
                                    AppDestructionManager.executeDestruction(context)
                                }
                                else -> {
                                    // Wrong PIN
                                    errorMessage = "Incorrect PIN"
                                    pin = ""
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pin.isNotEmpty() && !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Unlock")
                    }
                }
            }
        }
    }
}

@Composable
fun FakeCrashScreen() {
    // Android-style crash screen
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Unfortunately, PurplePad has stopped.",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(32.dp))
            
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Cleaning up...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
