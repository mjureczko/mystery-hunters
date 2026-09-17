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

package pl.marianjureczko.mysteryhunters.port

/**
 * Wraps the device orientation sensors. The azimuth is needed to work out where, relative to the
 * phone, the augmented reality marker has to be drawn.
 */
interface DeviceOrientationPort {

    /** @param onAzimuth receives the heading in degrees clockwise from true north, 0..360. */
    fun startListening(onAzimuth: (Float) -> Unit)

    fun stopListening()
}
