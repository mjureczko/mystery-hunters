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

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

/**
 * Decides which point the searching screen navigates to when it is opened.
 *
 * The first time a route is opened there is nothing remembered yet, so the hunt starts at the first
 * point. On every later opening the point that was navigated to last is restored.
 */
class SelectPointToSearchUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(route: Route): Route {
        val remembered = route.pointById(route.lastSelectedPointId)
        if (remembered != null) {
            return route
        }
        val first = route.firstPoint() ?: return route
        return storage.save(route.copy(lastSelectedPointId = first.id))
    }
}
