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

package pl.marianjureczko.mysteryhunters.screen.camera

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.math.cos
import kotlin.math.sin

class ViewFrustumTest {

    private val sut = ViewFrustum()

    /** A camera opened ninety degrees wide on a square picture, as OpenGL builds it. */
    private val projection = perspective(nearPlane = 0.1f, farPlane = 100f)

    /** A camera at the origin looking down -Z, which is where a scene starts. */
    private val lookingForward = identity()

    @Test
    fun `SHOULD hold what stands straight in front of the camera`() {
        // when
        val held = sut.holds(projection, lookingForward, x = 0f, y = 0f, z = -5f)

        // then
        assertThat(held).isTrue()
    }

    @Test
    fun `SHOULD not hold what stands behind the camera`() {
        // when
        val held = sut.holds(projection, lookingForward, x = 0f, y = 0f, z = 5f)

        // then
        assertThat(held).isFalse()
    }

    @Test
    fun `SHOULD not hold what stands off to the side`() {
        // given the camera opens forty five degrees each way, so five metres ahead the picture
        // reaches five metres across, and nine is well outside it

        // when
        val held = sut.holds(projection, lookingForward, x = 9f, y = 0f, z = -5f)

        // then
        assertThat(held).isFalse()
    }

    @Test
    fun `SHOULD hold what stands just inside the edge of the picture`() {
        // when
        val held = sut.holds(projection, lookingForward, x = 4.9f, y = 0f, z = -5f)

        // then
        assertThat(held).isTrue()
    }

    @Test
    fun `SHOULD not hold what stands too high to be in the picture`() {
        // when
        val held = sut.holds(projection, lookingForward, x = 0f, y = 9f, z = -5f)

        // then
        assertThat(held).isFalse()
    }

    @Test
    fun `SHOULD lose what the camera has turned away from`() {
        // given
        val turnedAQuarterToTheRight = turnedAboutTheUprightAxis(90f)

        // when
        val held = sut.holds(projection, turnedAQuarterToTheRight, x = 0f, y = 0f, z = -5f)

        // then
        assertThat(held).isFalse()
    }

    private fun identity() = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    private fun perspective(nearPlane: Float, farPlane: Float): FloatArray {
        val depthSpan = nearPlane - farPlane
        return floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, (farPlane + nearPlane) / depthSpan, -1f,
            0f, 0f, 2f * farPlane * nearPlane / depthSpan, 0f
        )
    }

    /**
     * View matrix of a camera turned [degrees] to the right, which is the world turned the same
     * amount to the left.
     */
    private fun turnedAboutTheUprightAxis(degrees: Float): FloatArray {
        val angle = Math.toRadians(-degrees.toDouble())
        val turnCos = cos(angle).toFloat()
        val turnSin = sin(angle).toFloat()
        return floatArrayOf(
            turnCos, 0f, -turnSin, 0f,
            0f, 1f, 0f, 0f,
            turnSin, 0f, turnCos, 0f,
            0f, 0f, 0f, 1f
        )
    }
}
