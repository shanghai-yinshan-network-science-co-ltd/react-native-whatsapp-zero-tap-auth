#import "WhatsappZeroTapAuth.h"
#import <React/RCTUtils.h>

@implementation WhatsappZeroTapAuth

RCT_EXPORT_MODULE()

// 导出同步方法
RCT_EXPORT_SYNCHRONOUS_TYPED_METHOD(NSNumber *, multiply:(double)a b:(double)b) {
    return @(a * b);
}

// 导出异步方法
RCT_EXPORT_METHOD(isWhatsAppInstalled:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    // iOS 平台不支持 WhatsApp Zero Tap Auth
    resolve(@NO);
}

RCT_EXPORT_METHOD(getInstalledWhatsAppApps:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    // iOS 平台不支持，返回空数组
    resolve(@[]);
}

RCT_EXPORT_METHOD(initiateHandshake:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    // iOS 平台不支持
    reject(@"PLATFORM_NOT_SUPPORTED", @"WhatsApp Zero Tap Auth is only supported on Android", nil);
}

RCT_EXPORT_METHOD(getAppSignatureHash:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    // iOS 平台不支持
    reject(@"PLATFORM_NOT_SUPPORTED", @"WhatsApp Zero Tap Auth is only supported on Android", nil);
}

RCT_EXPORT_METHOD(getDeviceInfo:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSMutableDictionary *deviceInfo = [NSMutableDictionary dictionary];
    
    // 获取基本的设备信息
    deviceInfo[@"packageName"] = [[NSBundle mainBundle] bundleIdentifier] ?: @"unknown";
    deviceInfo[@"sdkVersion"] = [[UIDevice currentDevice] systemVersion] ?: @"unknown";
    deviceInfo[@"deviceModel"] = [[UIDevice currentDevice] model] ?: @"unknown";
    deviceInfo[@"manufacturer"] = @"Apple";
    
    resolve(deviceInfo);
}

@end
