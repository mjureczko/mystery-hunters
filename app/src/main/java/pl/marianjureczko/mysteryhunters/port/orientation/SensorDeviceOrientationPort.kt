/*
 * Copyright (C) 2026 Marian Jureczko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package pl.marianjureczko.mysteryhunters.port.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import pl.marianjureczko.mysteryhunters.port.DeviceOrientationPort
import kotlin.math.atan2

class SensorDeviceOrientationPort(context: Context) : DeviceOrientationPort {

    companion object {
        private const val FULL_CIRCLE_IN_DEGREES = 360f

        /**
         * Where the camera is aimed, in degrees clockwise from north.
         *
         * The obvious [SensorManager.getOrientation] reports something else: where the top edge of
         * the phone points. Lying flat that is the direction the hunter walks in, which is what a
         * compass needs, but the augmented reality screen is used with the phone held up, and then
         * the two readings differ by however far the phone is rolled to the side. Measured on a
         * Galaxy S21 Ultra the error was equal to the roll angle - a natural grip is some twenty
         * degrees out, enough to push the question mark off the screen, and a phone turned to
         * landscape is ninety degrees out.
         *
         * The camera looks along the device -Z axis, so its heading is read straight out of the
         * rotation matrix instead. Column 2 of the matrix is the device Z axis expressed in the
         * east-north-up frame of the world, which makes -[2] the east and -[5] the north component
         * of the direction the camera is aimed at. That is free of the roll of the phone.
         *
         * @param rotationMatrix nine element rotation matrix from
         *        [SensorManager.getRotationMatrixFromVector].
         */
        fun cameraHeadingInDegrees(rotationMatrix: FloatArray): Float {
            val degrees = Math.toDegrees(
                atan2(-rotationMatrix[2].toDouble(), -rotationMatrix[5].toDouble())
            ).toFloat()
            return (degrees + FULL_CIRCLE_IN_DEGREES) % FULL_CIRCLE_IN_DEGREES
        }
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val rotationMatrix = FloatArray(9)
    private var listener: SensorEventListener? = null

    override fun startListening(onAzimuth: (Float) -> Unit) {
        val sensor = rotationVector ?: return
        stopListening()
        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                onAzimuth(cameraHeadingInDegrees(rotationMatrix))
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
