import apiClient from './apiClient';

/**
 * Khớp 1:1 với DuyetKhoaHocController.java.
 * trangThai = undefined/'' -> lấy TẤT CẢ trạng thái (danh sách quản lý đầy đủ).
 */

export async function layDanhSachQuanLy({ page = 0, size = 20, trangThai, tuKhoa } = {}) {
  const response = await apiClient.get('/khoa-hoc/cho-duyet', {
    params: { page, size, trangThai: trangThai || undefined, tuKhoa: tuKhoa || undefined },
  });
  return response.data; // Page<PhanHoiDanhSachKhoaHoc>
}

export async function moHoSoKiemDuyet(maKhoaHoc) {
  const response = await apiClient.get(`/khoa-hoc/cho-duyet/${maKhoaHoc}`);
  return response.data; // PhanHoiKhoaHoc
}

export async function dongHoSoKiemDuyet(maKhoaHoc) {
  await apiClient.post(`/khoa-hoc/cho-duyet/${maKhoaHoc}/dong`);
}

export async function duyetKhoaHoc(maKhoaHoc, { hanhDong, ghiChu }) {
  const response = await apiClient.post(`/khoa-hoc/cho-duyet/${maKhoaHoc}/quyet-dinh`, { hanhDong, ghiChu });
  return response.data;
}