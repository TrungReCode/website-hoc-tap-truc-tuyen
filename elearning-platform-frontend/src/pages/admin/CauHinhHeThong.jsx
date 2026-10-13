import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layCauHinhHeThong, capNhatCauHinhHeThong } from '../../services/adminService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { NAV_ADMIN_CAO_CAP } from '../../components/DashboardShell';

export default function CauHinhHeThong() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    tyLePhiCaNhan: '', tyLePhiDayHoc: '', soNgayGiuKyQuyKhoaHoc: '', soNgayGiuKyQuyLopHoc: '',
  });
  const [banDau, setBanDau] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [dangLuu, setDangLuu] = useState(false);
  const [loi, setLoi] = useState('');
  const [thanhCong, setThanhCong] = useState(false);

  useEffect(() => {
    layCauHinhHeThong()
      .then(cauHinh => {
        setForm({
          tyLePhiCaNhan: String(cauHinh.tyLePhiCaNhan ?? ''),
          tyLePhiDayHoc: String(cauHinh.tyLePhiDayHoc ?? ''),
          soNgayGiuKyQuyKhoaHoc: String(cauHinh.soNgayGiuKyQuyKhoaHoc ?? ''),
          soNgayGiuKyQuyLopHoc: String(cauHinh.soNgayGiuKyQuyLopHoc ?? ''),
        });
        setBanDau(cauHinh);
      })
      .catch(err => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, []);

  function capNhatTruong(e) {
    setThanhCong(false);
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
  }

  const daThayDoi = banDau && (
    String(banDau.tyLePhiCaNhan) !== form.tyLePhiCaNhan ||
    String(banDau.tyLePhiDayHoc) !== form.tyLePhiDayHoc ||
    String(banDau.soNgayGiuKyQuyKhoaHoc) !== form.soNgayGiuKyQuyKhoaHoc ||
    String(banDau.soNgayGiuKyQuyLopHoc) !== form.soNgayGiuKyQuyLopHoc
  );

  async function xuLyLuu(e) {
    e.preventDefault();
    setLoi(''); setThanhCong(false);

    const tyLeCaNhan = Number(form.tyLePhiCaNhan);
    const tyLeDayHoc = Number(form.tyLePhiDayHoc);
    const soNgayKH = Number(form.soNgayGiuKyQuyKhoaHoc);
    const soNgayLH = Number(form.soNgayGiuKyQuyLopHoc);

    if (form.tyLePhiCaNhan === '' || Number.isNaN(tyLeCaNhan) || tyLeCaNhan < 0 || tyLeCaNhan > 100) {
      setLoi('Tỷ lệ phí Giấy phép cá nhân phải là số từ 0 đến 100'); return;
    }
    if (form.tyLePhiDayHoc === '' || Number.isNaN(tyLeDayHoc) || tyLeDayHoc < 0 || tyLeDayHoc > 100) {
      setLoi('Tỷ lệ phí Giấy phép giảng dạy phải là số từ 0 đến 100'); return;
    }
    if (form.soNgayGiuKyQuyKhoaHoc === '' || !Number.isInteger(soNgayKH) || soNgayKH < 0 || soNgayKH > 365) {
      setLoi('Số ngày giữ ký quỹ khóa học phải là số nguyên từ 0 đến 365'); return;
    }
    if (form.soNgayGiuKyQuyLopHoc === '' || !Number.isInteger(soNgayLH) || soNgayLH < 0 || soNgayLH > 365) {
      setLoi('Số ngày giữ ký quỹ lớp học phải là số nguyên từ 0 đến 365'); return;
    }

    setDangLuu(true);
    try {
      const cap = await capNhatCauHinhHeThong({
        tyLePhiCaNhan: tyLeCaNhan,
        tyLePhiDayHoc: tyLeDayHoc,
        soNgayGiuKyQuyKhoaHoc: soNgayKH,
        soNgayGiuKyQuyLopHoc: soNgayLH,
      });
      setBanDau(cap);
      setForm({
        tyLePhiCaNhan: String(cap.tyLePhiCaNhan),
        tyLePhiDayHoc: String(cap.tyLePhiDayHoc),
        soNgayGiuKyQuyKhoaHoc: String(cap.soNgayGiuKyQuyKhoaHoc),
        soNgayGiuKyQuyLopHoc: String(cap.soNgayGiuKyQuyLopHoc),
      });
      setThanhCong(true);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangLuu(false);
    }
  }

  return (
    <DashboardShell roleLabel="Quản trị viên cấp cao" navItems={NAV_ADMIN_CAO_CAP} crumb="Cấu hình hệ thống">
      <div style={{ maxWidth: 560, margin: '0 auto', width: '100%' }}>
        <p style={{ fontSize: 12, color: 'var(--text-3)', marginBottom: 16 }}>
          Các thông số này áp dụng cho những giao dịch phát sinh <strong>sau</strong> khi
          lưu — không ảnh hưởng ngược tới các giao dịch đã xử lý trước đó.
        </p>

        {dangTai ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
        ) : (
          <div className="card">
            {loi && <div className="alert alert-danger">{loi}</div>}
            {thanhCong && (
              <div className="alert alert-success">
                <i className="ti ti-circle-check" aria-hidden="true"></i> Đã lưu cấu hình mới.
              </div>
            )}

            <div className="form-group">
              <label className="form-label">Tỷ lệ phí dịch vụ — Giấy phép cá nhân (%)</label>
              <input className="form-input" name="tyLePhiCaNhan" type="number" step="0.01" min="0" max="100"
                value={form.tyLePhiCaNhan} onChange={capNhatTruong} />
              <p style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 4 }}>
                Phần trăm nền tảng giữ lại trên giao dịch mua Giấy phép cá nhân (chỉ tự học).
              </p>
            </div>

            <div className="form-group">
              <label className="form-label">Tỷ lệ phí dịch vụ — Giấy phép giảng dạy (%)</label>
              <input className="form-input" name="tyLePhiDayHoc" type="number" step="0.01" min="0" max="100"
                value={form.tyLePhiDayHoc} onChange={capNhatTruong} />
              <p style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 4 }}>
                Áp dụng cho giao dịch mua Giấy phép giảng dạy VÀ cho phí lớp học (lớp học chỉ mở
                được khi giảng viên đã có Giấy phép giảng dạy nên dùng chung tỷ lệ này).
              </p>
            </div>

            <div className="form-group">
              <label className="form-label">Số ngày giữ ký quỹ — Khóa học</label>
              <input className="form-input" name="soNgayGiuKyQuyKhoaHoc" type="number" step="1" min="0" max="365"
                value={form.soNgayGiuKyQuyKhoaHoc} onChange={capNhatTruong} />
              <p style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 4 }}>
                Số ngày kể từ lúc học viên thanh toán trước khi tiền được đưa vào đối soát cho giảng viên.
              </p>
            </div>

            <div className="form-group">
              <label className="form-label">Số ngày giữ ký quỹ — Lớp học</label>
              <input className="form-input" name="soNgayGiuKyQuyLopHoc" type="number" step="1" min="0" max="365"
                value={form.soNgayGiuKyQuyLopHoc} onChange={capNhatTruong} />
              <p style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 4 }}>
                Số ngày kể từ lúc lớp học khai giảng trước khi tiền được đưa vào đối soát cho giảng viên.
              </p>
            </div>

            <div className="btn-row">
              <button className="btn btn-primary" onClick={xuLyLuu} disabled={dangLuu || !daThayDoi}>
                {dangLuu ? 'Đang lưu...' : 'Lưu thay đổi'}
              </button>
              <button className="btn btn-secondary" onClick={() => navigate('/admin')}>Quay lại</button>
            </div>
          </div>
        )}
      </div>
    </DashboardShell>
  );
}