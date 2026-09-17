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

package pl.marianjureczko.mysteryhunters.port.ar

import android.content.Context
import android.util.Log
import com.google.ar.core.ArCoreApk
import pl.marianjureczko.mysteryhunters.port.ArAvailabilityPort

class ArCoreAvailabilityPort(private val context: Context) : ArAvailabilityPort {

    private val TAG = javaClass.simpleName

    override fun isArAvailable(): Boolean =
        try {
            ArCoreApk.getInstance().checkAvailability(context).isSupported
        } catch (e: Exception) {
            Log.w(TAG, "ARCore availability check failed", e)
            false
        }
}
