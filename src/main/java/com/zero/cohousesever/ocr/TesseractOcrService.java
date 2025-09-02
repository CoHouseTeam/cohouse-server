package com.zero.cohousesever.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TesseractOcrService {
    private final Tesseract tesseract;

    public TesseractOcrService() {
        this.tesseract = new Tesseract();
        // local 테스트 경로
//        tesseract.setDatapath("/opt/homebrew/Cellar/tesseract/5.5.1/share/tessdata/");
        tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata/");
        tesseract.setLanguage("kor");
    }

    /**
     * MultipartFile에서 금액 추출
     */
    public Long extractAmountFromReceipt(MultipartFile file) throws IOException, TesseractException {
        // MultipartFile을 BufferedImage로 직접 변환
        BufferedImage image = ImageIO.read(file.getInputStream());

        // OCR 수행 (디스크 I/O 없음)
        String ocrResult = tesseract.doOCR(image);

        // 금액 추출
        return parseAmountFromOcrText(ocrResult);
    }

    /**
     * OCR 텍스트에서 금액 파싱
     */
    private Long parseAmountFromOcrText(String ocrText) {
        // 정규식을 사용해서 금액 패턴 찾기
        // 예: "총액: 15,000원", "합계 12000원", "총 금액 : 25,000" 등
        Pattern[] patterns = {
                // 1. 총액 관련 (총액, 총 액, 총금액, 총 금액 등)
                Pattern.compile("총[\\s]*금[\\s]*액[\\s]*:?[\\s]*([0-9,]+)"),

                // 2. 합계 관련 (합계, 합 계, 합계금액, 합 계 금 액 등)
                Pattern.compile("합[\\s]*계[\\s]*금[\\s]*액[\\s]*:?[\\s]*([0-9,]+)"),

                // 3. 결제 관련 (결제금액, 결 제 금 액, 결제액 등)
                Pattern.compile("결[\\s]*제[\\s]*금[\\s]*액[\\s]*:?[\\s]*([0-9,]+)"),

                // 4. 현금 관련 (현금, 현금금액, 현 금 금 액 등)
                Pattern.compile("현[\\s]*금[\\s]*금[\\s]*액[\\s]*:?[\\s]*([0-9,]+)"),

                // 5. 받을금액, 받을 금액
                Pattern.compile("받[\\s]*을[\\s]*금[\\s]*액[\\s]*:?[\\s]*([0-9,]+)"),

                // 6. 지불금액, 지불 금액
                Pattern.compile("지[\\s]*불[\\s]*금[\\s]*액[\\s]*:?[\\s]*([0-9,]+)"),

                // 7. 청구금액, 청구 금액
                Pattern.compile("청[\\s]*구[\\s]*금[\\s]*액[\\s]*:?[\\s]*([0-9,]+)"),

                // 8. 카드결제, 카드 결제
                Pattern.compile("카[\\s]*드[\\s]*결[\\s]*제[\\s]*:?[\\s]*([0-9,]+)"),

                // 9. 일반적인 금액 표현 (금액:, 금액 :, 금 액: 등)
                Pattern.compile("금[\\s]*액[\\s]*:?[\\s]*([0-9,]+)"),

                // 10. 숫자 + 원 (가장 일반적이지만 우선순위는 낮음)
                Pattern.compile("([0-9,]+)[\\s]*원"),
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(ocrText);
            if (matcher.find()) {
                String amountStr = matcher.group(1).replaceAll(",", "");
                try {
                    return Long.parseLong(amountStr);
                } catch (NumberFormatException e) {
                    continue; // 다음 패턴 시도
                }
            }
        }

        // 금액을 찾지 못한 경우 null 반환
        return null;
    }
}
