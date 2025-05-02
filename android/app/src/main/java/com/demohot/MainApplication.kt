package com.demohot

import android.app.Application
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.PackageManagerCompat.LOG_TAG
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.flipper.ReactNativeFlipper
import com.facebook.soloader.SoLoader
import java.io.File

class MainApplication : Application(), ReactApplication {

  override val reactNativeHost: ReactNativeHost =
      object : DefaultReactNativeHost(this) {
        override fun getPackages(): List<ReactPackage> =
            PackageList(this).packages.apply {
              // Packages that cannot be autolinked yet can be added manually here, for example:
              add(MyAppPackage())
              add(MyUpdatePackage())

            }
        override fun getJSBundleFile(): String? {
//          val bundleUrl = UpdateContext.getBundleUrl(this@MainApplication)
//            Log.d("MainApplication", "Bundle URL: $bundleUrl")
//                  return "http://10.56.238.168:8080/index.android.bundle"
        //          return "ws://localhost:8080/message?device=Android%20SDK%20built%20for%20x86_64%20-%2013%20-%20API%2033&app=com.awesomeproject&clientid=BridgeDevSupportManager"
//            // 获取jsBundle所在的文件夹
//            val dir = FileConstants.getJsBundleLocalPath(this.application)
//            val file = File(dir)
//            // 判断文件是否存在 , 存在就说明有新的jsBundle , 不存在就使用默认的bundle
//             if (file.exists()) {
//                 return dir
//            }
//          return bundleUrl
            // 获取jsBundle所在的文件夹
            Log.d(LOG_TAG, "================================")
            Log.d("path", application.externalCacheDir!!.absolutePath)

            Log.d(LOG_TAG, "================================")

            val dir = FileConstants.getJsBundleLocalPath(application.externalCacheDir!!.absolutePath)
            val file = File(dir);
            Toast.makeText(this.application.applicationContext,dir, Toast.LENGTH_LONG).show()

            // 判断文件是否存在 , 存在就说明有新的jsBundle , 不存在就使用默认的bundle
            if (file.exists()){
                Toast.makeText(this.application.applicationContext, "文件存在:$dir",Toast.LENGTH_LONG).show()
                return dir;
            }
            return super.getJSBundleFile();
//            return Environment.getExternalStorageDirectory().absolutePath+File.separator+"bundles/index.android.bundle"

        }
        override fun getJSMainModuleName(): String = "index"

        override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

        override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
        override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
      }

  override val reactHost: ReactHost
    get() = getDefaultReactHost(this.applicationContext, reactNativeHost)

  override fun onCreate() {
    super.onCreate()
    SoLoader.init(this, false)
    if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
      // If you opted-in for the New Architecture, we load the native entry point for this app.
      load()
    }
    ReactNativeFlipper.initializeFlipper(this, reactNativeHost.reactInstanceManager)
  }
}
