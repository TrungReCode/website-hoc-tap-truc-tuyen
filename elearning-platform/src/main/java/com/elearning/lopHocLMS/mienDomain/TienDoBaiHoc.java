package com.elearning.lopHocLMS.mienDomain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * maBaiHoc tham chiếu bai_hoc của choKhoaHoc — chỉ lưu BIGINT thuần.
 *
 * maLopHoc: NULL = tiến độ TỰ HỌC (khóa học mua riêng, không qua lớp),
 * NOT NULL = tiến độ riêng của ĐÚNG 1 lớp học cụ thể. Bắt buộc phải tách
 * theo lớp vì nhiều lớp khác nhau có thể dùng chung 1 khóa học làm giáo
 * trình gốc — nếu không tách, tiến độ của học viên ở lớp/luồng tự học này
 * sẽ "dính" sang mọi lớp khác dùng chung khóa học đó (xem V14 migration).
 */
@Entity
@Table(name = "tien_do_hoc")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TienDoBaiHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_tien_do")
    private Long maTienDo;

    @Column(name = "ma_hoc_vien", nullable = false)
    private Long maHocVien;

    @Column(name = "ma_bai_hoc")
    private Long maBaiHoc;

    /** NULL = tiến độ tự học; NOT NULL = tiến độ riêng của lớp học này. */
    @Column(name = "ma_lop_hoc")
    private Long maLopHoc;

    @Column(name = "hoan_thanh")
    private Boolean hoanThanh = false;

    /** Vị trí dừng video (giây) hoặc số trang để tiếp tục lần sau. */
    @Column(name = "vi_tri_dung")
    private Long viTriDung;

    @Column(name = "ngay_hoan_thanh")
    private LocalDateTime ngayHoanThanh;

    /** Tiến độ TỰ HỌC — không gắn với lớp học nào. */
    public TienDoBaiHoc(Long maHocVien, Long maBaiHoc) {
        this.maHocVien = maHocVien;
        this.maBaiHoc = maBaiHoc;
        this.maLopHoc = null;
        this.hoanThanh = false;
    }

    /** Tiến độ riêng của 1 LỚP HỌC cụ thể — maLopHoc bắt buộc khác null. */
    public TienDoBaiHoc(Long maHocVien, Long maBaiHoc, Long maLopHoc) {
        this.maHocVien = maHocVien;
        this.maBaiHoc = maBaiHoc;
        this.maLopHoc = maLopHoc;
        this.hoanThanh = false;
    }

    public void danhDauHoanThanh() {
        this.hoanThanh = true;
        this.ngayHoanThanh = LocalDateTime.now();
    }
}