package com.whatsappzerotapauth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * 零点击验证码广播接收器
 * 根据WhatsApp文档实现，无需依赖WhatsApp SDK
 */
class OtpCodeReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "OtpCodeReceiver"
        const val OTP_RECEIVED_EVENT = "onOtpReceived"
        const val OTP_ERROR_EVENT = "onOtpError"
        
        // WhatsApp包名
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
        
        private var reactContext: ReactApplicationContext? = null
        
        fun setReactContext(context: ReactApplicationContext) {
            reactContext = context
        }
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "收到广播: ${intent?.action}")
        
        try {
            if (intent?.action == "com.whatsapp.otp.OTP_RETRIEVED") {
                handleOtpReceived(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理OTP接收时出错", e)
            sendErrorEvent("RECEIVE_ERROR", "处理验证码时出错: ${e.message}")
        }
    }
    
    private fun handleOtpReceived(intent: Intent) {
        try {
            // 获取PendingIntent来验证来源
            val pendingIntent = intent.getParcelableExtra<PendingIntent>("_ci_")
            
            if (pendingIntent == null) {
                Log.e(TAG, "PendingIntent为空")
                sendErrorEvent("INVALID_INTENT", "无效的Intent")
                return
            }
            
            // 验证发送方包名
            val creatorPackage = pendingIntent.creatorPackage
            Log.d(TAG, "发送方包名: $creatorPackage")
            
            if (creatorPackage != WHATSAPP_PACKAGE && creatorPackage != WHATSAPP_BUSINESS_PACKAGE) {
                Log.e(TAG, "未授权的发送方: $creatorPackage")
                sendErrorEvent("UNAUTHORIZED_SENDER", "未授权的发送方")
                return
            }
            
            // 获取验证码
            val otpCode = intent.getStringExtra("code")
            Log.d(TAG, "接收到验证码: $otpCode")
            
            if (otpCode.isNullOrEmpty()) {
                Log.e(TAG, "验证码为空")
                sendErrorEvent("EMPTY_CODE", "验证码为空")
                return
            }
            
            // 发送成功事件到React Native
            sendOtpReceivedEvent(otpCode, creatorPackage)
            
        } catch (e: Exception) {
            Log.e(TAG, "处理OTP时出错", e)
            sendErrorEvent("PROCESSING_ERROR", "处理验证码时出错: ${e.message}")
        }
    }
    
    private fun sendOtpReceivedEvent(code: String, source: String) {
        reactContext?.let { context ->
            try {
                val params = Arguments.createMap().apply {
                    putString("code", code)
                    putString("source", source)
                    putDouble("timestamp", System.currentTimeMillis().toDouble())
                }
                
                context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit(OTP_RECEIVED_EVENT, params)
                    
                Log.d(TAG, "已发送OTP接收事件到React Native")
            } catch (e: Exception) {
                Log.e(TAG, "发送事件到React Native失败", e)
            }
        } ?: run {
            Log.w(TAG, "ReactContext为空，无法发送事件")
        }
    }
    
    private fun sendErrorEvent(errorCode: String, errorMessage: String) {
        reactContext?.let { context ->
            try {
                val params = Arguments.createMap().apply {
                    putString("errorCode", errorCode)
                    putString("errorMessage", errorMessage)
                    putDouble("timestamp", System.currentTimeMillis().toDouble())
                }
                
                context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit(OTP_ERROR_EVENT, params)
                    
                Log.d(TAG, "已发送错误事件到React Native")
            } catch (e: Exception) {
                Log.e(TAG, "发送错误事件到React Native失败", e)
            }
        } ?: run {
            Log.w(TAG, "ReactContext为空，无法发送错误事件")
        }
    }
}
