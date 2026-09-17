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
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.screen.routelist.SELECT_ROUTE_BUTTON
import pl.marianjureczko.mysteryhunters.screen.searching.CHANGE_POINT_BUTTON
import pl.marianjureczko.mysteryhunters.screen.searching.CONGRATULATIONS_LABEL
import pl.marianjureczko.mysteryhunters.screen.searching.SEARCHED_POINT_LABEL
import pl.marianjureczko.mysteryhunters.ui.components.TOPBAR_GO_BACK

@HiltAndroidTest
class SearchingScreenTest : AbstractUiTest() {

    @Test
    fun navigateToTheFirstPointWhenTheRouteIsOpenedForTheFirstTime() {
        // given
        val route = given(RouteArranger.routeWithPoints(3))

        // when
        selectRoute(route.name)

        // then
        waitUntilDisplayed(SEARCHED_POINT_LABEL)
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
            storedRoute(route.id)?.lastSelectedPointId == PointOfInterest.FIRST_ID
        }
        assertThat(storedRoute(route.id)?.lastSelectedPointId).isEqualTo(PointOfInterest.FIRST_ID)
    }

    @Test
    fun restoreTheLastNavigatedPointWhenTheRouteIsReopened() {
        // given
        val route = given(RouteArranger.routeWithPoints(3).copy(lastSelectedPointId = 3))

        // when
        selectRoute(route.name)

        // then
        waitUntilDisplayed(SEARCHED_POINT_LABEL)
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
            storedRoute(route.id)?.lastSelectedPointId == 3
        }
        assertThat(storedRoute(route.id)?.lastSelectedPointId).isEqualTo(3)
    }

    @Test
    fun switchToAnotherPointWithTheChangePointButton() {
        // given
        val route = given(RouteArranger.routeWithPoints(3))
        selectRoute(route.name)
        waitUntilDisplayed(CHANGE_POINT_BUTTON)

        // when
        composeRule.onNodeWithContentDescription(CHANGE_POINT_BUTTON).performClick()

        // then
        composeRule.waitUntil(WAIT_TIMEOUT_IN_MILLIS) {
            storedRoute(route.id)?.lastSelectedPointId == 2
        }
        assertThat(storedRoute(route.id)?.lastSelectedPointId).isEqualTo(2)
    }

    @Test
    fun congratulateWhenEveryPointHasBeenCaught() {
        // given
        val route = given(RouteArranger.routeWithAllPointsCaught(2))

        // when
        selectRoute(route.name)

        // then
        waitUntilDisplayed(CONGRATULATIONS_LABEL)
        composeRule.onNodeWithContentDescription(CONGRATULATIONS_LABEL).assertIsDisplayed()
    }

    protected fun selectRoute(routeName: String) {
        waitUntilDisplayed("$SELECT_ROUTE_BUTTON $routeName")
        composeRule.onNodeWithContentDescription("$SELECT_ROUTE_BUTTON $routeName").performClick()
    }

    protected fun goBack() {
        composeRule.onNodeWithContentDescription(TOPBAR_GO_BACK).performClick()
    }
}
