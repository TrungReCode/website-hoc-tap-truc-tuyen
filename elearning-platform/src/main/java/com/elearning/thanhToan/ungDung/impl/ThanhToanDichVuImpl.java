package com.elearning.thanhToan.ungDung.impl;

import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.choKhoaHoc.mienDomain.KhoaHoc;
import com.elearning.dinhDanhPhanQuyen.haTang.TaiKhoanNganHangKho;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;
import com.elearning.dinhDanhPhanQuyen.ungDung.CauHinhHeThongDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.NguoiDungTraCuuDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.dungChung.ngoaiLe.NgoaiLeNghiepVu;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.QuyenSuDung;
import com.elearning.lopHocLMS.mienDomain.ThanhVienLop;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiGiayPhep;
import com.elearning.lopHocLMS.ungDung.GhiDanhDichVu;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDich;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiGiaoDichNhan;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiTaoDonThanhToan;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiYeuCauHoanTien;
import com.elearning.thanhToan.giaoDien.dto.phanHoi.PhanHoiXuLyVnpay;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauHoanTien;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauThanhToanKhoaHoc;
import com.elearning.thanhToan.giaoDien.dto.yeuCau.YeuCauThanhToanLopHoc;
import com.elearning.thanhToan.haTang.PhieuHoanTienKhoaHocKho;
import com.elearning.thanhToan.haTang.PhieuHoanTienLopHocKho;
import com.elearning.thanhToan.haTang.PhieuDoiSoatKho;
import com.elearning.thanhToan.haTang.PhieuMuaKhoaHocKho;
import com.elearning.thanhToan.haTang.PhieuTraPhiLopHocKho;
import com.elearning.thanhToan.haTang.ThanhToanProperties;
import com.elearning.thanhToan.haTang.VnpayCongCu;
import com.elearning.thanhToan.mienDomain.PhieuDoiSoat;
import com.elearning.thanhToan.mienDomain.PhieuHoanTienKhoaHoc;
import com.elearning.thanhToan.mienDomain.PhieuHoanTienLopHoc;
import com.elearning.thanhToan.mienDomain.PhieuMuaKhoaHoc;
import com.elearning.thanhToan.mienDomain.PhieuTraPhiLopHoc;
import com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich;
import com.elearning.thanhToan.mienDomain.TrangThaiHoanTien;
import com.elearning.thanhToan.ungDung.ThanhToanDichVu;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.ThongBaoDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class ThanhToanDichVuImpl implements ThanhToanDichVu {

    private final CauHinhHeThongDichVu cauHinhHeThongDichVu;
    private final KhoaHocKho khoaHocKho;
    private final LopHocKho lopHocKho;
    private final GhiDanhDichVu ghiDanhDichVu;
    private final QuyenSuDungKho quyenSuDungKho;
    private final ThanhVienLopKho thanhVienLopKho;
    private final TaiKhoanNganHangKho taiKhoanNganHangKho;
    private final PhieuMuaKhoaHocKho phieuMuaKhoaHocKho;
    private final PhieuTraPhiLopHocKho phieuTraPhiLopHocKho;
    private final PhieuHoanTienKhoaHocKho phieuHoanTienKhoaHocKho;
    private final PhieuHoanTienLopHocKho phieuHoanTienLopHocKho;
    private final PhieuDoiSoatKho phieuDoiSoatKho;
    private final ThongBaoDichVu thongBaoDichVu;
    private final ThanhToanProperties thanhToanProperties;
    private final NguoiDungTraCuuDichVu nguoiDungTraCuuDichVu;

    @Override
    public PhanHoiTaoDonThanhToan taoDonMuaKhoaHoc(Long maNguoiDung, Long maKhoaHoc, YeuCauThanhToanKhoaHoc yeuCau,
            String originNguoiDung) {
        // 1. Kiểm tra xem đã sở hữu chưa
        if (quyenSuDungKho.existsByMaNguoiDungAndMaKhoaHocAndLoaiGiayPhep(maNguoiDung, maKhoaHoc,
                yeuCau.loaiGiayPhep())) {
            throw new NgoaiLeDuLieuKhongHopLe("Bạn đã sở hữu khóa học này rồi!");
        }
        KhoaHoc khoaHoc = khoaHocKho.findById(maKhoaHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("khóa học", maKhoaHoc));

        BigDecimal soTien = laySoTienKhoaHoc(khoaHoc, yeuCau.loaiGiayPhep());

        // XỬ LÝ RIÊNG CHO KHÓA HỌC MIỄN PHÍ (0 VNĐ)
        // FIX: trước đây vẫn lưu PhieuMuaKhoaHoc (0đ, THANH_CONG) — không sai
        // về mặt cấp quyền (đã cấp quyền đúng), nhưng phiếu 0đ này sau đó bị
        // BoXuLyDoiSoatTuDong quét trúng và sinh thêm PhieuDoiSoat 0đ vô
        // nghĩa (đã chặn ở BoXuLyDoiSoatTuDong, nhưng gốc rễ hơn là: khóa
        // học miễn phí không có giao dịch tài chính thật nào để mà lưu vết
        // — không cần tạo PhieuMuaKhoaHoc, giống hệt cách taoDonPhiLopHoc()
        // đã xử lý cho lớp học miễn phí (hoàn toàn không tạo phiếu). Cấp
        // quyền truy cập trực tiếp, không lưu vết giao dịch nào cả.
        if (soTien.compareTo(BigDecimal.ZERO) == 0) {
            quyenSuDungKho.save(new QuyenSuDung(maNguoiDung, maKhoaHoc, yeuCau.loaiGiayPhep()));
            String maGiaoDichMienPhi = taoMaGiaoDich("KH", maNguoiDung, maKhoaHoc, yeuCau.loaiGiayPhep().name());
            return taoPhanHoiDon(maGiaoDichMienPhi, soTien, null, TrangThaiGiaoDich.THANH_CONG, null);
        }

        // XỬ LÝ CHO KHÓA HỌC CÓ PHÍ (> 0 VNĐ)
        String maGiaoDich = taoMaGiaoDich("KH", maNguoiDung, maKhoaHoc, yeuCau.loaiGiayPhep().name());
        PhieuMuaKhoaHoc phieu = new PhieuMuaKhoaHoc(maNguoiDung, maKhoaHoc, yeuCau.loaiGiayPhep(), soTien, maGiaoDich);
        phieu.getDuLieuDong().put("tieuDeKhoaHoc", khoaHoc.getTieuDe());
        phieu.getDuLieuDong().put("maTacGia", khoaHoc.getMaTacGia());
        phieu.getDuLieuDong().put("loaiGiayPhep", yeuCau.loaiGiayPhep().name());
        phieu.setHanGiuTien(LocalDateTime.now().plusDays(cauHinhHeThongDichVu.layCauHinh().soNgayGiuKyQuyKhoaHoc()));
        phieuMuaKhoaHocKho.save(phieu);

        return taoPhanHoiDon(phieu.getMaGiaoDichVnpay(), soTien,
                taoUrlThanhToan(phieu.getMaGiaoDichVnpay(), soTien, "Mua khóa học: " + khoaHoc.getTieuDe(), "KHOA_HOC",
                        originNguoiDung),
                phieu.getTrangThai(), phieu.getHanGiuTien());
    }

    @Override
    public PhanHoiTaoDonThanhToan taoDonPhiLopHoc(Long maNguoiDung, String maLop, YeuCauThanhToanLopHoc yeuCau,
            String originNguoiDung) {
        // 1. Tìm lớp theo mã chuỗi
        LopHoc lop = lopHocKho.timKhoaVaKhoaTheoMaLop(maLop)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLop));

        // 2. Kiểm tra miễn phí
        if (lop.coMienPhi()) {
            throw new NgoaiLeDuLieuKhongHopLe("Lớp học này không thu phí nên không cần tạo đơn thanh toán");
        }

        // 2b. FIX: Khóa 1 (sở hữu khóa học gốc) — luồng đóng phí lớp học đi
        // thẳng tới đây, KHÔNG qua GhiDanhDichVu.thucHienGhiDanh(), nên phải
        // tự gọi kiểm tra Khóa 1 ở đây; nếu không, học viên có thể trả phí
        // lớp học (Khóa 2) và được cấp quyền vào lớp mà chưa từng mua khóa
        // học gốc (Khóa 1) — xem Javadoc của GhiDanhDichVu.kiemTraKhoaThuNhat.
        ghiDanhDichVu.kiemTraKhoaThuNhat(maNguoiDung, lop.getMaLopHoc());

        // 3. Khóa lớp để chặn tạo đơn khi sĩ số đã đầy.
        kiemTraConChoTrongLop(lop);

        // 4. Sử dụng ID của lớp (lop.getMaLopHoc()) để tạo đơn
        BigDecimal soTien = lop.getPhiLopHoc();
        // Giữ nguyên logic tạo mã giao dịch với ID hệ thống để đảm bảo tính duy nhất
        String maGiaoDich = taoMaGiaoDich("LH", maNguoiDung, lop.getMaLopHoc(), lop.getMaLop());

        PhieuTraPhiLopHoc phieu = new PhieuTraPhiLopHoc(maNguoiDung, lop.getMaLopHoc(), soTien, maGiaoDich);
        phieu.getDuLieuDong().put("maLop", lop.getMaLop());
        phieu.getDuLieuDong().put("maGiangVien", lop.getMaGiangVien());
        phieu.setHanGiuTien(lop.getNgayBatDau().plusDays(cauHinhHeThongDichVu.layCauHinh().soNgayGiuKyQuyLopHoc()));
        phieuTraPhiLopHocKho.save(phieu);

        return taoPhanHoiDon(phieu.getMaGiaoDichVnpay(), soTien,
                taoUrlThanhToan(phieu.getMaGiaoDichVnpay(), soTien, "Đóng phí lớp học: " + lop.getMaLop(), "LOP_HOC",
                        originNguoiDung),
                phieu.getTrangThai(), phieu.getHanGiuTien());
    }

    @Override
    public PhanHoiYeuCauHoanTien taoYeuCauHoanTienKhoaHoc(Long maNguoiDung, Long maPhieuGiaoDich,
            YeuCauHoanTien yeuCau) {
        if (yeuCau == null) {
            throw new NgoaiLeDuLieuKhongHopLe("Thiếu dữ liệu yêu cầu hoàn tiền");
        }
        PhieuMuaKhoaHoc phieu = phieuMuaKhoaHocKho.findById(maPhieuGiaoDich)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("phiếu mua khóa học", maPhieuGiaoDich));
        kiemTraChuSoHuu(phieu.getMaNguoiDung(), maNguoiDung);
        kiemTraConTrongThoiGianGiu(phieu.getHanGiuTien(), "phiếu mua khóa học");
        if (!phieu.daThanhCong()) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Chỉ có thể tạo yêu cầu hoàn tiền cho giao dịch đã thanh toán thành công");
        }
        // FIX: chỉ chặn nếu yêu cầu hoàn tiền trước đó đang xử lý hoặc đã hoàn tất —
        // cho phép gửi lại nếu yêu cầu trước đó đã bị TỪ CHỐI (trước đây dùng
        // existsByMaPhieuGiaoDich() chặn vĩnh viễn bất kể trạng thái, kể cả sau khi
        // bị từ chối, khiến học viên không bao giờ gửi lại được).
        phieuHoanTienKhoaHocKho.findByMaPhieuGiaoDich(maPhieuGiaoDich).ifPresent(ht -> {
            if (ht.getTrangThai() != TrangThaiHoanTien.TU_CHOI) {
                throw new NgoaiLeDuLieuKhongHopLe("Giao dịch này đã có yêu cầu hoàn tiền đang xử lý hoặc đã hoàn tất");
            }
        });

        PhieuHoanTienKhoaHoc hoanTien = new PhieuHoanTienKhoaHoc(maPhieuGiaoDich, phieu.getSoTien(),
                yeuCau.lyDo().trim());
        hoanTien.getDuLieuDong().put("maNguoiDung", maNguoiDung);
        hoanTien.getDuLieuDong().put("maGiaoDichVnpay", phieu.getMaGiaoDichVnpay());
        hoanTien.getDuLieuDong().put("loaiGiaoDich", "KHOA_HOC");
        phieuHoanTienKhoaHocKho.save(hoanTien);
        baoQtvTaiChinhHoanTienMoi(hoanTien.getSoTien(), "khóa học");

        return new PhanHoiYeuCauHoanTien(hoanTien.getMaHoanTien(), hoanTien.getMaPhieuGiaoDich(), hoanTien.getSoTien(),
                hoanTien.getTrangThai(), hoanTien.getNgayTao());
    }

    @Override
    public PhanHoiYeuCauHoanTien taoYeuCauHoanTienLopHoc(Long maNguoiDung, Long maPhieuGiaoDich,
            YeuCauHoanTien yeuCau) {
        if (yeuCau == null) {
            throw new NgoaiLeDuLieuKhongHopLe("Thiếu dữ liệu yêu cầu hoàn tiền");
        }
        PhieuTraPhiLopHoc phieu = phieuTraPhiLopHocKho.findById(maPhieuGiaoDich)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("phiếu phí lớp học", maPhieuGiaoDich));
        kiemTraChuSoHuu(phieu.getMaNguoiDung(), maNguoiDung);
        kiemTraConTrongThoiGianGiu(phieu.getHanGiuTien(), "phiếu phí lớp học");
        if (!phieu.daThanhCong()) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Chỉ có thể tạo yêu cầu hoàn tiền cho giao dịch đã thanh toán thành công");
        }
        // FIX: điều kiện gốc bị NGƯỢC — trước đây chỉ chặn khi trạng thái là TU_CHOI
        // (đáng lẽ đây là trường hợp DUY NHẤT phải cho phép gửi lại), và ngầm cho
        // phép gửi trùng khi đang CHO_XAC_NHAN hoặc đã HOAN_TAT (nguy cơ hoàn tiền
        // 2 lần / tạo yêu cầu trùng). Đổi == thành != để chặn đúng chiều.
        phieuHoanTienLopHocKho.findByMaPhieuGiaoDich(maPhieuGiaoDich).ifPresent(ht -> {
            if (ht.getTrangThai() != TrangThaiHoanTien.TU_CHOI) {
                throw new NgoaiLeDuLieuKhongHopLe("Giao dịch này đã có yêu cầu hoàn tiền đang xử lý hoặc đã hoàn tất");
            }
        });

        PhieuHoanTienLopHoc hoanTien = new PhieuHoanTienLopHoc(maPhieuGiaoDich, phieu.getSoTien(),
                yeuCau.lyDo().trim());
        hoanTien.getDuLieuDong().put("maNguoiDung", maNguoiDung);
        hoanTien.getDuLieuDong().put("maGiaoDichVnpay", phieu.getMaGiaoDichVnpay());
        hoanTien.getDuLieuDong().put("loaiGiaoDich", "LOP_HOC");
        phieuHoanTienLopHocKho.save(hoanTien);
        baoQtvTaiChinhHoanTienMoi(hoanTien.getSoTien(), "lớp học");

        return new PhanHoiYeuCauHoanTien(hoanTien.getMaHoanTien(), hoanTien.getMaPhieuGiaoDich(), hoanTien.getSoTien(),
                hoanTien.getTrangThai(), hoanTien.getNgayTao());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiGiaoDich> layLichSuGiaoDich(Long maNguoiDung) {
        List<PhanHoiGiaoDich> ketQua = new ArrayList<>();
        LocalDateTime hienTai = LocalDateTime.now();

        for (PhieuMuaKhoaHoc p : phieuMuaKhoaHocKho.findByMaNguoiDungOrderByNgayTaoDesc(maNguoiDung)) {
            TrangThaiHoanTien trangThaiHoanTien = phieuHoanTienKhoaHocKho.findByMaPhieuGiaoDich(p.getMaPhieu())
                    .map(PhieuHoanTienKhoaHoc::getTrangThai)
                    .orElse(null);
            boolean coTheHoanTien = p.daThanhCong() && trangThaiHoanTien == null
                    && p.getHanGiuTien() != null && p.getHanGiuTien().isAfter(hienTai);
            String tieuDe = String
                    .valueOf(p.getDuLieuDong().getOrDefault("tieuDeKhoaHoc", "Khóa học #" + p.getMaKhoaHoc()));

            ketQua.add(new PhanHoiGiaoDich("KHOA_HOC", p.getMaPhieu(), tieuDe, p.getMaKhoaHoc(), p.getSoTien(),
                    p.getMaGiaoDichVnpay(), p.getTrangThai(), trangThaiHoanTien, p.getHanGiuTien(),
                    p.getNgayTao(), p.getNgayMuaThanhCong(), coTheHoanTien));
        }

        for (PhieuTraPhiLopHoc p : phieuTraPhiLopHocKho.findByMaNguoiDungOrderByNgayTaoDesc(maNguoiDung)) {
            TrangThaiHoanTien trangThaiHoanTien = phieuHoanTienLopHocKho.findByMaPhieuGiaoDich(p.getMaPhieu())
                    .map(PhieuHoanTienLopHoc::getTrangThai)
                    .orElse(null);
            boolean coTheHoanTien = p.daThanhCong() && trangThaiHoanTien == null
                    && p.getHanGiuTien() != null && p.getHanGiuTien().isAfter(hienTai);
            String maLop = String.valueOf(p.getDuLieuDong().getOrDefault("maLop", "#" + p.getMaLopHoc()));

            ketQua.add(new PhanHoiGiaoDich("LOP_HOC", p.getMaPhieu(), "Phí lớp học " + maLop, p.getMaLopHoc(),
                    p.getSoTien(), p.getMaGiaoDichVnpay(), p.getTrangThai(), trangThaiHoanTien, p.getHanGiuTien(),
                    p.getNgayTao(), p.getNgayMuaThanhCong(), coTheHoanTien));
        }

        ketQua.sort(Comparator.comparing(PhanHoiGiaoDich::ngayTao).reversed());
        return ketQua;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiGiaoDichNhan> layLichSuGiaoDichNhan(Long maNguoiDung) {
        List<PhanHoiGiaoDichNhan> ketQua = new ArrayList<>();

        // ── Khóa học do người này là TÁC GIẢ ──
        List<Long> maKhoaHocList = khoaHocKho.findByMaTacGia(maNguoiDung).stream()
                .map(KhoaHoc::getMaKhoaHoc).toList();
        if (!maKhoaHocList.isEmpty()) {
            List<PhieuMuaKhoaHoc> giaoDichs = phieuMuaKhoaHocKho.findByMaKhoaHocIn(maKhoaHocList).stream()
                    .filter(p -> p.getMaDoiSoat() != null)
                    .toList();
            ganGiaoDichNhan(ketQua, giaoDichs, "KHOA_HOC",
                    p -> String.valueOf(p.getDuLieuDong().getOrDefault("tieuDeKhoaHoc", "Khóa học #" + p.getMaKhoaHoc())),
                    PhieuMuaKhoaHoc::getMaKhoaHoc, PhieuMuaKhoaHoc::getMaDoiSoat, PhieuMuaKhoaHoc::getNgayTao);
        }

        // ── Lớp học do người này là GIẢNG VIÊN phụ trách ──
        List<Long> maLopHocList = lopHocKho.findByMaGiangVien(maNguoiDung).stream()
                .map(LopHoc::getMaLopHoc).toList();
        if (!maLopHocList.isEmpty()) {
            List<PhieuTraPhiLopHoc> giaoDichs = phieuTraPhiLopHocKho.findByMaLopHocIn(maLopHocList).stream()
                    .filter(p -> p.getMaDoiSoat() != null)
                    .toList();
            ganGiaoDichNhan(ketQua, giaoDichs, "LOP_HOC",
                    p -> "Phí lớp học " + p.getDuLieuDong().getOrDefault("maLop", "#" + p.getMaLopHoc()),
                    PhieuTraPhiLopHoc::getMaLopHoc, PhieuTraPhiLopHoc::getMaDoiSoat, PhieuTraPhiLopHoc::getNgayTao);
        }

        ketQua.sort(Comparator.comparing(PhanHoiGiaoDichNhan::ngayTao).reversed());
        return ketQua;
    }

    /**
     * Helper dùng chung cho cả 2 nhánh KHOA_HOC/LOP_HOC ở layLichSuGiaoDichNhan
     * — gộp danh sách giao dịch với danh sách PhieuDoiSoat tương ứng
     * (findAllById theo lô, tránh N+1) rồi map sang PhanHoiGiaoDichNhan.
     */
    private <T> void ganGiaoDichNhan(List<PhanHoiGiaoDichNhan> ketQua, List<T> giaoDichs, String loaiGiaoDich,
            java.util.function.Function<T, String> layTieuDe, java.util.function.Function<T, Long> layMaDoiTuong,
            java.util.function.Function<T, Long> layMaDoiSoat, java.util.function.Function<T, LocalDateTime> layNgayTao) {
        List<Long> maDoiSoatList = giaoDichs.stream().map(layMaDoiSoat).toList();
        Map<Long, PhieuDoiSoat> theoMa = phieuDoiSoatKho.findAllById(maDoiSoatList).stream()
                .collect(Collectors.toMap(PhieuDoiSoat::getMaDoiSoat, d -> d));

        for (T giaoDich : giaoDichs) {
            PhieuDoiSoat doiSoat = theoMa.get(layMaDoiSoat.apply(giaoDich));
            if (doiSoat == null) {
                continue; // dữ liệu không nhất quán hiếm gặp — bỏ qua thay vì lỗi cả danh sách
            }
            ketQua.add(new PhanHoiGiaoDichNhan(loaiGiaoDich, doiSoat.getMaDoiSoat(), layTieuDe.apply(giaoDich),
                    layMaDoiTuong.apply(giaoDich), doiSoat.getTongDoanhThu(), doiSoat.getHoaHong(),
                    doiSoat.getSoTienThucTe(), doiSoat.getTrangThai(), layNgayTao.apply(giaoDich),
                    doiSoat.getNgayChuyenThanhCong()));
        }
    }

    @Override
    public PhanHoiXuLyVnpay xuLyReturn(Map<String, String> params) {
        return xuLyGiaoDich(params, false);
    }

    @Override
    public PhanHoiXuLyVnpay xuLyIpn(Map<String, String> params) {
        return xuLyGiaoDich(params, true);
    }

    private PhanHoiXuLyVnpay xuLyGiaoDich(Map<String, String> params, boolean laIpn) {
        String maGiaoDich = lay(params, "vnp_TxnRef");
        String secureHash = lay(params, "vnp_SecureHash");
        String responseCode = lay(params, "vnp_ResponseCode");
        String transactionStatus = lay(params, "vnp_TransactionStatus");

        if (maGiaoDich == null || secureHash == null) {
            return new PhanHoiXuLyVnpay("97", "Thiếu dữ liệu chữ ký", maGiaoDich, false);
        }
        if (!VnpayCongCu.xacThucChuKy(params, thanhToanProperties.getHashSecret(), secureHash)) {
            return new PhanHoiXuLyVnpay("97", "Chữ ký không hợp lệ", maGiaoDich, false);
        }
        if (!"00".equals(responseCode) || !"00".equals(transactionStatus)) {
            capNhatThatBai(maGiaoDich, "VNPay trả về mã lỗi " + responseCode);
            return new PhanHoiXuLyVnpay(responseCode, "Giao dịch không thành công", maGiaoDich, false);
        }

        if (maGiaoDich.startsWith("KH-")) {
            return xuLyThanhCongKhoaHoc(maGiaoDich, params, laIpn);
        }
        if (maGiaoDich.startsWith("LH-")) {
            return xuLyThanhCongLopHoc(maGiaoDich, params, laIpn);
        }

        return new PhanHoiXuLyVnpay("01", "Không xác định được loại giao dịch", maGiaoDich, false);
    }

    private PhanHoiXuLyVnpay xuLyThanhCongKhoaHoc(String maGiaoDich, Map<String, String> params, boolean laIpn) {
        PhieuMuaKhoaHoc phieu = phieuMuaKhoaHocKho.timKhoaVaKhoa(maGiaoDich)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("phiếu mua khóa học", maGiaoDich));

        if (phieu.daThanhCong()) {
            return new PhanHoiXuLyVnpay("00", "Giao dịch đã được xử lý", maGiaoDich, true);
        }

        BigDecimal amount = laySoTienVnpay(params);
        if (phieu.getSoTien().multiply(BigDecimal.valueOf(100)).compareTo(amount) != 0) {
            throw new NgoaiLeDuLieuKhongHopLe("Số tiền giao dịch không khớp dữ liệu gốc");
        }

        phieu.setTrangThai(TrangThaiGiaoDich.THANH_CONG);
        phieu.setNgayMuaThanhCong(LocalDateTime.now());
        phieu.setTrangThaiDoiSoat("CHUA_DOI_SOAT");
        // Lưu lại mã giao dịch & thời điểm thanh toán phía VNPay — cần thiết khi
        // sau này gọi API hoàn tiền (Merchant API yêu cầu đúng 2 giá trị gốc này).
        if (params.get("vnp_TransactionNo") != null) {
            phieu.getDuLieuDong().put("vnpTransactionNo", params.get("vnp_TransactionNo"));
        }
        if (params.get("vnp_PayDate") != null) {
            phieu.getDuLieuDong().put("vnpPayDate", params.get("vnp_PayDate"));
        }

        if (!quyenSuDungKho.existsByMaNguoiDungAndMaKhoaHocAndLoaiGiayPhep(phieu.getMaNguoiDung(), phieu.getMaKhoaHoc(),
                phieu.getLoaiGiayPhep())) {
            quyenSuDungKho.save(new QuyenSuDung(phieu.getMaNguoiDung(), phieu.getMaKhoaHoc(), phieu.getLoaiGiayPhep()));
        }

        taoThongBaoThanhCong(phieu.getMaNguoiDung(), "Thanh toán khóa học thành công",
                "Giao dịch " + maGiaoDich + " đã hoàn tất", maGiaoDich);
        return new PhanHoiXuLyVnpay("00", laIpn ? "IPN hợp lệ" : "Return hợp lệ", maGiaoDich, true);
    }

    private PhanHoiXuLyVnpay xuLyThanhCongLopHoc(String maGiaoDich, Map<String, String> params, boolean laIpn) {
        PhieuTraPhiLopHoc phieu = phieuTraPhiLopHocKho.timKhoaVaKhoa(maGiaoDich)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("phiếu phí lớp học", maGiaoDich));

        LopHoc lop = lopHocKho.timKhoaVaKhoaTheoMaLopHoc(phieu.getMaLopHoc())
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", phieu.getMaLopHoc()));

        if (phieu.daThanhCong()) {
            return new PhanHoiXuLyVnpay("00", "Giao dịch đã được xử lý", maGiaoDich, true);
        }

        BigDecimal amount = laySoTienVnpay(params);
        if (phieu.getSoTien().multiply(BigDecimal.valueOf(100)).compareTo(amount) != 0) {
            throw new NgoaiLeDuLieuKhongHopLe("Số tiền giao dịch không khớp dữ liệu gốc");
        }

        phieu.setTrangThai(TrangThaiGiaoDich.THANH_CONG);
        phieu.setNgayMuaThanhCong(LocalDateTime.now());
        phieu.setTrangThaiDoiSoat("CHUA_DOI_SOAT");
        if (params.get("vnp_TransactionNo") != null) {
            phieu.getDuLieuDong().put("vnpTransactionNo", params.get("vnp_TransactionNo"));
        }
        if (params.get("vnp_PayDate") != null) {
            phieu.getDuLieuDong().put("vnpPayDate", params.get("vnp_PayDate"));
        }

        if (lopDaDuSiSo(lop)) {
            taoYeuCauHoanTienTuDongChoLopDay(phieu, lop,
                    "Tự động hoàn phí lớp học do lớp đã đủ sĩ số tại thời điểm xác nhận thanh toán");
            return new PhanHoiXuLyVnpay("02",
                    "Lớp học đã đủ sĩ số. Hệ thống đã tự động tạo yêu cầu hoàn tiền cho giao dịch này.",
                    maGiaoDich, false);
        }

        // FIX: xác nhận lại Khóa 1 (sở hữu khóa học gốc) ngay tại điểm cấp
        // quyền — không chỉ ở taoDonPhiLopHoc(). Tiền đã được VNPay xác nhận
        // thu thành công tại đây nên KHÔNG được ném lỗi làm hỏng phản hồi
        // IPN; nếu Khóa 1 vẫn không đạt (ví dụ đơn được tạo trước khi vá lỗi
        // này, hoặc một đường khác lỡ tạo PhieuTraPhiLopHoc mà bỏ qua kiểm
        // tra) thì xử lý giống hệt trường hợp lớp đã đủ sĩ số: tự động hoàn
        // tiền, không cấp ThanhVienLop.
        try {
            ghiDanhDichVu.kiemTraKhoaThuNhat(phieu.getMaNguoiDung(), lop.getMaLopHoc());
        } catch (NgoaiLeNghiepVu chuaSoHuuKhoaHocGoc) {
            taoYeuCauHoanTienTuDongChoLopDay(phieu, lop,
                    "Tự động hoàn phí lớp học do người thanh toán chưa sở hữu khóa học gốc của lớp");
            return new PhanHoiXuLyVnpay("02",
                    "Chưa sở hữu khóa học gốc. Hệ thống đã tự động tạo yêu cầu hoàn tiền cho giao dịch này.",
                    maGiaoDich, false);
        }

        if (!thanhVienLopKho.existsByMaHocVienAndMaLopHoc(phieu.getMaNguoiDung(), lop.getMaLopHoc())) {
            thanhVienLopKho.save(new ThanhVienLop(phieu.getMaNguoiDung(), lop.getMaLopHoc()));
        }

        taoThongBaoThanhCong(phieu.getMaNguoiDung(), "Thanh toán phí lớp học thành công",
                "Giao dịch " + maGiaoDich + " đã hoàn tất", maGiaoDich);
        return new PhanHoiXuLyVnpay("00", laIpn ? "IPN hợp lệ" : "Return hợp lệ", maGiaoDich, true);
    }

    private void capNhatThatBai(String maGiaoDich, String lyDo) {
        phieuMuaKhoaHocKho.findByMaGiaoDichVnpay(maGiaoDich).ifPresent(phieu -> {
            if (phieu.getTrangThai() == TrangThaiGiaoDich.CHO_XAC_NHAN) {
                phieu.setTrangThai(TrangThaiGiaoDich.THAT_BAI);
                phieu.getDuLieuDong().put("lyDoThatBai", lyDo);
            }
        });
        phieuTraPhiLopHocKho.findByMaGiaoDichVnpay(maGiaoDich).ifPresent(phieu -> {
            if (phieu.getTrangThai() == TrangThaiGiaoDich.CHO_XAC_NHAN) {
                phieu.setTrangThai(TrangThaiGiaoDich.THAT_BAI);
                phieu.getDuLieuDong().put("lyDoThatBai", lyDo);
            }
        });
    }

    private void taoThongBaoThanhCong(Long maNguoiDung, String tieuDe, String noiDung, String maGiaoDich) {
        thongBaoDichVu.guiThongBao(maNguoiDung, LoaiThongBao.THANH_TOAN_THANH_CONG, tieuDe, noiDung,
                Map.of("maGiaoDich", maGiaoDich));
    }

    // FIX: trước đây khi tạo yêu cầu hoàn tiền, không có QTV_TAI_CHINH nào
    // được báo — họ chỉ biết có việc cần xử lý nếu tự vào tab Hoàn tiền kiểm
    // tra thủ công. Báo cho toàn bộ QTV_TAI_CHINH đang hoạt động ngay khi có
    // yêu cầu hoàn tiền mới, bất kể khóa học hay lớp học.
    private void baoQtvTaiChinhHoanTienMoi(BigDecimal soTien, String loaiGiaoDich) {
        for (Long maQtv : nguoiDungTraCuuDichVu.layDanhSachMaNguoiDungTheoVaiTro(VaiTro.QTV_TAI_CHINH)) {
            thongBaoDichVu.guiThongBao(maQtv, LoaiThongBao.HOAN_TIEN_MOI,
                    "Có yêu cầu hoàn tiền mới",
                    "Một học viên vừa gửi yêu cầu hoàn tiền " + soTien + " VNĐ cho giao dịch " + loaiGiaoDich + ".",
                    Map.of());
        }
    }

    private PhanHoiTaoDonThanhToan taoPhanHoiDon(String maGiaoDich, BigDecimal soTien, String urlThanhToan,
            TrangThaiGiaoDich trangThai, LocalDateTime ngayHetHan) {
        return new PhanHoiTaoDonThanhToan(maGiaoDich, soTien, urlThanhToan, trangThai, ngayHetHan);
    }

    /**
     * Chỉ chấp nhận origin khớp 1 trong các mẫu an toàn đã biết trước — KHÔNG
     * bao giờ tin thẳng giá trị header Origin của client để tự ý dùng làm
     * vnp_ReturnUrl. Nếu cho phép tuỳ ý, kẻ tấn công có thể tự set Origin
     * trỏ về domain lạ, khiến VNPay điều hướng người dùng SAU KHI thanh toán
     * xong sang trang giả mạo (open redirect) — vẫn nguy hiểm dù đây là môi
     * trường sandbox/LAN nội bộ.
     *
     * Cho phép: localhost (mọi cổng), IP LAN riêng tư chuẩn (192.168.x.x,
     * 10.x.x.x, 172.16-31.x.x — RFC 1918) ở đúng cổng frontend dev
     * (:5173), hoặc đúng giá trị đã cấu hình app.frontend-url.
     */
    private static final Pattern MAU_ORIGIN_AN_TOAN = Pattern.compile(
            "^https?://(localhost|127\\.0\\.0\\.1|192\\.168\\.\\d{1,3}\\.\\d{1,3}|10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"
                    + "|172\\.(1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3})(:\\d+)?$");

    /**
     * Ưu tiên dùng đúng origin (scheme + host + port) mà người dùng đang
     * thực sự truy cập frontend lúc thanh toán — fix lỗi VNPay luôn điều
     * hướng về "localhost" dù người dùng đang test qua IP LAN khác (VD:
     * http://192.168.1.17:5173), do trước đây return-url lấy CỐ ĐỊNH từ
     * application.yml bất kể người dùng vào từ đâu.
     *
     * Chỉ dùng originNguoiDung khi khớp MAU_ORIGIN_AN_TOAN (xem giải
     * thích ở đó) — nếu thiếu hoặc không khớp mẫu an toàn, dùng lại giá
     * trị tĩnh đã cấu hình sẵn (thanhToanProperties.getReturnUrl()) như
     * hành vi cũ, không suy đoán/tự chế thêm.
     */
    private String xayDungReturnUrl(String originNguoiDung) {
        if (originNguoiDung != null && MAU_ORIGIN_AN_TOAN.matcher(originNguoiDung).matches()) {
            return originNguoiDung + "/thanh-toan/ket-qua";
        }
        return thanhToanProperties.getReturnUrl();
    }

    private String taoUrlThanhToan(String maGiaoDich, BigDecimal soTien, String noiDung, String loai,
            String originNguoiDung) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Amount", soTien.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toString());
        params.put("vnp_Command", "pay");
        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_Locale", "vn");

        // Đã trả lại nội dung gốc (tiếng Việt có dấu cũng được vì đã có URLEncoder xử
        // lý)
        params.put("vnp_OrderInfo", noiDung);

        params.put("vnp_OrderType", loai);
        params.put("vnp_ReturnUrl", xayDungReturnUrl(originNguoiDung));
        params.put("vnp_TmnCode", thanhToanProperties.getTmnCode());
        params.put("vnp_TxnRef", maGiaoDich);
        params.put("vnp_Version", "2.1.0");

        // 1. Sắp xếp lại map theo key (Alphabet)
        Map<String, String> sortedParams = new TreeMap<>(params);

        // 2. Tạo chữ ký dựa trên map đã sắp xếp
        String secureHash = VnpayCongCu.taoChuKy(sortedParams, thanhToanProperties.getHashSecret());

        // 3. Ghép URL (có kiểm tra null/blank để đồng bộ với thuật toán tạo chữ ký)
        StringBuilder url = new StringBuilder(thanhToanProperties.getPayUrl()).append('?');
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String value = entry.getValue();
            if (value != null && !value.isBlank()) {
                url.append(entry.getKey()).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                        .append('&');
            }
        }
        url.append("vnp_SecureHash=").append(secureHash);

        return url.toString();
    }

    private BigDecimal laySoTienKhoaHoc(KhoaHoc khoaHoc, LoaiGiayPhep loaiGiayPhep) {
        return switch (loaiGiayPhep) {
            case CA_NHAN -> khoaHoc.getGia();
            case DAY_HOC -> {
                // Phí dạy học độc lập với giá cá nhân — khóa học có thể miễn phí cho
                // học viên nhưng vẫn thu phí giấy phép giảng dạy (hoặc ngược lại).
                if (khoaHoc.getPhiDayHoc() == null) {
                    throw new NgoaiLeDuLieuKhongHopLe("Khóa học này chưa mở bán quyền giảng dạy");
                }
                yield khoaHoc.getPhiDayHoc();
            }
        };
    }

    private String taoMaGiaoDich(String tienTo, Long maNguoiDung, Long maTaiNguyen, String phanBiet) {
        return tienTo + '-' + maNguoiDung + '-' + maTaiNguyen + '-'
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + '-' + phanBiet.replace(' ', '_');
    }

    private BigDecimal laySoTienVnpay(Map<String, String> params) {
        String amount = lay(params, "vnp_Amount");
        if (amount == null) {
            throw new NgoaiLeDuLieuKhongHopLe("Thiếu số tiền VNPay");
        }
        return new BigDecimal(amount);
    }

    private String lay(Map<String, String> params, String key) {
        String value = params.get(key);
        return value != null && !value.isBlank() ? value.trim() : null;
    }

    private void kiemTraConChoTrongLop(LopHoc lop) {
        if (lopDaDuSiSo(lop)) {
            throw new NgoaiLeDuLieuKhongHopLe("Lớp học đã đủ sĩ số");
        }
    }

    private boolean lopDaDuSiSo(LopHoc lop) {
        return lop.getSiSo() != null && thanhVienLopKho.countByMaLopHoc(lop.getMaLopHoc()) >= lop.getSiSo();
    }

    private void taoYeuCauHoanTienTuDongChoLopDay(PhieuTraPhiLopHoc phieu, LopHoc lop, String lyDo) {
        if (phieuHoanTienLopHocKho.existsByMaPhieuGiaoDich(phieu.getMaPhieu())) {
            return;
        }

        PhieuHoanTienLopHoc hoanTien = new PhieuHoanTienLopHoc(phieu.getMaPhieu(), phieu.getSoTien(), lyDo);
        hoanTien.getDuLieuDong().put("maNguoiDung", phieu.getMaNguoiDung());
        hoanTien.getDuLieuDong().put("maGiaoDichVnpay", phieu.getMaGiaoDichVnpay());
        hoanTien.getDuLieuDong().put("loaiGiaoDich", "LOP_HOC");
        hoanTien.getDuLieuDong().put("maLopHoc", lop.getMaLopHoc());
        hoanTien.getDuLieuDong().put("maLop", lop.getMaLop());
        hoanTien.getDuLieuDong().put("tuDongPhatSinh", true);
        phieuHoanTienLopHocKho.save(hoanTien);
    }

    private void kiemTraChuSoHuu(Long maChuSoHuu, Long maNguoiDung) {
        if (!maChuSoHuu.equals(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không có quyền thao tác trên giao dịch này");
        }
    }

    private void kiemTraConTrongThoiGianGiu(LocalDateTime hanGiuTien, String tenTaiNguyen) {
        if (hanGiuTien != null && hanGiuTien.isBefore(LocalDateTime.now())) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "" + tenTaiNguyen + " đã hết thời gian giữ ký quỹ, không thể yêu cầu hoàn tiền");
        }
    }
}