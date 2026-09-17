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

import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * The initial bearing from the hunter to a point, in degrees clockwise from true north.
 */
class CalculateBearingUC {

    operator fun invoke(point: PointOfInterest, hunterLocation: AndroidLocation): Float {
        val fromLatitude = Math.toRadians(hunterLocation.latitude)
        val toLatitude = Math.toRadians(point.latitude)
        val deltaLongitude = Math.toRadians(point.longitude - hunterLocation.longitude)

        val y = sin(deltaLongitude) * cos(toLatitude)
        val x = cos(fromLatitude) * sin(toLatitude) -
                sin(fromLatitude) * cos(toLatitude) * cos(deltaLongitude)
        val degrees = Math.toDegrees(atan2(y, x))
        return ((degrees + 360.0) % 360.0).toFloat()
    }
}
