import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layThongKeHeThong } from '../../services/adminService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { NAV_ADMIN_CAO_CAP } from '../../components/DashboardShell';

const dinhDangTien = (n) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n || 0);

export default function BangDieuKhienCaoCap() {
  const navigate = useNavigate();
  const [tk, setTk] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    layThongKeHeThong()
      .then(setTk)
      .catch(err => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, []);



  return (
    <DashboardShell roleLabel="Quản trị viên cấp cao" navItems={NAV_ADMIN_CAO_CAP} crumb="Tổng quan">
      <div className="ds-page-head">
        <div>
          <h1>Tổng quan hệ thống</h1>
          <p>Số liệu tổng hợp về người dùng, nội dung, tài chính và tương tác trên toàn nền tảng</p>
        </div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}
      {dangTai && <p style={{ color: 'var(--text-3)', fontSize: 13 }}>Đang tải dữ liệu...</p>}

        {tk && (
          <>
            {/* ── Người dùng ── */}
            <h3 style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: .5, marginBottom: 10 }}>
              Người dùng
            </h3>
            <div className="stats-grid" style={{ marginBottom: 24 }}>
              <StatCard icon="ti-users" label="Tổng tài khoản" value={tk.tongNguoiDung} mau="var(--primary)"
                onClick={() => navigate('/admin/nguoi-dung')} />
              <StatCard icon="ti-circle-check" label="Hoạt động" value={tk.nguoiDungHoatDong} mau="var(--success)"
                onClick={() => navigate('/admin/nguoi-dung?trangThai=HOAT_DONG')} />
              <StatCard icon="ti-clock" label="Chờ xác nhận" value={tk.nguoiDungChoXacNhan} mau="var(--warning)"
                onClick={() => navigate('/admin/nguoi-dung?trangThai=CHO_XAC_NHAN')} />
              <StatCard icon="ti-ban" label="Bị cấm" value={tk.nguoiDungBiCam} mau="var(--danger)"
                onClick={() => navigate('/admin/nguoi-dung?trangThai=BI_CAM')} />
              <StatCard icon="ti-eye" label="QTV Nội dung" value={tk.qtvNoiDung} mau="#7c3aed"
                onClick={() => navigate('/admin/nguoi-dung?vaiTro=QTV_NOI_DUNG')} />
              <StatCard icon="ti-coin" label="QTV Tài chính" value={tk.qtvTaiChinh} mau="#0891b2"
                onClick={() => navigate('/admin/nguoi-dung?vaiTro=QTV_TAI_CHINH')} />
            </div>

            {/* ── Nội dung ── */}
            <h3 style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: .5, marginBottom: 10 }}>
              Nội dung
            </h3>
            <div className="stats-grid" style={{ marginBottom: 24 }}>
              {/* Không có trang "danh sách tất cả khóa học" trong hệ thống —
                  3 ô dưới đây tạm thời KHÔNG bấm được, để tránh trỏ tới link giả. */}
              <StatCard icon="ti-book" label="Tổng khóa học" value={tk.tongKhoaHoc} mau="var(--primary)" />
              <StatCard icon="ti-world" label="Khóa học công khai" value={tk.khoaHocCongKhai} mau="var(--success)" />
              <StatCard icon="ti-hourglass" label="Chờ kiểm duyệt" value={tk.khoaHocChoDuyet} mau="var(--warning)"
                onClick={() => navigate('/admin/kiem-duyet')} />
              <StatCard icon="ti-pencil" label="Bản nháp" value={tk.khoaHocNhap} mau="var(--text-3)" />
              <StatCard icon="ti-school" label="Tổng lớp học" value={tk.tongLopHoc} mau="var(--primary)"
                onClick={() => navigate('/admin/lop-hoc')} />
              <StatCard icon="ti-activity" label="Lớp đang hoạt động" value={tk.lopHocDangHoatDong} mau="var(--success)"
                onClick={() => navigate('/admin/lop-hoc?trangThai=HOAT_DONG')} />
            </div>

            {/* ── Tài chính ── */}
            <h3 style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: .5, marginBottom: 10 }}>
              Tài chính
            </h3>
            <div className="stats-grid" style={{ marginBottom: 24 }}>
              <StatCard icon="ti-cash" label="DT khóa học" value={dinhDangTien(tk.tongDoanhThuKhoaHoc)} mau="var(--success)" kieu="tien"
                onClick={() => navigate('/admin/tai-chinh?tab=doanh-thu')} />
              <StatCard icon="ti-cash" label="DT lớp học" value={dinhDangTien(tk.tongDoanhThuLopHoc)} mau="var(--success)" kieu="tien"
                onClick={() => navigate('/admin/tai-chinh?tab=doanh-thu')} />
              <StatCard icon="ti-check" label="GD thành công" value={tk.soGiaoDichThanhCong} mau="var(--success)"
                onClick={() => navigate('/admin/tai-chinh?tab=doanh-thu')} />
              <StatCard icon="ti-clock" label="GD chờ xác nhận" value={tk.soGiaoDichChoXacNhan} mau="var(--warning)"
                onClick={() => navigate('/admin/tai-chinh?tab=doanh-thu')} />
              <StatCard icon="ti-receipt-2" label="Phiếu đối soát chờ" value={tk.soPhieuDoiSoatChuaXuLy} mau="var(--danger)"
                onClick={() => navigate('/admin/tai-chinh?tab=doi-soat&trangThai=CHO_XAC_NHAN')} />
            </div>

            {/* ── Tương tác ── */}
            <h3 style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: .5, marginBottom: 10 }}>
              Tương tác
            </h3>
            <div className="stats-grid">
              {/* Diễn đàn chỉ xem được theo từng lớp học riêng lẻ, không có
                  trang tổng hợp toàn hệ thống — 2 ô này KHÔNG bấm được. */}
              <StatCard icon="ti-speakerphone" label="Tổng bài đăng" value={tk.tongBaiDang} mau="#7c3aed" />
              <StatCard icon="ti-message-2" label="Tổng bình luận" value={tk.tongBinhLuan} mau="#0891b2" />
            </div>
          </>
        )}
    </DashboardShell>
  );
}

function StatCard({ icon, label, value, mau, kieu, onClick }) {
  return (
    <div
      className="stat-card"
      style={{ cursor: onClick ? 'pointer' : 'default', position: 'relative' }}
      onClick={onClick}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onKeyDown={onClick ? (e) => { if (e.key === 'Enter') onClick(); } : undefined}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <div style={{
          width: 32, height: 32, borderRadius: 8, background: mau + '18',
          display: 'flex', alignItems: 'center', justifyContent: 'center', color: mau, fontSize: 16,
        }}>
          <i className={`ti ${icon}`} aria-hidden="true"></i>
        </div>
        <span className="stat-label" style={{ margin: 0 }}>{label}</span>
        {/* Gợi ý trực quan: ô nào bấm được thì có mũi tên nhỏ góc trên phải,
            để người dùng không phải thử bấm từng ô mới biết. */}
        {onClick && (
          <i className="ti ti-arrow-up-right" aria-hidden="true"
            style={{ marginLeft: 'auto', fontSize: 13, color: 'var(--text-3)' }}></i>
        )}
      </div>
      <div className="stat-value" style={{ fontSize: kieu === 'tien' ? 16 : 24, color: mau }}>
        {value}
      </div>
    </div>
  );
}
