package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * The initial bearing from the hunter to a point, in degrees clockwise from true north.
 */
class CalculateBearingUC {

    operator fun invoke(point: PointOfInterest, hunterLocation: AndroidLocation): Float {
        val fromLatitude = Math.toRadians(hunterLocation.latitude)
        val toLatitude = Math.toRadians(point.latitude)
        val deltaLongitude = Math.toRadians(point.longitude - hunterLocation.longitude)

        val y = sin(deltaLongitude) * cos(toLatitude)
        val x = cos(fromLatitude) * sin(toLatitude) -
                sin(fromLatitude) * cos(toLatitude) * cos(deltaLongitude)
        val degrees = Math.toDegrees(atan2(y, x))
        return ((degrees + 360.0) % 360.0).toFloat()
    }
}
