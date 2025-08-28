# WhatsApp零点击认证使用指南

本模块实现了WhatsApp零点击认证功能，基于WhatsApp官方文档，无需依赖WhatsApp SDK。

## 功能特性

- ✅ 零点击验证码自动接收
- ✅ WhatsApp安装状态检查
- ✅ 获取应用签名哈希用于配置模板
- ✅ 与WhatsApp的握手机制
- ✅ 完整的事件监听系统
- ✅ 支持WhatsApp和WhatsApp Business
- ✅ 使用最新的React Native TurboModules架构

## 系统要求

- **Android Only**: 零点击认证仅在Android平台支持
- **Android API 19+**: 支持Android 4.4及以上版本
- **WhatsApp**: 需要安装WhatsApp或WhatsApp Business应用

## 安装配置

### 自动配置

本模块已经在Android模块的AndroidManifest.xml中预配置了所有必需的权限和组件，无需手动添加配置。

模块会自动包含：
- WhatsApp应用查询权限
- 零点击验证码广播接收器

### 手动配置（如果需要）

如果自动配置有问题，可以手动添加到您的应用AndroidManifest.xml：

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 查询WhatsApp应用权限 -->
    <queries>
        <package android:name="com.whatsapp"/>
        <package android:name="com.whatsapp.w4b"/>
    </queries>

    <application>
        <!-- 添加零点击验证码接收器 -->
        <receiver
            android:name="com.whatsappzerotapauth.OtpCodeReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="com.whatsapp.otp.OTP_RETRIEVED" />
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

### 获取应用签名哈希

运行应用后，使用以下代码获取签名信息：

```typescript
import { getAppSignatureHash } from 'react-native-whatsapp-zero-tap-auth';

const signatureInfo = await getAppSignatureHash();
console.log('Package Name:', signatureInfo.packageName);
console.log('Signature Hash:', signatureInfo.signatureHash);
```

## WhatsApp模板配置

### 1. 创建零点击认证模板

使用WhatsApp Business Management API创建模板：

```json
{
  "name": "your_template_name",
  "language": "zh_CN",
  "category": "authentication",
  "message_send_ttl_seconds": 600,
  "components": [
    {
      "type": "body",
      "add_security_recommendation": true
    },
    {
      "type": "footer",
      "code_expiration_minutes": 10
    },
    {
      "type": "buttons",
      "buttons": [
        {
          "type": "otp",
          "otp_type": "zero_tap",
          "text": "Copy Code",
          "autofill_text": "Autofill",
          "zero_tap_terms_accepted": true,
          "supported_apps": [
            {
              "package_name": "YOUR_PACKAGE_NAME",
              "signature_hash": "YOUR_SIGNATURE_HASH"
            }
          ]
        }
      ]
    }
  ]
}
```

## 使用方法

### 基本使用

```typescript
import {
  WhatsAppZeroTapAuth,
  isWhatsAppInstalled,
  type OtpReceivedEvent,
  type OtpErrorEvent
} from 'react-native-whatsapp-zero-tap-auth';

const otpAuth = new WhatsAppZeroTapAuth();

// 检查WhatsApp是否安装
const installed = await isWhatsAppInstalled();
if (!installed) {
  console.log('WhatsApp未安装');
  return;
}

// 请求验证码的完整流程
const result = await otpAuth.requestOtp(
  (event: OtpReceivedEvent) => {
    console.log('收到验证码:', event.code);
    console.log('来源:', event.source);
  },
  (event: OtpErrorEvent) => {
    console.log('接收失败:', event.errorMessage);
  }
);

if (result.success) {
  console.log('握手成功，等待验证码');
  // 现在可以通过API发送验证码模板消息
} else {
  console.log('失败:', result.message);
}
```

### 手动控制流程

