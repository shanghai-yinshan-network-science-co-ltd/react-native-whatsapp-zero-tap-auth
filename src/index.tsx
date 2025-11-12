import WhatsappZeroTapAuth from './NativeWhatsappZeroTapAuth';
import { DeviceEventEmitter, Platform } from 'react-native';
import type { AppSignatureInfo, DeviceInfo } from './NativeWhatsappZeroTapAuth';

// 事件名称常量
export const OTP_EVENTS = {
  OTP_RECEIVED: 'onOtpReceived',
  OTP_ERROR: 'onOtpError',
  AUTOFILL_BUTTON_CLICKED: 'onAutofillButtonClicked',
} as const;

// 事件类型定义
export interface OtpReceivedEvent {
  code: string;
  source: string; // 'com.whatsapp' 或 'com.whatsapp.w4b'
  timestamp: number;
}

export interface OtpErrorEvent {
  errorCode: string;
  errorMessage: string;
  timestamp: number;
}

export interface AutofillButtonEvent {
  code: string;
  packageName: string;
  timestamp: number;
}

// 事件监听器类型
export type OtpReceivedListener = (event: OtpReceivedEvent) => void;
export type OtpErrorListener = (event: OtpErrorEvent) => void;
export type AutofillButtonListener = (event: AutofillButtonEvent) => void;

// 导出类型
export type { AppSignatureInfo, DeviceInfo };

/**
 * 两个数字相乘（示例方法）
 */
export function multiply(a: number, b: number): number {
  return WhatsappZeroTapAuth?.multiply(a, b) ?? 0;
}

/**
 * 检查WhatsApp是否已安装
 */
export function isWhatsAppInstalled(): Promise<boolean> {
  return WhatsappZeroTapAuth?.isWhatsAppInstalled() ?? Promise.resolve(false);
}

/**
 * 获取已安装的WhatsApp应用列表
 */
export function getInstalledWhatsAppApps(): Promise<string[]> {
  return WhatsappZeroTapAuth?.getInstalledWhatsAppApps() ?? Promise.resolve([]);
}

/**
 * 发起与WhatsApp的握手
 * 必须在发送验证码模板消息之前调用
 */
export function initiateHandshake(): Promise<boolean> {
  return WhatsappZeroTapAuth?.initiateHandshake() ?? Promise.resolve(false);
}

/**
 * 获取应用签名哈希（用于配置WhatsApp模板）
 */
export function getAppSignatureHash(): Promise<AppSignatureInfo> {
  return (
    WhatsappZeroTapAuth?.getAppSignatureHash() ??
    Promise.reject(new Error('Native module not available'))
  );
}

/**
 * 获取设备信息（用于调试）
 */
export function getDeviceInfo(): Promise<DeviceInfo> {
  return (
    WhatsappZeroTapAuth?.getDeviceInfo() ??
    Promise.reject(new Error('Native module not available'))
  );
}

/**
 * 添加验证码接收事件监听器
 */
export function addOtpReceivedListener(
  listener: OtpReceivedListener
): () => void {
  if (Platform.OS !== 'android') {
    console.warn('WhatsApp零点击认证仅在Android平台支持');
    return () => {};
  }

  const subscription = DeviceEventEmitter.addListener(
    OTP_EVENTS.OTP_RECEIVED,
    listener
  );
  return () => subscription.remove();
}

/**
 * 添加验证码错误事件监听器
 */
export function addOtpErrorListener(listener: OtpErrorListener): () => void {
  if (Platform.OS !== 'android') {
    console.warn('WhatsApp零点击认证仅在Android平台支持');
    return () => {};
  }

  const subscription = DeviceEventEmitter.addListener(
    OTP_EVENTS.OTP_ERROR,
    listener
  );
  return () => subscription.remove();
}

/**
 * 添加自动填充按钮点击事件监听器
 */
export function addAutofillButtonListener(
  listener: AutofillButtonListener
): () => void {
  if (Platform.OS !== 'android') {
    console.warn('WhatsApp一键自动填充仅在Android平台支持');
    return () => {};
  }

  const subscription = DeviceEventEmitter.addListener(
    OTP_EVENTS.AUTOFILL_BUTTON_CLICKED,
    listener
  );
  return () => subscription.remove();
}

/**
 * 移除所有事件监听器
 */
export function removeAllListeners(): void {
  if (Platform.OS === 'android') {
    DeviceEventEmitter.removeAllListeners(OTP_EVENTS.OTP_RECEIVED);
    DeviceEventEmitter.removeAllListeners(OTP_EVENTS.OTP_ERROR);
    DeviceEventEmitter.removeAllListeners(OTP_EVENTS.AUTOFILL_BUTTON_CLICKED);
  }
}

/**
 * WhatsApp零点击认证工具类
 */
export class WhatsAppZeroTapAuth {
  private otpReceivedUnsubscribe?: () => void;
  private otpErrorUnsubscribe?: () => void;
  private autofillButtonUnsubscribe?: () => void;

  /**
   * 开始监听验证码接收
   */
  startListening(
    onOtpReceived: OtpReceivedListener,
    onOtpError?: OtpErrorListener,
    onAutofillButton?: AutofillButtonListener
  ): void {
    this.stopListening();

    this.otpReceivedUnsubscribe = addOtpReceivedListener(onOtpReceived);

    if (onOtpError) {
      this.otpErrorUnsubscribe = addOtpErrorListener(onOtpError);
    }

    if (onAutofillButton) {
      this.autofillButtonUnsubscribe =
        addAutofillButtonListener(onAutofillButton);
    }
  }

  /**
   * 停止监听验证码接收
   */
  stopListening(): void {
    this.otpReceivedUnsubscribe?.();
    this.otpErrorUnsubscribe?.();
    this.autofillButtonUnsubscribe?.();
    this.otpReceivedUnsubscribe = undefined;
    this.otpErrorUnsubscribe = undefined;
    this.autofillButtonUnsubscribe = undefined;
  }

  /**
   * 请求验证码的完整流程（支持自动填充按钮）
   * 1. 检查WhatsApp是否安装
   * 2. 发起握手
   * 3. 开始监听验证码接收和自动填充按钮点击
   */
  async requestOtp(
    onOtpReceived: OtpReceivedListener,
    onOtpError?: OtpErrorListener,
    onAutofillButton?: AutofillButtonListener
  ): Promise<{ success: boolean; message: string }> {
    try {
      // 检查平台支持
      if (Platform.OS !== 'android') {
        return {
          success: false,
          message: 'WhatsApp零点击认证仅在Android平台支持',
        };
      }

      // 检查WhatsApp是否安装
      const isInstalled = await isWhatsAppInstalled();
      if (!isInstalled) {
        return {
          success: false,
          message: 'WhatsApp未安装，请先安装WhatsApp应用',
        };
      }

      // 开始监听验证码接收和自动填充按钮
      this.startListening(onOtpReceived, onOtpError, onAutofillButton);

      // 发起握手
      const handshakeSuccess = await initiateHandshake();
      if (!handshakeSuccess) {
        this.stopListening();
        return {
          success: false,
          message: '与WhatsApp握手失败',
        };
      }

      return {
        success: true,
        message:
          '已发起握手，等待接收验证码。现在可以通过API发送验证码模板消息。',
      };
    } catch (error) {
      this.stopListening();
      return {
        success: false,
        message: `请求验证码失败: ${error instanceof Error ? error.message : String(error)}`,
      };
    }
  }
}
