package com.example.binaryeyecallback.model;

/**
 * 扫描结果模型
 */
public class ScanResult {
    
    /**
     * 扫描内容
     */
    private String content;
    
    /**
     * 条码格式 (如 QR_CODE, CODE_128 等)
     */
    private String format;
    
    /**
     * 原始字节数据的十六进制字符串
     */
    private String hexBytes;
    
    /**
     * 扫描时间戳
     */
    private Long timestamp;
    
    /**
     * 来源设备名称
     */
    private String deviceName;
    
    /**
     * 设备蓝牙地址
     */
    private String deviceAddress;
    
    public ScanResult() {
    }
    
    public ScanResult(String content, String format, String hexBytes, Long timestamp, String deviceName, String deviceAddress) {
        this.content = content;
        this.format = format;
        this.hexBytes = hexBytes;
        this.timestamp = timestamp;
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getHexBytes() {
        return hexBytes;
    }
    
    public void setHexBytes(String hexBytes) {
        this.hexBytes = hexBytes;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public String getDeviceAddress() {
        return deviceAddress;
    }
    
    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
    
    @Override
    public String toString() {
        return "ScanResult{" +
                "content='" + content + '\'' +
                ", format='" + format + '\'' +
                ", hexBytes='" + hexBytes + '\'' +
                ", timestamp=" + timestamp +
                ", deviceName='" + deviceName + '\'' +
                ", deviceAddress='" + deviceAddress + '\'' +
                '}';
    }
}
