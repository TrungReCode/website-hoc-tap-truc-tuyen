package com.elearning.lopHocLMS.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tien_do_bai_bo_sung")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TienDoBaiBoSung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_tien_do_bo_sung")
    private Long maTienDoBoSung;

    @Column(name = "ma_hoc_vien", nullable = false)
    private Long maHocVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_hoc_bo_sung")
    private BaiHocBoSung baiHocBoSung;

    @Column(name = "hoan_thanh")
    private Boolean hoanThanh = false;

    @Column(name = "vi_tri_dung")
    private Long viTriDung;

    @Column(name = "ngay_hoan_thanh")
    private LocalDateTime ngayHoanThanh;

    public TienDoBaiBoSung(Long maHocVien, BaiHocBoSung baiHocBoSung) {
        this.maHocVien = maHocVien;
        this.baiHocBoSung = baiHocBoSung;
        this.hoanThanh = false;
    }

    public void danhDauHoanThanh() {
        this.hoanThanh = true;
        this.ngayHoanThanh = LocalDateTime.now();
    }
}
