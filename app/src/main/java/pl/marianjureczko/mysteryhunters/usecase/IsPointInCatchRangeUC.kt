package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import pl.marianjureczko.poszukiwacz.compass.api.LocationCalculator
import pl.marianjureczko.mysteryhunters.model.PointOfInterest

/**
 * A point can be caught only when the hunter stands close enough to it. The distance is measured
 * with the compass module, the single source of truth for distance calculations.
 */
class IsPointInCatchRangeUC(private val locationCalculator: LocationCalculator) {

    companion object {
        // visibility for tests
        const val CATCH_RANGE_IN_METERS = 20.0f
    }

    operator fun invoke(point: PointOfInterest, hunterLocation: AndroidLocation?): Boolean {
        if (hunterLocation == null) {
            return false
        }
        val target = AndroidLocation.create(point.latitude, point.longitude)
        return locationCalculator.distanceInMeters(target, hunterLocation) <= CATCH_RANGE_IN_METERS
    }
}
