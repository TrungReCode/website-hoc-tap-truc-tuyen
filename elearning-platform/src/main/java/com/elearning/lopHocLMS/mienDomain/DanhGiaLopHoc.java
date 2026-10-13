package com.elearning.lopHocLMS.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "danh_gia_lop_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DanhGiaLopHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_danh_gia")
    private Long maDanhGia;

    @Column(name = "ma_hoc_vien", nullable = false)
    private Long maHocVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lop_hoc", nullable = false)
    private LopHoc lopHoc;

    @Column(name = "so_sao", nullable = false)
    private Integer soSao;

    @Column(name = "nhan_xet", columnDefinition = "TEXT")
    private String nhanXet;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    public DanhGiaLopHoc(Long maHocVien, LopHoc lopHoc, Integer soSao, String nhanXet) {
        this.maHocVien = maHocVien;
        this.lopHoc = lopHoc;
        this.soSao = soSao;
        this.nhanXet = nhanXet;
        this.ngayTao = LocalDateTime.now();
    }
}
