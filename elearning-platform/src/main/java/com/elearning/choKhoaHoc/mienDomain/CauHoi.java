package com.elearning.choKhoaHoc.mienDomain;

import com.elearning.choKhoaHoc.mienDomain.enumDomain.LoaiCauHoi;
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
@Table(name = "cau_hoi")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CauHoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_cau_hoi")
    private Long maCauHoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_kiem_tra", nullable = false)
    private BaiKiemTra baiKiemTra;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "vi_tri", nullable = false)
    private Integer viTri;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_cau_hoi", nullable = false, length = 20)
    private LoaiCauHoi loaiCauHoi = LoaiCauHoi.MOT_DAP_AN;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dap_an", nullable = false, columnDefinition = "jsonb")
    private List<DapAnCauHoi> dapAn = new ArrayList<>();

    public CauHoi(BaiKiemTra baiKiemTra, String noiDung, Integer viTri, LoaiCauHoi loaiCauHoi, List<DapAnCauHoi> dapAn) {
        this.baiKiemTra = baiKiemTra;
        this.noiDung = noiDung;
        this.viTri = viTri;
        this.loaiCauHoi = loaiCauHoi != null ? loaiCauHoi : LoaiCauHoi.MOT_DAP_AN;
        this.dapAn = dapAn;
    }
}
