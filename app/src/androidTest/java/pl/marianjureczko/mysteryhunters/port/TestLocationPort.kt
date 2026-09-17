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

import kotlinx.coroutines.CoroutineScope
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import pl.marianjureczko.poszukiwacz.compass.api.LocationPort

/**
 * Feeds the app a location chosen by the test instead of the real GPS.
 */
class TestLocationPort : LocationPort {

    var currentLocation: AndroidLocation = AndroidLocation.create(0.0, 0.0)
    private var callback: ((AndroidLocation) -> Unit)? = null

    override fun startFetching(
        coroutineScope: CoroutineScope,
        updateLocationCallback: (AndroidLocation) -> Unit
    ) {
        callback = updateLocationCallback
        updateLocationCallback(currentLocation)
    }

    override fun stopFetching() {
        callback = null
    }

    fun moveTo(location: AndroidLocation) {
        currentLocation = location
        callback?.invoke(location)
    }
}
