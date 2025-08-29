package com.whatsappzerotapauth

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import java.util.HashMap

class WhatsappZeroTapAuthPackage : TurboReactPackage() {
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return if (name == WhatsappZeroTapAuthModule.NAME) {
      WhatsappZeroTapAuthModule(reactContext)
    } else {
      null
    }
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
    return ReactModuleInfoProvider {
      val moduleInfos: MutableMap<String, ReactModuleInfo> = HashMap()
      val isTurboModule = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
      moduleInfos[WhatsappZeroTapAuthModule.NAME] = ReactModuleInfo(
        WhatsappZeroTapAuthModule.NAME,
        WhatsappZeroTapAuthModule.NAME,
        false,  // canOverrideExistingModule
        false,  // needsEagerInit
        false,  // isCxxModule
        isTurboModule // isTurboModule - 根据架构动态设置
      )
      moduleInfos
    }
  }
}
