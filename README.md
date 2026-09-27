# Mystery Hunters (Łowcy Tajemnic)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.html)

An Android mystery hunting game. 
A hunter picks a route, follows a compass to the next mistery, and when close enough uncovers a three dimensional question mark through the camera. 
Every point carries a description that is revealed once the point has been caught.

## 1. Features

- **Routes** stored in a local Room database, created and renamed by the hunter.
- **Points of interest** placed by tapping an OpenStreetMap map. Each point has a user visible id,
  assigned automatically and sequentially from 1 within its route, and immutable afterwards.
- **Descriptions dictated offline.** Speech to text runs entirely on the device through Vosk, with a
  Polish and an English model. Nothing is sent to a network service and no audio is stored. When the
  engine or the model is unavailable, or the hunter does not like the result, the text field is
  always there to fall back to.
- **Compass navigation** showing direction and distance in steps to the current mystery, delivered by
  the `compass` module.
- **Augmented reality catching.** Within 20 m of a point a three dimensional question mark appears in
  the camera picture; beyond that range the screen says there is nothing to catch.
- **Works without ARCore.** On devices that have no ARCore the camera screen falls back to a plain
  preview with a flat question mark, so no phone loses the ability to play.
- **Progress tracking** with a collected points list, automatic switching to the next point that is
  still missing, and congratulations when a route is finished.
- **Polish and English**, Polish being the default.

## 2. Architecture

The project follows Uncle Bob's Clean Architecture, with the same layering as the reference project.
It is a single module Android application written in Kotlin with Jetpack Compose.
The dependency rules are:

- **Model** — pure Kotlin data structures, depending on nothing at all, not even Android.
- **Use cases** — one action or business rule each, depending only on the model and on port APIs.
- **Ports** — interfaces wrapping every external dependency, which is what makes them mockable.
- **Screens** — the top of the tree, free to depend on anything. Each screen is split into a
  Composable, a state and a ViewModel; the Composable works off the state and only the ViewModel
  knows about use cases and ports.

### 2.1 Package layout

| Package | Holds |
|---------|-------|
| `model` | `Route`, `PointOfInterest` |
| `usecase` | one class per action, e.g. `CatchPointUC`, `AddPointOfInterestUC` |
| `port` | port interfaces, with their implementations in `port/storage`, `port/speech`, `port/ar`, `port/orientation` |
| `screen` | one package per screen, each with a Screen, a State and a ViewModel |
| `ui` | theme, reusable components, navigation graph, screen relative sizing |
| `di` | Hilt modules: `PortsModule` (replaced in instrumented tests) and `SingletonModule` |

### 2.2 How the question mark is placed

ARCore does not align its world with north, and the Geospatial API that would solve that needs a Google Cloud project and a network connection. 
The app deliberately stays keyless and offline, so the marker is positioned relative to the camera instead:

```mermaid
sequenceDiagram
    participant GPS as compass LocationPort
    participant Sensor as DeviceOrientationPort
    participant VM as CameraViewModel
    participant AR as ARSceneView

    GPS->>VM: hunter location
    Sensor->>VM: azimuth (degrees from north)
    VM->>VM: IsPointInCatchRangeUC (<= 20 m?)
    VM->>VM: CalculateBearingUC (hunter -> point)
    VM->>VM: CalculateMarkerPositionUC (bearing - azimuth, distance)
    VM->>AR: position of the question mark
```

Both calculations are plain use cases with no Android dependency, so they are covered by ordinary unit tests. 
`ARSceneView` is left at its default `GeospatialMode.DISABLED`, so no API key and no network connection are involved.

