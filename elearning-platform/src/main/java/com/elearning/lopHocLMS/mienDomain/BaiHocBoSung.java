package com.elearning.lopHocLMS.mienDomain;

import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiHocLieuBoSung;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "bai_hoc_bo_sung")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaiHocBoSung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bai_hoc_bo_sung")
    private Long maBaiHocBoSung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lop_hoc", nullable = false)
    private LopHoc lopHoc;

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false, length = 20)
    private LoaiHocLieuBoSung loai;

    @Column(name = "vi_tri", nullable = false)
    private Float viTri;

    @Column(name = "an_hien")
    private Boolean anHien = true;

    /** URL Cloudflare R2, hoặc URL trực tiếp do giảng viên nhập nếu loai = LINK. */
    @Column(name = "duong_dan", nullable = false, columnDefinition = "TEXT")
    private String duongDan;

    @Column(name = "loai_tep", length = 10)
    private String loaiTep;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "du_lieu_dong", columnDefinition = "jsonb")
    private Map<String, Object> duLieuDong = new HashMap<>();

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    public BaiHocBoSung(LopHoc lopHoc, String tieuDe, String moTa, LoaiHocLieuBoSung loai, Float viTri) {
        this.lopHoc = lopHoc;
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.loai = loai;
        this.viTri = viTri;
        this.anHien = true;
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
    }

    public void danhDauCapNhat() {
        this.ngayCapNhat = LocalDateTime.now();
    }
}