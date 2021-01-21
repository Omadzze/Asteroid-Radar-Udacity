package com.udacity.asteroidradar.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.udacity.asteroidradar.Asteroid

@Entity
data class DatabaseAsteroid constructor(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name = "codename")
    var codename: String,
    @ColumnInfo(name = "close_approach_date")
    var closeApproachDate: String,
    @ColumnInfo(name = "absolut_magnitude")
    val absoluteMagnitude: Double,
    @ColumnInfo(name = "estimated_diameter")
    val estimatedDiameter: Double,
    @ColumnInfo(name = "relative_velocity")
    val relativeVelocity: Double,
    @ColumnInfo(name = "distance_from_earth")
    val distanceFromEarth: Double,
    @ColumnInfo(name = "is_asteroid_hazardous")
    val isPotentiallyHazardous: Boolean)

// Converting database objects to domain objects (Model)
fun List<DatabaseAsteroid>.asDomainModel(): List<Asteroid> {
    return map {
        Asteroid(
            id = it.id,
            codename = it.codename,
            closeApproachDate = it.closeApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous)
    }
}

@Entity(tableName = "picture_of_day")
data class DatabasePictureOfDay constructor(
    @PrimaryKey
    val url: String,
    @ColumnInfo(name = "title")
    val title: String)