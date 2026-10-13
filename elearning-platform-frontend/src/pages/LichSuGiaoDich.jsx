import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layLichSuGiaoDich, layLichSuGiaoDichNhan, taoYeuCauHoanTienKhoaHoc, taoYeuCauHoanTienLopHoc } from '../services/thanhToanService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

const dinhDangTien = n =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n || 0);

function formatNgay(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}

const TRANG_THAI_GIAO_DICH = {
  CHO_XAC_NHAN: { text: 'Chờ xác nhận', mau: 'badge-amber' },
  THANH_CONG: { text: 'Thành công', mau: 'badge-green' },
  THAT_BAI: { text: 'Thất bại', mau: 'badge-gray' },
  HOAN_TIEN: { text: 'Đã hoàn tiền', mau: 'badge-blue' },
};

const TRANG_THAI_HOAN_TIEN = {
  CHO_XAC_NHAN: { text: 'Đang chờ duyệt hoàn tiền', mau: 'badge-amber' },
  HOAN_TAT: { text: 'Đã hoàn tiền', mau: 'badge-green' },
  TU_CHOI: { text: 'Yêu cầu hoàn tiền bị từ chối', mau: 'badge-red' },
};

const TRANG_THAI_DOI_SOAT = {
  CHO_XAC_NHAN: { text: 'Chờ đối soát', mau: 'badge-amber' },
  DANG_XU_LY: { text: 'Đang xử lý giải ngân', mau: 'badge-blue' },
  HOAN_TAT: { text: 'Đã nhận', mau: 'badge-green' },
  THAT_BAI: { text: 'Giải ngân thất bại', mau: 'badge-red' },
};

