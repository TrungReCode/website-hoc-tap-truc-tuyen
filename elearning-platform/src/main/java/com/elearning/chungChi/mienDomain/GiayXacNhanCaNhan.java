package com.elearning.chungChi.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Giấy xác nhận hoàn thành khóa học (tự học — Giấy phép cá nhân). */
@Entity
@Table(name = "giay_xac_nhan_ca_nhan")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GiayXacNhanCaNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_giay")
    private Long maGiay;

    @Column(name = "ma_xac_thuc", nullable = false, unique = true, length = 50)
    private String maXacThuc;

    @Column(name = "ma_hoc_vien", nullable = false)
    private Long maHocVien;

    @Column(name = "ma_khoa_hoc", nullable = false)
    private Long maKhoaHoc;

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

    /** HOP_LE | DA_THU_HOI */
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "HOP_LE";

    public GiayXacNhanCaNhan(String maXacThuc, Long maHocVien, Long maKhoaHoc,
                              BigDecimal phanTramHoanThanh, BigDecimal diemTrungBinh) {
        this.maXacThuc = maXacThuc;
        this.maHocVien = maHocVien;
        this.maKhoaHoc = maKhoaHoc;
        this.phanTramHoanThanh = phanTramHoanThanh;
        this.diemTrungBinh = diemTrungBinh;
        this.ngayCap = LocalDateTime.now();
        this.trangThai = "HOP_LE";
    }
}
