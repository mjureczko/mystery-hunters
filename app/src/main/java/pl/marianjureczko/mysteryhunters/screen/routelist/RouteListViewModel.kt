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

package pl.marianjureczko.mysteryhunters.screen.routelist

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.usecase.DeleteRouteUC
import pl.marianjureczko.mysteryhunters.usecase.ObserveRoutesUC
import javax.inject.Inject

@HiltViewModel
class RouteListViewModel @Inject constructor(
    private val observeRoutesUC: ObserveRoutesUC,
    private val deleteRouteUC: DeleteRouteUC
) : ViewModel() {

    private val _state = mutableStateOf(RouteListState())
    val state: State<RouteListState> = _state

    init {
        viewModelScope.launch {
            observeRoutesUC().collectLatest { routes ->
                _state.value = _state.value.copy(routes = routes)
            }
        }
    }

    fun askToDelete(route: Route) {
        _state.value = _state.value.copy(routeToDelete = route)
    }

    fun cancelDeletion() {
        _state.value = _state.value.copy(routeToDelete = null)
    }

    fun confirmDeletion() {
        val route = _state.value.routeToDelete ?: return
        viewModelScope.launch {
            deleteRouteUC(route.id)
            _state.value = _state.value.copy(routeToDelete = null)
        }
    }
}
