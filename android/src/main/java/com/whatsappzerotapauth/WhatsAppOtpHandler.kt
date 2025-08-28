package com.whatsappzerotapauth

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.app.PendingIntent
import android.os.Build
import android.os.Bundle
import android.util.Log

/**
 * WhatsApp零点击认证处理器
 * 根据WhatsApp文档实现，无需依赖WhatsApp SDK
 */
class WhatsAppOtpHandler {
    
    companion object {
        private const val TAG = "WhatsAppOtpHandler"
        
        // WhatsApp包名
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
        
        // Intent动作
        private const val OTP_REQUESTED_ACTION = "com.whatsapp.otp.OTP_REQUESTED"
    }
    
    /**
     * 检查WhatsApp是否已安装
     */
    fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val whatsappInstalled = isPackageInstalled(packageManager, WHATSAPP_PACKAGE)
            val whatsappBusinessInstalled = isPackageInstalled(packageManager, WHATSAPP_BUSINESS_PACKAGE)
            
            val result = whatsappInstalled || whatsappBusinessInstalled
            Log.d(TAG, "WhatsApp安装状态 - 普通版: $whatsappInstalled, 商务版: $whatsappBusinessInstalled")
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "检查WhatsApp安装状态时出错", e)
            false
        }
    }
    
    /**
     * 获取已安装的WhatsApp应用列表
     */
    fun getInstalledWhatsAppApps(context: Context): List<String> {
        val installedApps = mutableListOf<String>()
        val packageManager = context.packageManager
        
        try {
            if (isPackageInstalled(packageManager, WHATSAPP_PACKAGE)) {
                installedApps.add(WHATSAPP_PACKAGE)
            }
            if (isPackageInstalled(packageManager, WHATSAPP_BUSINESS_PACKAGE)) {
                installedApps.add(WHATSAPP_BUSINESS_PACKAGE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取WhatsApp应用列表时出错", e)
        }
        
        Log.d(TAG, "已安装的WhatsApp应用: $installedApps")
        return installedApps
    }
    
    /**
     * 向WhatsApp发送OTP请求握手
     */
    fun sendOtpIntentToWhatsApp(context: Context): Boolean {
        return try {
            val installedApps = getInstalledWhatsAppApps(context)
            
            if (installedApps.isEmpty()) {
                Log.w(TAG, "未发现已安装的WhatsApp应用")
                return false
            }
            
            var success = false
            installedApps.forEach { packageName ->
                if (sendOtpIntentToWhatsAppPackage(context, packageName)) {
                    success = true
                }
            }
            
            Log.d(TAG, "握手发送结果: $success")
            success
        } catch (e: Exception) {
            Log.e(TAG, "发送OTP Intent时出错", e)
            false
        }
    }
    
    private fun sendOtpIntentToWhatsAppPackage(context: Context, packageName: String): Boolean {
        return try {
            // 创建PendingIntent
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(),
                flags
            )
            
            // 创建发送给WhatsApp的Intent
            val intentToWhatsApp = Intent().apply {
                setPackage(packageName)
                action = OTP_REQUESTED_ACTION
            }
            
            // 添加PendingIntent到extras
            val extras = intentToWhatsApp.extras ?: Bundle()
            extras.putParcelable("_ci_", pendingIntent)
            intentToWhatsApp.putExtras(extras)
            
            // 发送广播
            context.sendBroadcast(intentToWhatsApp)
            
            Log.d(TAG, "已向 $packageName 发送OTP请求")
            true
        } catch (e: Exception) {
            Log.e(TAG, "向 $packageName 发送OTP Intent失败", e)
            false
        }
    }
    
    private fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * 获取应用的签名哈希（用于配置WhatsApp模板）
     */
    fun getAppSignatureHash(context: Context): String? {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            signatures?.firstOrNull()?.let { signature ->
                val digest = java.security.MessageDigest.getInstance("SHA")
                digest.update(signature.toByteArray())
                val hash = android.util.Base64.encodeToString(digest.digest(), android.util.Base64.NO_WRAP)
                Log.d(TAG, "应用签名哈希: $hash")
                hash
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取应用签名哈希时出错", e)
            null
        }
    }
}
