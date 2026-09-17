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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.usecase.IsPointInCatchRangeUC.Companion.CATCH_RANGE_IN_METERS
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation
import pl.marianjureczko.poszukiwacz.compass.api.LocationCalculator

class IsPointInCatchRangeUCTest {

    private val sut = IsPointInCatchRangeUC(LocationCalculator())

    /** One degree of latitude is about 111320 m, which turns metres into coordinates. */
    private val metersPerLatitudeDegree = 111_320.0
    private val point = some<PointOfInterest>().copy(latitude = 52.0, longitude = 21.0)

    private fun hunterMetersAway(meters: Double): AndroidLocation =
        AndroidLocation.create(point.latitude + meters / metersPerLatitudeDegree, point.longitude)

    @ParameterizedTest(name = "{0} m away is in range: {1}")
    @CsvSource("0, true", "5, true", "19, true", "25, false", "100, false", "1000, false")
    fun catchRange(distanceInMeters: Double, expected: Boolean) {
        // given
        val hunterLocation = hunterMetersAway(distanceInMeters)

        // when
        val actual = sut(point, hunterLocation)

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `SHOULD allow catching WHEN the hunter stands exactly at the point`() {
        // given
        val hunterLocation = AndroidLocation.create(point.latitude, point.longitude)

        // when
        val actual = sut(point, hunterLocation)

        // then
        assertThat(actual).isTrue()
    }

    @Test
    fun `SHOULD refuse catching WHEN the location is not known yet`() {
        // given

        // when
        val actual = sut(point, null)

        // then
        assertThat(actual).isFalse()
    }

    @Test
    fun `SHOULD use twenty meters as the catch range`() {
        // given

        // when
        val actual = CATCH_RANGE_IN_METERS

        // then
        assertThat(actual).isEqualTo(20.0f)
    }
}
