package com.elearning.lopHocLMS.ungDung.impl;

import com.elearning.choKhoaHoc.ungDung.TimKiemKhoaHocDichVu;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauTaoLop;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.haTang.QuyenSuDungKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiGiayPhep;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import com.elearning.lopHocLMS.ungDung.TaoLopHocDichVu;
import com.elearning.thanhToan.haTang.PhieuHoanTienLopHocKho;
import com.elearning.thanhToan.haTang.PhieuTraPhiLopHocKho;
import com.elearning.thanhToan.mienDomain.PhieuHoanTienLopHoc;
import com.elearning.thanhToan.mienDomain.PhieuTraPhiLopHoc;
import com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiThongBao;
import com.elearning.traoDoiTrongLop.ungDung.ThongBaoDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
class TaoLopHocDichVuImpl implements TaoLopHocDichVu {

    private static final String BANG_CHU = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // bỏ ký tự dễ nhầm (I,O,0,1)
    private static final SecureRandom NGAU_NHIEN = new SecureRandom();
    private static final List<TrangThaiLop> TRANG_THAI_DANG_HOAT_DONG =
            List.of(TrangThaiLop.SAP_DIEN_RA, TrangThaiLop.HOAT_DONG);

    private final LopHocKho lopHocKho;
    private final ThanhVienLopKho thanhVienLopKho;
    private final QuyenSuDungKho quyenSuDungKho;
    private final TimKiemKhoaHocDichVu timKiemKhoaHocDichVu;
    private final LopMapper lopMapper;
    private final PhieuTraPhiLopHocKho phieuTraPhiLopHocKho;
    private final PhieuHoanTienLopHocKho phieuHoanTienLopHocKho;
    private final ThongBaoDichVu thongBaoDichVu;

@Override
    public PhanHoiLop taoLop(Long maNguoiDung, YeuCauTaoLop yeuCau) {
        if (!timKiemKhoaHocDichVu.khoaHocDangCongKhai(yeuCau.maKhoaHoc())) {
            throw new NgoaiLeDuLieuKhongHopLe("Chỉ có thể mở lớp từ khóa học đang ở trạng thái công khai");
        }
        
        // Cập nhật kiểm tra quyền với cờ muaKhoaHocGoc
        kiemTraDuDieuKienMoLop(maNguoiDung, yeuCau.maKhoaHoc(), yeuCau.muaKhoaHocGoc());

        LopHoc lopHoc = new LopHoc(
                yeuCau.maKhoaHoc(), maNguoiDung, sinhMaLopDuyNhat(),
                yeuCau.ngayBatDau(), yeuCau.ngayKetThuc(), yeuCau.siSo(),
                yeuCau.phiLopHoc(), yeuCau.congKhai());
        
        // Thiết lập cờ từ yêu cầu
        lopHoc.setMuaKhoaHocGoc(Boolean.TRUE.equals(yeuCau.muaKhoaHocGoc()));
        
        lopHocKho.save(lopHoc);

        return lopMapper.sangPhanHoiLop(lopHoc);
    }

