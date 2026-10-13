import apiClient from './apiClient';

/**
 * Mỗi hàm khớp 1:1 với endpoint của XacThucController.java
 * (com.elearning.dinhDanhPhanQuyen.giaoDien.XacThucController).
 */

export async function dangKy({ hoTen, email, matKhau, sdt }) {
  // Backend trả 201 Created, không có body — không cần return gì thêm.
  await apiClient.post('/xac-thuc/dang-ky', { hoTen, email, matKhau, sdt });
}

export async function kichHoatTaiKhoan(token) {
  await apiClient.get('/xac-thuc/kich-hoat', { params: { token } });
}

export async function guiLaiMaKichHoat(email) {
  await apiClient.post('/xac-thuc/gui-lai-kich-hoat', { email });
}

export async function dangNhap({ email, matKhau }) {
  const response = await apiClient.post('/xac-thuc/dang-nhap', { email, matKhau });
  // FIX (Vấn đề #2): response.data giờ khớp với PhanHoiDangNhapCong.java —
  // CHỈ còn hoTen, email, vaiTro, maNguoiDung. accessToken/refreshToken
  // không còn trong body nữa — backend đã set 2 giá trị đó vào cookie
  // HttpOnly (Set-Cookie) kèm theo response này, trình duyệt tự lưu, JS
  // không đọc/không cần đọc được.
  return response.data;
}

export async function dangXuat() {
  // FIX (Vấn đề #2): không còn truyền refreshToken làm query param — cookie
  // refreshToken (path=/api/xac-thuc) được trình duyệt tự gửi kèm, backend
  // đọc qua @CookieValue rồi phản hồi Set-Cookie xoá cả 2 cookie.
  await apiClient.post('/xac-thuc/dang-xuat');
}

export async function quenMatKhau(email) {
  await apiClient.post('/xac-thuc/quen-mat-khau', { email });
}

export async function datLaiMatKhau({ token, matKhauMoi, xacNhanMatKhauMoi }) {
  await apiClient.post('/xac-thuc/dat-lai-mat-khau', { token, matKhauMoi, xacNhanMatKhauMoi });
}