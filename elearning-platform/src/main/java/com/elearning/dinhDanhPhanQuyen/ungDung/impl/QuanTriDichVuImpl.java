package com.elearning.dinhDanhPhanQuyen.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiNguoiDungAdmin;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.ThongKeHeThong;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapLaiQuyenQuanTri;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauCapNhatTrangThai;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.yeuCau.YeuCauTaoTaiKhoanAdmin;
import com.elearning.dinhDanhPhanQuyen.haTang.QuanTriHeThongKho;
import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiTaiKhoan;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;
import com.elearning.dinhDanhPhanQuyen.ungDung.QuanTriDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.dungChung.nhatKy.GhiNhatKyHanhDong;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.ThongBaoDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
class QuanTriDichVuImpl implements QuanTriDichVu {

    private final QuanTriHeThongKho kho;
    private final PasswordEncoder   passwordEncoder;
    private final ThongBaoDichVu    thongBaoDichVu;
    private final GhiNhatKyHanhDong ghiNhatKyHanhDong;

    // ──────────────── Thống kê ────────────────

    @Override
    @Transactional(readOnly = true)
    public ThongKeHeThong layThongKeHeThong() {
        long tongND   = kho.count();
        long hoatDong = kho.countByTrangThai(TrangThaiTaiKhoan.HOAT_DONG);
        long biCam    = kho.countByTrangThai(TrangThaiTaiKhoan.BI_CAM);
        long choXN    = kho.countByTrangThai(TrangThaiTaiKhoan.CHO_XAC_NHAN);
        long qtvND    = kho.countByVaiTro(VaiTro.QTV_NOI_DUNG);
        long qtvTC    = kho.countByVaiTro(VaiTro.QTV_TAI_CHINH);

        long soGDThanhCong = kho.demGiaoDichKhoaHocThanhCong() + kho.demGiaoDichLopHocThanhCong();
        long soGDCho       = kho.demGiaoDichKhoaHocCho()       + kho.demGiaoDichLopHocCho();

        return new ThongKeHeThong(
                tongND, hoatDong, biCam, choXN, qtvND, qtvTC,
                kho.demTongKhoaHoc(), kho.demKhoaHocCongKhai(),
                kho.demKhoaHocChoDuyet(), kho.demKhoaHocNhap(),
                kho.demTongLopHoc(), kho.demLopHocHoatDong(),
                kho.tongDoanhThuKhoaHoc(), kho.tongDoanhThuLopHoc(),
                soGDThanhCong, soGDCho,
                kho.demPhieuDoiSoatChuaXuLy(),
                kho.demTongBaiDang(), kho.demTongBinhLuan()
        );
    }

    // ──────────────── Tạo tài khoản admin ────────────────

    @Override
    public PhanHoiNguoiDungAdmin taoTaiKhoanAdmin(Long maNguoiDungCaoCap, YeuCauTaoTaiKhoanAdmin yeuCau) {
        // Chỉ được tạo QTV_NOI_DUNG hoặc QTV_TAI_CHINH — không cho tự tạo QTV_CAO_CAP
        VaiTro vaiTroMoi;
        try {
            vaiTroMoi = VaiTro.valueOf(yeuCau.vaiTro());
        } catch (IllegalArgumentException e) {
            throw new NgoaiLeDuLieuKhongHopLe("Vai trò không hợp lệ: " + yeuCau.vaiTro());
        }
        if (vaiTroMoi == VaiTro.QTV_CAO_CAP || vaiTroMoi == VaiTro.NGUOI_DUNG) {
            throw new NgoaiLeKhongCoQuyen("Chỉ có thể tạo tài khoản QTV_NOI_DUNG hoặc QTV_TAI_CHINH");
        }

        // FIX: cùng lý do như DangKyDichVuImpl.dangKy() — chuẩn hóa về chữ
        // thường để tài khoản admin tạo ra vẫn đăng nhập được qua dangNhap()
        // (đã chuẩn hóa khi tra cứu), và tránh tạo 2 tài khoản khác nhau chỉ
        // khác hoa/thường cho cùng một email.
        String email = yeuCau.email().trim().toLowerCase();

        if (kho.existsByEmail(email)) {
            throw new NgoaiLeDuLieuKhongHopLe("Email đã được sử dụng: " + email);
        }

        NguoiDung nd = new NguoiDung(
                yeuCau.hoTen(), email,
                passwordEncoder.encode(yeuCau.matKhau()));
        nd.setVaiTro(vaiTroMoi);
        nd.setTrangThai(TrangThaiTaiKhoan.HOAT_DONG); // kích hoạt ngay
        kho.save(nd);

        ghiNhatKyHanhDong.ghiNhan(maNguoiDungCaoCap, "TAO_TAI_KHOAN_ADMIN",
                "nguoi_dung#" + nd.getMaNguoiDung(), "Vai trò: " + vaiTroMoi + ", email: " + email);

        return sangDto(nd);
    }

