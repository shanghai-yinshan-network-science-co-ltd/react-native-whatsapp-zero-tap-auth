package com.whatsappzerotapauth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * WhatsApp自动填充按钮处理器
 * 通过ActivityEventListener的onNewIntent方法处理自动填充
 * 参考: https://developers.facebook.com/docs/whatsapp/business-management-api/authentication-templates/autofill-button-authentication-templates
 */
class WhatsAppAutofillHandler(private val reactContext: ReactApplicationContext) : ActivityEventListener {
    
    companion object {
        private const val TAG = "WhatsAppAutofillHandler"
        const val AUTOFILL_BUTTON_EVENT = "onAutofillButtonClicked"
        
        // WhatsApp包名
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    }

    /**
     * 处理新的Intent（自动填充按钮点击）
     */
    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "onNewIntent called: ${intent?.action}")
        
        intent?.let {
            handleAutofillIntent(it)
        }
    }

    override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        // 不需要处理
    }

    /**
     * 处理自动填充Intent
     */
    private fun handleAutofillIntent(intent: Intent) {
        try {
            // 检查Intent的action和数据
            val action = intent.action
            val data = intent.data
            
            Log.d(TAG, "Intent action: $action")
            Log.d(TAG, "Intent data: $data")
            
            // 获取验证码
            // WhatsApp通过Intent的data或extras传递验证码
            val code = intent.getStringExtra("code") 
                ?: intent.data?.getQueryParameter("code")
                ?: intent.data?.lastPathSegment
            
            if (code != null) {
                Log.d(TAG, "收到自动填充验证码: $code")
                
                // 验证来源（如果Intent包含包名信息）
                val packageName = intent.getStringExtra("package_name") 
                    ?: intent.`package` 
                    ?: ""
                
                Log.d(TAG, "来源包名: $packageName")
                
                // 发送事件到React Native
                sendAutofillEvent(code, packageName)
            } else {
                Log.w(TAG, "Intent中未找到验证码")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "处理自动填充Intent时出错", e)
        }
    }

    /**
     * 发送自动填充事件到React Native
     */
    private fun sendAutofillEvent(code: String, packageName: String) {
        try {
            val params = Arguments.createMap().apply {
                putString("code", code)
                putString("packageName", packageName)
                putDouble("timestamp", System.currentTimeMillis().toDouble())
            }
            
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit(AUTOFILL_BUTTON_EVENT, params)
                
            Log.d(TAG, "已发送自动填充事件到React Native")
        } catch (e: Exception) {
            Log.e(TAG, "发送事件到React Native失败", e)
        }
    }
}

