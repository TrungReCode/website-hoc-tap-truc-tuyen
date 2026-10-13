package com.elearning.thanhToan.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "phieu_doi_soat")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhieuDoiSoat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_doi_soat")
    private Long maDoiSoat;

    @Column(name = "ma_tai_khoan", nullable = false)
    private Long maTaiKhoan;

    @Column(name = "hoa_hong", nullable = false, precision = 5, scale = 2)
    private BigDecimal hoaHong = new BigDecimal("10.00");

    @Column(name = "tong_doanh_thu", nullable = false, precision = 15, scale = 2)
    private BigDecimal tongDoanhThu;

    @Column(name = "so_tien_thuc_te", nullable = false, precision = 15, scale = 2)
    private BigDecimal soTienThucTe;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trang_thai", columnDefinition = "trang_thai_ds_enum")
    private TrangThaiDoiSoat trangThai;

    @Column(name = "ma_giao_dich_ngan_hang", length = 100)
    private String maGiaoDichNganHang;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_chuyen_thanh_cong")
    private LocalDateTime ngayChuyenThanhCong;

    public PhieuDoiSoat(Long maTaiKhoan, BigDecimal hoaHong, BigDecimal tongDoanhThu, BigDecimal soTienThucTe) {
        this.maTaiKhoan = maTaiKhoan;
        this.hoaHong = hoaHong;
        this.tongDoanhThu = tongDoanhThu;
        this.soTienThucTe = soTienThucTe;
        this.ngayTao = LocalDateTime.now();
    }
}