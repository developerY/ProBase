package com.zoewave.probase.gotmind.features.memblox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.gotmind.database.MemBloxScoreEntity
import com.zoewave.probase.gotmind.database.dao.MemBloxScoreDao
import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MemBloxState(
    val grid: List<MemBloxBlock> = emptyList(),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val flippedBlocks: List<MemBloxBlock> = emptyList()
)

@HiltViewModel
class MemBloxViewModel @Inject constructor(
    private val scoreDao: MemBloxScoreDao
) : ViewModel() {

    private val cols = 12
    private val rows = 20
    private val emojis = listOf("🍎", "🍌", "🍇", "🍊", "🍓", "🍒", "🍍", "🥝", "🍉", "🍑")

    private val _uiState = MutableStateFlow(MemBloxState())
    val uiState: StateFlow<MemBloxState> = _uiState.asStateFlow()

    val topScores = scoreDao.getTopScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var gameJob: Job? = null

    init {
        startGame()
    }

    fun startGame() {
        _uiState.value = MemBloxState()
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (!_uiState.value.isGameOver) {
                delay(1000)
                spawnBlock()
                applyGravity()
            }
        }
    }

    private fun spawnBlock() {
        val col = (0 until cols).random()
        // Check if top row is full
        if (_uiState.value.grid.any { it.row == 0 && it.col == col }) {
            _uiState.update { it.copy(isGameOver = true) }
            saveScore()
            return
        }

        // Generate random pastel coral color
        // Coral is roughly FF7F50. Pastel coral is lighter.
        // R: 255, G: 150-200, B: 120-170
        val r = 255
        val g = (160..210).random()
        val b = (140..190).random()
        val color = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

        val newBlock = MemBloxBlock(
            id = UUID.randomUUID().toString(),
            emoji = emojis.random(),
            row = 0,
            col = col,
            color = color
        )
        _uiState.update { it.copy(grid = it.grid + newBlock) }
    }

    private fun applyGravity() {
        _uiState.update { state ->
            val newGrid = state.grid.toMutableList()
            var changed = true
            while (changed) {
                changed = false
                for (i in newGrid.indices) {
                    val block = newGrid[i]
                    if (block.row < rows - 1 && !newGrid.any { it.row == block.row + 1 && it.col == block.col }) {
                        newGrid[i] = block.copy(row = block.row + 1)
                        changed = true
                    }
                }
            }
            state.copy(grid = newGrid)
        }
    }

    fun onBlockClick(block: MemBloxBlock) {
        if (block.isMatched || block.isFlipped || _uiState.value.flippedBlocks.size >= 2) return

        _uiState.update { state ->
            val newGrid = state.grid.map { if (it.id == block.id) it.copy(isFlipped = true) else it }
            val newFlipped = state.flippedBlocks + block.copy(isFlipped = true)
            state.copy(grid = newGrid, flippedBlocks = newFlipped)
        }

        if (_uiState.value.flippedBlocks.size == 2) {
            viewModelScope.launch {
                delay(500)
                checkMatch()
            }
        }
    }

    private fun checkMatch() {
        _uiState.update { state ->
            val flipped = state.flippedBlocks
            if (flipped[0].emoji == flipped[1].emoji) {
                // Match!
                val matchedIds = flipped.map { it.id }.toSet()
                val newGrid = state.grid.filterNot { it.id in matchedIds }
                state.copy(grid = newGrid, flippedBlocks = emptyList(), score = state.score + 10).also {
                    viewModelScope.launch { applyGravity() }
                }
            } else {
                // No match
                val newGrid = state.grid.map { it.copy(isFlipped = false) }
                state.copy(grid = newGrid, flippedBlocks = emptyList())
            }
        }
    }

    private fun saveScore() {
        viewModelScope.launch {
            scoreDao.insertScore(MemBloxScoreEntity(score = _uiState.value.score))
        }
    }
}
