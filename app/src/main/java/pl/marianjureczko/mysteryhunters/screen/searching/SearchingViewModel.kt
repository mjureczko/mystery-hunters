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

package pl.marianjureczko.mysteryhunters.screen.searching

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.screen.Screens
import pl.marianjureczko.mysteryhunters.usecase.LoadRouteUC
import pl.marianjureczko.mysteryhunters.usecase.SelectNextPointUC
import pl.marianjureczko.mysteryhunters.usecase.SelectPointToSearchUC
import javax.inject.Inject

@HiltViewModel
class SearchingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadRouteUC: LoadRouteUC,
    private val selectPointToSearchUC: SelectPointToSearchUC,
    private val selectNextPointUC: SelectNextPointUC
) : ViewModel() {

    val routeId: Long = savedStateHandle[Screens.Searching.PARAMETER_ROUTE_ID] ?: Route.NOT_PERSISTED

    private val _state = mutableStateOf(SearchingState())
    val state: State<SearchingState> = _state

    /**
     * Reads the route back from the storage, which is also how the screen learns about a point
     * caught on the camera screen while it was in the background.
     */
    fun refresh() {
        viewModelScope.launch {
            val route = loadRouteUC(routeId) ?: return@launch
            publish(selectPointToSearchUC(route))
        }
    }

    fun changePoint() {
        val route = _state.value.route ?: return
        viewModelScope.launch {
            publish(selectNextPointUC(route))
        }
    }

    private fun publish(route: Route) {
        _state.value = SearchingState(
            route = route,
            selectedPoint = route.pointById(route.lastSelectedPointId)
        )
    }
}
