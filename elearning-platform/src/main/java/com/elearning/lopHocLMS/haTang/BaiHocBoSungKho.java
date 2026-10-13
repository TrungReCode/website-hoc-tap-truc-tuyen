package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.BaiHocBoSung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BaiHocBoSungKho extends JpaRepository<BaiHocBoSung, Long> {

    List<BaiHocBoSung> findByLopHoc_MaLopHocOrderByViTriAsc(Long maLopHoc);
}
