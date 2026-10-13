package com.elearning.dinhDanhPhanQuyen.haTang;

import com.elearning.dinhDanhPhanQuyen.mienDomain.MaLamMoiPhien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** PUBLIC vì lý do kỹ thuật cross-package — xem chú thích ở NguoiDungKho. */
public interface MaLamMoiPhienKho extends JpaRepository<MaLamMoiPhien, Long> {

    Optional<MaLamMoiPhien> findByToken(String token);
    
    // Tìm tất cả refresh token của một người dùng (dùng để thu hồi khi đổi mật khẩu)
    java.util.List<MaLamMoiPhien> findByNguoiDungId(Long nguoiDungId);
}
