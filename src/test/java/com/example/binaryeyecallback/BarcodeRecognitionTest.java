package com.example.binaryeyecallback;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 条形码与二维码识别测试用例
 * 使用ZXing库识别图片中的所有条形码和二维码
 */
public class BarcodeRecognitionTest {

    private static final String[] IMAGE_NAMES = {"1.JPG", "2.JPG", "33.JPG", "44.JPG"};
    
    // Homebrew tesseract library path on macOS
    private static final String TESSERACT_LIB_PATH = "/opt/homebrew/lib";
    
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
     * 从图片中识别所有条形码和二维码（支持多条码）
     */
    private List<Result> recognizeAllBarcodes(BufferedImage image) throws NotFoundException {
        List<Result> results = new ArrayList<>();
        
        // 将BufferedImage转换为LuminanceSource
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        // 定义多种条码格式
        Map<DecodeHintType, Object> hints = new HashMap<>();
        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);              // 二维码
        formats.add(BarcodeFormat.CODE_128);             // Code 128
        formats.add(BarcodeFormat.CODE_39);              // Code 39
        formats.add(BarcodeFormat.CODE_93);              // Code 93
        formats.add(BarcodeFormat.EAN_13);               // EAN-13
        formats.add(BarcodeFormat.EAN_8);                // EAN-8
        formats.add(BarcodeFormat.UPC_A);                // UPC-A
        formats.add(BarcodeFormat.UPC_E);                // UPC-E
        formats.add(BarcodeFormat.ITF);                  // ITF
        formats.add(BarcodeFormat.RSS_14);               // RSS 14
        formats.add(BarcodeFormat.DATA_MATRIX);          // Data Matrix
        formats.add(BarcodeFormat.PDF_417);              // PDF 417
        formats.add(BarcodeFormat.AZTEC);                // Aztec
        
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        
        // 使用 GenericMultipleBarcodeReader 识别多个条码
        MultiFormatReader reader = new MultiFormatReader();
        MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
        
        try {
            // 尝试识别多个条码
            Result[] multiResults = multiReader.decodeMultiple(bitmap, hints);
            if (multiResults != null) {
                for (Result result : multiResults) {
                    results.add(result);
                }
            }
        } catch (NotFoundException e) {
            // 没有找到条码，尝试单条码识别
            try {
                Result singleResult = reader.decode(bitmap, hints);
                results.add(singleResult);
            } catch (NotFoundException ex) {
                // 没有找到条码
            }
        }
        
        return results;
    }

    /**
     * 获取条码格式的中文描述
     */
    private String getFormatDescription(BarcodeFormat format) {
        switch (format) {
            case QR_CODE: return "二维码 (QR Code)";
            case CODE_128: return "Code 128";
            case CODE_39: return "Code 39";
            case CODE_93: return "Code 93";
            case EAN_13: return "EAN-13";
            case EAN_8: return "EAN-8";
            case UPC_A: return "UPC-A";
            case UPC_E: return "UPC-E";
            case ITF: return "ITF";
            case DATA_MATRIX: return "Data Matrix";
            case PDF_417: return "PDF 417";
            case AZTEC: return "Aztec";
            default: return format.toString();
        }
    }

    @Test
    public void testRecognizeAllBarcodesInImages() throws IOException, NotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        
        System.out.println("========== 条形码与二维码识别测试 ==========");
        System.out.println("开始识别图片中的条码...\n");
        
        int totalBarcodes = 0;
        
        for (String imageName : IMAGE_NAMES) {
            File imageFile = new File(classLoader.getResource("images/" + imageName).getFile());
            
            System.out.println("----------------------------------------");
            System.out.println("正在处理: " + imageName);
            System.out.println("文件路径: " + imageFile.getAbsolutePath());
            System.out.println("文件存在: " + imageFile.exists());
            
            if (!imageFile.exists()) {
                System.out.println("文件不存在，跳过！\n");
                continue;
            }
            
            // 读取图片
            BufferedImage image = ImageIO.read(imageFile);
            System.out.println("图片尺寸: " + image.getWidth() + "x" + image.getHeight());
            
            // 识别所有条码
            List<Result> results = recognizeAllBarcodes(image);
            
            if (results.isEmpty()) {
                System.out.println("未识别到条形码或二维码");
            } else {
                System.out.println("识别到 " + results.size() + " 个条码/二维码:");
                for (int i = 0; i < results.size(); i++) {
                    Result result = results.get(i);
                    System.out.println("  [" + (i + 1) + "] 类型: " + getFormatDescription(result.getBarcodeFormat()));
                    System.out.println("      内容: " + result.getText());
                    System.out.println("      位置: " + result.getResultMetadata());
                }
                totalBarcodes += results.size();
            }
            System.out.println();
        }
        
        System.out.println("========== 测试完成 ==========");
        System.out.println("共处理 " + IMAGE_NAMES.length + " 张图片");
        System.out.println("共识别到 " + totalBarcodes + " 个条码/二维码");
    }

    @Test
    public void testRecognizeSingleImage() throws IOException, NotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        File imageFile = new File(classLoader.getResource("images/1.JPG").getFile());

        System.out.println("========== 单张图片条码识别测试 ==========");
        System.out.println("图片路径: " + imageFile.getAbsolutePath());

        BufferedImage image = ImageIO.read(imageFile);
        System.out.println("图片尺寸: " + image.getWidth() + "x" + image.getHeight());

        List<Result> results = recognizeAllBarcodes(image);
        
        if (results.isEmpty()) {
            System.out.println("未识别到条形码或二维码");
        } else {
            System.out.println("\n识别结果:");
            for (int i = 0; i < results.size(); i++) {
                Result result = results.get(i);
                System.out.println("  类型: " + getFormatDescription(result.getBarcodeFormat()));
                System.out.println("  内容: " + result.getText());
            }
        }
        
        System.out.println("========== 测试完成 ==========");
    }

    @Test
    public void testRecognizeAllImageDetails() throws IOException, NotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        
        System.out.println("========== 详细条码识别测试 ==========\n");

        int imageIndex = 0;
        for (String imageName : IMAGE_NAMES) {
            imageIndex++;
            File imageFile = new File(classLoader.getResource("images/" + imageName).getFile());
            
            System.out.println("【图片 " + imageIndex + "】 " + imageName);
            
            if (!imageFile.exists()) {
                System.out.println("  ❌ 文件不存在\n");
                continue;
            }
            
            BufferedImage image = ImageIO.read(imageFile);
            System.out.println("  📐 尺寸: " + image.getWidth() + " x " + image.getHeight());
            
            try {
                List<Result> results = recognizeAllBarcodes(image);
                
                if (results.isEmpty()) {
                    System.out.println("  🔍 未识别到条码");
                } else {
                    System.out.println("  ✅ 识别到 " + results.size() + " 个条码:");
                    for (int i = 0; i < results.size(); i++) {
                        Result result = results.get(i);
                        System.out.println("     ──────────────");
                        System.out.println("     条码 " + (i + 1) + ":");
                        System.out.println("       📋 格式: " + getFormatDescription(result.getBarcodeFormat()));
                        System.out.println("       📝 内容: " + result.getText());
                    }
                }
            } catch (Exception e) {
                System.out.println("  ❌ 识别失败: " + e.getMessage());
            }
            
            System.out.println();
        }
        
        System.out.println("========== 测试完成 ==========");
    }
}
