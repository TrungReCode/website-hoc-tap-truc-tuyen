package com.elearning.thanhToan.ungDung.impl;

import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.choKhoaHoc.mienDomain.KhoaHoc;
import com.elearning.dinhDanhPhanQuyen.haTang.QuanTriHeThongKho;
import com.elearning.dinhDanhPhanQuyen.haTang.TaiKhoanNganHangKho;
import com.elearning.dinhDanhPhanQuyen.mienDomain.NguoiDung;
import com.elearning.dinhDanhPhanQuyen.mienDomain.TaiKhoanNganHang;
import com.elearning.dinhDanhPhanQuyen.ungDung.QuanTriDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.ThanhVienLop;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiDoanhThuGiangVien;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDichDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiHoanTienChiTiet;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiPhieuDangChoHoanThanhLop;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiPhieuDoiSoat;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTaiChinhTongQuan;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTongHopDoiSoatTheoGiangVien;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTongQuanDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiXuHuongDoanhThu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiXuHuongDoanhThu.DiemXuHuong;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauCapNhatPhieuDoiSoat;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauXuLyHoanTien;
import com.elearning.thanhToan.haTang.PhieuDoiSoatKho;
import com.elearning.thanhToan.haTang.PhieuHoanTienKhoaHocKho;
import com.elearning.thanhToan.haTang.PhieuHoanTienLopHocKho;
import com.elearning.thanhToan.haTang.PhieuMuaKhoaHocKho;
import com.elearning.thanhToan.haTang.PhieuTraPhiLopHocKho;
import com.elearning.thanhToan.haTang.boPhanNgoai.AdapterVnpayHoanTien;
import com.elearning.thanhToan.haTang.boPhanNgoai.KetQuaHoanTienVnpay;
import com.elearning.thanhToan.mienDomain.PhieuDoiSoat;
import com.elearning.thanhToan.mienDomain.PhieuHoanTienKhoaHoc;
import com.elearning.thanhToan.mienDomain.PhieuHoanTienLopHoc;
import com.elearning.thanhToan.mienDomain.PhieuMuaKhoaHoc;
import com.elearning.thanhToan.mienDomain.PhieuTraPhiLopHoc;
import com.elearning.thanhToan.mienDomain.TrangThaiDoiSoat;
import com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich;
import com.elearning.thanhToan.mienDomain.TrangThaiHoanTien;
import com.elearning.thanhToan.ungDung.TaiChinhDichVu;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.ThongBaoDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.YearMonth;
import java.time.DayOfWeek;

@Service
@RequiredArgsConstructor
@Transactional
class TaiChinhDichVuImpl implements TaiChinhDichVu {

    private final QuanTriDichVu quanTriDichVu;
    private final QuanTriHeThongKho quanTriHeThongKho;
    private final TaiKhoanNganHangKho taiKhoanNganHangKho;
    private final PhieuDoiSoatKho phieuDoiSoatKho;

    private final PhieuHoanTienKhoaHocKho phieuHoanTienKhoaHocKho;
    private final PhieuHoanTienLopHocKho phieuHoanTienLopHocKho;
    private final PhieuMuaKhoaHocKho phieuMuaKhoaHocKho;
    private final PhieuTraPhiLopHocKho phieuTraPhiLopHocKho;
    private final QuyenSuDungKho quyenSuDungKho;
    private final ThanhVienLopKho thanhVienLopKho;
    private final LopHocKho lopHocKho;
    private final KhoaHocKho khoaHocKho;
    private final ThongBaoDichVu thongBaoDichVu;
    private final AdapterVnpayHoanTien adapterVnpayHoanTien;

