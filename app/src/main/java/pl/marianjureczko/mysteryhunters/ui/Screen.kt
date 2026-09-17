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

package pl.marianjureczko.mysteryhunters.ui

import android.app.Activity
import androidx.compose.ui.unit.Dp
import androidx.window.layout.WindowMetricsCalculator

/**
 * Screen relative sizing, so that the layout scales with the device instead of using fixed dp.
 */
object Screen {
    var WIDTH = 1080f
        private set
    var HEIGHT = 2280f
        private set
    var DENSITY = 2.75f
        private set

    fun init(activity: Activity) {
        WindowMetricsCalculator
            .getOrCreate()
            .computeCurrentWindowMetrics(activity)
            .bounds.let {
                WIDTH = it.width().toFloat()
                HEIGHT = it.height().toFloat()
            }
        DENSITY = activity.resources.displayMetrics.density
    }

    inline val Number.PxToDp get() = this.toFloat() / DENSITY
    inline val Number.dw: Dp get() = Dp(value = (this.toFloat() * WIDTH).PxToDp)
    inline val Number.dh: Dp get() = Dp(value = (this.toFloat() * HEIGHT).PxToDp)
}
