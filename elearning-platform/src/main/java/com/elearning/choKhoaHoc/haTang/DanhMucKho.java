package com.elearning.choKhoaHoc.haTang;

import com.elearning.choKhoaHoc.mienDomain.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DanhMucKho extends JpaRepository<DanhMuc, Long> {

    List<DanhMuc> findByMucChaIsNull();

    List<DanhMuc> findByMucCha_MaDanhMuc(Long maDanhMucCha);

    long countByMucCha_MaDanhMuc(Long maDanhMucCha);

    boolean existsByTenDanhMucIgnoreCase(String tenDanhMuc);
}