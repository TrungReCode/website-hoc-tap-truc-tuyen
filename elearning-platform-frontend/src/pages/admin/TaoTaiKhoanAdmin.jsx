import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { taoTaiKhoanAdmin } from '../../services/adminService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { NAV_ADMIN_CAO_CAP } from '../../components/DashboardShell';

export default function TaoTaiKhoanAdmin() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    hoTen: '', email: '', matKhau: '', vaiTro: 'QTV_NOI_DUNG',
  });
  const [dangGui, setDangGui]   = useState(false);
  const [loi,     setLoi]       = useState('');
  const [thanhCong, setThanhCong] = useState(null);

  function capNhat(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
  }

  async function xuLyGui(e) {
    e.preventDefault();
    setLoi(''); setThanhCong(null);
    if (!form.hoTen.trim() || !form.email.trim() || !form.matKhau) {
      setLoi('Vui lòng điền đầy đủ thông tin.'); return;
    }
    setDangGui(true);
    try {
      const nd = await taoTaiKhoanAdmin(form);
      setThanhCong(nd);
      setForm({ hoTen: '', email: '', matKhau: '', vaiTro: 'QTV_NOI_DUNG' });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  }

  return (
    <DashboardShell roleLabel="Quản trị viên cấp cao" navItems={NAV_ADMIN_CAO_CAP} crumb={<>Tài khoản quản trị / <b>Tạo mới</b></>}>
      <div style={{ maxWidth: 520, margin: '0 auto', width: '100%' }}>
        {thanhCong && (
          <div className="alert alert-success" style={{ marginBottom: 16 }}>
            <i className="ti ti-circle-check" aria-hidden="true"></i>
            Đã tạo thành công tài khoản <strong>{thanhCong.hoTen}</strong> ({thanhCong.vaiTro}).
            Tài khoản đã được kích hoạt ngay lập tức.
          </div>
        )}

        <div className="card">
          <div className="card-title">Thông tin tài khoản mới</div>
          <p style={{ fontSize: 12, color: 'var(--text-3)', marginBottom: 16 }}>
            Tài khoản admin được kích hoạt ngay mà không cần xác nhận email.
            Chỉ có thể tạo QTV Nội dung hoặc QTV Tài chính.
          </p>

          {loi && <div className="alert alert-danger">{loi}</div>}

          <div className="form-group">
            <label className="form-label">Vai trò *</label>
            <div style={{ display: 'flex', gap: 10 }}>
              {[
                { val: 'QTV_NOI_DUNG',  nhan: 'QTV Nội dung',  icon: 'ti-eye',  mota: 'Kiểm duyệt khóa học, quản lý tài khoản người dùng' },
                { val: 'QTV_TAI_CHINH', nhan: 'QTV Tài chính', icon: 'ti-coin', mota: 'Xem thống kê doanh thu, xử lý đối soát thanh toán'  },
              ].map(v => (
                <div key={v.val}
                  onClick={() => setForm(f => ({ ...f, vaiTro: v.val }))}
                  style={{
                    flex: 1, padding: 14, borderRadius: 10, cursor: 'pointer', border: `2px solid ${form.vaiTro === v.val ? 'var(--primary)' : 'var(--border)'}`,
                    background: form.vaiTro === v.val ? 'var(--primary-light)' : 'var(--white)',
                  }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
                    <i className={`ti ${v.icon}`} style={{ color: 'var(--primary)', fontSize: 18 }} aria-hidden="true"></i>
                    <strong style={{ fontSize: 13 }}>{v.nhan}</strong>
                  </div>
                  <p style={{ fontSize: 11, color: 'var(--text-3)', margin: 0, lineHeight: 1.4 }}>{v.mota}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Họ tên *</label>
            <input className="form-input" name="hoTen" value={form.hoTen} onChange={capNhat}
              placeholder="Nguyễn Văn A" />
          </div>

          <div className="form-group">
            <label className="form-label">Email *</label>
            <input className="form-input" name="email" type="email" value={form.email} onChange={capNhat}
              placeholder="admin@example.com" />
          </div>

          <div className="form-group">
            <label className="form-label">Mật khẩu tạm thời *</label>
            <input className="form-input" name="matKhau" type="password" value={form.matKhau} onChange={capNhat}
              placeholder="Tối thiểu 8 ký tự" />
            <p style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 4 }}>
              Thông báo cho nhân viên đổi mật khẩu ngay sau lần đăng nhập đầu tiên.
            </p>
          </div>

          <div className="btn-row">
            <button className="btn btn-primary" onClick={xuLyGui} disabled={dangGui}>
              {dangGui ? 'Đang tạo...' : 'Tạo tài khoản'}
            </button>
            <button className="btn btn-secondary" onClick={() => navigate('/admin')}>Huỷ</button>
          </div>
        </div>
      </div>
    </DashboardShell>
  );
}
