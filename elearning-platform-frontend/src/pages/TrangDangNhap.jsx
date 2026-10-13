import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { dangNhap } from '../services/xacThucService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

export default function TrangDangNhap() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [email, setEmail] = useState('');
  const [matKhau, setMatKhau] = useState('');
  const [dangXuLy, setDangXuLy] = useState(false);
  const [loi, setLoi] = useState('');

  async function xuLySubmit(e) {
    e.preventDefault();
    setLoi('');
    setDangXuLy(true);

    try {
      const ketQua = await dangNhap({ email, matKhau });
      // FIX (Vấn đề #2): accessToken/refreshToken không còn trong ketQua nữa
      // (đã chuyển sang cookie HttpOnly, xem xacThucService.dangNhap) nên
      // không có gì để lưu vào localStorage cho 2 giá trị đó. "daDangNhap"
      // chỉ là CỜ HIỂN THỊ (không phải bí mật) để các trang biết cần hỏi API
      // hay hiện thẳng UI khách — cookie thật mới là thứ backend kiểm tra.
      localStorage.setItem('daDangNhap', '1');
      localStorage.setItem('hoTen', ketQua.hoTen);
      localStorage.setItem('vaiTro', ketQua.vaiTro);
      localStorage.setItem('maNguoiDung', ketQua.maNguoiDung);

      // FIX: hỗ trợ quay lại đúng trang đang dang dở trước khi bị yêu cầu
      // đăng nhập (VD: khách bấm "Tham gia lớp học" từ trang tổng quan lớp
      // công khai) — chỉ dùng "tiep" khi có, chỉ chấp nhận đường dẫn NỘI BỘ
      // (bắt đầu bằng "/") để tránh open-redirect nếu ai đó chèn URL ngoài
      // vào query string.
      const tiep = searchParams.get('tiep');
      if (tiep && tiep.startsWith('/')) {
        navigate(tiep);
        return;
      }

      const vt = ketQua.vaiTro;
      if (vt === 'QTV_CAO_CAP')        navigate('/admin');
      else if (vt === 'QTV_NOI_DUNG')  navigate('/admin/noi-dung');
      else if (vt === 'QTV_TAI_CHINH') navigate('/admin/tai-chinh');
      else                             navigate('/trang-chu');
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  }

  return (
    <div
      style={{
        display: 'flex',
        flex: 1,
        height: '100vh',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 30,
      }}
    >
      <div style={{ width: '100%', maxWidth: 380 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <div style={{ fontSize: 28, color: 'var(--primary)', marginBottom: 6 }}>
            <i className="ti ti-school" aria-hidden="true"></i>
          </div>
          <h2 style={{ fontSize: 20, fontWeight: 500 }}>Đăng nhập hệ thống</h2>
          <Link
            to="/trang-chu"
            className="btn btn-secondary"
            style={{ marginTop: 12, width: '100%', justifyContent: 'center' }}
          >
            Về trang chủ
          </Link>
        </div>

        {loi && (
          <div className="alert alert-danger">
            <i className="ti ti-lock-x" aria-hidden="true"></i>
            {loi}
          </div>
        )}

        <form onSubmit={xuLySubmit}>
          <div className="form-group">
            <label className="form-label">Địa chỉ email</label>
            <input
              className="form-input"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Mật khẩu</label>
            <input
              className="form-input"
              type="password"
              value={matKhau}
              onChange={(e) => setMatKhau(e.target.value)}
              required
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 14 }}>
            <Link to="/quen-mat-khau" style={{ fontSize: 12 }}>
              Quên mật khẩu?
            </Link>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={dangXuLy}
            style={{ width: '100%', height: 40, fontSize: 14 }}
          >
            {dangXuLy ? 'Đang xử lý...' : 'Đăng nhập'}
          </button>
        </form>

        <p style={{ textAlign: 'center', fontSize: 12, color: 'var(--text-3)', marginTop: 14 }}>
          Chưa có tài khoản? <Link to="/dang-ky">Đăng ký</Link>
        </p>
      </div>
    </div>
  );
}