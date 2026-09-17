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

package pl.marianjureczko.mysteryhunters.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pl.marianjureczko.mysteryhunters.R

val FANCY_FONT = FontFamily(Font(R.font.akaya_telivigala))

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    displayLarge = TextStyle(fontFamily = FANCY_FONT, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = FANCY_FONT, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = FANCY_FONT, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = FANCY_FONT, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = FANCY_FONT, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = FANCY_FONT, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FANCY_FONT),
    titleLarge = TextStyle(fontSize = 20.sp)
)
