package com.elearning.thanhToan.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "phieu_tra_phi_lop_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhieuTraPhiLopHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phieu")
    private Long maPhieu;

    @Column(name = "ma_nguoi_dung", nullable = false)
    private Long maNguoiDung;

    @Column(name = "ma_lop_hoc", nullable = false)
    private Long maLopHoc;

    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal soTien;

    @Column(name = "ma_giao_dich_vnpay", nullable = false, unique = true, length = 100)
    private String maGiaoDichVnpay;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trang_thai", nullable = false, columnDefinition = "trang_thai_gd_enum")
    private TrangThaiGiaoDich trangThai = TrangThaiGiaoDich.CHO_XAC_NHAN;

    @Column(name = "trang_thai_doi_soat", nullable = false, length = 20)
    private String trangThaiDoiSoat = "CHUA_DOI_SOAT";

    @Column(name = "ma_doi_soat")
    private Long maDoiSoat;

    @Column(name = "han_giu_tien")
    private LocalDateTime hanGiuTien;

    @Column(name = "ngay_mua_thanh_cong")
    private LocalDateTime ngayMuaThanhCong;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "du_lieu_dong", columnDefinition = "jsonb")
    private Map<String, Object> duLieuDong = new HashMap<>();

    public PhieuTraPhiLopHoc(Long maNguoiDung, Long maLopHoc, BigDecimal soTien, String maGiaoDichVnpay) {
        this.maNguoiDung = maNguoiDung;
        this.maLopHoc = maLopHoc;
        this.soTien = soTien;
        this.maGiaoDichVnpay = maGiaoDichVnpay;
        this.ngayTao = LocalDateTime.now();
    }

    public boolean daThanhCong() {
        return trangThai == TrangThaiGiaoDich.THANH_CONG;
    }
}