export default function LichSuGiaoDich() {
  const navigate = useNavigate();
  const [danhSach, setDanhSach] = useState([]);
  const [danhSachNhan, setDanhSachNhan] = useState([]);
  const [tab, setTab] = useState('GUI'); // 'GUI' | 'NHAN'
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [locLoai, setLocLoai] = useState(''); // '' | 'KHOA_HOC' | 'LOP_HOC'
  const [dangGuiHoanTien, setDangGuiHoanTien] = useState(null); // maPhieu đang xử lý

  async function taiDanhSach() {
    setDangTai(true);
    setLoi('');
    try {
      const [gui, nhan] = await Promise.all([layLichSuGiaoDich(), layLichSuGiaoDichNhan()]);
      setDanhSach(gui);
      setDanhSachNhan(nhan);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangTai(false);
    }
  }

  useEffect(() => { taiDanhSach(); }, []);

  async function xuLyYeuCauHoanTien(giaoDich) {
    const lyDo = window.prompt('Vui lòng cho biết lý do yêu cầu hoàn tiền:');
    if (lyDo === null) return;
    if (!lyDo.trim()) {
      alert('Vui lòng nhập lý do hoàn tiền.');
      return;
    }
    setDangGuiHoanTien(giaoDich.maPhieu);
    try {
      const taoYeuCau = giaoDich.loaiGiaoDich === 'KHOA_HOC' ? taoYeuCauHoanTienKhoaHoc : taoYeuCauHoanTienLopHoc;
      await taoYeuCau(giaoDich.maPhieu, lyDo.trim());
      await taiDanhSach();
    } catch (err) {
      alert(layThongBaoLoi(err));
    } finally {
      setDangGuiHoanTien(null);
    }
  }

  const danhSachHienThi = (locLoai
    ? danhSach.filter(g => g.loaiGiaoDich === locLoai)
    : danhSach
  ).filter(g => Number(g.soTien || 0) > 0);
  // FIX: tab "Đã nhận" trước đây hiện TẤT CẢ trạng thái đối soát (Chờ đối
  // soát, Đang xử lý, Đã nhận, Thất bại) — chỉ nên hiện phiếu ĐÃ ĐỐI SOÁT
  // XONG (HOAN_TAT, tức giảng viên đã thực sự nhận được tiền). Các trạng
  // thái khác chưa phải "đã nhận" thật, hiện ra dễ gây hiểu nhầm "đã có
  // tiền" trong khi thực ra còn đang chờ xử lý.
  const danhSachNhanDaDoiSoat = danhSachNhan.filter(
    g => g.trangThai === 'HOAN_TAT' && Number(g.soTienThucNhan || 0) > 0
  ); const danhSachNhanHienThi = locLoai
    ? danhSachNhanDaDoiSoat.filter(g => g.loaiGiaoDich === locLoai)
    : danhSachNhanDaDoiSoat;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-receipt" aria-hidden="true"></i> Lịch sử giao dịch</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
      </div>

      <div style={{ padding: 20, maxWidth: 860, width: '100%', margin: '0 auto' }}>
        {loi && <div className="alert alert-danger">{loi}</div>}

        <div style={{ display: 'flex', gap: 6, marginBottom: 12 }}>
          {[['GUI', 'Đã gửi'], ['NHAN', 'Đã nhận']].map(([t, nhan]) => (
            <button
              key={t}
              className={`btn btn-sm ${tab === t ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setTab(t)}
            >
              {nhan}
            </button>
          ))}
        </div>

        {/* FIX: bộ lọc Khóa học/Lớp học trước đây chỉ hiện ở tab "Đã gửi" —
            giờ dùng chung cho cả 2 tab (bộ lọc locLoai đã dùng chung sẵn). */}
        <div style={{ display: 'flex', gap: 6, marginBottom: 16 }}>
          {[['', 'Tất cả'], ['KHOA_HOC', 'Khóa học'], ['LOP_HOC', 'Lớp học']].map(([gt, nhan]) => (
            <span key={gt} className={`dd-filter ${locLoai === gt ? 'active' : ''}`} onClick={() => setLocLoai(gt)}>
              {nhan}
            </span>
          ))}
        </div>

        {dangTai && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải lịch sử giao dịch...</p>}

        {!dangTai && tab === 'GUI' && danhSachHienThi.length === 0 && (
          <div className="empty-state">
            <i className="ti ti-receipt-off" style={{ fontSize: 28 }} aria-hidden="true"></i>
            <p style={{ marginTop: 6 }}>Chưa có giao dịch nào.</p>
          </div>
        )}

        {!dangTai && tab === 'NHAN' && danhSachNhanHienThi.length === 0 && (
          <div className="empty-state">
            <i className="ti ti-receipt-off" style={{ fontSize: 28 }} aria-hidden="true"></i>
            <p style={{ marginTop: 6 }}>
              {danhSachNhan.length === 0
                ? 'Chưa có khoản nhận nào — mục này chỉ hiện nếu bạn là tác giả khóa học hoặc giảng viên phụ trách lớp.'
                : 'Chưa có khoản nào đối soát xong khớp bộ lọc hiện tại. Các khoản đang chờ/đang xử lý đối soát chưa hiển thị ở đây.'}
            </p>
          </div>
        )}

        {tab === 'GUI' && danhSachHienThi.map(g => {
          const nhanTrangThai = TRANG_THAI_GIAO_DICH[g.trangThai] || { text: g.trangThai, mau: 'badge-gray' };
          const nhanHoanTien = g.trangThaiHoanTien ? TRANG_THAI_HOAN_TIEN[g.trangThaiHoanTien] : null;

          return (
            <div key={`${g.loaiGiaoDich}-${g.maPhieu}`} className="card sm" style={{ marginBottom: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10, flexWrap: 'wrap' }}>
                <div style={{ flex: 1, minWidth: 200 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    <span className="badge badge-blue">{g.loaiGiaoDich === 'KHOA_HOC' ? 'Khóa học' : 'Lớp học'}</span>
                    <span className={`badge ${nhanTrangThai.mau}`}>{nhanTrangThai.text}</span>
                  </div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{g.tieuDe}</div>
                  <div style={{ fontSize: 12, color: 'var(--text-3)' }}>
                    Mã GD: {g.maGiaoDichVnpay || '—'} · {formatNgay(g.ngayMuaThanhCong || g.ngayTao)}
                  </div>
                  {nhanHoanTien && (
                    <div style={{ marginTop: 6 }}>
                      <span className={`badge ${nhanHoanTien.mau}`}>
                        <i className="ti ti-receipt-refund" aria-hidden="true" style={{ marginRight: 4 }}></i>
                        {nhanHoanTien.text}
                      </span>
                    </div>
                  )}
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontWeight: 700, fontSize: 16 }}>{dinhDangTien(g.soTien)}</div>
                  {g.coTheYeuCauHoanTien && (
                    <button
                      className="btn btn-danger btn-sm"
                      style={{ marginTop: 8 }}
                      disabled={dangGuiHoanTien === g.maPhieu}
                      onClick={() => xuLyYeuCauHoanTien(g)}
                    >
                      {dangGuiHoanTien === g.maPhieu ? 'Đang gửi...' : 'Yêu cầu hoàn tiền'}
                    </button>
                  )}
                  {g.trangThai === 'THANH_CONG' && !g.trangThaiHoanTien && !g.coTheYeuCauHoanTien && g.hanGiuTien && (
                    <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 6 }}>
                      Đã hết hạn yêu cầu hoàn tiền
                    </div>
                  )}
                </div>
              </div>
            </div>
          );
        })}

        {tab === 'NHAN' && danhSachNhanHienThi.map(g => {
          const nhanTrangThai = TRANG_THAI_DOI_SOAT[g.trangThai] || { text: g.trangThai, mau: 'badge-gray' };
          return (
            <div key={`nhan-${g.loaiGiaoDich}-${g.maDoiSoat}`} className="card sm" style={{ marginBottom: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10, flexWrap: 'wrap' }}>
                <div style={{ flex: 1, minWidth: 200 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    <span className="badge badge-blue">{g.loaiGiaoDich === 'KHOA_HOC' ? 'Khóa học' : 'Lớp học'}</span>
                    <span className={`badge ${nhanTrangThai.mau}`}>{nhanTrangThai.text}</span>
                  </div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{g.tieuDe}</div>
                  <div style={{ fontSize: 12, color: 'var(--text-3)' }}>
                    Học viên trả: {dinhDangTien(g.soTienGiaoDichGoc)} · Hoa hồng nền tảng: {g.hoaHong}%
                    · {formatNgay(g.ngayChuyenThanhCong || g.ngayTao)}
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontWeight: 700, fontSize: 16, color: 'var(--success)' }}>
                    +{dinhDangTien(g.soTienThucNhan)}
                  </div>
                  <div style={{ fontSize: 11, color: 'var(--text-3)' }}>Thực nhận</div>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}