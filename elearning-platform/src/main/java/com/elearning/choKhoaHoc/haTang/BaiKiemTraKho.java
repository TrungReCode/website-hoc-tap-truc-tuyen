package com.elearning.choKhoaHoc.haTang;

import com.elearning.choKhoaHoc.mienDomain.BaiKiemTra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaiKiemTraKho extends JpaRepository<BaiKiemTra, Long> {

    Optional<BaiKiemTra> findByBaiHoc_MaBaiHoc(Long maBaiHoc);
}
