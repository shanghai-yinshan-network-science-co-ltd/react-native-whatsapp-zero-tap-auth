# React Native WhatsApp Zero Tap Authentication

[![npm version](https://badge.fury.io/js/react-native-whatsapp-zero-tap-auth.svg)](https://badge.fury.io/js/react-native-whatsapp-zero-tap-auth)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

一个React Native模块，实现WhatsApp零点击认证功能，基于WhatsApp官方文档，无需依赖WhatsApp SDK。

A React Native module that implements WhatsApp Zero Tap Authentication, based on official WhatsApp documentation, without requiring WhatsApp SDK dependencies.

## 功能特性 / Features

- ✅ 零点击验证码自动接收 / Zero-tap OTP auto-retrieval
- ✅ WhatsApp安装状态检查 / WhatsApp installation status check  
- ✅ 获取应用签名哈希用于配置模板 / Get app signature hash for template configuration
- ✅ 与WhatsApp的握手机制 / Handshake mechanism with WhatsApp
- ✅ 完整的事件监听系统 / Complete event listening system
- ✅ 支持WhatsApp和WhatsApp Business / Support for WhatsApp and WhatsApp Business
- ✅ 使用最新的React Native TurboModules架构 / Built with React Native TurboModules architecture

## 系统要求 / Requirements

- **Android Only**: 零点击认证仅在Android平台支持 / Zero tap authentication is only supported on Android
- **Android API 19+**: 支持Android 4.4及以上版本 / Supports Android 4.4 and above
- **WhatsApp**: 需要安装WhatsApp或WhatsApp Business应用 / Requires WhatsApp or WhatsApp Business app

## 安装 / Installation

### 通过 NPM / Via NPM

```sh
npm install react-native-whatsapp-zero-tap-auth
```

或 / or

```sh
yarn add react-native-whatsapp-zero-tap-auth
```

### 通过 GitHub 直接安装 / Direct Installation from GitHub

如果您想使用最新的开发版本或 NPM 包尚未发布，可以直接从 GitHub 安装：

If you want to use the latest development version or the NPM package is not yet published, you can install directly from GitHub:

```sh
# 使用最新的 master 分支 / Use latest master branch
npm install git+https://github.com/shanghai-yinshan-network-science-co-ltd/react-native-whatsapp-zero-tap-auth.git

# 或使用 yarn / Or with yarn
yarn add git+https://github.com/shanghai-yinshan-network-science-co-ltd/react-native-whatsapp-zero-tap-auth.git

# 或使用 pnpm / Or with pnpm
pnpm add git+https://github.com/shanghai-yinshan-network-science-co-ltd/react-native-whatsapp-zero-tap-auth.git
```

您也可以在 `package.json` 中直接配置：

You can also configure it directly in `package.json`:

```json
{
  "dependencies": {
    "react-native-whatsapp-zero-tap-auth": "git+https://github.com/shanghai-yinshan-network-science-co-ltd/react-native-whatsapp-zero-tap-auth.git"
  }
}
```

### 自动配置 / Auto Configuration

模块已预配置所有必需的Android权限和组件，无需手动配置。
The module comes pre-configured with all necessary Android permissions and components.

### 获取应用签名哈希 / Get App Signature Hash

```typescript
import { getAppSignatureHash } from 'react-native-whatsapp-zero-tap-auth';

const signatureInfo = await getAppSignatureHash();
console.log('Package Name:', signatureInfo.packageName);
console.log('Signature Hash:', signatureInfo.signatureHash);
```

## 快速开始 / Quick Start

```typescript
import {
  WhatsAppZeroTapAuth,
  isWhatsAppInstalled,
  type OtpReceivedEvent,
  type OtpErrorEvent
} from 'react-native-whatsapp-zero-tap-auth';

const otpAuth = new WhatsAppZeroTapAuth();

// 检查WhatsApp是否安装 / Check if WhatsApp is installed
const installed = await isWhatsAppInstalled();
if (!installed) {
  console.log('WhatsApp未安装 / WhatsApp not installed');
  return;
}

// 请求验证码的完整流程 / Complete OTP request flow
const result = await otpAuth.requestOtp(
  (event: OtpReceivedEvent) => {
    console.log('收到验证码 / OTP received:', event.code);
    console.log('来源 / Source:', event.source);
  },
  (event: OtpErrorEvent) => {
    console.log('接收失败 / Receive failed:', event.errorMessage);
  }
);

if (result.success) {
  console.log('握手成功，等待验证码 / Handshake successful, waiting for OTP');
  // 现在可以通过API发送验证码模板消息
  // Now you can send OTP template message via API
} else {
  console.log('失败 / Failed:', result.message);
}
```

## API参考 / API Reference

### 主要方法 / Main Methods

#### `isWhatsAppInstalled(): Promise<boolean>`
检查WhatsApp是否已安装 / Check if WhatsApp is installed

#### `getInstalledWhatsAppApps(): Promise<string[]>`
获取已安装的WhatsApp应用列表 / Get list of installed WhatsApp apps

#### `initiateHandshake(): Promise<boolean>`
发起与WhatsApp的握手 / Initiate handshake with WhatsApp

#### `getAppSignatureHash(): Promise<AppSignatureInfo>`
获取应用签名哈希 / Get app signature hash

#### `getDeviceInfo(): Promise<DeviceInfo>`
获取设备信息 / Get device information

### 事件监听 / Event Listening

#### `addOtpReceivedListener(listener: OtpReceivedListener): () => void`
添加验证码接收事件监听器 / Add OTP received event listener

#### `addOtpErrorListener(listener: OtpErrorListener): () => void`
添加验证码错误事件监听器 / Add OTP error event listener

#### `removeAllListeners(): void`
移除所有事件监听器 / Remove all event listeners

### WhatsAppZeroTapAuth类 / WhatsAppZeroTapAuth Class

#### `requestOtp(onOtpReceived, onOtpError?): Promise<{success: boolean, message: string}>`
请求验证码的完整流程 / Complete OTP request flow

#### `startListening(onOtpReceived, onOtpError?): void`
开始监听验证码接收 / Start listening for OTP

#### `stopListening(): void`
停止监听验证码接收 / Stop listening for OTP

## 类型定义 / Type Definitions

```typescript
interface OtpReceivedEvent {
  code: string;           // 验证码 / OTP code
  source: string;         // 来源应用包名 / Source app package name
  timestamp: number;      // 时间戳 / Timestamp
}

interface OtpErrorEvent {
  errorCode: string;      // 错误代码 / Error code
  errorMessage: string;   // 错误消息 / Error message
  timestamp: number;      // 时间戳 / Timestamp
}

interface AppSignatureInfo {
  signatureHash: string;  // 应用签名哈希 / App signature hash
  packageName: string;    // 应用包名 / App package name
}

interface DeviceInfo {
  packageName: string;    // 应用包名 / App package name
  sdkVersion: string;     // Android SDK版本 / Android SDK version
  deviceModel: string;    // 设备型号 / Device model
  manufacturer: string;   // 制造商 / Manufacturer
}
```

## 工作流程 / Workflow

1. **检查安装** / **Check Installation**: 确认WhatsApp已安装 / Verify WhatsApp is installed
2. **发起握手** / **Initiate Handshake**: 向WhatsApp发送握手请求 / Send handshake request to WhatsApp
3. **开始监听** / **Start Listening**: 注册验证码接收监听器 / Register OTP receipt listener
4. **发送模板** / **Send Template**: 通过API发送验证码模板消息 / Send OTP template message via API
5. **自动接收** / **Auto Receive**: 自动接收并处理验证码 / Automatically receive and process OTP

## 注意事项 / Important Notes

1. **平台限制** / **Platform Limitation**: 零点击认证仅在Android平台支持 / Zero tap auth only supports Android
2. **握手时效** / **Handshake Timeout**: 握手后必须在10分钟内发送验证码 / Must send OTP within 10 minutes after handshake
3. **签名匹配** / **Signature Match**: 模板中的签名哈希必须与应用实际签名匹配 / Template signature hash must match app signature
4. **包名匹配** / **Package Match**: 模板中的包名必须与应用包名匹配 / Template package name must match app package

## 完整文档 / Complete Documentation

详细的使用指南和配置说明，请参见 [WHATSAPP_ZERO_TAP_GUIDE.md](./WHATSAPP_ZERO_TAP_GUIDE.md)

For detailed usage guide and configuration instructions, see [WHATSAPP_ZERO_TAP_GUIDE.md](./WHATSAPP_ZERO_TAP_GUIDE.md)

## 示例 / Example

完整的示例应用请查看 [example](./example) 目录

Check out the complete example app in the [example](./example) directory

## 贡献 / Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## 许可证 / License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
