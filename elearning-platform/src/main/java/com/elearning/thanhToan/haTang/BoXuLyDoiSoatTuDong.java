package com.elearning.thanhToan.haTang;

import com.elearning.choKhoaHoc.haTang.KhoaHocKho;
import com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi.PhanHoiCauHinhHeThong;
import com.elearning.dinhDanhPhanQuyen.haTang.TaiKhoanNganHangKho;
import com.elearning.dinhDanhPhanQuyen.mienDomain.TaiKhoanNganHang;
import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro;
import com.elearning.dinhDanhPhanQuyen.ungDung.CauHinhHeThongDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.NguoiDungTraCuuDichVu;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiGiayPhep;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import com.elearning.thanhToan.mienDomain.PhieuDoiSoat;
import com.elearning.thanhToan.mienDomain.PhieuMuaKhoaHoc;
import com.elearning.thanhToan.mienDomain.PhieuTraPhiLopHoc;
import com.elearning.thanhToan.mienDomain.TrangThaiDoiSoat;
import com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich;
import com.elearning.thanhToan.mienDomain.TrangThaiHoanTien;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.ThongBaoDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BoXuLyDoiSoatTuDong {

    private final CauHinhHeThongDichVu cauHinhHeThongDichVu;
    private final PhieuMuaKhoaHocKho phieuMuaKhoaHocKho;
    private final PhieuTraPhiLopHocKho phieuTraPhiLopHocKho;
    private final PhieuDoiSoatKho phieuDoiSoatKho;
    private final PhieuHoanTienKhoaHocKho phieuHoanTienKhoaHocKho;
    private final PhieuHoanTienLopHocKho phieuHoanTienLopHocKho;
    private final TaiKhoanNganHangKho taiKhoanNganHangKho;
    private final KhoaHocKho khoaHocKho;
    private final LopHocKho lopHocKho;
    private final ThongBaoDichVu thongBaoDichVu;
    private final NguoiDungTraCuuDichVu nguoiDungTraCuuDichVu;

    @Scheduled(fixedDelayString = "${thanh-toan.doi-soat.fixed-delay-ms:600000}")
    @Transactional
    public void doiSoatTuDong() {
        LocalDateTime hienTai = LocalDateTime.now();

        // ── GIAI ĐOẠN 1: lập phiếu đối soát NGAY khi giao dịch thanh toán
        // thành công, ở trạng thái CHO_XAC_NHAN — KHÔNG chờ đủ ngày ký quỹ nữa.
        // Trước đây bước lập phiếu bị trộn chung với điều kiện "đã đến hạn giữ
        // tiền", khiến toàn bộ khoảng thời gian ký quỹ hoàn toàn vô hình với
        // QTV Tài chính (không có phiếu nào tồn tại để xem). Giờ tách riêng:
        // phiếu xuất hiện ngay, cho phép quản trị viên theo dõi khoản tiền
        // đang trong thời gian giữ ngay từ đầu.
        for (PhieuMuaKhoaHoc phieu : phieuMuaKhoaHocKho.findByTrangThaiAndMaDoiSoatIsNull(TrangThaiGiaoDich.THANH_CONG)) {
            // Khóa học miễn phí (0đ) vẫn được đánh dấu THANH_CONG (xem
            // ThanhToanDichVuImpl.taoDonMuaKhoaHoc — nhánh xử lý riêng cho
            // giá 0) để giữ lịch sử "đã nhận khóa học miễn phí", nhưng
            // KHÔNG có doanh thu thật để đối soát/giải ngân — không lập
            // phiếu đối soát cho trường hợp này, tránh sinh rác dữ liệu
            // PhieuDoiSoat 0đ vô nghĩa mỗi 10 phút.
            if (phieu.getSoTien() == null || phieu.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            khoaHocKho.findById(phieu.getMaKhoaHoc())
                    .ifPresent(khoaHoc -> lapPhieuChoXacNhan(phieu, khoaHoc.getMaTacGia(), phieu.getSoTien()));
        }
        for (PhieuTraPhiLopHoc phieu : phieuTraPhiLopHocKho.findByTrangThaiAndMaDoiSoatIsNull(TrangThaiGiaoDich.THANH_CONG)) {
            // Tương tự — phí lớp học lý thuyết cũng có thể là 0đ nếu giảng
            // viên đặt phiDayHoc = 0 cho khóa học gốc.
            if (phieu.getSoTien() == null || phieu.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            lopHocKho.findById(phieu.getMaLopHoc())
                    .ifPresent(lop -> lapPhieuChoXacNhan(phieu, lop.getMaGiangVien(), phieu.getSoTien()));
        }

        // ── GIAI ĐOẠN 2: phiếu đang CHO_XAC_NHAN mà giao dịch liên kết đã đủ
        // điều kiện giải ngân (đủ ngày ký quỹ, không có hoàn tiền đang chờ/đã
        // duyệt, riêng lớp học còn phải đã hoàn thành chương trình dạy) thì
        // chuyển sang DANG_XU_LY để QTV Tài chính xử lý chuyển khoản.
        //
        // Dùng timGiaoDichCanXetGiaiNgan() — lọc thẳng theo đúng PhieuDoiSoat.trangThai = CHO_XAC_NHAN bằng
        // subquery ở CSDL, tránh quét lại toàn bộ giao dịch đã có phiếu (kể cả
        // những phiếu đã DA_GIAI_NGAN/THAT_BAI từ lâu) mỗi 10 phút.
        for (PhieuMuaKhoaHoc phieu : phieuMuaKhoaHocKho.timGiaoDichCanXetGiaiNgan(
                TrangThaiGiaoDich.THANH_CONG, TrangThaiDoiSoat.CHO_XAC_NHAN)) {
            if (daDenHanGiu(phieu.getHanGiuTien(), hienTai) && khongCoHoanTienDangChoKhoaHoc(phieu.getMaPhieu())) {
                thuChuyenSangDangXuLy(phieu.getMaDoiSoat(), phieu);
            }
        }

        for (PhieuTraPhiLopHoc phieu : phieuTraPhiLopHocKho.timGiaoDichCanXetGiaiNgan(
                TrangThaiGiaoDich.THANH_CONG, TrangThaiDoiSoat.CHO_XAC_NHAN)) {
            if (!daDenHanGiu(phieu.getHanGiuTien(), hienTai) || !khongCoHoanTienDangChoLopHoc(phieu.getMaPhieu())) {
                continue;
            }
            boolean daHoanThanh = lopHocKho.findById(phieu.getMaLopHoc())
                    .map(lop -> daHoanThanhChuongTrinhDay(lop, hienTai))
                    .orElse(false);
            if (daHoanThanh) {
                thuChuyenSangDangXuLy(phieu.getMaDoiSoat(), phieu);
            }
        }
    }

    /**
     * Ngăn rủi ro "giảng viên thu phí rồi bỏ lớp" (mục 1.2 Chương 1) — chỉ lập
     * phiếu đối soát cho phí lớp học khi giảng viên đã thực sự hoàn thành
     * chương trình dạy: lớp đã đóng (DA_DONG, giảng viên chủ động kết thúc)
     * HOẶC đã qua ngày kết thúc đã cam kết khi tạo lớp. Nếu chưa, phiếu tiếp
     * tục chờ ở lượt chạy sau — không có gì bị mất, chỉ tạm hoãn giải ngân.
     */
    private boolean daHoanThanhChuongTrinhDay(LopHoc lop, LocalDateTime hienTai) {
        if (lop.getTrangThai() == TrangThaiLop.DA_DONG) {
            return true;
        }
        return lop.getNgayKetThuc() != null && !lop.getNgayKetThuc().isAfter(hienTai);
    }

    /**
     * Chặn đối soát tự động (chi trả cho giảng viên) nếu giao dịch đang có
     * yêu cầu hoàn tiền chưa bị từ chối (CHO_XAC_NHAN hoặc đã HOAN_TAT) —
     * tránh vừa trả tiền cho giảng viên vừa hoàn tiền cho học viên trên
     * cùng một giao dịch.
     */
    private boolean khongCoHoanTienDangChoKhoaHoc(Long maPhieu) {
        return phieuHoanTienKhoaHocKho.findByMaPhieuGiaoDich(maPhieu)
                .map(ht -> ht.getTrangThai() == TrangThaiHoanTien.TU_CHOI)
                .orElse(true);
    }

    private boolean khongCoHoanTienDangChoLopHoc(Long maPhieu) {
        return phieuHoanTienLopHocKho.findByMaPhieuGiaoDich(maPhieu)
                .map(ht -> ht.getTrangThai() == TrangThaiHoanTien.TU_CHOI)
                .orElse(true);
    }

    private boolean daDenHanGiu(LocalDateTime hanGiuTien, LocalDateTime hienTai) {
        return hanGiuTien == null || !hanGiuTien.isAfter(hienTai);
    }

    private void lapPhieuChoXacNhan(Object phieuGiaoDich, Long maGiangVien, BigDecimal tongDoanhThu) {
        List<TaiKhoanNganHang> taiKhoanNganHangs = taiKhoanNganHangKho.findByMaGiangVien(maGiangVien);
        if (taiKhoanNganHangs.isEmpty()) {
            // Giảng viên chưa thêm tài khoản ngân hàng — không lập phiếu được
            // (thiếu maTaiKhoan bắt buộc). Giao dịch vẫn còn maDoiSoat = null
            // nên sẽ được thử lại ở lượt chạy sau, ngay khi giảng viên bổ sung.
            return;
        }

        TaiKhoanNganHang taiKhoanMacDinh = taiKhoanNganHangs.stream()
                .filter(tk -> Boolean.TRUE.equals(tk.getMacDinh()))
                .findFirst()
                .orElse(taiKhoanNganHangs.get(0));

        BigDecimal tyLePhi = chonTyLePhi(phieuGiaoDich);
        BigDecimal soTienThucTe = tongDoanhThu.subtract(tongDoanhThu.multiply(tyLePhi).divide(new BigDecimal("100")));
        PhieuDoiSoat phieuDoiSoat = new PhieuDoiSoat(taiKhoanMacDinh.getMaTaiKhoan(), tyLePhi, tongDoanhThu, soTienThucTe);
        // Lập ngay ở CHO_XAC_NHAN — chưa xử lý, chỉ đánh dấu đang trong thời
        // gian giữ tiền ký quỹ. Việc chuyển sang DANG_XU_LY do GIAI ĐOẠN 2 của
        // doiSoatTuDong() đảm nhiệm khi đã đủ điều kiện giải ngân.
        phieuDoiSoat.setTrangThai(TrangThaiDoiSoat.CHO_XAC_NHAN);
        phieuDoiSoatKho.save(phieuDoiSoat);

        for (Long maQtv : nguoiDungTraCuuDichVu.layDanhSachMaNguoiDungTheoVaiTro(VaiTro.QTV_TAI_CHINH)) {
            thongBaoDichVu.guiThongBao(maQtv, LoaiThongBao.DOI_SOAT_MOI,
                    "Có phiếu đối soát mới đang chờ đủ ngày ký quỹ",
                    "Hệ thống vừa lập 1 phiếu đối soát trị giá " + soTienThucTe
                            + " VNĐ, đang trong thời gian giữ tiền ký quỹ, chưa cần xử lý ngay.",
                    Map.of("maDoiSoat", phieuDoiSoat.getMaDoiSoat()));
        }

        if (phieuGiaoDich instanceof PhieuMuaKhoaHoc phieuMuaKhoaHoc) {
            phieuMuaKhoaHoc.setMaDoiSoat(phieuDoiSoat.getMaDoiSoat());
            phieuMuaKhoaHoc.setTrangThaiDoiSoat("CHO_XAC_NHAN");
        } else if (phieuGiaoDich instanceof PhieuTraPhiLopHoc phieuTraPhiLopHoc) {
            phieuTraPhiLopHoc.setMaDoiSoat(phieuDoiSoat.getMaDoiSoat());
            phieuTraPhiLopHoc.setTrangThaiDoiSoat("CHO_XAC_NHAN");
        }
    }

    /**
     * Chuyển 1 phiếu đối soát từ CHO_XAC_NHAN sang DANG_XU_LY khi giao dịch
     * liên kết đã đủ điều kiện giải ngân. Tự kiểm tra lại trạng thái hiện tại
     * của phiếu (không chỉ dựa vào trangThaiDoiSoat cache trên giao dịch) để
     * tránh ghi đè nếu QTV Tài chính đã thao tác thủ công (VD: đánh dấu
     * THAT_BAI) trong lúc job đang chạy.
     */
    private void thuChuyenSangDangXuLy(Long maDoiSoat, Object phieuGiaoDich) {
        if (maDoiSoat == null) {
            return;
        }
        phieuDoiSoatKho.findById(maDoiSoat).ifPresent(phieuDoiSoat -> {
            if (phieuDoiSoat.getTrangThai() != TrangThaiDoiSoat.CHO_XAC_NHAN) {
                return;
            }
            phieuDoiSoat.setTrangThai(TrangThaiDoiSoat.DANG_XU_LY);

            if (phieuGiaoDich instanceof PhieuMuaKhoaHoc phieuMuaKhoaHoc) {
                phieuMuaKhoaHoc.setTrangThaiDoiSoat("DANG_XU_LY");
            } else if (phieuGiaoDich instanceof PhieuTraPhiLopHoc phieuTraPhiLopHoc) {
                phieuTraPhiLopHoc.setTrangThaiDoiSoat("DANG_XU_LY");
            }
        });
    }

    /**
     * Tra đúng tỷ lệ phí dịch vụ theo LOẠI GIẤY PHÉP của giao dịch — trước
     * đây toàn bộ hệ thống chỉ có MỘT tỷ lệ phí chung (tyLePhiDichVu), áp
     * dụng nhầm cho cả giao dịch Giấy phép giảng dạy (DAY_HOC), khiến doanh
     * thu thực trả cho giảng viên bị tính sai khi khóa học được bán dưới
     * dạng Giấy phép giảng dạy thay vì Giấy phép cá nhân.
     *
     * - PhieuMuaKhoaHoc: tra theo phieu.getLoaiGiayPhep() (CA_NHAN/DAY_HOC).
     * - PhieuTraPhiLopHoc: KHÔNG có trường loại giấy phép riêng, nhưng về
     *   bản chất lớp học chỉ mở được khi giảng viên đã có Giấy phép giảng
     *   dạy (xem GhiDanhDichVu/kiemTraDieuKienMoLop) — nên phí lớp học luôn
     *   tính theo tỷ lệ DAY_HOC.
     */
    private BigDecimal chonTyLePhi(Object phieuGiaoDich) {
        PhanHoiCauHinhHeThong cauHinh = cauHinhHeThongDichVu.layCauHinh();
        if (phieuGiaoDich instanceof PhieuMuaKhoaHoc phieuMuaKhoaHoc) {
            return phieuMuaKhoaHoc.getLoaiGiayPhep() == LoaiGiayPhep.CA_NHAN
                    ? cauHinh.tyLePhiCaNhan()
                    : cauHinh.tyLePhiDayHoc();
        }
        // PhieuTraPhiLopHoc (hoặc bất kỳ loại giao dịch nào khác trong tương lai
        // gắn với vai trò giảng viên) — cùng nhóm với Giấy phép giảng dạy.
        return cauHinh.tyLePhiDayHoc();
    }
}