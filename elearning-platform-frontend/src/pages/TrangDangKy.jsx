import { useState } from 'react';
import { Link } from 'react-router-dom';
import { dangKy, guiLaiMaKichHoat } from '../services/xacThucService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

export default function TrangDangKy() {
  const [hoTen, setHoTen] = useState('');
  const [email, setEmail] = useState('');
  const [sdt, setSdt] = useState('');
  const [matKhau, setMatKhau] = useState('');
  const [xacNhanMatKhau, setXacNhanMatKhau] = useState('');
  const [dangXuLy, setDangXuLy] = useState(false);
  const [loi, setLoi] = useState('');
  const [thanhCong, setThanhCong] = useState(false);
  const [dangGuiLai, setDangGuiLai] = useState(false);
  const [thongBaoGuiLai, setThongBaoGuiLai] = useState('');

  async function xuLyGuiLai() {
    setDangGuiLai(true);
    setThongBaoGuiLai('');
    try {
      await guiLaiMaKichHoat(email);
      setThongBaoGuiLai('Đã gửi lại email kích hoạt. Vui lòng kiểm tra hộp thư.');
    } catch (err) {
      setThongBaoGuiLai(layThongBaoLoi(err));
    } finally {
      setDangGuiLai(false);
    }
  }

  async function xuLySubmit(e) {
    e.preventDefault();
    setLoi('');

    if (matKhau !== xacNhanMatKhau) {
      setLoi('Mật khẩu xác nhận không khớp.');
      return;
    }

    setDangXuLy(true);

    try {
      await dangKy({ hoTen, email, matKhau, sdt });
      setThanhCong(true);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  }

  return (
    <div style={{ display: 'flex', height: '100vh', minHeight: 600 }}>
      <div
        style={{
          flex: 1,
          overflowY: 'auto',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: 30,
        }}
      >
        <div style={{ width: '100%', maxWidth: 380 }}>
          {thanhCong ? (
            <>
              <h2 style={{ fontSize: 20, fontWeight: 500, marginBottom: 4 }}>Đăng ký thành công</h2>
              <p style={{ fontSize: 13, color: 'var(--text-3)', marginBottom: 20 }}>
                Một email kích hoạt đã được gửi đến <strong>{email}</strong>.
              </p>
              <div className="alert alert-success">
                <i className="ti ti-circle-check" aria-hidden="true"></i>
                Email kích hoạt đã được gửi! Kiểm tra hộp thư của bạn.
              </div>

              {thongBaoGuiLai && (
                <p style={{ fontSize: 12, color: 'var(--text-2)', marginTop: 10 }}>{thongBaoGuiLai}</p>
              )}

              <button
                type="button"
                className="btn btn-secondary"
                onClick={xuLyGuiLai}
                disabled={dangGuiLai}
                style={{ width: '100%', height: 36, fontSize: 13, marginTop: 10 }}
              >
                {dangGuiLai ? 'Đang gửi...' : 'Không nhận được email? Gửi lại'}
              </button>

              <p style={{ textAlign: 'center', fontSize: 12, color: 'var(--text-3)', marginTop: 14 }}>
                <Link to="/dang-nhap">Quay lại trang đăng nhập</Link>
              </p>
            </>
          ) : (
            <>
              <h2 style={{ fontSize: 20, fontWeight: 500, marginBottom: 4 }}>Tạo tài khoản mới</h2>
              <Link
                to="/trang-chu"
                className="btn btn-secondary"
                style={{ marginBottom: 16, width: '100%', justifyContent: 'center' }}
              >
                Về trang chủ
              </Link>
              <p style={{ fontSize: 13, color: 'var(--text-3)', marginBottom: 20 }}>
                Điền đầy đủ thông tin để bắt đầu học tập
              </p>

              <form onSubmit={xuLySubmit} autoComplete="off">
                <div className="form-group">
                  <label className="form-label">Họ và tên *</label>
                  <input
                    className="form-input"
                    value={hoTen}
                    onChange={(e) => setHoTen(e.target.value)}
                    autoComplete="name"
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Địa chỉ email *</label>
                  <input
                    className="form-input"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    // "off" thay vì "email": nếu để "email", trình duyệt vẫn có xu
                    // hướng gợi ý/điền lại email đã lưu từ lần đăng nhập gần nhất —
                    // đây chính là nguồn gốc của lỗi báo cáo (dính email tài khoản
                    // cũ dù đã đăng xuất). Trang đăng ký là tạo tài khoản MỚI, không
                    // liên quan tới thông tin đăng nhập đã lưu trước đó.
                    autoComplete="off"
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Số điện thoại</label>
                  <input
                    className="form-input"
                    type="tel"
                    value={sdt}
                    onChange={(e) => setSdt(e.target.value)}
                    pattern="^$|^0[0-9]{9}$"
                    title="Số điện thoại phải có 10 số và bắt đầu bằng 0"
                    autoComplete="off"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Mật khẩu *</label>
                  <input
                    className="form-input"
                    type="password"
                    placeholder="Tối thiểu 8 ký tự"
                    value={matKhau}
                    onChange={(e) => setMatKhau(e.target.value)}
                    // "new-password" là giá trị chuẩn để báo trình duyệt/trình quản
                    // lý mật khẩu KHÔNG tự điền mật khẩu đã lưu của tài khoản trước
                    // vào đây — đây là ô đặt mật khẩu cho tài khoản mới.
                    autoComplete="new-password"
                    required
                    minLength={8}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Xác nhận mật khẩu *</label>
                  <input
                    className="form-input"
                    type="password"
                    placeholder="Nhập lại mật khẩu"
                    value={xacNhanMatKhau}
                    onChange={(e) => setXacNhanMatKhau(e.target.value)}
                    autoComplete="new-password"
                    required
                    minLength={8}
                  />
                </div>

                {loi && (
                  <div className="alert alert-danger">
                    <i className="ti ti-alert-circle" aria-hidden="true"></i>
                    {loi}
                  </div>
                )}

                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={dangXuLy}
                  style={{ width: '100%', height: 40, fontSize: 14 }}
                >
                  {dangXuLy ? 'Đang xử lý...' : 'Đăng ký'}
                </button>
              </form>

              <p style={{ textAlign: 'center', fontSize: 12, color: 'var(--text-3)', marginTop: 14 }}>
                Đã có tài khoản? <Link to="/dang-nhap">Đăng nhập</Link>
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
}