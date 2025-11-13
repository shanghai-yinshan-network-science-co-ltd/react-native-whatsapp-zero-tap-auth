package com.whatsappzerotapauth

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * 应用签名哈希生成助手类
 * 
 * 用于生成包含在 SMS 消息中的消息哈希。
 * 没有正确的哈希，应用将无法接收消息回调。
 * 
 * 此类只需要为每个应用生成一次并存储，然后可以从代码中移除此助手类。
 * 
 * 基于 Google 的 AppSignatureHelper 实现
 * @see <a href="https://github.com/googlearchive/android-credentials/blob/master/sms-verification/android/app/src/main/java/com/google/samples/smartlock/sms_verify/AppSignatureHelper.java">AppSignatureHelper.java</a>
 */
class AppSignatureHelper(context: Context) : ContextWrapper(context) {

    companion object {
        private const val TAG = "AppSignatureHelper"
        private const val HASH_TYPE = "SHA-256"
        private const val NUM_HASHED_BYTES = 9
        private const val NUM_BASE64_CHAR = 11
    }

    /**
     * 获取当前包的所有应用签名哈希
     * 
     * @return 签名哈希列表
     */
    fun getAppSignatures(): ArrayList<String> {
        val appCodes = ArrayList<String>()

        try {
            // 获取当前包的所有包签名
            val packageName = packageName
            val packageManager = packageManager
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android P (API 28) 及以上版本
                val packageInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                // Android P 以下版本
                @Suppress("DEPRECATION")
                val packageInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            // 为每个签名创建兼容的哈希
            signatures?.forEach { signature ->
                val hash = hash(packageName, signature.toCharsString())
                if (hash != null) {
                    appCodes.add(hash)
                    Log.d(TAG, "应用签名哈希: $hash")
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "无法找到包以获取哈希", e)
        }
        
        return appCodes
    }

    /**
     * 生成签名哈希
     * 
     * @param packageName 包名
     * @param signature 签名字符串
     * @return 11字符的Base64编码哈希
     */
    private fun hash(packageName: String, signature: String): String? {
        val appInfo = "$packageName $signature"
        
        try {
            val messageDigest = MessageDigest.getInstance(HASH_TYPE)
            messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
            var hashSignature = messageDigest.digest()

            // 截断为 NUM_HASHED_BYTES 字节
            hashSignature = hashSignature.copyOfRange(0, NUM_HASHED_BYTES)
            
            // 编码为 Base64
            var base64Hash = Base64.encodeToString(
                hashSignature,
                Base64.NO_PADDING or Base64.NO_WRAP
            )
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR)

            Log.d(TAG, "包名: $packageName -- 哈希: $base64Hash")
            return base64Hash
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "哈希算法不存在", e)
        }
        
        return null
    }
}

