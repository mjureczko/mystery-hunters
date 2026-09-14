package pl.marianjureczko.mysteryhunters.screen.searching

import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.Route

data class SearchingState(
    val route: Route? = null,
    val selectedPoint: PointOfInterest? = null
) {
    val caughtCount: Int
        get() = route?.caughtCount ?: 0

    val pointsCount: Int
        get() = route?.pointsCount ?: 0

    val allCaught: Boolean
        get() = route?.allCaught == true

    val hasPoints: Boolean
        get() = pointsCount > 0
}
