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
@Table(name = "phieu_hoan_tien_lop_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhieuHoanTienLopHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_hoan_tien")
    private Long maHoanTien;

    @Column(name = "ma_phieu_giao_dich", nullable = false)
    private Long maPhieuGiaoDich;

    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal soTien;

    @Column(name = "ly_do", nullable = false, columnDefinition = "TEXT")
    private String lyDo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trang_thai", nullable = false, columnDefinition = "trang_thai_ht_enum")
    private TrangThaiHoanTien trangThai = TrangThaiHoanTien.CHO_XAC_NHAN;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "du_lieu_dong", columnDefinition = "jsonb")
    private Map<String, Object> duLieuDong = new HashMap<>();

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_xu_ly")
    private LocalDateTime ngayXuLy;

    public PhieuHoanTienLopHoc(Long maPhieuGiaoDich, BigDecimal soTien, String lyDo) {
        this.maPhieuGiaoDich = maPhieuGiaoDich;
        this.soTien = soTien;
        this.lyDo = lyDo;
        this.ngayTao = LocalDateTime.now();
    }
}