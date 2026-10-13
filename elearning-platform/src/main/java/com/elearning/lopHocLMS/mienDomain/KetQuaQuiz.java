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

/**
 * Khớp bảng ket_qua_lam_bai. maBaiKiemTra tham chiếu bai_kiem_tra của
 * choKhoaHoc — chỉ lưu BIGINT thuần. Mỗi lần nộp bài tạo bản ghi mới
 * (cho phép làm lại nhiều lần, giữ nguyên lịch sử).
 */
@Entity
@Table(name = "ket_qua_lam_bai")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KetQuaQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_ket_qua")
    private Long maKetQua;

    @Column(name = "ma_nguoi_dung", nullable = false)
    private Long maNguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lop")
    private LopHoc lopHoc;

    @Column(name = "ma_bai_kiem_tra", nullable = false)
    private Long maBaiKiemTra;

    @Column(name = "diem", nullable = false)
    private Integer diem;

    @Column(name = "ket_qua", nullable = false)
    private Boolean ketQua;

    @Column(name = "thoi_diem_lam")
    private LocalDateTime thoiDiemLam;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chi_tiet_dap_an", nullable = false, columnDefinition = "jsonb")
    private List<ChiTietDapAnDaChon> chiTietDapAn;

    public KetQuaQuiz(Long maNguoiDung, LopHoc lopHoc, Long maBaiKiemTra, Integer diem, Boolean ketQua,
                       List<ChiTietDapAnDaChon> chiTietDapAn) {
        this.maNguoiDung = maNguoiDung;
        this.lopHoc = lopHoc;
        this.maBaiKiemTra = maBaiKiemTra;
        this.diem = diem;
        this.ketQua = ketQua;
        this.thoiDiemLam = LocalDateTime.now();
        this.chiTietDapAn = chiTietDapAn;
    }
}
