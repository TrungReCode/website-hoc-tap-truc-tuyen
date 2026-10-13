package com.elearning.chungChi.ungDung;

import com.elearning.chungChi.giaoDien.dto.phanHoi.PhanHoiGiayXacNhan;
import com.elearning.chungChi.giaoDien.dto.phanHoi.PhanHoiTienDo;

import java.util.List;
import java.util.Optional;

public interface GiayXacNhanDichVu {

    /** Tính lại (không cấp giấy) tiến độ tự học của 1 học viên cho 1 khóa học. */
    PhanHoiTienDo tinhLaiTienDoCaNhan(Long maHocVien, Long maKhoaHoc);

    /** Tính lại (không cấp giấy) tiến độ của 1 học viên trong 1 lớp học. */
    PhanHoiTienDo tinhLaiTienDoLop(Long maHocVien, Long maLopHoc);

    /**
     * Tính lại tiến độ mới nhất rồi cấp Giấy xác nhận hoàn thành khóa học
     * (tự học) nếu đủ điều kiện. Idempotent — gọi lại nhiều lần chỉ trả về
     * đúng 1 giấy đã cấp (UNIQUE ma_hoc_vien+ma_khoa_hoc), không cấp trùng.
     *
     * @throws com.elearning.dungChung.ngoaiLe.NgoaiLeDuLieuKhongHopLe nếu chưa đủ điều kiện
     */
    PhanHoiGiayXacNhan capGiayCaNhan(Long maHocVien, Long maKhoaHoc);

    /** Tương tự capGiayCaNhan, cho Giấy xác nhận hoàn thành lớp học. */
    PhanHoiGiayXacNhan capGiayLopHoc(Long maHocVien, Long maLopHoc);

    List<PhanHoiGiayXacNhan> layDanhSachGiayCuaHocVien(Long maHocVien);

    /** Tra cứu công khai bằng mã xác thực in trên giấy/QR — không yêu cầu đăng nhập. */
    Optional<PhanHoiGiayXacNhan> xacThuc(String maXacThuc);

    /** Trả về URL R2 đã lưu — controller dùng để redirect (302), không proxy qua backend. */
    String layUrlFilePdf(String maXacThuc);
}