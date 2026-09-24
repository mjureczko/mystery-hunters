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

import android.hardware.GeomagneticField
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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

    companion object {
        /**
         * How much of each fresh reading is taken into the running average of the offset. Small,
         * because a magnetometer is noisy and the offset it is measuring barely moves: what drifts
         * is the heading augmented reality keeps of itself, and that drifts slowly.
         */
        const val NORTH_OFFSET_SMOOTHING = 0.05f

        /**
         * The mark is only put somewhere else once the running average has moved this far. Without
         * it every reading would nudge the mark and it would never sit still. Two degrees is about
         * half a metre at the far end of the catching range.
         */
        const val MIN_NORTH_OFFSET_CHANGE_IN_DEGREES = 2.0f

        private const val FULL_CIRCLE_IN_DEGREES = 360f
        private const val HALF_CIRCLE_IN_DEGREES = 180f
    }

    private var route: Route? = null
    private var hunterLocation: AndroidLocation? = null

    /**
     * Which compass bearing straight ahead in the augmented reality scene corresponds to.
     *
     * The mark is put at a position in the world that augmented reality tracks, and that world does
     * not turn when the hunter turns - the tracking follows the camera through it. Feeding the
     * placement the compass reading of the moment would count every turn twice, once in the
     * position of the mark and once in the camera, and send the mark swinging away the wrong way.
     *
     * What is needed instead is the angle between the two headings: what the compass says the
     * camera is pointed at, against what augmented reality believes the camera is pointed at
     * inside its own world. That angle is a property of the world, not of how the hunter is
     * standing, so it stays put while the hunter turns. It is measured continuously rather than
     * taken once, because taking it once is only right at the instant the world is built, and
     * anything that rebuilds either side - the scene restarting, this screen being recreated -
     * silently leaves the mark pointing somewhere wrong.
     *
     * Averaged as a point on a circle rather than as a number, so that readings either side of
     * north do not average to south.
     */
    private var northOffsetSin = 0.0
    private var northOffsetCos = 0.0
    private var northOffsetKnown = false

    /** The offset the mark was last placed with, so it is only moved when the average has shifted. */
    private var placedWithNorthOffset: Float? = null

    private var compassHeadingDegrees: Float? = null

    /**
     * How far magnetic north lies from true north where the hunter stands, in degrees to the east.
     *
     * The compass reports headings from magnetic north, while the bearing to a point is worked out
     * on the globe and so counts from true north. In Poland the two are some five degrees apart,
     * which at the far end of the catching range is well over a metre; nearer the poles it is far
     * worse. Recomputed with every position fix, which is as often as it could possibly matter.
     */
    private var declinationDegrees = 0f
    private var cameraHeadingInSceneDegrees: Float? = null

    /**
     * Where the point lies and how far away it is depends on where the hunter stands, not on which
     * way the phone is pointed. Both are kept from the last position fix instead of being measured
     * again for every compass reading.
     */
    private var bearingDegrees: Float? = null
    private var distanceInMeters: Float? = null

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
            // The position fix may well have arrived before the route was read from the database.
            hunterLocation?.let { onHunterMoved() }
        }
        locationPort.startFetching(viewModelScope) { location ->
            hunterLocation = location
            onHunterMoved()
        }
        deviceOrientationPort.startListening { azimuth ->
            compassHeadingDegrees = azimuth
            updateNorthOffset()
        }
    }

    /**
     * Where the augmented reality scene currently has the camera pointed, within its own world,
     * and whether it is tracking well enough for that to mean anything. Called once per drawn
     * frame; it only reaches the state when the mark has to move.
     */
    fun onCameraHeadingInScene(headingDegrees: Float, tracking: Boolean) {
        // A scene that has lost tracking reports a heading that means nothing, so it is not let
        // into the average - unless nothing has been measured yet. Before tracking starts the scene
        // still sits at its starting heading, which is straight ahead, and taking the compass at
        // face value against it is exactly right at that moment. That way the mark is there from
        // the first second, and the average corrects it as soon as tracking has something to say.
        if (!tracking && northOffsetKnown) {
            return
        }
        cameraHeadingInSceneDegrees = headingDegrees
        updateNorthOffset()
    }

    private fun updateNorthOffset() {
        val compass = compassHeadingDegrees ?: return
        val inScene = cameraHeadingInSceneDegrees ?: return
        val measured = Math.toRadians(normalised(compass + declinationDegrees - inScene).toDouble())
        val measuredSin = sin(measured)
        val measuredCos = cos(measured)
        if (!northOffsetKnown) {
            northOffsetKnown = true
            northOffsetSin = measuredSin
            northOffsetCos = measuredCos
        } else {
            northOffsetSin += NORTH_OFFSET_SMOOTHING * (measuredSin - northOffsetSin)
            northOffsetCos += NORTH_OFFSET_SMOOTHING * (measuredCos - northOffsetCos)
        }
        val placedWith = placedWithNorthOffset
        if (placedWith == null ||
            degreesBetween(northOffsetDegrees(), placedWith) >= MIN_NORTH_OFFSET_CHANGE_IN_DEGREES
        ) {
            publishMarkerPosition(_state.value.inRange)
        }
    }

    private fun northOffsetDegrees(): Float =
        normalised(Math.toDegrees(atan2(northOffsetSin, northOffsetCos)).toFloat())

    private fun normalised(degrees: Float): Float =
        (degrees % FULL_CIRCLE_IN_DEGREES + FULL_CIRCLE_IN_DEGREES) % FULL_CIRCLE_IN_DEGREES

    /** The shorter of the two ways around the circle, so that 359 degrees is next to 1 degree. */
    private fun degreesBetween(one: Float, other: Float): Float {
        val difference = abs(one - other) % FULL_CIRCLE_IN_DEGREES
        return if (difference > HALF_CIRCLE_IN_DEGREES) FULL_CIRCLE_IN_DEGREES - difference else difference
    }

    private fun onHunterMoved() {
        val point = _state.value.point ?: return
        val location = hunterLocation ?: return
        val target = AndroidLocation.create(point.latitude, point.longitude)
        declinationDegrees = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            0f,
            System.currentTimeMillis()
        ).declination
        bearingDegrees = calculateBearingUC(point, location)
        distanceInMeters = locationCalculator.distanceInMeters(target, location)
        publishMarkerPosition(isPointInCatchRangeUC(point, location))
    }

    private fun publishMarkerPosition(inRange: Boolean) {
        val bearing = bearingDegrees
        val distance = distanceInMeters
        val northOffset = if (northOffsetKnown) northOffsetDegrees() else null
        val markerPosition = if (inRange && bearing != null && distance != null && northOffset != null) {
            calculateMarkerPositionUC(
                bearingDegrees = bearing,
                forwardBearingDegrees = northOffset,
                distanceInMeters = distance
            )
        } else {
            null
        }
        val current = _state.value
        if (current.inRange == inRange && current.markerPosition == markerPosition) {
            return
        }
        placedWithNorthOffset = northOffset
        _state.value = current.copy(inRange = inRange, markerPosition = markerPosition)
        // TEMPORARY diagnostic.
        Log.i(
            "ArMarker",
            "here=${hunterLocation?.latitude},${hunterLocation?.longitude} bearing=$bearing " +
                    "northOffset=$northOffset distance=$distance inRange=$inRange marker=$markerPosition"
        )
    }

    /** Reported by the scene once a frame; only reaches the state when the answer changes. */
    fun onMarkVisible(visible: Boolean) {
        if (_state.value.markVisible != visible) {
            _state.value = _state.value.copy(markVisible = visible)
        }
    }

    /**
     * The big button at the bottom of the screen. Standing close enough is not by itself enough:
     * the mark has to be on the screen to be caught, so that the hunt is a matter of looking around
     * rather than of pressing a button in the right postcode.
     */
    fun catchPoint() {
        val point = _state.value.point ?: return
        val currentRoute = route ?: return
        if (!_state.value.inRange) {
            _state.value = _state.value.copy(catchRefusal = CatchRefusal.TOO_FAR)
            return
        }
        // Only the augmented reality scene can say whether the mark can be seen. Without it the
        // hunt draws the mark flat in the middle of the picture, where it always can be.
        if (_state.value.arAvailable == true && !_state.value.markVisible) {
            _state.value = _state.value.copy(catchRefusal = CatchRefusal.MARK_NOT_VISIBLE)
            return
        }
        viewModelScope.launch {
            catchPointUC(currentRoute, point.id)
            _state.value = _state.value.copy(caughtPointId = point.id)
        }
    }

    fun hideCatchRefusal() {
        _state.value = _state.value.copy(catchRefusal = null)
    }

    override fun onCleared() {
        super.onCleared()
        // The position is not stopped here: the port is shared with the compass screen and this
        // screen has no business silencing it. The listener registered in start() goes away with
        // viewModelScope, which has just been cancelled. See SharedLocationPort.
        deviceOrientationPort.stopListening()
    }
}
