import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { layDanhSachQuanLy } from '../../services/duyetKhoaHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';

const TABS = [
  { gia_tri: '', nhan: 'Tất cả' },
  { gia_tri: 'CHO_KIEM_DUYET', nhan: 'Chờ duyệt' },
  { gia_tri: 'CHO_BO_SUNG', nhan: 'Chờ bổ sung' },
  { gia_tri: 'CONG_KHAI', nhan: 'Công khai' },
  { gia_tri: 'NHAP', nhan: 'Nháp' },
  { gia_tri: 'LUU_TRU', nhan: 'Lưu trữ' },
  { gia_tri: 'NOI_BO', nhan: 'Nội bộ' },
];

const NHAN_TRANG_THAI = {
  CHO_KIEM_DUYET: { nhan: 'Chờ duyệt', lop: 'badge-amber' },
  CHO_BO_SUNG: { nhan: 'Chờ giảng viên bổ sung', lop: 'badge-amber' },
  CONG_KHAI: { nhan: 'Công khai', lop: 'badge-green' },
  NHAP: { nhan: 'Nháp', lop: 'badge-gray' },
  LUU_TRU: { nhan: 'Lưu trữ', lop: 'badge-gray' },
  NOI_BO: { nhan: 'Nội bộ', lop: 'badge-red' },
};

export default function DanhSachChoDuyet() {
  const navigate = useNavigate();
  const [trang, setTrang] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 });
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [tabDangChon, setTabDangChon] = useState('');
  const [oTimKiem, setOTimKiem] = useState('');
  const [tuKhoa, setTuKhoa] = useState('');

  const taiLai = useCallback((trangSo = 0, trangThai = tabDangChon, tk = tuKhoa) => {
    setDangTai(true);
    layDanhSachQuanLy({ page: trangSo, trangThai, tuKhoa: tk })
      .then(setTrang)
      .catch((err) => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tabDangChon, tuKhoa]);

  useEffect(() => taiLai(0), [taiLai]);

  function xuLyChonTab(giaTri) {
    setTabDangChon(giaTri);
    taiLai(0, giaTri, tuKhoa);
  }

  function xuLyTimKiem(e) {
    e.preventDefault();
    setTuKhoa(oTimKiem.trim());
    taiLai(0, tabDangChon, oTimKiem.trim());
  }

  const { navItems, roleLabel } = layNavTheoVaiTro();

  return (
    <DashboardShell
      roleLabel={roleLabel}
      navItems={navItems}
      crumb="Hàng chờ kiểm duyệt"
      actions={<span className="badge badge-amber">{trang.totalElements} khóa học</span>}
    >
      <div className="ds-page-head">
        <div><h1>Kiểm duyệt nội dung</h1><p>Toàn bộ khóa học theo trạng thái kiểm duyệt</p></div>
      </div>
      {loi && <div className="alert alert-danger">{loi}</div>}

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12, marginBottom: 14 }}>
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            {TABS.map((t) => (
              <button
                key={t.gia_tri}
                className={`btn btn-sm ${tabDangChon === t.gia_tri ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => xuLyChonTab(t.gia_tri)}
              >
                {t.nhan}
              </button>
            ))}
          </div>

          <form onSubmit={xuLyTimKiem} style={{ display: 'flex', gap: 6 }}>
            <input
              className="form-input"
              style={{ width: 220 }}
              placeholder="Tìm theo tiêu đề..."
              value={oTimKiem}
              onChange={(e) => setOTimKiem(e.target.value)}
            />
            <button type="submit" className="btn btn-secondary btn-sm">
              <i className="ti ti-search" aria-hidden="true"></i>
            </button>
          </form>
        </div>

        {dangTai ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
        ) : trang.content.length === 0 ? (
          <div className="empty-state">
            <i className="ti ti-circle-check" style={{ fontSize: 32, color: 'var(--success)' }} aria-hidden="true"></i>
            <p style={{ marginTop: 8 }}>Không có khóa học nào khớp bộ lọc hiện tại.</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Tiêu đề khóa học</th>
                <th>Tác giả</th>
                <th>Danh mục</th>
                <th>Trạng thái</th>
                <th>Số bài học</th>
                <th>Ngày tạo</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {trang.content.map((k) => {
                const dangChoDuyet = k.trangThai === 'CHO_KIEM_DUYET';
                const nhan = NHAN_TRANG_THAI[k.trangThai] || { nhan: k.trangThai, lop: 'badge-gray' };
                return (
                  <tr
                    key={k.maKhoaHoc}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/admin/kiem-duyet/${k.maKhoaHoc}`)}
                  >
                    <td>{k.tieuDe}</td>
                    <td>{k.tenTacGia || '—'}</td>
                    <td>{k.tenDanhMuc}</td>
                    <td><span className={`badge ${nhan.lop}`}>{nhan.nhan}</span></td>
                    <td>{k.soBaiHoc}</td>
                    <td>{new Date(k.ngayTao).toLocaleDateString('vi-VN')}</td>
                    <td style={{ textAlign: 'right' }}>
                      <button className="btn btn-primary btn-sm" onClick={(e) => { e.stopPropagation(); navigate(`/admin/kiem-duyet/${k.maKhoaHoc}`); }}>
                        {dangChoDuyet ? 'Xét duyệt' : 'Xem nội dung'}
                      </button>
                    </td>
                  </tr>
                );
              })}
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