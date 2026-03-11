package edu.nd.pmcburne.hwapp.one

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL = "https://ncaa-api.henrygd.me/scoreboard/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface GameAPIService {
    @GET("basketball-{gender}/d1/{yyyy}/{mm}/{dd}")
    suspend fun getScores(
        @Path("gender") gender: String,
        @Path("yyyy") year: String,
        @Path("mm") month: String,
        @Path("dd") day: String
    ): Games
}

object GameAPI {
    val retrofitService: GameAPIService by lazy {
        retrofit.create(GameAPIService::class.java)
    }
}

data class Games(
    @SerializedName("games") val games: List<Game>
)

data class Game(
    @SerializedName("game") val game: GameData
)

data class GameData(
    @SerializedName("gameID") val gameID: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("gameState") val gameState: String,
    @SerializedName("currentPeriod") val currentPeriod: String,
    @SerializedName("contestClock") val contestClock: String,
    @SerializedName("finalMessage") val finalMessage: String,
    @SerializedName("home") val home: Team,
    @SerializedName("away") val away: Team
)

data class Team(
    @SerializedName("names") val names: TeamNames,
    @SerializedName("score") val score: String?,
    @SerializedName("winner") val winner: Boolean,
)

data class TeamNames(
    @SerializedName("short") val short: String,
    @SerializedName("char6") val char6: String
)
