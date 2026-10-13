import apiClient from './apiClient';

/**
 * Khớp 1:1 với HoSoCaNhanController.java (com.elearning.dinhDanhPhanQuyen.giaoDien).
 */

// ---------- Hồ sơ cá nhân ----------

export async function layHoSo() {
  const r = await apiClient.get('/ho-so/me');
  return r.data; // ThongTinCaNhan
}

export async function capNhatHoSo({ hoTen, sdt, anhDaiDien, tieuSu }) {
  const r = await apiClient.put('/ho-so/me', { hoTen, sdt, anhDaiDien, tieuSu });
  return r.data; // ThongTinCaNhan
}

export async function doiMatKhau({ matKhauHienTai, matKhauMoi, xacNhanMatKhauMoi }) {
  await apiClient.post('/ho-so/doi-mat-khau', { matKhauHienTai, matKhauMoi, xacNhanMatKhauMoi });
}

// Dùng lại endpoint upload ảnh dùng chung (Cloudinary/Firebase) của module choKhoaHoc —
// endpoint này không giới hạn riêng cho ảnh bìa khóa học, chỉ nhận file ảnh và trả về URL.
export async function taiLenAnhDaiDien(file, onTienDo) {
  const fd = new FormData();
  fd.append('file', file);
  const r = await apiClient.post('/khoa-hoc/tai-len-anh-bia', fd, {
    headers: { 'Content-Type': undefined },
    onUploadProgress: onTienDo
      ? (evt) => {
          if (evt.total) onTienDo(Math.round((evt.loaded * 100) / evt.total));
        }
      : undefined,
  });
  return r.data.url;
}

// ---------- Tài khoản ngân hàng (dành cho Giảng viên nhận đối soát) ----------

export async function layTaiKhoanNganHang() {
  const r = await apiClient.get('/ho-so/tai-khoan-ngan-hang');
  return r.data; // TaiKhoanNganHangDto[]
}

export async function themTaiKhoanNganHang({ tenChuTaiKhoan, soTaiKhoan, tenNganHang }) {
  const r = await apiClient.post('/ho-so/tai-khoan-ngan-hang', { tenChuTaiKhoan, soTaiKhoan, tenNganHang });
  return r.data;
}

export async function capNhatTaiKhoanNganHang(maTaiKhoan, { tenChuTaiKhoan, soTaiKhoan, tenNganHang }) {
  const r = await apiClient.put(`/ho-so/tai-khoan-ngan-hang/${maTaiKhoan}`, { tenChuTaiKhoan, soTaiKhoan, tenNganHang });
  return r.data;
}

export async function xoaTaiKhoanNganHang(maTaiKhoan) {
  await apiClient.delete(`/ho-so/tai-khoan-ngan-hang/${maTaiKhoan}`);
}

export async function datLamMacDinhTaiKhoanNganHang(maTaiKhoan) {
  const r = await apiClient.patch(`/ho-so/tai-khoan-ngan-hang/${maTaiKhoan}/mac-dinh`);
  return r.data;
}