package com.elearning.dinhDanhPhanQuyen.giaoDien;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiDangNhap;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiDangNhapCong;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDangKy;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDangNhap;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDatLaiMatKhau;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauQuenMatKhau;
import com.elearning.dinhDanhPhanQuyen.ungDung.DangKyDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.XacThucDichVu;
import com.elearning.dungChung.baoMat.CongCuCookie;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Khớp /api/xac-thuc/** đã khai báo công khai trong CauHinhBaoMat
 * (DUONG_DAN_CONG_KHAI) — không yêu cầu JWT.
 *
 * FIX (Vấn đề #2): /dang-nhap, /lam-moi-token, /dang-xuat giờ đọc/ghi
 * accessToken + refreshToken qua cookie HttpOnly (CongCuCookie) thay vì
 * body JSON / query param như trước — xem Javadoc của CongCuCookie để biết
 * lý do đầy đủ.
 */
@RestController
@RequestMapping("/api/xac-thuc")
@RequiredArgsConstructor
public class XacThucController {

    private final DangKyDichVu dangKyDichVu;
    private final XacThucDichVu xacThucDichVu;
    private final CongCuCookie congCuCookie;

    @PostMapping("/dang-ky")
    public ResponseEntity<Void> dangKy(@Valid @RequestBody YeuCauDangKy yeuCau) {
        dangKyDichVu.dangKy(yeuCau);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/kich-hoat")
    public ResponseEntity<Void> kichHoatTaiKhoan(@RequestParam String token) {
        dangKyDichVu.kichHoatTaiKhoan(token);
        return ResponseEntity.ok().build();
    }

    /**
     * Gửi lại email kích hoạt (KB-DD-03) — giới hạn 3 lần/giờ/email, xử lý
     * ở DangKyDichVuImpl.guiLaiMaKichHoat(). Tái dùng YeuCauQuenMatKhau vì
     * cùng hình dạng dữ liệu (chỉ 1 trường email đã validate @Email).
     */
    @PostMapping("/gui-lai-kich-hoat")
    public ResponseEntity<Void> guiLaiMaKichHoat(@Valid @RequestBody YeuCauQuenMatKhau yeuCau) {
        dangKyDichVu.guiLaiMaKichHoat(yeuCau.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dang-nhap")
    public ResponseEntity<PhanHoiDangNhapCong> dangNhap(
            @Valid @RequestBody YeuCauDangNhap yeuCau, HttpServletResponse response) {
        PhanHoiDangNhap ketQua = xacThucDichVu.dangNhap(yeuCau);
        congCuCookie.datCookieDangNhap(response, ketQua.accessToken(), ketQua.refreshToken());
        return ResponseEntity.ok(PhanHoiDangNhapCong.tu(ketQua));
    }

    @PostMapping("/lam-moi-token")
    public ResponseEntity<PhanHoiDangNhapCong> lamMoiToken(
            @CookieValue(name = CongCuCookie.TEN_COOKIE_REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new NgoaiLeDuLieuKhongHopLe("Không tìm thấy refresh token — vui lòng đăng nhập lại");
        }
        PhanHoiDangNhap ketQua = xacThucDichVu.lamMoiToken(refreshToken);
        congCuCookie.datCookieDangNhap(response, ketQua.accessToken(), ketQua.refreshToken());
        return ResponseEntity.ok(PhanHoiDangNhapCong.tu(ketQua));
    }

    @PostMapping("/dang-xuat")
    public ResponseEntity<Void> dangXuat(
            @CookieValue(name = CongCuCookie.TEN_COOKIE_REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            xacThucDichVu.dangXuat(refreshToken);
        }
        congCuCookie.xoaCookieDangNhap(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quen-mat-khau")
    public ResponseEntity<Void> quenMatKhau(@Valid @RequestBody YeuCauQuenMatKhau yeuCau) {
        xacThucDichVu.yeuCauDatLaiMatKhau(yeuCau.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dat-lai-mat-khau")
    public ResponseEntity<Void> datLaiMatKhau(@Valid @RequestBody YeuCauDatLaiMatKhau yeuCau) {
        xacThucDichVu.datLaiMatKhau(yeuCau);
        return ResponseEntity.ok().build();
    }
}