    private void kiemTraDuDieuKienMoLop(Long maNguoiDung, Long maKhoaHoc, Boolean muaKhoaHocGoc) {
        boolean laTacGia = timKiemKhoaHocDichVu.laTacGiaKhoaHoc(maNguoiDung, maKhoaHoc);
        boolean coGiayPhepDayHoc = quyenSuDungKho.existsByMaNguoiDungAndMaKhoaHocAndLoaiGiayPhep(
                maNguoiDung, maKhoaHoc, LoaiGiayPhep.DAY_HOC);
        
        if (!laTacGia && !coGiayPhepDayHoc) {
            throw new NgoaiLeKhongCoQuyen(
                    "Bạn cần là tác giả khóa học hoặc sở hữu Giấy phép Giảng dạy để mở lớp từ khóa học này");
        }
        
        // Logic kiểm tra đặc quyền: Chỉ tác giả gốc mới được tắt yêu cầu mua khóa gốc
        if (Boolean.FALSE.equals(muaKhoaHocGoc) && !laTacGia) {
            throw new NgoaiLeKhongCoQuyen(
                    "Chỉ Tác giả gốc của khóa học mới có đặc quyền tắt yêu cầu mua khóa học nền tảng cho học viên.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiLop> layLopCuaGiangVien(Long maNguoiDung) {
        return lopHocKho.findByMaGiangVien(maNguoiDung).stream().map(lopMapper::sangPhanHoiLop).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PhanHoiLop layChiTietChoGiangVien(Long maNguoiDung, Long maLopHoc) {
        LopHoc lop = timHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDung);
        return lopMapper.sangPhanHoiLop(lop);
    }

    @Override
    public void xoaLop(Long maNguoiDung, Long maLopHoc) {
        LopHoc lop = timHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDung);

        long soThanhVien = thanhVienLopKho.countByMaLopHoc(maLopHoc);

        if (soThanhVien == 0) {
            // Chưa ai tham gia -> chưa phát sinh giao dịch nào cần hoàn tiền, xóa vật lý an toàn.
            lopHocKho.delete(lop);
            return;
        }

        if (lop.getTrangThai() != TrangThaiLop.SAP_DIEN_RA) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Không thể xóa lớp đã có học viên tham gia — hãy đóng lớp thay vì xóa");
        }

        // Lớp chưa khai giảng nhưng đã có học viên ghi danh -> đây là luồng "Hủy lớp
        // chưa khai giảng": chuyển trạng thái sang Đã đóng và tự động phát sinh phiếu
        // hoàn tiền cho toàn bộ học viên đã đóng phụ phí trước đó.
        huyLopChuaKhaiGiang(lop);
    }

    private void huyLopChuaKhaiGiang(LopHoc lop) {
        lop.setTrangThai(TrangThaiLop.DA_DONG);

        List<PhieuTraPhiLopHoc> phieuDaThanhToan = phieuTraPhiLopHocKho
                .findByMaLopHocAndTrangThai(lop.getMaLopHoc(), TrangThaiGiaoDich.THANH_CONG);

        for (PhieuTraPhiLopHoc phieu : phieuDaThanhToan) {
            if (phieuHoanTienLopHocKho.existsByMaPhieuGiaoDich(phieu.getMaPhieu())) {
                continue; // đã có phiếu hoàn tiền cho giao dịch này, tránh tạo trùng
            }

            PhieuHoanTienLopHoc hoanTien = new PhieuHoanTienLopHoc(
                    phieu.getMaPhieu(), phieu.getSoTien(),
                    "Tự động hoàn phụ phí do giảng viên hủy lớp học trước khi khai giảng");
            hoanTien.getDuLieuDong().put("maNguoiDung", phieu.getMaNguoiDung());
            hoanTien.getDuLieuDong().put("maGiaoDichVnpay", phieu.getMaGiaoDichVnpay());
            hoanTien.getDuLieuDong().put("loaiGiaoDich", "LOP_HOC");
            hoanTien.getDuLieuDong().put("tuDongPhatSinh", true);
            phieuHoanTienLopHocKho.save(hoanTien);

            thongBaoDichVu.guiThongBao(phieu.getMaNguoiDung(), LoaiThongBao.LOP_HOC_DA_HUY,
                    "Lớp học đã bị hủy",
                    "Lớp " + lop.getMaLop() + " đã bị giảng viên hủy trước khi khai giảng. "
                            + "Bạn đã được gỡ khỏi lớp; phụ phí đã đóng sẽ được hoàn lại qua VNPay "
                            + "sau khi bộ phận tài chính xử lý.",
                    Map.of("maLopHoc", lop.getMaLopHoc(), "maLop", lop.getMaLop()));
        }

        // FIX: gỡ quyền truy cập nội dung NGAY khi hủy, không chờ tài chính xử lý
        // xong hoàn tiền — trước đây ThanhVienLop chỉ bị xóa khi QTV_TAI_CHINH duyệt
        // hoàn tiền (xem TaiChinhDichVuImpl.xuLyHoanTienLopHoc), tạo ra khoảng thời
        // gian học viên đang chờ hoàn tiền vẫn xem/đánh dấu hoàn thành được nội dung
        // của 1 lớp đã hủy (GiaoTrinhLaiDichVuImpl, TienDoHocDichVuImpl chỉ chặn
        // SAP_DIEN_RA, không chặn DA_DONG). Áp dụng cho TOÀN BỘ thành viên, kể cả
        // người chưa từng trả phí (được thêm trực tiếp qua lời mời) — lớp đã hủy
        // thì không ai còn quyền học nữa, bất kể có phát sinh hoàn tiền hay không.
        thanhVienLopKho.deleteAll(thanhVienLopKho.findByMaLopHoc(lop.getMaLopHoc()));
    }

