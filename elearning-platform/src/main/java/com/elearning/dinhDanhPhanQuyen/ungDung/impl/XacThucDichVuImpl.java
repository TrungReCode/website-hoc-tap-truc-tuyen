package com.elearning.dinhDanhPhanQuyen.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiDangNhap;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDangNhap;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDatLaiMatKhau;
import com.elearning.dinhDanhPhanQuyen.haTang.MaLamMoiPhienKho;
import com.elearning.dinhDanhPhanQuyen.haTang.NguoiDungKho;
import com.elearning.dinhDanhPhanQuyen.mienDomain.MaLamMoiPhien;
import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.ungDung.XacThucDichVu;
import com.elearning.dungChung.baoMat.DichVuJWT;
import com.elearning.dungChung.email.AdapterGuiEmail;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
class XacThucDichVuImpl implements XacThucDichVu {

    private final AuthenticationManager authenticationManager;
    private final NguoiDungKho nguoiDungKho;
    private final MaLamMoiPhienKho maLamMoiPhienKho;
    private final DichVuJWT dichVuJWT;
    private final PasswordEncoder passwordEncoder;
    private final AdapterGuiEmail adapterGuiEmail;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String MUC_DICH_DAT_LAI_MAT_KHAU = "DAT_LAI_MAT_KHAU";

    // ── Rate limit chống brute-force đăng nhập ──
    private static final String TIEN_TO_DEM_THAT_BAI = "dang-nhap:that-bai:";
    private static final int SO_LAN_SAI_TOI_DA = 5;
    private static final Duration THOI_GIAN_KHOA_TAM = Duration.ofMinutes(15);

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long thoiHanRefreshToken;

    @Override
    @Transactional
    public PhanHoiDangNhap dangNhap(YeuCauDangNhap yeuCau) {
        String email = yeuCau.email().trim().toLowerCase();
        String khoaDem = TIEN_TO_DEM_THAT_BAI + email;

        Object giaTriHienTai = redisTemplate.opsForValue().get(khoaDem);
        long soLanSaiHienTai = giaTriHienTai != null ? Long.parseLong(giaTriHienTai.toString()) : 0L;
        if (soLanSaiHienTai >= SO_LAN_SAI_TOI_DA) {
            Long ttlConLaiGiay = redisTemplate.getExpire(khoaDem);
            long phutConLai = ttlConLaiGiay != null ? Math.max(1, (ttlConLaiGiay + 59) / 60) : 15;
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Tài khoản tạm thời bị khóa đăng nhập do nhập sai quá " + SO_LAN_SAI_TOI_DA
                            + " lần. Vui lòng thử lại sau khoảng " + phutConLai + " phút.");
        }

