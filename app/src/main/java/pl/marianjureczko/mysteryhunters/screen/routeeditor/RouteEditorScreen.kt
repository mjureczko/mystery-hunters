package pl.marianjureczko.mysteryhunters.screen.routeeditor

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.ui.Screen.dh
import pl.marianjureczko.mysteryhunters.ui.components.ImageButton
import pl.marianjureczko.mysteryhunters.ui.components.MyCard
import pl.marianjureczko.mysteryhunters.ui.components.TopBar
import pl.marianjureczko.mysteryhunters.ui.components.YesNoDialog
import java.util.Locale

const val ROUTE_NAME_FIELD = "Route name"
const val SAVE_ROUTE_NAME_BUTTON = "Save route name"
const val POINT_DESCRIPTION_FIELD = "Point description"
const val POINT_COORDINATES_LABEL = "Point coordinates"
const val SAVE_POINT_BUTTON = "Save point"
const val CLOSE_POINT_EDITOR_BUTTON = "Close point editor"
const val MICROPHONE_BUTTON = "Dictate description"
const val STOP_MICROPHONE_BUTTON = "Stop dictating"
const val EDIT_POINT_BUTTON = "Edit point"
const val DELETE_POINT_BUTTON = "Delete point"
const val POINTS_LIST = "Points list"

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
                OpenStreetMap(
                    points = state.route.pointsOfInterest,
                    draftLatitude = state.draftLatitude,
                    draftLongitude = state.draftLongitude,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.35.dh),
                    onMapTapped = { latitude, longitude -> viewModel.onMapTapped(latitude, longitude) }
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
                PointsList(
                    points = state.route.pointsOfInterest,
                    onEdit = { viewModel.editPoint(it) },
                    onDelete = { viewModel.askToDeletePoint(it) }
                )
            }
        }
    )
}

@Composable
private fun RouteNameRow(name: String, onNameChanged: (String) -> Unit, onSaveName: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            singleLine = true,
            label = { Text(stringResource(R.string.route_name_label)) },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = ROUTE_NAME_FIELD }
        )
        ImageButton(R.drawable.save_point, SAVE_ROUTE_NAME_BUTTON, onClick = onSaveName)
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
    MyCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(R.string.point_number, state.editedPointDisplayId),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(
                    R.string.coordinates,
                    state.draftLatitude ?: 0.0,
                    state.draftLongitude ?: 0.0
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { contentDescription = POINT_COORDINATES_LABEL }
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.draftDescription,
                    onValueChange = onDescriptionChanged,
                    label = { Text(stringResource(R.string.description_label)) },
                    supportingText = {
                        if (state.listening) {
                            Text(state.recognizedPartial.ifBlank { stringResource(R.string.listening) })
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = POINT_DESCRIPTION_FIELD }
                )
                if (state.listening) {
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
                ImageButton(R.drawable.keyboard_input, CLOSE_POINT_EDITOR_BUTTON, onClick = onClose)
                ImageButton(
                    drawableId = R.drawable.save_point,
                    description = SAVE_POINT_BUTTON,
                    enabled = state.canSavePoint,
                    onClick = onSave
                )
            }
        }
    }
}

@Composable
private fun PointsList(points: List<PointOfInterest>, onEdit: (Int) -> Unit, onDelete: (Int) -> Unit) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .semantics { contentDescription = POINTS_LIST }) {
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
