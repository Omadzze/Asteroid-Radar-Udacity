package com.udacity.asteroidradar.repositroy

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaService
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.Network.retrofitService
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.DatabaseAsteroid
import com.udacity.asteroidradar.database.DatabasePictureOfDay
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception
import java.time.LocalDate

class AsteroidRepository(private val database: AsteroidDatabase) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _startDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _endDate = _startDate.plusDays(7)


    var asteroids: LiveData<List<Asteroid>> =
            Transformations.map(database.asteroidDao.getAsteroids()) {
                it.asDomainModel()
            }

    @RequiresApi(Build.VERSION_CODES.O)
    val todayAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getTodayAsteroids(_startDate.toString())) {
        it.asDomainModel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val weeklyAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getWeeklyAsteroids(_startDate.toString(), _endDate.toString())) {
        it.asDomainModel()
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val jsonResult = Network.retrofitService.getAsteroidList()
                val asteroids = parseAsteroidsJsonResult(JSONObject(jsonResult))
                val listDbAsteroids = mutableListOf<DatabaseAsteroid>()
                print(asteroids)

                for (asteroid in asteroids) {
                    val databaseAsteroid = DatabaseAsteroid(
                            asteroid.id,
                            asteroid.codename,
                            asteroid.closeApproachDate,
                            asteroid.absoluteMagnitude,
                            asteroid.estimatedDiameter,
                            asteroid.relativeVelocity,
                            asteroid.distanceFromEarth,
                            asteroid.isPotentiallyHazardous
                    )

                    listDbAsteroids.add(databaseAsteroid)
                }
                database.asteroidDao.insertAll(listDbAsteroids.toList())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshPictureOfTheDay() {
        withContext(Dispatchers.IO) {
            val pictureOfDay = Network.retrofitService.getImageOfTheDay(LocalDate.now())
            val dbPictureOfDay = DatabasePictureOfDay(pictureOfDay.title, pictureOfDay.url)
            database.asteroidDao.insertPictureOfTheDay(dbPictureOfDay)
        }
    }

    @WorkerThread
    suspend fun getAsteroidImageOfTheDay(): PictureOfDay {
        val databasePictureOfDay = database.asteroidDao.getPictureOfTheDay()
        return PictureOfDay(databasePictureOfDay.title, databasePictureOfDay.url)
    }
}