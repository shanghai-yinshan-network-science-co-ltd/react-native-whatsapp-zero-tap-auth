import React, { useState, useEffect, useCallback } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  Alert,
  Platform,
} from 'react-native';
import {
  WhatsAppZeroTapAuth,
  isWhatsAppInstalled,
  getInstalledWhatsAppApps,
  getAppSignatureHash,
  getDeviceInfo,
  type OtpReceivedEvent,
  type OtpErrorEvent,
  type AppSignatureInfo,
  type DeviceInfo,
} from 'react-native-whatsapp-zero-tap-auth';

export default function App() {
  const [isWhatsAppInstalled_, setIsWhatsAppInstalled] = useState<boolean | null>(null);
  const [installedApps, setInstalledApps] = useState<string[]>([]);
  const [signatureInfo, setSignatureInfo] = useState<AppSignatureInfo | null>(null);
  const [deviceInfo_, setDeviceInfo] = useState<DeviceInfo | null>(null);
  const [otpAuth] = useState(() => new WhatsAppZeroTapAuth());
  const [isListening, setIsListening] = useState(false);
  const [receivedOtp, setReceivedOtp] = useState<string | null>(null);
  const [lastError, setLastError] = useState<string | null>(null);

  // 检查WhatsApp安装状态
  const checkWhatsAppInstallation = useCallback(async () => {
    try {
      const installed = await isWhatsAppInstalled();
      setIsWhatsAppInstalled(installed);
      
      if (installed) {
        const apps = await getInstalledWhatsAppApps();
        setInstalledApps(apps);
      }
    } catch (error) {
      console.error('检查WhatsApp安装状态失败:', error);
      Alert.alert('错误', '检查WhatsApp安装状态失败');
    }
  }, []);

  // 获取应用签名信息
  const fetchSignatureInfo = useCallback(async () => {
    try {
      const info = await getAppSignatureHash();
      setSignatureInfo(info);
    } catch (error) {
      console.error('获取签名信息失败:', error);
      Alert.alert('错误', '获取签名信息失败');
    }
  }, []);

  // 获取设备信息
  const fetchDeviceInfo = useCallback(async () => {
    try {
      const info = await getDeviceInfo();
      setDeviceInfo(info);
    } catch (error) {
      console.error('获取设备信息失败:', error);
      Alert.alert('错误', '获取设备信息失败');
    }
  }, []);

  // OTP接收处理
  const handleOtpReceived = useCallback((event: OtpReceivedEvent) => {
    console.log('收到验证码:', event);
    setReceivedOtp(event.code);
    setLastError(null);
    Alert.alert(
      '验证码接收成功',
      `验证码: ${event.code}\n来源: ${event.source}`
    );
  }, []);

  // OTP错误处理
  const handleOtpError = useCallback((event: OtpErrorEvent) => {
    console.log('验证码错误:', event);
    setLastError(event.errorMessage);
    Alert.alert('验证码接收失败', event.errorMessage);
  }, []);

  // 开始请求OTP
  const requestOtp = useCallback(async () => {
    if (Platform.OS !== 'android') {
      Alert.alert('不支持', 'WhatsApp零点击认证仅在Android平台支持');
      return;
    }

    try {
      setIsListening(true);
      setReceivedOtp(null);
      setLastError(null);

      const result = await otpAuth.requestOtp(handleOtpReceived, handleOtpError);
      
      if (result.success) {
        Alert.alert(
          '握手成功',
          `${result.message}\n\n现在请通过WhatsApp Business API发送验证码模板消息到当前设备的WhatsApp号码。`
        );
      } else {
        setIsListening(false);
        Alert.alert('请求失败', result.message);
      }
    } catch (error) {
      setIsListening(false);
      console.error('请求OTP失败:', error);
      Alert.alert('错误', '请求OTP失败');
    }
  }, [otpAuth, handleOtpReceived, handleOtpError]);

  // 停止监听
  const stopListening = useCallback(() => {
    otpAuth.stopListening();
    setIsListening(false);
  }, [otpAuth]);

  // 复制签名信息
  const copySignatureInfo = useCallback(() => {
    if (signatureInfo) {
      const info = `Package Name: ${signatureInfo.packageName}\nSignature Hash: ${signatureInfo.signatureHash}`;
      // 这里可以使用Clipboard API复制到剪贴板
      Alert.alert(
        '应用签名信息',
        `${info}\n\n请将此信息用于配置WhatsApp模板`,
        [{ text: '确定' }]
      );
    }
  }, [signatureInfo]);

  useEffect(() => {
    checkWhatsAppInstallation();
    fetchSignatureInfo();
    fetchDeviceInfo();
    
    // 组件卸载时停止监听
    return () => {
      otpAuth.stopListening();
    };
  }, [checkWhatsAppInstallation, fetchSignatureInfo, fetchDeviceInfo, otpAuth]);

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>WhatsApp零点击认证测试</Text>
        <Text style={styles.subtitle}>
          {Platform.OS === 'android' ? '支持的平台' : '不支持的平台'}
        </Text>
      </View>

      {/* WhatsApp安装状态 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>WhatsApp安装状态</Text>
        <Text style={styles.info}>
          已安装: {isWhatsAppInstalled_ === null ? '检查中...' : isWhatsAppInstalled_ ? '是' : '否'}
        </Text>
        {installedApps.length > 0 && (
          <Text style={styles.info}>
            已安装应用: {installedApps.join(', ')}
          </Text>
        )}
        <TouchableOpacity style={styles.button} onPress={checkWhatsAppInstallation}>
          <Text style={styles.buttonText}>重新检查</Text>
        </TouchableOpacity>
      </View>

      {/* 应用签名信息 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>应用签名信息</Text>
        {signatureInfo ? (
          <View>
            <Text style={styles.info}>包名: {signatureInfo.packageName}</Text>
            <Text style={styles.info}>签名哈希: {signatureInfo.signatureHash}</Text>
            <TouchableOpacity style={styles.button} onPress={copySignatureInfo}>
              <Text style={styles.buttonText}>查看配置信息</Text>
            </TouchableOpacity>
          </View>
        ) : (
          <Text style={styles.info}>获取中...</Text>
        )}
      </View>

      {/* 设备信息 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>设备信息</Text>
        {deviceInfo_ ? (
          <View>
            <Text style={styles.info}>SDK版本: {deviceInfo_.sdkVersion}</Text>
            <Text style={styles.info}>设备型号: {deviceInfo_.deviceModel}</Text>
            <Text style={styles.info}>制造商: {deviceInfo_.manufacturer}</Text>
          </View>
        ) : (
          <Text style={styles.info}>获取中...</Text>
        )}
      </View>

      {/* OTP测试 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>验证码测试</Text>
        
        {Platform.OS === 'android' ? (
          <View>
            <Text style={styles.info}>
              状态: {isListening ? '等待验证码...' : '未监听'}
            </Text>
            
            {receivedOtp && (
              <Text style={[styles.info, styles.success]}>
                收到验证码: {receivedOtp}
              </Text>
            )}
            
            {lastError && (
              <Text style={[styles.info, styles.error]}>
                错误: {lastError}
              </Text>
            )}
            
            <View style={styles.buttonGroup}>
              <TouchableOpacity
                style={[styles.button, isListening && styles.buttonDisabled]}
                onPress={requestOtp}
                disabled={isListening}
              >
                <Text style={styles.buttonText}>
                  {isListening ? '监听中...' : '请求验证码'}
                </Text>
              </TouchableOpacity>
              
              {isListening && (
                <TouchableOpacity style={styles.button} onPress={stopListening}>
                  <Text style={styles.buttonText}>停止监听</Text>
                </TouchableOpacity>
              )}
            </View>
            
            <Text style={styles.instructions}>
              使用步骤：{'\n'}
              1. 点击"请求验证码"按钮{'\n'}
              2. 通过WhatsApp Business API发送验证码模板消息{'\n'}
              3. 验证码将自动显示在此应用中
            </Text>
          </View>
        ) : (
          <Text style={styles.error}>
            WhatsApp零点击认证仅在Android平台支持
          </Text>
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#25D366',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: 'white',
    opacity: 0.8,
  },
  section: {
    backgroundColor: 'white',
    margin: 16,
    padding: 16,
    borderRadius: 8,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
    color: '#333',
  },
  info: {
    fontSize: 14,
    marginBottom: 8,
    color: '#666',
  },
  success: {
    color: '#4CAF50',
    fontWeight: 'bold',
  },
  error: {
    color: '#F44336',
    fontWeight: 'bold',
  },
  button: {
    backgroundColor: '#25D366',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 6,
    marginTop: 8,
  },
  buttonDisabled: {
    backgroundColor: '#ccc',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    textAlign: 'center',
  },
  buttonGroup: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8,
  },
  instructions: {
    fontSize: 12,
    color: '#888',
    marginTop: 16,
    lineHeight: 18,
  },
});
