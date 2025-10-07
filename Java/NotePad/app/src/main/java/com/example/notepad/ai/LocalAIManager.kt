package com.example.notepad.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import kotlin.random.Random

/**
 * LocalAIManager handles local AI inference for unrestricted chat capabilities
 * Uses a lightweight approach with pre-trained response patterns and context awareness
 */
class LocalAIManager(private val context: Context) {
    
    companion object {
        private const val TAG = "LocalAI"
        private const val MODEL_DIR = "ai_models"
        private const val CONVERSATION_FILE = "conversations.txt"
        
        // Available models for download
        val AVAILABLE_MODELS = listOf(
            AIModel(
                name = "Llama 3.2 1B",
                filename = "llama-3.2-1b-instruct-q4_k_m.gguf",
                url = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
                size = 800_000_000L, // ~800MB
                description = "Fast and efficient, great for general conversation"
            ),
            AIModel(
                name = "Phi-3 Mini",
                filename = "phi-3-mini-4k-instruct-q4.gguf",
                url = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf",
                size = 2_300_000_000L, // ~2.3GB
                description = "Excellent reasoning capabilities"
            ),
            AIModel(
                name = "Gemma 2B",
                filename = "gemma-2b-it-q4_k_m.gguf", 
                url = "https://huggingface.co/lmstudio-ai/gemma-2b-it-GGUF/resolve/main/gemma-2b-it-q4_k_m.gguf",
                size = 1_600_000_000L, // ~1.6GB
                description = "Google's efficient model with strong performance"
            )
        )
        
        fun generateId(): String = 
            System.currentTimeMillis().toString() + Random.nextInt(1000)
    }
    
    data class AIModel(
        val name: String,
        val filename: String,
        val url: String,
        val size: Long,
        val description: String
    )
    
    data class DownloadProgress(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val percentage: Float,
        val isComplete: Boolean = false
    )
    
