package anroid.threadhandler.com.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.util.Log
import android.webkit.URLUtil
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class LocalService : Service() {

    private val binder = LocalBinder()
    lateinit var resultReceiver: ResultReceiver


    override fun onBind(intent: Intent?): IBinder? {
        resultReceiver = intent!!.getParcelableExtra("receiver")
        return binder
    }

    inner class LocalBinder : Binder(){
        fun getService():LocalService = this@LocalService
    }

    fun downLoadImage(url:String){
        DownLoadThread(resultReceiver,url,applicationContext).start()
    }


    class DownLoadThread(resultReceiver: ResultReceiver,url:String,context: Context) :Thread(){
        private val resultReceiver = resultReceiver
        private val url = url
        private val context = context

        override fun run() {
            var input: InputStream ?= null
            var output: OutputStream ?= null
            var connection : HttpURLConnection?= null
            val file = File(context.filesDir,"downloadedImage")
            val bundle = Bundle()
            var currentSize: Long = 0
            var imageSize :Long = 0


            try {
                val urlConnection = URL(url)
                connection = urlConnection.openConnection() as HttpURLConnection
                connection.connect()
                imageSize = connection.contentLength.toLong()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return
                }

                input = connection.inputStream

                input.let {

                    output = FileOutputStream(file, false) as OutputStream?
                    val data = ByteArray(4096)
                    var count: Int
                    do {
                    count = input.read(data)
                    if (count > 1) {
                        output!!.write(data, 0, count)
                        currentSize += count.toLong()
                        bundle.putInt("progress", (100 * currentSize / imageSize).toInt())
                        resultReceiver.send(111, bundle)
                    } else {
                        break
                    }

                } while (count != -1)

                    val newBundle:Bundle = Bundle()
                    if(file.exists()){
                        newBundle.putString("path",file.path)
                        newBundle.putInt("progress",100);
                        resultReceiver.send(112,newBundle)
                    }
                }
            }catch (e:Exception){
                Log.e("Exception",e.message)
            }finally {
                try {
                    output?.close()
                    input?.close()
                    connection?.disconnect()
                }catch (e:IOException){
                    Log.e("IO-exception",e.message)
                }
            }
        }
    }
}