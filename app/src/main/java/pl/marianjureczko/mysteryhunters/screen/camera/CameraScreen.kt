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
import androidx.hilt.navigation.compose.hiltViewModel
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
const val TOO_FAR_DIALOG = "Too far dialog"
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
        OkDialog(
            visible = state.tooFarMessageShown,
            hideIt = { viewModel.hideTooFarMessage() },
            text = stringResource(R.string.too_far_to_catch),
            description = TOO_FAR_DIALOG
        )
        if (cameraPermission.status.isGranted) {
            if (state.arAvailable) {
                ArQuestionMark(state.markerPosition, Modifier.fillMaxSize())
            } else {
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
        }
        if (!state.inRange) {
            Text(
                text = stringResource(R.string.nothing_to_catch_in_range),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(12.dp)
                    .semantics { contentDescription = NOTHING_IN_RANGE_MESSAGE }
            )
        }
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
