package com.elearning.lopHocLMS.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCongKhaiDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.ThongTinNguoiDungCoBan;
import com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongCoQuyen;
import com.elearning.dungChung.ngoaiLe.NgoaiLeKhongTimThay;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiDanhGiaLop;
import com.elearning.lopHocLMS.giaoDien.dto.yeuCau.YeuCauDanhGiaLop;
import com.elearning.lopHocLMS.haTang.DanhGiaLopHocKho;
import com.elearning.lopHocLMS.haTang.LopHocKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.mienDomain.DanhGiaLopHoc;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import com.elearning.lopHocLMS.ungDung.DanhGiaLopHocDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
class DanhGiaLopHocDichVuImpl implements DanhGiaLopHocDichVu {

    private final DanhGiaLopHocKho danhGiaLopHocKho;
    private final LopHocKho lopHocKho;
    private final ThanhVienLopKho thanhVienLopKho;
    private final HoSoCongKhaiDichVu hoSoCongKhaiDichVu;

    @Override
    public PhanHoiDanhGiaLop taoDanhGia(Long maNguoiDung, Long maLopHoc, YeuCauDanhGiaLop yeuCau) {
        if (!thanhVienLopKho.existsByMaHocVienAndMaLopHoc(maNguoiDung, maLopHoc)) {
            throw new NgoaiLeKhongCoQuyen("Bạn phải là thành viên của lớp học để đánh giá");
        }
        LopHoc lop = lopHocKho.findById(maLopHoc)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("lớp học", maLopHoc));

        if (danhGiaLopHocKho.findByMaHocVienAndLopHoc_MaLopHoc(maNguoiDung, maLopHoc).isPresent()) {
            throw new NgoaiLeDuLieuKhongHopLe("Bạn đã đánh giá lớp học này rồi, hãy chỉnh sửa đánh giá hiện có");
        }
        DanhGiaLopHoc dg = new DanhGiaLopHoc(maNguoiDung, lop, yeuCau.soSao(), yeuCau.nhanXet());
        danhGiaLopHocKho.save(dg);
        return sangPhanHoi(dg);
    }

    @Override
    public PhanHoiDanhGiaLop capNhatDanhGia(Long maNguoiDung, Long maDanhGia, YeuCauDanhGiaLop yeuCau) {
        DanhGiaLopHoc dg = timHoacNem(maDanhGia);
        kiemTraQuyenSoHuu(dg, maNguoiDung);
        dg.setSoSao(yeuCau.soSao());
        dg.setNhanXet(yeuCau.nhanXet());
        return sangPhanHoi(dg);
    }

    @Override
    public void xoaDanhGia(Long maNguoiDung, Long maDanhGia) {
        DanhGiaLopHoc dg = timHoacNem(maDanhGia);
        kiemTraQuyenSoHuu(dg, maNguoiDung);
        danhGiaLopHocKho.delete(dg);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhanHoiDanhGiaLop> layDanhGiaTheoLop(Long maLopHoc, Pageable pageable) {
        return danhGiaLopHocKho.findByLopHoc_MaLopHocOrderByNgayTaoDesc(maLopHoc, pageable).map(this::sangPhanHoi);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhanHoiDanhGiaLop> layDanhGiaCuaToi(Long maNguoiDung, Long maLopHoc) {
        return danhGiaLopHocKho.findByMaHocVienAndLopHoc_MaLopHoc(maNguoiDung, maLopHoc).map(this::sangPhanHoi);
    }

    private DanhGiaLopHoc timHoacNem(Long maDanhGia) {
        return danhGiaLopHocKho.findById(maDanhGia)
                .orElseThrow(() -> NgoaiLeKhongTimThay.cuaTaiNguyen("đánh giá", maDanhGia));
    }

    private void kiemTraQuyenSoHuu(DanhGiaLopHoc dg, Long maNguoiDung) {
        if (!dg.getMaHocVien().equals(maNguoiDung)) {
            throw new NgoaiLeKhongCoQuyen("Bạn không phải chủ sở hữu đánh giá này");
        }
    }

    private PhanHoiDanhGiaLop sangPhanHoi(DanhGiaLopHoc d) {
        ThongTinNguoiDungCoBan nd = hoSoCongKhaiDichVu.layThongTinCoBan(d.getMaHocVien()).orElse(null);
        return new PhanHoiDanhGiaLop(d.getMaDanhGia(), d.getMaHocVien(),
                nd != null ? nd.hoTen() : null, nd != null ? nd.anhDaiDien() : null,
                d.getSoSao(), d.getNhanXet(), d.getNgayTao());
    }
}
