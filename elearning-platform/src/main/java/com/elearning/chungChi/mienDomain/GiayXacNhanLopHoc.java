package com.elearning.chungChi.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Giấy xác nhận hoàn thành lớp học (có giảng viên phụ trách). */
@Entity
@Table(name = "giay_xac_nhan_lop_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GiayXacNhanLopHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_giay")
    private Long maGiay;

    @Column(name = "ma_xac_thuc", nullable = false, unique = true, length = 50)
    private String maXacThuc;

    @Column(name = "ma_hoc_vien", nullable = false)
    private Long maHocVien;

    @Column(name = "ma_lop_hoc", nullable = false)
    private Long maLopHoc;

    @Column(name = "phan_tram_hoan_thanh", nullable = false)
    private BigDecimal phanTramHoanThanh;

    @Column(name = "diem_trung_binh")
    private BigDecimal diemTrungBinh;

    @Column(name = "ngay_cap", nullable = false)
    private LocalDateTime ngayCap = LocalDateTime.now();

    @Column(name = "file_pdf_url")
    private String filePdfUrl;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "HOP_LE";

    public GiayXacNhanLopHoc(String maXacThuc, Long maHocVien, Long maLopHoc,
                              BigDecimal phanTramHoanThanh, BigDecimal diemTrungBinh) {
        this.maXacThuc = maXacThuc;
        this.maHocVien = maHocVien;
        this.maLopHoc = maLopHoc;
        this.phanTramHoanThanh = phanTramHoanThanh;
        this.diemTrungBinh = diemTrungBinh;
        this.ngayCap = LocalDateTime.now();
        this.trangThai = "HOP_LE";
    }
}
