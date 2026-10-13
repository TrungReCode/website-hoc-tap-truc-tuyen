package com.elearning.lopHocLMS.mienDomain;

import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiCauHoiBoSung;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cau_hoi_bai_bo_sung")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CauHoiBoSung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_cau_hoi_bai_bo_sung")
    private Long maCauHoiBaiBoSung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_kiem_tra_bo_sung", nullable = false)
    private BaiKiemTraBoSung baiKiemTraBoSung;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "vi_tri", nullable = false)
    private Integer viTri;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_cau_hoi", nullable = false, length = 20)
    private LoaiCauHoiBoSung loaiCauHoi = LoaiCauHoiBoSung.MOT_DAP_AN;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dap_an", nullable = false, columnDefinition = "jsonb")
    private List<DapAnBoSung> dapAn = new ArrayList<>();

    public CauHoiBoSung(BaiKiemTraBoSung baiKiemTraBoSung, String noiDung, Integer viTri,
                         LoaiCauHoiBoSung loaiCauHoi, List<DapAnBoSung> dapAn) {
        this.baiKiemTraBoSung = baiKiemTraBoSung;
        this.noiDung = noiDung;
        this.viTri = viTri;
        this.loaiCauHoi = loaiCauHoi != null ? loaiCauHoi : LoaiCauHoiBoSung.MOT_DAP_AN;
        this.dapAn = dapAn;
    }
}
