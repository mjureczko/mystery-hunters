package pl.marianjureczko.mysteryhunters.usecase

import com.ocadotechnology.gembus.test.some
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset.offset
import org.junit.jupiter.api.Test
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.poszukiwacz.compass.api.AndroidLocation

class CalculateBearingUCTest {

    private val sut = CalculateBearingUC()

    private val hunterLatitude = 52.0
    private val hunterLongitude = 21.0
    private val hunterLocation = AndroidLocation.create(hunterLatitude, hunterLongitude)

    private fun pointAt(latitude: Double, longitude: Double): PointOfInterest =
        some<PointOfInterest>().copy(latitude = latitude, longitude = longitude)

    @Test
    fun `SHOULD report north WHEN the point lies straight above`() {
        // given
        val point = pointAt(hunterLatitude + 0.01, hunterLongitude)

        // when
        val actual = sut(point, hunterLocation)

        // then
        assertThat(actual).isCloseTo(0f, offset(0.5f))
    }

    @Test
    fun `SHOULD report east WHEN the point lies to the right`() {
        // given
        val point = pointAt(hunterLatitude, hunterLongitude + 0.01)

        // when
        val actual = sut(point, hunterLocation)

        // then
        assertThat(actual).isCloseTo(90f, offset(0.5f))
    }

    @Test
    fun `SHOULD report south WHEN the point lies straight below`() {
        // given
        val point = pointAt(hunterLatitude - 0.01, hunterLongitude)

        // when
        val actual = sut(point, hunterLocation)

        // then
        assertThat(actual).isCloseTo(180f, offset(0.5f))
    }

    @Test
    fun `SHOULD report west WHEN the point lies to the left`() {
        // given
        val point = pointAt(hunterLatitude, hunterLongitude - 0.01)

        // when
        val actual = sut(point, hunterLocation)

        // then
        assertThat(actual).isCloseTo(270f, offset(0.5f))
    }

    @Test
    fun `SHOULD always answer within a full turn`() {
        // given
        val point = some<PointOfInterest>()

        // when
        val actual = sut(point, hunterLocation)

        // then
        assertThat(actual).isBetween(0f, 360f)
    }
}
