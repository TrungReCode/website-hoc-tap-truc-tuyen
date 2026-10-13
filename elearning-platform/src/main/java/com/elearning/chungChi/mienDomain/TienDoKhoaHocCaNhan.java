package com.elearning.chungChi.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Tiến độ tổng hợp học viên × khóa học (tự học, không qua lớp). */
@Entity
@Table(name = "tien_do_khoa_hoc_ca_nhan")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TienDoKhoaHocCaNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_tien_do")
    private Long maTienDo;

    @Column(name = "ma_hoc_vien", nullable = false)
    private Long maHocVien;

    @Column(name = "ma_khoa_hoc", nullable = false)
    private Long maKhoaHoc;

    @Column(name = "tong_bai_hoc", nullable = false)
    private int tongBaiHoc = 0;

    @Column(name = "bai_hoan_thanh", nullable = false)
    private int baiHoanThanh = 0;

    @Column(name = "tong_quiz", nullable = false)
    private int tongQuiz = 0;

    @Column(name = "quiz_dat", nullable = false)
    private int quizDat = 0;

    @Column(name = "phan_tram_hoan_thanh", nullable = false)
    private BigDecimal phanTramHoanThanh = BigDecimal.ZERO;

    @Column(name = "da_du_dieu_kien", nullable = false)
    private boolean daDuDieuKien = false;

    @Column(name = "ngay_cap_nhat", nullable = false)
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    public TienDoKhoaHocCaNhan(Long maHocVien, Long maKhoaHoc) {
        this.maHocVien = maHocVien;
        this.maKhoaHoc = maKhoaHoc;
    }

    public void capNhat(int tongBaiHoc, int baiHoanThanh, int tongQuiz, int quizDat) {
        this.tongBaiHoc = tongBaiHoc;
        this.baiHoanThanh = baiHoanThanh;
        this.tongQuiz = tongQuiz;
        this.quizDat = quizDat;
        this.phanTramHoanThanh = tongBaiHoc == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(baiHoanThanh * 100.0 / tongBaiHoc);
        this.daDuDieuKien = baiHoanThanh >= tongBaiHoc && quizDat >= tongQuiz && tongBaiHoc > 0;
        this.ngayCapNhat = LocalDateTime.now();
    }
}
