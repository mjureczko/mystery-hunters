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
