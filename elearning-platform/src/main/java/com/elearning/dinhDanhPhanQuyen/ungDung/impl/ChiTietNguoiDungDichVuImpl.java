package com.elearning.dinhDanhPhanQuyen.ungDung.impl;

import com.elearning.dinhDanhPhanQuyen.haTang.NguoiDungKho;
import com.elearning.dungChung.baoMat.ChiTietNguoiDungDichVu;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Triển khai cụ thể của interface ChiTietNguoiDungDichVu (định nghĩa ở
 * dungChung.baoMat). Đây là implementation duy nhất trong toàn hệ thống
 * vì chỉ có module IAM sở hữu Entity NguoiDung.
 */
@Service
@RequiredArgsConstructor
class ChiTietNguoiDungDichVuImpl implements ChiTietNguoiDungDichVu {

    private final NguoiDungKho nguoiDungKho;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return nguoiDungKho.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }
}
