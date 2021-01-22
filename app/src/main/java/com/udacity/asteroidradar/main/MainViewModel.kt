package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repositroy.AsteroidRepository
import kotlinx.coroutines.launch
import java.lang.Exception

@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    private val _navigateToSelectedProperty = MutableLiveData<Asteroid>()

    private val _imageOfDay = MutableLiveData<PictureOfDay>()
    val imageOfDay: LiveData<PictureOfDay> get() = _imageOfDay

    val navigateToSelectedProperty: LiveData<Asteroid>
        get() = _navigateToSelectedProperty

    enum class AsteroidFilter {
        ALL,
        WEEKLY,
        TODAY,
    }

    private val asteroidFilter = MutableLiveData(AsteroidFilter.WEEKLY)

    val asteroids = Transformations.switchMap(asteroidFilter) {
        when (it!!) {
            AsteroidFilter.WEEKLY -> asteroidRepository.weeklyAsteroids
            AsteroidFilter.TODAY -> asteroidRepository.todayAsteroids
            else -> asteroidRepository.asteroids
        }
    }


    init {
        viewModelScope.launch {
            refreshAsteroidsFromNetwork(AsteroidFilter.ALL)
            refreshPictureFromNetwork()
        }
    }

    private fun refreshAsteroidsFromNetwork(filter: AsteroidFilter) {
        viewModelScope.launch {
            try {
                asteroidRepository.refreshAsteroids()
//                asteroidRepository.todayAsteroids
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshPictureFromNetwork() {
        viewModelScope.launch {
            try {
                asteroidRepository.refreshPictureOfTheDay()
                _imageOfDay.value = asteroidRepository.getAsteroidImageOfTheDay()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun displayPropertyDetails(asteroidProperty: Asteroid) {
        _navigateToSelectedProperty.value = asteroidProperty
    }

    fun displayPropertyDetailsComplete() {
        _navigateToSelectedProperty.value = null
    }

    val asteroidList = asteroidRepository.asteroids
    val asteroidToday = asteroidRepository.todayAsteroids

    /**
     * Factory for constructing DevByteViewModel with parameter
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

    fun setAsteroidFilter(filter: AsteroidFilter) {
        asteroidFilter.postValue(filter)
    }
}