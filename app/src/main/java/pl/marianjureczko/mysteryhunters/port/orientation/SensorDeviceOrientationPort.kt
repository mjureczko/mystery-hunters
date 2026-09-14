package pl.marianjureczko.mysteryhunters.port.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import pl.marianjureczko.mysteryhunters.port.DeviceOrientationPort

class SensorDeviceOrientationPort(context: Context) : DeviceOrientationPort {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private var listener: SensorEventListener? = null

    override fun startListening(onAzimuth: (Float) -> Unit) {
        val sensor = rotationVector ?: return
        stopListening()
        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                onAzimuth((degrees + 360f) % 360f)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun stopListening() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }
}
