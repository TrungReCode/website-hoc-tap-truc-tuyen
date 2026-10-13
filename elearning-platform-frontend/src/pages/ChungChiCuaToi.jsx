import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layGiayCuaToi } from '../services/chungChiService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

function dinhDangNgay(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleDateString('vi-VN');
}

export default function ChungChiCuaToi() {
  const navigate = useNavigate();
  const [dsGiay, setDsGiay] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    let daHuy = false;
    layGiayCuaToi()
      .then(ds => { if (!daHuy) setDsGiay(ds || []); })
      .catch(err => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
  }, []);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-certificate" aria-hidden="true"></i> Chứng chỉ &amp; Giấy xác nhận</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
      </div>

      <div style={{ padding: 20 }}>
        {loi && <div className="alert alert-danger">{loi}</div>}

        {dangTai && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải danh sách...</p>}

        {!dangTai && !loi && dsGiay.length === 0 && (
          <div className="empty-state">
            <i className="ti ti-certificate" style={{ fontSize: 28 }} aria-hidden="true"></i>
            <p style={{ marginTop: 6 }}>Bạn chưa có chứng chỉ hoặc giấy xác nhận nào.</p>
            <p style={{ fontSize: 12, color: 'var(--text-3)' }}>
              Hoàn thành 100% học liệu và đạt mọi bài kiểm tra của khóa học/lớp học để đủ điều kiện nhận.
            </p>
          </div>
        )}

        {!dangTai && dsGiay.length > 0 && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 16 }}>
            {dsGiay.map(g => (
              <div key={g.maXacThuc} className="card sm" style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <i
                    className={`ti ${g.loai === 'LOP_HOC' ? 'ti-school' : 'ti-book'}`}
                    style={{ fontSize: 20, color: 'var(--brand)' }}
                    aria-hidden="true"
                  ></i>
                  <span className={`badge ${g.loai === 'LOP_HOC' ? 'badge-blue' : 'badge-green'}`}>
                    {g.loai === 'LOP_HOC' ? 'Giấy xác nhận hoàn thành Lớp học' : 'Giấy xác nhận hoàn thành Khóa học'}
                  </span>
                </div>

                <div style={{ fontSize: 12, color: 'var(--text-3)' }}>Mã xác thực</div>
                <div style={{ fontWeight: 700, fontSize: 15 }}>{g.maXacThuc}</div>

                <div style={{ display: 'flex', gap: 16, fontSize: 12, color: 'var(--text-3)' }}>
                  <span>Hoàn thành: <b style={{ color: 'var(--text)' }}>{Number(g.phanTramHoanThanh).toFixed(0)}%</b></span>
                  {g.diemTrungBinh != null && (
                    <span>Điểm TB: <b style={{ color: 'var(--text)' }}>{Number(g.diemTrungBinh).toFixed(1)}</b></span>
                  )}
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-3)' }}>Ngày cấp: {dinhDangNgay(g.ngayCap)}</div>

                <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
                  {g.filePdfUrl && (
                    <a className="btn btn-primary btn-sm" href={g.filePdfUrl} target="_blank" rel="noreferrer">
                      <i className="ti ti-download" aria-hidden="true"></i> Tải PDF
                    </a>
                  )}
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => navigate(`/xac-thuc-giay/${g.maXacThuc}`)}
                  >
                    <i className="ti ti-shield-check" aria-hidden="true"></i> Xem trang xác thực
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
