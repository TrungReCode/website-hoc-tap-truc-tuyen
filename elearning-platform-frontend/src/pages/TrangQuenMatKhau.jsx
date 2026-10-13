import { useState } from 'react';
import { Link } from 'react-router-dom';
import { quenMatKhau } from '../services/xacThucService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

export default function TrangQuenMatKhau() {
  const [email, setEmail] = useState('');
  const [dangGui, setDangGui] = useState(false);
  const [daGuiXong, setDaGuiXong] = useState(false);
  const [loi, setLoi] = useState('');

  async function xuLySubmit(e) {
    e.preventDefault();
    setLoi('');
    setDangGui(true);
    try {
      await quenMatKhau(email.trim());
      // Luôn hiện thông báo thành công bất kể email có tồn tại hay không —
      // khớp với hành vi backend (không lộ thông tin email đã đăng ký).
      setDaGuiXong(true);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  }

  return (
    <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', padding: 30 }}>
      <div style={{ width: '100%', maxWidth: 380 }}>
        <div style={{ textAlign: 'center', fontSize: 28, color: 'var(--primary)', marginBottom: 16 }}>
          <i className="ti ti-lock-question" aria-hidden="true"></i>
        </div>
        <h2 style={{ fontSize: 20, fontWeight: 500, textAlign: 'center', marginBottom: 8 }}>Quên mật khẩu</h2>

        {!daGuiXong ? (
          <>
            <p style={{ fontSize: 13, color: 'var(--text-3)', textAlign: 'center', marginBottom: 20 }}>
              Nhập email đã đăng ký, chúng tôi sẽ gửi liên kết đặt lại mật khẩu.
            </p>
            {loi && <div className="alert alert-danger">{loi}</div>}
            <form onSubmit={xuLySubmit}>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input className="form-input" type="email" value={email}
                  onChange={e => setEmail(e.target.value)} required autoFocus placeholder="ban@vidu.com" />
              </div>
              <button className="btn btn-primary" type="submit" disabled={dangGui}
                style={{ width: '100%', height: 40, marginTop: 8 }}>
                {dangGui ? 'Đang gửi...' : 'Gửi liên kết đặt lại mật khẩu'}
              </button>
            </form>
          </>
        ) : (
          <div className="alert alert-success" style={{ textAlign: 'left' }}>
            <i className="ti ti-mail-check" aria-hidden="true"></i>
            {' '}Nếu email <strong>{email}</strong> tồn tại trong hệ thống, một liên kết đặt lại mật khẩu đã được gửi tới.
            Liên kết có hiệu lực trong 15 phút.
          </div>
        )}

        <div style={{ textAlign: 'center', marginTop: 16, fontSize: 13 }}>
          <Link to="/dang-nhap">Quay lại đăng nhập</Link>
        </div>
      </div>
    </div>
  );
}