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

package pl.marianjureczko.mysteryhunters.port.location

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import pl.marianjureczko.poszukiwacz.compass.api.LocationPort
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Lets more than one screen listen to the position at the same time.
 *
 * The port of the compass module keeps room for a single listener: asking it to start replaces
 * whoever was listening, and asking it to stop silences everybody. With one port shared by the
 * whole application that is enough to break the hunt - opening the camera screen took the compass
 * screen's listener away, and leaving the camera screen stopped the fetching altogether, so the
 * compass went on showing the distance it had read last and never corrected it.
 *
 * This wrapper subscribes to the real port once and hands what comes out to every listener. A
 * listener is tied to the scope it was registered with rather than to a call to [stopFetching],
 * because on a shared port that call cannot say whose listening should end. The last listener
 * going away stops the real fetching, so nothing is drained when no screen is watching.
 */
class SharedLocationPort(private val delegate: LocationPort) : LocationPort {

    private val listeners = CopyOnWriteArrayList<(AndroidLocation) -> Unit>()
    private val ownScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Volatile
    private var fetching = false

    /** So a screen that opens between two fixes does not sit without a position. */
    @Volatile
    private var lastKnown: AndroidLocation? = null

    override fun startFetching(
        coroutineScope: CoroutineScope,
        updateLocationCallback: (AndroidLocation) -> Unit
    ) {
        listeners.add(updateLocationCallback)
        coroutineScope.coroutineContext[Job]?.invokeOnCompletion {
            listeners.remove(updateLocationCallback)
            if (listeners.isEmpty()) {
                fetching = false
                delegate.stopFetching()
            }
        }
        if (!fetching) {
            fetching = true
            delegate.startFetching(ownScope) { location ->
                lastKnown = location
                listeners.forEach { it(location) }
            }
        }
        lastKnown?.let(updateLocationCallback)
    }

    /**
     * Deliberately does nothing. One screen asking to stop must not silence the others, and the
     * call carries nothing to say which listener it means. Listeners end with their own scopes.
     */
    override fun stopFetching() = Unit
}
