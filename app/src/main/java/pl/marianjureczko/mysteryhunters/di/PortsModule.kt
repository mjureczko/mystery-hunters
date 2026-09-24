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

package pl.marianjureczko.mysteryhunters.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import pl.marianjureczko.mysteryhunters.port.ArAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.DeviceOrientationPort
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort
import pl.marianjureczko.mysteryhunters.port.SpeechToTextPort
import pl.marianjureczko.mysteryhunters.port.ar.ArCoreAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.location.SharedLocationPort
import pl.marianjureczko.mysteryhunters.port.orientation.SensorDeviceOrientationPort
import pl.marianjureczko.mysteryhunters.port.speech.VoskSpeechToTextPort
import pl.marianjureczko.mysteryhunters.port.storage.MysteryHuntersDatabase
import pl.marianjureczko.mysteryhunters.port.storage.RouteDao
import pl.marianjureczko.mysteryhunters.port.storage.RoomRouteStoragePort
import pl.marianjureczko.poszukiwacz.compass.api.CompassIoDispatcher
import pl.marianjureczko.poszukiwacz.compass.api.CompassMainDispatcher
import pl.marianjureczko.poszukiwacz.compass.api.LocationPort
import javax.inject.Singleton

/**
 * Module providing beans that shall be overridden in espresso tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object PortsModule {

    @Singleton
    @Provides
    fun database(@ApplicationContext appContext: Context): MysteryHuntersDatabase =
        Room.databaseBuilder(appContext, MysteryHuntersDatabase::class.java, MysteryHuntersDatabase.NAME)
            .build()

    @Singleton
    @Provides
    fun routeDao(database: MysteryHuntersDatabase): RouteDao = database.routeDao()

    @Singleton
    @Provides
    fun routeStoragePort(routeDao: RouteDao): RouteStoragePort = RoomRouteStoragePort(routeDao)

    @Singleton
    @Provides
    fun speechToTextPort(
        @ApplicationContext appContext: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SpeechToTextPort = VoskSpeechToTextPort(appContext, ioDispatcher)

    @Singleton
    @Provides
    fun arAvailabilityPort(@ApplicationContext appContext: Context): ArAvailabilityPort =
        ArCoreAvailabilityPort(appContext)

    @Provides
    fun deviceOrientationPort(@ApplicationContext appContext: Context): DeviceOrientationPort =
        SensorDeviceOrientationPort(appContext)

    @Singleton
    @Provides
    fun fusedLocationClient(@ApplicationContext appContext: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    /** Required by the compass module, see its README. */
    @Singleton
    @Provides
    fun locationPort(
        @ApplicationContext appContext: Context,
        locationClient: FusedLocationProviderClient,
        @CompassIoDispatcher ioDispatcher: CoroutineDispatcher,
        @CompassMainDispatcher mainDispatcher: CoroutineDispatcher
    ): LocationPort = SharedLocationPort(
        LocationPort.create(appContext, locationClient, ioDispatcher, mainDispatcher)
    )
}
