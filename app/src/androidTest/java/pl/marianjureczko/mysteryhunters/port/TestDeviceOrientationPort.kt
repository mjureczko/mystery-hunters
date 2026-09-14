package pl.marianjureczko.mysteryhunters.port

class TestDeviceOrientationPort : DeviceOrientationPort {

    private var onAzimuth: ((Float) -> Unit)? = null

    override fun startListening(onAzimuth: (Float) -> Unit) {
        this.onAzimuth = onAzimuth
        onAzimuth(0f)
    }

    override fun stopListening() {
        onAzimuth = null
    }

    fun turnTo(azimuthDegrees: Float) {
        onAzimuth?.invoke(azimuthDegrees)
    }
}
