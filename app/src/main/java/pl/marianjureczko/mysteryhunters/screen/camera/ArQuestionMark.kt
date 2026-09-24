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

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import android.media.Image
import com.google.ar.core.Camera
import com.google.ar.core.Config
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.scene.SceneUnderstanding
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.utils.readBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import kotlin.math.sqrt
import pl.marianjureczko.mysteryhunters.usecase.CalculateMarkerPositionUC
import kotlin.math.atan2

const val AR_SCENE = "Augmented reality scene"

/** Three metres tall and one metre across, yellow. Built by tools/question_mark_model.py. */
private const val QUESTION_MARK_MODEL = "question_mark.glb"

/** Height of the model, kept in step with tools/question_mark_model.py. */
private const val MARK_HEIGHT_IN_METERS = 3f

private const val NEAR_CLIP_IN_METERS = 0.1f
private const val FAR_CLIP_IN_METERS = 100f

/**
 * How much nearer than the mark a real thing has to be before it counts as standing in the way.
 * The depth ARCore works out from a moving camera is an estimate, and without a little room the
 * mark would flicker in and out against its own backdrop.
 */
private const val IN_THE_WAY_MARGIN_IN_METERS = 0.5f

/**
 * A depth sample is millimetres across all sixteen of its bits, so it is only the sign that has to
 * be undone when Kotlin widens the short. ARCore keeps how sure it is in an image of its own,
 * acquireRawDepthConfidenceImage, and packs nothing into the depth itself - masking off the top
 * bits, as the plain Android DEPTH16 format would want, silently wraps everything beyond 8.19
 * metres into a small reading and makes the whole world look like it is standing in the way.
 */
private const val DEPTH_MILLIMETRES_MASK = 0xFFFF

/** Reused every frame so that asking whether the mark can be seen allocates nothing. */
private class CameraMatrices {
    val projection = FloatArray(16)
    val view = FloatArray(16)
    val inPicture = FloatArray(2)
    val inDepthImage = FloatArray(2)
}

/**
 * Where augmented reality believes the camera stands in the world it is tracking. Kept outside of
 * the composition state on purpose: it is written on every rendered frame and nothing may redraw
 * because of it.
 */
/** Why the mark could or could not be seen, so that the log can say which it was. */
private enum class MarkSight { VISIBLE, OFF_SCREEN, BEHIND_SOMETHING, NOT_PLACED, NOT_TRACKING }

/** Where the mark was last put, so that the frame loop can ask whether it can be seen. */
private class MarkInWorld {
    @Volatile
    var placed: Boolean = false

    @Volatile
    var x: Float = 0f

    @Volatile
    var y: Float = 0f

    @Volatile
    var z: Float = 0f
}

private class CameraInWorld {
    @Volatile
    var x: Float = 0f

    @Volatile
    var y: Float = 0f

    @Volatile
    var z: Float = 0f
}

/**
 * Augmented reality view with a three dimensional question mark standing at the place configured
 * in the route.
 *
 * ARCore does not know where north is, so the mark cannot be anchored to a place on Earth without
 * cloud anchors. It is anchored to the world that augmented reality tracks instead, which is
 * enough: the compass says where north lies in that world once, when the scene starts, and the
 * tracking keeps the mark where it was put as the hunter turns.
 *
 * The mark is placed relative to where the tracking currently has the camera, not relative to the
 * corner of the world it started from. The hunter's own position comes from the satellites, but
 * between two fixes it is the tracking that follows the walking, and measuring from the corner of
 * the world would count every step twice - once in the distance the satellites report and once in
 * the camera the tracking has already moved.
 */
