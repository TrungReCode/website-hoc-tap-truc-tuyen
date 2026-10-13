import apiClient from './apiClient';

// ---------- Tiến độ ----------

/** Tiến độ tự học (không qua lớp) của 1 khóa học. */
export async function xemTienDoCaNhan(maKhoaHoc) {
  const r = await apiClient.get(`/chung-chi/tien-do/ca-nhan/${maKhoaHoc}`);
  return r.data;
}

/** Tiến độ trong 1 lớp học cụ thể (bài gốc + bài bổ sung). */
export async function xemTienDoLop(maLopHoc) {
  const r = await apiClient.get(`/chung-chi/tien-do/lop-hoc/${maLopHoc}`);
  return r.data;
}

/** Đánh dấu hoàn thành và/hoặc lưu vị trí dừng khi TỰ HỌC (không qua lớp). */
export async function capNhatTienDoTuHoc(maBaiHoc, { hoanThanh, viTriDung } = {}) {
  const body = {};
  if (hoanThanh !== undefined) body.hoanThanh = hoanThanh;
  if (viTriDung !== undefined) body.viTriDung = viTriDung;
  await apiClient.put(`/tien-do/tu-hoc/bai-hoc/${maBaiHoc}`, body);
}

// ---------- Cấp giấy (idempotent) ----------

export async function capGiayCaNhan(maKhoaHoc) {
  const r = await apiClient.post(`/chung-chi/cap/ca-nhan/${maKhoaHoc}`);
  return r.data;
}

export async function capGiayLopHoc(maLopHoc) {
  const r = await apiClient.post(`/chung-chi/cap/lop-hoc/${maLopHoc}`);
  return r.data;
}

// ---------- Danh sách của tôi ----------

export async function layGiayCuaToi() {
  const r = await apiClient.get('/chung-chi/cua-toi');
  return r.data;
}

// ---------- Xác thực công khai — không cần đăng nhập ----------

export async function xacThucGiay(maXacThuc) {
  const r = await apiClient.get(`/chung-chi/xac-thuc/${maXacThuc}`);
  return r.data;
}

/** URL tải PDF trực tiếp (backend redirect 302 sang Firebase) — dùng làm href, không cần gọi qua axios. */
export function urlTaiPdf(maXacThuc) {
  return `${apiClient.defaults.baseURL}/chung-chi/xac-thuc/${maXacThuc}/pdf`;
}
