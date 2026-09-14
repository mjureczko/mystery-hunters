package pl.marianjureczko.mysteryhunters.usecase

import com.ocadotechnology.gembus.test.someDouble
import com.ocadotechnology.gembus.test.someString
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.model.RouteArranger
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort

class UpdatePointOfInterestUCTest {

    private val storage = TestRouteStoragePort()
    private val sut = UpdatePointOfInterestUC(storage)

    @Test
    fun `SHOULD replace the description and the coordinates of the selected point`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val edited = route.pointsOfInterest[1]
        val latitude = someDouble(-90.0, 90.0)
        val longitude = someDouble(-180.0, 180.0)
        val description = someString()

        // when
        val actual = sut(route, edited.id, latitude, longitude, description)

        // then
        assertThat(actual.pointById(edited.id))
            .isEqualTo(edited.copy(latitude = latitude, longitude = longitude, description = description))
    }

    @Test
    fun `SHOULD keep the id unchanged`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(2)
        val edited = route.pointsOfInterest.first()

        // when
        val actual = sut(route, edited.id, someDouble(-90.0, 90.0), someDouble(-180.0, 180.0), someString())

        // then
        assertThat(actual.pointsOfInterest.map { it.id })
            .isEqualTo(route.pointsOfInterest.map { it.id })
    }

    @Test
    fun `SHOULD leave the other points untouched`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(3)
        val edited = route.pointsOfInterest[0]

        // when
        val actual = sut(route, edited.id, someDouble(-90.0, 90.0), someDouble(-180.0, 180.0), someString())

        // then
        assertThat(actual.pointsOfInterest.filter { it.id != edited.id })
            .isEqualTo(route.pointsOfInterest.filter { it.id != edited.id })
    }

    @Test
    fun `SHOULD change nothing WHEN the point id is unknown`() = runTest {
        // given
        val route = RouteArranger.routeWithPoints(2)
        val unknownId = route.nextPointId()

        // when
        val actual = sut(route, unknownId, someDouble(-90.0, 90.0), someDouble(-180.0, 180.0), someString())

        // then
        assertThat(actual).isEqualTo(route)
        assertThat(storage.saveCount).isZero()
    }
}
