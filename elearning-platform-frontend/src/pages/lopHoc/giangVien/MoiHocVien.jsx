import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { moiHocVien, layLoiMoiCuaLop, layThanhVien } from '../../../services/ghiDanhService';
import { layThongBaoLoi } from '../../../utils/layThongBaoLoi';

const NHAN_TRANG_THAI = { CHO_XAC_NHAN: { nhan: 'Chờ', lop: 'badge-amber' }, DONG_Y: { nhan: 'Đồng ý', lop: 'badge-green' }, TU_CHOI: { nhan: 'Từ chối', lop: 'badge-red' } };

export default function MoiHocVien() {
  const { maLopHoc } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const tabBanDau = searchParams.get('tab') === 'thanh-vien' ? 'thanh-vien' : 'loi-moi';

  const [emailInput, setEmailInput] = useState('');
  const [loiMoi, setLoiMoi] = useState([]);
  const [thanhVien, setThanhVien] = useState([]);
  const [tab, setTab] = useState(tabBanDau);
  const [dangGui, setDangGui] = useState(false);
  const [loi, setLoi] = useState('');

  function taiLai() {
    layLoiMoiCuaLop(maLopHoc).then(setLoiMoi).catch(() => {});
    layThanhVien(maLopHoc).then(setThanhVien).catch(() => {});
  }
  useEffect(taiLai, [maLopHoc]);

  async function xuLyMoi(e) {
    e.preventDefault();
    const dsEmail = emailInput.split(/[\n,;]+/).map(s => s.trim()).filter(Boolean);
    if (!dsEmail.length) return;
    setLoi('');
    setDangGui(true);
    try {
      await moiHocVien(maLopHoc, dsEmail);
      setEmailInput('');
      taiLai();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/lop-hoc/${maLopHoc}/quan-ly`)}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i>
        </button>
        <span className="t-title"><i className="ti ti-mail" aria-hidden="true"></i> Mời học viên</span>
      </div>

      <div className="sc-split">
        <div className="split-left">
          {loi && <div className="alert alert-danger">{loi}</div>}
          <div className="card">
            <div className="card-title">Gửi lời mời qua email</div>
            <p style={{ fontSize: 12, color: 'var(--text-2)', marginBottom: 12 }}>
              Nhập một hoặc nhiều email (phân cách bằng dấu phẩy, dấu chấm phẩy hoặc xuống dòng).
              Mỗi lời mời có hiệu lực trong 7 ngày.
            </p>
            <form onSubmit={xuLyMoi}>
              <textarea
                className="form-input"
                style={{ height: 100, padding: 8, marginBottom: 10 }}
                placeholder={'hocvien1@gmail.com\nhocvien2@gmail.com'}
                value={emailInput}
                onChange={e => setEmailInput(e.target.value)}
              />
              <button type="submit" className="btn btn-primary" disabled={dangGui || !emailInput.trim()}>
                {dangGui ? 'Đang gửi...' : 'Gửi lời mời'}
              </button>
            </form>
          </div>
        </div>

        <div className="split-right">
          <div className="tab-row">
            <div className={`tab-item ${tab === 'loi-moi' ? 'active' : ''}`} onClick={() => setTab('loi-moi')}>
              Lời mời ({loiMoi.length})
            </div>
            <div className={`tab-item ${tab === 'thanh-vien' ? 'active' : ''}`} onClick={() => setTab('thanh-vien')}>
              Thành viên ({thanhVien.length})
            </div>
          </div>

          {tab === 'loi-moi' && (
            loiMoi.length === 0 ? <div className="empty-state">Chưa có lời mời nào.</div>
            : loiMoi.map(m => {
              const tt = NHAN_TRANG_THAI[m.trangThai] || { nhan: m.trangThai, lop: 'badge-gray' };
              return (
                <div key={m.maLoiMoi} className="invite-row">
                  <span className="invite-email">{m.emailHocVien}</span>
                  <span className="invite-date">{new Date(m.ngayTao).toLocaleDateString('vi-VN')}</span>
                  <span className={`badge ${tt.lop}`}>{tt.nhan}</span>
                </div>
              );
            })
          )}

          {tab === 'thanh-vien' && (
            thanhVien.length === 0 ? <div className="empty-state">Chưa có thành viên nào.</div>
            : thanhVien.map(tv => (
              <div key={tv.maHocVien} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '8px 0', borderBottom: '1px solid var(--border)' }}>
                <div className="avatar" style={{ width: 28, height: 28, fontSize: 11 }}>
                  {(tv.tenHocVien || '?').charAt(0).toUpperCase()}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 13, fontWeight: 500 }}>{tv.tenHocVien || 'Học viên'}</div>
                  {tv.emailHocVien && (
                    <div style={{ fontSize: 11.5, color: 'var(--text-2)' }}>{tv.emailHocVien}</div>
                  )}
                  <div style={{ fontSize: 11, color: 'var(--text-3)' }}>
                    Tham gia: {new Date(tv.ngayThamGia).toLocaleDateString('vi-VN')}
                  </div>
                </div>
                <div style={{ fontSize: 12 }}>
                  <div className="progress-bar" style={{ width: 60 }}>
                    <div className="progress-fill" style={{ width: `${tv.tienDoPhanTram}%` }} />
                  </div>
                  <span style={{ fontSize: 11, color: 'var(--text-3)' }}>{tv.tienDoPhanTram}%</span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
