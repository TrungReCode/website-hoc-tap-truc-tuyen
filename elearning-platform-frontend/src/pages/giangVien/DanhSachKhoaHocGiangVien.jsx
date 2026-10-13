import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layKhoaHocCuaToi, guiKiemDuyet, rutLaiKiemDuyet, luuTru, taiCongKhai, xoaKhoaHoc } from '../../services/khoaHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { NAV_GIANG_VIEN } from '../../components/DashboardShell';

const NHAN_TRANG_THAI = {
  NHAP: { nhan: 'Nháp', lop: 'badge-gray' },
  CHO_KIEM_DUYET: { nhan: 'Chờ duyệt', lop: 'badge-amber' },
  CHO_BO_SUNG: { nhan: 'Cần bổ sung', lop: 'badge-amber' },
  CONG_KHAI: { nhan: 'Công khai', lop: 'badge-green' },
  LUU_TRU: { nhan: 'Lưu trữ', lop: 'badge-gray' },
  NOI_BO: { nhan: 'Nội bộ (đã gỡ)', lop: 'badge-red' },
};

export default function DanhSachKhoaHocGiangVien() {
  const navigate = useNavigate();
  const [danhSach, setDanhSach] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [dangXuLyId, setDangXuLyId] = useState(null);

  function taiLai() {
    setDangTai(true);
    layKhoaHocCuaToi()
      .then(setDanhSach)
      .catch((err) => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }

  useEffect(taiLai, []);

  async function xuLyHanhDong(maKhoaHoc, hanhDong) {
    setLoi('');
    setDangXuLyId(maKhoaHoc);
    try {
      if (hanhDong === 'gui-duyet') await guiKiemDuyet(maKhoaHoc);
      else if (hanhDong === 'rut-lai') await rutLaiKiemDuyet(maKhoaHoc);
      else if (hanhDong === 'luu-tru') await luuTru(maKhoaHoc);
      else if (hanhDong === 'tai-cong-khai') await taiCongKhai(maKhoaHoc);
      else if (hanhDong === 'xoa') {
        if (!window.confirm('Xóa khóa học này? Hành động không thể hoàn tác.')) return;
        await xoaKhoaHoc(maKhoaHoc);
      }
      taiLai();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLyId(null);
    }
  }

  return (
    <DashboardShell
      roleLabel="Giảng viên"
      navItems={NAV_GIANG_VIEN}
      crumb="Quản lý khóa học"
      actions={(
        <button className="btn btn-primary btn-sm" onClick={() => navigate('/giang-vien/khoa-hoc/moi')}>
          <i className="ti ti-plus" aria-hidden="true"></i> Tạo khóa học mới
        </button>
      )}
    >
      <div className="ds-page-head">
        <div>
          <h1>Quản lý khóa học</h1>
          <p>{danhSach.length} khóa học · {danhSach.reduce((s, k) => s + (k.soHocVien || 0), 0)} học viên đang theo học</p>
        </div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}

      {dangTai ? (
        <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
      ) : danhSach.length === 0 ? (
        <div className="empty-state">Bạn chưa tạo khóa học nào.</div>
      ) : (
        <div className="card" style={{ padding: 0 }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Tiêu đề</th>
                <th>Danh mục</th>
                <th>Giá</th>
                <th>Trạng thái</th>
                <th>Bài học</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {danhSach.map((k) => {
                const tt = NHAN_TRANG_THAI[k.trangThai] || { nhan: k.trangThai, lop: 'badge-gray' };
                const dangXuLy = dangXuLyId === k.maKhoaHoc;
                return (
                  <tr key={k.maKhoaHoc}>
                    <td style={{ cursor: 'pointer', color: 'var(--primary)' }} onClick={() => navigate(`/giang-vien/khoa-hoc/${k.maKhoaHoc}`)}>
                      {k.tieuDe}
                    </td>
                    <td>{k.tenDanhMuc}</td>
                    <td>{Number(k.gia) === 0 ? 'Miễn phí' : `${Number(k.gia).toLocaleString('vi-VN')} đ`}</td>
                    <td><span className={`badge ${tt.lop}`}>{tt.nhan}</span></td>
                    <td>{k.soBaiHoc}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                        {k.trangThai !== 'CHO_KIEM_DUYET' && k.trangThai !== 'CONG_KHAI' && (
                          <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/giang-vien/khoa-hoc/sua/${k.maKhoaHoc}`)}>
                            Sửa
                          </button>
                        )}
                        {k.trangThai === 'NHAP' && (
                          <>
                            <button className="btn btn-success btn-sm" disabled={dangXuLy} onClick={() => xuLyHanhDong(k.maKhoaHoc, 'gui-duyet')}>
                              Gửi duyệt
                            </button>
                            <button className="btn btn-danger btn-sm" disabled={dangXuLy} onClick={() => xuLyHanhDong(k.maKhoaHoc, 'xoa')}>
                              Xóa
                            </button>
                          </>
                        )}
                        {k.trangThai === 'CHO_KIEM_DUYET' && (
                          <button className="btn btn-secondary btn-sm" disabled={dangXuLy} onClick={() => xuLyHanhDong(k.maKhoaHoc, 'rut-lai')}>
                            Rút lại
                          </button>
                        )}
                        {k.trangThai === 'CHO_BO_SUNG' && (
                          <button
                            className="btn btn-secondary btn-sm"
                            style={{ color: 'var(--warning)', borderColor: 'var(--warning)' }}
                            onClick={() => navigate(`/giang-vien/khoa-hoc/sua/${k.maKhoaHoc}`)}
                          >
                            <i className="ti ti-alert-triangle" aria-hidden="true"></i> Xem yêu cầu & bổ sung
                          </button>
                        )}
                        {k.trangThai === 'CONG_KHAI' && (
                          <button className="btn btn-secondary btn-sm" disabled={dangXuLy} onClick={() => xuLyHanhDong(k.maKhoaHoc, 'luu-tru')}>
                            Lưu trữ
                          </button>
                        )}
                        {k.trangThai === 'LUU_TRU' && (
                          <button className="btn btn-success btn-sm" disabled={dangXuLy} onClick={() => xuLyHanhDong(k.maKhoaHoc, 'tai-cong-khai')}>
                            Tái công khai
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </DashboardShell>
  );
}
