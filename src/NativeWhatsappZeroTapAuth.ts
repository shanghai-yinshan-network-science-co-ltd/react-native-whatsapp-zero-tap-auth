import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

// 类型定义
export interface AppSignatureInfo {
  packageName: string;
  signatureHash: string;
}

export interface DeviceInfo {
  packageName: string;
  sdkVersion: string;
  deviceModel: string;
  manufacturer: string;
}

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  isWhatsAppInstalled(): Promise<boolean>;
  getInstalledWhatsAppApps(): Promise<string[]>;
  initiateHandshake(): Promise<boolean>;
  getAppSignatureHash(): Promise<AppSignatureInfo>;
  getDeviceInfo(): Promise<DeviceInfo>;
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.get<Spec>(
  'WhatsappZeroTapAuth'
) as Spec | null;
