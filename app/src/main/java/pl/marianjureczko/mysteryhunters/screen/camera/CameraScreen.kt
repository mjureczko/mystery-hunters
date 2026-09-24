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

package pl.marianjureczko.mysteryhunters.screen.camera

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.screen.Screens
import pl.marianjureczko.mysteryhunters.ui.Screen.dw
import pl.marianjureczko.mysteryhunters.ui.components.ImageButton
import pl.marianjureczko.mysteryhunters.ui.components.OkDialog

const val BIG_CATCH_BUTTON = "Big catch button"
const val NOTHING_IN_RANGE_MESSAGE = "Nothing in range"
const val CLOSE_LOOK_AROUND_MESSAGE = "Close, look around"
const val TOO_FAR_DIALOG = "Too far dialog"
const val MARK_NOT_VISIBLE_DIALOG = "Mark not visible dialog"
const val FLAT_QUESTION_MARK = "Flat question mark"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(navController: NavController, routeId: Long) {
    val viewModel: CameraViewModel = hiltViewModel()
    val state = viewModel.state.value
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermission.status.isGranted) {
        if (cameraPermission.status.isGranted) {
            viewModel.start()
        } else {
            cameraPermission.launchPermissionRequest()
        }
    }

    state.caughtPointId?.let { caughtPointId ->
        LaunchedEffect(caughtPointId) {
            navController.navigate(Screens.PointDetail.doRoute(routeId, caughtPointId)) {
                popUpTo(Screens.Camera.ROUTE) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val notVisible = state.catchRefusal == CatchRefusal.MARK_NOT_VISIBLE
        OkDialog(
            visible = state.catchRefusal != null,
            hideIt = { viewModel.hideCatchRefusal() },
            text = stringResource(
                if (notVisible) R.string.cannot_catch_mark_not_visible else R.string.too_far_to_catch
            ),
            description = if (notVisible) MARK_NOT_VISIBLE_DIALOG else TOO_FAR_DIALOG
        )
        if (cameraPermission.status.isGranted) {
            // Nothing is drawn while the ARCore check is still running. Showing the plain preview
            // in the meantime would bind CameraX to the camera, and CameraX would then keep
            // fighting ARCore over it for as long as the activity lives.
            when (state.arAvailable) {
                true -> ArQuestionMark(
                    markerPosition = state.markerPosition,
                    onCameraHeadingInScene = viewModel::onCameraHeadingInScene,
                    onMarkVisible = viewModel::onMarkVisible,
                    modifier = Modifier.fillMaxSize()
                )
                false -> {
                    CameraPreview(Modifier.fillMaxSize())
                    if (state.inRange) {
                        ImageButton(
                            drawableId = R.drawable.question_mark,
                            description = FLAT_QUESTION_MARK,
                            modifier = Modifier.align(Alignment.Center),
                            onClick = { viewModel.catchPoint() }
                        )
                    }
                }
                null -> Unit
            }
        }
        // Being in range is not the same as having found the mark: it stands somewhere around the
        // hunter and may well be behind a wall or a tree, so say so rather than saying nothing.
        val (message, description) = if (state.inRange) {
            R.string.close_look_around to CLOSE_LOOK_AROUND_MESSAGE
        } else {
            R.string.nothing_to_catch_in_range to NOTHING_IN_RANGE_MESSAGE
        }
        Text(
            text = stringResource(message),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(12.dp)
                .semantics { contentDescription = description }
        )
        ImageButton(
            drawableId = R.drawable.catch_point,
            description = BIG_CATCH_BUTTON,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.7f), MaterialTheme.shapes.large)
                .width(0.4.dw),
            onClick = { viewModel.catchPoint() }
        )
    }
}
