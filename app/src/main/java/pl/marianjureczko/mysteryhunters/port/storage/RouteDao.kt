package pl.marianjureczko.mysteryhunters.port.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Transaction
    @Query("SELECT * FROM routes ORDER BY name COLLATE NOCASE ASC")
    fun observeRoutes(): Flow<List<RouteWithPoints>>

    @Transaction
    @Query("SELECT * FROM routes WHERE id = :routeId")
    suspend fun findById(routeId: Long): RouteWithPoints?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRoute(route: RouteEntity): Long

    @Update
    suspend fun updateRoute(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: List<PointOfInterestEntity>)

    @Query("DELETE FROM points_of_interest WHERE routeId = :routeId")
    suspend fun deletePointsOfRoute(routeId: Long)

    @Query("DELETE FROM routes WHERE id = :routeId")
    suspend fun deleteRoute(routeId: Long)

    /** Replaces the whole route, which keeps the stored points in sync with the edited ones. */
    @Transaction
    suspend fun upsert(route: RouteEntity, points: List<PointOfInterestEntity>): Long {
        val routeId = if (route.id == 0L) {
            insertRoute(route)
        } else {
            updateRoute(route)
            route.id
        }
        deletePointsOfRoute(routeId)
        insertPoints(points.map { it.copy(routeId = routeId) })
        return routeId
    }
}
