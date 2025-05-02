package com.demohot // replace your-apps-package-name with your app’s package name
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import kotlin.system.exitProcess


class CalendarModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    // add to CalendarModule.kt
    override fun getName() = "CalendarModule"
//    const {CalendarModule} = ReactNative.NativeModules;


    @ReactMethod
    fun createCalendarEvent(name: String, location: String, callback: Callback) {
        Toast.makeText(this.reactApplicationContext,name,Toast.LENGTH_LONG).show()
        Log.d("CalendarModule","Create event called with name: $name and location: $location")
        val eventId =name
        callback.invoke(eventId)
        System.exit(0)
    }

    @ReactMethod
    fun restartApp() {
        val i = reactApplicationContext.packageManager.getLaunchIntentForPackage(
            reactApplicationContext.packageName
        )
        i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        reactApplicationContext.startActivity(i)
    }
}