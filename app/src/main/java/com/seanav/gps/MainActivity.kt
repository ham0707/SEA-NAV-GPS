
package com.seanav.gps

import android.app.Activity
import android.os.Bundle
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView

class MainActivity : Activity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)

        setContentView(mapView)

        mapView.getMapAsync { map ->
            map.setStyle(
                "https://demotiles.maplibre.org/style.json"
            ) {
                map.cameraPosition =
                    org.maplibre.android.camera.CameraPosition.Builder()
                        .target(
                            org.maplibre.android.geometry.LatLng(
                                -2.5, 118.0
                            )
                        )
                        .zoom(4.0)
                        .build()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}
