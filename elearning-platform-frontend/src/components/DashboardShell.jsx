import { Link, useLocation, useNavigate } from 'react-router-dom';
import NutThongBao from './NutThongBao';
import { dangXuat as dangXuatApi } from '../services/xacThucService';

/**
 * Khung điều hướng dạng sidebar tối dùng chung cho các trang Giảng viên
 * và Quản trị viên, khớp với "Hệ thống màn hình" (mục 3.3 tài liệu đồ án).
 *
 * Cách dùng:
 *   <DashboardShell
 *     roleLabel="Giảng viên"
 *     navItems={NAV_GIANG_VIEN}
 *     crumb={<>Quản lý khóa học</>}
 *   >
 *     ...nội dung trang...
 *   </DashboardShell>
 */

export const NAV_GIANG_VIEN = [
  { to: '/giang-vien', label: 'Tổng quan', icon: 'ti-layout-dashboard' },
  { to: '/giang-vien/khoa-hoc', label: 'Quản lý khóa học', icon: 'ti-books' },
  { to: '/lop-hoc/giang-vien', label: 'Quản lý lớp học', icon: 'ti-chalkboard' },
  { to: '/giang-vien/doanh-thu', label: 'Doanh thu & Thống kê', icon: 'ti-chart-bar' },
];

export const NAV_ADMIN_NOI_DUNG = [
  { to: '/admin/noi-dung', label: 'Tổng quan', icon: 'ti-layout-dashboard' },
  { to: '/admin/xac-thuc-giang-vien', label: 'Xác thực giảng viên', icon: 'ti-certificate' },
  { to: '/admin/kiem-duyet', label: 'Hàng chờ kiểm duyệt', icon: 'ti-clipboard-check' },
  { to: '/admin/kiem-duyet-danh-gia', label: 'Kiểm duyệt đánh giá', icon: 'ti-star' },
  { to: '/admin/lop-hoc', label: 'Lớp học', icon: 'ti-chalkboard' },
  { to: '/admin/danh-muc', label: 'Danh mục', icon: 'ti-category' },
  { to: '/admin/nguoi-dung', label: 'Tài khoản người dùng', icon: 'ti-users' },
];

export const NAV_ADMIN_TAI_CHINH = [
  { to: '/admin/tai-chinh', label: 'Đối soát giải ngân', icon: 'ti-report-money' },
];

export const NAV_ADMIN_CAO_CAP = [
  { to: '/admin', label: 'Tổng quan', icon: 'ti-layout-dashboard' },
  { to: '/admin/xac-thuc-giang-vien', label: 'Xác thực giảng viên', icon: 'ti-certificate' },
  { to: '/admin/nguoi-dung', label: 'Tài khoản người dùng', icon: 'ti-users' },
  { to: '/admin/tai-khoan-admin', label: 'Tài khoản quản trị', icon: 'ti-user-shield' },
  { to: '/admin/kiem-duyet', label: 'Kiểm duyệt nội dung', icon: 'ti-clipboard-check' },
  { to: '/admin/tai-chinh', label: 'Đối soát giải ngân', icon: 'ti-report-money' },
  { to: '/admin/danh-muc', label: 'Danh mục khóa học', icon: 'ti-category' },
  { to: '/admin/lop-hoc', label: 'Lớp học', icon: 'ti-chalkboard' },
  { to: '/admin/cau-hinh-he-thong', label: 'Cấu hình hệ thống', icon: 'ti-settings' },
  { to: '/admin/nhat-ky', label: 'Nhật ký hệ thống', icon: 'ti-history' },
];

// LƯU Ý: enum VaiTro ở backend (dinhDanhPhanQuyen.mienDomain.enumDomain.VaiTro)
// chỉ có 4 giá trị: QTV_CAO_CAP, QTV_NOI_DUNG, QTV_TAI_CHINH, NGUOI_DUNG.
// "Giảng viên" và "Học viên" KHÔNG phải vai trò lưu trong CSDL — bất kỳ
// NGUOI_DUNG nào cũng có thể là tác giả khóa học (giảng viên theo ngữ cảnh)
// đồng thời là học viên của khóa học khác. Vì vậy hàm layNavTheoVaiTro() bên
// dưới chỉ áp dụng cho các trang QTV dùng chung nhiều vai trò quản trị; các
// trang Giảng viên (BangDieuKhienGiangVien, DanhSachKhoaHocGiangVien...) tự
// truyền thẳng roleLabel="Giảng viên" và navItems={NAV_GIANG_VIEN} khi dùng
// DashboardShell, không đi qua hàm này.
const NHAN_VAI_TRO = {
  QTV_CAO_CAP: 'Quản trị viên cấp cao',
  QTV_NOI_DUNG: 'Quản trị viên nội dung',
  QTV_TAI_CHINH: 'Quản trị viên tài chính',
};

