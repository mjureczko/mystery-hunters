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

package pl.marianjureczko.mysteryhunters.usecase

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort

class SelectPointUCTest {

    private val storage = TestRouteStoragePort()
    private val sut = SelectPointUC(storage)

    @Test
    fun `SHOULD navigate to the chosen point`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val chosen = route.pointsOfInterest[2]

        // when
        val actual = sut(route, chosen.id)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(chosen.id)
    }

    @Test
    fun `SHOULD change nothing WHEN the point does not belong to the route`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(2)

        // when
        val actual = sut(route, route.nextPointId())

        // then
        assertThat(actual).isEqualTo(route)
        assertThat(storage.saveCount).isZero()
    }
}
