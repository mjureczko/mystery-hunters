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

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import pl.marianjureczko.mysteryhunters.di.PortsModule
import pl.marianjureczko.mysteryhunters.port.ArAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.DeviceOrientationPort
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort
import pl.marianjureczko.mysteryhunters.port.SpeechToTextPort
import pl.marianjureczko.mysteryhunters.port.TestArAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.TestDeviceOrientationPort
import pl.marianjureczko.mysteryhunters.port.TestLocationPort
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort
import pl.marianjureczko.mysteryhunters.port.TestSpeechToTextPort
import pl.marianjureczko.poszukiwacz.compass.api.LocationPort
import javax.inject.Singleton

/**
 * Replaces every external dependency with a test double, so the happy paths can be driven from a
 * test without a database, a GPS receiver, a microphone or ARCore.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [PortsModule::class])
object TestPortsModule {

    @Singleton
    @Provides
    fun testRouteStoragePort(): TestRouteStoragePort = TestRouteStoragePort()

    @Singleton
    @Provides
    fun routeStoragePort(testPort: TestRouteStoragePort): RouteStoragePort = testPort

    @Singleton
    @Provides
    fun testSpeechToTextPort(): TestSpeechToTextPort = TestSpeechToTextPort()

    @Singleton
    @Provides
    fun speechToTextPort(testPort: TestSpeechToTextPort): SpeechToTextPort = testPort

    @Singleton
    @Provides
    fun testArAvailabilityPort(): TestArAvailabilityPort = TestArAvailabilityPort()

    @Singleton
    @Provides
    fun arAvailabilityPort(testPort: TestArAvailabilityPort): ArAvailabilityPort = testPort

    @Singleton
    @Provides
    fun testDeviceOrientationPort(): TestDeviceOrientationPort = TestDeviceOrientationPort()

    @Singleton
    @Provides
    fun deviceOrientationPort(testPort: TestDeviceOrientationPort): DeviceOrientationPort = testPort

    @Singleton
    @Provides
    fun testLocationPort(): TestLocationPort = TestLocationPort()

    @Singleton
    @Provides
    fun locationPort(testPort: TestLocationPort): LocationPort = testPort
}
