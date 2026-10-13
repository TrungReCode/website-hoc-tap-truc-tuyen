import apiClient from './apiClient';

/**
 * Khớp 1:1 với DanhGiaQuanTriController.java — chỉ QTV_NOI_DUNG / QTV_CAO_CAP.
 */

export async function layTatCaDanhGiaChoAdmin(maKhoaHoc, { page = 0, size = 20 } = {}) {
  const response = await apiClient.get(`/admin/danh-gia/khoa-hoc/${maKhoaHoc}`, { params: { page, size } });
  return response.data; // Page<PhanHoiDanhGia>
}

export async function anDanhGiaViPham(maDanhGia, lyDo) {
  const response = await apiClient.post(`/admin/danh-gia/${maDanhGia}/an`, { lyDo });
  return response.data;
}

export async function boAnDanhGia(maDanhGia) {
  const response = await apiClient.post(`/admin/danh-gia/${maDanhGia}/bo-an`);
  return response.data;
}

export async function xoaDanhGiaViPham(maDanhGia, lyDo) {
  await apiClient.delete(`/admin/danh-gia/${maDanhGia}`, { data: { lyDo } });
}
