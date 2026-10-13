package com.elearning.dungChung.baoMat;

/**
 * Hợp đồng tối giản để các module nghiệp vụ (choKhoaHoc, lopHocLMS,
 * thanhToan, traoDoiTrongLop) lấy được "mã người dùng đang đăng nhập" từ
 * SecurityContext mà KHÔNG cần import Entity NguoiDung của module
 * dinhDanhPhanQuyen (vốn package-private theo quy ước kiến trúc).
 *
 * Cách dùng trong Controller của module khác:
 *   {@code @AuthenticationPrincipal NguoiDungThamChieu nguoiDung}
 *
 * Cơ chế: NguoiDung (entity thật trong dinhDanhPhanQuyen.mienDomain)
 * implements thêm interface này bên cạnh UserDetails. Khi Spring Security
 * resolve @AuthenticationPrincipal, nó ép kiểu Object principal đang lưu
 * trong SecurityContext sang interface được khai báo ở tham số — việc ép
 * kiểu này thành công vì principal thực tế (NguoiDung) có implements
 * NguoiDungThamChieu, dù Controller gọi không hề import lớp NguoiDung.
 *
 * Lưu ý: interface này KHÔNG trả về VaiTro (enum) để tránh dungChung phải
 * phụ thuộc ngược vào package dinhDanhPhanQuyen (gây circular dependency
 * giữa các module). Việc kiểm tra vai trò nên dùng cơ chế chuẩn của Spring
 * Security (@PreAuthorize("hasRole(...)")) vì NguoiDung.getAuthorities()
 * đã trả về "ROLE_" + tên vai trò.
 */
public interface NguoiDungThamChieu {

    Long getMaNguoiDung();
}