        try {
            // AuthenticationManager tự gọi ChiTietNguoiDungDichVu + PasswordEncoder,
            // ném AuthenticationException nếu sai (đã có XuLyNgoaiLeToanCuc bắt sẵn)
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, yeuCau.matKhau()));
        } catch (AuthenticationException ex) {
            ghiNhanDangNhapThatBai(khoaDem);
            throw ex;
        }

        // Đăng nhập thành công -> xóa bộ đếm thất bại (nếu có)
        redisTemplate.delete(khoaDem);

        NguoiDung nguoiDung = nguoiDungKho.findByEmail(email)
            .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("người dùng", email));

        String accessToken = dichVuJWT.taoAccessToken(nguoiDung);
        String refreshToken = taoRefreshTokenMoi(nguoiDung.getMaNguoiDung());

        return new PhanHoiDangNhap(
                accessToken, refreshToken,
                nguoiDung.getHoTen(), nguoiDung.getEmail(), nguoiDung.getVaiTro().name(),
                nguoiDung.getMaNguoiDung());
    }

    /** Tăng bộ đếm đăng nhập sai trong Redis; đặt TTL ngay từ lần sai đầu tiên. */
    private void ghiNhanDangNhapThatBai(String khoaDem) {
        Long soLanSau = redisTemplate.opsForValue().increment(khoaDem);
        if (soLanSau != null && soLanSau == 1L) {
            redisTemplate.expire(khoaDem, THOI_GIAN_KHOA_TAM);
        }
    }

    @Override
    @Transactional
    public PhanHoiDangNhap lamMoiToken(String refreshToken) {
        MaLamMoiPhien phien = maLamMoiPhienKho.findByToken(refreshToken)
                .orElseThrow(() -> new NgoaiLeDuLieuKhongHopLe("Refresh token không hợp lệ"));

        if (!phien.conHieuLuc()) {
            // FIX (refresh token reuse detection): nếu token này đã từng bị
            // CHÍNH HỆ THỐNG thu hồi trước đó (thuHoi = true) — khác với
            // chỉ hết hạn tự nhiên theo hetHan — thì đây là dấu hiệu token
            // đã bị đánh cắp: chủ tài khoản (hoặc kẻ tấn công) đã rotate
            // sang token mới rồi, mà giờ token CŨ lại bị đem dùng lại. Phản
            // ứng: thu hồi TOÀN BỘ phiên hiện có của người dùng này, buộc
            // đăng nhập lại ở mọi thiết bị — cùng hành động với đổi mật khẩu.
            if (Boolean.TRUE.equals(phien.getThuHoi())) {
                thuHoiTatCaPhien(phien.getNguoiDungId());
            }
            throw new NgoaiLeDuLieuKhongHopLe("Refresh token đã hết hạn hoặc đã bị thu hồi");
        }

        NguoiDung nguoiDung = nguoiDungKho.findById(phien.getNguoiDungId())
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("người dùng", phien.getNguoiDungId()));

        // Thu hồi token cũ, phát hành cặp token mới (rotation — tăng an toàn)
        phien.setThuHoi(true);
        maLamMoiPhienKho.save(phien);

        String accessTokenMoi = dichVuJWT.taoAccessToken(nguoiDung);
        String refreshTokenMoi = taoRefreshTokenMoi(nguoiDung.getMaNguoiDung());

        return new PhanHoiDangNhap(
                accessTokenMoi, refreshTokenMoi,
                nguoiDung.getHoTen(), nguoiDung.getEmail(), nguoiDung.getVaiTro().name(),
                nguoiDung.getMaNguoiDung());
    }

    @Override
    @Transactional
    public void dangXuat(String refreshToken) {
        maLamMoiPhienKho.findByToken(refreshToken).ifPresent(phien -> {
            phien.setThuHoi(true);
            maLamMoiPhienKho.save(phien);
        });
    }

    private String taoRefreshTokenMoi(Long maNguoiDung) {
        String token = taoChuoiNgauNhien();
        LocalDateTime hetHan = LocalDateTime.now().plusNanos(thoiHanRefreshToken * 1_000_000);
        maLamMoiPhienKho.save(new MaLamMoiPhien(maNguoiDung, token, hetHan));
        return token;
    }

    private String taoChuoiNgauNhien() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    @Transactional(readOnly = true)
    public void yeuCauDatLaiMatKhau(String email) {
        // FIX: chuẩn hóa như dangNhap()/dangKy() — nếu không, người đăng ký
        // bằng email có chữ hoa sẽ không "quên mật khẩu" được vì tra cứu
        // không khớp bản ghi đã lưu (chữ thường).
        String emailChuanHoa = email.trim().toLowerCase();
        nguoiDungKho.findByEmail(emailChuanHoa).ifPresent(nd -> {
            String token = dichVuJWT.taoTokenDatLaiMatKhau(nd.getEmail());
            adapterGuiEmail.guiEmailDatLaiMatKhau(nd.getEmail(), nd.getHoTen(), token);
        });
        // Không throw nếu không tìm thấy email — tránh lộ thông tin email đã đăng ký hay chưa.
    }

    @Override
    @Transactional
    public void datLaiMatKhau(YeuCauDatLaiMatKhau yeuCau) {
        if (!Objects.equals(yeuCau.matKhauMoi(), yeuCau.xacNhanMatKhauMoi())) {
            throw new NgoaiLeDuLieuKhongHopLe("Xác nhận mật khẩu mới không khớp");
        }

        String email;
        try {
            email = dichVuJWT.xacThucTokenChucNang(yeuCau.token(), MUC_DICH_DAT_LAI_MAT_KHAU);
        } catch (ExpiredJwtException e) {
            throw new NgoaiLeDuLieuKhongHopLe("Liên kết đặt lại mật khẩu đã hết hạn. Vui lòng yêu cầu gửi lại email.");
        } catch (JwtException e) {
            throw new NgoaiLeDuLieuKhongHopLe("Liên kết đặt lại mật khẩu không hợp lệ.");
        }

        NguoiDung nguoiDung = nguoiDungKho.findByEmail(email)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("người dùng", email));

        nguoiDung.setMatKhau(passwordEncoder.encode(yeuCau.matKhauMoi()));
        nguoiDungKho.save(nguoiDung);

        // Thu hồi mọi phiên đăng nhập hiện có — bắt buộc đăng nhập lại bằng mật khẩu mới.
        thuHoiTatCaPhien(nguoiDung.getMaNguoiDung());
    }

    /**
     * Thu hồi toàn bộ refresh token còn hiệu lực của một người dùng — dùng
     * khi đổi mật khẩu VÀ khi phát hiện refresh token bị dùng lại sau khi
     * đã rotate (xem lamMoiToken()), vì cả hai đều là tình huống cần buộc
     * đăng nhập lại ở mọi thiết bị.
     */
    private void thuHoiTatCaPhien(Long maNguoiDung) {
        List<MaLamMoiPhien> phienHienCo = maLamMoiPhienKho.findByNguoiDungId(maNguoiDung);
        phienHienCo.forEach(p -> p.setThuHoi(true));
        maLamMoiPhienKho.saveAll(phienHienCo);
    }
}