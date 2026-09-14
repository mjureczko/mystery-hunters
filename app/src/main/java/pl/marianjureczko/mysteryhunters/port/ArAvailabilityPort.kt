package pl.marianjureczko.mysteryhunters.port

/**
 * Tells whether the device can render the question mark in augmented reality. When it cannot, the
 * camera screen falls back to a plain camera preview with a flat marker.
 */
interface ArAvailabilityPort {
    fun isArAvailable(): Boolean
}
