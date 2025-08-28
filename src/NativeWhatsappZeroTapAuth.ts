import { NativeModules, Platform } from 'react-native';

export interface AppSignatureInfo {
  signatureHash: string;
  packageName: string;
}

export interface DeviceInfo {
  packageName: string;
  sdkVersion: string;
  deviceModel: string;
  manufacturer: string;
}

export interface Spec {
  multiply(a: number, b: number): number;

  // WhatsApp零点击认证相关方法
  isWhatsAppInstalled(): Promise<boolean>;
  getInstalledWhatsAppApps(): Promise<string[]>;
  initiateHandshake(): Promise<boolean>;
  getAppSignatureHash(): Promise<AppSignatureInfo>;
  getDeviceInfo(): Promise<DeviceInfo>;
}

const LINKING_ERROR =
  `The package 'react-native-whatsapp-zero-tap-auth' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

// 使用传统的 NativeModules 方式，兼容老架构
const WhatsappZeroTapAuth = NativeModules.WhatsappZeroTapAuth
  ? NativeModules.WhatsappZeroTapAuth
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export default WhatsappZeroTapAuth as Spec;
