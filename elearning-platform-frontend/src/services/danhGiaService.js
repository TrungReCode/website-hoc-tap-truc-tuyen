import apiClient from './apiClient';

/**
 * Khớp 1:1 với DanhGiaController.java.
 */

export async function layDanhGiaTheoKhoaHoc(maKhoaHoc, { page = 0, size = 10 } = {}) {
  const response = await apiClient.get(`/khoa-hoc/cong-khai/${maKhoaHoc}/danh-gia`, { params: { page, size } });
  return response.data; // Page<PhanHoiDanhGia>
}

export async function layDanhGiaCuaToi(maKhoaHoc) {
  try {
    const response = await apiClient.get(`/khoa-hoc/${maKhoaHoc}/danh-gia/cua-toi`);
    return response.status === 204 ? null : response.data;
  } catch (err) {
    if (err?.response?.status === 401) return null; // chưa đăng nhập — không phải lỗi cần báo
    throw err;
  }
}

export async function taoDanhGia(maKhoaHoc, { soSao, nhanXet }) {
  const response = await apiClient.post(`/khoa-hoc/${maKhoaHoc}/danh-gia`, { soSao, nhanXet });
  return response.data;
}

export async function capNhatDanhGia(maDanhGia, { soSao, nhanXet }) {
  const response = await apiClient.put(`/danh-gia/${maDanhGia}`, { soSao, nhanXet });
  return response.data;
}

export async function xoaDanhGia(maDanhGia) {
  await apiClient.delete(`/danh-gia/${maDanhGia}`);
}
