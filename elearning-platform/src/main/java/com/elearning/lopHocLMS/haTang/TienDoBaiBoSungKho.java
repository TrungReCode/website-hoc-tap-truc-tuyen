package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.TienDoBaiBoSung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TienDoBaiBoSungKho extends JpaRepository<TienDoBaiBoSung, Long> {

    Optional<TienDoBaiBoSung> findByMaHocVienAndBaiHocBoSung_MaBaiHocBoSung(Long maHocVien, Long maBaiHocBoSung);

    List<TienDoBaiBoSung> findByMaHocVienAndBaiHocBoSung_LopHoc_MaLopHoc(Long maHocVien, Long maLopHoc);

    long countByMaHocVienAndBaiHocBoSung_LopHoc_MaLopHocAndHoanThanhTrue(Long maHocVien, Long maLopHoc);

    /** Có bất kỳ học viên nào đã phát sinh dữ liệu tiến độ trên bài học bổ sung này chưa — dùng để chặn xóa cứng. */
    boolean existsByBaiHocBoSung_MaBaiHocBoSung(Long maBaiHocBoSung);
}
