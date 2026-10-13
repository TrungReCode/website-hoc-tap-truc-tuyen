package com.elearning.lopHocLMS.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bai_kiem_tra_bo_sung")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaiKiemTraBoSung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bai_kiem_tra_bo_sung")
    private Long maBaiKiemTraBoSung;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_bo_sung", nullable = false, unique = true)
    private BaiHocBoSung baiBoSung;

    @Column(name = "diem_toi_thieu", nullable = false)
    private Integer diemToiThieu = 70;

    @Column(name = "thoi_gian")
    private Integer thoiGian;

    public BaiKiemTraBoSung(BaiHocBoSung baiBoSung, Integer diemToiThieu, Integer thoiGian) {
        this.baiBoSung = baiBoSung;
        this.diemToiThieu = diemToiThieu != null ? diemToiThieu : 70;
        this.thoiGian = thoiGian;
    }
}
