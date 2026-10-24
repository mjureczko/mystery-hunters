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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Preview
import pl.marianjureczko.mysteryhunters.R

/**
 * A 3D-looking image button
 *
 * @param description the test id
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageButton3D(
    drawableId: Int,
    description: String,
    modifier: Modifier = Modifier,
    padding: Dp = 4.dp,
    enabled: Boolean = true,
    borderWidth: Dp = 2.dp,
    borderColor: Color = Color.Black.copy(alpha = 0.5f),
    onClick: () -> Unit
) {
    // Outer layer: Surface with elevation
    Surface(
        modifier = modifier,
        shadowElevation = 5.dp,
        shape = RoundedCornerShape(12),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        // Inner layer: draws the border, clipped shape, and the image on top
        Box(
            modifier = Modifier
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(12)
                )
                .clip(RoundedCornerShape(12))
                .clickable(
                    enabled = enabled,
                    interactionSource = null,
                    onClick = onClick
                )
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(drawableId),
                contentDescription = description,
                contentScale = ContentScale.Inside,
                colorFilter = if (enabled) null else ColorFilter.tint(Color.Gray),
                modifier = Modifier
                    .padding(padding)
                    .alpha(if (enabled) 1f else 0.4f)
            )
        }
    }
}
