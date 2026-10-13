import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { kichHoatTaiKhoan, guiLaiMaKichHoat } from '../services/xacThucService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

export default function TrangKichHoat() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [trangThai, setTrangThai] = useState('dang-xu-ly');
  const [thongBaoLoi, setThongBaoLoi] = useState('');

  // Form gửi lại mã kích hoạt — hiện khi kích hoạt thất bại (link hết hạn
  // hoặc đã dùng rồi), người dùng thường không nhớ đã bấm link cũ ở đâu.
  const [emailGuiLai, setEmailGuiLai] = useState('');
  const [dangGuiLai, setDangGuiLai] = useState(false);
  const [thongBaoGuiLai, setThongBaoGuiLai] = useState('');

  // Chặn gọi API kích hoạt 2 lần: React 18 StrictMode (dev only) chạy
  // useEffect 2 lần liên tiếp để phát hiện side-effect không idempotent.
  // Vì API kích hoạt KHÔNG idempotent (token chỉ dùng được 1 lần, lần
  // gọi thứ 2 sẽ luôn báo "đã hết hạn" dù lần 1 đã thành công), cần
  // dùng ref để đảm bảo logic bên trong chỉ thực thi đúng 1 lần thực sự,
  // bất kể effect được React gọi bao nhiêu lần.
  const daGoiApi = useRef(false);

  useEffect(() => {
    if (daGoiApi.current) {
      return;
    }
    daGoiApi.current = true;

    if (!token) {
      setTrangThai('loi');
      setThongBaoLoi('Liên kết kích hoạt không hợp lệ — thiếu token.');
      return;
    }

    kichHoatTaiKhoan(token)
      .then(() => setTrangThai('thanh-cong'))
      .catch((err) => {
        setTrangThai('loi');
        setThongBaoLoi(layThongBaoLoi(err));
      });
  }, [token]);

  async function xuLyGuiLai(e) {
    e.preventDefault();
    setDangGuiLai(true);
    setThongBaoGuiLai('');
    try {
      await guiLaiMaKichHoat(emailGuiLai);
      setThongBaoGuiLai('Nếu email hợp lệ, một liên kết kích hoạt mới đã được gửi. Vui lòng kiểm tra hộp thư.');
    } catch (err) {
      setThongBaoGuiLai(layThongBaoLoi(err));
    } finally {
      setDangGuiLai(false);
    }
  }

  return (
    <div
      style={{
        display: 'flex',
        height: '100vh',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 30,
      }}
    >
      <div style={{ width: '100%', maxWidth: 380, textAlign: 'center' }}>
        <div style={{ fontSize: 28, color: 'var(--primary)', marginBottom: 16 }}>
          <i className="ti ti-school" aria-hidden="true"></i>
        </div>

        {trangThai === 'dang-xu-ly' && <p style={{ color: 'var(--text-3)' }}>Đang kích hoạt tài khoản...</p>}

        {trangThai === 'thanh-cong' && (
          <>
            <h2 style={{ fontSize: 20, fontWeight: 500, marginBottom: 12 }}>Kích hoạt thành công</h2>
            <div className="alert alert-success" style={{ textAlign: 'left' }}>
              <i className="ti ti-circle-check" aria-hidden="true"></i>
              Tài khoản của bạn đã được kích hoạt. Bạn có thể đăng nhập ngay bây giờ.
            </div>
            <Link to="/dang-nhap" className="btn btn-primary" style={{ width: '100%', height: 40 }}>
              Đến trang đăng nhập
            </Link>
          </>
        )}

        {trangThai === 'loi' && (
          <>
            <h2 style={{ fontSize: 20, fontWeight: 500, marginBottom: 12 }}>Kích hoạt không thành công</h2>
            <div className="alert alert-danger" style={{ textAlign: 'left' }}>
              <i className="ti ti-alert-circle" aria-hidden="true"></i>
              {thongBaoLoi}
            </div>

            <form onSubmit={xuLyGuiLai} style={{ textAlign: 'left', marginTop: 16 }}>
              <label className="form-label" style={{ fontSize: 12 }}>
                Nhập lại email để gửi liên kết kích hoạt mới
              </label>
              <input
                className="form-input"
                type="email"
                placeholder="example@email.com"
                value={emailGuiLai}
                onChange={(e) => setEmailGuiLai(e.target.value)}
                required
                style={{ marginBottom: 8 }}
              />
              {thongBaoGuiLai && (
                <p style={{ fontSize: 12, color: 'var(--text-2)', marginBottom: 8 }}>{thongBaoGuiLai}</p>
              )}
              <button
                type="submit"
                className="btn btn-secondary"
                disabled={dangGuiLai}
                style={{ width: '100%', height: 36, fontSize: 13 }}
              >
                {dangGuiLai ? 'Đang gửi...' : 'Gửi lại email kích hoạt'}
              </button>
            </form>

            <Link to="/dang-ky" className="btn btn-primary" style={{ width: '100%', height: 40, marginTop: 10 }}>
              Đăng ký lại
            </Link>
          </>
        )}
      </div>
    </div>
  );
}