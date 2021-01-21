package com.udacity.asteroidradar.repositroy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.NasaService
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.Network.retrofitService
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.DatabaseAsteroid
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception

class AsteroidRepository(private val database: AsteroidDatabase) {


    var asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroids()) {
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
                    Log.i("TAG", databaseAsteroid.toString())
                }
                database.asteroidDao.insertAll(listDbAsteroids.toList())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}