    @Override
    @Transactional(readOnly = true)
    public PhanHoiTaiChinhTongQuan layTongQuanTaiChinh() {
        var thongKe = quanTriDichVu.layThongKeHeThong();

        long soYeuCauHoanTienChoXuLy = phieuHoanTienKhoaHocKho
                .findByTrangThai(TrangThaiHoanTien.CHO_XAC_NHAN, Pageable.unpaged()).getTotalElements()
                + phieuHoanTienLopHocKho.findByTrangThai(TrangThaiHoanTien.CHO_XAC_NHAN, Pageable.unpaged())
                        .getTotalElements();

        return new PhanHoiTaiChinhTongQuan(
                thongKe.tongDoanhThuKhoaHoc(),
                thongKe.tongDoanhThuLopHoc(),
                anToanCong(thongKe.tongDoanhThuKhoaHoc(), thongKe.tongDoanhThuLopHoc()),
                thongKe.soGiaoDichThanhCong(),
                thongKe.soGiaoDichChoXacNhan(),
                phieuDoiSoatKho.countByTrangThai(TrangThaiDoiSoat.CHO_XAC_NHAN),
                phieuDoiSoatKho.countByTrangThai(TrangThaiDoiSoat.DANG_XU_LY),
                phieuDoiSoatKho.countByTrangThai(TrangThaiDoiSoat.HOAN_TAT),
                phieuDoiSoatKho.countByTrangThai(TrangThaiDoiSoat.THAT_BAI),
                soYeuCauHoanTienChoXuLy);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiPhieuDoiSoat> layDanhSachPhieuDoiSoat(
            String trangThai, String timKiem, LocalDate tuNgay, LocalDate denNgay, Pageable pageable) {
        List<PhanHoiPhieuDoiSoat> daAnhXa = locPhieuDoiSoat(trangThai, timKiem, tuNgay, denNgay);

        int tuVitri = (int) pageable.getOffset();
        int denViTri = Math.min(tuVitri + pageable.getPageSize(), daAnhXa.size());
        List<PhanHoiPhieuDoiSoat> trangHienTai = tuVitri >= daAnhXa.size() ? List.of()
                : daAnhXa.subList(tuVitri, denViTri);

        return new PageImpl<>(trangHienTai, pageable, daAnhXa.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiPhieuDoiSoat> layTatCaPhieuDoiSoatDaLoc(
            String trangThai, String timKiem, LocalDate tuNgay, LocalDate denNgay) {
        return locPhieuDoiSoat(trangThai, timKiem, tuNgay, denNgay);
    }

    private List<PhanHoiPhieuDoiSoat> locPhieuDoiSoat(
            String trangThai, String timKiem, LocalDate tuNgay, LocalDate denNgay) {
        List<PhieuDoiSoat> tatCa = (trangThai == null || trangThai.isBlank())
                ? phieuDoiSoatKho.findAll()
                : phieuDoiSoatKho.findByTrangThaiIn(List.of(phanTichTrangThaiDoiSoat(trangThai)));

        Map<Long, TaiKhoanNganHang> taiKhoanTheoMa = trakhoanNganHangTheoLo(tatCa);
        Map<Long, NguoiDung> giangVienTheoMa = giangVienTheoLo(taiKhoanTheoMa.values());

        LocalDateTime tuThoiDiem = tuNgay != null ? tuNgay.atStartOfDay() : null;
        LocalDateTime denThoiDiem = denNgay != null ? denNgay.atTime(23, 59, 59) : null;

        List<PhanHoiPhieuDoiSoat> daAnhXa = tatCa.stream()
                .map(p -> sangDto(p, taiKhoanTheoMa, giangVienTheoMa))
                // Yêu cầu: chỉ hiện/xuất phiếu đối soát có giá trị, ẩn hẳn
                // phiếu giá 0 (thường do giao dịch khuyến mãi/tặng miễn phí
                // không phát sinh doanh thu thật để đối soát). Lọc NGAY Ở
                // ĐÂY — dùng chung bởi layDanhSachPhieuDoiSoat (bảng phân
                // trang) VÀ layTatCaPhieuDoiSoatDaLoc (xuất CSV toàn bộ) —
                // nên cả 2 nơi tự động đồng bộ, không cần sửa riêng từng
                // chỗ. Đồng thời fix luôn phần số liệu phân trang (trước
                // đây totalElements/totalPages tính trên tập CHƯA lọc, lệch
                // với số dòng thực tế hiển thị sau khi lọc ở frontend).
                .filter(p -> p.tongDoanhThu() != null && p.tongDoanhThu().compareTo(BigDecimal.ZERO) > 0)
                .filter(p -> tuThoiDiem == null || p.ngayTao() == null || !p.ngayTao().isBefore(tuThoiDiem))
                .filter(p -> denThoiDiem == null || p.ngayTao() == null || !p.ngayTao().isAfter(denThoiDiem))
                .sorted(Comparator.comparing(PhanHoiPhieuDoiSoat::ngayTao,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        if (timKiem != null && !timKiem.isBlank()) {
            String tuKhoa = timKiem.trim().toLowerCase(Locale.ROOT);
            daAnhXa = daAnhXa.stream()
                    .filter(p -> String.valueOf(p.maDoiSoat()).contains(tuKhoa)
                            || (p.tenGiangVien() != null && p.tenGiangVien().toLowerCase(Locale.ROOT).contains(tuKhoa))
                            || (p.tenChuTaiKhoan() != null
                                    && p.tenChuTaiKhoan().toLowerCase(Locale.ROOT).contains(tuKhoa))
                            || (p.maGiaoDichNganHang() != null
                                    && p.maGiaoDichNganHang().toLowerCase(Locale.ROOT).contains(tuKhoa))
                            || (p.soTaiKhoan() != null && p.soTaiKhoan().toLowerCase(Locale.ROOT).contains(tuKhoa)))
                    .toList();
        }

        return daAnhXa;
    }

    /**
     * Tra cứu TaiKhoanNganHang theo LÔ cho một danh sách phiếu đối soát —
     * gom hết mã tài khoản cần tra rồi chỉ chạy 1 câu {@code findAllById}
     * duy nhất, thay vì gọi {@code findById} riêng lẻ cho từng phiếu (N+1).
     */
    private Map<Long, TaiKhoanNganHang> trakhoanNganHangTheoLo(List<PhieuDoiSoat> dsPhieu) {
        Set<Long> maTaiKhoanCanTra = dsPhieu.stream()
                .map(PhieuDoiSoat::getMaTaiKhoan)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (maTaiKhoanCanTra.isEmpty()) {
            return Map.of();
        }
        return taiKhoanNganHangKho.findAllById(maTaiKhoanCanTra).stream()
                .collect(Collectors.toMap(TaiKhoanNganHang::getMaTaiKhoan, tk -> tk));
    }

    /**
     * Tương tự {@link #trakhoanNganHangTheoLo}, nhưng cho NguoiDung (giảng viên) từ
     * tập tài khoản ngân hàng đã tra.
     */
    private Map<Long, NguoiDung> giangVienTheoLo(Collection<TaiKhoanNganHang> dsTaiKhoan) {
        Set<Long> maGiangVienCanTra = dsTaiKhoan.stream()
                .map(TaiKhoanNganHang::getMaGiangVien)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (maGiangVienCanTra.isEmpty()) {
            return Map.of();
        }
        return quanTriHeThongKho.findAllById(maGiangVienCanTra).stream()
                .collect(Collectors.toMap(NguoiDung::getMaNguoiDung, nd -> nd));
    }

    @Override
    public PhanHoiPhieuDoiSoat capNhatPhieuDoiSoat(Long maDoiSoat, YeuCauCapNhatPhieuDoiSoat yeuCau) {
        validateYeuCauCapNhat(yeuCau);

        PhieuDoiSoat phieu = phieuDoiSoatKho.findById(maDoiSoat)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("phiếu đối soát", maDoiSoat));

        apDungCapNhatPhieuDoiSoat(phieu, yeuCau);
        return sangDto(phieu);
    }

    @Override
    public List<PhanHoiPhieuDoiSoat> capNhatHangLoatPhieuDoiSoat(List<Long> dsMaDoiSoat,
            YeuCauCapNhatPhieuDoiSoat yeuCau) {
        if (dsMaDoiSoat == null || dsMaDoiSoat.isEmpty()) {
            throw new NgoaiLeDuLieuKhongHopLe("Vui lòng chọn ít nhất 1 phiếu đối soát");
        }
        validateYeuCauCapNhat(yeuCau);

        // Tra cả lô phiếu bằng 1 câu findAllById, thay vì gọi capNhatPhieuDoiSoat
        // (tức findById + sangDto riêng lẻ) cho từng mã như trước — tránh N+1.
        Map<Long, PhieuDoiSoat> phieuTheoMa = phieuDoiSoatKho.findAllById(dsMaDoiSoat).stream()
                .collect(Collectors.toMap(PhieuDoiSoat::getMaDoiSoat, p -> p));
        for (Long ma : dsMaDoiSoat) {
            if (!phieuTheoMa.containsKey(ma)) {
                throw NgoaiLeKhongTimThay.cuaTaiNguyen("phiếu đối soát", ma);
            }
        }

        List<PhieuDoiSoat> dsPhieu = dsMaDoiSoat.stream().map(phieuTheoMa::get).toList();
        dsPhieu.forEach(phieu -> apDungCapNhatPhieuDoiSoat(phieu, yeuCau));

        Map<Long, TaiKhoanNganHang> taiKhoanTheoMa = trakhoanNganHangTheoLo(dsPhieu);
        Map<Long, NguoiDung> giangVienTheoMa = giangVienTheoLo(taiKhoanTheoMa.values());

        return dsPhieu.stream()
                .map(p -> sangDto(p, taiKhoanTheoMa, giangVienTheoMa))
                .toList();
    }

    private void validateYeuCauCapNhat(YeuCauCapNhatPhieuDoiSoat yeuCau) {
        if (yeuCau == null || yeuCau.trangThai() == null || yeuCau.trangThai().isBlank()) {
            throw new NgoaiLeDuLieuKhongHopLe("Thiếu trạng thái cập nhật phiếu đối soát");
        }
    }

    /**
     * Áp dụng thay đổi trạng thái/mã giao dịch lên MỘT phiếu (đối tượng đã được
     * quản lý bởi JPA, tự lưu khi transaction commit).
     */
    private void apDungCapNhatPhieuDoiSoat(PhieuDoiSoat phieu, YeuCauCapNhatPhieuDoiSoat yeuCau) {
        if (phieu.getTrangThai() == TrangThaiDoiSoat.HOAN_TAT) {
            throw new NgoaiLeDuLieuKhongHopLe("Phiếu đối soát đã hoàn tất chi trả, không thể thay đổi trạng thái");
        }

        TrangThaiDoiSoat trangThaiMoi = phanTichTrangThaiDoiSoat(yeuCau.trangThai());
        phieu.setTrangThai(trangThaiMoi);
        if (yeuCau.maGiaoDichNganHang() != null && !yeuCau.maGiaoDichNganHang().isBlank()) {
            phieu.setMaGiaoDichNganHang(yeuCau.maGiaoDichNganHang().trim());
        }

        if (trangThaiMoi == TrangThaiDoiSoat.HOAN_TAT) {
            phieu.setNgayChuyenThanhCong(LocalDateTime.now());

            TaiKhoanNganHang tk = taiKhoanNganHangKho.findById(phieu.getMaTaiKhoan())
                    .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("tài khoản ngân hàng", phieu.getMaTaiKhoan()));
            taoThongBao(tk.getMaGiangVien(), LoaiThongBao.DOI_SOAT_HOAN_TAT,
                    "Đã chuyển tiền thành công",
                    "Chúng tôi đã chuyển tiền cho bạn thành công. Vui lòng kiểm tra tài khoản ngân hàng của bạn.",
                    phieu.getMaDoiSoat().toString());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiGiaoDichDoanhThu> layGiaoDichTheoDoiSoat(Long maDoiSoat) {
        List<PhanHoiGiaoDichDoanhThu> ketQua = new ArrayList<>();

        for (PhieuMuaKhoaHoc p : phieuMuaKhoaHocKho.findByMaDoiSoat(maDoiSoat)) {
            KhoaHoc kh = khoaHocKho.findById(p.getMaKhoaHoc()).orElse(null);
            Long maGiangVien = kh != null ? kh.getMaTacGia() : null;
            String tenGiangVien = maGiangVien != null
                    ? quanTriHeThongKho.findById(maGiangVien).map(NguoiDung::getHoTen).orElse(null)
                    : null;
            LocalDateTime ngay = p.getNgayMuaThanhCong() != null ? p.getNgayMuaThanhCong() : p.getNgayTao();
            ketQua.add(new PhanHoiGiaoDichDoanhThu(
                    p.getMaPhieu(), "KHOA_HOC", ngay, p.getMaKhoaHoc(),
                    kh != null ? kh.getTieuDe() : "Khóa học #" + p.getMaKhoaHoc(),
                    null, maGiangVien, tenGiangVien, p.getSoTien(), p.getTrangThai().name(), true));
        }

        for (PhieuTraPhiLopHoc p : phieuTraPhiLopHocKho.findByMaDoiSoat(maDoiSoat)) {
            LopHoc lop = lopHocKho.findById(p.getMaLopHoc()).orElse(null);
            KhoaHoc kh = lop != null ? khoaHocKho.findById(lop.getMaKhoaHoc()).orElse(null) : null;
            Long maGiangVien = lop != null ? lop.getMaGiangVien() : null;
            String tenGiangVien = maGiangVien != null
                    ? quanTriHeThongKho.findById(maGiangVien).map(NguoiDung::getHoTen).orElse(null)
                    : null;
            ketQua.add(new PhanHoiGiaoDichDoanhThu(
                    p.getMaPhieu(), "LOP_HOC", p.getNgayTao(), lop != null ? lop.getMaKhoaHoc() : null,
                    kh != null ? kh.getTieuDe() : "(không rõ khóa học)",
                    lop != null ? lop.getMaLop() : null, maGiangVien, tenGiangVien, p.getSoTien(),
                    p.getTrangThai().name(), true));
        }

        ketQua.sort(Comparator.comparing(PhanHoiGiaoDichDoanhThu::ngayGiaoDich).reversed());
        return ketQua;
    }

    /**
     * Gộp danh sách phiếu đối soát (đã lọc theo cùng tiêu chí với màn hình
     * theo-phiếu) thành báo cáo theo giảng viên — mỗi giảng viên 1 dòng
     * tổng, phục vụ đúng nhu cầu "tổng tiền cần chuyển cho giảng viên vào
     * cuối tháng hoặc một khoảng thời gian tùy chọn".
     */
    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiTongHopDoiSoatTheoGiangVien> layTongHopDoiSoatTheoGiangVien(
            String trangThai, String timKiem, LocalDate tuNgay, LocalDate denNgay) {

        List<PhanHoiPhieuDoiSoat> daLoc = locPhieuDoiSoat(trangThai, timKiem, tuNgay, denNgay);

        Map<Long, List<PhanHoiPhieuDoiSoat>> theoGiangVien = daLoc.stream()
                .filter(p -> p.maGiangVien() != null)
                .collect(Collectors.groupingBy(PhanHoiPhieuDoiSoat::maGiangVien, LinkedHashMap::new,
                        Collectors.toList()));

        List<PhanHoiTongHopDoiSoatTheoGiangVien> ketQua = new ArrayList<>();
        for (Map.Entry<Long, List<PhanHoiPhieuDoiSoat>> entry : theoGiangVien.entrySet()) {
            List<PhanHoiPhieuDoiSoat> ds = entry.getValue();
            PhanHoiPhieuDoiSoat mauDauTien = ds.get(0);

            BigDecimal tongDoanhThu = ds.stream().map(PhanHoiPhieuDoiSoat::tongDoanhThu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tongSoTienThucTe = ds.stream().map(PhanHoiPhieuDoiSoat::soTienThucTe)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<PhanHoiPhieuDoiSoat> chuaHoanTat = ds.stream()
                    .filter(p -> p.trangThai() != TrangThaiDoiSoat.HOAN_TAT)
                    .toList();
            BigDecimal tongCanChuyen = chuaHoanTat.stream().map(PhanHoiPhieuDoiSoat::soTienThucTe)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            List<Long> dsMaChuaHoanTat = chuaHoanTat.stream()
                    .map(PhanHoiPhieuDoiSoat::maDoiSoat)
                    .toList();

            ketQua.add(new PhanHoiTongHopDoiSoatTheoGiangVien(
                    entry.getKey(),
                    mauDauTien.tenGiangVien(),
                    mauDauTien.tenChuTaiKhoan(),
                    mauDauTien.soTaiKhoan(),
                    mauDauTien.tenNganHang(),
                    ds.size(),
                    tongDoanhThu,
                    tongSoTienThucTe,
                    chuaHoanTat.size(),
                    tongCanChuyen,
                    dsMaChuaHoanTat));
        }

        // Giảng viên còn nhiều tiền cần chuyển nhất hiện lên đầu — ưu tiên xử lý trước.
        ketQua.sort(Comparator.comparing(PhanHoiTongHopDoiSoatTheoGiangVien::tongCanChuyen).reversed());
        return ketQua;
    }

    /**
     * Xem chi tiết toàn bộ phiếu của 1 giảng viên trong báo cáo tổng hợp.
     * Tái dùng {@link #locPhieuDoiSoat} (đã tối ưu tra theo lô) rồi lọc lại
     * đúng giảng viên cần xem — không thêm truy vấn CSDL nào khác.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiPhieuDoiSoat> layChiTietPhieuDoiSoatTheoGiangVien(
            Long maGiangVien, String trangThai, LocalDate tuNgay, LocalDate denNgay) {
        return locPhieuDoiSoat(trangThai, null, tuNgay, denNgay).stream()
                .filter(p -> maGiangVien.equals(p.maGiangVien()))
                .toList();
    }

    // ────────────────────────── Doanh thu — Giảng viên (tự xem của mình)
    // ──────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PhanHoiDoanhThuGiangVien layDoanhThuCuaGiangVien(Long maGiangVien) {
        List<Long> maKhoaHocList = khoaHocKho.findByMaTacGia(maGiangVien).stream()
                .map(KhoaHoc::getMaKhoaHoc)
                .toList();
        List<Long> maLopHocList = lopHocKho.findByMaGiangVien(maGiangVien).stream()
                .map(LopHoc::getMaLopHoc)
                .toList();

        BigDecimal doanhThuKhoaHoc = maKhoaHocList.isEmpty() ? BigDecimal.ZERO
                : phieuMuaKhoaHocKho.findByMaKhoaHocIn(maKhoaHocList).stream()
                        .filter(PhieuMuaKhoaHoc::daThanhCong)
                        .map(PhieuMuaKhoaHoc::getSoTien)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal doanhThuLopHoc = maLopHocList.isEmpty() ? BigDecimal.ZERO
                : phieuTraPhiLopHocKho.findByMaLopHocIn(maLopHocList).stream()
                        .filter(PhieuTraPhiLopHoc::daThanhCong)
                        .map(PhieuTraPhiLopHoc::getSoTien)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Long> maTaiKhoanList = taiKhoanNganHangKho.findByMaGiangVien(maGiangVien).stream()
                .map(tk -> tk.getMaTaiKhoan())
                .toList();

        List<PhieuDoiSoat> phieuDoiSoatCuaToi = maTaiKhoanList.isEmpty()
                ? List.of()
                : phieuDoiSoatKho.findByMaTaiKhoanIn(maTaiKhoanList);

        BigDecimal tongThucNhan = phieuDoiSoatCuaToi.stream()
                .filter(p -> p.getTrangThai() == TrangThaiDoiSoat.HOAN_TAT)
                .map(PhieuDoiSoat::getSoTienThucTe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PhanHoiDoanhThuGiangVien(
                maKhoaHocList.size(),
                maLopHocList.size(),
                doanhThuKhoaHoc,
                doanhThuLopHoc,
                anToanCong(doanhThuKhoaHoc, doanhThuLopHoc),
                tongThucNhan,
                demTheoTrangThai(phieuDoiSoatCuaToi, TrangThaiDoiSoat.CHO_XAC_NHAN),
                demTheoTrangThai(phieuDoiSoatCuaToi, TrangThaiDoiSoat.DANG_XU_LY),
                demTheoTrangThai(phieuDoiSoatCuaToi, TrangThaiDoiSoat.HOAN_TAT),
                demTheoTrangThai(phieuDoiSoatCuaToi, TrangThaiDoiSoat.THAT_BAI),
                !maTaiKhoanList.isEmpty());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiPhieuDoiSoat> layDanhSachPhieuDoiSoatCuaGiangVien(Long maGiangVien, String trangThai,
            Pageable pageable) {
        List<Long> maTaiKhoanList = taiKhoanNganHangKho.findByMaGiangVien(maGiangVien).stream()
                .map(tk -> tk.getMaTaiKhoan())
                .toList();
        if (maTaiKhoanList.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<PhieuDoiSoat> page = (trangThai == null || trangThai.isBlank())
                ? phieuDoiSoatKho.findByMaTaiKhoanIn(maTaiKhoanList, pageable)
                : phieuDoiSoatKho.findByMaTaiKhoanInAndTrangThai(maTaiKhoanList, phanTichTrangThaiDoiSoat(trangThai),
                        pageable);

        return page.map(this::sangDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiPhieuDangChoHoanThanhLop> layDanhSachDangChoHoanThanhLop(Long maGiangVien) {
        LocalDateTime hienTai = LocalDateTime.now();
        List<LopHoc> lopCuaGiangVien = lopHocKho.findByMaGiangVien(maGiangVien);
        Map<Long, LopHoc> lopTheoMa = lopCuaGiangVien.stream()
                .collect(java.util.stream.Collectors.toMap(LopHoc::getMaLopHoc, l -> l));

        List<PhanHoiPhieuDangChoHoanThanhLop> ketQua = new java.util.ArrayList<>();
        for (PhieuTraPhiLopHoc phieu : phieuTraPhiLopHocKho
                .findByMaLopHocIn(lopCuaGiangVien.stream().map(LopHoc::getMaLopHoc).toList())) {
            boolean daThanhCong = phieu.getTrangThai() == TrangThaiGiaoDich.THANH_CONG;
            boolean chuaDoiSoat = phieu.getMaDoiSoat() == null;
            boolean daHetHanGiu = phieu.getHanGiuTien() == null || !phieu.getHanGiuTien().isAfter(hienTai);
            if (!daThanhCong || !chuaDoiSoat || !daHetHanGiu) {
                continue;
            }
            LopHoc lop = lopTheoMa.get(phieu.getMaLopHoc());
            if (lop == null)
                continue;
            boolean daHoanThanh = lop.getTrangThai() == TrangThaiLop.DA_DONG
                    || (lop.getNgayKetThuc() != null && !lop.getNgayKetThuc().isAfter(hienTai));
            if (daHoanThanh) {
                continue; // đủ điều kiện rồi — sẽ được job đối soát xử lý ở lượt chạy tới, không phải
                          // "đang chờ"
            }

            String tenKhoaHoc = khoaHocKho.findById(lop.getMaKhoaHoc()).map(KhoaHoc::getTieuDe)
                    .orElse("Khóa học #" + lop.getMaKhoaHoc());
            ketQua.add(new PhanHoiPhieuDangChoHoanThanhLop(
                    phieu.getMaPhieu(), lop.getMaLopHoc(), lop.getMaLop(), tenKhoaHoc,
                    phieu.getSoTien(), lop.getTrangThai().name(), lop.getNgayKetThuc()));
        }
        return ketQua;
    }

    private long demTheoTrangThai(List<PhieuDoiSoat> danhSach, TrangThaiDoiSoat trangThai) {
        return danhSach.stream().filter(p -> p.getTrangThai() == trangThai).count();
    }

    // ────────────────────────── Hoàn tiền — Khóa học ──────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiHoanTienChiTiet> layDanhSachHoanTienKhoaHoc(String trangThai, Pageable pageable) {
        Page<PhieuHoanTienKhoaHoc> page = (trangThai == null || trangThai.isBlank())
                ? phieuHoanTienKhoaHocKho.findAll(pageable)
                : phieuHoanTienKhoaHocKho.findByTrangThai(phanTichTrangThaiHoanTien(trangThai), pageable);

        return page.map(this::sangDtoHoanTienKhoaHoc);
    }

    @Override
    public PhanHoiHoanTienChiTiet xuLyHoanTienKhoaHoc(Long maHoanTien, Long maNguoiDungAdmin,
            YeuCauXuLyHoanTien yeuCau) {
        TrangThaiHoanTien trangThaiMoi = phanTichQuyetDinhHoanTien(yeuCau);

        PhieuHoanTienKhoaHoc hoanTien = phieuHoanTienKhoaHocKho.findById(maHoanTien)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("yêu cầu hoàn tiền khóa học", maHoanTien));
        if (hoanTien.getTrangThai() != TrangThaiHoanTien.CHO_XAC_NHAN) {
            throw new NgoaiLeDuLieuKhongHopLe("Yêu cầu hoàn tiền này đã được xử lý trước đó");
        }

        PhieuMuaKhoaHoc phieu = phieuMuaKhoaHocKho.findById(hoanTien.getMaPhieuGiaoDich())
                .orElseThrow(
                        () -> NgoaiLeKhongTimThay.cuaTaiNguyen("phiếu mua khóa học", hoanTien.getMaPhieuGiaoDich()));

        if (trangThaiMoi == TrangThaiHoanTien.HOAN_TAT) {
            if (phieu.getMaDoiSoat() != null) {
                throw new NgoaiLeDuLieuKhongHopLe(
                        "Giao dịch đã được đối soát chi trả cho giảng viên — không thể tự động hoàn tiền, vui lòng xử lý thủ công");
            }

            // Gọi thật API hoàn tiền của VNPay TRƯỚC khi đổi trạng thái nội bộ —
            // nếu VNPay từ chối, dừng lại ở đây, không đụng tới dữ liệu.
            KetQuaHoanTienVnpay ketQuaVnpay = adapterVnpayHoanTien.hoanTien(
                    phieu.getMaGiaoDichVnpay(), hoanTien.getSoTien(),
                    (String) phieu.getDuLieuDong().get("vnpTransactionNo"),
                    (String) phieu.getDuLieuDong().get("vnpPayDate"),
                    String.valueOf(maNguoiDungAdmin), hoanTien.getLyDo(), true);
            if (!ketQuaVnpay.thanhCong()) {
                throw new NgoaiLeDuLieuKhongHopLe("VNPay từ chối yêu cầu hoàn tiền: " + ketQuaVnpay.thongDiep());
            }
            hoanTien.getDuLieuDong().put("vnpRefundResponseCode", ketQuaVnpay.maPhanHoi());
        }

        ghiNhanQuyetDinh(hoanTien.getDuLieuDong(), hoanTien::setTrangThai, hoanTien::setNgayXuLy, trangThaiMoi, yeuCau);

        if (trangThaiMoi == TrangThaiHoanTien.HOAN_TAT) {
            phieu.setTrangThai(TrangThaiGiaoDich.HOAN_TIEN);

            // Thu hồi quyền sử dụng khóa học (Khóa thứ nhất trong cơ chế Khóa kép)
            quyenSuDungKho.findByMaNguoiDungAndMaKhoaHoc(phieu.getMaNguoiDung(), phieu.getMaKhoaHoc())
                    .ifPresent(quyenSuDungKho::delete);

            // Gỡ khỏi mọi lớp học đang tham gia dựa trên khóa học vừa bị thu hồi
            for (ThanhVienLop tv : thanhVienLopKho.findByMaHocVien(phieu.getMaNguoiDung())) {
                LopHoc lop = lopHocKho.findById(tv.getMaLopHoc()).orElse(null);
                if (lop != null && lop.getMaKhoaHoc().equals(phieu.getMaKhoaHoc())) {
                    thanhVienLopKho.delete(tv);
                }
            }

            taoThongBao(phieu.getMaNguoiDung(), LoaiThongBao.HOAN_TIEN_THANH_CONG,
                    "Yêu cầu hoàn tiền đã được chấp nhận",
                    "Giao dịch " + phieu.getMaGiaoDichVnpay() + " đã được hoàn tiền thành công.",
                    phieu.getMaGiaoDichVnpay());
        } else {
            taoThongBao(phieu.getMaNguoiDung(), LoaiThongBao.HOAN_TIEN_TU_CHOI,
                    "Yêu cầu hoàn tiền bị từ chối",
                    "Yêu cầu hoàn tiền cho giao dịch " + phieu.getMaGiaoDichVnpay() + " đã bị từ chối.",
                    phieu.getMaGiaoDichVnpay());
        }

        return sangDtoHoanTienKhoaHoc(hoanTien);
    }

    // ────────────────────────── Hoàn tiền — Lớp học ──────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiHoanTienChiTiet> layDanhSachHoanTienLopHoc(String trangThai, Pageable pageable) {
        Page<PhieuHoanTienLopHoc> page = (trangThai == null || trangThai.isBlank())
                ? phieuHoanTienLopHocKho.findAll(pageable)
                : phieuHoanTienLopHocKho.findByTrangThai(phanTichTrangThaiHoanTien(trangThai), pageable);

        return page.map(this::sangDtoHoanTienLopHoc);
    }

    @Override
    public PhanHoiHoanTienChiTiet xuLyHoanTienLopHoc(Long maHoanTien, Long maNguoiDungAdmin,
            YeuCauXuLyHoanTien yeuCau) {
        TrangThaiHoanTien trangThaiMoi = phanTichQuyetDinhHoanTien(yeuCau);

        PhieuHoanTienLopHoc hoanTien = phieuHoanTienLopHocKho.findById(maHoanTien)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("yêu cầu hoàn tiền lớp học", maHoanTien));
        if (hoanTien.getTrangThai() != TrangThaiHoanTien.CHO_XAC_NHAN) {
            throw new NgoaiLeDuLieuKhongHopLe("Yêu cầu hoàn tiền này đã được xử lý trước đó");
        }

        PhieuTraPhiLopHoc phieu = phieuTraPhiLopHocKho.findById(hoanTien.getMaPhieuGiaoDich())
                .orElseThrow(
                        () -> NgoaiLeKhongTimThay.cuaTaiNguyen("phiếu phí lớp học", hoanTien.getMaPhieuGiaoDich()));

        if (trangThaiMoi == TrangThaiHoanTien.HOAN_TAT) {
            if (phieu.getMaDoiSoat() != null) {
                throw new NgoaiLeDuLieuKhongHopLe(
                        "Giao dịch đã được đối soát chi trả cho giảng viên — không thể tự động hoàn tiền, vui lòng xử lý thủ công");
            }

            KetQuaHoanTienVnpay ketQuaVnpay = adapterVnpayHoanTien.hoanTien(
                    phieu.getMaGiaoDichVnpay(), hoanTien.getSoTien(),
                    (String) phieu.getDuLieuDong().get("vnpTransactionNo"),
                    (String) phieu.getDuLieuDong().get("vnpPayDate"),
                    String.valueOf(maNguoiDungAdmin), hoanTien.getLyDo(), true);
            if (!ketQuaVnpay.thanhCong()) {
                throw new NgoaiLeDuLieuKhongHopLe("VNPay từ chối yêu cầu hoàn tiền: " + ketQuaVnpay.thongDiep());
            }
            hoanTien.getDuLieuDong().put("vnpRefundResponseCode", ketQuaVnpay.maPhanHoi());
        }

        ghiNhanQuyetDinh(hoanTien.getDuLieuDong(), hoanTien::setTrangThai, hoanTien::setNgayXuLy, trangThaiMoi, yeuCau);

        if (trangThaiMoi == TrangThaiHoanTien.HOAN_TAT) {
            phieu.setTrangThai(TrangThaiGiaoDich.HOAN_TIEN);

            // Gỡ khỏi lớp học tương ứng
            if (thanhVienLopKho.existsByMaHocVienAndMaLopHoc(phieu.getMaNguoiDung(), phieu.getMaLopHoc())) {
                thanhVienLopKho.findByMaHocVien(phieu.getMaNguoiDung()).stream()
                        .filter(tv -> tv.getMaLopHoc().equals(phieu.getMaLopHoc()))
                        .findFirst()
                        .ifPresent(thanhVienLopKho::delete);
            }

            taoThongBao(phieu.getMaNguoiDung(), LoaiThongBao.HOAN_TIEN_THANH_CONG,
                    "Yêu cầu hoàn tiền đã được chấp nhận",
                    "Giao dịch " + phieu.getMaGiaoDichVnpay() + " đã được hoàn tiền thành công.",
                    phieu.getMaGiaoDichVnpay());
        } else {
            taoThongBao(phieu.getMaNguoiDung(), LoaiThongBao.HOAN_TIEN_TU_CHOI,
                    "Yêu cầu hoàn tiền bị từ chối",
                    "Yêu cầu hoàn tiền cho giao dịch " + phieu.getMaGiaoDichVnpay() + " đã bị từ chối.",
                    phieu.getMaGiaoDichVnpay());
        }

        return sangDtoHoanTienLopHoc(hoanTien);
    }

    // ────────────────────────── Tiện ích chung ──────────────────────────

    private TrangThaiHoanTien phanTichQuyetDinhHoanTien(YeuCauXuLyHoanTien yeuCau) {
        if (yeuCau == null || yeuCau.trangThai() == null || yeuCau.trangThai().isBlank()) {
            throw new NgoaiLeDuLieuKhongHopLe("Thiếu quyết định xử lý hoàn tiền");
        }
        TrangThaiHoanTien trangThai = phanTichTrangThaiHoanTien(yeuCau.trangThai());
        if (trangThai != TrangThaiHoanTien.HOAN_TAT && trangThai != TrangThaiHoanTien.TU_CHOI) {
            throw new NgoaiLeDuLieuKhongHopLe("Chỉ có thể duyệt (HOAN_TAT) hoặc từ chối (TU_CHOI) yêu cầu hoàn tiền");
        }
        return trangThai;
    }

    private void ghiNhanQuyetDinh(Map<String, Object> duLieuDong,
            java.util.function.Consumer<TrangThaiHoanTien> setTrangThai,
            java.util.function.Consumer<LocalDateTime> setNgayXuLy,
            TrangThaiHoanTien trangThaiMoi, YeuCauXuLyHoanTien yeuCau) {
        setTrangThai.accept(trangThaiMoi);
        setNgayXuLy.accept(LocalDateTime.now());
        if (yeuCau.ghiChu() != null && !yeuCau.ghiChu().isBlank()) {
            duLieuDong.put("ghiChuAdmin", yeuCau.ghiChu().trim());
        }
    }

    private void taoThongBao(Long maNguoiDung, LoaiThongBao loai, String tieuDe, String noiDung, String maGiaoDich) {
        thongBaoDichVu.guiThongBao(maNguoiDung, loai, tieuDe, noiDung, Map.of("maGiaoDich", maGiaoDich));
    }

    private PhanHoiHoanTienChiTiet sangDtoHoanTienKhoaHoc(PhieuHoanTienKhoaHoc hoanTien) {
        PhieuMuaKhoaHoc phieu = phieuMuaKhoaHocKho.findById(hoanTien.getMaPhieuGiaoDich()).orElse(null);
        Long maNguoiDung = phieu != null ? phieu.getMaNguoiDung() : layTruong(hoanTien.getDuLieuDong(), "maNguoiDung");
        NguoiDung nguoiDung = maNguoiDung != null ? quanTriHeThongKho.findById(maNguoiDung).orElse(null) : null;
        String tieuDe = phieu != null
                ? String.valueOf(
                        phieu.getDuLieuDong().getOrDefault("tieuDeKhoaHoc", "Khóa học #" + phieu.getMaKhoaHoc()))
                : "(Không tìm thấy giao dịch gốc)";

        return new PhanHoiHoanTienChiTiet(
                hoanTien.getMaHoanTien(), hoanTien.getMaPhieuGiaoDich(), "KHOA_HOC",
                maNguoiDung, nguoiDung != null ? nguoiDung.getHoTen() : null,
                nguoiDung != null ? nguoiDung.getEmail() : null,
                tieuDe, phieu != null ? phieu.getMaGiaoDichVnpay() : null,
                hoanTien.getSoTien(), hoanTien.getLyDo(), hoanTien.getTrangThai(),
                (String) hoanTien.getDuLieuDong().get("ghiChuAdmin"),
                hoanTien.getNgayTao(), hoanTien.getNgayXuLy());
    }

    private PhanHoiHoanTienChiTiet sangDtoHoanTienLopHoc(PhieuHoanTienLopHoc hoanTien) {
        PhieuTraPhiLopHoc phieu = phieuTraPhiLopHocKho.findById(hoanTien.getMaPhieuGiaoDich()).orElse(null);
        Long maNguoiDung = phieu != null ? phieu.getMaNguoiDung() : layTruong(hoanTien.getDuLieuDong(), "maNguoiDung");
        NguoiDung nguoiDung = maNguoiDung != null ? quanTriHeThongKho.findById(maNguoiDung).orElse(null) : null;
        String tieuDe = "(Không tìm thấy giao dịch gốc)";
        if (phieu != null) {
            LopHoc lop = lopHocKho.findById(phieu.getMaLopHoc()).orElse(null);
            String maLop = String.valueOf(phieu.getDuLieuDong().getOrDefault("maLop", "#" + phieu.getMaLopHoc()));
            tieuDe = "Phí lớp học " + maLop + (lop != null ? " — " + tenKhoaHocCuaLop(lop) : "");
        }

        return new PhanHoiHoanTienChiTiet(
                hoanTien.getMaHoanTien(), hoanTien.getMaPhieuGiaoDich(), "LOP_HOC",
                maNguoiDung, nguoiDung != null ? nguoiDung.getHoTen() : null,
                nguoiDung != null ? nguoiDung.getEmail() : null,
                tieuDe, phieu != null ? phieu.getMaGiaoDichVnpay() : null,
                hoanTien.getSoTien(), hoanTien.getLyDo(), hoanTien.getTrangThai(),
                (String) hoanTien.getDuLieuDong().get("ghiChuAdmin"),
                hoanTien.getNgayTao(), hoanTien.getNgayXuLy());
    }

    private String tenKhoaHocCuaLop(LopHoc lop) {
        KhoaHoc kh = khoaHocKho.findById(lop.getMaKhoaHoc()).orElse(null);
        return kh != null ? kh.getTieuDe() : null;
    }

    private Long layTruong(Map<String, Object> duLieuDong, String truong) {
        Object gt = duLieuDong.get(truong);
        if (gt == null)
            return null;
        try {
            return Long.valueOf(String.valueOf(gt));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Dùng cho trường hợp 1 phiếu đơn lẻ (vd. sau khi cập nhật trạng thái) — không
     * cần tra theo lô.
     */
    private PhanHoiPhieuDoiSoat sangDto(PhieuDoiSoat phieu) {
        TaiKhoanNganHang taiKhoan = taiKhoanNganHangKho.findById(phieu.getMaTaiKhoan()).orElse(null);
        Map<Long, TaiKhoanNganHang> taiKhoanTheoMa = taiKhoan != null
                ? Map.of(taiKhoan.getMaTaiKhoan(), taiKhoan)
                : Map.of();
        Map<Long, NguoiDung> giangVienTheoMa = giangVienTheoLo(taiKhoanTheoMa.values());
        return sangDto(phieu, taiKhoanTheoMa, giangVienTheoMa);
    }

    /**
     * Ánh xạ PhieuDoiSoat -> DTO bằng cách tra trong 2 Map đã tải theo LÔ từ
     * trước (xem {@link #trakhoanNganHangTheoLo}/{@link #giangVienTheoLo}) —
     * không gọi thêm bất kỳ câu truy vấn CSDL nào ở đây, tránh N+1 khi ánh xạ
     * cả danh sách nhiều phiếu.
     */
    private PhanHoiPhieuDoiSoat sangDto(
            PhieuDoiSoat phieu, Map<Long, TaiKhoanNganHang> taiKhoanTheoMa, Map<Long, NguoiDung> giangVienTheoMa) {
        TaiKhoanNganHang taiKhoan = taiKhoanTheoMa.get(phieu.getMaTaiKhoan());
        NguoiDung giangVien = taiKhoan != null ? giangVienTheoMa.get(taiKhoan.getMaGiangVien()) : null;

        BigDecimal chenhLech = phieu.getTongDoanhThu().subtract(phieu.getSoTienThucTe());
        return new PhanHoiPhieuDoiSoat(
                phieu.getMaDoiSoat(),
                phieu.getMaTaiKhoan(),
                taiKhoan != null ? taiKhoan.getMaGiangVien() : null,
                giangVien != null ? giangVien.getHoTen() : null,
                taiKhoan != null ? taiKhoan.getTenChuTaiKhoan() : null,
                taiKhoan != null ? taiKhoan.getSoTaiKhoan() : null,
                taiKhoan != null ? taiKhoan.getTenNganHang() : null,
                phieu.getHoaHong(),
                phieu.getTongDoanhThu(),
                phieu.getSoTienThucTe(),
                chenhLech,
                phieu.getTrangThai(),
                phieu.getMaGiaoDichNganHang(),
                phieu.getNgayTao(),
                phieu.getNgayChuyenThanhCong());
    }

    private TrangThaiDoiSoat phanTichTrangThaiDoiSoat(String trangThai) {
        try {
            return TrangThaiDoiSoat.valueOf(trangThai.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new NgoaiLeDuLieuKhongHopLe("Trạng thái đối soát không hợp lệ: " + trangThai);
        }
    }

    private TrangThaiHoanTien phanTichTrangThaiHoanTien(String trangThai) {
        try {
            return TrangThaiHoanTien.valueOf(trangThai.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new NgoaiLeDuLieuKhongHopLe("Trạng thái hoàn tiền không hợp lệ: " + trangThai);
        }
    }

    private BigDecimal anToanCong(BigDecimal a, BigDecimal b) {
        return (a == null ? BigDecimal.ZERO : a).add(b == null ? BigDecimal.ZERO : b);
    }

    // ────────────────────────── UC-16: Lọc / biểu đồ / xuất CSV doanh thu
    // ──────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiGiaoDichDoanhThu> layGiaoDichDoanhThuGiangVien(
            Long maGiangVien, LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc) {
        List<Long> maKhoaHocList = khoaHocKho.findByMaTacGia(maGiangVien).stream().map(KhoaHoc::getMaKhoaHoc).toList();
        List<Long> maLopHocList = lopHocKho.findByMaGiangVien(maGiangVien).stream().map(LopHoc::getMaLopHoc).toList();
        return xayDungVaLocGiaoDich(maKhoaHocList, maLopHocList, tuNgay, denNgay, maKhoaHoc, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PhanHoiXuHuongDoanhThu layXuHuongDoanhThuGiangVien(
            Long maGiangVien, LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc) {

        // Lấy dữ liệu gốc
        // PhanHoiXuHuongDoanhThu xuHuongGoc = gomTheoThang(
        // layGiaoDichDoanhThuGiangVien(maGiangVien, tuNgay, denNgay, maKhoaHoc));

        // Trả về dữ liệu đã được lấp đầy
        // return lapDayCacThang(xuHuongGoc, tuNgay, denNgay);
        // Lấy danh sách giao dịch thô
        List<PhanHoiGiaoDichDoanhThu> danhSachGiaoDich = layGiaoDichDoanhThuGiangVien(maGiangVien, tuNgay, denNgay,
                maKhoaHoc);

        // Trả về biểu đồ đã gom và lấp đầy theo tuần
        return xuLyXuHuongTheoTuan(danhSachGiaoDich, tuNgay, denNgay);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiGiaoDichDoanhThu> layGiaoDichDoanhThuHeThong(
            LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc, Long maGiangVien) {
        List<Long> maKhoaHocList = null;
        List<Long> maLopHocList = null;
        if (maGiangVien != null) {
            maKhoaHocList = khoaHocKho.findByMaTacGia(maGiangVien).stream().map(KhoaHoc::getMaKhoaHoc).toList();
            maLopHocList = lopHocKho.findByMaGiangVien(maGiangVien).stream().map(LopHoc::getMaLopHoc).toList();
        }
        return xayDungVaLocGiaoDich(maKhoaHocList, maLopHocList, tuNgay, denNgay, maKhoaHoc, maGiangVien);
    }

    @Override
    @Transactional(readOnly = true)
    public PhanHoiXuHuongDoanhThu layXuHuongDoanhThuHeThong(
            LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc, Long maGiangVien) {

        // Lấy dữ liệu gốc của hệ thống
        // PhanHoiXuHuongDoanhThu xuHuongGoc = gomTheoThang(
        // layGiaoDichDoanhThuHeThong(tuNgay, denNgay, maKhoaHoc, maGiangVien));

        // Trả về dữ liệu đã được lấp đầy theo tháng
        // return lapDayCacThang(xuHuongGoc, tuNgay, denNgay);

        // 1. Lấy danh sách giao dịch thô của hệ thống (chưa gom nhóm)
        List<PhanHoiGiaoDichDoanhThu> danhSachGiaoDich = layGiaoDichDoanhThuHeThong(tuNgay, denNgay, maKhoaHoc,
                maGiangVien);

        // 2. Trả về dữ liệu đã được gom nhóm và lấp đầy theo tuần
        return xuLyXuHuongTheoTuan(danhSachGiaoDich, tuNgay, denNgay);
    }

    /**
     * @param maKhoaHocPham      phạm vi mã khóa học được phép lấy (null = không
     *                           giới hạn, dùng cho Admin xem toàn hệ thống)
     * @param maLopHocPham       phạm vi mã lớp học được phép lấy (null = không giới
     *                           hạn)
     * @param maKhoaHocLoc       lọc thêm theo 1 khóa học cụ thể (tùy chọn)
     * @param maGiangVienChiDinh chỉ dùng để gắn vào DTO khi phạm vi đã bị giới hạn
     *                           theo giảng viên; null nếu không giới hạn
     */
    private List<PhanHoiGiaoDichDoanhThu> xayDungVaLocGiaoDich(
            List<Long> maKhoaHocPham, List<Long> maLopHocPham,
            LocalDate tuNgay, LocalDate denNgay, Long maKhoaHocLoc, Long maGiangVienChiDinh) {

        LocalDateTime tuThoiDiem = tuNgay != null ? tuNgay.atStartOfDay() : null;
        LocalDateTime denThoiDiem = denNgay != null ? denNgay.atTime(23, 59, 59) : null;

        List<PhieuMuaKhoaHoc> dsMuaKhoaHoc = maKhoaHocPham == null
                ? phieuMuaKhoaHocKho.findByTrangThai(TrangThaiGiaoDich.THANH_CONG)
                : (maKhoaHocPham.isEmpty() ? List.of() : phieuMuaKhoaHocKho.findByMaKhoaHocIn(maKhoaHocPham));

        List<PhieuTraPhiLopHoc> dsPhiLopHoc = maLopHocPham == null
                ? phieuTraPhiLopHocKho.findByTrangThai(TrangThaiGiaoDich.THANH_CONG)
                : (maLopHocPham.isEmpty() ? List.of() : phieuTraPhiLopHocKho.findByMaLopHocIn(maLopHocPham));

        List<PhanHoiGiaoDichDoanhThu> ketQua = new ArrayList<>();

        Set<Long> maDoiSoatDaHoanTat = phieuDoiSoatKho.findByTrangThaiIn(List.of(TrangThaiDoiSoat.HOAN_TAT)).stream()
                .map(PhieuDoiSoat::getMaDoiSoat)
                .collect(Collectors.toSet());

        for (PhieuMuaKhoaHoc p : dsMuaKhoaHoc) {
            if (p.getTrangThai() != TrangThaiGiaoDich.THANH_CONG)
                continue;
            if (maKhoaHocLoc != null && !maKhoaHocLoc.equals(p.getMaKhoaHoc()))
                continue;
            LocalDateTime ngay = p.getNgayMuaThanhCong() != null ? p.getNgayMuaThanhCong() : p.getNgayTao();
            if (tuThoiDiem != null && ngay.isBefore(tuThoiDiem))
                continue;
            if (denThoiDiem != null && ngay.isAfter(denThoiDiem))
                continue;

            KhoaHoc kh = khoaHocKho.findById(p.getMaKhoaHoc()).orElse(null);
            Long maGiangVien = maGiangVienChiDinh != null ? maGiangVienChiDinh : (kh != null ? kh.getMaTacGia() : null);
            String tenGiangVien = maGiangVien != null
                    ? quanTriHeThongKho.findById(maGiangVien).map(NguoiDung::getHoTen).orElse(null)
                    : null;
            boolean daGiaiNgan = p.getMaDoiSoat() != null && maDoiSoatDaHoanTat.contains(p.getMaDoiSoat());

            ketQua.add(new PhanHoiGiaoDichDoanhThu(
                    p.getMaPhieu(), "KHOA_HOC", ngay, p.getMaKhoaHoc(),
                    kh != null ? kh.getTieuDe() : "Khóa học #" + p.getMaKhoaHoc(),
                    null, maGiangVien, tenGiangVien, p.getSoTien(), p.getTrangThai().name(), daGiaiNgan));
        }

        for (PhieuTraPhiLopHoc p : dsPhiLopHoc) {
            if (p.getTrangThai() != TrangThaiGiaoDich.THANH_CONG)
                continue;
            LopHoc lop = lopHocKho.findById(p.getMaLopHoc()).orElse(null);
            if (maKhoaHocLoc != null && (lop == null || !maKhoaHocLoc.equals(lop.getMaKhoaHoc())))
                continue;
            LocalDateTime ngay = p.getNgayTao();
            if (tuThoiDiem != null && ngay.isBefore(tuThoiDiem))
                continue;
            if (denThoiDiem != null && ngay.isAfter(denThoiDiem))
                continue;

            KhoaHoc kh = lop != null ? khoaHocKho.findById(lop.getMaKhoaHoc()).orElse(null) : null;
            Long maGiangVien = maGiangVienChiDinh != null ? maGiangVienChiDinh
                    : (lop != null ? lop.getMaGiangVien() : null);
            String tenGiangVien = maGiangVien != null
                    ? quanTriHeThongKho.findById(maGiangVien).map(NguoiDung::getHoTen).orElse(null)
                    : null;
            boolean daGiaiNgan = p.getMaDoiSoat() != null && maDoiSoatDaHoanTat.contains(p.getMaDoiSoat());

            ketQua.add(new PhanHoiGiaoDichDoanhThu(
                    p.getMaPhieu(), "LOP_HOC", ngay, lop != null ? lop.getMaKhoaHoc() : null,
                    kh != null ? kh.getTieuDe() : "(không rõ khóa học)",
                    lop != null ? lop.getMaLop() : null, maGiangVien, tenGiangVien, p.getSoTien(),
                    p.getTrangThai().name(), daGiaiNgan));
        }

        ketQua.sort(Comparator.comparing(PhanHoiGiaoDichDoanhThu::ngayGiaoDich).reversed());
        return ketQua;
    }

    // ────────────────────────── Màn Hình Tổng Quan: KPI + phân bổ + cảnh báo
    // ──────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PhanHoiTongQuanDoanhThu layTongQuanDoanhThuGiangVien(Long maGiangVien, LocalDate tuNgay, LocalDate denNgay) {
        List<Long> maKhoaHocList = khoaHocKho.findByMaTacGia(maGiangVien).stream().map(KhoaHoc::getMaKhoaHoc).toList();
        List<Long> maLopHocList = lopHocKho.findByMaGiangVien(maGiangVien).stream().map(LopHoc::getMaLopHoc).toList();
        List<PhanHoiGiaoDichDoanhThu> giaoDich = xayDungVaLocGiaoDich(maKhoaHocList, maLopHocList, tuNgay, denNgay,
                null, null);
        return tongHopTongQuan(giaoDich);
    }

    @Override
    @Transactional(readOnly = true)
    public PhanHoiTongQuanDoanhThu layTongQuanDoanhThuHeThong(
            LocalDate tuNgay, LocalDate denNgay, Long maKhoaHoc, Long maGiangVien) {
        List<Long> maKhoaHocList = null;
        List<Long> maLopHocList = null;
        if (maGiangVien != null) {
            maKhoaHocList = khoaHocKho.findByMaTacGia(maGiangVien).stream().map(KhoaHoc::getMaKhoaHoc).toList();
            maLopHocList = lopHocKho.findByMaGiangVien(maGiangVien).stream().map(LopHoc::getMaLopHoc).toList();
        }
        List<PhanHoiGiaoDichDoanhThu> giaoDich = xayDungVaLocGiaoDich(maKhoaHocList, maLopHocList, tuNgay, denNgay,
                maKhoaHoc, maGiangVien);
        return tongHopTongQuan(giaoDich);
    }

    private PhanHoiTongQuanDoanhThu tongHopTongQuan(List<PhanHoiGiaoDichDoanhThu> giaoDich) {
        BigDecimal tongGhiNhan = BigDecimal.ZERO;
        BigDecimal thucThu = BigDecimal.ZERO;
        Map<String, BigDecimal> tienTheoLoai = new LinkedHashMap<>();
        Map<String, Long> soLuongTheoLoai = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> tienTheoNgay = new TreeMap<>();

        for (PhanHoiGiaoDichDoanhThu gd : giaoDich) {
            tongGhiNhan = tongGhiNhan.add(gd.soTien());
            if (gd.daGiaiNgan()) {
                thucThu = thucThu.add(gd.soTien());
            }
            tienTheoLoai.merge(gd.loai(), gd.soTien(), BigDecimal::add);
            soLuongTheoLoai.merge(gd.loai(), 1L, Long::sum);
            LocalDate ngay = gd.ngayGiaoDich().toLocalDate();
            tienTheoNgay.merge(ngay, gd.soTien(), BigDecimal::add);
        }

        BigDecimal choDoiSoat = tongGhiNhan.subtract(thucThu);

        List<PhanHoiTongQuanDoanhThu.PhanBoTheoLoai> phanBo = tienTheoLoai.entrySet().stream()
                .map(e -> new PhanHoiTongQuanDoanhThu.PhanBoTheoLoai(
                        e.getKey(), e.getValue(), soLuongTheoLoai.getOrDefault(e.getKey(), 0L)))
                .toList();

        List<PhanHoiTongQuanDoanhThu.CanhBaoDoanhThu> canhBao = tinhCanhBaoBatThuong(tienTheoNgay);

        return new PhanHoiTongQuanDoanhThu(tongGhiNhan, thucThu, choDoiSoat, giaoDich.size(), phanBo, canhBao);
    }

    /**
     * Cảnh báo đơn giản: một ngày được coi là bất thường nếu doanh thu ngày
     * đó >= 2.5 lần trung bình các ngày còn lại trong kỳ đang xem, và kỳ
     * xem có ít nhất 3 ngày phát sinh giao dịch (tránh cảnh báo giả khi dữ
     * liệu quá ít để so sánh).
     */
    private List<PhanHoiTongQuanDoanhThu.CanhBaoDoanhThu> tinhCanhBaoBatThuong(
            Map<LocalDate, BigDecimal> tienTheoNgay) {
        if (tienTheoNgay.size() < 3) {
            return List.of();
        }
        BigDecimal tongTatCa = tienTheoNgay.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal trungBinh = tongTatCa.divide(BigDecimal.valueOf(tienTheoNgay.size()), 2, RoundingMode.HALF_UP);
        if (trungBinh.compareTo(BigDecimal.ZERO) == 0) {
            return List.of();
        }

        List<PhanHoiTongQuanDoanhThu.CanhBaoDoanhThu> ketQua = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> e : tienTheoNgay.entrySet()) {
            double lanSoVoiTrungBinh = e.getValue().divide(trungBinh, 2, RoundingMode.HALF_UP).doubleValue();
            if (lanSoVoiTrungBinh >= 2.5) {
                ketQua.add(new PhanHoiTongQuanDoanhThu.CanhBaoDoanhThu(e.getKey(), e.getValue(), lanSoVoiTrungBinh));
            }
        }
        ketQua.sort(Comparator.comparing(PhanHoiTongQuanDoanhThu.CanhBaoDoanhThu::ngay).reversed());
        return ketQua;
    }

    private PhanHoiXuHuongDoanhThu gomTheoThang(List<PhanHoiGiaoDichDoanhThu> giaoDich) {
        DateTimeFormatter dinhDang = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, BigDecimal> bang = new TreeMap<>();
        for (PhanHoiGiaoDichDoanhThu gd : giaoDich) {
            String nhan = gd.ngayGiaoDich().format(dinhDang);
            bang.merge(nhan, gd.soTien(), BigDecimal::add);
        }
        List<PhanHoiXuHuongDoanhThu.DiemXuHuong> diem = bang.entrySet().stream()
                .map(e -> new PhanHoiXuHuongDoanhThu.DiemXuHuong(e.getKey(), e.getValue()))
                .toList();
        return new PhanHoiXuHuongDoanhThu(diem);
    }

    private PhanHoiXuHuongDoanhThu lapDayCacThang(PhanHoiXuHuongDoanhThu xuHuongGoc, LocalDate tuNgay,
            LocalDate denNgay) {
        // 1. Chuyển list gốc thành Map để tra cứu
        Map<String, BigDecimal> mapDoanhThu = xuHuongGoc.diem().stream()
                .collect(Collectors.toMap(DiemXuHuong::nhan, DiemXuHuong::doanhThu));

        // 2. Xác định khoảng thời gian
        YearMonth thangBatDau = (tuNgay != null)
                ? YearMonth.from(tuNgay)
                : YearMonth.now().minusMonths(5); // Mặc định 6 tháng gần nhất

        YearMonth thangKetThuc = (denNgay != null)
                ? YearMonth.from(denNgay)
                : YearMonth.now(); // Mặc định đến tháng hiện tại

        // 3. Lấp đầy dữ liệu
        List<DiemXuHuong> duLieuDaLapDay = new ArrayList<>();
        YearMonth thangHienTai = thangBatDau;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        while (!thangHienTai.isAfter(thangKetThuc)) {
            String nhanThang = thangHienTai.format(formatter);
            BigDecimal doanhThu = mapDoanhThu.getOrDefault(nhanThang, BigDecimal.ZERO);

            duLieuDaLapDay.add(new DiemXuHuong(nhanThang, doanhThu));
            thangHienTai = thangHienTai.plusMonths(1);
        }

        return new PhanHoiXuHuongDoanhThu(duLieuDaLapDay);
    }

    private PhanHoiXuHuongDoanhThu xuLyXuHuongTheoTuan(
            List<PhanHoiGiaoDichDoanhThu> danhSachGiaoDich,
            LocalDate tuNgay, LocalDate denNgay) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        // 1. Gom nhóm danh sách giao dịch thô theo chuỗi nhãn tuần "dd/MM - dd/MM"
        Map<String, BigDecimal> mapDoanhThu = danhSachGiaoDich.stream()
                .collect(Collectors.groupingBy(
                        gd -> {
                            // Dùng gd.ngayGiaoDich() thay vì gd.getNgayGiaoDich()
                            // Nếu ngayGiaoDich() trả về LocalDateTime, hãy thêm .toLocalDate()
                            LocalDate ngay = gd.ngayGiaoDich().toLocalDate();

                            LocalDate dauTuan = ngay.with(DayOfWeek.MONDAY);
                            LocalDate cuoiTuan = dauTuan.plusDays(6);

                            return dauTuan.format(fmt) + " - " + cuoiTuan.format(fmt);
                        },
                        Collectors.mapping(
                                PhanHoiGiaoDichDoanhThu::soTien, // SỬA Ở ĐÂY: Dùng ::soTien thay vì ::getSoTien
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        // 2. Xác định mốc thời gian bắt đầu và kết thúc (lấy theo Thứ 2)
        // Nếu không có tuNgay, mặc định lấy 6 tuần gần nhất
        LocalDate ngayBatDau = (tuNgay != null) ? tuNgay : LocalDate.now().minusWeeks(5);
        LocalDate ngayKetThuc = (denNgay != null) ? denNgay : LocalDate.now();

        LocalDate tuanHienTai = ngayBatDau.with(DayOfWeek.MONDAY);
        LocalDate tuanCuoi = ngayKetThuc.with(DayOfWeek.MONDAY);

        // 3. Lấp đầy các tuần
        List<DiemXuHuong> duLieuTuan = new ArrayList<>();

        while (!tuanHienTai.isAfter(tuanCuoi)) {
            LocalDate cuoiTuan = tuanHienTai.plusDays(6);
            String nhanTuan = tuanHienTai.format(fmt) + " - " + cuoiTuan.format(fmt);

            // Tra cứu Map, nếu tuần đó không có ai mua -> 0đ
            BigDecimal doanhThu = mapDoanhThu.getOrDefault(nhanTuan, BigDecimal.ZERO);

            duLieuTuan.add(new DiemXuHuong(nhanTuan, doanhThu));

            // Nhảy sang tuần tiếp theo
            tuanHienTai = tuanHienTai.plusWeeks(1);
        }

        return new PhanHoiXuHuongDoanhThu(duLieuTuan);
    }
}