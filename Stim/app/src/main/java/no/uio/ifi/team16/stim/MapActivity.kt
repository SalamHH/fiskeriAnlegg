package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import no.uio.ifi.team16.stim.databinding.ActivityMapBinding

class MapActivity : StimActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    private val TAG = "MapActivity"
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnCameraMoveListener(this)
        val bounds = LatLngBounds(LatLng(60.0, 10.0), LatLng(60.5, 10.5))
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10)
        map.moveCamera(cameraUpdate)
    }

    override fun onCameraMove() {
        Log.d(TAG, "camera moved to ${map.cameraPosition}")
    }
}