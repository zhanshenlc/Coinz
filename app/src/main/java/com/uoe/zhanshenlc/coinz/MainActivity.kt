package com.uoe.zhanshenlc.coinz

import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
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
import com.uoe.zhanshenlc.coinz.dataModels.BankAccount
import com.uoe.zhanshenlc.coinz.dataModels.UserModel
import com.uoe.zhanshenlc.coinz.dataModels.CoinToday
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener {

    private val tag = "MainActivity"
    private var mapView: MapView? = null
    private var map: MapboxMap? = null

    private lateinit var originLocation: Location
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var locationLayerPlugin: LocationLayerPlugin

    private var mAuth = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private var collected = false
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

        //val toolbar: Toolbar = findViewById(R.id.nav_header)
        //setSupportActionBar(toolbar)

        drawer = findViewById(R.id.sidebar_main)
        //https://medium.com/quick-code/android-navigation-drawer-e80f7fc2594f

        //https://code.tutsplus.com/tutorials/how-to-code-a-navigation-drawer-in-an-android-app--cms-30263

        toggle = ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navView: NavigationView = findViewById(R.id.navView_main)
        navView.setNavigationItemSelectedListener { it ->
            when (it.itemId) {
            //R.id.nav_item_one -> Toast.makeText(this, "Clicked item one", Toast.LENGTH_SHORT).show()
            //R.id.nav_item_two -> Toast.makeText(this, "Clicked item two", Toast.LENGTH_SHORT).show()
            //R.id.nav_item_three -> Toast.makeText(this, "Clicked item three", Toast.LENGTH_SHORT).show()
                R.id.todayCoin_sidebar -> {
                    Toast.makeText(this, "Coin List Today", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, TodayListActivity::class.java))
                }
                R.id.friendList_sidebar -> {
                    Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, FriendListActivity::class.java))
                }
                R.id.friendRequestList_sidebar -> {
                    Toast.makeText(this, "Friend Requests", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, FriendRequestListActivity::class.java))
                }
                R.id.signOut_sidebar -> {
                    mAuth.signOut()
                    Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                }
            }
            drawer.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) { Log.d(tag, "[onMapReady] mapbox is null") }
        else {
            val geoJson = BufferedReader(InputStreamReader(openFileInput("coinzmap.geojson")))
                    .lines().collect(Collectors.joining(System.lineSeparator()))
            val featureCollection = FeatureCollection.fromJson(geoJson)

            fireStore.collection("users").document(mAuth.uid.toString()).collection("coins")
                    .document("today").addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                        if (firebaseFirestoreException != null) {
                            Log.d(tag, "Errors reading today's coin data: $firebaseFirestoreException")
                        } else {
                            Log.d(tag, "Successfully read today's coin data")
                            val currencies: HashMap<String, String>
                            if (documentSnapshot!!.data == null || documentSnapshot.data!!["date"] != today) {
                                currencies = HashMap()
                                Log.d(tag, "First visit on $today")
                                fireStore.collection("users").document(mAuth.uid.toString())
                                        .collection("coins").document("today")
                                        .set(CoinToday(today).toMap())
                                        .addOnCompleteListener { Log.d(tag, "") }
                                        .addOnFailureListener { Log.d(tag, "") }
                            } else {
                                currencies = documentSnapshot.data!!["currencies"] as HashMap<String, String>
                            }
                            for (f: Feature in featureCollection.features()!!.iterator()) {
                                val jo = f.properties()
                                val id = jo!!.get("id").asString
                                if (currencies.containsKey(id)) {
                                    continue
                                }
                                val value = jo.get("value").asDouble
                                val currency = jo.get("currency").asString
                                currenciesNotCollected[id] = currency
                                valuesNotCollected[id] = value
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
                                mapboxMap.addMarker(MarkerOptions().title(id).snippet(snippet).icon(icon)
                                        .position(LatLng(geo.latitude(), geo.longitude())))
                            }
                        }
                    }

            /*for (f: Feature in featureCollection.features()!!.iterator()) {
                val jo = f.properties()
                val id = jo!!.get("id").asString
                //if (! todayCoin.collected(id)) { continue }
                val title = jo.get("value").asString
                val currency = jo.get("currency").asString + "\n$id\nClick to collect"
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

            }*/

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
                /*if (!collected) {
                    if (canCollect) {
                        collected = true
                        Toast.makeText(applicationContext, it.position.latitude.toString(), Toast.LENGTH_SHORT).show()
                        it.remove()

                        fireStore.collection("users").document(mAuth.uid.toString()).collection("coins")
                    } else Toast.makeText(applicationContext, "Out of reach", Toast.LENGTH_SHORT).show()
                } else collected = false*/

                if (canCollect) {
                    Toast.makeText(applicationContext, "Coin Collected", Toast.LENGTH_SHORT).show()
                    fireStore.collection("users").document(mAuth.uid.toString()).collection("coins")
                            .document("today").get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(tag, "Today's coins loaded")
                                    val currencies = task.result!!.data!!["currencies"]!! as HashMap<String, String>
                                    val values = task.result!!.data!!["values"]!! as HashMap<String, Double>
                                    currencies[it.title] = currenciesNotCollected[it.title]!!
                                    values[it.title] = valuesNotCollected[it.title]!!
                                    currenciesNotCollected.remove(it.title)
                                    valuesNotCollected.remove(it.title)
                                    fireStore.collection("users").document(mAuth.uid.toString()).
                                                collection("coins").document("today").
                                                set(CoinToday(today, currencies, values).toMap())
                                } else {
                                    Log.d(tag, "Get data from database failed with ", task.exception)
                                }
                            }
                    it.remove()
                } else Toast.makeText(applicationContext, "Out of Reach", Toast.LENGTH_SHORT).show()
                // https://grokonez.com/android/kotlin-firestore-example-crud-operations-with-recyclerview-android
                /*val db = FirebaseFirestore.getInstance()

                System.out.println("???" + db.collection("users").document("a"))

                val noteDataMap = HashMap<String, Any>()
                noteDataMap["date"] = "20181124"

                db.collection("users")
                        .document(mAuth.uid.toString())
                        .collection("Coins")
                        .document("today")
                        .set(noteDataMap)
                        .addOnSuccessListener{
                            Log.d(tag, "???DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener{
                            e -> Log.w(tag, "???Error writing document", e)
                        }

                val docRef = db.collection("users").document("123")//mAuth.uid.toString())
                docRef.get().addOnCompleteListener{ task ->
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
                }*/

                /*val settings = FirebaseFirestoreSettings.Builder()
                        .setTimestampsInSnapshotsEnabled(true)
                        .build()
                fireStore = FirebaseFirestore.getInstance()
                fireStore?.firestoreSettings = settings

                val c: DocumentReference = fireStore?.collection("users")?.document(mAuth.uid.toString())!!
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
        locationEngine?.removeLocationUpdates()
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
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = r.toDouble() * c * 1000.0 // convert to meters

        return distance <= 100
    }

}
