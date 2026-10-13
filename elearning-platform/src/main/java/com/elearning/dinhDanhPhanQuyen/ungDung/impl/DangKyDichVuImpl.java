package com.elearning.dinhDanhPhanQuyen.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauDangKy;
import com.elearning.dinhDanhPhanQuyen.haTang.NguoiDungKho;
import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiTaiKhoan;
import com.elearning.dinhDanhPhanQuyen.ungDung.DangKyDichVu;
import com.elearning.dungChung.baoMat.DichVuJWT;
import com.elearning.dungChung.email.AdapterGuiEmail;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
class DangKyDichVuImpl implements DangKyDichVu {

    private final NguoiDungKho nguoiDungKho;
    private final PasswordEncoder passwordEncoder;
    private final AdapterGuiEmail adapterGuiEmail;
    private final DichVuJWT dichVuJWT;
    private final RedisTemplate<String, Object> redisTemplate;

    // ── Rate limit chống spam "gửi lại mã kích hoạt" (KB-DD-03) ──
    // Cùng cơ chế đếm-trong-Redis với XacThucDichVuImpl (chống brute-force
    // đăng nhập) — TTL 1 giờ đặt ngay từ lần gửi đầu tiên, quá 3 lần thì từ
    // chối tới khi TTL hết hạn.
    private static final String TIEN_TO_DEM_GUI_LAI_KICH_HOAT = "kich-hoat:gui-lai:";
    private static final int SO_LAN_GUI_LAI_TOI_DA = 3;
    private static final Duration THOI_GIAN_GIOI_HAN_GUI_LAI = Duration.ofHours(1);

    @Override
    @Transactional
    public void dangKy(YeuCauDangKy yeuCau) {
        // FIX (email enumeration): trước đây ném lỗi "Email đã được sử dụng"
        // khi email tồn tại — kẻ dò email chỉ cần thử đăng ký để biết một
        // địa chỉ có tài khoản hay không, cùng loại rủi ro mà
        // quenMatKhau()/guiLaiMaKichHoat() đã cố tình tránh. Giờ nhánh này
        // KHÔNG throw và KHÔNG tạo tài khoản mới — chỉ báo cho đúng chủ tài
        // khoản hiện có, còn controller vẫn trả 201 giống hệt trường hợp
        // đăng ký thành công nên người gọi API không phân biệt được.
        // FIX: chuẩn hóa email về chữ thường TRƯỚC khi lưu — bắt buộc phải
        // làm ở đây vì đây là nơi email đi vào hệ thống lần đầu. dangNhap()
        // đã chuẩn hóa về chữ thường khi tra cứu (so sánh bằng của Postgres
        // phân biệt hoa/thường); nếu dangKy() lưu nguyên văn "User@Gmail.com"
        // thì người đăng ký đúng kiểu đó sẽ KHÔNG đăng nhập lại được bằng
        // chính email mình đã dùng để đăng ký.
        String email = yeuCau.email().trim().toLowerCase();

        if (nguoiDungKho.existsByEmail(email)) {
            nguoiDungKho.findByEmail(email)
                    .ifPresent(nd -> adapterGuiEmail.guiEmailCanhBaoDangKyTrung(nd.getEmail(), nd.getHoTen()));
            return;
        }

        NguoiDung nguoiDungMoi = new NguoiDung(
                yeuCau.hoTen(),
                email,
                passwordEncoder.encode(yeuCau.matKhau()),
                chuanHoaSdt(yeuCau.sdt())
        );
        nguoiDungMoi = nguoiDungKho.save(nguoiDungMoi);

        // THAY ĐỔI: Dùng JWT thay vì SecureRandom
        String tokenKichHoat = dichVuJWT.taoTokenKichHoat(nguoiDungMoi.getEmail());

        adapterGuiEmail.guiEmailKichHoat(nguoiDungMoi.getEmail(), nguoiDungMoi.getHoTen(), tokenKichHoat);
    }

    @Override
    @Transactional
    public void kichHoatTaiKhoan(String token) {
        try {
            // Giải mã token bằng JWT
            String email = dichVuJWT.layEmailTuToken(token);

            NguoiDung nguoiDung = nguoiDungKho.findByEmail(email)
                    .orElseThrow(() -> new NgoaiLeDuLieuKhongHopLe("Không tìm thấy tài khoản hợp lệ."));

            if (nguoiDung.getTrangThai() == TrangThaiTaiKhoan.HOAT_DONG) {
                throw new NgoaiLeDuLieuKhongHopLe("Tài khoản này đã được kích hoạt trước đó!");
            }

            nguoiDung.setTrangThai(TrangThaiTaiKhoan.HOAT_DONG);
            nguoiDungKho.save(nguoiDung);

        } catch (ExpiredJwtException e) {
            throw new NgoaiLeDuLieuKhongHopLe("Mã kích hoạt đã hết hạn. Vui lòng yêu cầu gửi lại email.");
        } catch (JwtException e) {
            throw new NgoaiLeDuLieuKhongHopLe("Mã kích hoạt không hợp lệ hoặc đã bị hỏng.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void guiLaiMaKichHoat(String email) {
        String emailChuanHoa = email.trim().toLowerCase();
        String khoaDem = TIEN_TO_DEM_GUI_LAI_KICH_HOAT + emailChuanHoa;

        // Tính vào giới hạn NGAY CẢ KHI email chưa từng đăng ký — tránh lộ
        // thông tin "email này có tồn tại hay không" qua việc chỉ áp giới
        // hạn cho email có thật (nếu chỉ đếm khi tìm thấy tài khoản, kẻ tấn
        // công có thể suy ra email có tồn tại bằng cách gọi >3 lần rồi xem
        // có bị chặn hay không).
        Long soLanSau = redisTemplate.opsForValue().increment(khoaDem);
        if (soLanSau != null && soLanSau == 1L) {
            redisTemplate.expire(khoaDem, THOI_GIAN_GIOI_HAN_GUI_LAI);
        }
        if (soLanSau != null && soLanSau > SO_LAN_GUI_LAI_TOI_DA) {
            Long ttlConLaiGiay = redisTemplate.getExpire(khoaDem);
            long phutConLai = ttlConLaiGiay != null ? Math.max(1, (ttlConLaiGiay + 59) / 60) : 60;
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Bạn đã yêu cầu gửi lại mã kích hoạt quá " + SO_LAN_GUI_LAI_TOI_DA
                            + " lần trong 1 giờ. Vui lòng thử lại sau khoảng " + phutConLai + " phút.");
        }

        nguoiDungKho.findByEmail(emailChuanHoa).ifPresent(nd -> {
            // Tài khoản đã kích hoạt rồi — không gửi lại, nhưng vẫn được coi
            // là 1 lần yêu cầu hợp lệ (đã cộng vào bộ đếm ở trên) để tránh
            // dùng response khác biệt làm oracle dò trạng thái tài khoản.
            if (nd.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG) {
                String tokenKichHoat = dichVuJWT.taoTokenKichHoat(nd.getEmail());
                adapterGuiEmail.guiEmailKichHoat(nd.getEmail(), nd.getHoTen(), tokenKichHoat);
            }
        });
        // Không throw nếu không tìm thấy email — cùng nguyên tắc với quên mật khẩu.
    }

    private String chuanHoaSdt(String sdt) {
        return (sdt == null || sdt.isBlank()) ? null : sdt;
    }
}