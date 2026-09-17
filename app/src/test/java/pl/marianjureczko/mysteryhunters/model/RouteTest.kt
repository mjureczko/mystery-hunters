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

package pl.marianjureczko.mysteryhunters.model

import com.ocadotechnology.gembus.test.some
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RouteTest {

    @Test
    fun `SHOULD count how many points have been caught`() {
        // given
        val route = RouteArranger.routeWithPoints(4)
        val partiallyCaught = route.copy(
            pointsOfInterest = route.pointsOfInterest.map { it.copy(caught = it.id <= 2) }
        )

        // when
        val actual = partiallyCaught.caughtCount

        // then
        assertThat(actual).isEqualTo(2)
    }

    @Test
    fun `SHOULD report the route as finished WHEN every point is caught`() {
        // given
        val route = RouteArranger.routeWithAllPointsCaught(3)

        // when
        val actual = route.allCaught

        // then
        assertThat(actual).isTrue()
    }

    @Test
    fun `SHOULD not report an empty route as finished`() {
        // given
        val route = RouteArranger.routeWithoutPoints()

        // when
        val actual = route.allCaught

        // then
        assertThat(actual).isFalse()
    }

    @Test
    fun `SHOULD find the first point that is still missing`() {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val partiallyCaught = route.copy(
            pointsOfInterest = route.pointsOfInterest.map { it.copy(caught = it.id == 1) }
        )

        // when
        val actual = partiallyCaught.firstNotCaughtPoint()

        // then
        assertThat(actual?.id).isEqualTo(2)
    }

    @Test
    fun `SHOULD find nothing missing WHEN the whole route is caught`() {
        // given
        val route = RouteArranger.routeWithAllPointsCaught(3)

        // when
        val actual = route.firstNotCaughtPoint()

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun `SHOULD keep the points ordered by id WHEN one is added`() {
        // given
        val route = RouteArranger.routeWithPoints(3).withoutPoint(2)
        val restored = some<PointOfInterest>().copy(id = 2)

        // when
        val actual = route.withPoint(restored)

        // then
        assertThat(actual.pointsOfInterest.map { it.id }).containsExactly(1, 2, 3)
    }

    @Test
    fun `SHOULD replace the point with the same id WHEN one is added`() {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val replacement = some<PointOfInterest>().copy(id = 2)

        // when
        val actual = route.withPoint(replacement)

        // then
        assertThat(actual.pointsOfInterest).hasSize(3)
        assertThat(actual.pointById(2)).isEqualTo(replacement)
    }

    @Test
    fun `SHOULD answer with nothing WHEN no point is selected`() {
        // given
        val route = RouteArranger.routeWithPoints(3)

        // when
        val actual = route.pointById(null)

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun `SHOULD treat a route without an identifier as not persisted`() {
        // given
        val route = RouteArranger.routeWithoutPoints()

        // when
        val actual = route.isPersisted

        // then
        assertThat(actual).isFalse()
    }
}
