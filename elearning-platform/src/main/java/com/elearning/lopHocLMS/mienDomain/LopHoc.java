package com.elearning.lopHocLMS.mienDomain;

import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLop;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Khớp bảng lop_hoc. maKhoaHoc và maGiangVien chỉ lưu BIGINT thuần — KHÔNG
 * map sang Entity KhoaHoc (module choKhoaHoc) hay NguoiDung (module
 * dinhDanhPhanQuyen), đúng quy ước không tham chiếu Entity xuyên module.
 *
 * Lưu ý nghiệp vụ quan trọng (CSDL.md): "Giảng viên phụ trách lớp không
 * nhất thiết là tác giả khóa học" — chỉ cần sở hữu khóa học HOẶC có Giấy
 * phép Giảng dạy (DAY_HOC) là đủ điều kiện mở lớp từ khóa học đó.
 */
@Entity
@Table(name = "lop_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LopHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lop_hoc")
    private Long maLopHoc;

    @Column(name = "ma_khoa_hoc")
    private Long maKhoaHoc;

    @Column(name = "ma_giang_vien", nullable = false)
    private Long maGiangVien;

    @Column(name = "ma_lop", nullable = false, unique = true, length = 20)
    private String maLop;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    /** NULL = không giới hạn sĩ số. */
    @Column(name = "si_so")
    private Integer siSo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trang_thai", nullable = false, columnDefinition = "trang_thai_lop_enum")
    private TrangThaiLop trangThai = TrangThaiLop.SAP_DIEN_RA;

    @Column(name = "phi_lop_hoc")
    private BigDecimal phiLopHoc = BigDecimal.ZERO;

    @Column(name = "mua_khoa_hoc_goc")
    private Boolean muaKhoaHocGoc = true;
    
    @Column(name = "cong_khai")
    private Boolean congKhai = true;

    /** % buổi học tối thiểu giảng viên phải hoàn thành trước khi được giải ngân. */
    @Column(name = "ti_le_hoan_thanh", nullable = false)
    private Integer tiLeHoanThanh = 80;

    public LopHoc(Long maKhoaHoc, Long maGiangVien, String maLop, LocalDateTime ngayBatDau,
            LocalDateTime ngayKetThuc, Integer siSo, BigDecimal phiLopHoc, Boolean congKhai) {
        this.maKhoaHoc = maKhoaHoc;
        this.maGiangVien = maGiangVien;
        this.maLop = maLop;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.siSo = siSo;
        this.phiLopHoc = phiLopHoc != null ? phiLopHoc : BigDecimal.ZERO;
        this.congKhai = congKhai != null ? congKhai : true;
        this.trangThai = TrangThaiLop.SAP_DIEN_RA;
    }

    public boolean laGiangVien(Long maNguoiDung) {
        return this.maGiangVien.equals(maNguoiDung);
    }

    public boolean coMienPhi() {
        return phiLopHoc == null || phiLopHoc.compareTo(BigDecimal.ZERO) == 0;
    }
}
