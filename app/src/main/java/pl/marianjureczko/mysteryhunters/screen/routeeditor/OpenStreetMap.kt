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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.model.PointOfInterest

const val ROUTE_MAP = "Route map"

private const val DEFAULT_ZOOM = 16.0
private val DEFAULT_CENTER = GeoPoint(52.2297, 21.0122)

/**
 * OpenStreetMap based map, used to place and move the points of a route. osmdroid needs no access
 * token, which keeps the app free of proprietary map credentials.
 */
@Composable
fun OpenStreetMap(
    points: List<PointOfInterest>,
    draftLatitude: Double?,
    draftLongitude: Double?,
    modifier: Modifier = Modifier,
    onMapTapped: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val pinIcon = remember { ContextCompat.getDrawable(context, R.drawable.point_pin) }
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(DEFAULT_ZOOM)
            controller.setCenter(DEFAULT_CENTER)
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    AndroidView(
        modifier = modifier
            .clipToBounds()
            .semantics { contentDescription = ROUTE_MAP },
        factory = { mapView },
        update = { view ->
            view.overlays.clear()
            view.overlays.add(
                MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(geoPoint: GeoPoint?): Boolean {
                        geoPoint?.let { onMapTapped(it.latitude, it.longitude) }
                        return true
                    }

                    override fun longPressHelper(geoPoint: GeoPoint?): Boolean = false
                })
            )
            points.forEach { point ->
                view.overlays.add(
                    Marker(view).apply {
                        position = GeoPoint(point.latitude, point.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = point.id.toString()
                        pinIcon?.let { icon = it }
                    }
                )
            }
            if (draftLatitude != null && draftLongitude != null) {
                val draft = GeoPoint(draftLatitude, draftLongitude)
                view.overlays.add(
                    Marker(view).apply {
                        position = draft
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        pinIcon?.let { icon = it }
                    }
                )
                view.controller.animateTo(draft)
            } else {
                points.firstOrNull()?.let {
                    view.controller.setCenter(GeoPoint(it.latitude, it.longitude))
                }
            }
            view.invalidate()
        }
    )
}
