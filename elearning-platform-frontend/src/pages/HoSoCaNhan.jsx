import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  layHoSo, capNhatHoSo, doiMatKhau, taiLenAnhDaiDien,
  layTaiKhoanNganHang, themTaiKhoanNganHang, capNhatTaiKhoanNganHang, xoaTaiKhoanNganHang,
  datLamMacDinhTaiKhoanNganHang,
} from '../services/hoSoService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';
import ThanhTienDoTaiLen from '../components/ThanhTienDoTaiLen';

export default function HoSoCaNhan() {
  const navigate = useNavigate();
  const vaiTro = localStorage.getItem('vaiTro');
  // Lưu ý: "Giảng viên" KHÔNG phải là một VaiTro riêng trong CSDL (chỉ có
  // QTV_CAO_CAP/QTV_NOI_DUNG/QTV_TAI_CHINH/NGUOI_DUNG) — bất kỳ NGUOI_DUNG
  // nào cũng có thể trở thành giảng viên bằng cách tạo khóa học, nên hiện
  // mục tài khoản ngân hàng cho mọi tài khoản NGUOI_DUNG (không phải QTV_*).
  const coTheLaGiangVien = vaiTro === 'NGUOI_DUNG';

  const [hoSo, setHoSo] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    let daHuy = false;
    layHoSo()
      .then(h => { if (!daHuy) setHoSo(h); })
      .catch(err => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
  }, []);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-user-circle" aria-hidden="true"></i> Hồ sơ cá nhân</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
      </div>

      <div style={{ padding: 20, maxWidth: 640, width: '100%', margin: '0 auto' }}>
        {loi && <div className="alert alert-danger">{loi}</div>}
        {dangTai && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải hồ sơ...</p>}

        {hoSo && (
          <>
            <ThongTinCoBan hoSo={hoSo} onCapNhat={setHoSo} />
            {coTheLaGiangVien && <LienKetXacThucGiangVien />}
            <DoiMatKhau />
            {coTheLaGiangVien && <TaiKhoanNganHang />}
          </>
        )}
      </div>
    </div>
  );
}

/* ══════════════════════ Thông tin cơ bản ══════════════════════ */

