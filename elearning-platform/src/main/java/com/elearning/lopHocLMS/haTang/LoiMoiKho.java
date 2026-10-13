package com.elearning.lopHocLMS.haTang;

import com.elearning.lopHocLMS.mienDomain.LoiMoi;
import com.elearning.lopHocLMS.mienDomain.enumDomain.TrangThaiLoiMoi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoiMoiKho extends JpaRepository<LoiMoi, Long> {

    List<LoiMoi> findByLopHoc_MaLopHocOrderByNgayTaoDesc(Long maLopHoc);

    List<LoiMoi> findByEmailHocVienAndTrangThai(String emailHocVien, TrangThaiLoiMoi trangThai);

    Optional<LoiMoi> findByLopHoc_MaLopHocAndEmailHocVienAndTrangThai(
            Long maLopHoc, String emailHocVien, TrangThaiLoiMoi trangThai);
}
