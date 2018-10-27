package com.uoe.zhanshenlc.coinz

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.renderscript.ScriptGroup
import android.util.Log
import android.view.View
import com.uoe.zhanshenlc.coinz.myDownload.DownloadCompleteListener
import com.uoe.zhanshenlc.coinz.myDownload.DownloadCompleteRunner
import com.uoe.zhanshenlc.coinz.myDownload.DownloadCompleteTask
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class SplashActivity : AppCompatActivity() {

    private var handler: Handler? = null

    private val tag = "SplashActivity"
    private var downloadDate = "" // YYYY/MM/DD
    //private val preferencesFile = "/data/data/com.uoe.zhanshenlc.coinz/shared_prefs/MyPrefsFile.xml"
    private val preferencesFile = "MyPrefsFile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //handler = Handler()
        //val caller: DownloadCompleteListener = DownloadCompleteRunner
        //val downloadCompleteTask = DownloadCompleteTask(caller)
        //downloadCompleteTask.execute("http://homepages.inf.ed.ac.uk/stg/coinz/2018/10/03/coinzmap.geojson")
        val runnable: Runnable = Runnable {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /*override fun onStart() {
        super.onStart()
        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use "" as the default value (this might be the first time the app runs)
        downloadDate = settings.getString("lastDownloadDate", "")
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '$downloadDate'")
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
    }*/



    /*val SPLASH_TIME_OUT = 4000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(object : Runnable{
            override fun run() {
                val home = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(home)
                finish()
            }
        }, SPLASH_TIME_OUT.toLong())
    }*/

    /*private var delayHandler: Handler? = null
    private val timeout: Long = 3000 //3 seconds

    private val runnable: Runnable = Runnable {

        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Initializing the Handler
        delayHandler = Handler()

        //Navigate with delay
        delayHandler!!.postDelayed(runnable, timeout)
    }

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
    }*/

}
