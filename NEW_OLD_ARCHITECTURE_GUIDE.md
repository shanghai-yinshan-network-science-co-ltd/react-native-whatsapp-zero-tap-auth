# React Native 新老架构并存支持指南

本库已支持 React Native 新老架构并存，允许您在同一项目中无缝切换和测试两种架构。

## 架构概述

### 新架构 (TurboModule)
- 使用 TurboModule 系统
- 更好的性能和类型安全
- 从 React Native 0.76 开始默认启用

### 老架构 (Bridge)
- 使用传统的 Bridge 系统
- 广泛的兼容性
- 适用于较旧的 React Native 版本

## 实现原理

本库参考了 [react-native-update](https://github.com/reactnativecn/react-native-update) 的实现方式：

1. **构建时架构检测**：通过 `isNewArchitectureEnabled()` 函数检测当前架构
2. **分离源码目录**：
   - `android/src/newarch/` - 新架构实现
   - `android/src/oldarch/` - 老架构实现
   - `android/src/main/` - 共享实现逻辑
3. **动态模块选择**：根据架构标志选择对应的模块实现
4. **共享业务逻辑**：核心功能在 `WhatsappZeroTapAuthModuleImpl` 中实现

## 文件结构

```
android/
├── src/
│   ├── main/
│   │   └── java/com/whatsappzerotapauth/
│   │       ├── WhatsappZeroTapAuthModuleImpl.kt     # 共享实现
│   │       ├── WhatsappZeroTapAuthPackage.kt        # 动态包配置
│   │       ├── WhatsAppOtpHandler.kt                # 业务逻辑
│   │       └── OtpCodeReceiver.kt                   # 广播接收器
│   ├── newarch/
│   │   └── com/whatsappzerotapauth/
│   │       └── WhatsappZeroTapAuthModule.kt         # TurboModule 实现
│   └── oldarch/
│       └── com/whatsappzerotapauth/
│           └── WhatsappZeroTapAuthModule.kt         # Bridge 实现
└── build.gradle                                     # 架构检测配置
```

## 使用方法

### 1. 切换到新架构

在 `android/gradle.properties` 中设置：

```properties
newArchEnabled=true
```

### 2. 切换到老架构

在 `android/gradle.properties` 中设置：

```properties
newArchEnabled=false
```

### 3. 重新构建

切换架构后需要清理并重新构建：

```bash
cd android
./gradlew clean
cd ..
# 重新安装依赖
yarn install
# 重新构建
yarn android
```

## 架构检测

可以通过以下方式检查当前使用的架构：

### 在 Android 代码中
```kotlin
val isNewArch = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
Log.d("Architecture", "Using new architecture: $isNewArch")
```

### 在 JavaScript 中
```javascript
import { Platform } from 'react-native';

// 检查平台和版本
console.log('Platform:', Platform.OS);
console.log('React Native Version:', Platform.constants.reactNativeVersion);
```

## 测试建议

1. **功能测试**：在两种架构下测试所有功能
2. **性能测试**：比较两种架构的性能表现
3. **兼容性测试**：测试与其他库的兼容性

## 注意事项

1. **API 一致性**：两种架构提供相同的 JavaScript API
2. **类型安全**：新架构提供更好的类型检查
3. **性能差异**：新架构通常有更好的性能
4. **依赖要求**：确保 React Native 版本支持所选架构

## 故障排除

### 构建错误
- 确保 `gradle.properties` 中的 `newArchEnabled` 设置正确
- 清理构建缓存：`./gradlew clean`
- 重新安装依赖：`yarn install`

### 运行时错误
- 检查模块是否正确注册
- 验证架构设置是否一致
- 查看日志中的架构信息

## 升级指南

### 从老架构升级到新架构

1. 更新 React Native 到支持新架构的版本
2. 设置 `newArchEnabled=true`
3. 重新构建项目
4. 测试所有功能

### 从新架构降级到老架构

1. 设置 `newArchEnabled=false`
2. 重新构建项目
3. 验证功能正常

## 技术参考

- [React Native 新架构文档](https://reactnative.dev/architecture/landing-page)
- [TurboModule 指南](https://github.com/reactwg/react-native-new-architecture)
- [react-native-update 源码](https://github.com/reactnativecn/react-native-update)
