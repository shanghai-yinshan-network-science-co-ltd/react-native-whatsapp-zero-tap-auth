package com.whatsappzerotapauth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * WhatsApp自动填充按钮点击接收器
 * 根据WhatsApp Autofill Button Authentication Templates实现
 * 文档: https://developers.facebook.com/docs/whatsapp/business-management-api/authentication-templates/autofill-button-authentication-templates
 */
class AutofillButtonReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AutofillButtonReceiver"
        const val AUTOFILL_BUTTON_EVENT = "onAutofillButtonClicked"
        
        // WhatsApp包名
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
        
        // 自动填充按钮的Action
        private const val AUTOFILL_BUTTON_ACTION = "com.whatsapp.otp.AUTOFILL_CODE"
        
        private var reactContext: ReactApplicationContext? = null
        
        fun setReactContext(context: ReactApplicationContext) {
            reactContext = context
        }
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "收到自动填充按钮点击: ${intent?.action}")
        
        try {
            if (intent?.action == AUTOFILL_BUTTON_ACTION) {
                handleAutofillButtonClick(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理自动填充按钮点击时出错", e)
        }
    }
    
    private fun handleAutofillButtonClick(intent: Intent) {
        try {
            // 获取验证码
            val code = intent.getStringExtra("code")
            val packageName = intent.getStringExtra("package_name") ?: ""
            
            Log.d(TAG, "自动填充按钮点击 - 验证码: $code, 来自: $packageName")
            
            if (code.isNullOrEmpty()) {
                Log.e(TAG, "验证码为空")
                return
            }
            
            // 验证来源
            if (packageName != WHATSAPP_PACKAGE && packageName != WHATSAPP_BUSINESS_PACKAGE) {
                Log.w(TAG, "未授权的来源: $packageName")
                return
            }
            
            // 发送事件到React Native
            sendAutofillButtonEvent(code, packageName)
            
        } catch (e: Exception) {
            Log.e(TAG, "处理自动填充按钮时出错", e)
        }
    }
    
    private fun sendAutofillButtonEvent(code: String, packageName: String) {
        reactContext?.let { context ->
            try {
                val params = Arguments.createMap().apply {
                    putString("code", code)
                    putString("packageName", packageName)
                    putDouble("timestamp", System.currentTimeMillis().toDouble())
                }
                
                context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit(AUTOFILL_BUTTON_EVENT, params)
                    
                Log.d(TAG, "已发送自动填充按钮事件到React Native")
            } catch (e: Exception) {
                Log.e(TAG, "发送事件到React Native失败", e)
            }
        } ?: run {
            Log.w(TAG, "ReactContext为空，无法发送事件")
        }
    }
}

