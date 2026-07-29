package com.example.binaryeyecallback.controller;

import com.example.binaryeyecallback.model.ScanResult;
import com.example.binaryeyecallback.service.ScanResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 根路径 Controller - 处理 BinaryEye Deep Link 回调
 * 
 * 接收格式: GET /?content=xxx
 */
@RestController
public class RootController {
    
    private static final Logger logger = LoggerFactory.getLogger(RootController.class);
    
    @Autowired
    private ScanResultService scanResultService;
    
    /**
     * 接收扫描数据 (根路径)
     * 
     * 格式: GET /?content=xxx
     * 
     * @param content 扫描内容
     * @param result 结果 (别名，兼容不同格式)
     * @param format 条码格式 (可选)
     * @return 响应信息
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> receiveScan(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String format) {
        
        // 兼容：如果 content 为空但 result 有值，使用 result
        // 处理 URL 编码的逗号 (逗号在 URL 中编码为 %2C)
        String scanContent = (content != null ? content : result);
        if (scanContent != null && scanContent.startsWith(",")) {
            scanContent = scanContent.substring(1);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        if (scanContent == null || scanContent.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "No content provided");
            response.put("receivedParams", "content=" + content + ", result=" + result + ", format=" + format);
            logger.warn("Received empty scan request. Params: content={}, result={}, format={}", 
                       content, result, format);
            return ResponseEntity.ok(response);
        }
        
        logger.info("Received scan via root path: content={}, format={}", scanContent, format);
        
        try {
            ScanResult scanResult = new ScanResult();
            scanResult.setContent(scanContent);
            scanResult.setFormat(format);
            scanResult.setTimestamp(System.currentTimeMillis());
            
            scanResultService.saveScanResult(scanResult);
            
            response.put("success", true);
            response.put("message", "Scan data received successfully");
            response.put("timestamp", System.currentTimeMillis());
            response.put("content", scanContent);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing scan data", e);
            
            response.put("success", false);
            response.put("message", "Error processing scan data: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 接收扫描数据 (POST 请求)
     * 
     * 格式: POST /?content=xxx
     * 
     * @param content 扫描内容
     * @param result 结果 (别名，兼容不同格式)
     * @param format 条码格式 (可选)
     * @return 响应信息
     */
    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> receiveScanPost(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String format) {
        
        String scanContent = (content != null ? content : result);
        if (scanContent != null && scanContent.startsWith(",")) {
            scanContent = scanContent.substring(1);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        if (scanContent == null || scanContent.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "No content provided");
            response.put("receivedParams", "content=" + content + ", result=" + result + ", format=" + format);
            logger.warn("Received empty POST scan request. Params: content={}, result={}, format={}", 
                       content, result, format);
            return ResponseEntity.ok(response);
        }
        
        logger.info("Received scan via POST: content={}, format={}", scanContent, format);
        
        try {
            ScanResult scanResult = new ScanResult();
            scanResult.setContent(scanContent);
            scanResult.setFormat(format);
            scanResult.setTimestamp(System.currentTimeMillis());
            
            scanResultService.saveScanResult(scanResult);
            
            response.put("success", true);
            response.put("message", "Scan data received successfully");
            response.put("timestamp", System.currentTimeMillis());
            response.put("content", scanContent);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing POST scan data", e);
            
            response.put("success", false);
            response.put("message", "Error processing scan data: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
