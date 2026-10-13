package com.elearning.lopHocLMS.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Khớp bảng cau_hinh_bai_hoc. maBaiHoc tham chiếu bai_hoc của module
 * choKhoaHoc — chỉ lưu BIGINT thuần, không map Entity xuyên module.
 * Khi render giáo trình lai: ưu tiên viTriMoi nếu khác NULL, ngược lại
 * dùng vị trí gốc lấy được qua QuanLyNoiDungDichVu (choKhoaHoc).
 */
@Entity
@Table(name = "cau_hinh_bai_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CauHinhBaiGoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_cau_hinh")
    private Long maCauHinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lop_hoc", nullable = false)
    private LopHoc lopHoc;

    @Column(name = "ma_bai_hoc", nullable = false)
    private Long maBaiHoc;

    @Column(name = "an_bai_hoc", nullable = false)
    private Boolean anBaiHoc = false;

    @Column(name = "vi_tri_moi")
    private Float viTriMoi;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    public CauHinhBaiGoc(LopHoc lopHoc, Long maBaiHoc) {
        this.lopHoc = lopHoc;
        this.maBaiHoc = maBaiHoc;
        this.anBaiHoc = false;
        this.ngayCapNhat = LocalDateTime.now();
    }
}
