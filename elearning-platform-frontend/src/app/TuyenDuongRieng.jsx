import { Navigate } from 'react-router-dom';

const VAI_TRO_ADMIN = ['QTV_CAO_CAP', 'QTV_NOI_DUNG', 'QTV_TAI_CHINH'];

/**
 * Bảo vệ route theo vai trò.
 *
 * chiAdmin    : bất kỳ vai trò quản trị (QTV_CAO_CAP | QTV_NOI_DUNG | QTV_TAI_CHINH)
 * chiCaoCap   : chỉ QTV_CAO_CAP
 * chiNoiDung  : QTV_CAO_CAP hoặc QTV_NOI_DUNG
 * chiTaiChinh : QTV_CAO_CAP hoặc QTV_TAI_CHINH
 * (không truyền prop nào) : chỉ cần đã đăng nhập
 */
export default function TuyenDuongRieng({
  children,
  chiAdmin    = false,
  chiCaoCap   = false,
  chiNoiDung  = false,
  chiTaiChinh = false,
}) {
  // FIX (Vấn đề #2): accessToken không còn đọc được từ localStorage nữa
  // (đã chuyển sang cookie HttpOnly) nên dùng "daDangNhap" — một CỜ HIỂN
  // THỊ do TrangDangNhap set, không phải bí mật — chỉ để quyết định render
  // UI nào. Đây vẫn CHỈ là kiểm tra phía client cho UX (như trước khi sửa):
  // quyền truy cập THẬT luôn do backend tự xác thực lại qua cookie mỗi
  // request (@PreAuthorize + BoLocJWT), route guard này giả mạo được nhưng
  // không rò dữ liệu gì (xem Vấn đề #9 trong báo cáo đồ án).
  const daDangNhap = localStorage.getItem('daDangNhap');
  const vaiTro     = localStorage.getItem('vaiTro');

  if (!daDangNhap) return <Navigate to="/dang-nhap" replace />;

  if (chiCaoCap   && vaiTro !== 'QTV_CAO_CAP')                            return <Navigate to="/trang-chu" replace />;
  if (chiNoiDung  && !['QTV_CAO_CAP', 'QTV_NOI_DUNG'].includes(vaiTro))  return <Navigate to="/trang-chu" replace />;
  if (chiTaiChinh && !['QTV_CAO_CAP', 'QTV_TAI_CHINH'].includes(vaiTro)) return <Navigate to="/trang-chu" replace />;
  if (chiAdmin    && !VAI_TRO_ADMIN.includes(vaiTro))                     return <Navigate to="/trang-chu" replace />;

  return children;
}