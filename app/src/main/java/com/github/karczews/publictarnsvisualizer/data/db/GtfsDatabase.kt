package com.github.karczews.publictarnsvisualizer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.karczews.publictarnsvisualizer.data.db.dao.GtfsMetadataDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.RouteDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.StopDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.StopTimeDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.TripDao
import com.github.karczews.publictarnsvisualizer.data.db.entity.GtfsMetadataEntity
import com.github.karczews.publictarnsvisualizer.data.db.entity.RouteEntity
import com.github.karczews.publictarnsvisualizer.data.db.entity.StopEntity
import com.github.karczews.publictarnsvisualizer.data.db.entity.StopTimeEntity
import com.github.karczews.publictarnsvisualizer.data.db.entity.TripEntity

@Database(
    entities = [
        RouteEntity::class,
        StopEntity::class,
        TripEntity::class,
        StopTimeEntity::class,
        GtfsMetadataEntity::class,
    ],
    version = 1,
)
abstract class GtfsDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun stopDao(): StopDao
    abstract fun tripDao(): TripDao
    abstract fun stopTimeDao(): StopTimeDao
    abstract fun metadataDao(): GtfsMetadataDao
}
