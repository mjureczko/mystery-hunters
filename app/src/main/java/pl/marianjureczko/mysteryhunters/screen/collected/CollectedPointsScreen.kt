package pl.marianjureczko.mysteryhunters.screen.collected

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.screen.Screens
import pl.marianjureczko.mysteryhunters.ui.components.MyCard
import pl.marianjureczko.mysteryhunters.ui.components.OkDialog
import pl.marianjureczko.mysteryhunters.ui.components.TopBar

const val COLLECTED_POINTS_LIST = "Collected points list"
const val NOT_CAUGHT_INFO = "Not caught info"
const val COLLECTED_POINT_ROW = "Collected point"

@Composable
fun CollectedPointsScreen(navController: NavController) {
    val viewModel: CollectedPointsViewModel = hiltViewModel()
    val state = viewModel.state.value

    Scaffold(
        topBar = { TopBar(navController, stringResource(R.string.collected_points_title)) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                OkDialog(
                    visible = state.notCaughtInfoShown,
                    hideIt = { viewModel.hideNotCaughtInfo() },
                    text = stringResource(R.string.point_not_caught_yet),
                    description = NOT_CAUGHT_INFO
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = COLLECTED_POINTS_LIST }
                ) {
                    items(state.points, key = { it.id }) { point ->
                        CollectedPointRow(point) {
                            if (point.caught) {
                                navController.navigate(Screens.PointDetail.doRoute(viewModel.routeId, point.id))
                            } else {
                                viewModel.showNotCaughtInfo()
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun CollectedPointRow(point: PointOfInterest, onClick: () -> Unit) {
    MyCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .semantics { contentDescription = "$COLLECTED_POINT_ROW ${point.id}" }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.point_number, point.id),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(
                        if (point.caught) R.string.point_caught else R.string.point_not_caught
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (point.caught) {
                Image(
                    painter = painterResource(R.drawable.save_point),
                    contentDescription = null,
                    contentScale = ContentScale.Inside
                )
            }
        }
    }
}
