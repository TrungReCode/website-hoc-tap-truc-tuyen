package com.elearning.dungChung.baoMat;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Interface PUBLIC để dungChung định nghĩa hợp đồng "load user theo email".
 * Việc triển khai thực tế (query bảng nguoi_dung) nằm ở module
 * dinhDanhPhanQuyen, vì đó là nơi sở hữu Entity NguoiDung — dungChung
 * không được biết về Entity của module nghiệp vụ nào.
 *
 * Đây là ví dụ rõ nhất của "Dependency Inversion" giữa dungChung và
 * module nghiệp vụ: dungChung định nghĩa interface, module IAM cung cấp
 * implementation, Spring tự inject đúng bean lúc runtime.
 */
public interface ChiTietNguoiDungDichVu extends UserDetailsService {

    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;
}
