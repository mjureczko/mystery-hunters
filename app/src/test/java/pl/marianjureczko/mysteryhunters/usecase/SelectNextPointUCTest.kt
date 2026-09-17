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

class SelectNextPointUCTest {

    private val storage = TestRouteStoragePort()
    private val sut = SelectNextPointUC(storage)

    @Test
    fun `SHOULD move to the following point`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3).copy(lastSelectedPointId = 1)

        // when
        val actual = sut(route)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(2)
    }

    @Test
    fun `SHOULD wrap around to the first point WHEN the last one is selected`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3).copy(lastSelectedPointId = 3)

        // when
        val actual = sut(route)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(1)
    }

    @Test
    fun `SHOULD start from the first point WHEN nothing is selected yet`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)

        // when
        val actual = sut(route)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(1)
    }

    @Test
    fun `SHOULD persist the new selection`() = runTest {
        // given
        val route = storage.save(RouteArranger.routeWithPoints(2).copy(lastSelectedPointId = 1))

        // when
        val actual = sut(route)

        // then
        assertThat(storage.stored(route.id)).isEqualTo(actual)
    }

    @Test
    fun `SHOULD change nothing WHEN the route has no points`() = runTest {
        // given
        val route = RouteArranger.routeWithoutPoints()

        // when
        val actual = sut(route)

        // then
        assertThat(actual).isEqualTo(route)
        assertThat(storage.saveCount).isZero()
    }
}
