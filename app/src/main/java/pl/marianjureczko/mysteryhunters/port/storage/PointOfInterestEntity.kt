package pl.marianjureczko.mysteryhunters.port.storage

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * The point id is unique only within its route, hence the composite primary key.
 */
@Entity(
    tableName = "points_of_interest",
    primaryKeys = ["routeId", "pointId"],
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routeId")]
)
data class PointOfInterestEntity(
    val routeId: Long,
    val pointId: Int,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val caught: Boolean
)
