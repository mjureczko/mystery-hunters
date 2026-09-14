package pl.marianjureczko.mysteryhunters.port

/**
 * Wraps the device orientation sensors. The azimuth is needed to work out where, relative to the
 * phone, the augmented reality marker has to be drawn.
 */
interface DeviceOrientationPort {

    /** @param onAzimuth receives the heading in degrees clockwise from true north, 0..360. */
    fun startListening(onAzimuth: (Float) -> Unit)

    fun stopListening()
}