export function layNavTheoVaiTro() {
  const vaiTro = localStorage.getItem('vaiTro');
  if (vaiTro === 'QTV_CAO_CAP') return { navItems: NAV_ADMIN_CAO_CAP, roleLabel: NHAN_VAI_TRO[vaiTro] };
  if (vaiTro === 'QTV_NOI_DUNG') return { navItems: NAV_ADMIN_NOI_DUNG, roleLabel: NHAN_VAI_TRO[vaiTro] };
  if (vaiTro === 'QTV_TAI_CHINH') return { navItems: NAV_ADMIN_TAI_CHINH, roleLabel: NHAN_VAI_TRO[vaiTro] };
  return { navItems: [], roleLabel: NHAN_VAI_TRO[vaiTro] || '' };
}


export default function DashboardShell({ roleLabel, navItems, crumb, actions, children }) {
  const location = useLocation();
  const navigate = useNavigate();
  const hoTen = localStorage.getItem('hoTen') || '';
  const chuCai = hoTen ? hoTen.trim().slice(0, 1).toUpperCase() : '?';

  async function dangXuat() {
    // FIX (Vấn đề #2): như TrangChu.jsx — accessToken/refreshToken nằm
    // trong cookie HttpOnly nên JS không tự xoá được, bắt buộc gọi API để
    // backend trả Set-Cookie xoá cookie và thu hồi refresh token trong DB.
    try {
      await dangXuatApi();
    } catch {
      // Vẫn dọn phía client và điều hướng dù API lỗi — không để kẹt UI.
    }
    localStorage.clear();
    navigate('/dang-nhap');
  }

  return (
    <div className="ds-shell">
      <aside className="ds-sidebar">
        <div className="ds-brand">
          <span className="dot"></span> EduHub VN
        </div>
        {roleLabel && <div className="ds-role">{roleLabel}</div>}
        {navItems.map((item) => {
          // FIX: "startsWith" so khớp THEO CHUỖI, không theo ranh giới path —
          // "/admin/kiem-duyet-danh-gia".startsWith("/admin/kiem-duyet") trả
          // về true dù đây là 2 route hoàn toàn khác nhau, khiến cả 2 mục
          // "Hàng chờ kiểm duyệt" lẫn "Kiểm duyệt đánh giá" cùng sáng lên khi
          // đang ở trang kiểm duyệt đánh giá. Bắt buộc phải có dấu "/" ngay
          // sau tiền tố (hoặc khớp tuyệt đối) mới coi là active, để phân biệt
          // đúng route cha/con thật (VD: "/admin/kiem-duyet/123") với 2 route
          // chỉ tình cờ trùng tiền tố ký tự.
          const active = location.pathname === item.to
            || (item.to !== '/admin' && item.to !== '/giang-vien' && location.pathname.startsWith(item.to + '/'));
          return (
            <Link key={item.to} to={item.to} className={active ? 'active' : ''}>
              <i className={`ti ${item.icon}`} aria-hidden="true"></i>
              <span>{item.label}</span>
            </Link>
          );
        })}
        <div className="ds-sep"></div>
        <button type="button" className="ds-link" onClick={dangXuat}>
          <i className="ti ti-logout" aria-hidden="true"></i>
          <span>Đăng xuất</span>
        </button>
        <div className="ds-foot">© EduHub VN {new Date().getFullYear()}</div>
      </aside>

      <div className="ds-main">
        <div className="ds-topstrip">
          <div className="ds-crumb">{crumb}</div>
          <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
            <NutThongBao />
            {actions}
            <div className="avatar" title={hoTen}>{chuCai}</div>
          </div>
        </div>
        <div className="ds-content">{children}</div>
      </div>
    </div>
  );
}