function ThongTinCoBan({ hoSo, onCapNhat }) {
  const [hoTen, setHoTen] = useState(hoSo.hoTen || '');
  const [sdt, setSdt] = useState(hoSo.sdt || '');
  const [tieuSu, setTieuSu] = useState(hoSo.tieuSu || '');
  const [anhDaiDien, setAnhDaiDien] = useState(hoSo.anhDaiDien || '');
  const [dangTaiAnh, setDangTaiAnh] = useState(false);
  const [tienDoAnh, setTienDoAnh] = useState(null);
  const [dangLuu, setDangLuu] = useState(false);
  const [loi, setLoi] = useState('');
  const [thanhCong, setThanhCong] = useState(false);
  const fileInputRef = useRef(null);

  async function xuLyChonAnh(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setDangTaiAnh(true);
    setTienDoAnh(0);
    setLoi('');
    try {
      const url = await taiLenAnhDaiDien(file, setTienDoAnh);
      setAnhDaiDien(url);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangTaiAnh(false);
      setTienDoAnh(null);
    }
  }

  async function xuLyLuu(e) {
    e.preventDefault();
    setDangLuu(true);
    setLoi('');
    setThanhCong(false);
    try {
      const capNhat = await capNhatHoSo({ hoTen: hoTen.trim(), sdt: sdt.trim(), anhDaiDien, tieuSu: tieuSu.trim() });
      onCapNhat(capNhat);
      localStorage.setItem('hoTen', capNhat.hoTen);
      setThanhCong(true);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangLuu(false);
    }
  }

  return (
    <div className="card" style={{ marginBottom: 20 }}>
      <div className="card-title"><i className="ti ti-id-badge-2" aria-hidden="true"></i> Thông tin cơ bản</div>
      {loi && <div className="alert alert-danger">{loi}</div>}
      {thanhCong && <div className="alert alert-success">Đã cập nhật hồ sơ thành công.</div>}

      <form onSubmit={xuLyLuu}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 18 }}>
          {anhDaiDien ? (
            <img src={anhDaiDien} alt="Ảnh đại diện"
              style={{ width: 72, height: 72, borderRadius: '50%', objectFit: 'cover' }} />
          ) : (
            <div className="avatar" style={{ width: 72, height: 72, fontSize: 26 }}>
              {(hoTen || '?').charAt(0).toUpperCase()}
            </div>
          )}
          <div>
            <input ref={fileInputRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={xuLyChonAnh} />
            <button type="button" className="btn btn-secondary btn-sm" disabled={dangTaiAnh}
              onClick={() => fileInputRef.current?.click()}>
              {dangTaiAnh ? 'Đang tải ảnh...' : 'Đổi ảnh đại diện'}
            </button>
            {dangTaiAnh && (
              <div style={{ maxWidth: 200 }}>
                <ThanhTienDoTaiLen phanTram={tienDoAnh} nhan={tienDoAnh == null ? 'Đang xử lý...' : 'Đang tải lên...'} />
              </div>
            )}
            <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 6 }}>JPG, PNG hoặc WebP</div>
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Họ và tên</label>
          <input className="form-input" value={hoTen} onChange={e => setHoTen(e.target.value)} required maxLength={100} />
        </div>

        <div className="form-group">
          <label className="form-label">Email</label>
          <input className="form-input" value={hoSo.email} disabled style={{ background: 'var(--surface)', color: 'var(--text-3)' }} />
        </div>

        <div className="form-group">
          <label className="form-label">Số điện thoại</label>
          <input className="form-input" value={sdt} onChange={e => setSdt(e.target.value)} maxLength={15} placeholder="Chưa cập nhật" />
        </div>

        <div className="form-group">
          <label className="form-label">Giới thiệu bản thân</label>
          <textarea className="form-input" style={{ height: 'auto', padding: '8px 10px', resize: 'vertical' }}
            rows={3} maxLength={1000} value={tieuSu} onChange={e => setTieuSu(e.target.value)}
            placeholder="Đôi nét về bạn..." />
        </div>

        <button className="btn btn-primary" type="submit" disabled={dangLuu}>
          {dangLuu ? 'Đang lưu...' : 'Lưu thay đổi'}
        </button>
      </form>
    </div>
  );
}

function LienKetXacThucGiangVien() {
  const navigate = useNavigate();

  return (
    <div className="card" style={{ marginBottom: 20 }}>
      <div className="card-title"><i className="ti ti-certificate" aria-hidden="true"></i> Trở thành giảng viên</div>
      <p style={{ fontSize: 12.5, color: 'var(--text-3)', marginBottom: 12 }}>
        Gửi bằng cấp hoặc chứng chỉ chuyên môn để được xét duyệt quyền tạo khóa học.
      </p>
      <button className="btn btn-primary" type="button" onClick={() => navigate('/xac-thuc-giang-vien')}>
        Xem hồ sơ xác thực
      </button>
    </div>
  );
}

/* ══════════════════════ Đổi mật khẩu ══════════════════════ */

