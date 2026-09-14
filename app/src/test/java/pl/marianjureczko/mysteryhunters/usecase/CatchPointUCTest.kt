package pl.marianjureczko.mysteryhunters.usecase

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort

class CatchPointUCTest {

    private val storage = TestRouteStoragePort()
    private val sut = CatchPointUC(storage)

    @Test
    fun `SHOULD mark the point as caught`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val caught = route.pointsOfInterest[1]

        // when
        val actual = sut(route, caught.id)

        // then
        assertThat(actual.pointById(caught.id)?.caught).isTrue()
    }

    @Test
    fun `SHOULD leave the other points not caught`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val caught = route.pointsOfInterest[1]

        // when
        val actual = sut(route, caught.id)

        // then
        assertThat(actual.pointsOfInterest.filter { it.caught }).hasSize(1)
    }

    @Test
    fun `SHOULD switch to the first not caught point`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3).copy(lastSelectedPointId = 1)

        // when
        val actual = sut(route, 1)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(2)
    }

    @Test
    fun `SHOULD switch back to an earlier point WHEN it is still not caught`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3).copy(lastSelectedPointId = 3)

        // when
        val actual = sut(route, 3)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(1)
    }

    @Test
    fun `SHOULD report the whole route as caught WHEN the last missing point is found`() = runTest {
        // given
        val route = RouteArranger.routeWithAllPointsCaught(3)
            .let { it.copy(pointsOfInterest = it.pointsOfInterest.map { point -> point.copy(caught = point.id != 2) }) }

        // when
        val actual = sut(route, 2)

        // then
        assertThat(actual.allCaught).isTrue()
    }

    @Test
    fun `SHOULD keep the selection on the found point WHEN everything is caught`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(1).copy(lastSelectedPointId = 1)

        // when
        val actual = sut(route, 1)

        // then
        assertThat(actual.lastSelectedPointId).isEqualTo(1)
    }

    @Test
    fun `SHOULD persist the progress`() = runTest {
        // given
        val route = storage.save(RouteArranger.routeWithPoints(2))

        // when
        val actual = sut(route, 1)

        // then
        assertThat(storage.stored(route.id)).isEqualTo(actual)
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
