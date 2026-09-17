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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import pl.marianjureczko.mysteryhunters.usecase.CalculateMarkerPositionUC

const val AR_SCENE = "Augmented reality scene"

/**
 * Augmented reality view with a three dimensional question mark standing at the place configured
 * in the route.
 *
 * ARCore does not know where north is, so the mark is not anchored to world coordinates. Instead
 * its position is recomputed from the hunter's location and the direction the phone is held in,
 * which keeps it in the right place as the hunter turns around and needs no cloud anchors.
 */
@Composable
fun ArQuestionMark(markerPosition: CalculateMarkerPositionUC.Position?, modifier: Modifier = Modifier) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)

    ARSceneView(
        modifier = modifier.semantics { contentDescription = AR_SCENE },
        engine = engine,
        modelLoader = modelLoader,
        materialLoader = materialLoader,
        planeRenderer = false
    ) {
        if (markerPosition != null) {
            TextNode(
                text = "?",
                position = Float3(markerPosition.x, markerPosition.y, markerPosition.z)
            )
        }
    }
}
