package com.uoe.zhanshenlc.coinz

import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
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
import com.uoe.zhanshenlc.coinz.dataModels.CoinToday
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener {

    private val tag = "MainActivity"
    private var mapView: MapView? = null
    private var map: MapboxMap? = null

    private var originLocation: Location? = null
    private var permissionsManager: PermissionsManager? = null
    private var locationEngine: LocationEngine? = null
    private var locationLayerPlugin: LocationLayerPlugin? = null

    private var mAuth = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    private lateinit var toggle: ActionBarDrawerToggle

    private val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())
    private var currenciesNotCollected = HashMap<String, String>()
    private var valuesNotCollected = HashMap<String, Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Mapbox.getInstance(applicationContext, getString(R.string.access_token))
        mapView = findViewById(R.id.mapView_main)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        // https://medium.com/quick-code/android-navigation-drawer-e80f7fc2594f
        // https://code.tutsplus.com/tutorials/how-to-code-a-navigation-drawer-in-an-android-app--cms-30263
        val drawer = findViewById<DrawerLayout>(R.id.sidebar_main)
        toggle = ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navView: NavigationView = findViewById(R.id.navView_main)
        // Click events for the items in navigation drawer
        navView.setNavigationItemSelectedListener { it ->
            when (it.itemId) {
                R.id.profile_sidebar -> startActivity(Intent(applicationContext, ProfileActivity::class.java))
                R.id.wallet_sidebar -> {
                    // to check whether mission is completed today
                    fireStore.collection("shopping carts")
                            .document(mAuth.currentUser?.email.toString()).get()
                            .addOnSuccessListener {
                                Log.d(tag, "Read data success.")
                                if (it.data != null) {
                                    val date = it.data!!["date"] as String
                                    val missionComplete = it.data!!["missionComplete"] as Boolean
                                    if (date == today && missionComplete) {
                                        startActivity(Intent(applicationContext, WalletActivity::class.java))
                                    } else {
                                        Toast.makeText(this, "Please complete mission at shop first.",
                                                Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(this, "Please complete mission at shop first.",
                                            Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener { Log.e(tag, "Failed to read data with: $it") }
                }
                R.id.bank_sidebar -> startActivity(Intent(applicationContext, BankActivity::class.java))
                R.id.shop_sidebar -> startActivity(Intent(applicationContext, ShopActivity::class.java))
                R.id.friendList_sidebar -> startActivity(Intent(applicationContext, FriendListActivity::class.java))
                R.id.friendRequestList_sidebar -> startActivity(Intent(applicationContext, FriendRequestListActivity::class.java))
                R.id.signOut_sidebar -> {
                    mAuth.signOut()
                    Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()
                    if (File(this.cacheDir.toString(), "/iconTemp.jpg").exists()) {
                        File(this.cacheDir.toString(), "/iconTemp.jpg").delete()
                    }
                    finish()
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                }
            }
            drawer.closeDrawer(GravityCompat.START)
            true
        }
        // An entrance button for opening the navigation drawer
        mapView?.findViewById<ImageButton>(R.id.sidebar_map)?.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
        // The header of the navigation drawer
        navView.getHeaderView(0).findViewById<ImageView>(R.id.userIcon_navHeader).setOnClickListener {
            startActivity(Intent(applicationContext, UserIconActivity::class.java))
            drawer.closeDrawer(GravityCompat.START)
        }
        // Check if cache of user icon exists
        // If not, cache user icon if exists
        val iconFile = File(this.cacheDir, "iconTemp.jpg")
        if (! iconFile.exists()) {
            val email = mAuth.currentUser?.email.toString()
            val iconRef = FirebaseStorage.getInstance().reference.child("userIcons/$email.jpg")
            iconRef.getFile(iconFile)
                    .addOnSuccessListener {
                        Log.d(tag, "[getUserIcon] Success")
                        navView.getHeaderView(0).findViewById<ImageView>(R.id.userIcon_navHeader)
                                .setImageBitmap(BitmapFactory.decodeFile(this.cacheDir.toString() + "/iconTemp.jpg"))
                    }
                    .addOnFailureListener { e -> Log.e(tag, "[getUserIcon] Failed with: $e") }
        } else {
            navView.getHeaderView(0).findViewById<ImageView>(R.id.userIcon_navHeader)
                    .setImageBitmap(BitmapFactory.decodeFile(this.cacheDir.toString() + "/iconTemp.jpg"))
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) { Log.d(tag, "[onMapReady] mapbox is null") }
        else {
            // Get the GeoJson file and get a Feature Collection
            val geoJson = BufferedReader(InputStreamReader(openFileInput("coinzmap.geojson")))
                    .lines().collect(Collectors.joining(System.lineSeparator()))
            val featureCollection = FeatureCollection.fromJson(geoJson)
            // Check with online database and show the coins to be collected by the user
            fireStore.collection("today coins list").document(mAuth.currentUser?.email.toString())
                    .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                        if (firebaseFirestoreException != null) {
                            Log.d(tag, "Errors reading today's coin data: $firebaseFirestoreException")
                        } else {
                            Log.d(tag, "Successfully read today's coin data")
                            val currencies: HashMap<String, String>
                            // If new user or first visit today
                            // Update the database and show all 50 coins
                            if (documentSnapshot!!.data == null || documentSnapshot.data!!["date"] != today) {
                                currencies = HashMap()
                                Log.d(tag, "First visit on $today")
                                fireStore.collection("today coins list")
                                        .document(mAuth.currentUser?.email.toString())
                                        .set(CoinToday(today).toMap())
                                        .addOnCompleteListener { Log.d(tag, "Success uploading data") }
                                        .addOnFailureListener { e -> Log.e(tag, "Fail to upload data with $e") }
                            } else {
                                // We just need the IDs of the coins that has been collected today
                                currencies = documentSnapshot.data!!["currencies"] as HashMap<String, String>
                            }
                            for (f: Feature in featureCollection.features()!!.iterator()) {
                                val jo = f.properties()
                                val id = jo!!.get("id").asString
                                // Filter out collected coins
                                if (currencies.containsKey(id)) {
                                    continue
                                }
                                val value = jo.get("value").asDouble
                                val currency = jo.get("currency").asString
                                // Store information of coins to be collected
                                currenciesNotCollected[id] = currency
                                valuesNotCollected[id] = value
                                // Create info windows
                                val snippet = "Value: $value\nCurrency: $currency\nClick to collect"
                                val geo: Point = Point.fromJson(f.geometry()!!.toJson())
                                // Thanks to the website below for providing free icons
                                // https://www.flaticon.com/packs/simpleicon-ecommerce
                                val color = jo.get("marker-color")?.asString
                                lateinit var icon: Icon
                                when (color) {
                                    "#ffdf00" -> icon = IconFactory.getInstance(this@MainActivity)
                                            .fromResource(R.drawable.yellow_coin_24) // QUID
                                    "#0000ff" -> icon = IconFactory.getInstance(this@MainActivity)
                                            .fromResource(R.drawable.blue_coin_24) // SHIL
                                    "#ff0000" -> icon = IconFactory.getInstance(this@MainActivity)
                                            .fromResource(R.drawable.red_coin_24) // PENY
                                    "#008000" -> icon = IconFactory.getInstance(this@MainActivity)
                                            .fromResource(R.drawable.green_coin_24) // DOLR
                                }
                                // Add coins on map
                                mapboxMap.addMarker(MarkerOptions().title(id).snippet(snippet).icon(icon)
                                        .position(LatLng(geo.latitude(), geo.longitude())))
                            }
                        }
                    }

            // Set collect behaviour
            mapboxMap.setOnInfoWindowClickListener { it ->
                if (locationEngine != null) {
                    val lastLocation = locationEngine?.lastLocation
                    if (lastLocation != null) {
                        // Check whether distance between coin and user is in range (25m) or not
                        val canCollect = collectable(lastLocation.latitude, lastLocation.longitude,
                                it.position.latitude, it.position.longitude)
                        if (canCollect) {
                            Toast.makeText(applicationContext, "Coin Collected", Toast.LENGTH_SHORT).show()
                            fireStore.collection("today coins list").document(mAuth.currentUser?.email.toString())
                                    .get().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d(tag, "Today's coins loaded")
                                            // Retrieve data of coins collected
                                            val currencies = task.result!!.data!!["currencies"]!! as HashMap<String, String>
                                            val values = task.result!!.data!!["values"]!! as HashMap<String, Double>
                                            // Add coin into collected list
                                            currencies[it.title] = currenciesNotCollected[it.title]!!
                                            values[it.title] = valuesNotCollected[it.title]!!
                                            // Remove coin from to be collected list
                                            currenciesNotCollected.remove(it.title)
                                            valuesNotCollected.remove(it.title)
                                            // Update collected list in database
                                            fireStore.collection("today coins list")
                                                    .document(mAuth.currentUser?.email.toString())
                                                    .update(CoinToday(currencies, values).updateCollection())
                                                    .addOnSuccessListener { Log.d(tag, "Success update collection") }
                                                    .addOnFailureListener { e -> Log.e(tag, "Fail to update collection with: $e") }
                                        } else {
                                            Log.d(tag, "Get data from database failed with ", task.exception)
                                        }
                                    }
                            it.remove()
                        } else Toast.makeText(applicationContext, "Out of Reach", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(applicationContext, "Fail to get last location", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(applicationContext, "Please grant location permission", Toast.LENGTH_LONG).show()
                false
            }

            /*mapboxMap.setInfoWindowAdapter {
                val a = layoutInflater.inflate(R.layout.coin_info_window, null)
                a.setBackgroundColor(Color.RED)
                val b = a.findViewById<TextView>(R.id.titlea)
                b.text = "heyhey"
                a
            }*/  // Customise Info Window; Would implement if time allows

            map = mapboxMap
            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true
            enableLocation()
        }
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initialiseLocationEngine()
            initialiseLocationLayer()
        } else {
            Log.d(tag, "Permissions are not granted")
            permissionsManager = PermissionsManager(this)
            permissionsManager?.requestLocationPermissions(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine?.apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        } else { locationEngine?.addLocationEngineListener(this) }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationLayer() {
        if (mapView == null) { Log.d(tag, "mapView is null") }
        else {
            if (map == null) { Log.d(tag, "map is null") }
            else {
                locationLayerPlugin = LocationLayerPlugin(mapView!!, map!!, locationEngine)
                locationLayerPlugin?.apply {
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
            setCameraPosition(originLocation!!)
        }
    }

    @SuppressWarnings("MissingPermissions")
    override fun onConnected() {
        Log.d(tag, "[onConnected] requesting location updates")
        locationEngine?.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Log.d(tag, "Permissions: $permissionsToExplain")
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag, "[onPermissionResult] granted == $granted")
        if (granted) { enableLocation() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(tag, "[onRequestPermissionsResult] called")
        enableLocation()
    }

    //@SuppressWarnings("MissingPermissions")
    override fun onStart() {
        super.onStart()
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        // Update user icon on navigation drawer after setting new user icon
        val navView = findViewById<NavigationView>(R.id.navView_main)
        navView.getHeaderView(0).findViewById<ImageView>(R.id.userIcon_navHeader)
                .setImageBitmap(BitmapFactory.decodeFile(this.cacheDir.toString() + "/iconTemp.jpg"))
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        locationEngine?.deactivate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun collectable(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Boolean {

        val r = 6371 // Radius of the earth

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = r.toDouble() * c * 1000.0 // convert to meters

        return distance <= 25
    }

}
