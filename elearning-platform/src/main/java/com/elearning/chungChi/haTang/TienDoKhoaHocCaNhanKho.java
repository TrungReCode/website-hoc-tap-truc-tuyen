package com.elearning.chungChi.haTang;

import com.elearning.chungChi.mienDomain.TienDoKhoaHocCaNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TienDoKhoaHocCaNhanKho extends JpaRepository<TienDoKhoaHocCaNhan, Long> {
    Optional<TienDoKhoaHocCaNhan> findByMaHocVienAndMaKhoaHoc(Long maHocVien, Long maKhoaHoc);
}
