package com.uoe.zhanshenlc.coinz

import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.model.Document
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import kotlinx.android.synthetic.main.activity_login.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener {

    private val tag = "MainActivity"
    private var mapView: MapView? = null
    private var map: MapboxMap? = null

    private lateinit var originLocation: Location
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var locationLayerPlugin: LocationLayerPlugin

    private var mAuth = FirebaseAuth.getInstance()
    private var firestore: FirebaseFirestore? = null
    private var firestoreChat: DocumentReference? = null

    private var collected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)
        //Mapbox.getInstance(this, "");
        Mapbox.getInstance(applicationContext, getString(R.string.access_token))

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) { Log.d(tag, "[onMapReady] mapbox is null") }
        else {
            val geoJson = BufferedReader(InputStreamReader(openFileInput("coinzmap.geojson")))
                    .lines().collect(Collectors.joining(System.lineSeparator()))
            val featureCollection = FeatureCollection.fromJson(geoJson)
            for (f: Feature in featureCollection.features()!!.iterator()) {
                val jo = f.properties()
                val title = jo!!.get("value").asString
                val currency = jo.get("currency").asString + "\nClick to collect"
                val geo: Point = Point.fromJson(f.geometry()!!.toJson())
                // Thanks to the website below for providing free icons
                // https://www.flaticon.com/packs/simpleicon-ecommerce
                val color = jo.get("marker-color")?.asString
                lateinit var icon: Icon
                when (color) {
                    "#ffdf00" -> icon = IconFactory.getInstance(this@MainActivity).fromResource(R.drawable.yellow_coin_24) // QUID
                    "#0000ff" -> icon = IconFactory.getInstance(this@MainActivity).fromResource(R.drawable.blue_coin_24) // SHIL
                    "#ff0000" -> icon = IconFactory.getInstance(this@MainActivity).fromResource(R.drawable.red_coin_24) // PENY
                    "#008000" -> icon = IconFactory.getInstance(this@MainActivity).fromResource(R.drawable.green_coin_24) // DOLR
                }
                mapboxMap.addMarker(MarkerOptions().title(title).snippet(currency).icon(icon).
                        position(LatLng(geo.latitude(), geo.longitude())))
            }

            /*mapboxMap.setInfoWindowAdapter {
                val a = layoutInflater.inflate(R.layout.coin_info_window, null)
                a.setBackgroundColor(Color.RED)
                val b = a.findViewById<TextView>(R.id.titlea)
                b.text = "heyhey"
                a
            }*/

            mapboxMap.setOnInfoWindowClickListener { it ->
                val lastLocation = locationEngine.lastLocation
                val canCollect = collectable(lastLocation.latitude, lastLocation.longitude,
                        it.position.latitude, it.position.longitude)
                if (!collected) {
                    if (canCollect) {
                        collected = true
                        Toast.makeText(applicationContext, it.position.latitude.toString(), Toast.LENGTH_SHORT).show()
                        it.remove()
                    } else Toast.makeText(applicationContext, "Out of reach", Toast.LENGTH_SHORT).show()
                } else collected = false

                // https://grokonez.com/android/kotlin-firestore-example-crud-operations-with-recyclerview-android
                val db = FirebaseFirestore.getInstance()

                val noteDataMap = HashMap<String, Any>()
                noteDataMap["date"] = "20181124"

                db.collection("users")
                        .document(mAuth.uid.toString())
                        .collection("Coins")
                        .document("today")
                        .set(noteDataMap)
                        .addOnSuccessListener(OnSuccessListener<Void> {
                            Log.d(tag, "???DocumentSnapshot successfully written!")
                        })
                        .addOnFailureListener(OnFailureListener {
                            e -> Log.w(tag, "???Error writing document", e)
                        })

                val docRef = db.collection("users").document(mAuth.uid.toString())
                docRef.get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null) {
                            Log.d(tag, "???DocumentSnapshot data: " + task.result!!.data)
                        } else {
                            Log.d(tag, "???No such document")
                        }
                    } else {
                        Log.d(tag, "???get failed with ", task.exception)
                    }
                })

                /*val settings = FirebaseFirestoreSettings.Builder()
                        .setTimestampsInSnapshotsEnabled(true)
                        .build()
                firestore = FirebaseFirestore.getInstance()
                firestore?.firestoreSettings = settings

                val c: DocumentReference = firestore?.collection("users")?.document(mAuth.uid.toString())!!
                System.out.println("???")
                val document = Tasks.await(c.get())
                System.out.println("????"+document.exists())
                val publicProfile = document.toObject(User::class.java)
                System.out.println("?????" + publicProfile?.email)
                Toast.makeText(applicationContext, publicProfile?.email
                        , Toast.LENGTH_LONG).show()*/

                false
            }

            mapboxMap.setOnInfoWindowCloseListener { _ -> collected = false }

            map = mapboxMap
            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true
            enableLocation()
        }
    }

    private fun getPublicProfile(userId: String): User? {
        return try {
            //Get "PublicProfile" collection reference
            val privateDataRef = firestore?.collection("Users")?.document(userId)
            val document = Tasks.await(privateDataRef!!.get())
            //Check if data exists
            if (document.exists()) {
                //Cast the given DocumentSnapshot to our POJO class
                val publicProfile = document.toObject(User::class.java)
                publicProfile
            } else null
            //Task successful
        } catch (e: Throwable) {
            //Manage error
            null
        }
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initialiseLocationEngine()
            initialiseLocationLayer()
        } else {
            Log.d(tag, "Permissions are not granted")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine.apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        } else { locationEngine.addLocationEngineListener(this) }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationLayer() {
        if (mapView == null) { Log.d(tag, "mapView is null") }
        else {
            if (map == null) { Log.d(tag, "map is null") }
            else {
                locationLayerPlugin = LocationLayerPlugin(mapView!!, map!!, locationEngine)
                locationLayerPlugin.apply {
                    setLocationLayerEnabled(true)
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.NORMAL
                }
            }
        }
    }

    private fun setCameraPosition(location: Location) {
        val latlng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLng(latlng))
    }

    override fun onLocationChanged(location: Location?) {
        if (location == null) { Log.d(tag, "[onLocationChanged] location is null") }
        else {
            originLocation = location
            setCameraPosition(originLocation)
        }
    }

    @SuppressWarnings("MissingPermissions")
    override fun onConnected() {
        Log.d(tag, "[onConnected] requesting location updates")
        locationEngine.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Log.d(tag, "Permissions: $permissionsToExplain")
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag, "[onPermissionResult] granted == $granted")
        if (granted) { enableLocation() }
    }

    //@SuppressWarnings("MissingPermissions")
    override fun onStart() {
        super.onStart()
        /*if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationEngine.requestLocationUpdates()
            locationLayerPlugin.onStart()
        }*/
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationEngine.removeLocationUpdates()
        locationLayerPlugin.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        locationEngine.deactivate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    private fun collectable(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Boolean {

        val r = 6371 // Radius of the earth

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = r.toDouble() * c * 1000.0 // convert to meters

        return distance <= 100
    }

    data class User(
            val user_id: String = "",
            val name: String?,
            val email: String = "",
            val friends: Array<String>?,
            val Coins: CollectionReference
    )
}
