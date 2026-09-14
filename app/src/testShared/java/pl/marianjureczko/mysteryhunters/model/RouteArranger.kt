package pl.marianjureczko.mysteryhunters.model

import com.ocadotechnology.gembus.test.CustomArranger
import com.ocadotechnology.gembus.test.someObjects
import com.ocadotechnology.gembus.test.someString

/**
 * Point ids are assigned sequentially from 1 within a route, so a randomly generated route has to
 * respect that or the id related rules could never be tested meaningfully.
 */
class RouteArranger : CustomArranger<Route>() {

    companion object {
        const val DEFAULT_POINTS_COUNT = 3

        fun sequentialPoints(count: Int): List<PointOfInterest> =
            someObjects<PointOfInterest>(count)
                .toList()
                .mapIndexed { index, point -> point.copy(id = PointOfInterest.FIRST_ID + index) }

        fun routeWithPoints(count: Int): Route =
            Route(name = someString(), pointsOfInterest = sequentialPoints(count))

        fun routeWithAllPointsCaught(count: Int): Route =
            routeWithPoints(count).let { route ->
                route.copy(pointsOfInterest = route.pointsOfInterest.map { it.copy(caught = true) })
            }

        fun routeWithoutPoints(): Route = Route(name = someString())
    }

    override fun instance(): Route = Route(
        id = Route.NOT_PERSISTED,
        name = someString(),
        pointsOfInterest = sequentialPoints(DEFAULT_POINTS_COUNT),
        lastSelectedPointId = null
    )
}