    // ──────────────── Quản lý người dùng ────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiNguoiDungAdmin> layDanhSachNguoiDung(
            String timKiem, String vaiTro, String trangThai, Pageable pageable) {

        // 1. Đưa tất cả null về chuỗi rỗng "" để triệt tiêu hoàn toàn lỗi bytea của PostgreSQL
        // Nếu có từ khóa, tự động thêm % và in thường để tìm kiếm tương đối (LIKE)
        String kw = (timKiem == null || timKiem.isBlank()) ? "" : "%" + timKiem.trim().toLowerCase() + "%";
        
        // 2. Giữ nguyên định dạng String cho Vai trò và Trạng thái
        String vt = (vaiTro == null || vaiTro.isBlank()) ? "" : vaiTro.trim();
        String tt = (trangThai == null || trangThai.isBlank()) ? "" : trangThai.trim();

        // 3. Truyền trực tiếp 3 chuỗi String xuống Repository
        return kho.timKiemNguoiDung(kw, vt, tt, pageable).map(this::sangDto);
    }

    @Override
    public PhanHoiNguoiDungAdmin capNhatTrangThaiNguoiDung(Long maNguoiDungAdmin, boolean nguoiGoiLaCaoCap,
            Long maNguoiDung, YeuCauCapNhatTrangThai yeuCau) {
        // Chặn tự khoá/tự mở khoá chính tài khoản đang đăng nhập — áp dụng
        // cho MỌI người gọi (cả QTV_CAO_CAP lẫn QTV_NOI_DUNG), vì việc tự
        // khoá chính mình luôn là thao tác vô nghĩa/nguy hiểm (có thể tự
        // khoá nhầm rồi không ai gỡ được nếu là QTV_CAO_CAP duy nhất).
        if (maNguoiDungAdmin.equals(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Không thể thực hiện thao tác này trên tài khoản đang đăng nhập");
        }

        NguoiDung nd = timHoacNem(maNguoiDung);

        if (nd.getVaiTro() == VaiTro.QTV_CAO_CAP) {
            // Không ai khoá được QTV_CAO_CAP khác qua API này — kể cả một
            // QTV_CAO_CAP khác. Việc thu hồi vai trò cao cấp của người khác
            // (nếu thật sự cần) đi qua luồng riêng thuHoiQuyenQuanTri(), có
            // xét duyệt/ghi log tách biệt, không lẫn vào thao tác khoá/mở
            // khoá tài khoản thông thường.
            throw new NgoaiLeKhongCoQuyen("Không thể thay đổi trạng thái tài khoản Quản trị viên cấp cao qua API này");
        }

        if (nd.getVaiTro() != VaiTro.NGUOI_DUNG && !nguoiGoiLaCaoCap) {
            // Mục tiêu là QTV_NOI_DUNG hoặc QTV_TAI_CHINH nhưng người gọi chỉ
            // là QTV_NOI_DUNG (không phải QTV_CAO_CAP) — chặn để tránh leo
            // thang đặc quyền ngang hàng (1 QTV_NOI_DUNG khoá được QTV_TAI_CHINH).
            throw new NgoaiLeKhongCoQuyen("Chỉ Quản trị viên cấp cao mới có quyền thay đổi trạng thái tài khoản quản trị viên khác");
        }

        TrangThaiTaiKhoan tt;
        try {
            tt = TrangThaiTaiKhoan.valueOf(yeuCau.trangThai());
        } catch (IllegalArgumentException e) {
            throw new NgoaiLeDuLieuKhongHopLe("Trạng thái không hợp lệ: " + yeuCau.trangThai());
        }
        if (tt == TrangThaiTaiKhoan.CHO_XAC_NHAN) {
            throw new NgoaiLeDuLieuKhongHopLe("Không thể chuyển thủ công về trạng thái CHO_XAC_NHAN");
        }

        if (tt == TrangThaiTaiKhoan.BI_CAM) {
            if (yeuCau.lyDo() == null || yeuCau.lyDo().isBlank()) {
                throw new NgoaiLeDuLieuKhongHopLe("Vui lòng nhập lý do khóa tài khoản");
            }
            nd.setTrangThai(tt);

            // Gửi thông báo vi phạm đến người dùng. Việc khóa có hiệu lực NGAY
            // LẬP TỨC cho mọi request tiếp theo — xem DichVuJWT.tokenHopLe(),
            // vốn kiểm tra isEnabled()/isAccountNonLocked() được load mới từ
            // DB ở mỗi request, không cần thu hồi token theo cơ chế riêng.
            thongBaoDichVu.guiThongBao(nd.getMaNguoiDung(), LoaiThongBao.TAI_KHOAN_BI_KHOA,
                    "Tài khoản của bạn đã bị khóa",
                    "Tài khoản của bạn đã bị Quản trị viên khóa do vi phạm. Lý do: " + yeuCau.lyDo(),
                    Map.of("maNguoiDung", nd.getMaNguoiDung()));

            ghiNhatKyHanhDong.ghiNhan(maNguoiDungAdmin, "KHOA_TAI_KHOAN",
                    "nguoi_dung#" + nd.getMaNguoiDung(), yeuCau.lyDo());
        } else {
            nd.setTrangThai(tt);
            ghiNhatKyHanhDong.ghiNhan(maNguoiDungAdmin, "MO_KHOA_TAI_KHOAN",
                    "nguoi_dung#" + nd.getMaNguoiDung(), null);
        }

        return sangDto(nd);
    }

    @Override
    public PhanHoiNguoiDungAdmin thuHoiQuyenQuanTri(Long maNguoiDungCaoCap, Long maNguoiDungMucTieu) {
        if (maNguoiDungCaoCap.equals(maNguoiDungMucTieu)) {
            throw new NgoaiLeKhongCoQuyen("Không thể tự thu hồi quyền của chính mình");
        }

        NguoiDung nd = timHoacNem(maNguoiDungMucTieu);
        if (nd.getVaiTro() != VaiTro.QTV_NOI_DUNG && nd.getVaiTro() != VaiTro.QTV_TAI_CHINH) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Chỉ có thể thu hồi quyền của tài khoản QTV_NOI_DUNG hoặc QTV_TAI_CHINH");
        }

        VaiTro vaiTroCu = nd.getVaiTro();
        nd.setVaiTro(VaiTro.NGUOI_DUNG);

        // Không cần thu hồi phiên riêng: quyền hạn (authorities) được load lại
        // mới hoàn toàn từ DB ở mỗi request (BoLocJWT không cache UserDetails),
        // nên ngay request kế tiếp tài khoản này đã mất mọi quyền quản trị.
        ghiNhatKyHanhDong.ghiNhan(maNguoiDungCaoCap, "THU_HOI_QUYEN_QUAN_TRI",
                "nguoi_dung#" + nd.getMaNguoiDung(), "Vai trò cũ: " + vaiTroCu + " -> NGUOI_DUNG");

        return sangDto(nd);
    }

    /**
     * Đối xứng với thuHoiQuyenQuanTri() — cấp lại quyền cho tài khoản đang ở
     * NGUOI_DUNG, kể cả tài khoản từng bị thu hồi quyền trước đó. Khác với
     * taoTaiKhoanAdmin(): không tạo bản ghi mới, không cần hoTen/email/matKhau,
     * vì tài khoản đã tồn tại sẵn trong hệ thống.
     */
    @Override
    public PhanHoiNguoiDungAdmin capLaiQuyenQuanTri(Long maNguoiDungCaoCap, Long maNguoiDungMucTieu,
                                                     YeuCauCapLaiQuyenQuanTri yeuCau) {
        VaiTro vaiTroMoi;
        try {
            vaiTroMoi = VaiTro.valueOf(yeuCau.vaiTro());
        } catch (IllegalArgumentException e) {
            throw new NgoaiLeDuLieuKhongHopLe("Vai trò không hợp lệ: " + yeuCau.vaiTro());
        }
        if (vaiTroMoi != VaiTro.QTV_NOI_DUNG && vaiTroMoi != VaiTro.QTV_TAI_CHINH) {
            throw new NgoaiLeKhongCoQuyen("Chỉ có thể cấp quyền QTV_NOI_DUNG hoặc QTV_TAI_CHINH qua thao tác này");
        }

        NguoiDung nd = timHoacNem(maNguoiDungMucTieu);
        if (nd.getVaiTro() != VaiTro.NGUOI_DUNG) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Chỉ có thể cấp quyền cho tài khoản đang ở vai trò người dùng thông thường — "
                            + "tài khoản này hiện đang là " + nd.getVaiTro());
        }
        if (nd.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG) {
            throw new NgoaiLeDuLieuKhongHopLe("Không thể cấp quyền quản trị cho tài khoản chưa/không hoạt động");
        }

        nd.setVaiTro(vaiTroMoi);

        ghiNhatKyHanhDong.ghiNhan(maNguoiDungCaoCap, "CAP_LAI_QUYEN_QUAN_TRI",
                "nguoi_dung#" + nd.getMaNguoiDung(), "Vai trò mới: " + vaiTroMoi);

        return sangDto(nd);
    }

    @Override
    @Transactional(readOnly = true)
    public PhanHoiNguoiDungAdmin layChiTietNguoiDung(Long maNguoiDung) {
        return sangDto(timHoacNem(maNguoiDung));
    }

    // ──────────────── Helper ────────────────

    private NguoiDung timHoacNem(Long maNguoiDung) {
        return kho.findById(maNguoiDung)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("người dùng", maNguoiDung));
    }

    private PhanHoiNguoiDungAdmin sangDto(NguoiDung nd) {
        return new PhanHoiNguoiDungAdmin(
                nd.getMaNguoiDung(), nd.getHoTen(), nd.getEmail(),
                nd.getSdt(), nd.getVaiTro().name(),
                nd.getTrangThai().name(), nd.getAnhDaiDien(), nd.getNgayTao());
    }
}