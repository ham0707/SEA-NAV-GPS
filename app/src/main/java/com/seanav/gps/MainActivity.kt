
package com.seanav.gps

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions

import java.util.Locale

import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource

class MainActivity : Activity() {

    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager

    private var marineMap: MapLibreMap? = null
    private var shipMarker: Marker? = null
    private var lastLocation: Location? = null
private var bathymetryLayer: RasterLayer? = null
private var bathymetryEnabled = false
    private lateinit var coordinateText: TextView
    private lateinit var speedText: TextView
    private lateinit var accuracyText: TextView
    private lateinit var gpsStatus: TextView

    private val navy = Color.rgb(7, 20, 34)
    private val panel = Color.rgb(13, 34, 52)
    private val blue = Color.rgb(35, 155, 230)
    private val white = Color.WHITE
    private val muted = Color.rgb(155, 180, 200)

    private val requestCodeGps = 100

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun background(
        color: Int,
        radius: Int = 16
    ) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(1), Color.rgb(40, 70, 90))
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) {
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = navy
        window.navigationBarColor = navy

        MapLibre.getInstance(this)

        locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        val root = FrameLayout(this)

        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)

        root.addView(
            mapView,
            FrameLayout.LayoutParams(
                -1, -1
            )
        )

        // HEADER
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(12), dp(15), dp(12))
            background = background(navy, 18)
        }

        val logo = text("S", 23f, white, true).apply {
            gravity = Gravity.CENTER
            background = background(blue, 12)
        }

        header.addView(
            logo,
            LinearLayout.LayoutParams(dp(43), dp(43))
        )

        val titleArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        titleArea.addView(
            text("SEA NAV GPS", 17f, white, true)
        )

        titleArea.addView(
            text("MARINE NAVIGATION SYSTEM", 9f, muted)
        )

        header.addView(
            titleArea,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        gpsStatus = text(
            "GPS MENUNGGU", 10f, muted, true
        )

        header.addView(gpsStatus)

        root.addView(
            header,
            FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.TOP
                setMargins(dp(12), dp(12), dp(12), 0)
            }
        )

        // DASHBOARD
        val dashboard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(14))
            background = background(navy, 22)
        }

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val bathymetry = controlButton("◈ BATIMETRI")
        val offline = controlButton("↓ OFFLINE")
        val mapMode = controlButton("⌖ PETA")

        controls.addView(
            bathymetry,
            LinearLayout.LayoutParams(0, dp(42), 1f)
        )

        controls.addView(
            offline,
            LinearLayout.LayoutParams(0, dp(42), 1f)
        )

        controls.addView(
            mapMode,
            LinearLayout.LayoutParams(0, dp(42), 1f)
        )

        dashboard.addView(controls)

        val divider = View(this).apply {
            setBackgroundColor(Color.rgb(40, 65, 82))
        }

        dashboard.addView(
            divider,
            LinearLayout.LayoutParams(-1, dp(1)).apply {
                topMargin = dp(13)
                bottomMargin = dp(13)
            }
        )

        dashboard.addView(
            text("POSISI KAPAL", 11f, muted, true)
        )

        coordinateText = text(
            "Menunggu sinyal GPS...",
            17f, white, true
        )

        coordinateText.setPadding(0, dp(10), 0, dp(14))
        dashboard.addView(coordinateText)

        val information = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        information.addView(
            infoCard("KECEPATAN", "-- kn"),
            LinearLayout.LayoutParams(0, dp(70), 1f)
        )

        speedText = (information.getChildAt(0) as LinearLayout)
            .getChildAt(1) as TextView

        information.addView(
            View(this),
            LinearLayout.LayoutParams(dp(10), 1)
        )

        information.addView(
            infoCard("AKURASI GPS", "-- m"),
            LinearLayout.LayoutParams(0, dp(70), 1f)
        )

        accuracyText = (information.getChildAt(2) as LinearLayout)
            .getChildAt(1) as TextView

        dashboard.addView(information)

        dashboard.addView(
            text(
                "KEDALAMAN: BELUM TERSEDIA",
                9f, muted, true
            ).apply {
                setPadding(0, dp(12), 0, 0)
            }
        )

        root.addView(
            dashboard,
            FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.BOTTOM
                setMargins(dp(12), 0, dp(12), dp(12))
            }
        )

        // GPS BUTTON
        val gpsButton = text("⌖", 27f, white, true).apply {
            gravity = Gravity.CENTER
            background = background(blue, 18)
            elevation = dp(6).toFloat()

            setOnClickListener {
                val location = lastLocation

                if (location != null) {
                    marineMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ),
                            13.0
                        )
                    )
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Menunggu sinyal GPS.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        root.addView(
            gpsButton,
            FrameLayout.LayoutParams(dp(52), dp(52)).apply {
                gravity = Gravity.END or Gravity.BOTTOM
                setMargins(0, 0, dp(18), dp(270))
            }
        )

        setContentView(root)

        // MAPLIBRE
        mapView.getMapAsync { map ->

            marineMap = map

            map.setStyle(
    "https://demotiles.maplibre.org/style.json"
) { style ->

    val bathySource = RasterSource(
        "gebco-bathy-source",
        "https://wms.gebco.net/mapserv?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&LAYERS=GEBCO_Latest_2&STYLES=&FORMAT=image/png&TRANSPARENT=true&SRS=EPSG:3857&WIDTH=256&HEIGHT=256&BBOX={bbox-epsg-3857}",
        256
    )

    style.addSource(bathySource)

    val bathyLayer = RasterLayer(
        "gebco-bathy-layer",
        "gebco-bathy-source"
    ).withProperties(
        PropertyFactory.rasterOpacity(0.75f),
        PropertyFactory.visibility("none")
    )

    style.addLayer(bathyLayer)

    bathymetryLayer = bathyLayer

    map.cameraPosition =
        CameraPosition.Builder()
            .target(LatLng(-2.5, 118.0))
            .zoom(4.0)
            .build()

    lastLocation?.let {
        updateMapMarker(it)
    }
}

        bathymetry.setOnClickListener {

    bathymetryEnabled = !bathymetryEnabled

    bathymetryLayer?.setProperties(
        PropertyFactory.visibility(
            if (bathymetryEnabled) "visible" else "none"
        )
    )

    bathymetry.text =
        if (bathymetryEnabled)
            "◈ BATIMETRI ON"
        else
            "◈ BATIMETRI"
}

        offline.setOnClickListener {
            showMessage("Modul peta offline dalam pengembangan.")
        }

        mapMode.setOnClickListener {
            showMessage("Mode peta navigasi aktif.")
        }

        checkGpsPermission()
    }

    private fun controlButton(label: String) =
        text(label, 10f, white, true).apply {
            gravity = Gravity.CENTER
            background = background(panel, 12)
        }

    private fun infoCard(
        label: String,
        value: String
    ): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(7), dp(12), dp(7))
            background = background(panel, 12)
        }

        card.addView(text(label, 9f, muted, true))
        card.addView(text(value, 18f, white, true))

        return card
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // PERMISSION GPS
    private fun checkGpsPermission() {

        if (
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestCodeGps
            )
        } else {
            startGps()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == requestCodeGps) {

            if (
                checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startGps()
            } else {
                gpsStatus.text = "IZIN GPS DITOLAK"
                showMessage("Izinkan lokasi presisi untuk GPS.")
            }
        }
    }

    // START GPS SATELLITE
    private fun startGps() {

        if (
            checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        try {

            if (!locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
                )
            ) {
                gpsStatus.text = "GPS NONAKTIF"
                showMessage("Aktifkan lokasi GPS HP.")
                return
            }

            gpsStatus.text = "MENCARI SATELIT"

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                gpsListener
            )

            val last = locationManager.getLastKnownLocation(
                LocationManager.GPS_PROVIDER
            )

            last?.let {
                updateGps(it)
            }

        } catch (e: SecurityException) {
            gpsStatus.text = "IZIN GPS DITOLAK"
        }
    }

    private val gpsListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            updateGps(location)
        }

        override fun onProviderEnabled(provider: String) {
            gpsStatus.text = "GPS AKTIF"
        }

        override fun onProviderDisabled(provider: String) {
            gpsStatus.text = "GPS NONAKTIF"
        }
    }

    private fun updateGps(location: Location) {

        lastLocation = location

        gpsStatus.text = "● GPS AKTIF"

        coordinateText.text = String.format(
            Locale.US,
            "%.6f° , %.6f°",
            location.latitude,
            location.longitude
        )

        if (location.hasSpeed()) {

            val knots = location.speed * 1.943844f

            speedText.text = String.format(
                Locale.US,
                "%.1f kn",
                knots
            )

        } else {
            speedText.text = "-- kn"
        }

        if (location.hasAccuracy()) {

            accuracyText.text = String.format(
                Locale.US,
                "%.0f m",
                location.accuracy
            )
        }

        updateMapMarker(location)
    }

    private fun updateMapMarker(location: Location) {

        val map = marineMap ?: return

        val position = LatLng(
            location.latitude,
            location.longitude
        )

        try {

            if (shipMarker != null) {
                map.removeMarker(shipMarker!!)
            }

            shipMarker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("Posisi GPS")
                    .snippet("Posisi perangkat saat ini")
            )

        } catch (e: Exception) {
            // Marker akan dicoba diperbarui pada lokasi berikutnya.
        }
    }

    private fun stopGps() {
        locationManager.removeUpdates(gpsListener)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        if (
            checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startGps()
        }
    }

    override fun onPause() {
        stopGps()
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
        stopGps()
        mapView.onDestroy()
        super.onDestroy()
    }
}