```typescript
import {
  initiateHandshake,
  addOtpReceivedListener,
  addOtpErrorListener,
  removeAllListeners
} from 'react-native-whatsapp-zero-tap-auth';

// 1. 添加监听器
const unsubscribeOtp = addOtpReceivedListener((event) => {
  console.log('验证码:', event.code);
});

const unsubscribeError = addOtpErrorListener((event) => {
  console.log('错误:', event.errorMessage);
});

// 2. 发起握手
const success = await initiateHandshake();
if (success) {
  // 3. 通过API发送验证码模板消息
  // ...
}

// 4. 清理监听器
unsubscribeOtp();
unsubscribeError();
// 或者
removeAllListeners();
```

## API参考

### 主要方法

#### `isWhatsAppInstalled(): Promise<boolean>`
检查WhatsApp是否已安装

#### `getInstalledWhatsAppApps(): Promise<string[]>`
获取已安装的WhatsApp应用列表

#### `initiateHandshake(): Promise<boolean>`
发起与WhatsApp的握手，必须在发送验证码模板消息之前调用

#### `getAppSignatureHash(): Promise<AppSignatureInfo>`
获取应用签名哈希，用于配置WhatsApp模板

#### `getDeviceInfo(): Promise<DeviceInfo>`
获取设备信息，用于调试

### 事件监听

#### `addOtpReceivedListener(listener: OtpReceivedListener): () => void`
添加验证码接收事件监听器，返回取消订阅函数

#### `addOtpErrorListener(listener: OtpErrorListener): () => void`
添加验证码错误事件监听器，返回取消订阅函数

#### `removeAllListeners(): void`
移除所有事件监听器

### WhatsAppZeroTapAuth类

#### `requestOtp(onOtpReceived, onOtpError?): Promise<{success: boolean, message: string}>`
请求验证码的完整流程：检查安装→发起握手→开始监听

#### `startListening(onOtpReceived, onOtpError?): void`
开始监听验证码接收

#### `stopListening(): void`
停止监听验证码接收

## 类型定义

```typescript
interface OtpReceivedEvent {
  code: string;           // 验证码
  source: string;         // 来源应用包名
  timestamp: number;      // 时间戳
}

interface OtpErrorEvent {
  errorCode: string;      // 错误代码
  errorMessage: string;   // 错误消息
  timestamp: number;      // 时间戳
}

interface AppSignatureInfo {
  signatureHash: string;  // 应用签名哈希
  packageName: string;    // 应用包名
}

interface DeviceInfo {
  packageName: string;    // 应用包名
  sdkVersion: string;     // Android SDK版本
  deviceModel: string;    // 设备型号
  manufacturer: string;   // 制造商
}
```

## 工作流程

1. **检查安装**: 确认WhatsApp已安装
2. **发起握手**: 调用`initiateHandshake()`向WhatsApp发送OTP_REQUESTED intent
3. **开始监听**: 注册验证码接收监听器
4. **发送模板**: 通过WhatsApp Business API发送验证码模板消息
5. **自动接收**: WhatsApp客户端验证通过后广播验证码
6. **处理验证码**: 应用自动接收并处理验证码

## 注意事项

1. **平台限制**: 零点击认证仅在Android平台支持
2. **握手时效**: 握手后必须在10分钟内发送验证码模板消息
3. **签名匹配**: 模板中的签名哈希必须与应用实际签名匹配
4. **包名匹配**: 模板中的包名必须与应用包名匹配
5. **权限配置**: 必须在AndroidManifest.xml中正确配置查询权限和广播接收器

## 调试技巧

1. 使用`getAppSignatureHash()`获取正确的签名信息
2. 使用`getDeviceInfo()`检查设备兼容性
3. 查看Android日志中的`OtpCodeReceiver`和`WhatsAppOtpHandler`标签
4. 确保WhatsApp应用已登录并具有必要权限

## 错误处理

常见错误及解决方法：

- **HANDSHAKE_FAILED**: WhatsApp未安装或版本不兼容
- **UNAUTHORIZED_SENDER**: 验证码来源不是官方WhatsApp应用
- **EMPTY_CODE**: 接收到的验证码为空
- **INVALID_INTENT**: Intent格式不正确
- **PROCESSING_ERROR**: 处理验证码时发生异常

## 完整示例

参考`example/src/App.tsx`文件查看完整的使用示例，包括UI交互和错误处理。
