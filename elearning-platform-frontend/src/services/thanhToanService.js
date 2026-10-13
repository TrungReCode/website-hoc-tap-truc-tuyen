import apiClient from './apiClient'; // Import apiClient đã cấu hình

export async function taoDonMuaKhoaHoc(maKhoaHoc, { loaiGiayPhep }) {
  const r = await apiClient.post(`/thanh-toan/khoa-hoc/${maKhoaHoc}/vnpay`, { loaiGiayPhep });
  return r.data;
}

export async function taoDonPhiLopHoc(maLopHoc) {
  const r = await apiClient.post(`/thanh-toan/lop-hoc/${maLopHoc}/vnpay`, {});
  return r.data;
}

export async function taoYeuCauHoanTienKhoaHoc(maPhieuGiaoDich, lyDo) {
  const r = await apiClient.post(`/thanh-toan/hoan-tien/khoa-hoc/${maPhieuGiaoDich}`, { lyDo });
  return r.data;
}

// ---------- UC-16: Lọc / biểu đồ / xuất CSV doanh thu (của tôi) ----------

export async function layTongQuanDoanhThuCuaToi({ tuNgay, denNgay } = {}) {
  const params = {};
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  const r = await apiClient.get('/thanh-toan/doanh-thu-cua-toi/tong-quan', { params });
  return r.data;
}

export async function layGiaoDichDoanhThuCuaToi({ tuNgay, denNgay, maKhoaHoc } = {}) {
  const params = {};
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  if (maKhoaHoc) params.maKhoaHoc = maKhoaHoc;
  const r = await apiClient.get('/thanh-toan/doanh-thu-cua-toi/giao-dich', { params });
  return r.data;
}

export async function layXuHuongDoanhThuCuaToi({ tuNgay, denNgay, maKhoaHoc } = {}) {
  const params = {};
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  if (maKhoaHoc) params.maKhoaHoc = maKhoaHoc;
  const r = await apiClient.get('/thanh-toan/doanh-thu-cua-toi/xu-huong', { params });
  return r.data;
}

export async function taiXuongCsvDoanhThuCuaToi({ tuNgay, denNgay, maKhoaHoc } = {}) {
  const params = {};
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  if (maKhoaHoc) params.maKhoaHoc = maKhoaHoc;
  const r = await apiClient.get('/thanh-toan/doanh-thu-cua-toi/xuat-csv', { params, responseType: 'blob' });
  taiXuongBlob(r.data, 'doanh-thu.csv');
}

function taiXuongBlob(blob, tenTep) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = tenTep;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

export async function taoYeuCauHoanTienLopHoc(maPhieuGiaoDich, lyDo) {
  const r = await apiClient.post(`/thanh-toan/hoan-tien/lop-hoc/${maPhieuGiaoDich}`, { lyDo });
  return r.data;
}

export async function layLichSuGiaoDich() {
  const r = await apiClient.get('/thanh-toan/lich-su');
  return r.data;
}

/** Lịch sử NHẬN tiền (vai trò tác giả khóa học/giảng viên phụ trách lớp). */
export async function layLichSuGiaoDichNhan() {
  const r = await apiClient.get('/thanh-toan/lich-su-nhan');
  return r.data;
}

// ---------- Giảng viên — doanh thu & đối soát của chính mình ----------

export async function layDoanhThuCuaToi() {
  const r = await apiClient.get('/thanh-toan/doanh-thu-cua-toi');
  return r.data;
}

export async function layDoiSoatCuaToi({ trangThai, page = 0, size = 10 } = {}) {
  const params = { page, size };
  if (trangThai) params.trangThai = trangThai;
  const r = await apiClient.get('/thanh-toan/doi-soat-cua-toi', { params });
  return r.data;
}

export async function layDangChoHoanThanhLop() {
  const r = await apiClient.get('/thanh-toan/dang-cho-hoan-thanh-lop');
  return r.data;
}

export const xacNhanThanhToanVnpay = async (chuoiTruyVan) => {
  // Dùng apiClient.get và bỏ '/api'
  const response = await apiClient.get(`/thanh-toan/vnpay-return${chuoiTruyVan}`);
  return response.data;
};

export const kiemTraSoHuu = async (maKhoaHoc, loaiGiayPhep) => {
  // Dùng apiClient.get và bỏ '/api'
  const response = await apiClient.get(`/thanh-toan/kiem-tra-so-huu/${maKhoaHoc}?loai=${loaiGiayPhep}`);
  return response.data;
};