    private String sinhMaLopDuyNhat() {
        String ma;
        do {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(BANG_CHU.charAt(NGAU_NHIEN.nextInt(BANG_CHU.length())));
            }
            ma = sb.toString();
        } while (lopHocKho.existsByMaLop(ma));
        return ma;
    }

    private LopHoc timHoacNem(Long maLopHoc) {
        return lopHocKho.findById(maLopHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));
    }

    private void kiemTraQuyenGiangVien(LopHoc lop, Long maNguoiDung) {
        if (!lop.laGiangVien(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải giảng viên phụ trách lớp học này");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean laGiangVienCuaLop(Long maNguoiDung, Long maLopHoc) {
        return lopHocKho.findById(maLopHoc)
                .map(lop -> lop.laGiangVien(maNguoiDung))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiLop> layDanhSachChoAdmin(TrangThaiLop trangThai, String maLop, Pageable pageable) {
        String tenTrangThai = trangThai == null ? null : trangThai.name();
        String maLopSach = (maLop == null || maLop.isBlank()) ? null : maLop.trim();
        return lopHocKho.timKiemChoAdmin(tenTrangThai, maLopSach, pageable)
                .map(lopMapper::sangPhanHoiLop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhanHoiLop> layLopTheoKhoaHocChoTacGia(Long maNguoiDungTacGia, Long maKhoaHoc) {
        if (!timKiemKhoaHocDichVu.laTacGiaKhoaHoc(maNguoiDungTacGia, maKhoaHoc)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải tác giả của khóa học này");
        }
        return lopHocKho.findByMaKhoaHoc(maKhoaHoc).stream()
                .map(lopMapper::sangPhanHoiLop)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiLop> layDanhSachCongKhai(Long maKhoaHoc, String tuKhoa, String gia, Pageable pageable) {
        String tuKhoaSach = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        String giaSach = (gia == null || gia.isBlank()) ? null : gia.trim();

        boolean coLocTuKhoa = tuKhoaSach != null;
        List<Long> dsMaKhoaHocKhopTuKhoa = List.of();
        if (coLocTuKhoa) {
            // Tìm theo TÊN KHÓA HỌC thay vì mã lớp (mã lớp ngẫu nhiên 8 ký tự,
            // học viên chưa tham gia lớp không biết để tìm kiếm).
            dsMaKhoaHocKhopTuKhoa = timKiemKhoaHocDichVu.timMaKhoaHocKhopTuKhoa(tuKhoaSach);
            if (dsMaKhoaHocKhopTuKhoa.isEmpty()) {
                // Không có khóa học nào khớp -> chắc chắn không có lớp nào khớp,
                // trả rỗng ngay không cần query LopHoc.
                return Page.empty(pageable);
            }
        }

        return lopHocKho.timKiemCongKhai(TRANG_THAI_DANG_HOAT_DONG, maKhoaHoc, coLocTuKhoa, dsMaKhoaHocKhopTuKhoa, giaSach, pageable)
                .map(lopMapper::sangPhanHoiLop);
    }
}