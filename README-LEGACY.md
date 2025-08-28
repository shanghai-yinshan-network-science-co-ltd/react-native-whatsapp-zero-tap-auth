# React Native WhatsApp Zero Tap Auth - Legacy Architecture

这是专门为 React Native 0.72 和老架构设计的版本。

## 版本说明

- **当前版本**: `0.1.0-legacy`
- **支持的 React Native 版本**: 0.72.x
- **架构**: 老架构 (Old Architecture / Paper)
- **平台**: Android (iOS 仅提供基础兼容性)

## 与主版本的区别

### 主要变更

1. **移除新架构依赖**
   - 不使用 `TurboModuleRegistry`
   - 不使用 Codegen 自动生成的接口
   - 使用传统的 `NativeModules` API

2. **Android 实现变更**
   - `WhatsappZeroTapAuthModule` 继承自 `ReactContextBaseJavaModule`
   - `WhatsappZeroTapAuthPackage` 实现 `ReactPackage` 接口
   - 使用 `@ReactMethod` 注解

3. **iOS 实现变更**
   - 使用 `RCTBridgeModule` 协议
   - 使用 `RCT_EXPORT_METHOD` 宏

### 兼容性

| React Native 版本 | 新架构 | 支持状态 |
|------------------|-------|---------|
| 0.72.x | 未启用 | ✅ 完全支持 |
| 0.72.x | 已启用 | ❌ 不支持 |
| 0.73+ | 任意 | ❌ 请使用主版本 |

## 安装方法

### 1. 使用本地版本

```bash
# 克隆仓库并切换到 legacy 分支
git clone https://github.com/shanghai-yinshan-network-science-co-ltd/react-native-whatsapp-zero-tap-auth.git
cd react-native-whatsapp-zero-tap-auth
git checkout rn-0.72-legacy-arch

# 在你的项目中安装
cd /path/to/your/project
npm install /path/to/react-native-whatsapp-zero-tap-auth
```

### 2. 从 NPM 安装 (如果发布了 legacy 版本)

```bash
npm install react-native-whatsapp-zero-tap-auth@legacy
```

## 配置说明

### Android 配置

确保在 `MainApplication.java` 或 `MainApplication.kt` 中添加包：

```kotlin
import com.whatsappzerotapauth.WhatsappZeroTapAuthPackage

class MainApplication : Application(), ReactApplication {
    // ...
    override fun getReactNativeHost(): ReactNativeHost {
        return object : DefaultReactNativeHost(this) {
            // ...
            override fun getPackages(): List<ReactPackage> {
                return PackageList(this).packages.apply {
                    // 手动添加包
                    add(WhatsappZeroTapAuthPackage())
                }
            }
        }
    }
}
```

### iOS 配置

在 iOS 中，该库会自动注册，但 WhatsApp Zero Tap Auth 功能仅在 Android 上可用。

## 使用方法

使用方法与主版本完全相同：

```typescript
import {
  WhatsAppZeroTapAuth,
  isWhatsAppInstalled,
  getAppSignatureHash,
  // ... 其他导出
} from 'react-native-whatsapp-zero-tap-auth';

// 检查 WhatsApp 是否安装
const installed = await isWhatsAppInstalled();

// 获取应用签名哈希
const signatureInfo = await getAppSignatureHash();

// 使用工具类
const otpAuth = new WhatsAppZeroTapAuth();
const result = await otpAuth.requestOtp(
  (event) => console.log('收到验证码:', event.code),
  (error) => console.log('错误:', error.errorMessage)
);
```

## 注意事项

1. **仅支持老架构**: 这个版本专门为老架构设计，不能在启用新架构的项目中使用
2. **Android 专用**: WhatsApp Zero Tap Auth 功能仅在 Android 平台可用
3. **手动链接**: 如果自动链接失败，可能需要手动添加包到 `MainApplication`
4. **版本锁定**: 请确保 React Native 版本在 0.72.x 范围内

## 故障排除

### 常见问题

1. **模块未找到错误**
   ```
   The package 'react-native-whatsapp-zero-tap-auth' doesn't seem to be linked
   ```
   
   **解决方案**: 确保包已正确添加到 `MainApplication` 中

2. **编译错误**
   ```
   Cannot resolve symbol 'NativeWhatsappZeroTapAuthSpec'
   ```
   
   **解决方案**: 确保使用的是 legacy 分支，而不是主分支

3. **运行时错误**
   ```
   TurboModuleRegistry.getEnforcing is not a function
   ```
   
   **解决方案**: 检查 React Native 版本和架构配置

## 迁移指南

### 从新架构版本迁移

如果你之前使用的是新架构版本，迁移到 legacy 版本：

1. 卸载当前版本
2. 安装 legacy 版本
3. 确保 React Native 版本为 0.72.x
4. 重新构建项目

### 迁移到新架构版本

当你准备升级到 React Native 0.73+ 并启用新架构时：

1. 升级 React Native 版本
2. 启用新架构
3. 卸载 legacy 版本
4. 安装主版本

## 支持

如果遇到问题，请在 GitHub 仓库中创建 issue，并标明使用的是 legacy 版本。
