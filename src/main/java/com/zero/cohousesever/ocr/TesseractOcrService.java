package com.zero.cohousesever.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class TesseractOcrService {
    public static void main(String[] args) {
        File imageFile = new File("temp/sample.png");

        if (!imageFile.exists() || !imageFile.isFile()) {
            System.err.println("이미지 파일이 존재하지 않거나 올바른 파일이 아닙니다: " + imageFile.getAbsolutePath());
            return;
        }

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/opt/homebrew/Cellar/tesseract/5.5.1/share/tessdata/");
        tesseract.setLanguage("kor");

        try {
            String result = tesseract.doOCR(imageFile);
            System.out.println(result);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }
}
