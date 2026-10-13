package com.elearning.chungChi.haTang;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/** Tạo QR code từ nội dung văn bản, trả về PNG bytes. */
@Component
public class TaoQrCode {

    private static final int KICH_THUOC = 250; // px — vừa in rõ, không quá nặng

    /**
     * @param noiDung  chuỗi encode vào QR (thường là URL xác thực giấy)
     * @return PNG bytes của ảnh QR
     */
    public byte[] tao(String noiDung) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(noiDung, BarcodeFormat.QR_CODE, KICH_THUOC, KICH_THUOC,
                Map.of(EncodeHintType.MARGIN, 1));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return baos.toByteArray();
    }
}
