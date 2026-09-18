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

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.screen.Screens
import pl.marianjureczko.mysteryhunters.ui.Screen.dh
import pl.marianjureczko.mysteryhunters.ui.Screen.dw
import pl.marianjureczko.mysteryhunters.ui.components.ImageButton
import pl.marianjureczko.mysteryhunters.ui.components.TopBar
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import pl.marianjureczko.poszukiwacz.compass.api.CompassAndSteps

const val CATCH_POINT_BUTTON = "Catch point"
const val CHANGE_POINT_BUTTON = "Change point"
const val COLLECTED_POINTS_BUTTON = "Collected points"
const val SEARCHED_POINT_LABEL = "Searched point"
const val PROGRESS_LABEL = "Collected progress"
const val CONGRATULATIONS_LABEL = "Congratulations"
const val LOCATION_PERMISSION_LABEL = "Location permission info"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchingScreen(navController: NavController) {
    val viewModel: SearchingViewModel = hiltViewModel()
    val state = viewModel.state.value
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Coming back from the camera screen has to pick up the freshly caught point.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = { TopBar(navController, state.route?.name ?: stringResource(R.string.searching_title)) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Header(state)
                val selectedPoint = state.selectedPoint
                if (locationPermission.status.isGranted) {
                    CompassAndSteps(
                        selectedTreasure = selectedPoint?.let {
                            AndroidLocation.create(it.latitude, it.longitude)
                        },
                        height = 0.59.dh,
                        textStyle = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LocationPermissionRequest { locationPermission.launchPermissionRequest() }
                }
                Buttons(
                    enabled = state.hasPoints && selectedPoint != null,
                    onCatch = {
                        selectedPoint?.let {
                            navController.navigate(Screens.Camera.doRoute(viewModel.routeId, it.id))
                        }
                    },
                    onChangePoint = { viewModel.changePoint() },
                    onCollectedPoints = {
                        navController.navigate(Screens.CollectedPoints.doRoute(viewModel.routeId))
                    }
                )
            }
        }
    )
}

/**
 * The compass module silently skips fetching locations without the fine location permission,
 * so it must be granted at runtime before the compass is shown.
 */
@Composable
private fun LocationPermissionRequest(onRequest: () -> Unit) {
    LaunchedEffect(Unit) {
        onRequest()
    }
    Text(
        text = stringResource(R.string.location_permission_needed),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(12.dp)
            .clickable { onRequest() }
            .semantics { contentDescription = LOCATION_PERMISSION_LABEL }
    )
}

@Composable
private fun Header(state: SearchingState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.allCaught) {
            Text(
                text = stringResource(R.string.congratulations),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { contentDescription = CONGRATULATIONS_LABEL }
            )
        } else {
            Text(
                text = state.selectedPoint
                    ?.let { stringResource(R.string.looking_for_point, it.id) }
                    ?: stringResource(R.string.no_points_on_route),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { contentDescription = SEARCHED_POINT_LABEL }
            )
        }
        Text(
            text = stringResource(R.string.points_summary, state.caughtCount, state.pointsCount),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { contentDescription = PROGRESS_LABEL }
        )
    }
}

@Composable
private fun Buttons(
    enabled: Boolean,
    onCatch: () -> Unit,
    onChangePoint: () -> Unit,
    onCollectedPoints: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(0.2.dw), horizontalAlignment = Alignment.CenterHorizontally) {
            ImageButton(R.drawable.change_point, CHANGE_POINT_BUTTON, enabled = enabled, onClick = onChangePoint)
        }
        Column(modifier = Modifier.width(0.5.dw), horizontalAlignment = Alignment.CenterHorizontally) {
            ImageButton(
                drawableId = R.drawable.question_mark,
                description = CATCH_POINT_BUTTON,
                enabled = enabled,
                padding = 4.dp,
                onClick = onCatch
            )
        }
        Column(modifier = Modifier.width(0.2.dw), horizontalAlignment = Alignment.CenterHorizontally) {
            ImageButton(R.drawable.collected_points, COLLECTED_POINTS_BUTTON, onClick = onCollectedPoints)
        }
    }
}
