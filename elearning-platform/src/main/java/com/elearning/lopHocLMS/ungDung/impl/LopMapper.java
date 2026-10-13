package com.elearning.lopHocLMS.ungDung.impl;

import com.elearning.choKhoaHoc.ungDung.ThongTinKhoaHocCoBan;
import com.elearning.choKhoaHoc.ungDung.TimKiemKhoaHocDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCongKhaiDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.ThongTinNguoiDungCoBan;
import com.elearning.lopHocLMS.giaoDien.dto.phanHoi.PhanHoiLop;
import com.elearning.lopHocLMS.haTang.DanhGiaLopHocKho;
import com.elearning.lopHocLMS.haTang.ThanhVienLopKho;
import com.elearning.lopHocLMS.mienDomain.LopHoc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class LopMapper {

    private final ThanhVienLopKho thanhVienLopKho;
    private final DanhGiaLopHocKho danhGiaLopHocKho;
    private final TimKiemKhoaHocDichVu timKiemKhoaHocDichVu;
    private final HoSoCongKhaiDichVu hoSoCongKhaiDichVu;

    PhanHoiLop sangPhanHoiLop(LopHoc lop) {
        return sangPhanHoiLop(lop, null);
    }

    PhanHoiLop sangPhanHoiLop(LopHoc lop, Integer tienDoPhanTramHocVien) {
        ThongTinKhoaHocCoBan khoaHoc = lop.getMaKhoaHoc() != null
                ? timKiemKhoaHocDichVu.layThongTinCoBan(lop.getMaKhoaHoc()).orElse(null)
                : null;
        ThongTinNguoiDungCoBan giangVien = hoSoCongKhaiDichVu.layThongTinCoBan(lop.getMaGiangVien()).orElse(null);
        long soThanhVien = thanhVienLopKho.countByMaLopHoc(lop.getMaLopHoc());
        Double diemTrungBinh = danhGiaLopHocKho.tinhDiemTrungBinh(lop.getMaLopHoc());
        long soLuongDanhGia = danhGiaLopHocKho.countByLopHoc_MaLopHoc(lop.getMaLopHoc());

        return new PhanHoiLop(
                lop.getMaLopHoc(),
                lop.getMaKhoaHoc(),
                khoaHoc != null ? khoaHoc.tieuDe() : null,
                khoaHoc != null ? khoaHoc.anhBia() : null,
                lop.getMaGiangVien(),
                giangVien != null ? giangVien.hoTen() : null,
                lop.getMaLop(),
                lop.getNgayBatDau(),
                lop.getNgayKetThuc(),
                lop.getSiSo(),
                soThanhVien,
                lop.getTrangThai(),
                lop.getPhiLopHoc(),
                lop.getCongKhai(),
                lop.getTiLeHoanThanh(),
                tienDoPhanTramHocVien,
                diemTrungBinh,
                soLuongDanhGia
        );
    }
}
