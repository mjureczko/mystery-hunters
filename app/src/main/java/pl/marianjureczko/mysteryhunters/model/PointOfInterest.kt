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

/**
 * A single point of interest belonging to a [Route].
 *
 * The [id] is a natural number, unique within the route, assigned automatically when the point is
 * created and never changed afterwards. It is presented to the user.
 */
data class PointOfInterest(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val caught: Boolean = false
) {
    companion object {
        const val FIRST_ID = 1
    }
}
