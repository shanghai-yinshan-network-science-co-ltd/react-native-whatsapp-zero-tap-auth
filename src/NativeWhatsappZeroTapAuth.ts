import { TurboModuleRegistry, type TurboModule } from 'react-native';

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

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  
  // WhatsApp零点击认证相关方法
  isWhatsAppInstalled(): Promise<boolean>;
  getInstalledWhatsAppApps(): Promise<string[]>;
  initiateHandshake(): Promise<boolean>;
  getAppSignatureHash(): Promise<AppSignatureInfo>;
  getDeviceInfo(): Promise<DeviceInfo>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('WhatsappZeroTapAuth');
