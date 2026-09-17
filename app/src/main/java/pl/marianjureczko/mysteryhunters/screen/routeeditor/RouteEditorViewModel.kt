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

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.SpeechToTextPort
import pl.marianjureczko.mysteryhunters.screen.Screens
import pl.marianjureczko.mysteryhunters.usecase.AddPointOfInterestUC
import pl.marianjureczko.mysteryhunters.usecase.DeletePointOfInterestUC
import pl.marianjureczko.mysteryhunters.usecase.LoadRouteUC
import pl.marianjureczko.mysteryhunters.usecase.RenameRouteUC
import pl.marianjureczko.mysteryhunters.usecase.UpdatePointOfInterestUC
import javax.inject.Inject

@HiltViewModel
class RouteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadRouteUC: LoadRouteUC,
    private val renameRouteUC: RenameRouteUC,
    private val addPointOfInterestUC: AddPointOfInterestUC,
    private val updatePointOfInterestUC: UpdatePointOfInterestUC,
    private val deletePointOfInterestUC: DeletePointOfInterestUC,
    private val speechToTextPort: SpeechToTextPort
) : ViewModel() {

    private val routeId: Long = savedStateHandle[Screens.RouteEditor.PARAMETER_ROUTE_ID] ?: Screens.RouteEditor.NEW_ROUTE

    private val _state = mutableStateOf(RouteEditorState())
    val state: State<RouteEditorState> = _state

    init {
        if (routeId != Screens.RouteEditor.NEW_ROUTE) {
            viewModelScope.launch {
                loadRouteUC(routeId)?.let { route ->
                    _state.value = _state.value.copy(route = route, name = route.name)
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun saveName() {
        val name = _state.value.name
        if (name.isBlank()) {
            _state.value = _state.value.copy(messageId = R.string.name_required)
            return
        }
        viewModelScope.launch {
            val saved = renameRouteUC(_state.value.route, name)
            _state.value = _state.value.copy(route = saved, name = saved.name)
        }
    }

    /** A tap on the map either positions the point being edited or starts a new one. */
    fun onMapTapped(latitude: Double, longitude: Double) {
        if (_state.value.name.isBlank()) {
            _state.value = _state.value.copy(messageId = R.string.name_required)
            return
        }
        _state.value = _state.value.copy(
            pointEditorOpen = true,
            draftLatitude = latitude,
            draftLongitude = longitude
        )
    }

    fun editPoint(pointId: Int) {
        val point = _state.value.route.pointById(pointId) ?: return
        _state.value = _state.value.copy(
            pointEditorOpen = true,
            editedPointId = point.id,
            draftLatitude = point.latitude,
            draftLongitude = point.longitude,
            draftDescription = point.description,
            recognizedPartial = ""
        )
    }

    fun closePointEditor() {
        stopListening()
        _state.value = _state.value.copy(
            pointEditorOpen = false,
            editedPointId = null,
            draftLatitude = null,
            draftLongitude = null,
            draftDescription = "",
            recognizedPartial = ""
        )
    }

    fun onDescriptionChanged(description: String) {
        _state.value = _state.value.copy(draftDescription = description)
    }

    fun savePoint() {
        val current = _state.value
        val latitude = current.draftLatitude
        val longitude = current.draftLongitude
        if (latitude == null || longitude == null) {
            _state.value = current.copy(messageId = R.string.coordinates_required)
            return
        }
        stopListening()
        viewModelScope.launch {
            val editedId = current.editedPointId
            val saved = if (editedId == null) {
                addPointOfInterestUC(current.route.copy(name = current.name), latitude, longitude, current.draftDescription)
            } else {
                updatePointOfInterestUC(current.route.copy(name = current.name), editedId, latitude, longitude, current.draftDescription)
            }
            _state.value = _state.value.copy(
                route = saved,
                name = saved.name,
                pointEditorOpen = false,
                editedPointId = null,
                draftLatitude = null,
                draftLongitude = null,
                draftDescription = "",
                recognizedPartial = ""
            )
        }
    }

    fun askToDeletePoint(pointId: Int) {
        _state.value = _state.value.copy(pointToDelete = pointId)
    }

    fun cancelPointDeletion() {
        _state.value = _state.value.copy(pointToDelete = null)
    }

    fun confirmPointDeletion() {
        val pointId = _state.value.pointToDelete ?: return
        viewModelScope.launch {
            val saved = deletePointOfInterestUC(_state.value.route, pointId)
            _state.value = _state.value.copy(route = saved, pointToDelete = null)
            if (_state.value.editedPointId == pointId) {
                closePointEditor()
            }
        }
    }

    /**
     * Offline recognition is attempted first; when the engine or the language model is missing the
     * user is told and keeps using the always present text field.
     */
    fun startListening(languageTag: String) {
        viewModelScope.launch {
            if (!speechToTextPort.initialize(languageTag)) {
                _state.value = _state.value.copy(messageId = R.string.speech_unavailable, listening = false)
                return@launch
            }
            _state.value = _state.value.copy(listening = true, recognizedPartial = "")
            speechToTextPort.startListening(object : SpeechToTextPort.RecognitionListener {
                override fun onPartialResult(text: String) {
                    _state.value = _state.value.copy(recognizedPartial = text)
                }

                override fun onResult(text: String) {
                    if (text.isNotBlank()) {
                        val separator = if (_state.value.draftDescription.isBlank()) "" else " "
                        _state.value = _state.value.copy(
                            draftDescription = _state.value.draftDescription + separator + text
                        )
                    }
                    _state.value = _state.value.copy(recognizedPartial = "")
                }

                override fun onError(message: String) {
                    _state.value = _state.value.copy(
                        listening = false,
                        recognizedPartial = "",
                        messageId = R.string.speech_failed
                    )
                }
            })
        }
    }

    fun stopListening() {
        if (_state.value.listening) {
            speechToTextPort.stopListening()
            _state.value = _state.value.copy(listening = false, recognizedPartial = "")
        }
    }

    fun onMessageShown() {
        _state.value = _state.value.copy(messageId = null)
    }

    override fun onCleared() {
        super.onCleared()
        speechToTextPort.release()
    }
}
