import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layKhoaHocDaMua } from '../../services/khoaHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';

export default function KhoaHocDaMua() {
  const navigate = useNavigate();
  const [dsKhoaHoc, setDsKhoaHoc] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    let daHuy = false;
    layKhoaHocDaMua()
      .then(ds => { if (!daHuy) setDsKhoaHoc(ds || []); })
      .catch(err => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
  }, []);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-books" aria-hidden="true"></i> Khóa học đã mua</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
      </div>

      <div style={{ padding: 20 }}>
        {loi && <div className="alert alert-danger">{loi}</div>}

        {dangTai && (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải danh sách khóa học...</p>
        )}

        {!dangTai && !loi && dsKhoaHoc.length === 0 && (
          <div className="empty-state">
            <i className="ti ti-books" style={{ fontSize: 28 }} aria-hidden="true"></i>
            <p style={{ marginTop: 6 }}>Bạn chưa mua khóa học nào.</p>
            <button className="btn btn-primary btn-sm" style={{ marginTop: 8 }} onClick={() => navigate('/cho-khoa-hoc')}>
              Khám phá khóa học
            </button>
          </div>
        )}

        {!dangTai && dsKhoaHoc.length > 0 && (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
              gap: 16,
            }}
          >
            {dsKhoaHoc.map(kh => (
              <div
                key={kh.maKhoaHoc}
                className="card sm"
                role="button"
                tabIndex={0}
                onClick={() => navigate(`/khoa-hoc-cua-toi/${kh.maKhoaHoc}/hoc`)}
                onKeyDown={e => { if (e.key === 'Enter') navigate(`/khoa-hoc-cua-toi/${kh.maKhoaHoc}/hoc`); }}
                style={{ cursor: 'pointer', display: 'flex', flexDirection: 'column', gap: 8 }}
              >
                {kh.anhBia && (
                  <img
                    src={kh.anhBia}
                    alt={kh.tieuDe}
                    style={{ width: '100%', height: 120, objectFit: 'cover', borderRadius: 6 }}
                  />
                )}
                <div style={{ fontWeight: 600, fontSize: 14 }}>{kh.tieuDe}</div>
                <div style={{ fontSize: 12, color: 'var(--text-3)' }}>
                  GV: {kh.tenTacGia || '—'} · {kh.tenDanhMuc || 'Chưa phân loại'}
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-3)' }}>
                  {kh.soBaiHoc ?? 0} bài học
                  {kh.diemTrungBinh ? ` · ⭐ ${kh.diemTrungBinh.toFixed(1)} (${kh.soLuongDanhGia})` : ''}
                </div>
                <button
                  className="btn btn-primary btn-sm"
                  onClick={e => { e.stopPropagation(); navigate(`/khoa-hoc-cua-toi/${kh.maKhoaHoc}/hoc`); }}
                >
                  Vào học
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
