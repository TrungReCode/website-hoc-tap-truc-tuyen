package com.elearning.lopHocLMS.mienDomain;

import com.elearning.lopHocLMS.mienDomain.enumDomain.LoaiGiayPhep;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Khớp bảng quyen_su_dung — "Khóa thứ nhất" trong cơ chế Khóa kép (UC-06):
 * học viên phải sở hữu bản ghi này (CA_NHAN hoặc DAY_HOC) cho đúng khóa
 * học gốc trước khi được ghi danh vào bất kỳ lớp học nào dựa trên khóa
 * học đó. loai_giay_phep VARCHAR(20) thuần — không phải Postgres enum.
 */
@Entity
@Table(name = "quyen_su_dung")
@IdClass(QuyenSuDungId.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuyenSuDung {

    @Id
    @Column(name = "ma_nguoi_dung")
    private Long maNguoiDung;

    @Id
    @Column(name = "ma_khoa_hoc")
    private Long maKhoaHoc;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_giay_phep", nullable = false, length = 20)
    private LoaiGiayPhep loaiGiayPhep;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;

    /** NULL = vĩnh viễn. */
    @Column(name = "ngay_het_han")
    private LocalDateTime ngayHetHan;

    public QuyenSuDung(Long maNguoiDung, Long maKhoaHoc, LoaiGiayPhep loaiGiayPhep) {
        this.maNguoiDung = maNguoiDung;
        this.maKhoaHoc = maKhoaHoc;
        this.loaiGiayPhep = loaiGiayPhep;
        this.ngayBatDau = LocalDateTime.now();
    }

    public boolean conHieuLuc() {
        return ngayHetHan == null || ngayHetHan.isAfter(LocalDateTime.now());
    }
}
