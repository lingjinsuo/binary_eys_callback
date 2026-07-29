package com.example.binaryeyecallback.service;

import com.example.binaryeyecallback.model.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 扫描结果服务
 */
@Service
public class ScanResultService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanResultService.class);
    
    /**
     * 存储最近100条扫描记录
     */
    private final ConcurrentLinkedQueue<ScanResult> recentScans = new ConcurrentLinkedQueue<>();
    private static final int MAX_RECENT_SCANS = 100;
    
    @PostConstruct
    public void init() {
        logger.info("ScanResultService initialized");
    }
    
    /**
     * 保存扫描结果
     */
    public void saveScanResult(ScanResult scanResult) {
        if (scanResult == null || scanResult.getContent() == null) {
            logger.warn("Invalid scan result received");
            return;
        }
        
        // 设置时间戳
        if (scanResult.getTimestamp() == null) {
            scanResult.setTimestamp(System.currentTimeMillis());
        }
        
        // 添加到队列
        recentScans.offer(scanResult);
        
        // 保持队列大小
        while (recentScans.size() > MAX_RECENT_SCANS) {
            recentScans.poll();
        }
        
        logger.info("Received scan result: {} (format: {})", 
                    scanResult.getContent(), 
                    scanResult.getFormat());
        
        // 这里可以添加更多处理逻辑，如：
        // - 保存到数据库
        // - 发送通知
        // - 调用其他服务
    }
    
    /**
     * 获取最近的所有扫描结果
     */
    public List<ScanResult> getRecentScans() {
        return new ArrayList<>(recentScans);
    }
    
    /**
     * 获取最近的N条扫描结果
     */
    public List<ScanResult> getRecentScans(int limit) {
        List<ScanResult> result = new ArrayList<>();
        int count = 0;
        for (ScanResult scan : recentScans) {
            if (count >= limit) break;
            result.add(scan);
            count++;
        }
        return result;
    }
    
    /**
     * 清空历史记录
     */
    public void clearHistory() {
        recentScans.clear();
        logger.info("Scan history cleared");
    }
    
    /**
     * 获取总扫描数量
     */
    public int getTotalScans() {
        return recentScans.size();
    }
}