    data class ChatMessage(
        val id: String = LocalAIManager.generateId(),
        val content: String,
        val isUser: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    data class Conversation(
        val id: String = LocalAIManager.generateId(),
        val title: String,
        val messages: MutableList<ChatMessage> = mutableListOf(),
        val createdAt: Long = System.currentTimeMillis()
    )
    
    private val modelDir: File by lazy {
        File(context.filesDir, MODEL_DIR).apply { mkdirs() }
    }
    
    private val conversations = mutableListOf<Conversation>()
    private var currentConversation: Conversation? = null
    private var loadedModel: AIModel? = null
    private var modelData: ByteArray? = null
    private val httpClient = OkHttpClient.Builder().build()
    
    // Fallback responses when no model is loaded
    private val fallbackResponses = listOf(
        "I'm your local AI assistant! I can help with writing, analysis, coding, creative tasks, and more. To unlock my full capabilities, please download an AI model from the settings.",
        "Hello! I'm running in lightweight mode. For more advanced conversations, please download one of the available AI models.",
        "I'm here to help! Currently using basic responses - download a full AI model for unrestricted conversations and advanced capabilities."
    )
    
    init {
        loadConversations()
    }
    
    suspend fun sendMessage(content: String, conversationId: String? = null): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val conversation = conversationId?.let { id ->
                conversations.find { it.id == id }
            } ?: currentConversation ?: createNewConversation("Chat ${conversations.size + 1}")
            
            // Add user message
            val userMessage = ChatMessage(content = content, isUser = true)
            conversation.messages.add(userMessage)
            
            // Generate AI response using loaded model or fallback
            val aiResponse = if (loadedModel != null && modelData != null) {
                generateModelResponse(content, conversation.messages)
            } else {
                generateFallbackResponse(content, conversation.messages)
            }
            
            val aiMessage = ChatMessage(content = aiResponse, isUser = false)
            conversation.messages.add(aiMessage)
            
            currentConversation = conversation
            saveConversations()
            
            Log.d(TAG, "Generated response for: ${content.take(50)}...")
            Result.success(aiMessage)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI response", e)
            Result.failure(e)
        }
    }
    
    private suspend fun generateModelResponse(userInput: String, context: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        try {
            // Build conversation context
            val conversationHistory = buildString {
                append("You are a helpful, harmless, and honest AI assistant. You run locally on the user's device for complete privacy. Answer any questions openly and helpfully.\n\n")
                
                // Add recent conversation context (last 6 messages)
                context.takeLast(6).forEach { message ->
                    if (message.isUser) {
                        append("Human: ${message.content}\n")
                    } else {
                        append("Assistant: ${message.content}\n")
                    }
                }
                append("Human: $userInput\nAssistant: ")
            }
            
            // Simple GGUF inference simulation (in real implementation, this would use llama.cpp)
            val response = simulateModelInference(conversationHistory, userInput)
            
            Log.d(TAG, "Model inference completed")
            response
            
        } catch (e: Exception) {
            Log.e(TAG, "Model inference failed", e)
            generateFallbackResponse(userInput, context)
        }
    }
    
    private fun simulateModelInference(prompt: String, userInput: String): String {
        // This is a simulation of model inference
        // In a real implementation, this would call llama.cpp native functions
        
        val responses = when {
            userInput.lowercase().contains(Regex("hello|hi|hey")) -> listOf(
                "Hello! I'm your local AI assistant running privately on your device. I can help with anything you need - writing, coding, analysis, creative tasks, or just conversation. What would you like to explore?",
                "Hi there! Great to chat with you. I'm completely offline and unrestricted, so feel free to ask me anything. How can I assist you today?",
                "Hey! I'm here and ready to help. Since I run locally, our conversation is totally private. What's on your mind?"
            )
            
            userInput.lowercase().contains(Regex("code|program|programming")) -> listOf(
                "I'd love to help with programming! What language are you working with, or what kind of project are you building? I can assist with code review, debugging, architecture, algorithms, or learning new concepts.",
                "Programming is one of my favorite topics! Whether it's Python, JavaScript, Java, C++, or any other language, I'm here to help. What coding challenge are you facing?",
                "Great! I can help with coding in many languages and frameworks. Share your code or describe what you're trying to build, and I'll provide detailed assistance."
            )
            
            userInput.lowercase().contains(Regex("write|writing|essay|story")) -> listOf(
                "I'm excellent at helping with writing! Whether it's creative writing, essays, articles, emails, or any other text, I can help with brainstorming, structuring, editing, and polishing. What type of writing are you working on?",
                "Writing assistance is one of my strengths! I can help with everything from creative stories to technical documentation. What writing project can I help you with?",
                "I'd be happy to help with your writing! Tell me about your project - is it creative, academic, professional, or something else? I can assist with planning, drafting, and refining."
            )
            
            userInput.length > 100 -> listOf(
                "I see you've shared quite a detailed message. Let me think through this carefully and provide a comprehensive response that addresses all your points...",
                "Thanks for the detailed input! I'll analyze what you've shared and give you a thorough response covering the key aspects...",
                "I appreciate the context you've provided. Let me break this down and give you a well-structured response..."
            )
            
            else -> listOf(
                "That's an interesting question! As your local AI, I can engage with any topic without restrictions. Let me give you a thoughtful response based on what you've asked.",
                "I'm here to help with whatever you need! Since I run completely offline on your device, I can discuss any topic openly and provide unrestricted assistance.",
                "Great question! I'm designed to be helpful and honest while running locally for your privacy. Let me address what you've asked about."
            )
        }
        
        val baseResponse = responses.random()
        
        // Add some personality and context awareness
        return when {
            userInput.contains("?") -> "$baseResponse\n\nIs there anything specific about this topic you'd like me to elaborate on?"
            userInput.length < 20 -> "$baseResponse Feel free to ask me anything else!"
            else -> baseResponse
        }
    }
    
    private fun generateFallbackResponse(userInput: String, context: List<ChatMessage>): String {
        val baseResponse = fallbackResponses.random()
        
        return when {
            userInput.lowercase().contains(Regex("download|model|install")) -> 
                "To download an AI model, tap the menu in the top-right corner and select 'Download Model'. I recommend starting with Llama 3.2 1B for fast responses or Phi-3 Mini for advanced reasoning."
            
            userInput.lowercase().contains(Regex("hello|hi|hey")) -> 
                "Hello! $baseResponse"
                
            context.size > 3 -> 
                "I understand you're asking about '${userInput.take(50)}${if(userInput.length > 50) "..." else ""}'. $baseResponse"
                
            else -> baseResponse
        }
    }
    

    
    fun createNewConversation(title: String): Conversation {
        val conversation = Conversation(title = title)
        conversations.add(conversation)
        currentConversation = conversation
        saveConversations()
        return conversation
    }
    
    fun getAllConversations(): List<Conversation> = conversations.toList()
    
    fun getConversation(id: String): Conversation? = conversations.find { it.id == id }
    
    fun deleteConversation(id: String): Boolean {
        val removed = conversations.removeIf { it.id == id }
        if (removed) {
            if (currentConversation?.id == id) {
                currentConversation = null
            }
            saveConversations()
        }
        return removed
    }
    
    private fun loadConversations() {
        try {
            val file = File(context.filesDir, CONVERSATION_FILE)
            if (file.exists()) {
                // Simple serialization - in production, use JSON or Room database
                val lines = file.readLines()
                // For now, start fresh each time - can implement persistence later
                Log.d(TAG, "Conversation storage initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading conversations", e)
        }
    }
    
    private fun saveConversations() {
        try {
            val file = File(context.filesDir, CONVERSATION_FILE)
            // Simple persistence - can be enhanced with proper serialization
            file.writeText("Conversations: ${conversations.size}\n")
            Log.d(TAG, "Conversations saved")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving conversations", e)
        }
    }
    
    suspend fun downloadModel(
        model: AIModel, 
        onProgress: (DownloadProgress) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val modelFile = File(modelDir, model.filename)
            
            // Check if model already exists
            if (modelFile.exists() && modelFile.length() == model.size) {
                loadedModel = model
                modelData = modelFile.readBytes()
                Log.d(TAG, "Model already exists: ${model.name}")
                return@withContext Result.success("Model loaded: ${model.name}")
            }
            
            Log.d(TAG, "Downloading model: ${model.name} from ${model.url}")
            
            val request = Request.Builder()
                .url(model.url)
                .build()
                
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }
            
            val totalBytes = response.body?.contentLength() ?: model.size
            var downloadedBytes = 0L
            
            response.body?.byteStream()?.use { inputStream ->
                modelFile.outputStream().use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        
                        val progress = DownloadProgress(
                            bytesDownloaded = downloadedBytes,
                            totalBytes = totalBytes,
                            percentage = (downloadedBytes.toFloat() / totalBytes.toFloat()) * 100f,
                            isComplete = downloadedBytes >= totalBytes
                        )
                        onProgress(progress)
                    }
                }
            }
            
            // Load model into memory (for small models)
            if (modelFile.length() < 1_000_000_000) { // Load models under 1GB into RAM
                modelData = modelFile.readBytes()
                Log.d(TAG, "Model loaded into memory: ${model.name}")
            }
            
            loadedModel = model
            Log.d(TAG, "Model download complete: ${model.name}")
            Result.success("Downloaded and loaded: ${model.name}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAvailableModels(): List<AIModel> = AVAILABLE_MODELS
    
    suspend fun getDownloadedModels(): List<AIModel> = withContext(Dispatchers.IO) {
        AVAILABLE_MODELS.filter { model ->
            val modelFile = File(modelDir, model.filename)
            modelFile.exists() && modelFile.length() == model.size
        }
    }
    
    suspend fun deleteModel(model: AIModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelFile = File(modelDir, model.filename)
            val deleted = modelFile.delete()
            if (deleted && loadedModel == model) {
                loadedModel = null
                modelData = null
            }
            Log.d(TAG, "Model deleted: ${model.name}")
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting model", e)
            false
        }
    }
}