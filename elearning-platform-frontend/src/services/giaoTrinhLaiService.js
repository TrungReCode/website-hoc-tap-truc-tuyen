import apiClient from './apiClient';

// ---------- Giáo trình lai ----------

export async function layGiaoTrinhGiangVien(maLopHoc) {
  const r = await apiClient.get(`/lop-hoc/${maLopHoc}/giao-trinh/quan-ly`);
  return r.data;
}

export async function layGiaoTrinhHocVien(maLopHoc) {
  const r = await apiClient.get(`/lop-hoc/${maLopHoc}/giao-trinh`);
  return r.data;
}

/** Cho Quản trị viên nội dung giám sát — xem cả bài học bổ sung không qua kiểm duyệt khóa học. */
export async function layGiaoTrinhChoAdmin(maLopHoc) {
  const r = await apiClient.get(`/admin/lop-hoc/${maLopHoc}/giao-trinh`);
  return r.data;
}

/** Xem chi tiết câu hỏi + đáp án đúng của 1 bài Quiz — dùng để kiểm duyệt sâu. */
export async function layDeBaiQuizChoAdmin(loaiNguon, maThamChieu) {
  const r = await apiClient.get(`/admin/quiz/${loaiNguon}/${maThamChieu}`);
  return r.data;
}

export async function capNhatCauHinhBaiGoc(maLopHoc, maBaiHoc, { anBaiHoc, viTriMoi }) {
  await apiClient.put(`/lop-hoc/${maLopHoc}/cau-hinh-bai-goc/${maBaiHoc}`, { anBaiHoc, viTriMoi });
}

// ---------- Bài học bổ sung (multipart) ----------

export async function themBaiBoSung(maLopHoc, params, file, onTienDo) {
  const fd = new FormData();
  fd.append('tieuDe', params.tieuDe);
  fd.append('loai', params.loai);
  fd.append('anHien', String(params.anHien !== false));
  if (params.moTa) fd.append('moTa', params.moTa);
  if (params.viTriSau != null) fd.append('viTriSau', params.viTriSau);
  if (params.viTriTruoc != null) fd.append('viTriTruoc', params.viTriTruoc);
  if (params.duongDanLink) fd.append('duongDanLink', params.duongDanLink);
  if (params.diemToiThieu != null) fd.append('diemToiThieu', params.diemToiThieu);
  if (params.thoiGian != null) fd.append('thoiGian', params.thoiGian);
  if (file) fd.append('file', file);

  // KHÔNG set Content-Type thủ công — để browser tự sinh boundary cho multipart
  const r = await apiClient.post(`/lop-hoc/${maLopHoc}/bai-bo-sung`, fd, {
    headers: { 'Content-Type': undefined },
    onUploadProgress: onTienDo
      ? (evt) => {
          if (evt.total) onTienDo(Math.round((evt.loaded * 100) / evt.total));
        }
      : undefined,
  });
  return r.data;
}

export async function capNhatBaiBoSung(maBaiHocBoSung, { tieuDe, moTa, anHien, viTriSau, viTriTruoc }) {
  const r = await apiClient.put(`/bai-bo-sung/${maBaiHocBoSung}`, {
    tieuDe, moTa, anHien,
    viTriSau: viTriSau ?? null,
    viTriTruoc: viTriTruoc ?? null,
  });
  return r.data;
}

export async function xoaBaiBoSung(maBaiHocBoSung) {
  await apiClient.delete(`/bai-bo-sung/${maBaiHocBoSung}`);
}

export async function themCauHoiBoSung(maBaiHocBoSung, yeuCau) {
  await apiClient.post(`/bai-bo-sung/${maBaiHocBoSung}/cau-hoi`, yeuCau);
}

export async function layCauHoiBoSung(maBaiHocBoSung) {
  const r = await apiClient.get(`/bai-bo-sung/${maBaiHocBoSung}/cau-hoi`);
  return r.data;
}

export async function xoaCauHoiBoSung(maCauHoiBoSung) {
  await apiClient.delete(`/cau-hoi-bo-sung/${maCauHoiBoSung}`);
}

// ---------- Tiến độ ----------

export async function capNhatTienDoBaiGoc(maLopHoc, maBaiHoc, { viTriDung, hoanThanh } = {}) {
  await apiClient.put(`/lop-hoc/${maLopHoc}/tien-do/bai-goc/${maBaiHoc}`, { viTriDung, hoanThanh });
}

export async function capNhatTienDoBaiBoSung(maBaiHocBoSung, { viTriDung, hoanThanh } = {}) {
  await apiClient.put(`/tien-do/bai-bo-sung/${maBaiHocBoSung}`, { viTriDung, hoanThanh });
}