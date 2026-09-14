package pl.marianjureczko.mysteryhunters.screen.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARScene
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

    ARScene(
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