What this costs: the mark is only as accurate as the phone's magnetometer and GPS. 
Near metal or indoors the compass drifts and the mark drifts with it, and it is positioned relative to the phone rather than pinned to the world, so it does not stay put when the hunter walks around it. 
For a 20 m catch radius that is good enough, and it keeps the game working offline. 
If you want the mark truly anchored to its place on Earth, see [Optional: the ARCore Geospatial API](#optional-the-arcore-geospatial-api).

## 3. Setup

### 3.1. Android SDK

Android Studio, or a command line SDK with platform 36 and build tools 36. `local.properties` has to point at it:

```properties
sdk.dir=/path/to/Android/Sdk
```

### 3.2. GitHub Packages credentials for the compass module

The `compass` artifact is published to GitHub Packages from the `maly-poszukiwacz-skarbow` repository, and GitHub requires authentication even for public packages.
Create a personal access
token with the `read:packages` scope and add it to `~/.gradle/gradle.properties`:

```properties
gpr.user=<your github login>
gpr.key=<the token>
```

`GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables work as well, which is what the pipeline uses. 
The artifact version is set by `COMPASS_VERSION` in `gradle.properties`.

### 3.3. Offline speech models

The Vosk models are around 95 MB together, too much for the repository, so they are fetched on demand:

```bash
./gradlew downloadVoskModels
```

This unpacks `vosk-model-small-pl-0.22` and `vosk-model-small-en-us-0.15` into `app/src/main/assets`, where `.gitignore` keeps them out of commits. 
Alongside each model the task writes a `uuid` marker, which Vosk needs to decide whether its copy of the model on the device is still current; the published archives do not contain one and recognition cannot start without it.

Running the task by hand is rarely necessary, because asset merging depends on it, so any APK you assemble carries the models. 
Unit tests do not merge assets and therefore never trigger the download. 
An APK built without them falls back to the text field instead of offering the microphone, so if the app reports that speech recognition is unavailable, check that `app/src/main/assets/vosk-model-*/` are populated and reinstall.

### 3.4. Build

```bash
./gradlew assembleDebug
```

### 3.5. ARCore API key — not required

**The app needs no Google API key and no Google Cloud project.** 
It uses plain ARCore motion tracking, which is free, offline and keyless. 
Nothing has to be configured for augmented reality to work; on a device without ARCore the camera screen falls back by itself.

An API key only becomes necessary if you move the app onto the ARCore Geospatial API, described below. 
That is an upgrade, not a missing piece.

#### Optional: the ARCore Geospatial API

The Geospatial API places anchors at real world coordinates using Google's Visual Positioning System, instead of working the direction out from the phone's compass. 
The mark would then stay exactly where it belongs while the hunter walks around it, and would not drift when the magnetometer is disturbed. 
In exchange it needs a Google Cloud project, a network connection, and VPS coverage at the place being hunted — where there is no coverage, and there is none in most of the countryside, it cannot resolve a position at all. 
Keep the current placement as a fallback if you take this on.

## 4. Testing

### 4.1 Unit tests

JUnit 5 with AssertJ and test-arranger, following [the test-arranger Android guidelines](https://github.com/ocadotechnology/test-arranger/blob/master/ai/android/claude_tester_SKILL.md).
Every use case and the domain model are covered.

```bash
./gradlew testDebugUnitTest
```

Test data comes from `some` methods and custom arrangers, when needed for unit and for instrumented (android) tests are located under `src/testShared`. 
Android cannot discover those arrangers reliably by reflection, so each one has to be listed in `src/testShared/resources/arranger.properties`. 
The `checkArrangers` task enforces it and runs before the tests:

```bash
./gradlew checkArrangers
```

### 5.1 Instrumented tests

The happy paths run against an emulator. 
Every port is replaced by a test double through `TestPortsModule`, so there is no database, GPS, microphone or ARCore involved.

```bash
./gradlew connectedDebugAndroidTest
```

An emulator to run them on, if there is none yet:

```bash
sdkmanager "system-images;android-36;google_apis;x86_64"
avdmanager create avd -n mystery_hunters -k "system-images;android-36;google_apis;x86_64" -d pixel_6
emulator -avd mystery_hunters
```

Two details are worth knowing when adding tests here. 
`ARSceneView` renders frame after frame, so Compose never reports itself idle and synchronised assertions time out; the camera tests take the Compose clock over and advance it by hand. 
And osmdroid only confirms a single tap after the double tap window has passed, so a test that taps the map has to wait for the state to change rather than assume the tap was handled at once.

## 6. Continuous integration

`.github/workflows/test_app.yml` runs the unit tests on every push to any branch. 
It restores `gradle.properties` from the `GRADLE_PROPERTIES` secret, which is where the GitHub Packages credentials come from.

## Licence

This project is licensed under the [GNU General Public License v3.0 or later](LICENSE)
(GPL-3.0-or-later, `SPDX-License-Identifier: GPL-3.0-or-later`), as the reference project
[maly-poszukiwacz-skarbow](https://github.com/mjureczko/maly-poszukiwacz-skarbow).

Copyright (C) 2026 Marian Jureczko

This program is free software: you can redistribute it and/or modify it under the terms of the GNU
General Public License as published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not,
see <https://www.gnu.org/licenses/>.
