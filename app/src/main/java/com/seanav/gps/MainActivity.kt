
package com.seanav.gps

import android.app.Activity
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
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

class MainActivity : Activity() {

    private lateinit var mapView: MapView
    private lateinit var root: FrameLayout

    private val navy = Color.rgb(7, 20, 34)
    private val panel = Color.rgb(13, 34, 52)
    private val blue = Color.rgb(35, 155, 230)
    private val white = Color.WHITE
    private val muted = Color.rgb(155, 180, 200)

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun background(
        color: Int,
        radius: Int = 16
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), Color.rgb(40, 70, 90))
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = navy
        window.navigationBarColor = navy

        MapLibre.getInstance(this)

        root = FrameLayout(this)

        // MAP VIEW
        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)

        root.addView(
            mapView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // HEADER
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = background(navy, 18)
        }

        val logo = TextView(this).apply {
            text = "S"
            textSize = 23f
            setTextColor(white)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
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
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val status = text("● ONLINE", 10f, Color.rgb(80, 220, 150), true)
        header.addView(status)

        val headerParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP
            setMargins(dp(12), dp(12), dp(12), 0)
        }

        root.addView(header, headerParams)

        // BOTTOM DASHBOARD
        val dashboard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(14))
            background = background(navy, 22)
        }

        // CONTROL BUTTONS
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val bathymetry = controlButton("◈  BATIMETRI")
        val offline = controlButton("↓  OFFLINE")
        val mapMode = controlButton("⌖  PETA")

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

        // DIVIDER
        val divider = View(this).apply {
            setBackgroundColor(Color.rgb(40, 65, 82))
        }

        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(1)
        ).apply {
            topMargin = dp(13)
            bottomMargin = dp(13)
        }

        dashboard.addView(divider, dividerParams)

        // GPS INFORMATION
        dashboard.addView(
            text("POSISI KAPAL", 11f, muted, true)
        )

        val coordinates = text(
            "--° --' --\"     --° --' --\"",
            19f,
            white,
            true
        )

        coordinates.setPadding(0, dp(8), 0, dp(13))
        dashboard.addView(coordinates)

        // SPEED AND DEPTH
        val information = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        information.addView(
            infoCard("KECEPATAN", "-- kn"),
            LinearLayout.LayoutParams(0, dp(70), 1f)
        )

        information.addView(
            View(this),
            LinearLayout.LayoutParams(dp(10), 1)
        )

        information.addView(
            infoCard("KEDALAMAN", "-- m"),
            LinearLayout.LayoutParams(0, dp(70), 1f)
        )

        dashboard.addView(information)

        val gpsNote = text(
            "GPS belum diaktifkan  •  Data kedalaman belum tersedia",
            9f,
            muted
        )

        gpsNote.setPadding(0, dp(12), 0, 0)
        dashboard.addView(gpsNote)

        val bottomParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
            setMargins(dp(12), 0, dp(12), dp(12))
        }

        root.addView(dashboard, bottomParams)

        // GPS BUTTON
        val gpsButton = TextView(this).apply {
            text = "⌖"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(white)
            background = background(blue, 18)
            elevation = dp(6).toFloat()

            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "Modul GPS akan diaktifkan pada tahap berikutnya.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val gpsParams = FrameLayout.LayoutParams(
            dp(52),
            dp(52)
        ).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            setMargins(0, 0, dp(18), dp(260))
        }

        root.addView(gpsButton, gpsParams)

        setContentView(root)

        // LOAD MAP
        mapView.getMapAsync { map ->

            map.setStyle(
                "https://demotiles.maplibre.org/style.json"
            ) {

                map.cameraPosition =
                    CameraPosition.Builder()
                        .target(LatLng(-2.5, 118.0))
                        .zoom(4.0)
                        .build()
            }
        }

        // BUTTON ACTIONS
        bathymetry.setOnClickListener {
            Toast.makeText(
                this,
                "Modul batimetri belum tersedia.",
                Toast.LENGTH_SHORT
            ).show()
        }

        offline.setOnClickListener {
            Toast.makeText(
                this,
                "Modul peta offline belum tersedia.",
                Toast.LENGTH_SHORT
            ).show()
        }

        mapMode.setOnClickListener {
            Toast.makeText(
                this,
                "Menampilkan peta navigasi.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun controlButton(label: String): TextView {
        return TextView(this).apply {
            text = label
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(white)
            background = background(panel, 12)
        }
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

        card.addView(
            text(value, 18f, white, true)
        )

        return card
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
