#import "WhatsappZeroTapAuth.h"
#import <UIKit/UIKit.h>

@implementation WhatsappZeroTapAuth
RCT_EXPORT_MODULE()

- (NSNumber *)multiply:(double)a b:(double)b {
    NSNumber *result = @(a * b);

    return result;
}

// 检查WhatsApp是否已安装
- (void)isWhatsAppInstalled:(RCTPromiseResolveBlock)resolve
                     reject:(RCTPromiseRejectBlock)reject {
    NSURL *whatsappOtpURL = [NSURL URLWithString:@"whatsapp://otp"];
    NSURL *whatsappBusinessOtpURL = [NSURL URLWithString:@"whatsapp-business://otp"];
    
    BOOL isInstalled = [[UIApplication sharedApplication] canOpenURL:whatsappOtpURL] ||
                       [[UIApplication sharedApplication] canOpenURL:whatsappBusinessOtpURL];
    
    resolve(@(isInstalled));
}

// 获取已安装的WhatsApp应用列表
- (void)getInstalledWhatsAppApps:(RCTPromiseResolveBlock)resolve
                          reject:(RCTPromiseRejectBlock)reject {
    NSMutableArray *installedApps = [NSMutableArray array];
    
    NSURL *whatsappOtpURL = [NSURL URLWithString:@"whatsapp://otp"];
    NSURL *whatsappBusinessOtpURL = [NSURL URLWithString:@"whatsapp-business://otp"];
    
    if ([[UIApplication sharedApplication] canOpenURL:whatsappOtpURL]) {
        [installedApps addObject:@"com.whatsapp"];
    }
    
    if ([[UIApplication sharedApplication] canOpenURL:whatsappBusinessOtpURL]) {
        [installedApps addObject:@"com.whatsapp.w4b"];
    }
    
    resolve(installedApps);
}

// 发起握手（iOS不需要实现，仅Android需要）
- (void)initiateHandshake:(RCTPromiseResolveBlock)resolve
                   reject:(RCTPromiseRejectBlock)reject {
    // iOS平台不需要握手机制，直接返回true
    resolve(@(YES));
}

// 获取应用签名哈希（iOS不需要实现，仅Android需要）
- (void)getAppSignatureHash:(RCTPromiseResolveBlock)resolve
                     reject:(RCTPromiseRejectBlock)reject {
    NSDictionary *result = @{
        @"packageName": [[NSBundle mainBundle] bundleIdentifier] ?: @"unknown",
        @"signatureHash": @"iOS不需要签名哈希"
    };
    resolve(result);
}

// 获取设备信息
- (void)getDeviceInfo:(RCTPromiseResolveBlock)resolve
               reject:(RCTPromiseRejectBlock)reject {
    NSString *systemVersion = [[UIDevice currentDevice] systemVersion];
    NSString *model = [[UIDevice currentDevice] model];
    NSString *bundleId = [[NSBundle mainBundle] bundleIdentifier] ?: @"unknown";
    
    NSDictionary *result = @{
        @"packageName": bundleId,
        @"sdkVersion": systemVersion,
        @"deviceModel": model,
        @"manufacturer": @"Apple"
    };
    resolve(result);
}

// 实现TurboModule所需的addListener和removeListeners方法
- (void)addListener:(NSString *)eventName {
    // iOS端事件监听（如需要可以后续实现）
}

- (void)removeListeners:(double)count {
    // iOS端移除监听器（如需要可以后续实现）
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeWhatsappZeroTapAuthSpecJSI>(params);
}

@end
