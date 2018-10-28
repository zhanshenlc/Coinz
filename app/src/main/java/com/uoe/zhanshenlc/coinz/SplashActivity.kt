package com.uoe.zhanshenlc.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
//import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import com.uoe.zhanshenlc.coinz.myDownload.DownloadCompleteListener
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 4000

    private val tag = "SplashActivity"
    private var downloadDate: String? = "" // YYYY/MM/DD
    //private val preferencesFile = "/data/data/com.uoe.zhanshenlc.coinz/shared_prefs/MyPrefsFile.xml"
    private val preferencesFile = "MyPrefsFile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val today: String = SimpleDateFormat("YYYY/MM/dd", Locale.getDefault()).format(Date())
        /*if (today == downloadDate) {
            Handler().postDelayed({
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }, splashTimeOut)
        } else {*/
            DownloadFileTask(object : DownloadCompleteListener {
                var result: String? = null
                override fun downloadComplete(result: String) {
                    this.result = result

                    writeToFile(result)

                    downloadDate = today
                    /*var file: File = Environment.getExternalStorageDirectory()
                file = File(file, "coinzmap.geojson")
                file.createNewFile()
                val fout = FileOutputStream(file)
                val outwriter = OutputStreamWriter(fout)
                outwriter.append(result)
                outwriter.close()
                fout.flush()
                fout.close()*/

                    Handler().postDelayed({
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    }, splashTimeOut)
                }
            }).execute("http://homepages.inf.ed.ac.uk/stg/coinz/$today/coinzmap.geojson")
        //}
    }

    @SuppressLint("StaticFieldLeak")
    internal class DownloadFileTask (private val caller: DownloadCompleteListener) : AsyncTask<String, Void, String>() {
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

    /* Checks if external storage is available for read and write */
    /*private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /* Checks if external storage is available to at least read */
    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }*/

    /*fun getPrivateAlbumStorageDir(context: Context, albumName: String): File? {
        // Get the directory for the app's private pictures directory.
        isExternalStorageReadable()
        isExternalStorageWritable()
        val file = File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), albumName)
        if (!file.mkdirs()) {
            Log.d(tag, "Directory not created")
        }
        return file
    }*/

    fun writeToFile(data: String) {
        // Get the directory for the user's public pictures directory.
        /*val path = Environment.getExternalStoragePublicDirectory(
                //Environment.DIRECTORY_PICTURES
                Environment.DIRECTORY_DCIM + "/YourFolder/"
        )

        // Make sure the path directory exists.
        if (!path.exists()) {
            // Make it, if it doesn't exit
            path.mkdirs()
        }*/

        val file = File( "/data/data/com.uoe.zhanshenlc.coinz/files", "coinzmap.geojson")

        // Save your stream, don't forget to flush() it before closing it.

        try {
            val fos: FileOutputStream = openFileOutput("coinzmap.geojson", Context.MODE_PRIVATE)
            fos.write(data.toByteArray())
            fos.close()
            /*file.createNewFile()
            val fOut = FileOutputStream(file)
            val myOutWriter = OutputStreamWriter(fOut)
            myOutWriter.append(data)

            myOutWriter.close()

            fOut.flush()
            fOut.close()*/
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }

    }

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
