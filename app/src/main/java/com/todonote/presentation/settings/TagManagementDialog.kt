package com.todonote.presentation.settings
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todonote.domain.model.Tag
import com.todonote.domain.repository.ITagRepository
import com.todonote.presentation.common.components.TagColorPicker
import com.todonote.presentation.common.components.tagColors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class TagManagementUiState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = false
)

class TagManagementViewModel(
    private val tagRepository: ITagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagManagementUiState())
    val uiState: StateFlow<TagManagementUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            tagRepository.getAll()
                .catch { }
                .collect { tags ->
                    _uiState.update { it.copy(tags = tags, isLoading = false) }
                }
        }
    }

    fun createTag(name: String, color: Int) {
        viewModelScope.launch {
            val tag = Tag(name = name, color = color)
            tagRepository.insert(tag)
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.delete(tag)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagementDialog(
    onDismiss: () -> Unit,
    viewModel: TagManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableIntStateOf(tagColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("标签管理") },
        text = {
            Column {
                if (uiState.tags.isEmpty()) {
                    Text(
                        text = "还没有标签，点击添加创建标签",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.tags, key = { it.id }) { tag ->
                            TagManagementItem(
                                tag = tag,
                                onDelete = { viewModel.deleteTag(tag) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加标签")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新建标签") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("标签名称") },
                        singleLine = true
                    )
                    Text("选择颜色", style = MaterialTheme.typography.titleSmall)
                    TagColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            viewModel.createTag(newTagName, selectedColor)
                            newTagName = ""
                            selectedColor = tagColors.first()
                            showAddDialog = false
                        }
                    }
                ) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun TagManagementItem(
    tag: Tag,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(tag.color))
                )
                Text(tag.name)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
