package com.example.binaryeyecallback.service;

import com.example.binaryeyecallback.model.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 蓝牙服务 - 接收 BinaryEye 通过蓝牙发送的扫描数据
 * 
 * BinaryEye 使用 RFCOMM 协议和 UUID: 8a8478c9-2ca8-404b-a0de-101f34ab71ae
 */
@Service
public class BluetoothService {
    
    private static final Logger logger = LoggerFactory.getLogger(BluetoothService.class);
    
    /**
     * BinaryEye 使用的固定 UUID
     */
    private static final String BINARY_EYE_UUID = "8a8478c9-2ca8-404b-a0de-101f34ab71ae";
    
    @Value("${bluetooth.enabled:false}")
    private boolean bluetoothEnabled;
    
    @Value("${bluetooth.server-name:BinaryEye-Receiver}")
    private String serverName;
    
    @Autowired
    private ScanResultService scanResultService;
    
    private ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private Object bluetoothServerSocket = null;
    
    @PostConstruct
    public void init() {
        if (!bluetoothEnabled) {
            logger.info("Bluetooth service is disabled in configuration");
            return;
        }
        
        logger.info("Initializing Bluetooth service...");
        
        // 注意: BlueCove 在 macOS 上可能不工作，这是平台限制
        // 在 Windows/Linux 上可以正常使用
        startBluetoothServer();
    }
    
    /**
     * 启动蓝牙服务器
     * 注意: BlueCove 库在 macOS 上有兼容性问题
     */
    private void startBluetoothServer() {
        executorService = Executors.newSingleThreadExecutor();
        
        executorService.submit(() -> {
            try {
                logger.info("Bluetooth server starting...");
                running.set(true);
                
                // 由于 BlueCove 在 macOS 上的限制，这里提供占位实现
                // 在 Windows/Linux 环境下，需要使用真实的 BlueCove API
                
                // 示例代码 (需要 BlueCove 库支持):
                /*
                LocalDevice localDevice = LocalDevice.getLocalDevice();
                System.out.println("Bluetooth device: " + localDevice.getFriendlyName());
                
                // 创建服务注册
                UUID serviceUUID = UUID.fromString(BINARY_EYE_UUID);
                Vector<String> browseableUUIDs = new Vector<>();
                browseableUUIDs.add(serviceUUID.toString());
                
                // 监听连接
                while (running.get()) {
                    try {
                        // 接受客户端连接
                        StreamConnectionNotifier notifier = (StreamConnectionNotifier) 
                            Connector.open("btspp://localhost:" + serviceUUID + 
                                          ";name=" + serverName + 
                                          ";authenticate=false;encrypt=false");
                        
                        StreamConnection connection = notifier.acceptAndOpen();
                        
                        // 处理连接
                        handleConnection(connection);
                        
                        notifier.close();
                    } catch (Exception e) {
                        if (running.get()) {
                            logger.error("Error accepting Bluetooth connection", e);
                        }
                    }
                }
                */
                
                logger.warn("BlueCove Bluetooth server requires native Bluetooth support. " +
                           "This feature is available on Windows/Linux platforms.");
                
            } catch (Exception e) {
                logger.error("Bluetooth server error", e);
            }
        });
    }
    
    /**
     * 处理蓝牙连接
     */
    private void handleConnection(Object connection) {
        try {
            // 这里需要根据 BlueCove API 实现
            // StreamConnection connection = (StreamConnection) conn;
            // InputStreamReader reader = new InputStreamReader(connection.openInputStream());
            
            BufferedReader reader = null;
            String line;
            
            logger.info("New Bluetooth connection established");
            
            while ((line = reader.readLine()) != null) {
                logger.info("Received via Bluetooth: {}", line);
                
                // 创建扫描结果对象
                ScanResult scanResult = new ScanResult();
                scanResult.setContent(line.trim());
                scanResult.setTimestamp(System.currentTimeMillis());
                scanResult.setDeviceName(serverName);
                
                // 保存结果
                scanResultService.saveScanResult(scanResult);
            }
            
        } catch (Exception e) {
            logger.error("Error handling Bluetooth connection", e);
        }
    }
    
    /**
     * 发送响应给客户端 (可选功能)
     */
    public void sendResponse(Object connection, String message) {
        try {
            // OutputStreamWriter writer = new OutputStreamWriter(connection.openOutputStream());
            // writer.write(message);
            // writer.flush();
            logger.info("Sending response via Bluetooth: {}", message);
        } catch (Exception e) {
            logger.error("Error sending Bluetooth response", e);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Bluetooth service...");
        running.set(false);
        
        if (executorService != null) {
            executorService.shutdown();
        }
        
        try {
            if (bluetoothServerSocket != null) {
                // 关闭蓝牙服务器套接字
                // bluetoothServerSocket.close();
            }
        } catch (Exception e) {
            logger.error("Error closing Bluetooth server", e);
        }
        
        logger.info("Bluetooth service stopped");
    }
    
    /**
     * 检查蓝牙服务是否运行
     */
    public boolean isRunning() {
        return running.get();
    }
}
