import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layKhoaHocCuaToi } from '../../services/khoaHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { NAV_GIANG_VIEN } from '../../components/DashboardShell';

const NHAN_TRANG_THAI = {
  NHAP: { nhan: 'Nháp', lop: 'badge-gray' },
  CHO_KIEM_DUYET: { nhan: 'Chờ duyệt', lop: 'badge-amber' },
  CHO_BO_SUNG: { nhan: 'Cần bổ sung', lop: 'badge-amber' },
  CONG_KHAI: { nhan: 'Công khai', lop: 'badge-green' },
  LUU_TRU: { nhan: 'Lưu trữ', lop: 'badge-gray' },
  NOI_BO: { nhan: 'Nội bộ', lop: 'badge-red' },
};

export default function BangDieuKhienGiangVien() {
  const navigate = useNavigate();
  const [danhSach, setDanhSach] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    layKhoaHocCuaToi()
      .then(setDanhSach)
      .catch((err) => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, []);

  const tongCongKhai = danhSach.filter((k) => k.trangThai === 'CONG_KHAI').length;
  const tongChoDuyet = danhSach.filter((k) => k.trangThai === 'CHO_KIEM_DUYET').length;
  const danhGiaCo = danhSach.filter((k) => k.diemTrungBinh != null);
  const diemTrungBinhChung = danhGiaCo.length
    ? (danhGiaCo.reduce((t, k) => t + k.diemTrungBinh, 0) / danhGiaCo.length).toFixed(1)
    : '—';

  return (
    <DashboardShell
      roleLabel="Giảng viên"
      navItems={NAV_GIANG_VIEN}
      crumb="Tổng quan"
      actions={(
        <button className="btn btn-primary btn-sm" onClick={() => navigate('/giang-vien/khoa-hoc/moi')}>
          <i className="ti ti-plus" aria-hidden="true"></i> Tạo khóa học mới
        </button>
      )}
    >
      <div className="ds-page-head">
        <div>
          <h1>Tổng quan</h1>
          <p>Bức tranh toàn cảnh về các khóa học và hoạt động giảng dạy của bạn</p>
        </div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Tổng khóa học</div>
          <div className="stat-value">{danhSach.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Đang công khai</div>
          <div className="stat-value">{tongCongKhai}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Chờ kiểm duyệt</div>
          <div className="stat-value">{tongChoDuyet}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Đánh giá trung bình</div>
          <div className="stat-value">{diemTrungBinhChung}</div>
        </div>
      </div>

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
          <div className="card-title" style={{ marginBottom: 0 }}>Danh sách Khóa học</div>
          <span style={{ fontSize: 15, color: 'var(--primary)', cursor: 'pointer' }} onClick={() => navigate('/giang-vien/khoa-hoc')}>
            Xem chi tiết danh sách ›
          </span>
        </div>

        {dangTai ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
        ) : danhSach.length === 0 ? (
          <div className="empty-state">
            <p>Bạn chưa tạo khóa học nào.</p>
            <button className="btn btn-primary btn-sm" style={{ marginTop: 10 }} onClick={() => navigate('/giang-vien/khoa-hoc/moi')}>
              Tạo khóa học đầu tiên
            </button>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Tiêu đề</th>
                <th>Trạng thái</th>
                <th>Đánh giá</th>
              </tr>
            </thead>
            <tbody>
              {danhSach.slice(0, 5).map((k) => {
                const tt = NHAN_TRANG_THAI[k.trangThai] || { nhan: k.trangThai, lop: 'badge-gray' };
                return (
                  <tr key={k.maKhoaHoc}>
                    <td>{k.tieuDe}</td>
                    <td><span className={`badge ${tt.lop}`}>{tt.nhan}</span></td>
                    <td>{k.diemTrungBinh ? `${k.diemTrungBinh.toFixed(1)} ★ (${k.soLuongDanhGia})` : 'Chưa có'}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </DashboardShell>
  );
}