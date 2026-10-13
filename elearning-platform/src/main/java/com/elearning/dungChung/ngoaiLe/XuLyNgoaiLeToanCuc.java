package com.elearning.dungChung.ngoaiLe;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bắt toàn bộ exception ném ra từ Controller/Service ở mọi module và
 * chuẩn hóa thành PhanHoiLoi duy nhất. Đây là điểm tập trung duy nhất
 * xử lý lỗi — các module nghiệp vụ KHÔNG cần viết try-catch riêng,
 * chỉ cần ném đúng loại NgoaiLeNghiepVu phù hợp.
 */
@RestControllerAdvice
public class XuLyNgoaiLeToanCuc {

    private static final Logger LOG = LoggerFactory.getLogger(XuLyNgoaiLeToanCuc.class);

    @ExceptionHandler(NgoaiLeNghiepVu.class)
    public ResponseEntity<PhanHoiLoi> xuLyNgoaiLeNghiepVu(NgoaiLeNghiepVu ex, HttpServletRequest req) {
        // Lỗi nghiệp vụ (dữ liệu không hợp lệ, không tìm thấy, không có quyền...)
        // là luồng bình thường của ứng dụng, không phải sự cố — chỉ cần ghi WARN
        // ngắn gọn (không cần stack trace) để có dấu vết khi cần tra cứu.
        LOG.warn("Lỗi nghiệp vụ [{}] tại {}: {}", ex.getMaLoi(), req.getRequestURI(), ex.getMessage());
        PhanHoiLoi loi = PhanHoiLoi.tao(
                ex.getHttpStatus().value(), ex.getMaLoi(), ex.getMessage(), req.getRequestURI(), ex.getDuLieu());
        return ResponseEntity.status(ex.getHttpStatus()).body(loi);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> xuLyLoiValidate(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> chiTietLoi = new LinkedHashMap<>();
        for (FieldError loi : ex.getBindingResult().getFieldErrors()) {
            chiTietLoi.put(loi.getField(), loi.getDefaultMessage());
        }
        LOG.warn("Dữ liệu không hợp lệ tại {}: {}", req.getRequestURI(), chiTietLoi);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("trangThaiHttp", HttpStatus.BAD_REQUEST.value());
        body.put("maLoi", "DU_LIEU_KHONG_HOP_LE");
        body.put("thongBao", "Dữ liệu gửi lên không hợp lệ");
        body.put("chiTiet", chiTietLoi);
        body.put("duongDan", req.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<PhanHoiLoi> xuLySaiThongTinDangNhap(BadCredentialsException ex, HttpServletRequest req) {
        LOG.warn("Đăng nhập thất bại tại {}: sai email hoặc mật khẩu", req.getRequestURI());
        PhanHoiLoi loi = PhanHoiLoi.tao(
                HttpStatus.UNAUTHORIZED.value(), "SAI_THONG_TIN_DANG_NHAP",
                "Email hoặc mật khẩu không đúng", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loi);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<PhanHoiLoi> xuLyTaiKhoanChuaKichHoat(DisabledException ex, HttpServletRequest req) {
        LOG.warn("Đăng nhập thất bại tại {}: tài khoản chưa kích hoạt/đã bị khóa", req.getRequestURI());
        PhanHoiLoi loi = PhanHoiLoi.tao(
                HttpStatus.UNAUTHORIZED.value(), "TAI_KHOAN_CHUA_KICH_HOAT",
                "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email để kích hoạt trước khi đăng nhập.",
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loi);
    }

    /**
     * Tài khoản trạng thái BI_CAM — Spring Security kiểm tra
     * isAccountNonLocked() TRƯỚC isEnabled() (DefaultPreAuthenticationChecks),
     * nên ném LockedException trước khi kịp rơi vào DisabledException.
     * Trước đây thiếu handler riêng, LockedException rơi tuột xuống
     * xuLyLoiKhongXacDinh() ở cuối — trả về lỗi 500 "hệ thống lỗi" chung
     * chung, sai hoàn toàn với thực tế và không nhất quán với thông báo
     * (TAI_KHOAN_BI_KHOA) mà QuanTriDichVuImpl đã gửi lúc admin khóa tài
     * khoản.
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<PhanHoiLoi> xuLyTaiKhoanBiCam(LockedException ex, HttpServletRequest req) {
        LOG.warn("Đăng nhập thất bại tại {}: tài khoản đã bị khóa/cấm", req.getRequestURI());
        PhanHoiLoi loi = PhanHoiLoi.tao(
                HttpStatus.FORBIDDEN.value(), "TAI_KHOAN_BI_KHOA",
                "Tài khoản của bạn đã bị khóa do vi phạm.",
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(loi);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<PhanHoiLoi> xuLyKhongCoQuyenSpring(AccessDeniedException ex, HttpServletRequest req) {
        LOG.warn("Truy cập bị từ chối tại {}: {}", req.getRequestURI(), ex.getMessage());
        PhanHoiLoi loi = PhanHoiLoi.tao(
                HttpStatus.FORBIDDEN.value(), "KHONG_CO_QUYEN",
                "Bạn không có quyền thực hiện hành động này", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(loi);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PhanHoiLoi> xuLyLoiKhongXacDinh(Exception ex, HttpServletRequest req) {
        // Đây là lỗi ngoài dự kiến (bug, lỗi kết nối DB/dịch vụ ngoài, NPE...) —
        // BẮT BUỘC phải log đầy đủ stack trace ở đây, vì đây là "cửa cuối cùng"
        // bắt mọi exception trong hệ thống. Nếu không log, lỗi 500 thật sự sẽ
        // biến mất hoàn toàn khỏi log, không còn cách nào debug khi production
        // gặp sự cố.
        LOG.error("Lỗi hệ thống không xác định tại {}", req.getRequestURI(), ex);
        PhanHoiLoi loi = PhanHoiLoi.tao(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "LOI_HE_THONG",
                "Đã có lỗi xảy ra, vui lòng thử lại sau", req.getRequestURI());
        return ResponseEntity.internalServerError().body(loi);
    }
}