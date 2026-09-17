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
import com.ocadotechnology.gembus.test.someDouble
import com.ocadotechnology.gembus.test.someString
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort

class AddPointOfInterestUCTest {

    private val storage = TestRouteStoragePort()
    private val sut = AddPointOfInterestUC(storage)

    @Test
    fun `SHOULD assign id 1 to the first point WHEN the route has no points yet`() = runTest {
        // given
        val route = RouteArranger.routeWithoutPoints()

        // when
        val actual = sut(route, someDouble(-90.0, 90.0), someDouble(-180.0, 180.0), someString())

        // then
        assertThat(actual.pointsOfInterest).hasSize(1)
        assertThat(actual.pointsOfInterest.first().id).isEqualTo(PointOfInterest.FIRST_ID)
    }

    @Test
    fun `SHOULD assign the next free id WHEN the route already has points`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)

        // when
        val actual = sut(route, someDouble(-90.0, 90.0), someDouble(-180.0, 180.0), someString())

        // then
        assertThat(actual.pointsOfInterest.map { it.id }).containsExactly(1, 2, 3, 4)
    }

    @Test
    fun `SHOULD continue after the highest id WHEN a point in the middle was removed`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3).withoutPoint(2)

        // when
        val actual = sut(route, someDouble(-90.0, 90.0), someDouble(-180.0, 180.0), someString())

        // then
        assertThat(actual.pointsOfInterest.map { it.id }).containsExactly(1, 3, 4)
    }

    @Test
    fun `SHOULD reuse the free number WHEN the point with the highest id was removed`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3).withoutPoint(3)

        // when
        val actual = sut(route, someDouble(-90.0, 90.0), someDouble(-180.0, 180.0), someString())

        // then
        assertThat(actual.pointsOfInterest.map { it.id }).containsExactly(1, 2, 3)
    }

    @Test
    fun `SHOULD store the given coordinates and description`() = runTest {
        // given
        val route = RouteArranger.routeWithoutPoints()
        val latitude = someDouble(-90.0, 90.0)
        val longitude = someDouble(-180.0, 180.0)
        val description = someString()

        // when
        val actual = sut(route, latitude, longitude, description)

        // then
        assertThat(actual.pointsOfInterest.first())
            .isEqualTo(
                PointOfInterest(
                    id = PointOfInterest.FIRST_ID,
                    latitude = latitude,
                    longitude = longitude,
                    description = description,
                    caught = false
                )
            )
    }

    @Test
    fun `SHOULD persist the route with the new point`() = runTest {
        // given
        val route = some<Route>()

        // when
        val actual = sut(route, someDouble(-90.0, 90.0), someDouble(-180.0, 180.0), someString())

        // then
        assertThat(storage.stored(actual.id)).isEqualTo(actual)
    }
}
