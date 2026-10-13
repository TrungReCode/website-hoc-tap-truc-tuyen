package com.elearning.dinhDanhPhanQuyen.haTang;

import com.elearning.dinhDanhPhanQuyen.mienDomain.TaiKhoanNganHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** PUBLIC vì lý do kỹ thuật cross-package — xem chú thích ở NguoiDungKho. */
public interface TaiKhoanNganHangKho extends JpaRepository<TaiKhoanNganHang, Long> {

    List<TaiKhoanNganHang> findByMaGiangVien(Long maGiangVien);
}
