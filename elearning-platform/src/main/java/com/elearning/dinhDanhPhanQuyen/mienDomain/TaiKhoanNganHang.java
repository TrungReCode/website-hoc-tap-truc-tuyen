package com.elearning.dinhDanhPhanQuyen.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Khớp với bảng tai_khoan_ngan_hang trong V1__tao_bang_nguoi_dung.sql.
 * Thuộc module IAM dù được module thanhToan dùng để đối soát — vì đây
 * là thông tin định danh của giảng viên (chủ tài khoản), không phải
 * dữ liệu giao dịch. Entity PUBLIC (lý do kỹ thuật giống NguoiDung).
 */
@Entity
@Table(name = "tai_khoan_ngan_hang")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaiKhoanNganHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_tai_khoan")
    private Long maTaiKhoan;

    @Column(name = "ma_giang_vien", nullable = false)
    private Long maGiangVien;

    @Column(name = "ten_chu_tai_khoan", nullable = false, length = 100)
    private String tenChuTaiKhoan;

    @Column(name = "so_tai_khoan", nullable = false, length = 30)
    private String soTaiKhoan;

    @Column(name = "ten_ngan_hang", nullable = false, length = 100)
    private String tenNganHang;

    @Column(name = "mac_dinh")
    private Boolean macDinh = true;

    public TaiKhoanNganHang(Long maGiangVien, String tenChuTaiKhoan, String soTaiKhoan, String tenNganHang) {
        this.maGiangVien = maGiangVien;
        this.tenChuTaiKhoan = tenChuTaiKhoan;
        this.soTaiKhoan = soTaiKhoan;
        this.tenNganHang = tenNganHang;
        this.macDinh = true;
    }
}
