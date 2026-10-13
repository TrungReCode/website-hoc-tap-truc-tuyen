import axios from 'axios';

// FIX (Vấn đề #2 — localStorage -> cookie HttpOnly):
// - withCredentials: true để trình duyệt gửi kèm cookie accessToken/
//   refreshToken (HttpOnly, do backend set qua CongCuCookie) trên mọi
//   request, kể cả khi frontend/backend khác cổng (Vite dev proxy /api
//   khiến trình duyệt coi là cùng origin, nhưng để y nguyên vẫn đúng khi
//   không qua proxy — vd. gọi thẳng backend production).
// - KHÔNG còn interceptor tự gắn header Authorization từ localStorage:
//   JS giờ không đọc được giá trị 2 cookie này nữa (đúng mục đích HttpOnly),
//   nên cũng không có gì để tự gắn — trình duyệt tự làm việc đó.
const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// BỘ ĐÁNH CHẶN LỖI (RESPONSE INTERCEPTOR) — làm mới access token khi hết hạn

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const urlGoc = originalRequest?.url || '';
    const laRequestXacThuc = urlGoc.startsWith('/xac-thuc/');

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !laRequestXacThuc) {

      // Nhiều request cùng lúc gặp 401 khi token hết hạn -> xếp hàng chờ,
      // chỉ 1 request thực sự gọi lam-moi-token (tránh rotation bị đua nhau).
      if (isRefreshing) {
        return new Promise(function (resolve, reject) {
          failedQueue.push({ resolve, reject });
        }).then(() => {
          // Cookie mới đã được trình duyệt tự cập nhật qua Set-Cookie của
          // lần làm mới trước đó (do request đó thực hiện) — chỉ cần gọi
          // lại request gốc, KHÔNG cần tự gắn token nào vào đây nữa.
          return apiClient(originalRequest);
        }).catch(err => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Không cần đọc/truyền refreshToken thủ công nữa — cookie
        // refreshToken (path=/api/xac-thuc) được trình duyệt tự đính kèm.
        // Dùng axios mặc định (không phải apiClient) để tránh vòng lặp vô
        // hạn nếu chính request này lại trả 401, nhưng vẫn cần
        // withCredentials: true để cookie được gửi đi.
        await axios.post('/api/xac-thuc/lam-moi-token', null, { withCredentials: true });

        // Backend đã set cookie accessToken/refreshToken MỚI qua Set-Cookie
        // trong chính response trên — không còn gì để lưu vào localStorage.
        processQueue(null);

        return apiClient(originalRequest);

      } catch (err) {
        processQueue(err);

        // Backend có phản hồi nhưng từ chối làm mới (400 refresh token hết hạn/đã
        // thu hồi, 401, 403...) -> phiên đã chết -> về trang đăng nhập. Lỗi mạng
        // thuần tuý (không có response) thì KHÔNG đá ra, người dùng còn thử lại được.
        if (err.response) {
          localStorage.removeItem('daDangNhap');
          localStorage.removeItem('hoTen');
          localStorage.removeItem('vaiTro');
          localStorage.removeItem('maNguoiDung');

          window.location.href = '/dang-nhap';
        }

        return Promise.reject(err);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;