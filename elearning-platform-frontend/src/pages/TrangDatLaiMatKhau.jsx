import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { datLaiMatKhau } from '../services/xacThucService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

export default function TrangDatLaiMatKhau() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const [matKhauMoi, setMatKhauMoi] = useState('');
  const [xacNhan, setXacNhan] = useState('');
  const [dangGui, setDangGui] = useState(false);
  const [thanhCong, setThanhCong] = useState(false);
  const [loi, setLoi] = useState('');

  async function xuLySubmit(e) {
    e.preventDefault();
    setLoi('');

    if (matKhauMoi !== xacNhan) {
      setLoi('Xác nhận mật khẩu mới không khớp');
      return;
    }
    if (matKhauMoi.length < 8) {
      setLoi('Mật khẩu mới phải có ít nhất 8 ký tự');
      return;
    }

    setDangGui(true);
    try {
      await datLaiMatKhau({ token, matKhauMoi, xacNhanMatKhauMoi: xacNhan });
      setThanhCong(true);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  }

  if (!token) {
    return (
      <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', padding: 30 }}>
        <div style={{ width: '100%', maxWidth: 380, textAlign: 'center' }}>
          <div className="alert alert-danger" style={{ textAlign: 'left' }}>
            <i className="ti ti-alert-circle" aria-hidden="true"></i>
            Liên kết đặt lại mật khẩu không hợp lệ — thiếu token.
          </div>
          <Link to="/quen-mat-khau" className="btn btn-primary" style={{ width: '100%', height: 40 }}>
            Yêu cầu liên kết mới
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', padding: 30 }}>
      <div style={{ width: '100%', maxWidth: 380 }}>
        <div style={{ textAlign: 'center', fontSize: 28, color: 'var(--primary)', marginBottom: 16 }}>
          <i className="ti ti-lock-check" aria-hidden="true"></i>
        </div>
        <h2 style={{ fontSize: 20, fontWeight: 500, textAlign: 'center', marginBottom: 20 }}>Đặt lại mật khẩu</h2>

        {!thanhCong ? (
          <>
            {loi && <div className="alert alert-danger">{loi}</div>}
            <form onSubmit={xuLySubmit}>
              <div className="form-group">
                <label className="form-label">Mật khẩu mới</label>
                <input className="form-input" type="password" value={matKhauMoi}
                  onChange={e => setMatKhauMoi(e.target.value)} required minLength={8} autoFocus />
              </div>
              <div className="form-group">
                <label className="form-label">Xác nhận mật khẩu mới</label>
                <input className="form-input" type="password" value={xacNhan}
                  onChange={e => setXacNhan(e.target.value)} required minLength={8} />
              </div>
              <button className="btn btn-primary" type="submit" disabled={dangGui}
                style={{ width: '100%', height: 40, marginTop: 8 }}>
                {dangGui ? 'Đang cập nhật...' : 'Đặt lại mật khẩu'}
              </button>
            </form>
          </>
        ) : (
          <>
            <div className="alert alert-success" style={{ textAlign: 'left' }}>
              <i className="ti ti-circle-check" aria-hidden="true"></i>
              Đặt lại mật khẩu thành công. Mọi phiên đăng nhập cũ đã bị đăng xuất, vui lòng đăng nhập lại.
            </div>
            <button className="btn btn-primary" style={{ width: '100%', height: 40 }}
              onClick={() => navigate('/dang-nhap')}>
              Đến trang đăng nhập
            </button>
          </>
        )}
      </div>
    </div>
  );
}