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

package pl.marianjureczko.mysteryhunters.testdata

import com.ocadotechnology.gembus.test.someDouble
import com.ocadotechnology.gembus.test.someFloat

/**
 * someDouble(min,max) and someFlout(min,max)  doesn't work on Android
 */
fun someDoubleBetween(min: Double, max: Double): Double = min + (max - min) * someDouble()

fun someFloatBetween(min: Float, max: Float): Float = min + (max - min) * someFloat()
