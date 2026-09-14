package pl.marianjureczko.mysteryhunters.port

/** Lets a test decide whether the device pretends to support augmented reality. */
class TestArAvailabilityPort : ArAvailabilityPort {

    var available: Boolean = true

    override fun isArAvailable(): Boolean = available
}
