package com.elearning.thanhToan.haTang;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public final class VnpayCongCu {

    private VnpayCongCu() {
    }

    public static String taoChuKy(Map<String, String> params, String secret) {
        try {
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            StringBuilder data = new StringBuilder();
            for (String fieldName : fieldNames) {
                String value = params.get(fieldName);
                // VNPAY không lấy các giá trị null hoặc rỗng
                if (value != null && !value.isBlank()) {
                    if (data.length() > 0) {
                        data.append('&');
                    }
                    // BẮT BUỘC PHẢI ENCODE Ở ĐÂY THEO CHUẨN VNPAY
                    data.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                }
            }
            return hmacSha512Hex(data.toString(), secret);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tạo chữ ký VNPay", e);
        }
    }

    /**
     * Chữ ký cho VNPay Merchant API (querydr/refund) — KHÁC định dạng với taoChuKy
     * (dùng cho URL thanh toán): đây là chuỗi các giá trị nối bằng "|" theo ĐÚNG
     * thứ tự cố định do VNPay quy định, không sắp xếp theo tên trường và không
     * URL-encode từng giá trị. Gọi taoChuoiDuLieuGiaoDich() để ghép chuỗi trước.
     */
    public static String taoChuKyGiaoDich(String duLieuDaGhep, String secret) {
        return hmacSha512Hex(duLieuDaGhep, secret);
    }

    private static String hmacSha512Hex(String data, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tạo chữ ký VNPay", e);
        }
    }

    public static boolean xacThucChuKy(Map<String, String> params, String secret, String secureHash) {
        Map<String, String> copy = new HashMap<>(params);
        copy.remove("vnp_SecureHash");
        copy.remove("vnp_SecureHashType");
        String hash = taoChuKy(copy, secret);
        return MessageDigest.isEqual(hash.getBytes(StandardCharsets.US_ASCII),
                secureHash.getBytes(StandardCharsets.US_ASCII));
    }

    public static String layDiaChiIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public static String dinhDangNgay(LocalDateTime ngay) {
        return ngay.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static String dinhDangNgay(java.util.Date ngay) {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(ngay);
    }
}