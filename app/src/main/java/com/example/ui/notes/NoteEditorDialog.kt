package com.example.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.NoteTodoItem
import com.example.data.local.NoteWithTodos
import com.example.ui.theme.ChartBarGoalMet
import com.example.util.CsvCalculator
import com.example.util.CsvSummary

@Composable
fun NoteEditorDialog(
    noteWithTodos: NoteWithTodos?,
    onDismiss: () -> Unit,
    onSave: (id: Long?, title: String, content: String, csvNumbers: String, todos: List<String>) -> Unit,
    onToggleTodo: ((NoteTodoItem) -> Unit)? = null,
    onDeleteTodo: ((Long) -> Unit)? = null
) {
    var title by remember { mutableStateOf(noteWithTodos?.note?.title ?: "") }
    var content by remember { mutableStateOf(noteWithTodos?.note?.content ?: "") }
    var csvNumbers by remember { mutableStateOf(noteWithTodos?.note?.csvNumbers ?: "") }

    var newTodoText by remember { mutableStateOf("") }
    val newTodoList = remember { mutableStateListOf<String>() }

    // Live calculation of CSV numbers
    var csvSummary by remember { mutableStateOf(CsvCalculator.calculate(csvNumbers)) }

    LaunchedEffect(csvNumbers) {
        csvSummary = CsvCalculator.calculate(csvNumbers)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("note_editor_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_note_editor")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (noteWithTodos == null) "New Note" else "Edit Note",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = {
                            if (title.isBlank()) title = "Untitled Note"
                            onSave(
                                noteWithTodos?.note?.id,
                                title,
                                content,
                                csvNumbers,
                                newTodoList
                            )
                        },
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Text("Save")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title TextField
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Note Title") },
                            placeholder = { Text("e.g. Project Sprint Budget & Tasks") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_title_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Content / Body TextField
                    item {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Note Details (Optional)") },
                            placeholder = { Text("Write your general notes, thoughts, or meeting recap...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("note_content_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // CSV Numbers Section Header
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "CSV Numbers (Auto-Summed)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // CSV Numbers Input Area
                    item {
                        Column {
                            OutlinedTextField(
                                value = csvNumbers,
                                onValueChange = { csvNumbers = it },
                                label = { Text("Enter CSV or line-separated numbers") },
                                placeholder = { Text("e.g.\n45.50, 120, 35\n80, 15.25") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .testTag("note_csv_input"),
                                shape = RoundedCornerShape(14.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Live Summary Box at the bottom of the CSV section!
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("csv_live_summary_box")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "TOTAL SUM",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = csvSummary.formattedSum,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(30.dp)
                                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    )

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "AVERAGE",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = csvSummary.formattedAvg,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(30.dp)
                                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    )

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "COUNT",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${csvSummary.count}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // To-Do List Section Header (Optional)
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = ChartBarGoalMet,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "To-Do Checklist (Optional)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Existing Saved To-Dos (if editing)
                    if (noteWithTodos != null && noteWithTodos.todos.isNotEmpty()) {
                        itemsIndexed(noteWithTodos.todos) { _, todo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = todo.isCompleted,
                                    onCheckedChange = { onToggleTodo?.invoke(todo) }
                                )
                                Text(
                                    text = todo.text,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (todo.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = { onDeleteTodo?.invoke(todo.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete task",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // New items currently being added
                    itemsIndexed(newTodoList) { index, itemText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = false, onCheckedChange = {})
                            Text(
                                text = itemText,
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { newTodoList.removeAt(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove task",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Add To-Do Item Input Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newTodoText,
                                onValueChange = { newTodoText = it },
                                placeholder = { Text("Add a task...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_todo_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (newTodoText.isNotBlank()) {
                                        newTodoList.add(newTodoText.trim())
                                        newTodoText = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("add_todo_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }
                    }
                }
            }
        }
    }
}
