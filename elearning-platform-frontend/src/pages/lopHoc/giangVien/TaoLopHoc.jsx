import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { taoLop } from '../../../services/lopHocService';
import { layKhoaHocCoTheMoLop } from '../../../services/khoaHocService';
import { layThongBaoLoi } from '../../../utils/layThongBaoLoi';

export default function TaoLopHoc() {
  const navigate = useNavigate();
  const [danhSachKhoaHoc, setDanhSachKhoaHoc] = useState([]);

  // Trạng thái form
  const [form, setForm] = useState({
    maKhoaHoc: '', ngayBatDau: '', ngayKetThuc: '',
    siSo: '', phiLopHoc: 0, congKhai: true,
    muaKhoaHocGoc: true
  });

  // Trạng thái quản lý đặc quyền tác giả
  const [canEditAccess, setCanEditAccess] = useState(false);
  const [dangXuLy, setDangXuLy] = useState(false);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    layKhoaHocCoTheMoLop()
      .then(ds => setDanhSachKhoaHoc(ds.filter(k => k.trangThai === 'CONG_KHAI')))
      .catch(() => { });
  }, []);

  function xuLyChonKhoaHoc(maKhoaHoc) {
    const khoaHocDaChon = danhSachKhoaHoc.find(k => String(k.maKhoaHoc) === String(maKhoaHoc));
    const currentUserId = localStorage.getItem('maNguoiDung');
    // Đảm bảo là String để so sánh chuẩn
    const authorId = khoaHocDaChon ? String(khoaHocDaChon.maTacGia) : null;
    const isAuthor = authorId === currentUserId;

    setCanEditAccess(isAuthor);
    setForm({ ...form, maKhoaHoc, muaKhoaHocGoc: !isAuthor ? true : form.muaKhoaHocGoc });
  }

  async function xuLyTao(e) {
    e.preventDefault();
    setLoi('');

    if (!form.siSo || Number(form.siSo) <= 0) {
      setLoi('Vui lòng nhập sĩ số lớp học (lớn hơn 0)');
      return;
    }
    if (form.phiLopHoc === '' || Number(form.phiLopHoc) < 0) {
      setLoi('Phí tham gia lớp học không hợp lệ');
      return;
    }
    
    if (form.ngayBatDau && new Date(form.ngayBatDau) < new Date()) {
      setLoi('Ngày khai giảng không được trước thời điểm hiện tại');
      return;
    }

    if (form.ngayKetThuc && form.ngayBatDau && new Date(form.ngayKetThuc) < new Date(form.ngayBatDau)) {
      setLoi('Ngày kết thúc không được trước ngày khai giảng');
      return;
    }

    setDangXuLy(true);
    try {
      const yeuCau = {
        maKhoaHoc: form.maKhoaHoc ? Number(form.maKhoaHoc) : null,
        ngayBatDau: form.ngayBatDau || null,
        ngayKetThuc: form.ngayKetThuc || null,
        siSo: form.siSo ? Number(form.siSo) : null,
        phiLopHoc: Number(form.phiLopHoc) || 0,
        congKhai: form.congKhai,
        muaKhoaHocGoc: form.muaKhoaHocGoc
      };
      const lop = await taoLop(yeuCau);
      navigate(`/lop-hoc/${lop.maLopHoc}/quan-ly`);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-plus" aria-hidden="true"></i> Tạo lớp học mới</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/lop-hoc/giang-vien')}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i> Quay lại
        </button>
      </div>
      <div style={{ padding: 20, maxWidth: 600, margin: '0 auto', width: '100%' }}>
        {loi && <div className="alert alert-danger">{loi}</div>}
        <form onSubmit={xuLyTao} className="card">
          <div className="form-group">
            <label className="form-label">Khóa học gốc (chỉ chọn khóa học đang công khai)</label>
            <select className="form-input" value={form.maKhoaHoc} onChange={e => xuLyChonKhoaHoc(e.target.value)} required>
              <option value="" disabled>Chọn khóa học</option>
              {danhSachKhoaHoc.map(k => <option key={k.maKhoaHoc} value={k.maKhoaHoc}>{k.tieuDe}</option>)}
            </select>
          </div>

          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Ngày khai giảng</label>
              <input className="form-input" type="datetime-local" value={form.ngayBatDau} onChange={e => setForm({ ...form, ngayBatDau: e.target.value })} required />
            </div>
            <div className="form-group">
              <label className="form-label">Ngày kết thúc (tùy chọn)</label>
              <input className="form-input" type="datetime-local" value={form.ngayKetThuc} onChange={e => setForm({ ...form, ngayKetThuc: e.target.value })} />
            </div>
          </div>

          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Sĩ số lớp học</label>
              <input
                className="form-input"
                type="number"
                min="1"
                step="1"
                placeholder="Vd: 30"
                value={form.siSo}
                onChange={e => setForm({ ...form, siSo: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Phí tham gia lớp học (VNĐ)</label>
              <input
                className="form-input"
                type="number"
                min="0"
                step="1000"
                placeholder="0 = miễn phí"
                value={form.phiLopHoc}
                onChange={e => setForm({ ...form, phiLopHoc: e.target.value })}
                required
              />
            </div>
          </div>

          {/* Nhóm các thiết lập */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16, marginBottom: 20 }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: 10 }}>
              <button type="button" className={`toggle ${form.congKhai ? 'on' : ''}`} onClick={() => setForm({ ...form, congKhai: !form.congKhai })} style={{ marginTop: 2 }} />
              <span style={{ fontSize: 13, lineHeight: 1.4 }}><strong>Lớp công khai:</strong> Học viên có thể tham gia bằng mã lớp mà không cần lời mời.</span>
            </div>

            {/* Chỉ hiện nếu là tác giả */}
            {canEditAccess && (
              <div style={{ background: 'var(--surface-2)', padding: '12px', borderRadius: 8, borderLeft: '3px solid var(--primary)' }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: 10 }}>
                  <button type="button" className={`toggle ${form.muaKhoaHocGoc ? 'on' : ''}`} onClick={() => setForm({ ...form, muaKhoaHocGoc: !form.muaKhoaHocGoc })} style={{ marginTop: 2 }} />
                  <span style={{ fontSize: 13, fontWeight: 600 }}>Yêu cầu sở hữu khóa học gốc</span>
                </div>
                <p style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 6, marginLeft: 40, lineHeight: 1.5 }}>
                  {form.muaKhoaHocGoc ? "Học viên PHẢI mua khóa học gốc trước khi được phép tham gia lớp này." : "ĐẶC QUYỀN: Học viên được phép tham gia lớp mà không cần mua khóa học gốc."}
                </p>
              </div>
            )}
          </div>

          <div className="btn-row">
            <button type="submit" className="btn btn-primary" disabled={dangXuLy}>{dangXuLy ? 'Đang tạo...' : 'Tạo lớp học'}</button>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/lop-hoc/giang-vien')}>Hủy</button>
          </div>
        </form>
      </div>
    </div>
  );
}