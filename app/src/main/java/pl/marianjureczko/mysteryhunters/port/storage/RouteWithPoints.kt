package pl.marianjureczko.mysteryhunters.port.storage

import androidx.room.Embedded
import androidx.room.Relation

data class RouteWithPoints(
    @Embedded val route: RouteEntity,
    @Relation(parentColumn = "id", entityColumn = "routeId")
    val points: List<PointOfInterestEntity>
)
