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

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.ArAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.DeviceOrientationPort
import pl.marianjureczko.mysteryhunters.screen.Screens
import pl.marianjureczko.mysteryhunters.usecase.CalculateBearingUC
import pl.marianjureczko.mysteryhunters.usecase.CalculateMarkerPositionUC
import pl.marianjureczko.mysteryhunters.usecase.CatchPointUC
import pl.marianjureczko.mysteryhunters.usecase.IsPointInCatchRangeUC
import pl.marianjureczko.mysteryhunters.usecase.LoadRouteUC
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import pl.marianjureczko.poszukiwacz.compass.api.LocationCalculator
import pl.marianjureczko.poszukiwacz.compass.api.LocationPort
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadRouteUC: LoadRouteUC,
    private val catchPointUC: CatchPointUC,
    private val isPointInCatchRangeUC: IsPointInCatchRangeUC,
    private val calculateBearingUC: CalculateBearingUC,
    private val calculateMarkerPositionUC: CalculateMarkerPositionUC,
    private val locationCalculator: LocationCalculator,
    private val locationPort: LocationPort,
    private val deviceOrientationPort: DeviceOrientationPort,
    private val arAvailabilityPort: ArAvailabilityPort
) : ViewModel() {

    private val routeId: Long = savedStateHandle[Screens.Camera.PARAMETER_ROUTE_ID] ?: Route.NOT_PERSISTED
    private val pointId: Int? = savedStateHandle[Screens.Camera.PARAMETER_POINT_ID]

    private val _state = mutableStateOf(CameraState())
    val state: State<CameraState> = _state

    private var route: Route? = null
    private var hunterLocation: AndroidLocation? = null
    private var azimuthDegrees: Float = 0f
    private var started = false

    fun start() {
        if (started) {
            return
        }
        started = true
        _state.value = _state.value.copy(arAvailable = arAvailabilityPort.isArAvailable())
        viewModelScope.launch {
            val loaded = loadRouteUC(routeId) ?: return@launch
            route = loaded
            _state.value = _state.value.copy(point = loaded.pointById(pointId))
        }
        locationPort.startFetching(viewModelScope) { location ->
            hunterLocation = location
            recalculate()
        }
        deviceOrientationPort.startListening { azimuth ->
            azimuthDegrees = azimuth
            recalculate()
        }
    }

    private fun recalculate() {
        val point = _state.value.point ?: return
        val location = hunterLocation ?: return
        val inRange = isPointInCatchRangeUC(point, location)
        _state.value = _state.value.copy(
            inRange = inRange,
            markerPosition = if (inRange) markerPosition(point, location) else null
        )
    }

    private fun markerPosition(
        point: PointOfInterest,
        location: AndroidLocation
    ): CalculateMarkerPositionUC.Position {
        val target = AndroidLocation.create(point.latitude, point.longitude)
        return calculateMarkerPositionUC(
            bearingDegrees = calculateBearingUC(point, location),
            azimuthDegrees = azimuthDegrees,
            distanceInMeters = locationCalculator.distanceInMeters(target, location)
        )
    }

    /**
     * The big button at the bottom of the screen. It only succeeds while the question mark is
     * visible, otherwise the hunter is told they are too far away.
     */
    fun catchPoint() {
        val point = _state.value.point ?: return
        val currentRoute = route ?: return
        if (!_state.value.inRange) {
            _state.value = _state.value.copy(tooFarMessageShown = true)
            return
        }
        viewModelScope.launch {
            catchPointUC(currentRoute, point.id)
            _state.value = _state.value.copy(caughtPointId = point.id)
        }
    }

    fun hideTooFarMessage() {
        _state.value = _state.value.copy(tooFarMessageShown = false)
    }

    override fun onCleared() {
        super.onCleared()
        locationPort.stopFetching()
        deviceOrientationPort.stopListening()
    }
}
