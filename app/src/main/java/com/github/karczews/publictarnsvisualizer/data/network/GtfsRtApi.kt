package com.github.karczews.publictarnsvisualizer.data.network

import com.google.transit.realtime.GtfsRealtime.FeedMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class GtfsRtApi(private val client: OkHttpClient) {

    suspend fun fetchVehiclePositions(): Result<FeedMessage> =
        fetchFeed(GtfsEndpoints.vehiclePositionsUrl())

    suspend fun fetchTripUpdates(): Result<FeedMessage> =
        fetchFeed(GtfsEndpoints.tripUpdatesUrl())

    suspend fun fetchAlerts(): Result<FeedMessage> =
        fetchFeed(GtfsEndpoints.alertsUrl())

    private suspend fun fetchFeed(url: String): Result<FeedMessage> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                error("HTTP ${response.code}: ${response.message}")
            }
            val bytes = response.body!!.bytes()
            FeedMessage.parseFrom(bytes)
        }
    }
}
