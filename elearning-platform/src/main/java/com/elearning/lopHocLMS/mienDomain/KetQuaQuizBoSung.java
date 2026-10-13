package com.elearning.lopHocLMS.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ket_qua_lam_bai_bo_sung")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KetQuaQuizBoSung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_ket_qua_bo_sung")
    private Long maKetQuaBoSung;

    @Column(name = "ma_nguoi_dung", nullable = false)
    private Long maNguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lop", nullable = false)
    private LopHoc lopHoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_kiem_tra_bo_sung", nullable = false)
    private BaiKiemTraBoSung baiKiemTraBoSung;

    @Column(name = "diem", nullable = false)
    private Integer diem;

    @Column(name = "ket_qua", nullable = false)
    private Boolean ketQua;

    @Column(name = "thoi_diem_lam")
    private LocalDateTime thoiDiemLam;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chi_tiet_dap_an", nullable = false, columnDefinition = "jsonb")
    private List<ChiTietDapAnDaChon> chiTietDapAn;

    public KetQuaQuizBoSung(Long maNguoiDung, LopHoc lopHoc, BaiKiemTraBoSung baiKiemTraBoSung, Integer diem,
                             Boolean ketQua, List<ChiTietDapAnDaChon> chiTietDapAn) {
        this.maNguoiDung = maNguoiDung;
        this.lopHoc = lopHoc;
        this.baiKiemTraBoSung = baiKiemTraBoSung;
        this.diem = diem;
        this.ketQua = ketQua;
        this.thoiDiemLam = LocalDateTime.now();
        this.chiTietDapAn = chiTietDapAn;
    }
}
