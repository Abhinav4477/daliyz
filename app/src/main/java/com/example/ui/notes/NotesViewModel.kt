package com.example.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.DailyzApplication
import com.example.data.local.NoteTodoItem
import com.example.data.local.NoteWithTodos
import com.example.util.CsvCalculator
import com.example.util.CsvSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel : ViewModel() {

    private val notesRepository = DailyzApplication.instance.notesRepository

    val notesList: StateFlow<List<NoteWithTodos>> = notesRepository.allNotesWithTodos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _liveCsvSummary = MutableStateFlow(CsvSummary())
    val liveCsvSummary: StateFlow<CsvSummary> = _liveCsvSummary.asStateFlow()

    fun updateLiveCsvCalculation(text: String) {
        _liveCsvSummary.value = CsvCalculator.calculate(text)
    }

    fun saveNote(
        id: Long?,
        title: String,
        content: String,
        csvNumbers: String,
        todoTexts: List<String>
    ) {
        viewModelScope.launch {
            if (id == null || id == 0L) {
                notesRepository.createNote(
                    title = title,
                    content = content,
                    csvNumbers = csvNumbers,
                    todoTexts = todoTexts
                )
            } else {
                notesRepository.updateNote(
                    id = id,
                    title = title,
                    content = content,
                    csvNumbers = csvNumbers
                )
                // Add any new todos entered
                todoTexts.forEach { text ->
                    if (text.isNotBlank()) {
                        notesRepository.addTodoItem(id, text)
                    }
                }
            }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            notesRepository.deleteNote(id)
        }
    }

    fun toggleTodo(todo: NoteTodoItem) {
        viewModelScope.launch {
            notesRepository.toggleTodoItem(todo)
        }
    }

    fun addTodoToExistingNote(noteId: Long, text: String) {
        viewModelScope.launch {
            notesRepository.addTodoItem(noteId, text)
        }
    }

    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            notesRepository.deleteTodoItem(id)
        }
    }
}
