package com.elearning.choKhoaHoc.haTang;

import com.elearning.choKhoaHoc.mienDomain.BaiHoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BaiHocKho extends JpaRepository<BaiHoc, Long> {

    List<BaiHoc> findByChuong_MaChuongOrderByViTriAsc(Long maChuong);

    long countByChuong_KhoaHoc_MaKhoaHoc(Long maKhoaHoc);

    /** Dùng bởi chungChi để tính tiến độ/điều kiện cấp giấy xác nhận — toàn bộ
     *  bài học gốc (mọi chương) thuộc 1 khóa học, không phân trang. */
    List<BaiHoc> findByChuong_KhoaHoc_MaKhoaHoc(Long maKhoaHoc);
}
