package pl.marianjureczko.mysteryhunters.port

import kotlinx.coroutines.CoroutineScope
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import pl.marianjureczko.poszukiwacz.compass.api.LocationPort

/**
 * Feeds the app a location chosen by the test instead of the real GPS.
 */
class TestLocationPort : LocationPort {

    var currentLocation: AndroidLocation = AndroidLocation.create(0.0, 0.0)
    private var callback: ((AndroidLocation) -> Unit)? = null

    override fun startFetching(
        coroutineScope: CoroutineScope,
        updateLocationCallback: (AndroidLocation) -> Unit
    ) {
        callback = updateLocationCallback
        updateLocationCallback(currentLocation)
    }

    override fun stopFetching() {
        callback = null
    }

    fun moveTo(location: AndroidLocation) {
        currentLocation = location
        callback?.invoke(location)
    }
}
