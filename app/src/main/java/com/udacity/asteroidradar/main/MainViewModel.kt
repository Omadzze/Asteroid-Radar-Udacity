package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repositroy.AsteroidRepository
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    private val _navigateToSelectedProperty = MutableLiveData<Asteroid>()

    private val _imageOfDay = MutableLiveData<PictureOfDay>()
    val imageOfDay: LiveData<PictureOfDay> get() = _imageOfDay

    val navigateToSelectedProperty: LiveData<Asteroid>
        get() = _navigateToSelectedProperty

    init {
        viewModelScope.launch {
            asteroidRepository.refreshAsteroids()
            _imageOfDay.value = asteroidRepository.getAsteroidImageOfTheDay()
        }
    }

    fun displayPropertyDetails(asteroidProperty: Asteroid) {
        _navigateToSelectedProperty.value = asteroidProperty
    }

    fun displayPropertyDetailsComplete() {
        _navigateToSelectedProperty.value = null
    }

    val asteroidList = asteroidRepository.asteroids

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
}