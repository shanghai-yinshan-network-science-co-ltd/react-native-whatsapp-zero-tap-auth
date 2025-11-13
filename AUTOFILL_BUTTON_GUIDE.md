# WhatsApp 一键自动填充按钮使用指南

本指南介绍如何使用 WhatsApp 的自动填充按钮功能，让用户点击 WhatsApp 消息中的按钮后自动将验证码填充到您的应用中。

## 参考文档

本功能基于 WhatsApp 官方文档实现：
- [Autofill Button Authentication Templates](https://developers.facebook.com/docs/whatsapp/business-management-api/authentication-templates/autofill-button-authentication-templates)

## 功能特性

- ✅ 一键自动填充验证码到您的应用
- ✅ 提升用户体验，无需手动输入或复制
- ✅ 与零点击认证同时支持
- ✅ 支持 WhatsApp 和 WhatsApp Business
- ✅ 自动验证来源安全性

## 工作原理

1. **用户请求验证码**：应用发起握手并监听验证码
2. **WhatsApp 发送模板消息**：包含验证码和自动填充按钮
3. **用户点击按钮**：WhatsApp 通过 Intent 启动您的应用并传递验证码
4. **Activity 接收 Intent**：通过 `onNewIntent` 方法接收验证码
5. **应用处理验证码**：自动填充到输入框

**技术实现：**
- 使用 `ActivityEventListener` 监听 Activity 的 `onNewIntent` 事件
- 从 Intent 中提取验证码参数
- 通过 React Native 事件系统发送到 JavaScript 层

## 前置要求

### MainActivity 配置

**重要：** 自动填充按钮需要在 `AndroidManifest.xml` 中配置 MainActivity 的 `launchMode`。

在您的应用的 `android/app/src/main/AndroidManifest.xml` 中：

```xml
<activity
  android:name=".MainActivity"
  android:launchMode="singleTask"
  <!-- 其他配置... -->
</activity>
```

📖 **详细配置说明：** 请查看 [AUTOFILL_ACTIVITY_CONFIG.md](./AUTOFILL_ACTIVITY_CONFIG.md)

## 使用方法

### 1. 创建支持自动填充按钮的模板

使用 WhatsApp Business API 创建认证模板时，配置自动填充按钮：

```json
{
  "name": "your_auth_template",
  "language": "zh_CN",
  "category": "authentication",
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

**关键参数说明：**
- `otp_type: "zero_tap"` - 启用零点击认证
- `text: "Copy Code"` - 复制按钮的文本
- `autofill_text: "Autofill"` - 自动填充按钮的文本
- `supported_apps` - 支持的应用列表（包名和签名哈希）

### 2. 在应用中监听自动填充事件

```typescript
import {
  WhatsAppZeroTapAuth,
  isWhatsAppInstalled,
  type OtpReceivedEvent,
  type OtpErrorEvent,
  type AutofillButtonEvent,
} from 'react-native-whatsapp-zero-tap-auth';

const otpAuth = new WhatsAppZeroTapAuth();

// 请求验证码（支持零点击 + 自动填充按钮）
const result = await otpAuth.requestOtp(
  // 零点击接收到验证码（后台自动接收）
  (event: OtpReceivedEvent) => {
    console.log('零点击收到验证码:', event.code);
    console.log('来源:', event.source);
    // 自动填充到输入框
    setOtpCode(event.code);
  },
  
  // 错误处理
  (event: OtpErrorEvent) => {
    console.error('错误:', event.errorMessage);
    Alert.alert('错误', event.errorMessage);
  },
  
  // 用户点击自动填充按钮（主动点击）
  (event: AutofillButtonEvent) => {
    console.log('用户点击了自动填充按钮');
    console.log('验证码:', event.code);
    console.log('来自:', event.packageName);
    // 自动填充到输入框
    setOtpCode(event.code);
  }
);

if (result.success) {
  console.log('已准备好接收验证码');
} else {
  console.error('准备失败:', result.message);
}
```

### 3. 完整的 React 组件示例

```typescript
import React, { useState, useCallback } from 'react';
import { View, TextInput, Button, Alert } from 'react-native';
import {
  WhatsAppZeroTapAuth,
  type OtpReceivedEvent,
  type AutofillButtonEvent,
} from 'react-native-whatsapp-zero-tap-auth';

function OTPScreen() {
  const [otpCode, setOtpCode] = useState('');
  const [otpAuth] = useState(() => new WhatsAppZeroTapAuth());

  const handleOtpReceived = useCallback((event: OtpReceivedEvent) => {
    console.log('零点击收到验证码:', event.code);
    setOtpCode(event.code);
    Alert.alert('成功', '已自动接收验证码');
  }, []);

  const handleAutofillButton = useCallback((event: AutofillButtonEvent) => {
    console.log('用户点击自动填充按钮:', event.code);
    setOtpCode(event.code);
    Alert.alert('成功', '验证码已自动填充');
  }, []);

  const requestVerificationCode = useCallback(async () => {
    const result = await otpAuth.requestOtp(
      handleOtpReceived,
      (error) => Alert.alert('错误', error.errorMessage),
      handleAutofillButton  // 支持自动填充按钮
    );

    if (result.success) {
      Alert.alert(
        '请求成功',
        '请在 WhatsApp 中查看验证码消息。\n\n' +
        '• 零点击：验证码将自动接收\n' +
        '• 自动填充：点击消息中的按钮自动填充'
      );
    } else {
      Alert.alert('错误', result.message);
    }
  }, [otpAuth, handleOtpReceived, handleAutofillButton]);

  return (
    <View>
      <TextInput
        value={otpCode}
        onChangeText={setOtpCode}
        placeholder="验证码"
        keyboardType="number-pad"
        maxLength={6}
      />
      <Button title="请求验证码" onPress={requestVerificationCode} />
    </View>
  );
}
```

### 4. 只监听自动填充按钮（不使用零点击）

如果您只想使用自动填充按钮而不使用零点击认证：

```typescript
import { addAutofillButtonListener } from 'react-native-whatsapp-zero-tap-auth';

// 只监听自动填充按钮
const unsubscribe = addAutofillButtonListener((event) => {
  console.log('收到验证码:', event.code);
  setOtpCode(event.code);
});

// 清理监听器
unsubscribe();
```

## 事件类型说明

### AutofillButtonEvent

```typescript
interface AutofillButtonEvent {
  code: string;          // 验证码
  packageName: string;   // 来源包名 (com.whatsapp 或 com.whatsapp.w4b)
  timestamp: number;     // 接收时间戳
}
```

## 零点击 vs 自动填充按钮

| 特性 | 零点击认证 | 自动填充按钮 |
|------|-----------|-------------|
| **用户操作** | 无需操作，自动接收 | 需要点击按钮 |
| **接收方式** | 后台自动 | 主动触发 |
| **用户体验** | 完全自动化 | 用户控制 |
| **适用场景** | 快速验证 | 需要用户确认 |
| **兼容性** | 需要握手成功 | 独立工作 |

**建议：** 同时支持两种方式，让用户有更多选择：
- 零点击适合快速登录
- 自动填充按钮适合需要用户确认的场景

## 最佳实践

### 1. 提供清晰的用户提示

```typescript
Alert.alert(
  '验证码已发送',
  '请查看 WhatsApp 消息\n\n' +
  '方式一：验证码将自动接收（零点击）\n' +
  '方式二：点击消息中的"Autofill"按钮'
);
```

### 2. 处理超时情况

```typescript
// 设置超时
const timeout = setTimeout(() => {
  Alert.alert('提示', '如果未收到验证码，请点击 WhatsApp 消息中的按钮');
}, 30000); // 30秒后提示

// 收到验证码后清除超时
clearTimeout(timeout);
```

### 3. 错误处理

```typescript
const result = await otpAuth.requestOtp(
  handleOtpReceived,
  (error) => {
    console.error('错误代码:', error.errorCode);
    console.error('错误信息:', error.errorMessage);
    
    // 根据错误类型给出不同的提示
    if (error.errorCode === 'HANDSHAKE_FAILED') {
      Alert.alert('提示', '请点击 WhatsApp 消息中的自动填充按钮');
    }
  },
  handleAutofillButton
);
```

## 安全性说明

1. **包名验证**：自动验证来源包名，只接受来自 WhatsApp 的消息
2. **签名验证**：模板配置中包含应用签名哈希，确保消息发送到正确的应用
3. **时间戳**：每个事件包含时间戳，可用于验证时效性

## 常见问题

### Q: 自动填充按钮不工作？

A: 检查以下几点：
1. 确保模板配置中的 `package_name` 和 `signature_hash` 正确
2. 确保应用的 MainActivity 可以接收 Intent（`android:launchMode` 设置为 `singleTask` 或 `singleTop`）
3. 检查应用的签名哈希是否与配置一致
4. 查看日志输出，确认 `onNewIntent` 方法被调用

### Q: 可以同时支持零点击和自动填充吗？

A: 可以！这正是推荐的做法。`requestOtp` 方法支持同时监听两种方式：

```typescript
await otpAuth.requestOtp(
  handleZeroTap,      // 零点击
  handleError,        // 错误
  handleAutofill      // 自动填充按钮
);
```

### Q: 如何获取应用签名哈希？

A: 使用模块提供的方法：

```typescript
import { getAppSignatureHash } from 'react-native-whatsapp-zero-tap-auth';

const info = await getAppSignatureHash();
console.log('Package Name:', info.packageName);
console.log('Signature Hash:', info.signatureHash);
```

## 测试建议

1. **测试零点击认证**：不点击按钮，验证是否自动接收
2. **测试自动填充按钮**：点击 WhatsApp 消息中的按钮，验证是否触发事件
3. **测试错误处理**：断开网络、取消请求等场景
4. **测试不同 WhatsApp 版本**：WhatsApp 和 WhatsApp Business

## 示例应用

查看 `example/` 目录获取完整的示例代码。

## 技术支持

如有问题，请查看：
- [WhatsApp 官方文档](https://developers.facebook.com/docs/whatsapp)
- [项目 Issues](https://github.com/your-repo/issues)

