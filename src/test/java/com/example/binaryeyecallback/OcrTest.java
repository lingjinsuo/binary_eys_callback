package com.example.binaryeyecallback;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * OCR测试用例 - 识别硬盘与内存条图片内容
 */
public class OcrTest {

    private static final String[] IMAGE_NAMES = {"1.JPG", "2.JPG", "33.JPG", "44.JPG"};
    
    // Homebrew tesseract library path on macOS
    private static final String TESSERACT_LIB_PATH = "/opt/homebrew/lib";
    // Homebrew tessdata path on macOS
    private static final String TESSDATA_PATH = "/opt/homebrew/share/tessdata";
    
    // 白色阈值 (0-255)
    private static final int WHITE_THRESHOLD = 200;

    @BeforeAll
    public static void setup() {
        // 设置Tesseract原生库路径
        System.setProperty("jna.library.path", TESSERACT_LIB_PATH);
        
        // 确保DYLD_LIBRARY_PATH包含tesseract库路径
        String existingPath = System.getenv("DYLD_LIBRARY_PATH");
        String newPath = TESSERACT_LIB_PATH;
        if (existingPath != null && !existingPath.isEmpty()) {
            newPath = TESSERACT_LIB_PATH + ":" + existingPath;
        }
        System.setProperty("DYLD_LIBRARY_PATH", newPath);
    }

    /**
     * 检测白色区域的边界
     */
    private int[] findWhiteRegionBounds(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        int minX = width, maxX = 0, minY = height, maxY = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                // 检查是否是白色区域 (RGB值都高于阈值)
                if (isWhite(color)) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        
        return new int[]{minX, minY, maxX, maxY};
    }
    
    /**
     * 检查像素是否为白色
     */
    private boolean isWhite(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return r >= WHITE_THRESHOLD && g >= WHITE_THRESHOLD && b >= WHITE_THRESHOLD;
    }
    
    /**
     * 裁剪并识别白色区域
     */
    private String recognizeWhiteRegions(BufferedImage image, Tesseract tesseract) throws TesseractException {
        StringBuilder results = new StringBuilder();
        int width = image.getWidth();
        int height = image.getHeight();
        
        // 按行扫描，识别每行的白色区域
        int minRowHeight = 10; // 最小行高，避免噪声
        
        int y = 0;
        while (y < height) {
            // 找到白色区域的起始行
            int startY = -1;
            int whitePixelCount = 0;
            
            for (; y < height; y++) {
                int rowWhiteCount = 0;
                for (int x = 0; x < width; x++) {
                    if (isWhite(new Color(image.getRGB(x, y)))) {
                        rowWhiteCount++;
                    }
                }
                // 如果这一行有超过10%的白色像素，认为是内容行
                if (rowWhiteCount > width * 0.1) {
                    if (startY == -1) {
                        startY = y;
                    }
                    whitePixelCount++;
                } else if (startY != -1) {
                    break;
                }
            }
            
            // 如果找到足够的白色像素，识别这一行
            if (startY != -1 && whitePixelCount >= minRowHeight) {
                // 扩展边界
                int endY = Math.min(y, height - 1);
                
                // 找到左右边界
                int startX = width, endX = 0;
                for (int row = startY; row <= endY; row++) {
                    for (int x = 0; x < width; x++) {
                        if (isWhite(new Color(image.getRGB(x, row)))) {
                            startX = Math.min(startX, x);
                            endX = Math.max(endX, x);
                        }
                    }
                }
                
                // 添加边距
                startX = Math.max(0, startX - 5);
                endX = Math.min(width - 1, endX + 5);
                startY = Math.max(0, startY - 2);
                endY = Math.min(height - 1, endY + 2);
                
                if (endX > startX && endY > startY) {
                    BufferedImage rowImage = image.getSubimage(startX, startY, endX - startX + 1, endY - startY + 1);
                    String text = tesseract.doOCR(rowImage).trim();
                    if (!text.isEmpty()) {
                        results.append(text).append("\n");
                    }
                }
            }
            y++;
        }
        
        return results.toString().trim();
    }

    @Test
    public void testOcrImages() throws IOException, TesseractException {
        // 获取图片资源路径
        ClassLoader classLoader = getClass().getClassLoader();
        List<String> results = new ArrayList<>();

        System.out.println("========== OCR 测试开始 ==========");
        System.out.println("开始识别图片...\n");

        for (int i = 0; i < IMAGE_NAMES.length; i++) {
            String imageName = IMAGE_NAMES[i];
            File imageFile = new File(classLoader.getResource("images/" + imageName).getFile());
            
            System.out.println("正在处理: " + imageName);
            
            // 读取图片
            BufferedImage image = ImageIO.read(imageFile);
            System.out.println("图片尺寸: " + image.getWidth() + "x" + image.getHeight());
            
            // 创建Tesseract实例
            Tesseract tesseract = new Tesseract();
            // 设置训练数据路径
            tesseract.setDatapath(TESSDATA_PATH);
            
            // 设置语言为英文（默认已安装）
            tesseract.setLanguage("eng");
            
            // 只识别白色区域
            String result = recognizeWhiteRegions(image, tesseract);
            
            results.add(result);
            
            System.out.println("识别结果 " + (i + 1) + ": " + result);
            System.out.println("---");
        }

        System.out.println("\n========== OCR 测试完成 ==========");
        System.out.println("共处理 " + results.size() + " 张图片");
    }

    @Test
    public void testOcrSingleImage() throws IOException, TesseractException {
        // 测试单张图片
        ClassLoader classLoader = getClass().getClassLoader();
        File imageFile = new File(classLoader.getResource("images/1.JPG").getFile());

        System.out.println("========== 单张图片OCR测试 ==========");
        System.out.println("图片路径: " + imageFile.getAbsolutePath());

        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage("eng+chi_sim");

        String result = tesseract.doOCR(imageFile);
        System.out.println("识别结果:\n" + result);
        System.out.println("========== 测试完成 ==========");
    }

    @Test
    public void testOcrWithDetailedResult() throws IOException, TesseractException {
        // 测试并获取详细结果
        ClassLoader classLoader = getClass().getClassLoader();

        System.out.println("========== 详细OCR测试 ==========\n");

        for (String imageName : IMAGE_NAMES) {
            File imageFile = new File(classLoader.getResource("images/" + imageName).getFile());
            
            System.out.println("图片: " + imageName);
            System.out.println("文件存在: " + imageFile.exists());
            
            if (imageFile.exists()) {
                BufferedImage image = ImageIO.read(imageFile);
                System.out.println("图片尺寸: " + image.getWidth() + "x" + image.getHeight());
                
                Tesseract tesseract = new Tesseract();
                tesseract.setLanguage("eng+chi_sim");
                
                String result = tesseract.doOCR(imageFile);
                System.out.println("识别内容: " + result.trim());
            }
            System.out.println("----------------------------\n");
        }
    }
}
