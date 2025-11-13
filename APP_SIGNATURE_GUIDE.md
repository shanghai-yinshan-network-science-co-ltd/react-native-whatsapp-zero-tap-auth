# 应用签名哈希获取指南

## 概述

此模块使用 Google 推荐的 `AppSignatureHelper` 来生成应用签名哈希。此哈希值用于配置 WhatsApp 短信验证码模板，确保应用能够正确接收和识别来自 WhatsApp 的 OTP 消息。

## 实现说明

### AppSignatureHelper 类

基于 Google 的官方实现：
https://github.com/googlearchive/android-credentials/blob/master/sms-verification/android/app/src/main/java/com/google/samples/smartlock/sms_verify/AppSignatureHelper.java

该类已转换为 Kotlin 版本，位于：
- `android/src/main/java/com/whatsappzerotapauth/AppSignatureHelper.kt`

### 签名哈希算法

使用以下规范生成签名哈希：
1. **算法**: SHA-256
2. **输入**: 包名 + 空格 + 签名字符串
3. **处理**: 
   - 取哈希结果的前 9 个字节
   - 使用 Base64 编码（NO_PADDING | NO_WRAP）
   - 截取前 11 个字符
4. **输出**: 11 字符的 Base64 字符串

## 使用方法

### 在 React Native 中获取签名哈希

```javascript
import WhatsappZeroTapAuth from 'react-native-whatsapp-zero-tap-auth';

// 获取应用签名哈希
const getSignature = async () => {
  try {
    const result = await WhatsappZeroTapAuth.getAppSignatureHash();
    console.log('包名:', result.packageName);
    console.log('签名哈希:', result.signatureHash);
    
    // 如果应用有多个签名（例如，调试和发布签名）
    if (result.allSignatures) {
      console.log('所有签名:', result.allSignatures);
    }
    
    return result.signatureHash;
  } catch (error) {
    console.error('获取签名哈希失败:', error);
  }
};
```

### 在 Android 原生代码中使用

```kotlin
import com.whatsappzerotapauth.AppSignatureHelper

// 在 Activity 或其他 Context 中
val helper = AppSignatureHelper(context)
val signatures = helper.getAppSignatures()

signatures.forEach { signature ->
    Log.d("AppSignature", "签名哈希: $signature")
}
```

## 配置 WhatsApp 消息模板

获取到签名哈希后，您需要在 WhatsApp Business API 的消息模板中包含此哈希：

### 消息格式示例

```
<#> 您的验证码是: 123456
此验证码将在 5 分钟内过期。
YOUR_HASH_HERE
```

其中 `YOUR_HASH_HERE` 应该替换为通过 `getAppSignatureHash()` 获取的 11 字符哈希值。

### 重要说明

1. **调试签名 vs 发布签名**
   - 开发时使用调试签名生成的哈希
   - 发布到 Google Play 时使用发布签名生成的哈希
   - 如果使用 Google Play App Signing，需要使用 Google 管理的签名证书的哈希

2. **获取发布签名哈希**
   
   方法 1 - 使用 keytool 和手动计算：
   ```bash
   # 获取签名证书
   keytool -list -v -keystore your-release-key.keystore
   
   # 或从 Google Play Console 获取 SHA-256 证书指纹
   ```
   
   方法 2 - 在已签名的 APK/AAB 中运行：
   - 构建发布版本的 APK
   - 安装到测试设备
   - 运行 `getAppSignatureHash()` 方法

3. **哈希格式验证**
   - 确保哈希正好是 11 个字符
   - 只包含 Base64 字符（A-Z, a-z, 0-9, +, /）
   - 不包含填充字符 (=)

## 常见问题

### Q: 为什么需要签名哈希？
A: WhatsApp 使用签名哈希来验证 SMS 消息是否应该被特定应用接收，这是一种安全机制，防止其他应用拦截您的验证码。

### Q: 调试版本和发布版本的哈希不同怎么办？
A: 您需要为不同的签名配置不同的消息模板，或者在 WhatsApp Business API 中配置多个哈希值（如果支持）。

### Q: 如何在 Google Play Console 中找到应用签名？
A: 
1. 进入 Google Play Console
2. 选择您的应用
3. 转到 "设置" > "应用完整性"
4. 查看 "应用签名" 部分的 SHA-256 证书指纹

### Q: 哈希值与 Google 官方示例不匹配？
A: 确保您：
- 使用的是正确的包名
- 使用的是正确的签名证书
- 算法使用 SHA-256（而非 SHA-1 或 MD5）
- 正确处理了字节截断和 Base64 编码

## 技术参考

- [Google SMS Verification API](https://developers.google.com/identity/sms-retriever/overview)
- [WhatsApp Business API - Message Templates](https://developers.facebook.com/docs/whatsapp/api/messages/message-templates)
- [Android App Signing](https://developer.android.com/studio/publish/app-signing)

## 调试技巧

在开发过程中，您可以启用详细日志来查看签名哈希生成过程：

```kotlin
// 在 Android Logcat 中过滤
adb logcat -s AppSignatureHelper
```

这将显示：
```
D/AppSignatureHelper: 包名: com.example.app -- 哈希: xYz1234AbCd
```

