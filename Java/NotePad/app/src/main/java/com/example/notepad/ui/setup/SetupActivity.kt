package com.example.notepad.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.notepad.security.SecurityManager
import kotlinx.coroutines.launch

class SetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                SetupScreen(
                    onComplete = { 
                        // Navigate to MainActivity after setup
                        startActivity(Intent(this, com.example.notepad.MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onComplete: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    var useLock by remember { mutableStateOf<Boolean?>(null) }
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var distressCode by remember { mutableStateOf("") }
    var distressConfirm by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PurplePad Setup") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A34D9),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (step) {
                0 -> {
                    // Welcome screen
                    Text(
                        "Welcome to PurplePad",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A34D9)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Ultra-secure notes with privacy features",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(32.dp))
                    Text(
                        "Would you like to set up a PIN for app security?",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = { useLock = true; step = 1 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Yes - Enable PIN Lock")
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = { 
                            SecurityManager.completeSetup(context, null, null)
                            onComplete()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("No - Skip Security")
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Note: Without a PIN, locked notes and vault features will be disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                1 -> {
                    // Set PIN
                    Text(
                        "Set Your PIN",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This PIN will protect your locked notes and vault",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it; errorMessage = "" },
                        label = { Text("Enter PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = pinConfirm,
                        onValueChange = { pinConfirm = it; errorMessage = "" },
                        label = { Text("Confirm PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    if (errorMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            when {
                                pin.length < 4 -> errorMessage = "PIN must be at least 4 characters"
                                pin != pinConfirm -> errorMessage = "PINs do not match"
                                else -> { step = 2; errorMessage = "" }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue")
                    }
                }
                
                2 -> {
                    // Set distress code
                    Text(
                        "Set Distress Code",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "⚠️ IMPORTANT: If this code is entered anywhere in the app, it will trigger immediate data destruction",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Use this in emergency situations to quickly destroy all data",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = distressCode,
                        onValueChange = { distressCode = it; errorMessage = "" },
                        label = { Text("Enter Distress Code") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = distressConfirm,
                        onValueChange = { distressConfirm = it; errorMessage = "" },
                        label = { Text("Confirm Distress Code") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    if (errorMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            when {
                                distressCode.length < 4 -> errorMessage = "Distress code must be at least 4 characters"
                                distressCode != distressConfirm -> errorMessage = "Codes do not match"
                                distressCode == pin -> errorMessage = "Distress code must be different from PIN"
                                else -> {
                                    SecurityManager.completeSetup(context, pin, distressCode)
                                    onComplete()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6A34D9)
                        )
                    ) {
                        Text("Complete Setup")
                    }
                }
            }
        }
    }
}
