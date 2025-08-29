package com.whatsappzerotapauth

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.Promise

/**
 * 新架构 (TurboModule) 版本的模块
 */
class WhatsappZeroTapAuthModule(reactContext: ReactApplicationContext) :
  NativeWhatsappZeroTapAuthSpec(reactContext) {

  companion object {
    const val NAME = "WhatsappZeroTapAuth"
  }
  
  private val moduleImpl = WhatsappZeroTapAuthModuleImpl(reactApplicationContext)

  override fun getName(): String {
    return NAME
  }

  override fun multiply(a: Double, b: Double): Double {
    return moduleImpl.multiply(a, b)
  }

  override fun isWhatsAppInstalled(promise: Promise) {
    moduleImpl.isWhatsAppInstalled(promise)
  }

  override fun getInstalledWhatsAppApps(promise: Promise) {
    moduleImpl.getInstalledWhatsAppApps(promise)
  }

  override fun initiateHandshake(promise: Promise) {
    moduleImpl.initiateHandshake(promise)
  }

  override fun getAppSignatureHash(promise: Promise) {
    moduleImpl.getAppSignatureHash(promise)
  }

  override fun getDeviceInfo(promise: Promise) {
    moduleImpl.getDeviceInfo(promise)
  }

  override fun addListener(eventName: String) {
    // Set up any upstream listeners or background tasks as necessary
  }

  override fun removeListeners(count: Double) {
    // Remove upstream listeners, stop unnecessary background tasks
  }
}
