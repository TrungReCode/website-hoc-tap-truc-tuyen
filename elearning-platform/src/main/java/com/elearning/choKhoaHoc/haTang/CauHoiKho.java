package com.elearning.choKhoaHoc.haTang;

import com.elearning.choKhoaHoc.mienDomain.CauHoi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CauHoiKho extends JpaRepository<CauHoi, Long> {

    List<CauHoi> findByBaiKiemTra_MaBaiKiemTraOrderByViTriAsc(Long maBaiKiemTra);

    long countByBaiKiemTra_MaBaiKiemTra(Long maBaiKiemTra);
}
