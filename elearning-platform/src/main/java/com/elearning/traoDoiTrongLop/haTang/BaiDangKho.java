package com.elearning.traoDoiTrongLop.haTang;

import com.elearning.traoDoiTrongLop.mienDomain.BaiDang;
import com.elearning.traoDoiTrongLop.mienDomain.enumDomain.LoaiBaiDang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaiDangKho extends JpaRepository<BaiDang, Long> {

    /** Lấy bài đăng chưa bị xoá mềm của một lớp, phân trang, sắp xếp mới nhất trước. */
    Page<BaiDang> findByMaLopHocAndXoaMemFalseOrderByNgayTaoDesc(Long maLopHoc, Pageable pageable);

    /** Lọc theo loại bài đăng. */
    Page<BaiDang> findByMaLopHocAndLoaiAndXoaMemFalseOrderByNgayTaoDesc(
            Long maLopHoc, LoaiBaiDang loai, Pageable pageable);

    long countByMaLopHocAndXoaMemFalse(Long maLopHoc);
}
