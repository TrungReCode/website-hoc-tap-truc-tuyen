package com.elearning.lopHocLMS.ungDung.impl;

import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauCapNhatLop;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import com.elearning.lopHocLMS.ungDung.CauHinhLopDichVu;
import com.elearning.thanhToan.haTang.PhieuTraPhiLopHocKho;
import com.elearning.thanhToan.mienDomain.TrangThaiGiaoDich;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
class CauHinhLopDichVuImpl implements CauHinhLopDichVu {

    private final LopHocKho lopHocKho;
    private final LopMapper lopMapper;
    private final ThanhVienLopKho thanhVienLopKho;
    private final PhieuTraPhiLopHocKho phieuTraPhiLopHocKho;

    @Override
    public PhanHoiLop capNhatCauHinh(Long maNguoiDung, Long maLopHoc, YeuCauCapNhatLop yeuCau) {
        LopHoc lop = timHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDung);

        if (lop.getTrangThai() == TrangThaiLop.DA_DONG) {
            throw new NgoaiLeDuLieuKhongHopLe("Lớp học đã đóng, không thể chỉnh sửa cấu hình");
        }

        // Ràng buộc 1: được phép tăng sĩ số, nhưng nếu giảm thì không được thấp
        // hơn số học viên thực tế đang ghi danh.
        if (yeuCau.siSo() != null) {
            if (yeuCau.siSo() <= 0) {
                throw new NgoaiLeDuLieuKhongHopLe("Sĩ số lớp học phải lớn hơn 0");
            }

            Integer siSoCu = lop.getSiSo();
            long soDangGhiDanh = thanhVienLopKho.countByMaLopHoc(maLopHoc);

            // Lớp đang hoạt động: chỉ được TĂNG sĩ số
            if (lop.getTrangThai() == TrangThaiLop.HOAT_DONG
                    && siSoCu != null
                    && yeuCau.siSo() < siSoCu) {

                throw new NgoaiLeDuLieuKhongHopLe(
                        "Lớp đang hoạt động chỉ được tăng sĩ số, không thể giảm từ "
                                + siSoCu + " xuống " + yeuCau.siSo());
            }

            // Lớp sắp diễn ra: được tăng/giảm nhưng không thấp hơn
            // số học viên đang ghi danh
            if (lop.getTrangThai() == TrangThaiLop.SAP_DIEN_RA
                    && yeuCau.siSo() < soDangGhiDanh) {

                throw new NgoaiLeDuLieuKhongHopLe(
                        "Không thể giảm sĩ số xuống dưới " + soDangGhiDanh
                                + " — đây là số học viên thực tế đang ghi danh trong lớp");
            }
        }

        // Ràng buộc 2: không cho sửa học phí bổ sung nếu lớp đã kết thúc hoặc
        // đang nằm trong chu kỳ giải ngân tài chính (đã có phiếu được đối soát).
        if (daThayDoiHocPhi(lop.getPhiLopHoc(), yeuCau.phiLopHoc())) {
            boolean daKetThuc = lop.getNgayKetThuc() != null && lop.getNgayKetThuc().isBefore(LocalDateTime.now());
            boolean dangGiaiNgan = phieuTraPhiLopHocKho.existsByMaLopHocAndMaDoiSoatIsNotNull(maLopHoc);
            if (daKetThuc || dangGiaiNgan) {
                throw new NgoaiLeDuLieuKhongHopLe(
                        "Không thể sửa học phí vì lớp học đã kết thúc hoặc đang trong chu kỳ giải ngân tài chính");
            }
        }

        lop.setNgayBatDau(yeuCau.ngayBatDau());
        lop.setNgayKetThuc(yeuCau.ngayKetThuc());
        lop.setSiSo(yeuCau.siSo());
        lop.setPhiLopHoc(yeuCau.phiLopHoc());
        if (yeuCau.congKhai() != null)
            lop.setCongKhai(yeuCau.congKhai());
        if (yeuCau.tiLeHoanThanh() != null)
            lop.setTiLeHoanThanh(yeuCau.tiLeHoanThanh());

        return lopMapper.sangPhanHoiLop(lop);
    }

    private boolean daThayDoiHocPhi(BigDecimal hocPhiCu, BigDecimal hocPhiMoi) {
        if (hocPhiCu == null && hocPhiMoi == null) {
            return false;
        }
        if (hocPhiCu == null || hocPhiMoi == null) {
            return true;
        }
        return hocPhiCu.compareTo(hocPhiMoi) != 0;
    }

    @Override
    public PhanHoiLop dongLop(Long maNguoiDung, Long maLopHoc) {
        LopHoc lop = timHoacNem(maLopHoc);
        kiemTraQuyenGiangVien(lop, maNguoiDung);

        if (lop.getTrangThai() == TrangThaiLop.DA_DONG) {
            throw new NgoaiLeDuLieuKhongHopLe("Lớp học đã đóng từ trước");
        }
        // Chặn "Đóng lớp" bỏ qua cơ chế hoàn tiền tự động: nếu lớp CHƯA khai giảng
        // mà đã có người trả phí thành công, đây thực chất là một lượt HỦY lớp
        // (không phải chỉ ngừng nhận thêm học viên như khi lớp đang HOAT_DONG) —
        // bắt buộc phải đi qua xoaLop()/huyLopChuaKhaiGiang() để tự động phát
        // sinh phiếu hoàn tiền, thay vì đóng "chay" khiến học viên mất tiền mà
        // không có phiếu hoàn tiền nào được tạo.
        if (lop.getTrangThai() == TrangThaiLop.SAP_DIEN_RA
                && phieuTraPhiLopHocKho.existsByMaLopHocAndTrangThai(maLopHoc, TrangThaiGiaoDich.THANH_CONG)) {
            throw new NgoaiLeDuLieuKhongHopLe(
                    "Lớp chưa khai giảng đã có học viên đóng phí — vui lòng dùng chức năng \"Hủy lớp\" "
                            + "để hệ thống tự động phát sinh hoàn tiền, không dùng \"Đóng lớp\"");
        }
        lop.setTrangThai(TrangThaiLop.DA_DONG);
        return lopMapper.sangPhanHoiLop(lop);
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
}