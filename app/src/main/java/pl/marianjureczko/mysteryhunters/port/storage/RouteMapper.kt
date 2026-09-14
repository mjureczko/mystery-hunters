package pl.marianjureczko.mysteryhunters.port.storage

import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.Route

fun RouteWithPoints.toModel(): Route = Route(
    id = route.id,
    name = route.name,
    pointsOfInterest = points.sortedBy { it.pointId }.map { it.toModel() },
    lastSelectedPointId = route.lastSelectedPointId
)

fun PointOfInterestEntity.toModel(): PointOfInterest = PointOfInterest(
    id = pointId,
    latitude = latitude,
    longitude = longitude,
    description = description,
    caught = caught
)

fun Route.toEntity(): RouteEntity = RouteEntity(
    id = id,
    name = name,
    lastSelectedPointId = lastSelectedPointId
)

fun PointOfInterest.toEntity(routeId: Long): PointOfInterestEntity = PointOfInterestEntity(
    routeId = routeId,
    pointId = id,
    latitude = latitude,
    longitude = longitude,
    description = description,
    caught = caught
)
