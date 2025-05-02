package com.demohot

import android.os.Environment
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.ReactApplicationContext
import java.io.File


interface FileConstants {

    companion object {

        private var cacheDir: String=""

        const val JS_BUNDLE_DOWNLOAD_URL = "http://10.56.238.168:80/index.android.bundle"
        // 文件名
        private const val JS_BUNDLE_NAME = "index.android.bundle"

        // 文件夹名
        private const val JS_BUNDLE_DIR_NAME = "HotUpdate"

        // 相对路径
        private val JS_BUNDLE_RELATIVE_PATH: String
            get() = "$JS_BUNDLE_DIR_NAME/$JS_BUNDLE_NAME"

        // 获取JSBundle的完整路径
        fun getJsBundleLocalPath(cacheDir: String=""): String {
            if (cacheDir !=""){
                this.cacheDir = cacheDir;
            }else{
//                return "/storage/emulated/0/Android/data/com.demohot/cache/HotUpdate/index.android.bundle"
            }
//            return "/storage/emulated/0/Android/data/com.demohot/cache/HotUpdate/index.android.bundle"

//            return reactContext?.externalCacheDir?.absoluteFile?.toString() +"/"+ JS_BUNDLE_RELATIVE_PATH
            return this.cacheDir + File.separator+JS_BUNDLE_RELATIVE_PATH

        }
    }

}