function DoiMatKhau() {
  const [matKhauHienTai, setMatKhauHienTai] = useState('');
  const [matKhauMoi, setMatKhauMoi] = useState('');
  const [xacNhan, setXacNhan] = useState('');
  const [dangLuu, setDangLuu] = useState(false);
  const [loi, setLoi] = useState('');
  const [thanhCong, setThanhCong] = useState(false);

  async function xuLySubmit(e) {
    e.preventDefault();
    setLoi('');
    setThanhCong(false);
    if (matKhauMoi !== xacNhan) {
      setLoi('Xác nhận mật khẩu mới không khớp');
      return;
    }
    if (matKhauMoi.length < 8) {
      setLoi('Mật khẩu mới phải có ít nhất 8 ký tự');
      return;
    }
    setDangLuu(true);
    try {
      await doiMatKhau({ matKhauHienTai, matKhauMoi, xacNhanMatKhauMoi: xacNhan });
      setThanhCong(true);
      setMatKhauHienTai(''); setMatKhauMoi(''); setXacNhan('');
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangLuu(false);
    }
  }

  return (
    <div className="card" style={{ marginBottom: 20 }}>
      <div className="card-title"><i className="ti ti-lock" aria-hidden="true"></i> Đổi mật khẩu</div>
      {loi && <div className="alert alert-danger">{loi}</div>}
      {thanhCong && <div className="alert alert-success">Đổi mật khẩu thành công.</div>}

      <form onSubmit={xuLySubmit}>
        <div className="form-group">
          <label className="form-label">Mật khẩu hiện tại</label>
          <input className="form-input" type="password" value={matKhauHienTai}
            onChange={e => setMatKhauHienTai(e.target.value)} required />
        </div>
        <div className="form-group">
          <label className="form-label">Mật khẩu mới</label>
          <input className="form-input" type="password" value={matKhauMoi}
            onChange={e => setMatKhauMoi(e.target.value)} required minLength={8} />
        </div>
        <div className="form-group">
          <label className="form-label">Xác nhận mật khẩu mới</label>
          <input className="form-input" type="password" value={xacNhan}
            onChange={e => setXacNhan(e.target.value)} required minLength={8} />
        </div>
        <button className="btn btn-primary" type="submit" disabled={dangLuu}>
          {dangLuu ? 'Đang cập nhật...' : 'Đổi mật khẩu'}
        </button>
      </form>
    </div>
  );
}

/* ══════════════════════ Tài khoản ngân hàng (Giảng viên) ══════════════════════ */

