import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { layDanhSachNguoiDung, capNhatTrangThaiNguoiDung, thuHoiQuyenQuanTri, capLaiQuyenQuanTri } from '../../services/adminService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import '../../components/XemTepTinModal.css';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';

const VAI_TRO_NHAN = {
  QTV_CAO_CAP: 'QTV Cao cấp', QTV_NOI_DUNG: 'QTV Nội dung',
  QTV_TAI_CHINH: 'QTV Tài chính', NGUOI_DUNG: 'Người dùng',
};
const TRANG_THAI_BADGE = {
  HOAT_DONG: 'badge-green', BI_CAM: 'badge-red', CHO_XAC_NHAN: 'badge-amber',
};
const TRANG_THAI_NHAN = {
  HOAT_DONG: 'Hoạt động', BI_CAM: 'Bị cấm', CHO_XAC_NHAN: 'Chờ xác nhận',
};

export default function QuanLyNguoiDung() {
  const navigate = useNavigate();
  // FIX: đọc filter ban đầu từ query string (?vaiTro=...&trangThai=...) — cho
  // phép các trang khác (VD: Tổng quan) điều hướng thẳng tới đây với đúng bộ
  // lọc đã chọn sẵn, thay vì luôn mở ra danh sách rỗng bộ lọc. Trước đây
  // BangDieuKhienNoiDung.jsx đã có sẵn link "/admin/nguoi-dung?trangThai=BI_CAM"
  // nhưng trang này chưa đọc query string nên link đó thực ra chưa có tác dụng
  // lọc gì — tiện sửa luôn ở đây.
  const [searchParams] = useSearchParams();
  const vaiTroHienTai = localStorage.getItem('vaiTro');
  const laQTVCaoCap   = vaiTroHienTai === 'QTV_CAO_CAP';
  const maNguoiDungHienTai = Number(localStorage.getItem('maNguoiDung'));

  const [ds,        setDs]        = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 });
  const [timKiem,   setTimKiem]   = useState('');
  const [locVaiTro, setLocVaiTro] = useState(searchParams.get('vaiTro') || '');
  const [locTT,     setLocTT]     = useState(searchParams.get('trangThai') || '');
  const [dangTai,   setDangTai]   = useState(true);
  const [loi,       setLoi]       = useState('');
  const [ndChon,    setNdChon]    = useState(null);  // modal chi tiết
  const [dangCapNhat, setDangCapNhat] = useState(false);
  const [modalCam,  setModalCam]  = useState(null);  // { nd } — modal nhập lý do khi cấm
  const [modalCapQuyen, setModalCapQuyen] = useState(null); // { nd } — modal chọn vai trò khi cấp lại quyền

  const taiLai = useCallback((trangSo = 0) => {
    setDangTai(true); setLoi('');
    layDanhSachNguoiDung({ timKiem, vaiTro: locVaiTro, trangThai: locTT, page: trangSo, size: 20 })
      .then(setDs)
      .catch(err => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, [timKiem, locVaiTro, locTT]);

  useEffect(() => { taiLai(0); }, [taiLai]);

  // ttMoi === 'BI_CAM' -> mở modal bắt nhập lý do trước; 'HOAT_DONG' -> mở khoá luôn
  function xuLyDoiTrangThai(nd, ttMoi) {
    if (ttMoi === 'BI_CAM') {
      setModalCam({ nd });
      return;
    }
    if (!confirm(`Xác nhận mở khoá tài khoản "${nd.hoTen}"?`)) return;
    thucHienDoiTrangThai(nd, 'HOAT_DONG', null);
  }

  async function thucHienDoiTrangThai(nd, ttMoi, lyDo) {
    setDangCapNhat(true);
    try {
      const cap = await capNhatTrangThaiNguoiDung(nd.maNguoiDung, ttMoi, lyDo);
      setDs(prev => ({
        ...prev,
        content: prev.content.map(u => u.maNguoiDung === nd.maNguoiDung ? cap : u),
      }));
      if (ndChon?.maNguoiDung === nd.maNguoiDung) setNdChon(cap);
      setModalCam(null);
    } catch (err) {
      alert(layThongBaoLoi(err));
    } finally {
      setDangCapNhat(false);
    }
  }

  async function xuLyThuHoiQuyen(nd) {
    if (!confirm(`Thu hồi quyền quản trị viên của "${nd.hoTen}"? Tài khoản sẽ trở về vai trò Người dùng thường.`)) return;
    setDangCapNhat(true);
    try {
      const cap = await thuHoiQuyenQuanTri(nd.maNguoiDung);
      setDs(prev => ({
        ...prev,
        content: prev.content.map(u => u.maNguoiDung === nd.maNguoiDung ? cap : u),
      }));
      if (ndChon?.maNguoiDung === nd.maNguoiDung) setNdChon(cap);
    } catch (err) {
      alert(layThongBaoLoi(err));
    } finally {
      setDangCapNhat(false);
    }
  }

  async function thucHienCapLaiQuyen(nd, vaiTroMoi) {
    setDangCapNhat(true);
    try {
      const cap = await capLaiQuyenQuanTri(nd.maNguoiDung, vaiTroMoi);
      setDs(prev => ({
        ...prev,
        content: prev.content.map(u => u.maNguoiDung === nd.maNguoiDung ? cap : u),
      }));
      if (ndChon?.maNguoiDung === nd.maNguoiDung) setNdChon(cap);
      setModalCapQuyen(null);
    } catch (err) {
      alert(layThongBaoLoi(err));
    } finally {
      setDangCapNhat(false);
    }
  }

  const { navItems, roleLabel } = layNavTheoVaiTro();

  return (
    <DashboardShell
      roleLabel={roleLabel}
      navItems={navItems}
      crumb="Tài khoản người dùng"
      actions={<span className="badge badge-blue">{ds.totalElements} tài khoản</span>}
    >
      <div className="ds-page-head">
        <div><h1>Tài khoản người dùng</h1><p>Tìm kiếm, lọc và quản lý trạng thái tài khoản trong toàn hệ thống</p></div>
      </div>

      {/* Bộ lọc */}
      <div className="card sm" style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
        <input className="form-input" style={{ width: 240 }}
          placeholder="Tìm theo tên, email..."
          value={timKiem} onChange={e => setTimKiem(e.target.value)}
          onKeyDown={e => { if (e.key === 'Enter') taiLai(0); }} />

        <select className="form-input" style={{ width: 160 }}
          value={locVaiTro} onChange={e => { setLocVaiTro(e.target.value); }}>
          <option value="">Tất cả vai trò</option>
          {Object.entries(VAI_TRO_NHAN).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>

        <select className="form-input" style={{ width: 160 }}
          value={locTT} onChange={e => { setLocTT(e.target.value); }}>
          <option value="">Tất cả trạng thái</option>
          {Object.entries(TRANG_THAI_NHAN).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>

        <button className="btn btn-primary btn-sm" onClick={() => taiLai(0)}>
          <i className="ti ti-search" aria-hidden="true"></i> Tìm
        </button>
        <button className="btn btn-secondary btn-sm" onClick={() => { setTimKiem(''); setLocVaiTro(''); setLocTT(''); }}>
          Xoá lọc
        </button>
      </div>

      {/* Bảng dữ liệu */}
      <div>
        {loi && <div className="alert alert-danger">{loi}</div>}
        {dangTai && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>}

        {!dangTai && ds.content.length === 0 && (
          <div className="alert alert-info">Không tìm thấy tài khoản nào phù hợp.</div>
        )}

        {ds.content.length > 0 && (
          <table className="data-table">
            <thead>
              <tr>
                <th>Họ tên</th>
                <th>Email</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {ds.content.map(nd => (
                <tr key={nd.maNguoiDung}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <div className="avatar" style={{ width: 28, height: 28, fontSize: 11, flexShrink: 0 }}>
                        {nd.hoTen?.[0]?.toUpperCase()}
                      </div>
                      <span style={{ fontWeight: 500 }}>{nd.hoTen}</span>
                    </div>
                  </td>
                  <td style={{ color: 'var(--text-2)' }}>{nd.email}</td>
                  <td>
                    <span className={`badge ${nd.vaiTro === 'NGUOI_DUNG' ? 'badge-gray' : 'badge-blue'}`}>
                      {VAI_TRO_NHAN[nd.vaiTro] || nd.vaiTro}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${TRANG_THAI_BADGE[nd.trangThai]}`}>
                      {TRANG_THAI_NHAN[nd.trangThai]}
                    </span>
                  </td>
                  <td style={{ color: 'var(--text-3)', fontSize: 12 }}>
                    {nd.ngayTao ? new Date(nd.ngayTao).toLocaleDateString('vi-VN') : '—'}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                      <button className="btn btn-secondary btn-sm" onClick={() => setNdChon(nd)}>
                        Chi tiết
                      </button>
                      {/* Khoá/mở khoá: người dùng thường (mọi QTV) hoặc QTV Nội dung/Tài
                          chính (chỉ QTV Cao cấp) — không bao giờ hiện cho chính tài khoản
                          đang đăng nhập hoặc cho QTV Cao cấp khác, khớp đúng ràng buộc ở
                          QuanTriDichVuImpl.capNhatTrangThaiNguoiDung() */}
                      {nd.maNguoiDung !== maNguoiDungHienTai
                        && (nd.vaiTro === 'NGUOI_DUNG' || (laQTVCaoCap && nd.vaiTro !== 'QTV_CAO_CAP'))
                        && (
                        nd.trangThai === 'BI_CAM' ? (
                          <button className="btn btn-success btn-sm" disabled={dangCapNhat}
                            onClick={() => xuLyDoiTrangThai(nd, 'HOAT_DONG')}>
                            <i className="ti ti-lock-open" aria-hidden="true"></i> Mở khoá
                          </button>
                        ) : nd.trangThai === 'HOAT_DONG' ? (
                          <button className="btn btn-danger btn-sm" disabled={dangCapNhat}
                            onClick={() => xuLyDoiTrangThai(nd, 'BI_CAM')}>
                            <i className="ti ti-ban" aria-hidden="true"></i> Cấm
                          </button>
                        ) : null
                      )}
                      {/* Cấp quyền QTV — chỉ QTV Cao cấp, chỉ nhắm vào Người dùng thường đang hoạt động
                          (kể cả tài khoản từng bị thu hồi quyền trước đó) */}
                      {laQTVCaoCap && nd.vaiTro === 'NGUOI_DUNG' && nd.trangThai === 'HOAT_DONG' && (
                        <button className="btn btn-secondary btn-sm" disabled={dangCapNhat}
                          onClick={() => setModalCapQuyen({ nd })}>
                          <i className="ti ti-shield-plus" aria-hidden="true"></i> Cấp quyền
                        </button>
                      )}
                      {/* Thu hồi quyền QTV — chỉ QTV Cao cấp, chỉ nhắm vào QTV Nội dung/Tài chính */}
                      {laQTVCaoCap && (nd.vaiTro === 'QTV_NOI_DUNG' || nd.vaiTro === 'QTV_TAI_CHINH') && (
                        <button className="btn btn-danger btn-sm" disabled={dangCapNhat}
                          onClick={() => xuLyThuHoiQuyen(nd)}>
                          <i className="ti ti-shield-x" aria-hidden="true"></i> Thu hồi quyền
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {/* Phân trang */}
        {ds.totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginTop: 20 }}>
            <button className="btn btn-secondary btn-sm" disabled={ds.number === 0}
              onClick={() => taiLai(ds.number - 1)}>‹</button>
            {Array.from({ length: Math.min(ds.totalPages, 7) }, (_, i) => i).map(i => (
              <button key={i} className={`btn btn-sm ${i === ds.number ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => taiLai(i)}>{i + 1}</button>
            ))}
            <button className="btn btn-secondary btn-sm" disabled={ds.number >= ds.totalPages - 1}
              onClick={() => taiLai(ds.number + 1)}>›</button>
          </div>
        )}
      </div>

      {/* Modal chi tiết */}
      {ndChon && (
        <ModalChiTietNguoiDung nd={ndChon} laQTVCaoCap={laQTVCaoCap} maNguoiDungHienTai={maNguoiDungHienTai}
          dangCapNhat={dangCapNhat}
          onDong={() => setNdChon(null)}
          onDoiTrangThai={xuLyDoiTrangThai}
          onThuHoiQuyen={xuLyThuHoiQuyen}
          onMoModalCapQuyen={(nd) => setModalCapQuyen({ nd })} />
      )}

      {/* Modal nhập lý do khi cấm tài khoản */}
      {modalCam && (
        <ModalLyDoCam nd={modalCam.nd} dangCapNhat={dangCapNhat}
          onDong={() => setModalCam(null)}
          onXacNhan={(lyDo) => thucHienDoiTrangThai(modalCam.nd, 'BI_CAM', lyDo)} />
      )}

      {/* Modal chọn vai trò khi cấp lại quyền quản trị */}
      {modalCapQuyen && (
        <ModalCapQuyen nd={modalCapQuyen.nd} dangCapNhat={dangCapNhat}
          onDong={() => setModalCapQuyen(null)}
          onXacNhan={(vaiTroMoi) => thucHienCapLaiQuyen(modalCapQuyen.nd, vaiTroMoi)} />
      )}
    </DashboardShell>
  );
}

function ModalLyDoCam({ nd, dangCapNhat, onDong, onXacNhan }) {
  const [lyDo, setLyDo] = useState('');
  const [loi, setLoi] = useState('');

  function xacNhan() {
    if (!lyDo.trim()) {
      setLoi('Vui lòng nhập lý do khóa tài khoản');
      return;
    }
    onXacNhan(lyDo.trim());
  }

  return (
    <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) onDong(); }}>
      <div className="card" style={{ width: 420, margin: 0, padding: 20 }}>
        <div className="card-title" style={{ marginBottom: 10 }}>
          Cấm tài khoản "{nd.hoTen}"
        </div>
        {loi && <div className="alert alert-danger">{loi}</div>}
        <div className="form-group">
          <label className="form-label">Lý do (người dùng sẽ nhận được thông báo này)</label>
          <textarea className="form-input" style={{ height: 90, padding: 8 }}
            value={lyDo} onChange={e => setLyDo(e.target.value)}
            placeholder="Vd: Vi phạm điều khoản sử dụng — đăng nội dung spam nhiều lần."
            autoFocus />
        </div>
        <div className="btn-row" style={{ flexDirection: 'column' }}>
          <button className="btn btn-danger" style={{ width: '100%' }} disabled={dangCapNhat} onClick={xacNhan}>
            {dangCapNhat ? 'Đang xử lý...' : 'Xác nhận cấm tài khoản'}
          </button>
          <button className="btn btn-secondary" style={{ width: '100%' }} onClick={onDong}>Hủy</button>
        </div>
      </div>
    </div>
  );
}

