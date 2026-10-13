import apiClient from './apiClient';

// ─── BÀI ĐĂNG ────────────────────────────────────────────────

export async function taiLenTepDinhKem(maLopHoc, file, onTienDo) {
  const fd = new FormData();
  fd.append('file', file);
  const r = await apiClient.post(`/lop-hoc/${maLopHoc}/bai-dang/tai-len-tep`, fd, {
    headers: { 'Content-Type': undefined },
    onUploadProgress: onTienDo
      ? (evt) => {
          if (evt.total) onTienDo(Math.round((evt.loaded * 100) / evt.total));
        }
      : undefined,
  });
  return r.data; // TepDinhKem { ten, duongDan, loai }
}

export async function layDanhSachBaiDang(maLopHoc, { loai, page = 0, size = 10 } = {}) {
  const params = { page, size };
  if (loai) params.loai = loai;
  const r = await apiClient.get(`/lop-hoc/${maLopHoc}/bai-dang`, { params });
  return r.data;
}

export async function layChiTietBaiDang(maBaiDang) {
  const r = await apiClient.get(`/bai-dang/${maBaiDang}`);
  return r.data;
}

export async function taoBaiDang(maLopHoc, yeuCau) {
  const r = await apiClient.post(`/lop-hoc/${maLopHoc}/bai-dang`, yeuCau);
  return r.data;
}

export async function capNhatBaiDang(maBaiDang, yeuCau) {
  const r = await apiClient.put(`/bai-dang/${maBaiDang}`, yeuCau);
  return r.data;
}

export async function xoaBaiDang(maBaiDang) {
  await apiClient.delete(`/bai-dang/${maBaiDang}`);
}

export async function danhDauGiaiDap(maBaiDang, giaiDap = true) {
  const r = await apiClient.patch(`/bai-dang/${maBaiDang}/giai-dap`, null, {
    params: { giaiDap },
  });
  return r.data;
}

// ─── BÌNH LUẬN ───────────────────────────────────────────────

export async function layBinhLuan(maBaiDang) {
  const r = await apiClient.get(`/bai-dang/${maBaiDang}/binh-luan`);
  return r.data;
}

export async function taoBinhLuan(maBaiDang, { noiDung, maCha = null }) {
  const r = await apiClient.post(`/bai-dang/${maBaiDang}/binh-luan`, { noiDung, maCha });
  return r.data;
}

export async function xoaBinhLuan(maBinhLuan) {
  await apiClient.delete(`/binh-luan/${maBinhLuan}`);
}

// ─── BÀI TẬP: NỘP BÀI / CHẤM ĐIỂM (kiểu Google Classroom) ────

export async function nopBaiTap(maBaiDang, { ghiChu, tepDinhKem }) {
  const r = await apiClient.post(`/bai-dang/${maBaiDang}/nop-bai`, { ghiChu, tepDinhKem });
  return r.data; // PhanHoiBinhLuan (loaiBinhLuan = "NOP_BAI")
}

export async function chamDiem(maBinhLuanBaiNop, { diem, diemToiDa, nhanXet }) {
  const r = await apiClient.post(`/binh-luan/${maBinhLuanBaiNop}/cham-diem`, { diem, diemToiDa, nhanXet });
  return r.data; // PhanHoiBinhLuan (bài nộp, kèm reply chấm điểm mới nhất)
}

/** Giảng viên: trạng thái nộp bài (đã nộp/chưa nộp/đã chấm) của TOÀN BỘ học viên trong lớp. */
export async function layDanhSachNopBai(maBaiDang) {
  const r = await apiClient.get(`/bai-dang/${maBaiDang}/danh-sach-nop-bai`);
  return r.data; // PhanHoiTrangThaiNopBai[]
}

// ─── THÔNG BÁO ───────────────────────────────────────────────

export async function layThongBao({ page = 0, size = 20 } = {}) {
  const r = await apiClient.get('/thong-bao', { params: { page, size } });
  return r.data;
}

export async function demChuaDoc() {
  try {
    const r = await apiClient.get('/thong-bao/so-chua-doc');
    return r.data;
  } catch {
    return 0;
  }
}

export async function docTatCa() {
  await apiClient.patch('/thong-bao/doc-tat-ca');
}

export async function docMot(maThongBao) {
  await apiClient.patch(`/thong-bao/${maThongBao}/doc`);
}