import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layLopGiangVien, dongLop, xoaLop } from '../../../services/lopHocService';
import { layThongBaoLoi } from '../../../utils/layThongBaoLoi';
import DashboardShell, { NAV_GIANG_VIEN } from '../../../components/DashboardShell';

const NHAN_TRANG_THAI = { SAP_DIEN_RA: { nhan: 'Sắp diễn ra', lop: 'badge-blue' }, HOAT_DONG: { nhan: 'Hoạt động', lop: 'badge-green' }, DA_DONG: { nhan: 'Đã đóng', lop: 'badge-gray' } };

export default function DanhSachLopGiangVien() {
  const navigate = useNavigate();
  const [danhSach, setDanhSach] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  function taiLai() {
    setDangTai(true);
    layLopGiangVien().then(setDanhSach).catch(e => setLoi(layThongBaoLoi(e))).finally(() => setDangTai(false));
  }
  useEffect(taiLai, []);

  async function xuLyDong(maLopHoc) {
    if (!window.confirm('Đóng lớp học này? Học viên sẽ không thể tham gia thêm.')) return;
    try { await dongLop(maLopHoc); taiLai(); } catch (e) { setLoi(layThongBaoLoi(e)); }
  }
  async function xuLyXoa(maLopHoc) {
    if (!window.confirm('Xóa lớp học? Chỉ xóa được khi chưa có học viên.')) return;
    try { await xoaLop(maLopHoc); taiLai(); } catch (e) { setLoi(layThongBaoLoi(e)); }
  }
  async function xuLyHuy(maLopHoc, phiLopHoc) {
    const mienPhi = Number(phiLopHoc) === 0;

    const noiDung = mienPhi
      ? 'Hủy lớp học này? Lớp chưa khai giảng, học viên sẽ được gỡ khỏi lớp. Vì đây là lớp miễn phí nên không phát sinh hoàn tiền.'
      : 'Hủy lớp học này? Lớp chưa khai giảng nên toàn bộ học viên đã đóng phí sẽ được TỰ ĐỘNG tạo phiếu hoàn tiền và bị gỡ khỏi lớp ngay lập tức.';

    if (!window.confirm(noiDung)) return;

    try {
      await xoaLop(maLopHoc);
      taiLai();
    } catch (e) {
      setLoi(layThongBaoLoi(e));
    }
  }

  return (
    <DashboardShell
      roleLabel="Giảng viên"
      navItems={NAV_GIANG_VIEN}
      crumb="Quản lý lớp học"
      actions={(
        <button className="btn btn-primary btn-sm" onClick={() => navigate('/lop-hoc/tao-lop')}>
          <i className="ti ti-plus" aria-hidden="true"></i> Tạo lớp học
        </button>
      )}
    >
      <div className="ds-page-head">
        <div>
          <h1>Lớp học của tôi</h1>
          <p>{danhSach.length} lớp đang hoạt động</p>
        </div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}
      {dangTai ? <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
        : danhSach.length === 0 ? <div className="empty-state">Bạn chưa tạo lớp học nào.</div>
          : (
            <div className="card" style={{ padding: 0 }}>
              <table className="data-table">
                <thead><tr><th>Giáo trình - Khóa học gốc</th><th>Mã lớp</th><th>Sĩ số</th><th>Học phí</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
                <tbody>
                  {danhSach.map(lop => {
                    const tt = NHAN_TRANG_THAI[lop.trangThai] || { nhan: lop.trangThai, lop: 'badge-gray' };
                    return (
                      <tr key={lop.maLopHoc}>
                        <td>{lop.tenKhoaHoc || '(Lớp độc lập)'}</td>
                        <td><code style={{ fontFamily: 'monospace', fontWeight: 600 }}>{lop.maLop}</code></td>
                        <td>
                          <span
                            style={{ cursor: 'pointer', color: 'var(--primary)', textDecoration: 'underline' }}
                            onClick={() => navigate(`/lop-hoc/${lop.maLopHoc}/quan-ly?tab=thanh-vien`)}
                            title="Xem danh sách học viên"
                          >
                            {lop.soThanhVien}/{lop.siSo || '∞'}
                          </span>
                        </td>
                        <td>{Number(lop.phiLopHoc) === 0 ? 'Miễn phí' : `${Number(lop.phiLopHoc).toLocaleString('vi-VN')} đ`}</td>
                        <td><span className={`badge ${tt.lop}`}>{tt.nhan}</span></td>
                        <td>
                          <div style={{ display: 'flex', gap: 4 }}>
                            {/* Nút mới thêm: Điều hướng vào Giao diện lớp học chung */}
                            <button
                              className="btn btn-secondary btn-sm"
                              onClick={() => navigate(`/lop-hoc/${lop.maLopHoc}`)}
                            >
                              <i className="ti ti-login" aria-hidden="true"></i> Vào lớp
                            </button>
                            {/* Đóng lớp: chỉ hợp lệ khi KHÔNG phải trường hợp "chưa khai giảng + đã có người trả phí"
                                (backend sẽ từ chối trường hợp đó — ẩn luôn để tránh hiện lỗi khó hiểu, thay bằng nút Hủy lớp) */}
                            {lop.trangThai !== 'DA_DONG' && !(lop.trangThai === 'SAP_DIEN_RA' && lop.soThanhVien > 0) && (
                              <button className="btn btn-warning btn-sm" onClick={() => xuLyDong(lop.maLopHoc)}>Đóng lớp</button>
                            )}
                            {/* Xóa vật lý: chỉ khi chưa có ai tham gia — không phát sinh hoàn tiền vì chưa có giao dịch */}
                            {lop.trangThai !== 'DA_DONG' && lop.soThanhVien === 0 && (
                              <button className="btn btn-danger btn-sm" onClick={() => xuLyXoa(lop.maLopHoc)}>Xóa</button>
                            )}
                            {/* Hủy lớp: chưa khai giảng nhưng đã có học viên trả phí — tự động hoàn tiền + gỡ thành viên */}
                            {lop.trangThai === 'SAP_DIEN_RA' && lop.soThanhVien > 0 && (
                              <button
                                className="btn btn-danger btn-sm"
                                onClick={() => xuLyHuy(lop.maLopHoc, lop.phiLopHoc)}
                              >
                                <i
                                  className={Number(lop.phiLopHoc) === 0 ? 'ti ti-x' : 'ti ti-receipt-refund'}
                                  aria-hidden="true"
                                ></i>
                                {Number(lop.phiLopHoc) === 0 ? ' Hủy lớp' : ' Hủy lớp (hoàn tiền)'}
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