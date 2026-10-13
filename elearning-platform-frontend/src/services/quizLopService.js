import apiClient from './apiClient';

export async function layDeBaiGoc(maLopHoc, maBaiHoc) {
  const r = await apiClient.get(`/lop-hoc/${maLopHoc}/quiz/goc/${maBaiHoc}/de-bai`);
  return r.data;
}

export async function layDeBoSung(maLopHoc, maBaiHocBoSung) {
  const r = await apiClient.get(`/lop-hoc/${maLopHoc}/quiz/bo-sung/${maBaiHocBoSung}/de-bai`);
  return r.data;
}

export async function nopBaiGoc(maLopHoc, maBaiHoc, cauTraLoi) {
  const r = await apiClient.post(`/lop-hoc/${maLopHoc}/quiz/goc/${maBaiHoc}/nop-bai`, { cauTraLoi });
  return r.data;
}

export async function nopBaiBoSung(maLopHoc, maBaiHocBoSung, cauTraLoi) {
  const r = await apiClient.post(`/lop-hoc/${maLopHoc}/quiz/bo-sung/${maBaiHocBoSung}/nop-bai`, { cauTraLoi });
  return r.data;
}

export async function layKetQuaGoc(maLopHoc, maBaiHoc) {
  try {
    const r = await apiClient.get(`/lop-hoc/${maLopHoc}/quiz/goc/${maBaiHoc}/ket-qua`);
    return r.status === 204 ? null : r.data;
  } catch { return null; }
}

export async function layKetQuaBoSung(maLopHoc, maBaiHocBoSung) {
  try {
    const r = await apiClient.get(`/lop-hoc/${maLopHoc}/quiz/bo-sung/${maBaiHocBoSung}/ket-qua`);
    return r.status === 204 ? null : r.data;
  } catch { return null; }
}
