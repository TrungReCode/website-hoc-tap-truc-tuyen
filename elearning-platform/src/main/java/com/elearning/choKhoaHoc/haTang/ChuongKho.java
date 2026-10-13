package com.elearning.choKhoaHoc.haTang;

import com.elearning.choKhoaHoc.mienDomain.Chuong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChuongKho extends JpaRepository<Chuong, Long> {

    List<Chuong> findByKhoaHoc_MaKhoaHocOrderByViTriAsc(Long maKhoaHoc);

    long countByKhoaHoc_MaKhoaHoc(Long maKhoaHoc);
}
