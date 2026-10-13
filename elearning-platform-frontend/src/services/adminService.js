import apiClient from './apiClient';

// ─── THỐNG KÊ (QTV_CAO_CAP) ──────────────────────────────────
export async function layThongKeHeThong() {
  const r = await apiClient.get('/admin/thong-ke');
  return r.data;
}

// ─── TÀI KHOẢN ADMIN (QTV_CAO_CAP) ──────────────────────────
export async function taoTaiKhoanAdmin({ hoTen, email, matKhau, vaiTro }) {
  const r = await apiClient.post('/admin/tai-khoan-admin', { hoTen, email, matKhau, vaiTro });
  return r.data;
}

export async function thuHoiQuyenQuanTri(maNguoiDung) {
  const r = await apiClient.post(`/admin/tai-khoan-admin/${maNguoiDung}/thu-hoi`);
  return r.data;
}

export async function capLaiQuyenQuanTri(maNguoiDung, vaiTro) {
  const r = await apiClient.post(`/admin/tai-khoan-admin/${maNguoiDung}/cap-lai-quyen`, { vaiTro });
  return r.data;
}

// ─── CẤU HÌNH HỆ THỐNG (QTV_CAO_CAP) ─────────────────────────
export async function layCauHinhHeThong() {
  const r = await apiClient.get('/admin/cau-hinh-he-thong');
  return r.data;
}

export async function capNhatCauHinhHeThong(cauHinh) {
  const r = await apiClient.put('/admin/cau-hinh-he-thong', cauHinh);
  return r.data;
}

// ─── QUẢN LÝ NGƯỜI DÙNG (QTV_CAO_CAP + QTV_NOI_DUNG) ────────
export async function layDanhSachNguoiDung({ timKiem, vaiTro, trangThai, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (timKiem)   params.timKiem   = timKiem;
  if (vaiTro)    params.vaiTro    = vaiTro;
  if (trangThai) params.trangThai = trangThai;
  const r = await apiClient.get('/admin/nguoi-dung', { params });
  return r.data; // Page<PhanHoiNguoiDungAdmin>
}

export async function layChiTietNguoiDung(maNguoiDung) {
  const r = await apiClient.get(`/admin/nguoi-dung/${maNguoiDung}`);
  return r.data;
}

export async function capNhatTrangThaiNguoiDung(maNguoiDung, trangThai, lyDo) {
  const r = await apiClient.patch(`/admin/nguoi-dung/${maNguoiDung}/trang-thai`, { trangThai, lyDo });
  return r.data;
}

// ─── KIỂM DUYỆT NỘI DUNG (QTV_NOI_DUNG) ─────────────────────
// (Tái sử dụng duyetKhoaHocService đã có sẵn)
export { layDanhSachQuanLy, moHoSoKiemDuyet, duyetKhoaHoc } from './duyetKhoaHocService';

// ─── NHẬT KÝ HỆ THỐNG (QTV_CAO_CAP) ──────────────────────────
export async function xemNhatKy({ ngay, soDong = 200, tuKhoa } = {}) {
  const params = { soDong };
  if (ngay) params.ngay = ngay;       // 'yyyy-MM-dd'
  if (tuKhoa) params.tuKhoa = tuKhoa;
  const r = await apiClient.get('/admin/nhat-ky', { params });
  return r.data; // PhanHoiNhatKy
}