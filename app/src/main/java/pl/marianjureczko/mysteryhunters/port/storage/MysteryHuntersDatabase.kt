package pl.marianjureczko.mysteryhunters.port.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RouteEntity::class, PointOfInterestEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MysteryHuntersDatabase : RoomDatabase() {

    abstract fun routeDao(): RouteDao

    companion object {
        const val NAME = "mystery-hunters.db"
    }
}
