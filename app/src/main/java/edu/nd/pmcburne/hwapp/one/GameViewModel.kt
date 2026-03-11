package edu.nd.pmcburne.hwapp.one

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.room.Room

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "game_database").build()
    private val dao = db.gameDao()
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _isMen = MutableStateFlow(true)
    val isMen: StateFlow<Boolean> = _isMen.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val games: StateFlow<List<GameEntity>> =
        combine(_selectedDate, _isMen) { date, isMen ->
            Pair(dateToString(date), if (isMen) "men" else "women")
        }.flatMapLatest { (dateStr, gender) ->
            dao.getGames(dateStr, gender)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadGames()
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        loadGames()
    }

    fun toggleGender() {
        _isMen.value = !_isMen.value
        loadGames()
    }

    fun refresh() {
        loadGames()
    }

    private fun loadGames() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val date = _selectedDate.value
                val gender = if (_isMen.value) "men" else "women"
                val year = date.format(DateTimeFormatter.ofPattern("yyyy"))
                val month = date.format(DateTimeFormatter.ofPattern("MM"))
                val day = date.format(DateTimeFormatter.ofPattern("dd"))
                val response = GameAPI.retrofitService.getScores(gender, year, month, day)
                val dateStr = dateToString(date)

                val games = response.games.map { game ->
                    val game = game.game
                    GameEntity(
                        id = game.gameID,
                        date = dateStr,
                        gender = gender,
                        homeTeam = game.home.names.short,
                        awayTeam = game.away.names.short,
                        homeScore = game.home.score ?: "",
                        awayScore = game.away.score ?: "",
                        gameState = game.gameState,
                        startTime = game.startTime,
                        contestClock = game.contestClock,
                        currentPeriod = game.currentPeriod,
                        finalMessage = game.finalMessage,
                        homeWinner = game.home.winner,
                        awayWinner = game.away.winner
                    )
                }
                dao.insertGames(games)
            } catch (e: Exception) {
                _errorMessage.value = "Server is offline. Showing downloaded data."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun dateToString(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
}