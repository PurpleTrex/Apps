package com.hades.sshserver.ui.viewmodel

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hades.sshserver.data.FileItem
import com.hades.sshserver.data.NavigationLocation
import com.hades.sshserver.data.SortOrder
import com.hades.sshserver.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class FileBrowserViewModel(context: Context) : ViewModel() {

    private val fileRepository = FileRepository(context)

    private val _currentPath = MutableStateFlow(Environment.getExternalStorageDirectory().absolutePath)
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _fileList = MutableStateFlow<List<FileItem>>(emptyList())
    val fileList: StateFlow<List<FileItem>> = _fileList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedFiles = MutableStateFlow<Set<FileItem>>(emptySet())
    val selectedFiles: StateFlow<Set<FileItem>> = _selectedFiles.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _navigationLocations = MutableStateFlow<List<NavigationLocation>>(emptyList())
    val navigationLocations: StateFlow<List<NavigationLocation>> = _navigationLocations.asStateFlow()

    init {
        loadFiles()
        _navigationLocations.value = fileRepository.getNavigationLocations()
    }

    fun loadFiles(path: String = _currentPath.value) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val files = fileRepository.getFilesInDirectory(path)
                _fileList.value = sortFiles(files)
                _currentPath.value = path
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error loading files"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun navigateUp() {
        val parent = File(_currentPath.value).parent
        if (parent != null) {
            loadFiles(parent)
        }
    }

    fun navigateTo(item: FileItem) {
        if (item.isDirectory) {
            loadFiles(item.path)
            clearSelection()
        }
    }

    fun toggleFileSelection(item: FileItem) {
        _selectedFiles.value = if (item in _selectedFiles.value) {
            _selectedFiles.value - item
        } else {
            _selectedFiles.value + item
        }
    }

    fun clearSelection() {
        _selectedFiles.value = emptySet()
    }

    fun selectAll() {
        _selectedFiles.value = _fileList.value.toSet()
    }

    fun deleteSelectedFiles() {
        viewModelScope.launch {
            _selectedFiles.value.forEach { file ->
                fileRepository.deleteFile(file)
            }
            clearSelection()
            loadFiles()
        }
    }

    fun copySelectedFiles(destinationPath: String) {
        viewModelScope.launch {
            _selectedFiles.value.forEach { file ->
                fileRepository.copyFile(file, destinationPath)
            }
            clearSelection()
            loadFiles()
        }
    }

    fun moveSelectedFiles(destinationPath: String) {
        viewModelScope.launch {
            _selectedFiles.value.forEach { file ->
                fileRepository.moveFile(file, destinationPath)
            }
            clearSelection()
            loadFiles()
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val success = fileRepository.createDirectory(_currentPath.value, name)
            if (success) {
                loadFiles()
            } else {
                _errorMessage.value = "Failed to create folder"
            }
        }
    }

    fun renameFile(file: FileItem, newName: String) {
        viewModelScope.launch {
            val success = fileRepository.renameFile(file, newName)
            if (success) {
                loadFiles()
            } else {
                _errorMessage.value = "Failed to rename file"
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _fileList.value = sortFiles(_fileList.value)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun sortFiles(files: List<FileItem>): List<FileItem> {
        return when (_sortOrder.value) {
            SortOrder.NAME_ASC -> files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            SortOrder.NAME_DESC -> files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })).reversed()
            SortOrder.DATE_ASC -> files.sortedWith(compareBy({ !it.isDirectory }, { it.lastModified }))
            SortOrder.DATE_DESC -> files.sortedWith(compareBy({ !it.isDirectory }, { -it.lastModified }))
            SortOrder.SIZE_ASC -> files.sortedWith(compareBy({ !it.isDirectory }, { it.size }))
            SortOrder.SIZE_DESC -> files.sortedWith(compareBy({ !it.isDirectory }, { -it.size }))
            SortOrder.TYPE -> files.sortedWith(compareBy({ !it.isDirectory }, { it.mimeType ?: "" }))
        }
    }

    fun canNavigateUp(): Boolean {
        val parent = File(_currentPath.value).parent
        return parent != null
    }
}
