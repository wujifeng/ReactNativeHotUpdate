package com.demohot

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import okio.IOException
import java.io.File
import java.util.Timer
import java.util.TimerTask

class HotUpdate(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext)  {
    // 将接收器作为类成员变量，并在构造函数中注册
    private val downloadCompleteListener = DownloadCompleteReceiver { downloadedFilePath ->
        Log.d(TAG, "Stored at: $downloadedFilePath")
        Toast.makeText(this@HotUpdate.reactApplicationContext, "请手动重启APP. Stored at: "+downloadedFilePath, Toast.LENGTH_SHORT).show()

        // 在这里处理下载成功的逻辑，如通知RN端等
    }
    init {
        reactApplicationContext.registerReceiver(downloadCompleteListener, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

    }

    override fun getName(): String {
        return "HotUpdate"
    }

    var mDownloadId: Long = 0
    var downloadManager: DownloadManager? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val completeId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (completeId == mDownloadId) {
                Toast.makeText(context, "下载成功!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //检查下载状态
    private fun checkDownloadStatus(downloadId: Long, timer: Timer?): String {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId) //筛选下载任务，传入任务ID，可变参数
        val c = downloadManager!!.query(query)
        var hasCancel = false
        var flag = ""
        if (c.moveToFirst()) {
            @SuppressLint("Range") val status =
                c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_PAUSED -> flag = "pause"
                DownloadManager.STATUS_PENDING -> {
                    hasCancel = true
                    flag = "timeout"
                }

                DownloadManager.STATUS_RUNNING -> flag = "downloading"
                DownloadManager.STATUS_SUCCESSFUL -> {
                    hasCancel = true
                    flag = "success"
                }

                DownloadManager.STATUS_FAILED -> {
                    hasCancel = true
                    flag = "fail"
                }
            }
            if (timer != null && hasCancel) timer.cancel()
        }
        return flag
    }

    //下载JSBundle
    private fun downloadBundle(url:String) {
        val file = File(FileConstants.getJsBundleLocalPath())
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        } else if (file.exists()) {
            file.delete()
        }

        downloadManager = reactApplicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        // 设置通知栏显示, 我这里不需要, 就隐藏吧
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        // 设置网络模式
        // 这里得记得加入对应的网络权限
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.setDestinationUri(Uri.fromFile(file))
        mDownloadId = downloadManager!!.enqueue(request)

        // 对了  <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" /> 这个权限也得加上
    }

    @ReactMethod
    fun update(url:String) {

        // 刷新下载进度
        val timer = Timer()
        // 上一个时间点的下载进度 , 用于计算下载速度
        val last = intArrayOf(0)

        downloadBundle(url)

        // 定时任务, 每秒钟请求下下载状态
        val task: TimerTask = object : TimerTask() {
            @SuppressLint("Range")
            override fun run() {
                // 这玩意儿相当于给js传json
                val map = Arguments.createMap()
                val query = DownloadManager.Query()
                val cursor = downloadManager!!.query(query.setFilterById(mDownloadId))
                if (cursor != null && cursor.moveToFirst()) {
                    // 检查下载状态
                    val downloadFlag = checkDownloadStatus(mDownloadId, timer)
                    // 已下载大小
                    val downloaded =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    // 文件总大小
                    val total =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    // 下载进度(百分比)
                    val pro = downloaded * 100 / total
                    // 传值
                    map.putString("status", downloadFlag)
                    map.putInt("progress", pro)
                    map.putInt("fileSize", total)
                    map.putInt("downloaded", downloaded)
                    map.putInt("speed", downloaded - last[0])
                    last[0] = downloaded
                    // 发送事件, 在js中监听该事件, 获取数据
                    reactApplicationContext.getJSModule(
                        DeviceEventManagerModule.RCTDeviceEventEmitter::class.java
                    ).emit("onUpdateDownload", map)
                }
                cursor!!.close()
            }
        }
        timer.schedule(task, 0, 1000)


    }


    @ReactMethod
    fun createUpdateEvent(name: String, location: String, callback: Callback) {
        Toast.makeText(this.reactApplicationContext,FileConstants.getJsBundleLocalPath(),Toast.LENGTH_LONG).show()
        Log.d("CalendarModule","Create event called with name: $name and location: $location")
        val eventId =FileConstants.getJsBundleLocalPath()
        callback.invoke(eventId)
    }

    inner class DownloadCompleteReceiver(private val onDownloadSuccess: (String) -> Unit) : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (downloadId == mDownloadId) {
                    val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)

                    if (cursor.moveToFirst()) {
                        var status =this.getStatus(DownloadManager.COLUMN_STATUS,cursor)
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                var localUri="";
                                if (columnIndex != -1) {
                                    localUri = cursor.getString(columnIndex)
                                }
                                // 下载成功，调用回调函数并传入存储路径
                                onDownloadSuccess(localUri)
                            }
                            DownloadManager.STATUS_PENDING,
                            DownloadManager.STATUS_FAILED,
                            DownloadManager.STATUS_PAUSED,
                            DownloadManager.STATUS_PENDING,
                            DownloadManager.STATUS_RUNNING -> {
                                var error = this.getStatus(DownloadManager.COLUMN_REASON, cursor)
                                val errorMsg = when (error) {
                                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
                                    DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
                                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space"
                                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
                                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
                                    DownloadManager.ERROR_UNKNOWN -> "Unknown error"
                                    else -> "Unknown error code: $error"
                                }
                                Log.e(TAG, "Download failed with status $status and reason: $errorMsg")
                            }
                        }
                    }
                    cursor.close()
                }
            }
        }

        private fun getStatus(status: String, cursor: Cursor):Int{
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (columnIndex != -1) {
                return cursor.getInt(columnIndex)

            }
            return  0;
        }
    }
}