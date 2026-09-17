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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.screen.Screens
import pl.marianjureczko.mysteryhunters.ui.components.ImageButton
import pl.marianjureczko.mysteryhunters.ui.components.MyCard
import pl.marianjureczko.mysteryhunters.ui.components.TopBar
import pl.marianjureczko.mysteryhunters.ui.components.YesNoDialog

const val ADD_ROUTE_BUTTON = "Add route"
const val EDIT_ROUTE_BUTTON = "Edit route"
const val DELETE_ROUTE_BUTTON = "Delete route"
const val SELECT_ROUTE_BUTTON = "Select route"
const val ROUTES_LIST = "Routes list"

@Composable
fun RouteListScreen(navController: NavController) {
    val viewModel: RouteListViewModel = hiltViewModel()
    val state = viewModel.state.value

    Scaffold(
        topBar = { TopBar(navController, stringResource(R.string.route_list_title)) },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                YesNoDialog(
                    visible = state.routeToDelete != null,
                    hideIt = { viewModel.cancelDeletion() },
                    text = stringResource(R.string.delete_route_msg, state.routeToDelete?.name ?: ""),
                    onConfirm = { viewModel.confirmDeletion() }
                )
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    ImageButton(
                        drawableId = R.drawable.add_point,
                        description = ADD_ROUTE_BUTTON,
                        onClick = { navController.navigate(Screens.RouteEditor.doRoute(Screens.RouteEditor.NEW_ROUTE)) }
                    )
                }
                if (state.routes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_routes_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.routes, key = { it.id }) { route ->
                            RouteRow(
                                route = route,
                                onEdit = { navController.navigate(Screens.RouteEditor.doRoute(route.id)) },
                                onDelete = { viewModel.askToDelete(route) },
                                onSelect = { navController.navigate(Screens.Searching.doRoute(route.id)) }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun RouteRow(route: Route, onEdit: () -> Unit, onDelete: () -> Unit, onSelect: () -> Unit) {
    MyCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = route.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = stringResource(R.string.points_summary, route.caughtCount, route.pointsCount),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            ImageButton(R.drawable.edit_point, "$EDIT_ROUTE_BUTTON ${route.name}", onClick = onEdit)
            ImageButton(R.drawable.delete_point, "$DELETE_ROUTE_BUTTON ${route.name}", onClick = onDelete)
            ImageButton(R.drawable.question_mark, "$SELECT_ROUTE_BUTTON ${route.name}", onClick = onSelect)
        }
    }
}
