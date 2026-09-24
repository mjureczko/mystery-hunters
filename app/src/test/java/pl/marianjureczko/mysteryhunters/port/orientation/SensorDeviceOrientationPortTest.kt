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

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SensorDeviceOrientationPortTest {

    private val tolerance = within(0.01f)

    @ParameterizedTest
    @ValueSource(floats = [0f, 90f, 180f, 270f, 37f])
    fun `report the heading the camera is aimed at`(heading: Float) {
        // given
        val rotationMatrix = phoneHeldUp(cameraHeading = heading, roll = 0f)

        // when
        val reported = SensorDeviceOrientationPort.cameraHeadingInDegrees(rotationMatrix)

        // then
        assertThat(reported).isCloseTo(heading, tolerance)
    }

    @ParameterizedTest
    @ValueSource(floats = [-90f, -25f, 0f, 25f, 90f, 178f])
    fun `report the same heading however far the phone is rolled to the side`(roll: Float) {
        // given
        val heading = 196f
        val rotationMatrix = phoneHeldUp(cameraHeading = heading, roll = roll)

        // when
        val reported = SensorDeviceOrientationPort.cameraHeadingInDegrees(rotationMatrix)

        // then
        assertThat(reported).isCloseTo(heading, tolerance)
    }

    /**
     * The reading that [android.hardware.SensorManager.getOrientation] produces, which the port
     * used before, does not follow the camera: rolling the phone around its line of sight swings
     * it away while the camera stays put. On a Galaxy S21 Ultra the measured error was equal to
     * the roll angle, enough to push the question mark off the screen at a natural grip. This pins
     * the difference down so that nobody quietly puts the old formula back.
     */
    @Test
    fun `the heading of the top edge of the phone does not follow the camera`() {
        // given
        val heading = 196f
        val rolledLeft = phoneHeldUp(cameraHeading = heading, roll = -25f)
        val rolledRight = phoneHeldUp(cameraHeading = heading, roll = 25f)

        // when
        val cameraLeft = SensorDeviceOrientationPort.cameraHeadingInDegrees(rolledLeft)
        val cameraRight = SensorDeviceOrientationPort.cameraHeadingInDegrees(rolledRight)
        val topEdgeLeft = topEdgeHeadingInDegrees(rolledLeft)
        val topEdgeRight = topEdgeHeadingInDegrees(rolledRight)

        // then
        assertThat(cameraLeft).isCloseTo(heading, tolerance)
        assertThat(cameraRight).isCloseTo(heading, tolerance)
        assertThat(degreesBetween(topEdgeLeft, topEdgeRight)).isGreaterThan(90f)
    }

    /** What the port used to report: where the top edge of the phone points. */
    private fun topEdgeHeadingInDegrees(rotationMatrix: FloatArray): Float {
        val degrees = Math.toDegrees(
            atan2(rotationMatrix[1].toDouble(), rotationMatrix[4].toDouble())
        ).toFloat()
        return (degrees + 360f) % 360f
    }

    private fun degreesBetween(one: Float, other: Float): Float {
        val difference = kotlin.math.abs(one - other) % 360f
        return if (difference > 180f) 360f - difference else difference
    }

    /**
     * Rotation matrix of a phone held up the way the augmented reality screen is used: the camera
     * aimed at the horizon along [cameraHeading], the phone rolled [roll] degrees around that line
     * of sight. Columns of the matrix are the device axes written in the east-north-up frame of
     * the world, which is the shape
     * [android.hardware.SensorManager.getRotationMatrixFromVector] produces.
     */
    private fun phoneHeldUp(cameraHeading: Float, roll: Float): FloatArray {
        val heading = Math.toRadians(cameraHeading.toDouble())
        val rollRadians = Math.toRadians(roll.toDouble())
        // Where the camera looks, and the two directions across it, before any roll.
        val camera = floatArrayOf(sin(heading).toFloat(), cos(heading).toFloat(), 0f)
        val right = floatArrayOf(cos(heading).toFloat(), -sin(heading).toFloat(), 0f)
        val up = floatArrayOf(0f, 0f, 1f)

        val deviceZ = FloatArray(3) { -camera[it] }
        val deviceX = FloatArray(3) {
            (cos(rollRadians) * right[it] + sin(rollRadians) * up[it]).toFloat()
        }
        val deviceY = FloatArray(3) {
            (-sin(rollRadians) * right[it] + cos(rollRadians) * up[it]).toFloat()
        }
        return floatArrayOf(
            deviceX[0], deviceY[0], deviceZ[0],
            deviceX[1], deviceY[1], deviceZ[1],
            deviceX[2], deviceY[2], deviceZ[2]
        )
    }
}
