# iOS WhatsApp检测配置指南

## 概述

在iOS平台上检测WhatsApp是否安装，需要在应用的`Info.plist`文件中配置`LSApplicationQueriesSchemes`，以允许应用查询特定的URL schemes。

## 配置步骤

### 1. 修改Info.plist文件

在你的iOS项目的`Info.plist`文件中添加以下配置：

```xml
<key>LSApplicationQueriesSchemes</key>
<array>
    <string>whatsapp</string>
    <string>whatsapp-business</string>
</array>
```

### 2. 完整示例

以下是添加配置后的`Info.plist`文件示例：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <!-- 其他配置项 -->
    
    <key>LSApplicationQueriesSchemes</key>
    <array>
        <string>whatsapp</string>
        <string>whatsapp-business</string>
    </array>
</dict>
</plist>
```

## 使用方法

配置完成后，你可以在React Native代码中使用以下方法：

```typescript
import { isWhatsAppInstalled, getInstalledWhatsAppApps } from 'react-native-whatsapp-zero-tap-auth';

// 检查是否安装了WhatsApp（包括普通版和商业版）
const installed = await isWhatsAppInstalled();
console.log('WhatsApp已安装:', installed);

// 获取已安装的WhatsApp应用列表
const apps = await getInstalledWhatsAppApps();
console.log('已安装的WhatsApp应用:', apps);
// 可能的返回值: ['com.whatsapp'] 或 ['com.whatsapp.w4b'] 或 ['com.whatsapp', 'com.whatsapp.w4b'] 或 []
```

### iOS原生代码实现

本库基于WhatsApp官方文档的推荐方法实现：

```swift
let schemeURL = URL(string: "whatsapp://otp")!
let isWhatsAppInstalled = UIApplication.shared.canOpenURL(schemeURL)
```

对应的Objective-C实现：

```objc
NSURL *whatsappOtpURL = [NSURL URLWithString:@"whatsapp://otp"];
BOOL isInstalled = [[UIApplication sharedApplication] canOpenURL:whatsappOtpURL];
```

## WhatsApp URL Schemes

该库使用WhatsApp官方推荐的OTP URL schemes来检测应用是否安装：

- **WhatsApp普通版**: `whatsapp://otp`
  - Package标识: `com.whatsapp`
  - 在Info.plist中声明: `whatsapp`
  
- **WhatsApp Business**: `whatsapp-business://otp`
  - Package标识: `com.whatsapp.w4b`
  - 在Info.plist中声明: `whatsapp-business`

**注意**: 虽然代码中检测的是 `whatsapp://otp` 完整URL，但在 `Info.plist` 的 `LSApplicationQueriesSchemes` 中只需要声明基础scheme（即 `whatsapp` 和 `whatsapp-business`），不需要包含路径部分。

## 注意事项

1. **必须配置**: 如果不在`Info.plist`中添加`LSApplicationQueriesSchemes`配置，`canOpenURL`方法将始终返回`false`，即使设备上已安装WhatsApp。

2. **Apple限制**: iOS系统限制了应用可以查询的URL schemes数量（最多50个）。确保不要超过这个限制。

3. **隐私政策**: 如果你的应用要检测其他应用是否安装，Apple可能会在审核时要求你说明理由。请在应用的隐私政策中说明为什么需要检测WhatsApp。

4. **测试**: 
   - 在模拟器上测试时，可能需要在模拟器上安装WhatsApp才能测试检测功能
   - 建议在真实设备上进行完整测试

## 常见问题

### Q: 为什么canOpenURL总是返回false？
A: 请确保已经在`Info.plist`中正确配置了`LSApplicationQueriesSchemes`。

### Q: 是否需要申请特殊权限？
A: 不需要。检测URL scheme不需要特殊权限，但需要在`Info.plist`中声明。

### Q: 这个检测方法是否会影响应用审核？
A: 一般不会。检测WhatsApp是否安装是合理的需求，特别是当你的应用提供与WhatsApp相关的功能时。

## 相关链接

- [Apple官方文档: LSApplicationQueriesSchemes](https://developer.apple.com/documentation/bundleresources/information_property_list/lsapplicationqueriesschemes)
- [WhatsApp URL Schemes](https://faq.whatsapp.com/general/ios/how-to-link-to-whatsapp-from-a-different-app)

