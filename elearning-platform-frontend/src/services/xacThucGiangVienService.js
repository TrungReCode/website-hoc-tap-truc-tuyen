import apiClient from './apiClient';

// ---------- Người dùng: gửi yêu cầu + tự xem trạng thái ----------

export async function guiYeuCauXacThuc({ ngaySinh, bangCap, file }, onTienDo) {
    const formData = new FormData();
    formData.append('ngaySinh', ngaySinh);
    formData.append('bangCap', bangCap);
    formData.append('file', file);

    const response = await apiClient.post('/xac-thuc-giang-vien', formData, {
        headers: { 'Content-Type': undefined },
        onUploadProgress: onTienDo
            ? (evt) => {
                if (evt.total) onTienDo(Math.round((evt.loaded * 100) / evt.total));
            }
            : undefined,
    });
    return response.data;
}

/** Trả về null nếu chưa từng gửi yêu cầu (backend trả 204 No Content). */
export async function layTrangThaiXacThucCuaToi() {
    const response = await apiClient.get('/xac-thuc-giang-vien/cua-toi');
    return response.status === 204 ? null : response.data;
}

// ---------- Admin: duyệt yêu cầu ----------

export async function layDanhSachXacThucChoAdmin({ trangThai, page = 0, size = 10 } = {}) {
    const params = { page, size };
    if (trangThai) params.trangThai = trangThai;
    const response = await apiClient.get('/admin/xac-thuc-giang-vien', { params });
    return response.data;
}

export async function duyetXacThuc(maXacThuc, { chapDuyet, lyDoTuChoi }) {
    const response = await apiClient.post(`/admin/xac-thuc-giang-vien/${maXacThuc}/duyet`, {
        chapDuyet,
        lyDoTuChoi: lyDoTuChoi || null,
    });
    return response.data;
}

/** Tải file minh chứng (blob) — mở trong tab mới hoặc tự động tải xuống. */
export async function taiFileMinhChung(maXacThuc, tenFileGoi) {
    const response = await apiClient.get(`/admin/xac-thuc-giang-vien/${maXacThuc}/file`, {
        responseType: 'blob',
    });
    const url = window.URL.createObjectURL(response.data);
    const a = document.createElement('a');
    a.href = url;
    a.download = tenFileGoi || 'minh-chung';
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}