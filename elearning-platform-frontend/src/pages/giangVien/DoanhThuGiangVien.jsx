import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  layDoanhThuCuaToi, layDoiSoatCuaToi, layDangChoHoanThanhLop,
  layGiaoDichDoanhThuCuaToi, layXuHuongDoanhThuCuaToi, taiXuongCsvDoanhThuCuaToi,
  layTongQuanDoanhThuCuaToi,
} from '../../services/thanhToanService';
import { layKhoaHocCuaToi } from '../../services/khoaHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import BieuDoDoanhThu from '../../components/BieuDoDoanhThu';
import TongQuanDoanhThu from '../../components/TongQuanDoanhThu';
import DashboardShell, { NAV_GIANG_VIEN } from '../../components/DashboardShell';

const dinhDangTien = n =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n || 0);

function formatNgay(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}

function dichTrangThaiDoiSoat(trangThai) {
  const banDo = { CHO_XAC_NHAN: 'Chờ xác nhận', DANG_XU_LY: 'Đang xử lý', HOAN_TAT: 'Đã nhận tiền', THAT_BAI: 'Thất bại' };
  return banDo[trangThai] || trangThai || 'Không rõ';
}

function layMauTrangThaiDoiSoat(trangThai) {
  switch (trangThai) {
    case 'HOAN_TAT': return 'green';
    case 'DANG_XU_LY':
    case 'CHO_XAC_NHAN': return 'amber';
    case 'THAT_BAI': return 'red';
    default: return 'gray';
  }
}

