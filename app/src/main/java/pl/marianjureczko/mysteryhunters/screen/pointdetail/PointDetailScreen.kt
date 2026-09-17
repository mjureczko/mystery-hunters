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

package pl.marianjureczko.mysteryhunters.screen.pointdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.ui.components.MyCard
import pl.marianjureczko.mysteryhunters.ui.components.TopBar

const val POINT_DESCRIPTION_LABEL = "Point description text"

@Composable
fun PointDetailScreen(navController: NavController) {
    val viewModel: PointDetailViewModel = hiltViewModel()
    val point = viewModel.state.value.point

    Scaffold(
        topBar = {
            TopBar(
                navController,
                point?.let { stringResource(R.string.point_number, it.id) }
                    ?: stringResource(R.string.point_detail_title)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = point?.description?.ifBlank { stringResource(R.string.no_description) }
                            ?: stringResource(R.string.no_description),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                            .semantics { contentDescription = POINT_DESCRIPTION_LABEL }
                    )
                }
            }
        }
    )
}
