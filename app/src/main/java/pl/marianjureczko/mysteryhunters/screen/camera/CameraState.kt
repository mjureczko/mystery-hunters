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

/** What stopped the hunter from catching the point they are hunting. */
enum class CatchRefusal {
    /** Not close enough to the point yet. */
    TOO_FAR,

    /** Close enough, but the mark cannot be seen, so there is nothing to catch yet. */
    MARK_NOT_VISIBLE
}

data class CameraState(
    val point: PointOfInterest? = null,
    /**
     * Null until the ARCore check has answered. Neither the augmented reality scene nor the plain
     * preview may be shown before that, because both open the camera and two camera clients in one
     * application evict each other.
     */
    val arAvailable: Boolean? = null,
    /** True once the hunter is close enough for the question mark to show up. */
    val inRange: Boolean = false,
    val markerPosition: CalculateMarkerPositionUC.Position? = null,
    /**
     * True only while the scene can show that the mark is there to be seen: placed, tracked, on
     * the screen and with nothing in front of it. Anything less counts as not visible, and what
     * cannot be seen cannot be caught.
     */
    val markVisible: Boolean = false,
    /** Why the last attempt to catch was turned down, or null when none was. */
    val catchRefusal: CatchRefusal? = null,
    /** Set once the point has been caught, which sends the screen on to the point description. */
    val caughtPointId: Int? = null
)
