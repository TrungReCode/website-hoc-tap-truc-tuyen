import apiClient from './apiClient';

export async function thamGiaBangMa(maLop) {
  const r = await apiClient.post('/lop-hoc/tham-gia', { maLop });
  return r.data;
}

export async function layLoiMoiCuaToi() {
  const r = await apiClient.get('/loi-moi/cua-toi');
  return r.data;
}

export async function chapNhanLoiMoi(maLoiMoi) {
  const r = await apiClient.post(`/loi-moi/${maLoiMoi}/chap-nhan`);
  return r.data;
}

export async function tuChoiLoiMoi(maLoiMoi) {
  await apiClient.post(`/loi-moi/${maLoiMoi}/tu-choi`);
}

export async function moiHocVien(maLopHoc, dsEmail) {
  const r = await apiClient.post(`/lop-hoc/${maLopHoc}/moi`, { dsEmail });
  return r.data;
}

export async function layLoiMoiCuaLop(maLopHoc) {
  const r = await apiClient.get(`/lop-hoc/${maLopHoc}/loi-moi`);
  return r.data;
}

export async function layThanhVien(maLopHoc) {
  const r = await apiClient.get(`/lop-hoc/${maLopHoc}/thanh-vien`);
  return r.data;
}