function TaiKhoanNganHang() {
  const [danhSach, setDanhSach] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [hienForm, setHienForm] = useState(false);
  const [dangSua, setDangSua] = useState(null); // maTaiKhoan đang sửa, null = thêm mới
  const [tenChuTaiKhoan, setTenChuTaiKhoan] = useState('');
  const [soTaiKhoan, setSoTaiKhoan] = useState('');
  const [tenNganHang, setTenNganHang] = useState('');
  const [dangLuu, setDangLuu] = useState(false);

  async function taiDanhSach() {
    setDangTai(true);
    try {
      setDanhSach(await layTaiKhoanNganHang());
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangTai(false);
    }
  }

  useEffect(() => { taiDanhSach(); }, []);

  function moFormThemMoi() {
    setDangSua(null);
    setTenChuTaiKhoan(''); setSoTaiKhoan(''); setTenNganHang('');
    setHienForm(true);
  }

  function moFormSua(tk) {
    setDangSua(tk.maTaiKhoan);
    setTenChuTaiKhoan(tk.tenChuTaiKhoan); setSoTaiKhoan(tk.soTaiKhoan); setTenNganHang(tk.tenNganHang);
    setHienForm(true);
  }

  async function xuLyLuu(e) {
    e.preventDefault();
    setDangLuu(true);
    setLoi('');
    try {
      const yeuCau = { tenChuTaiKhoan: tenChuTaiKhoan.trim(), soTaiKhoan: soTaiKhoan.trim(), tenNganHang: tenNganHang.trim() };
      if (dangSua) {
        await capNhatTaiKhoanNganHang(dangSua, yeuCau);
      } else {
        await themTaiKhoanNganHang(yeuCau);
      }
      setHienForm(false);
      await taiDanhSach();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangLuu(false);
    }
  }

  async function xuLyXoa(maTaiKhoan) {
    if (!window.confirm('Xóa tài khoản ngân hàng này?')) return;
    try {
      await xoaTaiKhoanNganHang(maTaiKhoan);
      await taiDanhSach();
    } catch (err) {
      alert(layThongBaoLoi(err));
    }
  }

  async function xuLyDatMacDinh(maTaiKhoan) {
    try {
      await datLamMacDinhTaiKhoanNganHang(maTaiKhoan);
      await taiDanhSach();
    } catch (err) {
      alert(layThongBaoLoi(err));
    }
  }

  return (
    <div className="card">
      <div className="card-title">
        <i className="ti ti-building-bank" aria-hidden="true"></i> Tài khoản ngân hàng nhận thanh toán
      </div>
      <p style={{ fontSize: 12.5, color: 'var(--text-3)', marginTop: -6, marginBottom: 14 }}>
        Dùng để hệ thống đối soát và chuyển doanh thu từ khóa học/lớp học của bạn. Tài khoản mặc định sẽ được dùng để nhận tiền đối soát.
      </p>

      {loi && <div className="alert alert-danger">{loi}</div>}
      {dangTai && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>}

      {!dangTai && danhSach.length === 0 && !hienForm && (
        <div className="empty-state" style={{ marginBottom: 14 }}>
          <i className="ti ti-building-bank" style={{ fontSize: 26 }} aria-hidden="true"></i>
          <p style={{ marginTop: 6, fontSize: 13 }}>Bạn chưa khai báo tài khoản ngân hàng nào.</p>
        </div>
      )}

      {danhSach.map(tk => (
        <div key={tk.maTaiKhoan} className="lh-lesson" style={{ cursor: 'default', marginBottom: 8 }}>
          <div className="ll-icon"><i className="ti ti-building-bank" aria-hidden="true"></i></div>
          <div className="ll-title">
            <div style={{ fontWeight: 600 }}>{tk.tenNganHang} · {tk.soTaiKhoan}</div>
            <div style={{ fontSize: 11.5, color: 'var(--text-3)' }}>{tk.tenChuTaiKhoan}</div>
          </div>
          <div className="ll-tags">
            {tk.macDinh
              ? <span className="badge badge-blue">Mặc định</span>
              : <button className="btn btn-secondary btn-sm" onClick={() => xuLyDatMacDinh(tk.maTaiKhoan)}>Đặt làm mặc định</button>}
            <button className="btn btn-secondary btn-sm" onClick={() => moFormSua(tk)}>Sửa</button>
            <button className="btn btn-danger btn-sm" onClick={() => xuLyXoa(tk.maTaiKhoan)}>Xóa</button>
          </div>
        </div>
      ))}

      {!hienForm && (
        <button className="btn btn-secondary btn-sm" onClick={moFormThemMoi}>
          <i className="ti ti-plus" aria-hidden="true"></i> Thêm tài khoản ngân hàng
        </button>
      )}

      {hienForm && (
        <form onSubmit={xuLyLuu} style={{ marginTop: 12, padding: 14, background: 'var(--surface)', borderRadius: 10 }}>
          <div className="form-group">
            <label className="form-label">Tên chủ tài khoản</label>
            <input className="form-input" value={tenChuTaiKhoan} onChange={e => setTenChuTaiKhoan(e.target.value)}
              required maxLength={100} placeholder="NGUYEN VAN A" />
          </div>
          <div className="form-group">
            <label className="form-label">Số tài khoản</label>
            <input className="form-input" value={soTaiKhoan} onChange={e => setSoTaiKhoan(e.target.value)}
              required maxLength={30} />
          </div>
          <div className="form-group">
            <label className="form-label">Ngân hàng</label>
            <input className="form-input" value={tenNganHang} onChange={e => setTenNganHang(e.target.value)}
              required maxLength={100} placeholder="Vietcombank, Techcombank..." />
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-primary btn-sm" type="submit" disabled={dangLuu}>
              {dangLuu ? 'Đang lưu...' : 'Lưu'}
            </button>
            <button className="btn btn-secondary btn-sm" type="button" onClick={() => setHienForm(false)}>Hủy</button>
          </div>
        </form>
      )}
    </div>
  );
}