export default function DoanhThuGiangVien() {
  const navigate = useNavigate();
  const [tongQuan, setTongQuan] = useState(null);
  const [dangTaiTongQuan, setDangTaiTongQuan] = useState(true);
  const [loi, setLoi] = useState('');

  const [dangChoHoanThanh, setDangChoHoanThanh] = useState([]);
  const [dangTaiChoHoanThanh, setDangTaiChoHoanThanh] = useState(true);

  const [trangThaiLoc, setTrangThaiLoc] = useState('');
  const [trang, setTrang] = useState(0);
  const [danhSach, setDanhSach] = useState(null);
  const [dangTaiDs, setDangTaiDs] = useState(true);

  // ── UC-16: lọc theo thời gian/khóa học, biểu đồ xu hướng, xuất CSV ──
  const [dsKhoaHoc, setDsKhoaHoc] = useState([]);
  const [tuNgayLoc, setTuNgayLoc] = useState('');
  const [denNgayLoc, setDenNgayLoc] = useState('');
  const [maKhoaHocLoc, setMaKhoaHocLoc] = useState('');
  const [xuHuong, setXuHuong] = useState(null);
  const [giaoDich, setGiaoDich] = useState(null);
  const [dangTaiBaoCao, setDangTaiBaoCao] = useState(true);
  const [dangXuatCsv, setDangXuatCsv] = useState(false);

  useEffect(() => {
    layKhoaHocCuaToi().then(setDsKhoaHoc).catch(() => {});
  }, []);

  useEffect(() => {
    let daHuy = false;
    setDangTaiBaoCao(true);
    const boLoc = {
      tuNgay: tuNgayLoc || undefined,
      denNgay: denNgayLoc || undefined,
      maKhoaHoc: maKhoaHocLoc || undefined,
    };
    Promise.all([layXuHuongDoanhThuCuaToi(boLoc), layGiaoDichDoanhThuCuaToi(boLoc)])
      .then(([xh, gd]) => { if (!daHuy) { setXuHuong(xh); setGiaoDich(gd); } })
      .catch(err => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTaiBaoCao(false); });
    return () => { daHuy = true; };
  }, [tuNgayLoc, denNgayLoc, maKhoaHocLoc]);

  async function xuLyXuatCsv() {
    setDangXuatCsv(true);
    try {
      await taiXuongCsvDoanhThuCuaToi({
        tuNgay: tuNgayLoc || undefined,
        denNgay: denNgayLoc || undefined,
        maKhoaHoc: maKhoaHocLoc || undefined,
      });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuatCsv(false);
    }
  }

  useEffect(() => {
    layDangChoHoanThanhLop()
      .then(setDangChoHoanThanh)
      .catch(() => {})
      .finally(() => setDangTaiChoHoanThanh(false));
  }, []);

  useEffect(() => {
    layDoanhThuCuaToi()
      .then(setTongQuan)
      .catch(err => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTaiTongQuan(false));
  }, []);

  useEffect(() => { setTrang(0); }, [trangThaiLoc]);

  useEffect(() => {
    let daHuy = false;
    setDangTaiDs(true);
    layDoiSoatCuaToi({ trangThai: trangThaiLoc, page: trang, size: 10 })
      .then(d => { if (!daHuy) setDanhSach(d); })
      .catch(err => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTaiDs(false); });
    return () => { daHuy = true; };
  }, [trangThaiLoc, trang]);

  const tongSoTrang = danhSach?.totalPages || 0;

  return (
    <DashboardShell roleLabel="Giảng viên" navItems={NAV_GIANG_VIEN} crumb="Doanh thu & Thống kê">
      <div className="ds-page-head">
        <div>
          <h1>Doanh thu & Thống kê</h1>
          <p>Tổng hợp doanh thu từ khóa học và lớp học</p>
        </div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}
        {dangTaiTongQuan && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>}

        {tongQuan && !tongQuan.coTaiKhoanNganHang && (
          <div className="alert alert-danger" style={{ marginBottom: 16 }}>
            <i className="ti ti-alert-triangle" aria-hidden="true"></i>
            {' '}Bạn chưa khai báo tài khoản ngân hàng nên hệ thống không thể chuyển tiền đối soát cho bạn.{' '}
            <a href="/ho-so" onClick={e => { e.preventDefault(); navigate('/ho-so'); }} style={{ fontWeight: 600 }}>
              Khai báo ngay
            </a>
          </div>
        )}

        <div className="card" style={{ marginBottom: 24 }}>
          <div className="card-title" style={{ marginBottom: 4 }}>
            <i className="ti ti-layout-dashboard" aria-hidden="true"></i> Tổng quan doanh thu
          </div>
          <TongQuanDoanhThu layTongQuan={layTongQuanDoanhThuCuaToi} extraParams={{}} />
        </div>

        <div className="card-title" style={{ marginBottom: 8, marginTop: 4 }}>
          <i className="ti ti-sum" aria-hidden="true"></i> Tổng cộng từ trước đến nay
        </div>

        {tongQuan && (
          <>
            <div className="stats-grid" style={{ marginBottom: 24 }}>
              <div className="stat-card">
                <div className="stat-label">Doanh thu khóa học</div>
                <div className="stat-value" style={{ fontSize: 18, color: 'var(--success)' }}>
                  {dinhDangTien(tongQuan.tongDoanhThuKhoaHoc)}
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Doanh thu lớp học</div>
                <div className="stat-value" style={{ fontSize: 18, color: 'var(--success)' }}>
                  {dinhDangTien(tongQuan.tongDoanhThuLopHoc)}
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Tổng doanh thu (gộp)</div>
                <div className="stat-value" style={{ fontSize: 18, color: 'var(--primary)', fontWeight: 700 }}>
                  {dinhDangTien(tongQuan.tongDoanhThu)}
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Đã thực nhận</div>
                <div className="stat-value" style={{ fontSize: 18, color: 'var(--success)', fontWeight: 700 }}>
                  {dinhDangTien(tongQuan.tongThucNhan)}
                </div>
                <div className="stat-change">Sau khi trừ hoa hồng nền tảng</div>
              </div>
            </div>

            <div className="stats-grid" style={{ marginBottom: 24 }}>
              <div className="stat-card">
                <div className="stat-label">Khóa học đang có</div>
                <div className="stat-value">{tongQuan.soKhoaHoc}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Lớp học đang dạy</div>
                <div className="stat-value">{tongQuan.soLopHoc}</div>
              </div>
              <div className="stat-card" style={{ border: tongQuan.soPhieuChoXacNhan > 0 ? '1px solid var(--warning)' : undefined }}>
                <div className="stat-label">Chờ xác nhận</div>
                <div className="stat-value" style={{ color: 'var(--warning)' }}>{tongQuan.soPhieuChoXacNhan}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Đang xử lý</div>
                <div className="stat-value" style={{ color: 'var(--warning)' }}>{tongQuan.soPhieuDangXuLy}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Đã nhận tiền</div>
                <div className="stat-value" style={{ color: 'var(--success)' }}>{tongQuan.soPhieuHoanTat}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Thất bại</div>
                <div className="stat-value" style={{ color: 'var(--danger)' }}>{tongQuan.soPhieuThatBai}</div>
              </div>
            </div>
          </>
        )}

        {!dangTaiChoHoanThanh && dangChoHoanThanh.length > 0 && (
          <div className="card" style={{ marginBottom: 20, borderLeft: '3px solid var(--warning)' }}>
            <div className="card-title">
              <i className="ti ti-hourglass" style={{ color: 'var(--warning)' }} aria-hidden="true"></i>
              Đang chờ lớp học kết thúc ({dangChoHoanThanh.length})
            </div>
            <p style={{ fontSize: 12.5, color: 'var(--text-3)', marginTop: -6, marginBottom: 14 }}>
              Các khoản thu phí lớp học dưới đây đã qua hạn giữ tiền nhưng <strong>chưa được lập phiếu đối soát</strong> vì
              lớp học chưa kết thúc hoặc chưa được đóng. Hệ thống sẽ tự động giải ngân ngay khi lớp đạt điều kiện — không cần thao tác gì thêm.
            </p>
            {dangChoHoanThanh.map(p => (
              <div key={p.maPhieu} style={{ display: 'flex', justifyContent: 'space-between', gap: 10, padding: '10px 0', borderTop: '1px solid var(--border)' }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 13.5 }}>{p.tenKhoaHoc}</div>
                  <div style={{ fontSize: 12, color: 'var(--text-3)' }}>
                    Mã lớp {p.maLop} · {p.trangThaiLop === 'HOAT_DONG' ? 'Đang hoạt động' : p.trangThaiLop === 'SAP_DIEN_RA' ? 'Sắp diễn ra' : p.trangThaiLop}
                    {p.ngayKetThucLop && ` · Dự kiến kết thúc ${formatNgay(p.ngayKetThucLop)}`}
                  </div>
                </div>
                <div style={{ fontWeight: 600, whiteSpace: 'nowrap' }}>{dinhDangTien(p.soTien)}</div>
              </div>
            ))}
          </div>
        )}

        <div className="card" style={{ marginBottom: 20 }}>
          <div className="card-title"><i className="ti ti-chart-bar" aria-hidden="true"></i> Xu hướng doanh thu</div>

          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 16 }}>
            <label style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 12, color: 'var(--text-3)' }}>
              Từ ngày
              <input type="date" className="form-input" value={tuNgayLoc} onChange={e => setTuNgayLoc(e.target.value)} />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 12, color: 'var(--text-3)' }}>
              Đến ngày
              <input type="date" className="form-input" value={denNgayLoc} onChange={e => setDenNgayLoc(e.target.value)} />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 12, color: 'var(--text-3)', minWidth: 220 }}>
              Khóa học
              <select className="btn btn-secondary btn-sm" value={maKhoaHocLoc} onChange={e => setMaKhoaHocLoc(e.target.value)}>
                <option value="">Tất cả khóa học</option>
                {dsKhoaHoc.map(kh => (
                  <option key={kh.maKhoaHoc} value={kh.maKhoaHoc}>{kh.tieuDe}</option>
                ))}
              </select>
            </label>
            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button className="btn btn-secondary btn-sm" disabled={dangXuatCsv} onClick={xuLyXuatCsv}>
                <i className="ti ti-file-spreadsheet" aria-hidden="true"></i>
                {dangXuatCsv ? ' Đang xuất...' : ' Xuất CSV'}
              </button>
            </div>
          </div>

          {dangTaiBaoCao ? (
            <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
          ) : (
            <>
              <BieuDoDoanhThu diem={xuHuong?.diem} />

              <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-3)', margin: '18px 0 8px', textTransform: 'uppercase' }}>
                Chi tiết giao dịch ({giaoDich?.length || 0})
              </div>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Ngày</th>
                    <th>Loại</th>
                    <th>Khóa học</th>
                    <th>Lớp</th>
                    <th>Số tiền</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {(!giaoDich || giaoDich.length === 0) && (
                    <tr><td colSpan={6} style={{ textAlign: 'center', color: 'var(--text-3)', padding: '20px 0' }}>Không có giao dịch nào khớp bộ lọc</td></tr>
                  )}
                  {giaoDich?.map(gd => (
                    <tr key={`${gd.loai}-${gd.maPhieu}`}>
                      <td style={{ fontSize: 12.5, color: 'var(--text-3)' }}>{formatNgay(gd.ngayGiaoDich)}</td>
                      <td>{gd.loai === 'KHOA_HOC' ? 'Mua khóa học' : 'Phí lớp học'}</td>
                      <td>{gd.tenKhoaHoc}</td>
                      <td>{gd.maLop || '—'}</td>
                      <td style={{ fontWeight: 600 }}>{dinhDangTien(gd.soTien)}</td>
                      <td><span className={`badge badge-${gd.trangThai === 'THANH_CONG' ? 'green' : 'red'}`}>{gd.trangThai}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </div>

        <div className="card">
          <div className="card-title"><i className="ti ti-receipt-2" aria-hidden="true"></i> Lịch sử đối soát</div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 13, color: 'var(--text-3)', width: 220 }}>
              Lọc theo trạng thái
              <select className="btn btn-secondary btn-sm" value={trangThaiLoc} onChange={e => setTrangThaiLoc(e.target.value)}>
                <option value="">Tất cả</option>
                <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
                <option value="DANG_XU_LY">Đang xử lý</option>
                <option value="HOAN_TAT">Đã nhận tiền</option>
                <option value="THAT_BAI">Thất bại</option>
              </select>
            </label>
          </div>

          <table className="data-table">
            <thead>
              <tr>
                <th>Mã đối soát</th>
                <th>Tài khoản nhận</th>
                <th>Doanh thu</th>
                <th>Thực nhận</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
              </tr>
            </thead>
            <tbody>
              {!dangTaiDs && danhSach?.content?.length === 0 && (
                <tr><td colSpan={6} style={{ textAlign: 'center', color: 'var(--text-3)', padding: '20px 0' }}>Chưa có phiếu đối soát nào</td></tr>
              )}
              {danhSach?.content?.map(p => (
                <tr key={p.maDoiSoat}>
                  <td>#{p.maDoiSoat}</td>
                  <td>
                    <div style={{ fontWeight: 600 }}>{p.soTaiKhoan || 'N/A'}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-3)' }}>{p.tenNganHang || 'N/A'}</div>
                  </td>
                  <td>{dinhDangTien(p.tongDoanhThu)}</td>
                  <td>{dinhDangTien(p.soTienThucTe)}</td>
                  <td><span className={`badge badge-${layMauTrangThaiDoiSoat(p.trangThai)}`}>{dichTrangThaiDoiSoat(p.trangThai)}</span></td>
                  <td style={{ fontSize: 13, color: 'var(--text-3)' }}>{formatNgay(p.ngayTao)}</td>
                </tr>
              ))}
            </tbody>
          </table>

          {tongSoTrang > 1 && (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, marginTop: 16, flexWrap: 'wrap' }}>
              <div style={{ fontSize: 13, color: 'var(--text-3)' }}>Trang {trang + 1} / {tongSoTrang}</div>
              <div style={{ display: 'flex', gap: 8 }}>
                <button className="btn btn-secondary btn-sm" disabled={trang <= 0} onClick={() => setTrang(t => Math.max(0, t - 1))}>Trước</button>
                <button className="btn btn-secondary btn-sm" disabled={trang >= tongSoTrang - 1} onClick={() => setTrang(t => Math.min(tongSoTrang - 1, t + 1))}>Sau</button>
              </div>
            </div>
          )}
        </div>
    </DashboardShell>
  );
}