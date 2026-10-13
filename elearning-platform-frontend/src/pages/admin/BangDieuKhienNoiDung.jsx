import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layDanhSachQuanLy } from '../../services/duyetKhoaHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';

export default function BangDieuKhienNoiDung() {
  const navigate = useNavigate();
  const [trang, setTrang] = useState({ content: [], totalElements: 0 });
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    layDanhSachQuanLy({ page: 0, size: 5, trangThai: 'CHO_KIEM_DUYET' })
      .then(setTrang)
      .catch(err => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, []);

  const { navItems, roleLabel } = layNavTheoVaiTro();

  return (
    <DashboardShell roleLabel={roleLabel} navItems={navItems} crumb="Tổng quan">
      {loi && <div className="alert alert-danger">{loi}</div>}

        {/* Stat cards */}
        <div className="stats-grid" style={{ marginBottom: 24 }}>
          <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/admin/kiem-duyet')}>
            <div className="stat-label">Khóa học chờ duyệt</div>
            <div className="stat-value" style={{ color: 'var(--warning)' }}>
              {dangTai ? '...' : trang.totalElements}
            </div>
            <div className="stat-change" style={{ color: 'var(--warning)', fontSize: 11 }}>
              Nhấn để xét duyệt →
            </div>
          </div>
          <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/admin/nguoi-dung?trangThai=BI_CAM')}>
            <div className="stat-label">Quản lý tài khoản</div>
            <div className="stat-value" style={{ color: 'var(--primary)' }}>
              <i className="ti ti-users" style={{ fontSize: 28 }} aria-hidden="true"></i>
            </div>
            <div className="stat-change" style={{ color: 'var(--primary)', fontSize: 11 }}>
              Khoá/mở khoá người dùng →
            </div>
          </div>
        </div>

        {/* Preview danh sách chờ duyệt */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
            <div className="card-title" style={{ margin: 0 }}>Hồ sơ chờ kiểm duyệt gần nhất</div>
            <button className="btn btn-primary btn-sm" onClick={() => navigate('/admin/kiem-duyet')}>
              Xem tất cả
            </button>
          </div>

          {dangTai && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>}
          {!dangTai && trang.content.length === 0 && (
            <div style={{ textAlign: 'center', padding: '20px 0', color: 'var(--text-3)', fontSize: 13 }}>
              <i className="ti ti-circle-check" style={{ fontSize: 28, color: 'var(--success)', display: 'block', marginBottom: 6 }} aria-hidden="true"></i>
              Không có hồ sơ nào đang chờ duyệt!
            </div>
          )}
          {trang.content.map(k => (
            <div key={k.maKhoaHoc}
              onClick={() => navigate(`/admin/kiem-duyet/${k.maKhoaHoc}`)}
              style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 0', borderBottom: '1px solid var(--border)', cursor: 'pointer' }}>
              <div style={{ width: 36, height: 36, borderRadius: 8, background: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary)', flexShrink: 0 }}>
                <i className="ti ti-book" aria-hidden="true"></i>
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 13, fontWeight: 500 }}>{k.tieuDe}</div>
                <div style={{ fontSize: 11, color: 'var(--text-3)' }}>
                  {k.tenTacGia || '—'} · {k.tenDanhMuc} · {k.soBaiHoc} bài học
                </div>
              </div>
              <div style={{ fontSize: 11, color: 'var(--text-3)' }}>
                {new Date(k.ngayTao).toLocaleDateString('vi-VN')}
              </div>
              <button className="btn btn-primary btn-sm" onClick={e => { e.stopPropagation(); navigate(`/admin/kiem-duyet/${k.maKhoaHoc}`); }}>
                Xét duyệt
              </button>
            </div>
          ))}
        </div>
    </DashboardShell>
  );
}