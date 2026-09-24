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

package pl.marianjureczko.mysteryhunters.usecase

import kotlin.math.cos
import kotlin.math.sin

/**
 * Works out where the question mark has to be drawn in the augmented reality scene.
 *
 * ARCore does not align its world with north, so the marker is placed by the angle between where
 * the point lies ([bearingDegrees]) and whichever compass bearing the scene happens to call
 * straight ahead ([forwardBearingDegrees]); the measured distance decides how far away it is. In
 * the scene the camera looks along -Z with +X to the right.
 */
class CalculateMarkerPositionUC {

    companion object {
        // visibility for tests
        const val NEAREST_RENDERING_DISTANCE_IN_METERS = 1.0f
        const val FARTHEST_RENDERING_DISTANCE_IN_METERS = 20.0f
        const val HEIGHT_IN_METERS = -0.5f
    }

    data class Position(val x: Float, val y: Float, val z: Float)

    /**
     * @param bearingDegrees where the point lies, clockwise from north.
     * @param forwardBearingDegrees the compass bearing that straight ahead in the scene, its -Z
     *        axis, corresponds to. Not the direction the phone is pointed in: the scene does not
     *        turn when the hunter turns, augmented reality tracks the camera through it instead.
     * @param distanceInMeters how far the point is, before clamping to the drawable range.
     */
    operator fun invoke(
        bearingDegrees: Float,
        forwardBearingDegrees: Float,
        distanceInMeters: Float
    ): Position {
        val relativeBearing =
            Math.toRadians(((bearingDegrees - forwardBearingDegrees + 360f) % 360f).toDouble())
        val distance = distanceInMeters.coerceIn(
            NEAREST_RENDERING_DISTANCE_IN_METERS,
            FARTHEST_RENDERING_DISTANCE_IN_METERS
        )
        return Position(
            x = (sin(relativeBearing) * distance).toFloat(),
            y = HEIGHT_IN_METERS,
            z = (-cos(relativeBearing) * distance).toFloat()
        )
    }
}
