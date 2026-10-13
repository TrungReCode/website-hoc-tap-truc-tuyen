import apiClient from './apiClient';

/**
 * Khớp 1:1 với DanhMucController.java (com.elearning.choKhoaHoc.giaoDien).
 */

export async function layDanhSachDanhMuc() {
  const response = await apiClient.get('/danh-muc');
  return response.data; // PhanHoiDanhMuc[]
}
