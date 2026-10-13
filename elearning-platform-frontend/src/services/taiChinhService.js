import apiClient from './apiClient';

export async function layTongQuanTaiChinh() {
  const r = await apiClient.get('/admin/tai-chinh/tong-quan');
  return r.data;
}

export async function layDanhSachPhieuDoiSoat({ trangThai, timKiem, tuNgay, denNgay, page = 0, size = 10 } = {}) {
  const params = { page, size };
  if (trangThai) params.trangThai = trangThai;
  if (timKiem) params.timKiem = timKiem;
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  const r = await apiClient.get('/admin/tai-chinh/doi-soat', { params });
  return r.data;
}

export async function taiXuongCsvDoiSoat({ trangThai, timKiem, tuNgay, denNgay } = {}) {
  const params = {};
  if (trangThai) params.trangThai = trangThai;
  if (timKiem) params.timKiem = timKiem;
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  const r = await apiClient.get('/admin/tai-chinh/doi-soat/xuat-csv', { params, responseType: 'blob' });
  const url = window.URL.createObjectURL(r.data);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'doi-soat.csv';
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

export async function capNhatPhieuDoiSoat(maDoiSoat, { trangThai, maGiaoDichNganHang } = {}) {
  const r = await apiClient.patch(`/admin/tai-chinh/doi-soat/${maDoiSoat}`, {
    trangThai,
    maGiaoDichNganHang,
  });
  return r.data;
}

export async function capNhatHangLoatPhieuDoiSoat(dsMaDoiSoat, { trangThai, maGiaoDichNganHang } = {}) {
  const r = await apiClient.patch('/admin/tai-chinh/doi-soat/hang-loat', {
    dsMaDoiSoat,
    capNhat: { trangThai, maGiaoDichNganHang },
  });
  return r.data;
}

export async function layGiaoDichTheoDoiSoat(maDoiSoat) {
  const r = await apiClient.get(`/admin/tai-chinh/doi-soat/${maDoiSoat}/giao-dich`);
  return r.data;
}

/** Báo cáo tổng hợp đối soát THEO GIẢNG VIÊN — tổng tiền cần chuyển mỗi giảng viên trong kỳ đã chọn. */
export async function layTongHopDoiSoatTheoGiangVien({ trangThai, timKiem, tuNgay, denNgay } = {}) {
  const params = {};
  if (trangThai) params.trangThai = trangThai;
  if (timKiem) params.timKiem = timKiem;
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  const r = await apiClient.get('/admin/tai-chinh/doi-soat/theo-giang-vien', { params });
  return r.data;
}

/** Xem chi tiết từng phiếu của 1 giảng viên trong báo cáo tổng hợp — dùng cùng bộ lọc trạng thái/thời gian. */
export async function layChiTietDoiSoatTheoGiangVien(maGiangVien, { trangThai, tuNgay, denNgay } = {}) {
  const params = {};
  if (trangThai) params.trangThai = trangThai;
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  const r = await apiClient.get(`/admin/tai-chinh/doi-soat/theo-giang-vien/${maGiangVien}/chi-tiet`, { params });
  return r.data;
}

// ---------- Duyệt hoàn tiền ----------

export async function layDanhSachHoanTienKhoaHoc({ trangThai, page = 0, size = 10 } = {}) {
  const params = { page, size };
  if (trangThai) params.trangThai = trangThai;
  const r = await apiClient.get('/admin/tai-chinh/hoan-tien/khoa-hoc', { params });
  return r.data;
}

export async function xuLyHoanTienKhoaHoc(maHoanTien, { trangThai, ghiChu } = {}) {
  const r = await apiClient.patch(`/admin/tai-chinh/hoan-tien/khoa-hoc/${maHoanTien}`, { trangThai, ghiChu });
  return r.data;
}

export async function layDanhSachHoanTienLopHoc({ trangThai, page = 0, size = 10 } = {}) {
  const params = { page, size };
  if (trangThai) params.trangThai = trangThai;
  const r = await apiClient.get('/admin/tai-chinh/hoan-tien/lop-hoc', { params });
  return r.data;
}

export async function xuLyHoanTienLopHoc(maHoanTien, { trangThai, ghiChu } = {}) {
  const r = await apiClient.patch(`/admin/tai-chinh/hoan-tien/lop-hoc/${maHoanTien}`, { trangThai, ghiChu });
  return r.data;
}

// ---------- UC-16: Lọc / biểu đồ / xuất CSV doanh thu toàn hệ thống ----------

export async function layTongQuanDoanhThuHeThong({ tuNgay, denNgay, maKhoaHoc, maGiangVien } = {}) {
  const params = {};
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  if (maKhoaHoc) params.maKhoaHoc = maKhoaHoc;
  if (maGiangVien) params.maGiangVien = maGiangVien;
  const r = await apiClient.get('/admin/tai-chinh/doanh-thu/tong-quan', { params });
  return r.data;
}

export async function layGiaoDichDoanhThuHeThong({ tuNgay, denNgay, maKhoaHoc, maGiangVien } = {}) {
  const params = {};
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  if (maKhoaHoc) params.maKhoaHoc = maKhoaHoc;
  if (maGiangVien) params.maGiangVien = maGiangVien;
  const r = await apiClient.get('/admin/tai-chinh/doanh-thu/giao-dich', { params });
  return r.data;
}

export async function layXuHuongDoanhThuHeThong({ tuNgay, denNgay, maKhoaHoc, maGiangVien } = {}) {
  const params = {};
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  if (maKhoaHoc) params.maKhoaHoc = maKhoaHoc;
  if (maGiangVien) params.maGiangVien = maGiangVien;
  const r = await apiClient.get('/admin/tai-chinh/doanh-thu/xu-huong', { params });
  return r.data;
}

export async function taiXuongCsvDoanhThuHeThong({ tuNgay, denNgay, maKhoaHoc, maGiangVien } = {}) {
  const params = {};
  if (tuNgay) params.tuNgay = tuNgay;
  if (denNgay) params.denNgay = denNgay;
  if (maKhoaHoc) params.maKhoaHoc = maKhoaHoc;
  if (maGiangVien) params.maGiangVien = maGiangVien;
  const r = await apiClient.get('/admin/tai-chinh/doanh-thu/xuat-csv', { params, responseType: 'blob' });
  const url = window.URL.createObjectURL(r.data);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'doanh-thu-he-thong.csv';
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}