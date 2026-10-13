import apiClient from './apiClient';

// Khớp 1:1 với QuizTuHocController.java (com.elearning.lopHocLMS.giaoDien).
// Dùng cho khóa học CÁ NHÂN (tự học, không thuộc lớp nào) — khác quizLopService.js
// vốn dùng cho quiz trong lớp học (cần maLopHoc).

export async function layDeBaiTuHoc(maBaiHoc) {
  const r = await apiClient.get(`/khoa-hoc/tu-hoc/quiz/${maBaiHoc}/de-bai`);
  return r.data;
}

export async function nopBaiTuHoc(maBaiHoc, cauTraLoi) {
  const r = await apiClient.post(`/khoa-hoc/tu-hoc/quiz/${maBaiHoc}/nop-bai`, { cauTraLoi });
  return r.data;
}

export async function layKetQuaTuHoc(maBaiHoc) {
  try {
    const r = await apiClient.get(`/khoa-hoc/tu-hoc/quiz/${maBaiHoc}/ket-qua`);
    return r.status === 204 ? null : r.data;
  } catch { return null; }
}
