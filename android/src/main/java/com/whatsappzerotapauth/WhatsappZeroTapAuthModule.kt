package com.whatsappzerotapauth

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule
import android.util.Log

@ReactModule(name = WhatsappZeroTapAuthModule.NAME)
class WhatsappZeroTapAuthModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  companion object {
    const val NAME = "WhatsappZeroTapAuth"
    private const val TAG = "WhatsappZeroTapAuthModule"
  }
  
  private val otpHandler = WhatsAppOtpHandler()

  init {
    // 设置ReactContext给广播接收器
    OtpCodeReceiver.setReactContext(reactContext)
  }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  /**
   * 检查WhatsApp是否已安装
   */
  @ReactMethod
  fun isWhatsAppInstalled(promise: Promise) {
    try {
      val isInstalled = otpHandler.isWhatsAppInstalled(reactApplicationContext)
      Log.d(TAG, "WhatsApp安装检查结果: $isInstalled")
      promise.resolve(isInstalled)
    } catch (e: Exception) {
      Log.e(TAG, "检查WhatsApp安装状态时出错", e)
      promise.reject("CHECK_INSTALLATION_ERROR", "检查WhatsApp安装状态失败: ${e.message}", e)
    }
  }

  /**
   * 获取已安装的WhatsApp应用列表
   */
  @ReactMethod
  fun getInstalledWhatsAppApps(promise: Promise) {
    try {
      val installedApps = otpHandler.getInstalledWhatsAppApps(reactApplicationContext)
      val result = Arguments.createArray()
      installedApps.forEach { packageName ->
        result.pushString(packageName)
      }
      Log.d(TAG, "已安装的WhatsApp应用: $installedApps")
      promise.resolve(result)
    } catch (e: Exception) {
      Log.e(TAG, "获取WhatsApp应用列表时出错", e)
      promise.reject("GET_APPS_ERROR", "获取WhatsApp应用列表失败: ${e.message}", e)
    }
  }

  /**
   * 发起与WhatsApp的握手
   */
  @ReactMethod
  fun initiateHandshake(promise: Promise) {
    try {
      val success = otpHandler.sendOtpIntentToWhatsApp(reactApplicationContext)
      Log.d(TAG, "握手发起结果: $success")
      
      if (success) {
        promise.resolve(true)
      } else {
        promise.reject("HANDSHAKE_FAILED", "握手失败，请检查WhatsApp是否已安装")
      }
    } catch (e: Exception) {
      Log.e(TAG, "发起握手时出错", e)
      promise.reject("HANDSHAKE_ERROR", "发起握手失败: ${e.message}", e)
    }
  }

  /**
   * 获取应用签名哈希（用于配置WhatsApp模板）
   */
  @ReactMethod
  fun getAppSignatureHash(promise: Promise) {
    try {
      val signatureHash = otpHandler.getAppSignatureHash(reactApplicationContext)
      
      if (signatureHash != null) {
        val result = Arguments.createMap().apply {
          putString("signatureHash", signatureHash)
          putString("packageName", reactApplicationContext.packageName)
        }
        Log.d(TAG, "应用签名信息: 包名=${reactApplicationContext.packageName}, 哈希=$signatureHash")
        promise.resolve(result)
      } else {
        promise.reject("SIGNATURE_ERROR", "无法获取应用签名哈希")
      }
    } catch (e: Exception) {
      Log.e(TAG, "获取应用签名哈希时出错", e)
      promise.reject("SIGNATURE_ERROR", "获取应用签名哈希失败: ${e.message}", e)
    }
  }

  /**
   * 获取设备信息（用于调试）
   */
  @ReactMethod
  fun getDeviceInfo(promise: Promise) {
    try {
      val result = Arguments.createMap().apply {
        putString("packageName", reactApplicationContext.packageName)
        putString("sdkVersion", android.os.Build.VERSION.SDK_INT.toString())
        putString("deviceModel", android.os.Build.MODEL)
        putString("manufacturer", android.os.Build.MANUFACTURER)
      }
      Log.d(TAG, "设备信息: $result")
      promise.resolve(result)
    } catch (e: Exception) {
      Log.e(TAG, "获取设备信息时出错", e)
      promise.reject("DEVICE_INFO_ERROR", "获取设备信息失败: ${e.message}", e)
    }
  }
}
