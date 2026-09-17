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

package pl.marianjureczko.mysteryhunters

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.screen.collected.COLLECTED_POINTS_LIST
import pl.marianjureczko.mysteryhunters.screen.collected.COLLECTED_POINT_ROW
import pl.marianjureczko.mysteryhunters.screen.collected.NOT_CAUGHT_INFO
import pl.marianjureczko.mysteryhunters.screen.pointdetail.POINT_DESCRIPTION_LABEL
import pl.marianjureczko.mysteryhunters.screen.routelist.SELECT_ROUTE_BUTTON
import pl.marianjureczko.mysteryhunters.screen.searching.COLLECTED_POINTS_BUTTON

@HiltAndroidTest
class CollectedPointsScreenTest : AbstractUiTest() {

    @Test
    fun showTheDescriptionOfAPointThatHasBeenCaught() {
        // given
        val base = RouteArranger.routeWithPoints(2)
        val caught = base.pointsOfInterest.first().copy(caught = true, description = "Odkryta tajemnica")
        val route = given(base.withPoint(caught))
        openCollectedPoints(route.name)

        // when
        composeRule.onNodeWithContentDescription("$COLLECTED_POINT_ROW ${caught.id}").performClick()

        // then
        waitUntilDisplayed(POINT_DESCRIPTION_LABEL)
        composeRule.onNodeWithText("Odkryta tajemnica").assertIsDisplayed()
    }

    @Test
    fun explainThatAPointHasNotBeenCaughtYet() {
        // given
        val route = given(RouteArranger.routeWithPoints(2))
        openCollectedPoints(route.name)

        // when
        composeRule.onNodeWithContentDescription("$COLLECTED_POINT_ROW 2").performClick()

        // then
        waitUntilDisplayed(NOT_CAUGHT_INFO)
        composeRule.onNodeWithContentDescription(NOT_CAUGHT_INFO).assertIsDisplayed()
    }

    private fun openCollectedPoints(routeName: String) {
        waitUntilDisplayed("$SELECT_ROUTE_BUTTON $routeName")
        composeRule.onNodeWithContentDescription("$SELECT_ROUTE_BUTTON $routeName").performClick()
        waitUntilDisplayed(COLLECTED_POINTS_BUTTON)
        composeRule.onNodeWithContentDescription(COLLECTED_POINTS_BUTTON).performClick()
        waitUntilDisplayed(COLLECTED_POINTS_LIST)
    }
}
