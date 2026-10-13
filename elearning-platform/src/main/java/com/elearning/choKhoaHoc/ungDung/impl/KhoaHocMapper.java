package com.elearning.choKhoaHoc.ungDung.impl;

import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiChuong;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiDanhSachKhoaHoc;
import com.elearning.choKhoaHoc.giaoDien.dto.phanHoi.PhanHoiKhoaHoc;
import com.elearning.choKhoaHoc.haTang.BaiHocKho;
import com.elearning.choKhoaHoc.haTang.DanhGiaKhoaHocKho;
import com.elearning.choKhoaHoc.mienDomain.KhoaHoc;
import com.elearning.choKhoaHoc.ungDung.QuanLyNoiDungDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.HoSoCongKhaiDichVu;
import com.elearning.dinhDanhPhanQuyen.ungDung.ThongTinNguoiDungCoBan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class KhoaHocMapper {

    private final BaiHocKho baiHocKho;
    private final DanhGiaKhoaHocKho danhGiaKhoaHocKho;
    private final HoSoCongKhaiDichVu hoSoCongKhaiDichVu;
    private final QuanLyNoiDungDichVu quanLyNoiDungDichVu;

    PhanHoiKhoaHoc sangPhanHoiChiTiet(KhoaHoc k) {
        return dungGiaoTrinh(k, quanLyNoiDungDichVu.layGiaoTrinh(k.getMaKhoaHoc()));
    }

    /**
     * Như sangPhanHoiChiTiet nhưng dùng giáo trình đã lọc bài ẩn
     * (layGiaoTrinhHienThi) — dùng cho các luồng hiển thị cho học viên/khách
     * xem (xem QuanLyNoiDungDichVu.layGiaoTrinhHienThi để biết vì sao cần
     * tách riêng khỏi bản dùng cho Giảng viên/Admin).
     */
    PhanHoiKhoaHoc sangPhanHoiChiTietHienThi(KhoaHoc k) {
        return dungGiaoTrinh(k, quanLyNoiDungDichVu.layGiaoTrinhHienThi(k.getMaKhoaHoc()));
    }

    private PhanHoiKhoaHoc dungGiaoTrinh(KhoaHoc k, List<PhanHoiChuong> giaoTrinh) {
        ThongTinNguoiDungCoBan tacGia = hoSoCongKhaiDichVu.layThongTinCoBan(k.getMaTacGia()).orElse(null);
        Double diemTrungBinh = danhGiaKhoaHocKho.tinhDiemTrungBinh(k.getMaKhoaHoc());
        long soLuongDanhGia = danhGiaKhoaHocKho.countByKhoaHoc_MaKhoaHocAndAnFalse(k.getMaKhoaHoc());

        return new PhanHoiKhoaHoc(
                k.getMaKhoaHoc(),
                k.getMaTacGia(),
                tacGia != null ? tacGia.hoTen() : null,
                tacGia != null ? tacGia.anhDaiDien() : null,
                k.getTieuDe(),
                k.getMieuTa(),
                k.getAnhBia(),
                k.getDanhMuc().getMaDanhMuc(),
                k.getDanhMuc().getTenDanhMuc(),
                k.getGia(),
                k.getPhiDayHoc(),
                k.getTrangThai(),
                k.getLyDoTuChoi(),
                k.getNgayTao(),
                k.getNgayCapNhat(),
                diemTrungBinh,
                soLuongDanhGia,
                giaoTrinh
        );
    }

    PhanHoiDanhSachKhoaHoc sangPhanHoiDanhSach(KhoaHoc k) {
        Optional<ThongTinNguoiDungCoBan> tacGia = hoSoCongKhaiDichVu.layThongTinCoBan(k.getMaTacGia());
        Double diemTrungBinh = danhGiaKhoaHocKho.tinhDiemTrungBinh(k.getMaKhoaHoc());
        long soLuongDanhGia = danhGiaKhoaHocKho.countByKhoaHoc_MaKhoaHocAndAnFalse(k.getMaKhoaHoc());
        long soBaiHoc = baiHocKho.countByChuong_KhoaHoc_MaKhoaHoc(k.getMaKhoaHoc());

        return new PhanHoiDanhSachKhoaHoc(
                k.getMaKhoaHoc(),
                k.getTieuDe(),
                k.getAnhBia(),
                k.getDanhMuc().getTenDanhMuc(),
                k.getMaTacGia(),
                tacGia.map(ThongTinNguoiDungCoBan::hoTen).orElse(null),
                k.getGia(),
                k.getPhiDayHoc(),
                k.getTrangThai(),
                diemTrungBinh,
                soLuongDanhGia,
                soBaiHoc,
                k.getNgayTao()
        );
    }
}
