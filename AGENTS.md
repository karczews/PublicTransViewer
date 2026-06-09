# AGENTS.md

Guidance for AI coding agents working in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device
./gradlew test                   # Run unit tests
./gradlew lint                   # Android lint (no detekt/ktlint/spotless configured)
./gradlew connectedAndroidTest   # Run instrumented tests on a connected device/emulator
./gradlew test --tests "*.HomeViewModelTest"  # Run a single unit test class
# Single instrumented test:
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.github.karczews.publictransportviewer.ui.StopsScreenTest
```

The TomTom API key must be set in `gradle.properties` as `tomtomApiKey=YOUR_KEY`. It is injected into `BuildConfig.TOMTOM_API_KEY` at build time.

## Architecture

See `docs/ARCHITECTURE.md` for data-flow diagrams, the Room schema, and component relationships.

Single-module Android app (`app/`) using MVVM with Jetpack Compose. Dependency injection uses **Koin with the Koin Compiler Plugin** (`io.insert-koin.compiler.plugin`) — the annotation-based, compile-time-verified config (not KSP). The graph lives in `di/AppModule.kt` (a `@Module @ComponentScan` class of `@Single` provider functions); view models are `@KoinViewModel`, the WorkManager worker is `@KoinWorker`, and `PublicTransportApp` starts Koin via `startKoin<PublicTransportKoinConfiguration>` (a `@KoinApplication` aggregator). `compileSafety`/`strictSafety` validate the whole graph at build time. Composables obtain view models with `koinViewModel()`. Instrumented tests swap the graph through `TestPublicTransportApp` + `KoinTestRunner` (a plain DSL `testModule` of fakes).

### Data Pipeline

Two separate data pipelines feed the app from the Lodz Open Data portal (`otwarte.miasto.lodz.pl`):

**GTFS Static** — A ZIP of CSV files (`routes.txt`, `stops.txt`, `trips.txt`, `stop_times.txt`, `shapes.txt`) downloaded on first launch and refreshed weekly via WorkManager. Parsed by `GtfsCsvParser` and stored in a Room database (`GtfsDatabase`, 6 tables). Large files (`stop_times.txt`, `shapes.txt`) are streamed in 1000-row batches.

**GTFS Realtime** — Three protobuf binary feeds (`.bin` files) fetched via OkHttp and deserialized with `gtfs-realtime-bindings`. Vehicle positions poll every 5s, trip updates every 15s, alerts every 60s. Each repository caches the last successful response in-memory as a fallback on network failure.

Feed URLs contain a hardcoded year/month path in `GtfsEndpoints` (currently `2025/06`). When the city rotates feeds, this must be updated.

### Map Rendering

The map uses the **TomTom Maps SDK** Compose API (`map-display-compose-standard`; version pinned as `tomtomSdk` in `gradle/libs.versions.toml`, currently 2.3.0). The SDK requires async initialization in `MainActivity` before any map composables render — the UI shows a loading spinner until `TomTomSdk.initialize()` completes.

Key composables: `TomTomMap` (map surface), `Marker` (vehicles/stops), `Polyline` (route shapes), `CurrentLocationMarker` (blue dot). The location provider lifecycle (`enable()`/`disable()`) must be managed manually in a `DisposableEffect`.

### Enrichment Pattern

Realtime data is enriched with static GTFS data from Room. Repositories fetch raw RT data from data sources, then look up route names, colors, headsigns, and vehicle types from Room DAOs before emitting to ViewModels. Vehicle type is determined from GTFS `route_type` (0=Tram, 3=Bus) with a fallback heuristic (route numbers 1–46 = Tram) when static data hasn't loaded yet.

## Conventions

- Base package: `com.github.karczews.publictransportviewer`
- TomTom SDK flavor: `complete` (set via `missingDimensionStrategy` in build.gradle.kts)
- Room schemas exported to `app/schemas/`
- NDK ABI filters: `arm64-v8a` and `x86_64` only
- Dependency versions live in the version catalog `gradle/libs.versions.toml` — resolve and bump them there, not in `app/build.gradle.kts`
