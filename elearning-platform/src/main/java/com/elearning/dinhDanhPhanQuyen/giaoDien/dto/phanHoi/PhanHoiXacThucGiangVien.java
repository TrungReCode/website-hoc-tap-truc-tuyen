package com.elearning.dinhDanhPhanQuyen.giaoDien.dto.phanHoi;

import com.elearning.dinhDanhPhanQuyen.mienDomain.enumDomain.TrangThaiXacThucGiangVien;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PhanHoiXacThucGiangVien(
        Long maXacThuc,
        Long maNguoiDung,
        String hoTenNguoiDung, // null khi tự xem trạng thái của chính mình (không cần thiết); có giá trị khi admin xem danh sách
        String emailNguoiDung,
        LocalDate ngaySinh,
        Integer tuoi, // tính sẵn phía backend, tránh frontend tự tính sai múi giờ/năm nhuận
        String bangCap,
        String tenFileGoc,
        TrangThaiXacThucGiangVien trangThai,
        String lyDoTuChoi,
        LocalDateTime ngayGui,
        LocalDateTime ngayXuLy
) {
}
