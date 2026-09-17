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

import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.usecase.CalculateMarkerPositionUC

data class CameraState(
    val point: PointOfInterest? = null,
    val arAvailable: Boolean = false,
    /** True once the hunter is close enough for the question mark to show up. */
    val inRange: Boolean = false,
    val markerPosition: CalculateMarkerPositionUC.Position? = null,
    val tooFarMessageShown: Boolean = false,
    /** Set once the point has been caught, which sends the screen on to the point description. */
    val caughtPointId: Int? = null
)
