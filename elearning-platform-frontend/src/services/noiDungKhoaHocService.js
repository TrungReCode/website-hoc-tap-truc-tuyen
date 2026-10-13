import apiClient from './apiClient';

/**
 * Khớp 1:1 với NoiDungKhoaHocController.java.
 */

// ---------- Chương ----------

export async function themChuong(maKhoaHoc, { tieuDe, viTri }) {
  const response = await apiClient.post(`/khoa-hoc/${maKhoaHoc}/chuong`, { tieuDe, viTri });
  return response.data;
}

export async function capNhatChuong(maChuong, { tieuDe, viTri }) {
  const response = await apiClient.put(`/chuong/${maChuong}`, { tieuDe, viTri });
  return response.data;
}

export async function xoaChuong(maChuong) {
  await apiClient.delete(`/chuong/${maChuong}`);
}

// ---------- Bài học (multipart — kèm file video/tài liệu) ----------

export async function themBaiHoc(maChuong, { tieuDe, loai, viTri, xemThu, diemToiThieu, thoiGian, file }, onTienDo) {
  const formData = new FormData();
  formData.append('tieuDe', tieuDe);
  formData.append('loai', loai);
  formData.append('viTri', viTri);
  formData.append('xemThu', xemThu ? 'true' : 'false');
  if (diemToiThieu != null) formData.append('diemToiThieu', diemToiThieu);
  if (thoiGian != null) formData.append('thoiGian', thoiGian);
  if (file) formData.append('file', file);

  // QUAN TRỌNG: không tự set 'Content-Type': 'multipart/form-data' — nếu set tay sẽ
  // thiếu tham số "boundary" bắt buộc, khiến Spring không parse được file. Phải để
  // trình duyệt tự sinh header này khi phát hiện body là FormData, nên ở đây cần
  // ghi đè header mặc định 'application/json' của apiClient thành undefined.
  //
  // onTienDo(phanTram): gọi liên tục trong lúc trình duyệt gửi file lên BACKEND.
  // Lưu ý: sau khi đạt 100%, backend còn phải tự tải video lên Cloudinary (có thể
  // mất thêm vài chục giây với video lớn) — request vẫn đang chờ phản hồi trong
  // lúc đó, chỉ là trình duyệt không biết % của bước này. UI gọi onTienDo nên tự
  // chuyển sang trạng thái "đang xử lý" (vô định) khi phanTram chạm 100.
  const response = await apiClient.post(`/chuong/${maChuong}/bai-hoc`, formData, {
    headers: { 'Content-Type': undefined },
    onUploadProgress: onTienDo
      ? (evt) => {
          if (evt.total) onTienDo(Math.round((evt.loaded * 100) / evt.total));
        }
      : undefined,
  });
  return response.data;
}

export async function capNhatBaiHoc(maBaiHoc, { tieuDe, viTri, xemThu }) {
  const response = await apiClient.put(`/bai-hoc/${maBaiHoc}`, { tieuDe, viTri, xemThu });
  return response.data;
}

export async function xoaBaiHoc(maBaiHoc) {
  await apiClient.delete(`/bai-hoc/${maBaiHoc}`);
}

// ---------- Câu hỏi quiz ----------

export async function themCauHoi(maBaiHoc, { noiDung, viTri, loaiCauHoi, dapAn }) {
  const response = await apiClient.post(`/bai-hoc/${maBaiHoc}/cau-hoi`, { noiDung, viTri, loaiCauHoi, dapAn });
  return response.data;
}

export async function xoaCauHoi(maCauHoi) {
  await apiClient.delete(`/cau-hoi/${maCauHoi}`);
}