@Composable
fun ArQuestionMark(
    markerPosition: CalculateMarkerPositionUC.Position?,
    onCameraHeadingInScene: (headingDegrees: Float, tracking: Boolean) -> Unit,
    onMarkVisible: (visible: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val questionMark = rememberQuestionMark(modelLoader)
    val camera = remember { CameraInWorld() }
    val mark = remember { MarkInWorld() }
    val frustum = remember { ViewFrustum() }
    val matrices = remember { CameraMatrices() }
    // TEMPORARY, see ArSceneLog.
    val sceneLog = remember { ArSceneLog() }

    ARSceneView(
        modifier = modifier.semantics { contentDescription = AR_SCENE },
        engine = engine,
        modelLoader = modelLoader,
        materialLoader = materialLoader,
        planeRenderer = false,
        // Real world depth, so that the mark is hidden by whatever stands in front of it instead
        // of being painted over the top of everything. SceneView does not turn the depth mode on
        // by itself; without it the camera stream quietly falls back to its flat material and the
        // occlusion flag below does nothing.
        depthMode = Config.DepthMode.AUTOMATIC,
        sceneUnderstanding = SceneUnderstanding(
            occlusion = true,
            lighting = true,
            physics = false,
            planeVisualization = false
        ),
        onConfigDowngraded = { downgrade -> Log.w("ArScene", "ARCore downgraded: $downgrade") },
        onSessionUpdated = { session, frame ->
            val pose = frame.camera.pose
            camera.x = pose.tx()
            camera.y = pose.ty()
            camera.z = pose.tz()
            val tracking = frame.camera.trackingState == TrackingState.TRACKING
            onCameraHeadingInScene(headingInScene(pose.zAxis), tracking)
            val sight = when {
                !tracking -> MarkSight.NOT_TRACKING
                !mark.placed -> MarkSight.NOT_PLACED
                else -> sight(frustum, matrices, frame, mark, sceneLog)
            }
            onMarkVisible(sight == MarkSight.VISIBLE)
            // TEMPORARY, see ArSceneLog.
            sceneLog.sight = sight.name
            sceneLog.onFrame(session, frame, camera.x, camera.y, camera.z)
        }
    ) {
        if (markerPosition != null && questionMark != null) {
            val inWorld = Float3(
                camera.x + markerPosition.x,
                camera.y + markerPosition.y,
                camera.z + markerPosition.z
            )
            mark.x = inWorld.x
            mark.y = inWorld.y
            mark.z = inWorld.z
            mark.placed = true
            // TEMPORARY, see ArSceneLog.
            sceneLog.markInWorldX = inWorld.x
            sceneLog.markInWorldY = inWorld.y
            sceneLog.markInWorldZ = inWorld.z
            ModelNode(
                modelInstance = questionMark,
                position = inWorld,
                // The character is flat, so a hunter coming at it from the side would see a bare
                // yellow line. Turning it about the upright axis keeps it readable from wherever
                // the hunter happens to stand. Only this one axis turns, so the mark goes on
                // standing upright instead of tipping over like a signboard.
                rotation = Float3(0f, facingTheHunter(markerPosition), 0f)
            )
        }
    }
}

/**
 * Whether the mark is there to be seen: inside the picture, with nothing real standing in front of
 * it.
 *
 * Its foot, its middle and its head are all tried, because a mark three metres tall can easily
 * stand with its foot below the picture, or with its foot behind a parked car while its head is in
 * plain view. Any one part that can be seen is enough to count as seen.
 */
private fun sight(
    frustum: ViewFrustum,
    matrices: CameraMatrices,
    frame: Frame,
    mark: MarkInWorld,
    sceneLog: ArSceneLog
): MarkSight {
    val camera = frame.camera
    camera.getProjectionMatrix(matrices.projection, 0, NEAR_CLIP_IN_METERS, FAR_CLIP_IN_METERS)
    camera.getViewMatrix(matrices.view, 0)
    val depthImage = acquireDepth(frame)
    var anyOnScreen = false
    try {
        for (height in listOf(0f, MARK_HEIGHT_IN_METERS / 2, MARK_HEIGHT_IN_METERS)) {
            val y = mark.y + height
            if (!frustum.holds(matrices.projection, matrices.view, mark.x, y, mark.z)) {
                continue
            }
            anyOnScreen = true
            if (depthImage == null) {
                return MarkSight.VISIBLE
            }
            val awayInMeters = distanceFromCamera(camera, mark.x, y, mark.z)
            val realWorldInMeters = realWorldAt(frame, matrices, depthImage, frustum)
            // TEMPORARY, see ArSceneLog.
            sceneLog.markAwayInMeters = awayInMeters
            sceneLog.realWorldAwayInMeters = realWorldInMeters
            val clear = realWorldInMeters <= 0f ||
                    realWorldInMeters >= awayInMeters - IN_THE_WAY_MARGIN_IN_METERS
            if (clear) {
                return MarkSight.VISIBLE
            }
        }
        return if (anyOnScreen) MarkSight.BEHIND_SOMETHING else MarkSight.OFF_SCREEN
    } finally {
        depthImage?.close()
    }
}

private fun acquireDepth(frame: Frame): Image? = try {
    frame.acquireDepthImage16Bits()
} catch (notReady: NotYetAvailableException) {
    // ARCore has not worked out the depth of this frame yet. Nothing has been seen standing in
    // the way, so nothing is treated as standing in the way.
    null
}

private fun distanceFromCamera(camera: Camera, x: Float, y: Float, z: Float): Float {
    val pose = camera.pose
    val acrossX = x - pose.tx()
    val acrossY = y - pose.ty()
    val acrossZ = z - pose.tz()
    return sqrt(acrossX * acrossX + acrossY * acrossY + acrossZ * acrossZ)
}

/**
 * How far away the real world is at this spot in the picture, in metres, or zero where ARCore
 * could not measure it.
 *
 * Nothing measured is taken to mean nothing was seen there rather than something was: beyond a
 * handful of metres the depth of a scene worked out from a moving camera runs out, and refusing
 * every distant point would leave most of the catching range unplayable.
 */
private fun realWorldAt(
    frame: Frame,
    matrices: CameraMatrices,
    depthImage: Image,
    frustum: ViewFrustum
): Float {
    matrices.inPicture[0] = frustum.pictureX
    matrices.inPicture[1] = frustum.pictureY
    frame.transformCoordinates2d(
        Coordinates2d.VIEW_NORMALIZED,
        matrices.inPicture,
        Coordinates2d.TEXTURE_NORMALIZED,
        matrices.inDepthImage
    )
    val column = (matrices.inDepthImage[0] * depthImage.width).toInt()
        .coerceIn(0, depthImage.width - 1)
    val row = (matrices.inDepthImage[1] * depthImage.height).toInt()
        .coerceIn(0, depthImage.height - 1)
    val plane = depthImage.planes[0]
    val samples = plane.buffer.order(ByteOrder.nativeOrder())
    val sample = samples.getShort(row * plane.rowStride + column * plane.pixelStride).toInt()
    return (sample and DEPTH_MILLIMETRES_MASK) / 1000f
}

/**
 * Reads the model out of the assets and hands it to Filament.
 *
 * SceneView ships rememberModelInstance for this, but it builds the model on whichever thread the
 * composition happens to run its effects on. In the application that is the main thread and all is
 * well; under an instrumented test the composition brings its own dispatcher, Filament finds itself
 * called from a thread it has not adopted, and the whole process is brought down with an abort that
 * no amount of catching will stop. The main thread is therefore asked for by name.
 */
@Composable
private fun rememberQuestionMark(modelLoader: ModelLoader): ModelInstance? {
    val context = LocalContext.current
    val instance = produceState<ModelInstance?>(initialValue = null, key1 = modelLoader) {
        val buffer = withContext(Dispatchers.IO) {
            runCatching { context.assets.readBuffer(QUESTION_MARK_MODEL) }.getOrNull()
        } ?: return@produceState
        value = withContext(Dispatchers.Main) {
            runCatching { modelLoader.createModelInstance(buffer) }.getOrNull()
        }
    }.value
    DisposableEffect(instance) {
        onDispose { instance?.let { modelLoader.destroyModel(it.asset) } }
    }
    return instance
}

/**
 * Which way the camera is pointed within the scene, in degrees clockwise from straight ahead, so
 * that the hunt can work out how the scene is turned against the compass.
 *
 * @param cameraZAxis the +Z axis of the camera in world coordinates; the camera looks the other
 *        way, along -Z, which is why both components come in negated.
 */
private fun headingInScene(cameraZAxis: FloatArray): Float {
    val degrees = Math.toDegrees(
        atan2(-cameraZAxis[0].toDouble(), cameraZAxis[2].toDouble())
    ).toFloat()
    return (degrees + 360f) % 360f
}

/**
 * Degrees to turn the mark about the upright axis so that its face points back at the camera.
 * The model faces +Z when it is not turned at all, and [markerPosition] is measured from the
 * camera, so the mark has to look back down that line.
 */
private fun facingTheHunter(markerPosition: CalculateMarkerPositionUC.Position): Float =
    Math.toDegrees(atan2(-markerPosition.x.toDouble(), -markerPosition.z.toDouble())).toFloat()
