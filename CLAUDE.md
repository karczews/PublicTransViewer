# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests on device
./gradlew test --tests "*.ExampleUnitTest.addition_isCorrect"  # Run single test
```

The TomTom API key must be set in `gradle.properties` as `tomtomApiKey=YOUR_KEY`. It is injected into `BuildConfig.TOMTOM_API_KEY` at build time.

## Architecture

Single-module Android app (`app/`) using MVVM with Jetpack Compose. Dependency injection is handled by **Metro** (`dev.zacsweers.metro`), a compile-time DI compiler plugin. The application-wide graph is `di/AppGraph.kt` (`@DependencyGraph(AppScope::class)`); bindings are aggregated from `@ContributesTo` containers (`InfrastructureBindings`, `RepositoryBindings`), `@ContributesIntoMap` ViewModels (keyed by `@ViewModelKey`), and the `@WorkerKey` worker factory. `PublicTransApp` builds the graph via `createGraphFactory<AppGraph.Factory>().create(this)`. ViewModels are obtained in Compose with `metroViewModel()` (MetroX `metrox-viewmodel-compose`), backed by `InjectedViewModelFactory` exposed through `LocalMetroViewModelFactory`. Workers are built by `MetroWorkerFactory` using Metro assisted injection.

### Data Pipeline

Two separate data pipelines feed the app from the Lodz Open Data portal (`otwarte.miasto.lodz.pl`):

**GTFS Static** — A ZIP of CSV files (`routes.txt`, `stops.txt`, `trips.txt`, `stop_times.txt`, `shapes.txt`) downloaded on first launch and refreshed weekly via WorkManager. Parsed by `GtfsCsvParser` and stored in a Room database (`GtfsDatabase`, 6 tables). Large files (`stop_times.txt`, `shapes.txt`) are streamed in 1000-row batches.

**GTFS Realtime** — Three protobuf binary feeds (`.bin` files) fetched via OkHttp and deserialized with `gtfs-realtime-bindings`. Vehicle positions poll every 5s, trip updates every 15s, alerts every 60s. Each repository caches the last successful response in-memory as a fallback on network failure.

Feed URLs contain a hardcoded year/month path in `GtfsEndpoints` (currently `2025/06`). When the city rotates feeds, this must be updated.

### Map Rendering

The map uses **TomTom Maps SDK v2.2.1** Compose API (`map-display-compose-standard`). The SDK requires async initialization in `MainActivity` before any map composables render — the UI shows a loading spinner until `TomTomSdk.initialize()` completes.

Key composables: `TomTomMap` (map surface), `Marker` (vehicles/stops), `Polyline` (route shapes), `CurrentLocationMarker` (blue dot). The location provider lifecycle (`enable()`/`disable()`) must be managed manually in a `DisposableEffect`.

### Enrichment Pattern

Realtime data is enriched with static GTFS data from Room. Repositories fetch raw RT data from data sources, then look up route names, colors, headsigns, and vehicle types from Room DAOs before emitting to ViewModels. Vehicle type is determined from GTFS `route_type` (0=Tram, 3=Bus) with a fallback heuristic (route numbers 1–46 = Tram) when static data hasn't loaded yet.

## Conventions

- Base package: `com.github.karczews.publictarnsvisualizer` (note: "tarns" is an intentional early typo preserved in the package name)
- TomTom SDK flavor: `complete` (set via `missingDimensionStrategy` in build.gradle.kts)
- Room schemas exported to `app/schemas/`
- NDK ABI filters: `arm64-v8a` and `x86_64` only
