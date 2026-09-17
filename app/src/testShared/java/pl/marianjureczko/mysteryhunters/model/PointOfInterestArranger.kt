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

package pl.marianjureczko.mysteryhunters.model

import com.ocadotechnology.gembus.test.CustomArranger
import com.ocadotechnology.gembus.test.someDouble
import com.ocadotechnology.gembus.test.somePositiveInt
import com.ocadotechnology.gembus.test.someString

/**
 * Keeps the invariants a point always has: a positive id, coordinates that exist on Earth and,
 * because a freshly created point has never been found, the not caught state.
 */
class PointOfInterestArranger : CustomArranger<PointOfInterest>() {

    companion object {
        private const val MAX_GENERATED_ID = 1000
    }

    override fun instance(): PointOfInterest = PointOfInterest(
        id = somePositiveInt(MAX_GENERATED_ID),
        latitude = someDouble(-90.0, 90.0),
        longitude = someDouble(-180.0, 180.0),
        description = someString(),
        caught = false
    )
}
