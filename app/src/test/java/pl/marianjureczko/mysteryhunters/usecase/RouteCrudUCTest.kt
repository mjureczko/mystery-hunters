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

import com.ocadotechnology.gembus.test.some
import com.ocadotechnology.gembus.test.someString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort

/**
 * The use cases that only create, read, rename and remove whole routes.
 */
class RouteCrudUCTest {

    private val storage = TestRouteStoragePort()

    @Test
    fun `SHOULD persist a route under the given name WHEN it is created`() = runTest {
        // given
        val name = someString()

        // when
        val actual = CreateRouteUC(storage)(name)

        // then
        assertThat(actual.name).isEqualTo(name)
        assertThat(storage.stored(actual.id)).isEqualTo(actual)
    }

    @Test
    fun `SHOULD create a route without any point`() = runTest {
        // given
        val name = someString()

        // when
        val actual = CreateRouteUC(storage)(name)

        // then
        assertThat(actual.pointsOfInterest).isEmpty()
    }

    @Test
    fun `SHOULD trim the name WHEN a route is created`() = runTest {
        // given
        val name = someString()

        // when
        val actual = CreateRouteUC(storage)("  $name  ")

        // then
        assertThat(actual.name).isEqualTo(name)
    }

    @Test
    fun `SHOULD give the route an identifier WHEN it is created`() = runTest {
        // given
        val name = someString()

        // when
        val actual = CreateRouteUC(storage)(name)

        // then
        assertThat(actual.isPersisted).isTrue()
    }

    @Test
    fun `SHOULD change only the name WHEN a route is renamed`() = runTest {
        // given
        val route = storage.save(some<Route>())
        val newName = someString()

        // when
        val actual = RenameRouteUC(storage)(route, newName)

        // then
        assertThat(actual).isEqualTo(route.copy(name = newName))
    }

    @Test
    fun `SHOULD keep the points WHEN a route is renamed`() = runTest {
        // given
        val route = storage.save(RouteArranger.routeWithPoints(3))

        // when
        val actual = RenameRouteUC(storage)(route, someString())

        // then
        assertThat(actual.pointsOfInterest).isEqualTo(route.pointsOfInterest)
    }

    @Test
    fun `SHOULD remove the route WHEN it is deleted`() = runTest {
        // given
        val route = storage.save(some<Route>())

        // when
        DeleteRouteUC(storage)(route.id)

        // then
        assertThat(storage.contains(route.id)).isFalse()
    }

    @Test
    fun `SHOULD read back the stored route`() = runTest {
        // given
        val route = storage.save(RouteArranger.routeWithPoints(2))

        // when
        val actual = LoadRouteUC(storage)(route.id)

        // then
        assertThat(actual).isEqualTo(route)
    }

    @Test
    fun `SHOULD answer with nothing WHEN the route does not exist`() = runTest {
        // given
        val route = storage.save(some<Route>())

        // when
        val actual = LoadRouteUC(storage)(route.id + 1)

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun `SHOULD observe all the stored routes`() = runTest {
        // given
        val first = storage.save(some<Route>())
        val second = storage.save(some<Route>())

        // when
        val actual = ObserveRoutesUC(storage)().first()

        // then
        assertThat(actual).containsExactlyInAnyOrder(first, second)
    }
}
