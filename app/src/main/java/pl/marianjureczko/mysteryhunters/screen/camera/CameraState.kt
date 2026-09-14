package pl.marianjureczko.mysteryhunters.screen.camera

import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.usecase.CalculateMarkerPositionUC

data class CameraState(
    val point: PointOfInterest? = null,
    val arAvailable: Boolean = false,
    /** True once the hunter is close enough for the question mark to show up. */
    val inRange: Boolean = false,
    val markerPosition: CalculateMarkerPositionUC.Position? = null,
    val tooFarMessageShown: Boolean = false,
    /** Set once the point has been caught, which sends the screen on to the point description. */
    val caughtPointId: Int? = null
)
