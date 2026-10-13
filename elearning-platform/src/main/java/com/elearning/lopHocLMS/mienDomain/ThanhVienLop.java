package com.elearning.lopHocLMS.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Dùng khóa chính kép thuần (không @ManyToOne) để tránh phức tạp derived-identifier của JPA, nhất quán với QuyenSuDung. */
@Entity
@Table(name = "thanh_vien_lop")
@IdClass(ThanhVienLopId.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThanhVienLop {

    @Id
    @Column(name = "ma_hoc_vien")
    private Long maHocVien;

    @Id
    @Column(name = "ma_lop_hoc")
    private Long maLopHoc;

    @Column(name = "ngay_tham_gia", nullable = false)
    private LocalDateTime ngayThamGia;

    public ThanhVienLop(Long maHocVien, Long maLopHoc) {
        this.maHocVien = maHocVien;
        this.maLopHoc = maLopHoc;
        this.ngayThamGia = LocalDateTime.now();
    }
}
