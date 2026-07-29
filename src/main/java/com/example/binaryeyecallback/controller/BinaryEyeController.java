package com.example.binaryeyecallback.controller;

import com.example.binaryeyecallback.model.ScanResult;
import com.example.binaryeyecallback.service.ScanResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BinaryEye 数据接收 Controller
 * 
 * 支持多种数据接收方式：
 * 1. HTTP POST - 接收 JSON 格式的扫描数据
 * 2. HTTP GET - 查询历史扫描记录
 * 
 * 使用说明：
 * - POST /api/scan - 接收扫描数据
 * - GET /api/scans - 获取所有历史记录
 * - GET /api/scans?limit=10 - 获取最近10条记录
 * - DELETE /api/scans - 清空历史记录
 */
@RestController
@RequestMapping("/api")
public class BinaryEyeController {
    
    private static final Logger logger = LoggerFactory.getLogger(BinaryEyeController.class);
    
    @Autowired
    private ScanResultService scanResultService;
    
    /**
     * 接收扫描数据 (JSON格式或Query参数格式)
     * 
     * 支持 BinaryEye Deep Link 回调格式：
     * GET /api/scan?content=xxx&format=xxx
     * 
     * @param content 扫描内容
     * @param format 条码格式
     * @param hexBytes 原始字节的十六进制字符串
     * @param scanResult JSON格式的扫描结果
     * @return 响应信息
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> receiveScan(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String hexBytes,
            @RequestBody(required = false) ScanResult scanResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ScanResult result;
            
            // 优先使用 Query 参数
            if (content != null) {
                result = new ScanResult();
                result.setContent(content);
                result.setFormat(format);
                result.setHexBytes(hexBytes);
                result.setTimestamp(System.currentTimeMillis());
                logger.info("Received scan via Query: content={}, format={}", content, format);
            } else if (scanResult != null) {
                result = scanResult;
                logger.info("Received scan via HTTP: {}", scanResult);
            } else {
                response.put("success", false);
                response.put("message", "No scan data provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            scanResultService.saveScanResult(result);
            
            response.put("success", true);
            response.put("message", "Scan data received successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing scan data", e);
            
            response.put("success", false);
            response.put("message", "Error processing scan data: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 接收扫描数据 (GET请求，支持Deep Link)
     * 
     * 格式: GET /?content=xxx
     * 
     * @param content 扫描内容
     * @param format 条码格式 (可选)
     * @param result 结果 (别名，用于兼容不同格式)
     * @return 响应信息
     */
    @GetMapping("/scan")
    public ResponseEntity<Map<String, Object>> receiveScanGet(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String result) {
        
        // 兼容：如果 content 为空但 result 有值，使用 result
        String scanContent = content != null ? content : result;
        
        if (scanContent == null || scanContent.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "No content provided");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        logger.info("Received scan via GET: content={}, format={}", scanContent, format);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ScanResult scanResult = new ScanResult();
            scanResult.setContent(scanContent);
            scanResult.setFormat(format);
            scanResult.setTimestamp(System.currentTimeMillis());
            
            scanResultService.saveScanResult(scanResult);
            
            response.put("success", true);
            response.put("message", "Scan data received successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing scan data", e);
            
            response.put("success", false);
            response.put("message", "Error processing scan data: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 接收扫描数据 (根路径，支持 Deep Link 格式)
     * 
     * 格式: GET /?content=xxx
     * 
     * @param content 扫描内容
     * @param result 结果 (别名)
     * @return 响应信息
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> receiveScanRoot(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String result) {
        
        String scanContent = content != null ? content : result;
        
        if (scanContent == null || scanContent.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "No content provided");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        logger.info("Received scan via root path: content={}", scanContent);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ScanResult scanResult = new ScanResult();
            scanResult.setContent(scanContent);
            scanResult.setTimestamp(System.currentTimeMillis());
            
            scanResultService.saveScanResult(scanResult);
            
            response.put("success", true);
            response.put("message", "Scan data received successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing scan data", e);
            
            response.put("success", false);
            response.put("message", "Error processing scan data: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 接收扫描数据 (简单文本格式)
     * 
     * @param content 扫描内容
     * @param format 条码格式 (可选)
     * @param hexBytes 原始字节的十六进制字符串 (可选)
     * @return 响应信息
     */
    @PostMapping("/scan/text")
    public ResponseEntity<Map<String, Object>> receiveScanText(
            @RequestParam String content,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String hexBytes) {
        
        logger.info("Received scan via HTTP (text): content={}, format={}", content, format);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ScanResult scanResult = new ScanResult();
            scanResult.setContent(content);
            scanResult.setFormat(format);
            scanResult.setHexBytes(hexBytes);
            scanResult.setTimestamp(System.currentTimeMillis());
            
            scanResultService.saveScanResult(scanResult);
            
            response.put("success", true);
            response.put("message", "Scan data received successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing scan data", e);
            
            response.put("success", false);
            response.put("message", "Error processing scan data: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取所有扫描历史
     * 
     * @param limit 限制返回数量 (可选，默认返回所有)
     * @return 扫描结果列表
     */
    @GetMapping("/scans")
    public ResponseEntity<Map<String, Object>> getScans(
            @RequestParam(required = false) Integer limit) {
        
        logger.info("Fetching scan history, limit={}", limit);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ScanResult> scans;
            if (limit != null && limit > 0) {
                scans = scanResultService.getRecentScans(limit);
            } else {
                scans = scanResultService.getRecentScans();
            }
            
            response.put("success", true);
            response.put("count", scans.size());
            response.put("scans", scans);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching scan history", e);
            
            response.put("success", false);
            response.put("message", "Error fetching scan history: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取单条扫描详情
     * 
     * @param index 历史记录索引 (0 = 最新)
     * @return 扫描结果
     */
    @GetMapping("/scan/{index}")
    public ResponseEntity<Map<String, Object>> getScanByIndex(@PathVariable int index) {
        logger.info("Fetching scan at index: {}", index);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ScanResult> scans = scanResultService.getRecentScans();
            
            if (index < 0 || index >= scans.size()) {
                response.put("success", false);
                response.put("message", "Index out of bounds");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("success", true);
            response.put("scan", scans.get(index));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching scan by index", e);
            
            response.put("success", false);
            response.put("message", "Error fetching scan: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 清空所有扫描历史
     * 
     * @return 操作结果
     */
    @DeleteMapping("/scans")
    public ResponseEntity<Map<String, Object>> clearScans() {
        logger.info("Clearing all scan history");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            scanResultService.clearHistory();
            
            response.put("success", true);
            response.put("message", "Scan history cleared successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing scan history", e);
            
            response.put("success", false);
            response.put("message", "Error clearing scan history: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取服务器状态信息
     * 
     * @return 状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("success", true);
        response.put("service", "BinaryEye Callback Server");
        response.put("version", "1.0.0");
        response.put("totalScans", scanResultService.getTotalScans());
        response.put("uptime", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
