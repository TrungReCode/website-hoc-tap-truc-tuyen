import apiClient from './apiClient';
 
/**
 * Khớp 1:1 với KhoaHocController.java (com.elearning.choKhoaHoc.giaoDien).
 */
 
// ---------- Upload ảnh bìa ----------
 
export async function taiLenAnhBia(file, onTienDo) {
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
  return r.data.url; // trả về chuỗi URL Cloudinary/Firebase
}
 
// ---------- Chợ Khóa Học — công khai ----------
 
export async function timKiemCongKhai({ maDanhMuc, tuKhoa, caNhanLoc, dayHocLoc, page = 0, size = 12, sort } = {}) {
  const params = { page, size };
  if (maDanhMuc) params.maDanhMuc = maDanhMuc;
  if (tuKhoa) params.tuKhoa = tuKhoa;
  if (caNhanLoc) params.caNhanLoc = caNhanLoc;
  if (dayHocLoc) params.dayHocLoc = dayHocLoc;
  if (sort) params.sort = sort;
  const response = await apiClient.get('/khoa-hoc/cong-khai', { params });
  return response.data; // Page<PhanHoiDanhSachKhoaHoc>
}
 
export async function layChiTietCongKhai(maKhoaHoc) {
  const response = await apiClient.get(`/khoa-hoc/cong-khai/${maKhoaHoc}`);
  return response.data; // PhanHoiKhoaHoc
}
 
// ---------- Quản lý khóa học — Giảng viên ----------
 
export async function layKhoaHocCuaToi() {
  const response = await apiClient.get('/khoa-hoc/cua-toi');
  return response.data; // PhanHoiDanhSachKhoaHoc[]
}

// Khóa học tự viết HOẶC đã mua Giấy phép Giảng dạy — dùng khi chọn khóa học để tạo lớp
export async function layKhoaHocCoTheMoLop() {
  const response = await apiClient.get('/khoa-hoc/co-the-mo-lop');
  return response.data; // PhanHoiDanhSachKhoaHoc[]
}
 
export async function layChiTietChoTacGia(maKhoaHoc) {
  const response = await apiClient.get(`/khoa-hoc/${maKhoaHoc}`);
  return response.data; // PhanHoiKhoaHoc
}
 
// ---------- Học viên — khóa học đã mua ----------
 
export async function layKhoaHocDaMua() {
  const response = await apiClient.get('/khoa-hoc/da-mua');
  return response.data; // PhanHoiDanhSachKhoaHoc[]
}
 
export async function layChiTietChoHocVien(maKhoaHoc) {
  const response = await apiClient.get(`/khoa-hoc/${maKhoaHoc}/hoc`);
  return response.data; // PhanHoiKhoaHoc (đầy đủ nội dung)
}
 
export async function taoKhoaHoc(yeuCau) {
  const response = await apiClient.post('/khoa-hoc', yeuCau);
  return response.data;
}
 
export async function capNhatKhoaHoc(maKhoaHoc, yeuCau) {
  const response = await apiClient.put(`/khoa-hoc/${maKhoaHoc}`, yeuCau);
  return response.data;
}
 
export async function xoaKhoaHoc(maKhoaHoc) {
  await apiClient.delete(`/khoa-hoc/${maKhoaHoc}`);
}
 
export async function guiKiemDuyet(maKhoaHoc) {
  const response = await apiClient.post(`/khoa-hoc/${maKhoaHoc}/gui-kiem-duyet`);
  return response.data;
}
 
export async function rutLaiKiemDuyet(maKhoaHoc) {
  const response = await apiClient.post(`/khoa-hoc/${maKhoaHoc}/rut-lai`);
  return response.data;
}
 
export async function luuTru(maKhoaHoc) {
  const response = await apiClient.post(`/khoa-hoc/${maKhoaHoc}/luu-tru`);
  return response.data;
}
 
export async function taiCongKhai(maKhoaHoc) {
  const response = await apiClient.post(`/khoa-hoc/${maKhoaHoc}/tai-cong-khai`);
  return response.data;
}