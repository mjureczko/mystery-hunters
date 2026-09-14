package pl.marianjureczko.mysteryhunters.model

import com.ocadotechnology.gembus.test.CustomArranger
import com.ocadotechnology.gembus.test.someDouble
import com.ocadotechnology.gembus.test.somePositiveInt
import com.ocadotechnology.gembus.test.someString

/**
 * Keeps the invariants a point always has: a positive id, coordinates that exist on Earth and,
 * because a freshly created point has never been found, the not caught state.
 */
class PointOfInterestArranger : CustomArranger<PointOfInterest>() {

    companion object {
        private const val MAX_GENERATED_ID = 1000
    }

    override fun instance(): PointOfInterest = PointOfInterest(
        id = somePositiveInt(MAX_GENERATED_ID),
        latitude = someDouble(-90.0, 90.0),
        longitude = someDouble(-180.0, 180.0),
        description = someString(),
        caught = false
    )
}