function ModalCapQuyen({ nd, dangCapNhat, onDong, onXacNhan }) {
  const [vaiTro, setVaiTro] = useState('QTV_NOI_DUNG');

  return (
    <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) onDong(); }}>
      <div className="card" style={{ width: 420, margin: 0, padding: 20 }}>
        <div className="card-title" style={{ marginBottom: 10 }}>
          Cấp quyền quản trị cho "{nd.hoTen}"
        </div>
        <p style={{ fontSize: 13, color: 'var(--text-3)', marginTop: 0 }}>
          Tài khoản sẽ được nâng từ Người dùng thường lên vai trò quản trị đã chọn —
          áp dụng ngay từ request kế tiếp, không cần đăng nhập lại.
        </p>
        <div className="form-group">
          <label className="form-label">Vai trò cấp</label>
          <select className="form-input" value={vaiTro} onChange={e => setVaiTro(e.target.value)} autoFocus>
            <option value="QTV_NOI_DUNG">QTV Nội dung</option>
            <option value="QTV_TAI_CHINH">QTV Tài chính</option>
          </select>
        </div>
        <div className="btn-row" style={{ flexDirection: 'column' }}>
          <button className="btn btn-primary" style={{ width: '100%' }} disabled={dangCapNhat}
            onClick={() => onXacNhan(vaiTro)}>
            {dangCapNhat ? 'Đang xử lý...' : 'Xác nhận cấp quyền'}
          </button>
          <button className="btn btn-secondary" style={{ width: '100%' }} onClick={onDong}>Hủy</button>
        </div>
      </div>
    </div>
  );
}

