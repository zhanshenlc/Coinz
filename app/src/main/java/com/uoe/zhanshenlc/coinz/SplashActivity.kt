package com.uoe.zhanshenlc.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.uoe.zhanshenlc.coinz.myDownload.DownloadCompleteListener
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 4000 // 4 seconds waiting time

    private val tag = "SplashActivity"
    private var downloadDate: String? = "" // YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile"

    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mAuth = FirebaseAuth.getInstance()
        onStart()
        val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())
        // Check if file has been downloaded, normal file size should be greater than 15 KB
        if (today == downloadDate && getFileSize("coinzmap.geojson") > 15.0 * 1024) {
            Toast.makeText(applicationContext, "Coinzmap file found", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                goToMapOrNot(currentUser)
            }, splashTimeOut)
        } else {
            DownloadFileTask(object : DownloadCompleteListener {
                var result: String? = null
                override fun downloadComplete(result: String) {
                    this.result = result
                    writeToFile(result)
                    downloadDate = today
                    Toast.makeText(applicationContext, "Download coinzmap file", Toast.LENGTH_SHORT).show()
                    Handler().postDelayed({
                        goToMapOrNot(currentUser)
                    }, splashTimeOut)
                }
            }).execute("http://homepages.inf.ed.ac.uk/stg/coinz/$today/coinzmap.geojson")
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal class DownloadFileTask (private val caller: DownloadCompleteListener): AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg urls: String?): String = try {
            loadFileFromNetworks(urls[0]!!)
        } catch (e: IOException) {
            "Unable to load content. Check your network connection"
        }

        private fun loadFileFromNetworks (urlString: String) : String {
            val stream: InputStream = downloadUrl(urlString)
            return BufferedReader(InputStreamReader(stream)).lines().collect(Collectors.joining(System.lineSeparator()))
        }

        @Throws(IOException::class)
        private fun downloadUrl(urlString: String): InputStream {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 10000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect()
            return conn.inputStream
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            caller.downloadComplete(result!!)
        }
    }

    // Full screen
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onStart() {
        super.onStart()
        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use "" as the default value (this might be the first time the app runs)
        downloadDate = settings.getString("lastDownloadDate", "")
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '$downloadDate'")

        currentUser = mAuth?.currentUser
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "[onStop] Storing lastDownloadDate of $downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        // Apply the edits!
        editor.apply()
    }

    private fun writeToFile(data: String) {
        try {
            val fos: FileOutputStream = openFileOutput("coinzmap.geojson", Context.MODE_PRIVATE)
            fos.write(data.toByteArray())
            fos.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

    private fun getFileSize(data: String): Double {
        val file = File(filesDir, data)
        return file.length().toDouble()
    }

    private fun goToMapOrNot(user: FirebaseUser?) {
        if (user == null) startActivity(Intent(applicationContext, RegisterActivity::class.java))
        else startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

}
