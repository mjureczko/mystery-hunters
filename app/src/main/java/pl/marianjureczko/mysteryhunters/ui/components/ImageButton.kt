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

package pl.marianjureczko.mysteryhunters.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app prefers image buttons over text buttons, this is the one used everywhere.
 *
 * @param description doubles as the test id
 */
@Composable
fun ImageButton(
    drawableId: Int,
    description: String,
    modifier: Modifier = Modifier,
    padding: Dp = 10.dp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(drawableId),
        contentDescription = description,
        contentScale = ContentScale.Inside,
        modifier = modifier
            .padding(padding)
            .alpha(if (enabled) 1f else 0.4f)
            .semantics { contentDescription = description }
            .clickable(enabled = enabled) { onClick() }
    )
}
