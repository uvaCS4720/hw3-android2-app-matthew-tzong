package edu.nd.pmcburne.hwapp.one

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val date: String,
    val gender: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: String,
    val awayScore: String,
    val gameState: String,
    val startTime: String,
    val contestClock: String,
    val currentPeriod: String,
    val finalMessage: String,
    val homeWinner: Boolean,
    val awayWinner: Boolean
)

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE date = :date AND gender = :gender")
    fun getGames(date: String, gender: String): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)
}

@Database(entities = [GameEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}