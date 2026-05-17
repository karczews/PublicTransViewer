package com.github.karczews.publictarnsvisualizer.ui.home

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.karczews.publictarnsvisualizer.R
import com.github.karczews.publictarnsvisualizer.data.model.RouteDisplayData
import com.github.karczews.publictarnsvisualizer.data.model.RouteStop
import com.github.karczews.publictarnsvisualizer.data.model.VehiclePosition
import com.github.karczews.publictarnsvisualizer.data.model.VehicleType
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapLocationInfrastructure
import com.tomtom.sdk.map.display.camera.InitialCameraOptions
import com.tomtom.sdk.map.display.common.WidthByZoom
import com.tomtom.sdk.map.display.compose.TomTomMap
import com.tomtom.sdk.map.display.compose.model.MapDisplayInfrastructure
import com.tomtom.sdk.map.display.compose.model.MarkerData
import com.tomtom.sdk.map.display.compose.model.PolylineData
import com.tomtom.sdk.map.display.compose.nodes.Marker
import com.tomtom.sdk.map.display.compose.nodes.Polyline
import com.tomtom.sdk.map.display.compose.properties.MarkerProperties
import com.tomtom.sdk.map.display.compose.properties.PolylineProperties
import com.tomtom.sdk.map.display.compose.state.rememberMapViewState
import com.tomtom.sdk.map.display.compose.state.rememberMarkerState
import com.tomtom.sdk.map.display.compose.state.rememberPolylineState
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.Label

private val LODZ_CENTER = GeoPoint(latitude = 51.7592, longitude = 19.4560)
private const val INITIAL_ZOOM = 13.0

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val vehicles by viewModel.vehiclePositions.collectAsStateWithLifecycle()
    val selectedVehicleId by viewModel.selectedVehicleId.collectAsStateWithLifecycle()
    val routeDisplay by viewModel.routeDisplay.collectAsStateWithLifecycle()

    val initialCameraOptions = InitialCameraOptions.LocationBased(
        position = LODZ_CENTER,
        zoom = INITIAL_ZOOM,
    )
    val mapDisplayInfrastructure = remember {
        MapDisplayInfrastructure(
            sdkContext = TomTomSdk.sdkContext,
        ) {
            locationInfrastructure = MapLocationInfrastructure {
                locationProvider = TomTomSdk.locationProvider
            }
        }
    }
    val mapViewState = rememberMapViewState(initialCameraOptions = initialCameraOptions)

    TomTomMap(
        state = mapViewState,
        infrastructure = mapDisplayInfrastructure,
        modifier = modifier.fillMaxSize(),
    ) {
        routeDisplay?.let { route ->
            RouteOverlay(route = route)
        }

        vehicles.forEach { vehicle ->
            key(vehicle.vehicleId) {
                VehicleMarker(
                    vehicle = vehicle,
                    isSelected = vehicle.vehicleId == selectedVehicleId,
                    onSelect = { viewModel.onVehicleSelected(vehicle) },
                )
            }
        }
    }
}

@Composable
private fun VehicleMarker(
    vehicle: VehiclePosition,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val markerState = rememberMarkerState()
    Marker(
        data = MarkerData(
            geoPoint = GeoPoint(vehicle.latitude, vehicle.longitude),
        ),
        properties = MarkerProperties(
            pinImage = ImageFactory.fromResource(
                when (vehicle.vehicleType) {
                    VehicleType.TRAM -> R.drawable.ic_tram
                    VehicleType.BUS -> R.drawable.ic_bus
                }
            ),
        ) {
            label = Label(
                text = vehicle.routeShortName ?: vehicle.routeId,
                textColor = Color.WHITE,
                textSize = 12.0,
                outlineColor = Color.BLACK,
                outlineWidth = 2.0,
            )
            balloonText = buildBalloonText(vehicle)
        },
        state = markerState,
        onClick = {
            if (markerState.isSelected()) markerState.deselect() else markerState.select()
            onSelect()
        },
    )
}

@Composable
private fun RouteOverlay(route: RouteDisplayData) {
    if (route.polylinePoints.isNotEmpty()) {
        val geoPoints = remember(route.routeId) {
            route.polylinePoints.map { GeoPoint(it.lat, it.lon) }
        }
        Polyline(
            data = PolylineData(geoPoints = geoPoints) {
                tag = "route_${route.routeId}"
            },
            properties = PolylineProperties {
                lineColor = route.routeColor
                lineWidths = listOf(
                    WidthByZoom(6.0, 10.0),
                    WidthByZoom(10.0, 14.0),
                    WidthByZoom(14.0, 18.0),
                )
                outlineColor = Color.argb(100, 0, 0, 0)
                outlineWidths = listOf(WidthByZoom(2.0))
            },
            state = rememberPolylineState(),
        )
    }

    route.stops.forEach { stop ->
        key("stop_${stop.stopId}") {
            StopMarker(stop = stop, routeColor = route.routeColor)
        }
    }
}

@Composable
private fun StopMarker(stop: RouteStop, routeColor: Int) {
    Marker(
        data = MarkerData(
            geoPoint = GeoPoint(stop.lat, stop.lon),
        ),
        properties = MarkerProperties(
            pinImage = ImageFactory.fromResource(R.drawable.ic_stop_dot),
        ) {
            label = Label(
                text = stop.stopName,
                textColor = Color.DKGRAY,
                textSize = 10.0,
                outlineColor = Color.WHITE,
                outlineWidth = 1.0,
            )
            balloonText = stop.stopName
        },
        state = rememberMarkerState(),
        onClick = {
            // no-op for now
        },
    )
}

private fun buildBalloonText(vehicle: VehiclePosition): String {
    val typeName = when (vehicle.vehicleType) {
        VehicleType.TRAM -> "Tram"
        VehicleType.BUS -> "Bus"
    }
    val routeName = vehicle.routeShortName ?: vehicle.routeId
    val headsign = vehicle.tripHeadsign
    return if (headsign != null) "$typeName $routeName → $headsign" else "$typeName $routeName"
}
