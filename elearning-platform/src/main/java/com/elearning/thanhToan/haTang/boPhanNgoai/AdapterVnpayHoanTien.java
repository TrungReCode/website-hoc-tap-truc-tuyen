package com.elearning.thanhToan.haTang.boPhanNgoai;

import com.elearning.thanhToan.haTang.ThanhToanProperties;
import com.elearning.thanhToan.haTang.VnpayCongCu;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gọi VNPay Merchant API ("/merchant_webapi/api/transaction", vnp_Command=refund)
 * để thực hiện hoàn tiền thật cho một giao dịch đã thanh toán thành công trước đó.
 *
 * LƯU Ý QUAN TRỌNG — khác hẳn taoChuKy dùng cho URL thanh toán:
 * chữ ký ở đây là chuỗi các giá trị nối bằng "|" theo ĐÚNG thứ tự cố định
 * VNPay quy định, KHÔNG sắp xếp theo tên trường và KHÔNG URL-encode.
 */
@Component
@RequiredArgsConstructor
public class AdapterVnpayHoanTien {

    private static final Logger log = LoggerFactory.getLogger(AdapterVnpayHoanTien.class);
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ThanhToanProperties thanhToanProperties;
    // FIX: trước đây `new RestTemplate()` KHÔNG cấu hình timeout — mặc định
    // của SimpleClientHttpRequestFactory là connectTimeout/readTimeout = -1
    // (VÔ HẠN). Nếu VNPay treo/không phản hồi, request có thể chờ vô thời
    // hạn, trong khi hàm gọi (TaiChinhDichVuImpl.xuLyHoanTienKhoaHoc/LopHoc)
    // đang chạy trong 1 @Transactional GHI thật — giữ luôn connection CSDL
    // suốt thời gian đó. Nhiều admin duyệt hoàn tiền cùng lúc khi VNPay
    // chậm/treo có thể làm cạn kiệt hẳn connection pool CSDL, kéo sập toàn
    // bộ ứng dụng chứ không chỉ riêng tính năng hoàn tiền.
    // 10s connect / 15s read là mức hợp lý cho API cổng thanh toán — đủ
    // chờ trong điều kiện mạng bình thường, không để admin chờ vô hạn.
    private final RestTemplate restTemplate = new RestTemplate(taoRequestFactoryCoTimeout());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static SimpleClientHttpRequestFactory taoRequestFactoryCoTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(15_000);
        return factory;
    }

    /**
     * @param maGiaoDich       vnp_TxnRef gốc (mã giao dịch nội bộ, vd "KH-...")
     * @param soTien           số tiền cần hoàn (VNĐ, chưa nhân 100)
     * @param vnpTransactionNo mã giao dịch phía VNPay của lần thanh toán gốc (lưu trong duLieuDong)
     * @param vnpPayDate       thời điểm thanh toán gốc theo định dạng VNPay trả về (yyyyMMddHHmmss)
     * @param nguoiThucHien    tên/email admin thực hiện thao tác (vnp_CreateBy)
     * @param lyDo             lý do hoàn tiền (vnp_OrderInfo)
     * @param hoanToanBo       true = hoàn toàn bộ (vnp_TransactionType=02), false = hoàn một phần (03)
     */
    public KetQuaHoanTienVnpay hoanTien(String maGiaoDich, BigDecimal soTien, String vnpTransactionNo,
                                        String vnpPayDate, String nguoiThucHien, String lyDo, boolean hoanToanBo) {
        if (vnpTransactionNo == null || vnpPayDate == null) {
            return new KetQuaHoanTienVnpay(false, null,
                    "Thiếu dữ liệu giao dịch gốc (vnp_TransactionNo/vnp_PayDate) — có thể giao dịch được tạo trước khi hệ thống lưu 2 trường này. Vui lòng hoàn tiền thủ công qua cổng quản trị VNPay.");
        }

        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        String createDate = LocalDateTime.now().format(DINH_DANG_NGAY);
        String amountVnpay = soTien.multiply(BigDecimal.valueOf(100)).toBigInteger().toString();
        String transactionType = hoanToanBo ? "02" : "03";
        String ipAddr = "127.0.0.1"; // gọi server-to-server, không có IP trình duyệt người dùng

        String duLieuKy = String.join("|",
                requestId, "2.1.0", "refund", thanhToanProperties.getTmnCode(), transactionType,
                maGiaoDich, amountVnpay, vnpTransactionNo, vnpPayDate, nguoiThucHien, createDate, ipAddr, lyDo);
        String chuKy = VnpayCongCu.taoChuKyGiaoDich(duLieuKy, thanhToanProperties.getHashSecret());

        Map<String, String> body = new LinkedHashMap<>();
        body.put("vnp_RequestId", requestId);
        body.put("vnp_Version", "2.1.0");
        body.put("vnp_Command", "refund");
        body.put("vnp_TmnCode", thanhToanProperties.getTmnCode());
        body.put("vnp_TransactionType", transactionType);
        body.put("vnp_TxnRef", maGiaoDich);
        body.put("vnp_Amount", amountVnpay);
        body.put("vnp_TransactionNo", vnpTransactionNo);
        body.put("vnp_CreateBy", nguoiThucHien);
        body.put("vnp_OrderInfo", lyDo);
        body.put("vnp_TransactionDate", vnpPayDate);
        body.put("vnp_CreateDate", createDate);
        body.put("vnp_IpAddr", ipAddr);
        body.put("vnp_SecureHash", chuKy);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    thanhToanProperties.getRefundApiUrl(), new HttpEntity<>(body, headers), String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            String maPhanHoi = json.path("vnp_ResponseCode").asText(null);
            String thongDiep = json.path("vnp_Message").asText("Không rõ phản hồi từ VNPay");

            boolean thanhCong = "00".equals(maPhanHoi);
            if (!thanhCong) {
                log.warn("VNPay từ chối yêu cầu hoàn tiền cho giao dịch {}: mã {} - {}", maGiaoDich, maPhanHoi, thongDiep);
            }
            return new KetQuaHoanTienVnpay(thanhCong, maPhanHoi, thongDiep);
        } catch (ResourceAccessException ex) {
            // Bao gồm cả timeout (SocketTimeoutException) lẫn lỗi kết nối
            // (ConnectException, UnknownHostException...) — RestTemplate bọc
            // chung vào ResourceAccessException. Phân biệt rõ bằng cause để
            // admin biết chính xác điều gì đã xảy ra thay vì thông báo chung
            // chung "không thể kết nối".
            boolean laTimeout = ex.getCause() instanceof java.net.SocketTimeoutException;
            String lyDoLoi = laTimeout
                    ? "VNPay không phản hồi kịp trong thời gian cho phép (timeout). "
                            + "Giao dịch CÓ THỂ đã được VNPay xử lý phía họ dù ta chưa nhận được phản hồi — "
                            + "vui lòng kiểm tra lại trên cổng quản trị VNPay trước khi thử lại, tránh hoàn tiền trùng."
                    : "Không thể kết nối tới VNPay: " + ex.getMessage();
            log.error("Gọi API hoàn tiền VNPay {} cho giao dịch {}", laTimeout ? "hết thời gian chờ" : "lỗi kết nối",
                    maGiaoDich, ex);
            return new KetQuaHoanTienVnpay(false, null, lyDoLoi);
        } catch (Exception ex) {
            log.error("Gọi API hoàn tiền VNPay thất bại cho giao dịch {}", maGiaoDich, ex);
            return new KetQuaHoanTienVnpay(false, null, "Không thể kết nối tới VNPay: " + ex.getMessage());
        }
    }
}