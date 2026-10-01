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

package pl.marianjureczko.mysteryhunters.screen.routeeditor

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.ui.Screen.dh
import pl.marianjureczko.mysteryhunters.ui.components.ImageButton
import pl.marianjureczko.mysteryhunters.ui.components.MyCard
import pl.marianjureczko.mysteryhunters.ui.components.TopBar
import pl.marianjureczko.mysteryhunters.ui.components.YesNoDialog
import java.util.Locale

const val ROUTE_NAME_FIELD = "Route name"
const val SAVE_ROUTE_NAME_BUTTON = "Save route name"
const val POINT_DESCRIPTION_FIELD = "Point description"
const val POINT_EDITOR_CARD = "Point editor card"
const val SAVE_POINT_BUTTON = "Save point"
const val CLOSE_POINT_EDITOR_BUTTON = "Close point editor"
const val MICROPHONE_BUTTON = "Dictate description"
const val STOP_MICROPHONE_BUTTON = "Stop dictating"
const val SPEECH_PREPARING_INDICATOR = "Preparing speech recognition"
const val EDIT_POINT_BUTTON = "Edit point"
const val DELETE_POINT_BUTTON = "Delete point"
const val POINTS_LIST = "Points list"

private const val MAP_WEIGHT = 2f
private const val MAP_MIN_HEIGHT = 0.34f
private const val LIST_WEIGHT = 1f

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RouteEditorScreen(navController: NavController) {
    val viewModel: RouteEditorViewModel = hiltViewModel()
    val state = viewModel.state.value
    val context = LocalContext.current
    val microphonePermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    state.messageId?.let { messageId ->
        LaunchedEffect(messageId) {
            Toast.makeText(context, messageId, Toast.LENGTH_LONG).show()
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        topBar = { TopBar(navController, stringResource(R.string.route_editor_title)) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 8.dp)
            ) {
                YesNoDialog(
                    visible = state.pointToDelete != null,
                    hideIt = { viewModel.cancelPointDeletion() },
                    text = stringResource(R.string.delete_point_msg, state.pointToDelete ?: 0),
                    onConfirm = { viewModel.confirmPointDeletion() }
                )
                RouteNameRow(
                    name = state.name,
                    onNameChanged = { viewModel.onNameChanged(it) },
                    onSaveName = { viewModel.saveName() }
                )
                PointsList(
                    points = state.route.pointsOfInterest,
                    onEdit = { viewModel.editPoint(it) },
                    onDelete = { viewModel.askToDeletePoint(it) },
                    modifier = Modifier.weight(LIST_WEIGHT)
                )
                if (state.pointEditorOpen) {
                    PointEditor(
                        state = state,
                        onDescriptionChanged = { viewModel.onDescriptionChanged(it) },
                        onSave = { viewModel.savePoint() },
                        onClose = { viewModel.closePointEditor() },
                        onStartListening = {
                            if (microphonePermission.status.isGranted) {
                                viewModel.startListening(Locale.getDefault().language)
                            } else {
                                microphonePermission.launchPermissionRequest()
                            }
                        },
                        onStopListening = { viewModel.stopListening() }
                    )
                } else {
                    Text(
                        text = stringResource(R.string.tap_map_to_add_point),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                OpenStreetMap(
                    points = state.route.pointsOfInterest,
                    draftLatitude = state.draftLatitude,
                    draftLongitude = state.draftLongitude,
                    editedPointId = state.editedPointId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(MAP_WEIGHT)
                        .layout { measurable, constraints ->
                            // The weighted share can shrink under the keyboard; the map keeps at
                            // least MAP_MIN_HEIGHT and the surplus is hidden behind the keyboard.
                            val height = constraints.maxHeight.coerceAtLeast(MAP_MIN_HEIGHT.dh.roundToPx())
                            val placeable = measurable.measure(
                                constraints.copy(minHeight = height, maxHeight = height)
                            )
                            layout(constraints.maxWidth, height) { placeable.placeRelative(0, 0) }
                        },
                    onMapTapped = { latitude, longitude -> viewModel.onMapTapped(latitude, longitude) }
                )
            }
        }
    )
}

@Composable
private fun RouteNameRow(name: String, onNameChanged: (String) -> Unit, onSaveName: () -> Unit) {
    var editing by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                editing = true
                onNameChanged(it)
            },
            singleLine = true,
            label = { Text(stringResource(R.string.route_name_label)) },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { if (it.isFocused) editing = true }
                .semantics { contentDescription = ROUTE_NAME_FIELD }
        )
        val spaceForButton = 48.dp
        if (editing) {
            Box(modifier = Modifier.size(spaceForButton), contentAlignment = Alignment.Center) {
                ImageButton(R.drawable.save_point, SAVE_ROUTE_NAME_BUTTON, onClick = {
                    editing = false
                    keyboard?.hide()
                    onSaveName()
                })
            }
        } else {
            Spacer(modifier = Modifier.size(spaceForButton))
        }
    }
}

@Composable
private fun PointEditor(
    state: RouteEditorState,
    onDescriptionChanged: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val coordinates = stringResource(
        R.string.coordinates,
        state.draftLatitude ?: 0.0,
        state.draftLongitude ?: 0.0
    )
    MyCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = POINT_EDITOR_CARD
                stateDescription = coordinates
            }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(R.string.point_number, state.editedPointDisplayId),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.draftDescription,
                    onValueChange = onDescriptionChanged,
                    label = { Text(stringResource(R.string.description_label)) },
                    supportingText = {
                        if (state.preparingSpeech) {
                            Text(stringResource(R.string.preparing_speech))
                        } else if (state.listening) {
                            Text(state.recognizedPartial.ifBlank { stringResource(R.string.listening) })
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = POINT_DESCRIPTION_FIELD }
                )
                // The spinner takes the same room as the icons it replaces so the row does not jump.
                if (state.preparingSpeech) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .size(48.dp)
                            .semantics { contentDescription = SPEECH_PREPARING_INDICATOR },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                } else if (state.listening) {
                    ImageButton(R.drawable.stop_listening, STOP_MICROPHONE_BUTTON, onClick = onStopListening)
                } else {
                    ImageButton(R.drawable.microphone, MICROPHONE_BUTTON, onClick = onStartListening)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImageButton(R.drawable.keyboard_input, CLOSE_POINT_EDITOR_BUTTON, onClick = {
                    keyboard?.hide()
                    onClose()
                })
                ImageButton(
                    drawableId = R.drawable.save_point,
                    description = SAVE_POINT_BUTTON,
                    enabled = state.canSavePoint,
                    onClick = {
                        keyboard?.hide()
                        onSave()
                    }
                )
            }
        }
    }
}

@Composable
private fun PointsList(
    points: List<PointOfInterest>,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = POINTS_LIST }
    ) {
        items(points, key = { it.id }) { point ->
            MyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.point_number, point.id),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = point.description.ifBlank { stringResource(R.string.no_description) },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2
                        )
                    }
                    ImageButton(R.drawable.edit_point, "$EDIT_POINT_BUTTON ${point.id}") { onEdit(point.id) }
                    ImageButton(R.drawable.delete_point, "$DELETE_POINT_BUTTON ${point.id}") { onDelete(point.id) }
                }
            }
        }
    }
}