function ModalChiTietNguoiDung({ nd, laQTVCaoCap, maNguoiDungHienTai, dangCapNhat, onDong, onDoiTrangThai, onThuHoiQuyen, onMoModalCapQuyen }) {
  const TRANG_THAI_BADGE = {
    HOAT_DONG: 'badge-green', BI_CAM: 'badge-red', CHO_XAC_NHAN: 'badge-amber',
  };
  const TRANG_THAI_NHAN = { HOAT_DONG: 'Hoạt động', BI_CAM: 'Bị cấm', CHO_XAC_NHAN: 'Chờ xác nhận' };
  const VAI_TRO_NHAN = {
    QTV_CAO_CAP: 'QTV Cao cấp', QTV_NOI_DUNG: 'QTV Nội dung',
    QTV_TAI_CHINH: 'QTV Tài chính', NGUOI_DUNG: 'Người dùng',
  };

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,.4)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
    }} onClick={e => { if (e.target === e.currentTarget) onDong(); }}>
      <div className="card" style={{ width: 420, margin: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
          <div className="avatar" style={{ width: 48, height: 48, fontSize: 18, flexShrink: 0 }}>
            {nd.hoTen?.[0]?.toUpperCase()}
          </div>
          <div>
            <div style={{ fontWeight: 600, fontSize: 15 }}>{nd.hoTen}</div>
            <div style={{ fontSize: 12, color: 'var(--text-3)' }}>#{nd.maNguoiDung}</div>
          </div>
          <button className="btn btn-secondary btn-sm" style={{ marginLeft: 'auto' }} onClick={onDong}>
            <i className="ti ti-x" aria-hidden="true"></i>
          </button>
        </div>

        <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
          {[
            ['Email', nd.email],
            ['Số điện thoại', nd.sdt || '—'],
            ['Vai trò', VAI_TRO_NHAN[nd.vaiTro] || nd.vaiTro],
            ['Trạng thái', <span className={`badge ${TRANG_THAI_BADGE[nd.trangThai]}`}>{TRANG_THAI_NHAN[nd.trangThai]}</span>],
            ['Ngày tạo', nd.ngayTao ? new Date(nd.ngayTao).toLocaleString('vi-VN') : '—'],
          ].map(([k, v]) => (
            <tr key={k} style={{ borderBottom: '1px solid var(--border)' }}>
              <td style={{ padding: '8px 0', color: 'var(--text-3)', width: 130 }}>{k}</td>
              <td style={{ padding: '8px 0', fontWeight: 500 }}>{v}</td>
            </tr>
          ))}
        </table>

        {nd.maNguoiDung !== maNguoiDungHienTai
          && (nd.vaiTro === 'NGUOI_DUNG' || (laQTVCaoCap && nd.vaiTro !== 'QTV_CAO_CAP')) && (
          <div className="btn-row" style={{ marginTop: 16 }}>
            {nd.trangThai === 'BI_CAM' ? (
              <button className="btn btn-success btn-sm" disabled={dangCapNhat}
                onClick={() => onDoiTrangThai(nd, 'HOAT_DONG')}>
                <i className="ti ti-lock-open" aria-hidden="true"></i> Mở khoá tài khoản
              </button>
            ) : nd.trangThai === 'HOAT_DONG' ? (
              <button className="btn btn-danger btn-sm" disabled={dangCapNhat}
                onClick={() => onDoiTrangThai(nd, 'BI_CAM')}>
                <i className="ti ti-ban" aria-hidden="true"></i> Cấm tài khoản
              </button>
            ) : null}
            {laQTVCaoCap && nd.vaiTro === 'NGUOI_DUNG' && nd.trangThai === 'HOAT_DONG' && (
              <button className="btn btn-secondary btn-sm" disabled={dangCapNhat}
                onClick={() => onMoModalCapQuyen(nd)}>
                <i className="ti ti-shield-plus" aria-hidden="true"></i> Cấp quyền quản trị
              </button>
            )}
          </div>
        )}

        {laQTVCaoCap && (nd.vaiTro === 'QTV_NOI_DUNG' || nd.vaiTro === 'QTV_TAI_CHINH') && (
          <div className="btn-row" style={{ marginTop: 16 }}>
            <button className="btn btn-danger btn-sm" disabled={dangCapNhat} onClick={() => onThuHoiQuyen(nd)}>
              <i className="ti ti-shield-x" aria-hidden="true"></i> Thu hồi quyền quản trị viên
            </button>
          </div>
        )}
      </div>
    </div>
  );
}