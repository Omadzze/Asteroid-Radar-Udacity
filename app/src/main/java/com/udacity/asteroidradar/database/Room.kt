package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("SELECT * FROM databaseasteroid WHERE close_approach_date = :date ORDER BY close_approach_date DESC")
    fun getTodayAsteroids(date: String): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * FROM databaseasteroid WHERE close_approach_date BETWEEN :startDate AND :endDate")
    fun getWeeklyAsteroids(startDate: String, endDate: String): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * FROM databaseasteroid")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * FROM picture_of_day")
    suspend fun getPictureOfTheDay(): DatabasePictureOfDay

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(asteroids: List<DatabaseAsteroid>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPictureOfTheDay(vararg pictureOfDay: DatabasePictureOfDay)

}

@Database(entities = [DatabaseAsteroid::class, DatabasePictureOfDay::class], version = 1)
abstract class AsteroidDatabase: RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    synchronized(AsteroidDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AsteroidDatabase::class.java, "asteroid_database").build()
        }
        return INSTANCE
    }
}