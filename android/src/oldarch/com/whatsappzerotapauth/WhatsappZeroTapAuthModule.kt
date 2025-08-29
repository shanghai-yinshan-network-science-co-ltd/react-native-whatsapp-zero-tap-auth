package com.whatsappzerotapauth

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.module.annotations.ReactModule

/**
 * 老架构版本的模块
 */
@ReactModule(name = WhatsappZeroTapAuthModule.NAME)
class WhatsappZeroTapAuthModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  companion object {
    const val NAME = "WhatsappZeroTapAuth"
  }
  
  private val moduleImpl = WhatsappZeroTapAuthModuleImpl(reactApplicationContext)

  override fun getName(): String {
    return NAME
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun multiply(a: Double, b: Double): Double {
    return moduleImpl.multiply(a, b)
  }

  @ReactMethod
  fun isWhatsAppInstalled(promise: Promise) {
    moduleImpl.isWhatsAppInstalled(promise)
  }

  @ReactMethod
  fun getInstalledWhatsAppApps(promise: Promise) {
    moduleImpl.getInstalledWhatsAppApps(promise)
  }

  @ReactMethod
  fun initiateHandshake(promise: Promise) {
    moduleImpl.initiateHandshake(promise)
  }

  @ReactMethod
  fun getAppSignatureHash(promise: Promise) {
    moduleImpl.getAppSignatureHash(promise)
  }

  @ReactMethod
  fun getDeviceInfo(promise: Promise) {
    moduleImpl.getDeviceInfo(promise)
  }

  @ReactMethod
  fun addListener(eventName: String) {
    // Set up any upstream listeners or background tasks as necessary
  }

  @ReactMethod
  fun removeListeners(count: Double) {
    // Remove upstream listeners, stop unnecessary background tasks
  }
}
