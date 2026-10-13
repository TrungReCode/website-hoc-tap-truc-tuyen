import apiClient from './apiClient';

/**
 * Khớp 1:1 với DanhMucQuanTriController.java (path /api/danh-muc-quan-tri).
 */

export async function layDanhSachDanhMucQuanTri() {
  const response = await apiClient.get('/danh-muc-quan-tri');
  return response.data; // List<PhanHoiDanhMucQuanTri>
}

export async function taoDanhMuc({ tenDanhMuc, maDanhMucCha }) {
  const response = await apiClient.post('/danh-muc-quan-tri', { tenDanhMuc, maDanhMucCha: maDanhMucCha || null });
  return response.data;
}

export async function capNhatDanhMuc(maDanhMuc, { tenDanhMuc, maDanhMucCha }) {
  const response = await apiClient.put(`/danh-muc-quan-tri/${maDanhMuc}`, { tenDanhMuc, maDanhMucCha: maDanhMucCha || null });
  return response.data;
}

export async function xoaDanhMuc(maDanhMuc) {
  await apiClient.delete(`/danh-muc-quan-tri/${maDanhMuc}`);
}