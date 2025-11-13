# MainActivity 配置说明（自动填充按钮支持）

## 为什么需要配置 MainActivity

WhatsApp 的自动填充按钮通过 **Intent** 启动您的应用并传递验证码。为了正确接收这些 Intent，需要配置您的 MainActivity。

## 必需的配置

### 1. 设置 launchMode

在您的应用的 `AndroidManifest.xml` 中，确保 MainActivity 的 `launchMode` 设置为 `singleTask` 或 `singleTop`：

```xml
<activity
  android:name=".MainActivity"
  android:launchMode="singleTask"
  android:configChanges="keyboard|keyboardHidden|orientation|screenSize|uiMode"
  android:windowSoftInputMode="adjustResize"
  android:exported="true">
  
  <intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
  </intent-filter>
  
  <!-- 支持从外部启动 -->
  <intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
  </intent-filter>
</activity>
```

### 2. launchMode 选项说明

| launchMode | 说明 | 推荐度 |
|-----------|------|--------|
| `singleTask` | Activity 在任务栈中唯一，新 Intent 通过 `onNewIntent` 传递 | ⭐⭐⭐ 推荐 |
| `singleTop` | 如果 Activity 在栈顶，新 Intent 通过 `onNewIntent` 传递 | ⭐⭐ 可用 |
| `standard` | 每次都创建新实例，**无法接收 onNewIntent** | ❌ 不支持 |

**推荐使用 `singleTask`**，因为：
- 确保应用只有一个实例
- 始终通过 `onNewIntent` 接收新 Intent
- 符合大多数应用的使用场景

## 工作流程

```
用户点击 WhatsApp 中的自动填充按钮
          ↓
WhatsApp 创建 Intent 并启动您的应用
          ↓
Android 系统检查 MainActivity 是否已存在
          ↓
如果存在：调用 onNewIntent() 方法 ← 我们在这里接收验证码
如果不存在：创建新的 Activity 实例
          ↓
WhatsAppAutofillHandler (ActivityEventListener) 处理
          ↓
提取验证码并发送到 React Native
          ↓
JavaScript 层接收事件并自动填充
```

## 代码实现原理

### Android 原生层

```kotlin
class WhatsAppAutofillHandler(
  private val reactContext: ReactApplicationContext
) : ActivityEventListener {
  
  // 当 Activity 接收到新的 Intent 时调用
  override fun onNewIntent(intent: Intent?) {
    intent?.let {
      // 从 Intent 中提取验证码
      val code = it.getStringExtra("code") 
        ?: it.data?.getQueryParameter("code")
      
      if (code != null) {
        // 发送到 React Native
        sendAutofillEvent(code)
      }
    }
  }
}
```

### 模块初始化

```kotlin
class WhatsappZeroTapAuthModuleImpl(
  private val reactContext: ReactApplicationContext
) {
  private val autofillHandler = WhatsAppAutofillHandler(reactContext)
  
  init {
    // 注册 Activity 事件监听器
    reactContext.addActivityEventListener(autofillHandler)
  }
}
```

## 示例配置

### 完整的 AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

  <uses-permission android:name="android.permission.INTERNET" />

  <!-- WhatsApp 查询权限 -->
  <queries>
    <package android:name="com.whatsapp"/>
    <package android:name="com.whatsapp.w4b"/>
  </queries>

  <application
    android:name=".MainApplication"
    android:label="@string/app_name"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:allowBackup="false"
    android:theme="@style/AppTheme">
    
    <!-- 主 Activity - 使用 singleTask -->
    <activity
      android:name=".MainActivity"
      android:label="@string/app_name"
      android:launchMode="singleTask"
      android:configChanges="keyboard|keyboardHidden|orientation|screenSize|uiMode"
      android:windowSoftInputMode="adjustResize"
      android:exported="true">
      
      <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>
      
      <!-- 支持从外部应用启动 -->
      <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
      </intent-filter>
    </activity>
  </application>

</manifest>
```

## 验证配置

### 1. 检查 launchMode

在 `android/app/src/main/AndroidManifest.xml` 中查找 MainActivity：

```bash
grep -A 5 "android:name=\".MainActivity\"" android/app/src/main/AndroidManifest.xml
```

应该看到：
```xml
android:launchMode="singleTask"
```

### 2. 测试自动填充

运行以下测试：

```kotlin
// 在 MainActivity.kt 中添加日志
override fun onNewIntent(intent: Intent?) {
  super.onNewIntent(intent)
  Log.d("MainActivity", "onNewIntent called: ${intent?.action}")
  Log.d("MainActivity", "Intent data: ${intent?.data}")
  Log.d("MainActivity", "Intent extras: ${intent?.extras}")
}
```

点击 WhatsApp 中的自动填充按钮后，应该在 logcat 中看到相关日志。

## 常见问题

### Q1: 为什么我的应用没有接收到 Intent？

**A:** 检查以下几点：
1. MainActivity 的 `launchMode` 是否设置为 `singleTask` 或 `singleTop`
2. MainActivity 的 `android:exported` 是否设置为 `true`
3. 是否添加了必要的 `intent-filter`
4. WhatsApp 模板中的包名和签名哈希是否正确

### Q2: launchMode 会影响应用的其他功能吗？

**A:** `singleTask` 模式是常用的配置，通常不会有问题：
- ✅ 适用于大多数应用
- ✅ 提升内存效率
- ✅ 防止重复创建 Activity
- ⚠️ 注意：如果应用需要多个实例，需要特殊处理

### Q3: 可以在 React Native 层处理吗？

**A:** 不能。这必须在原生层处理，因为：
1. Intent 是 Android 原生概念
2. `onNewIntent` 是 Activity 的生命周期方法
3. React Native 需要通过 bridge 接收这些事件

### Q4: 我已经有自定义的 MainActivity，怎么办？

**A:** 模块会自动注册 `ActivityEventListener`，您只需要：
1. 确保 `launchMode` 配置正确
2. 不需要修改 MainActivity 的代码
3. 模块会自动处理所有逻辑

## React Native 的默认配置

默认情况下，React Native 创建的项目的 MainActivity 配置为：

```xml
<activity
  android:name=".MainActivity"
  android:label="@string/app_name"
  android:configChanges="keyboard|keyboardHidden|orientation|screenSize|uiMode"
  android:windowSoftInputMode="adjustResize"
  android:exported="true">
```

**需要添加的配置：**
```xml
android:launchMode="singleTask"
```

## 总结

1. ✅ 在 `AndroidManifest.xml` 中设置 `android:launchMode="singleTask"`
2. ✅ 确保 `android:exported="true"`
3. ✅ 模块会自动注册 `ActivityEventListener`
4. ✅ 自动填充功能即可正常工作

这是正确的实现方式，通过 `onNewIntent` 接收 WhatsApp 传递的验证码！

