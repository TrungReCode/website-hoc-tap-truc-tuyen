import apiClient from './apiClient';

// ---------- Công khai — không cần đăng nhập ----------

/** Danh sách lớp học đang hoạt động (congKhai=true), tương tự Chợ Khóa Học. */
export async function timKiemLopHocCongKhai({
  maKhoaHoc,
  tuKhoa,
  gia,
  page = 0,
  size = 12,
  sort
} = {}) {
  const params = { page, size };

  if (maKhoaHoc) params.maKhoaHoc = maKhoaHoc;
  if (tuKhoa) params.tuKhoa = tuKhoa;
  if (gia) params.gia = gia;
  if (sort) params.sort = sort;

  const r = await apiClient.get('/lop-hoc/cong-khai', { params });
  return r.data;
}

// ---------- Giảng viên ----------

export async function layLopGiangVien() {
  const r = await apiClient.get('/lop-hoc/cua-toi');
  return r.data;
}

export async function taoLop(yeuCau) {
  const r = await apiClient.post('/lop-hoc', yeuCau);
  return r.data;
}

export async function layChiTietGiangVien(maLopHoc) {
  const r = await apiClient.get(`/lop-hoc/${maLopHoc}/quan-ly`);
  return r.data;
}

// Dành cho tác giả khóa học: xem toàn bộ lớp học
// (của mọi giảng viên) đang dùng khóa học này.
export async function layLopTheoKhoaHoc(maKhoaHoc) {
  const r = await apiClient.get(`/lop-hoc/theo-khoa-hoc/${maKhoaHoc}`);
  return r.data;
}

/**
 * Cập nhật cấu hình lớp học.
 *
 * Có thể dùng để cập nhật riêng sĩ số:
 * {
 *   siSo: 40
 * }
 *
 * hoặc cập nhật nhiều thuộc tính khác của lớp.
 */
export async function capNhatCauHinhLop(maLopHoc, yeuCau) {
  const r = await apiClient.put(
    `/lop-hoc/${maLopHoc}/cau-hinh`,
    yeuCau
  );

  return r.data;
}

export async function dongLop(maLopHoc) {
  const r = await apiClient.post(`/lop-hoc/${maLopHoc}/dong`);
  return r.data;
}

export async function xoaLop(maLopHoc) {
  await apiClient.delete(`/lop-hoc/${maLopHoc}`);
}

// ---------- Quản trị viên nội dung ----------

export async function layDanhSachLopHocChoAdmin({
  page = 0,
  size = 20,
  trangThai,
  maLop
} = {}) {
  const r = await apiClient.get('/lop-hoc/quan-tri', {
    params: {
      page,
      size,
      trangThai: trangThai || undefined,
      maLop: maLop || undefined
    }
  });

  return r.data;
}

// ---------- Học viên ----------

export async function layLopHocVien() {
  const r = await apiClient.get('/lop-hoc/cua-hoc-vien');
  return r.data;
}

// ---------- Đánh giá ----------

export async function layDanhGiaLop(
  maLopHoc,
  { page = 0, size = 10 } = {}
) {
  const r = await apiClient.get(
    `/lop-hoc/${maLopHoc}/danh-gia`,
    {
      params: {
        page,
        size
      }
    }
  );

  return r.data;
}

export async function layDanhGiaCuaToiLop(maLopHoc) {
  try {
    const r = await apiClient.get(
      `/lop-hoc/${maLopHoc}/danh-gia/cua-toi`
    );

    return r.status === 204 ? null : r.data;
  } catch (e) {
    if (e?.response?.status === 401) {
      return null;
    }

    throw e;
  }
}

export async function taoDanhGiaLop(
  maLopHoc,
  { soSao, nhanXet }
) {
  const r = await apiClient.post(
    `/lop-hoc/${maLopHoc}/danh-gia`,
    {
      soSao,
      nhanXet
    }
  );

  return r.data;
}

export async function capNhatDanhGiaLop(
  maDanhGia,
  { soSao, nhanXet }
) {
  const r = await apiClient.put(
    `/lop-hoc/danh-gia/${maDanhGia}`,
    {
      soSao,
      nhanXet
    }
  );

  return r.data;
}

export async function xoaDanhGiaLop(maDanhGia) {
  await apiClient.delete(
    `/lop-hoc/danh-gia/${maDanhGia}`
  );
}

// ---------- Tiến độ bài học ----------

export async function capNhatTienDoBaiHoc(
  maLopHoc,
  maThamChieu,
  loaiNguon,
  yeuCau
) {
  const duongDan =
    loaiNguon === 'GOC'
      ? `/lop-hoc/${maLopHoc}/tien-do/goc/${maThamChieu}`
      : `/lop-hoc/${maLopHoc}/tien-do/bo-sung/${maThamChieu}`;

  const r = await apiClient.put(
    duongDan,
    yeuCau
  );

  return r.data;
}