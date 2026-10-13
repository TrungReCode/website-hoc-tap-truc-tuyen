import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { layDanhSachQuanLy } from '../../services/duyetKhoaHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';

export default function KiemDuyetDanhGia() {
  const navigate = useNavigate();
  const [trang, setTrang] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 });
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [oTimKiem, setOTimKiem] = useState('');
  const [tuKhoa, setTuKhoa] = useState('');

  const taiLai = useCallback((trangSo = 0, tk = tuKhoa) => {
    setDangTai(true);
    // Chỉ khóa học đang công khai mới có đánh giá cần kiểm duyệt.
    layDanhSachQuanLy({ page: trangSo, trangThai: 'CONG_KHAI', tuKhoa: tk })
      .then(setTrang)
      .catch((err) => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tuKhoa]);

  useEffect(() => taiLai(0), [taiLai]);

  function xuLyTimKiem(e) {
    e.preventDefault();
    setTuKhoa(oTimKiem.trim());
    taiLai(0, oTimKiem.trim());
  }

  const { navItems, roleLabel } = layNavTheoVaiTro();

  return (
    <DashboardShell
      roleLabel={roleLabel}
      navItems={navItems}
      crumb="Kiểm duyệt đánh giá"
      actions={<span className="badge badge-amber">{trang.totalElements} khóa học công khai</span>}
    >
      <div className="ds-page-head">
        <div><h1>Kiểm duyệt đánh giá</h1><p>Chọn một khóa học để xem toàn bộ đánh giá (kể cả đã ẩn) và xử lý những đánh giá vi phạm</p></div>
      </div>
      {loi && <div className="alert alert-danger">{loi}</div>}

        <form onSubmit={xuLyTimKiem} style={{ display: 'flex', gap: 6, marginBottom: 14 }}>
          <input
            className="form-input"
            style={{ width: 280 }}
            placeholder="Tìm theo tiêu đề khóa học..."
            value={oTimKiem}
            onChange={(e) => setOTimKiem(e.target.value)}
          />
          <button type="submit" className="btn btn-secondary btn-sm">
            <i className="ti ti-search" aria-hidden="true"></i>
          </button>
        </form>

        {dangTai ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
        ) : trang.content.length === 0 ? (
          <div className="empty-state">Không tìm thấy khóa học công khai nào khớp.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Tiêu đề khóa học</th>
                <th>Tác giả</th>
                <th>Điểm trung bình</th>
                <th>Số đánh giá công khai</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {trang.content.map((k) => (
                <tr key={k.maKhoaHoc} style={{ cursor: 'pointer' }} onClick={() => navigate(`/admin/kiem-duyet-danh-gia/${k.maKhoaHoc}`)}>
                  <td>{k.tieuDe}</td>
                  <td>{k.tenTacGia || '—'}</td>
                  <td>{k.diemTrungBinh ? k.diemTrungBinh.toFixed(1) : '—'}</td>
                  <td>{k.soLuongDanhGia ?? 0}</td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={(e) => { e.stopPropagation(); navigate(`/admin/kiem-duyet-danh-gia/${k.maKhoaHoc}`); }}
                    >
                      Xem đánh giá
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {trang.totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginTop: 20 }}>
            <button className="btn btn-secondary btn-sm" disabled={trang.number === 0} onClick={() => taiLai(trang.number - 1)}>
              ‹
            </button>
            {Array.from({ length: trang.totalPages }, (_, i) => i).map((i) => (
              <button key={i} className={`btn btn-sm ${i === trang.number ? 'btn-primary' : 'btn-secondary'}`} onClick={() => taiLai(i)}>
                {i + 1}
              </button>
            ))}
            <button className="btn btn-secondary btn-sm" disabled={trang.number >= trang.totalPages - 1} onClick={() => taiLai(trang.number + 1)}>
              ›
            </button>
          </div>
        )}
    </DashboardShell>
  );
}
