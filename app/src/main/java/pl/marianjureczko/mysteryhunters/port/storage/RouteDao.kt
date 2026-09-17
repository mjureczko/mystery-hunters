/*
 * Copyright (C) 2026 Marian Jureczko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

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
