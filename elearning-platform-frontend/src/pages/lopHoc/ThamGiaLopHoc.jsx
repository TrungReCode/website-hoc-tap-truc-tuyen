import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { thamGiaBangMa, layLoiMoiCuaToi, chapNhanLoiMoi, tuChoiLoiMoi } from '../../services/ghiDanhService';
import { layLopGiangVien } from '../../services/lopHocService';
import { taoDonPhiLopHoc } from '../../services/thanhToanService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';

/**
 * Trang gộp "Tham gia lớp học" + "Thanh toán phí lớp học".
 *
 * Trước đây đây là 2 trang riêng biệt (/lop-hoc/tham-gia và /lop-hoc/thanh-toan)
 * không chia sẻ trạng thái: khi học viên nhập mã lớp ở trang Tham gia và bị chặn
 * vì lớp có phí (CAN_THANH_TOAN_PHI_LOP), bấm "Thanh toán phí lớp học" sẽ điều
 * hướng sang trang Thanh toán với ô nhập mã lớp TRỐNG, buộc gõ lại mã lớp.
 *
 * Gộp lại thành 1 trang với 2 tab dùng chung state `maLop`: chuyển tab không mất
 * mã đã nhập. Cả 2 route cũ vẫn trỏ về component này (xem App.jsx), khác nhau ở
 * tab mặc định; ai giữ link/bookmark cũ vẫn hoạt động bình thường.
 */
export default function ThamGiaLopHoc() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const tabMacDinh = location.pathname === '/lop-hoc/thanh-toan' ? 'thanh-toan' : 'tham-gia';
  const [tab, setTab] = useState(tabMacDinh);

  // State dùng chung cho cả 2 tab — đây là điểm mấu chốt của việc gộp trang:
  // mã lớp gõ ở tab này vẫn còn nguyên khi chuyển sang tab kia.
  const [maLop, setMaLop] = useState(() => (searchParams.get('ma') || '').toUpperCase());

  const [danhSachLopGiangVien, setDanhSachLopGiangVien] = useState([]);
  const [dangXacDinhLopGiangVien, setDangXacDinhLopGiangVien] = useState(true);
  const [danhSachLoiMoi, setDanhSachLoiMoi] = useState([]);
  const [dangXuLy, setDangXuLy] = useState(false);
  const [loi, setLoi] = useState('');
  const [maLoiCode, setMaLoiCode] = useState(null);
  // Mã khóa học cần mua khi lỗi CHUA_SO_HUU_KHOA_HOC_GOC — backend trả kèm
  // trong PhanHoiLoi.duLieu.maKhoaHoc để điều hướng thẳng tới đúng khóa học
  // thay vì đưa chung chung về Chợ khóa học.
  const [maKhoaHocCanMua, setMaKhoaHocCanMua] = useState(null);

  const [dangTaoDon, setDangTaoDon] = useState(false);
  const [loiThanhToan, setLoiThanhToan] = useState('');

  // Đến từ trang Kết quả thanh toán sau khi đóng phí lớp thành công (nút "Vào
  // Lớp Học Ngay") — thanh toán chỉ thỏa điều kiện phí, chưa tự ghi danh, nên
  // trang này cần tự động gọi tham gia thay người dùng thay vì bắt bấm lại.
  const tuDongThamGia = searchParams.get('tuDong') === '1';
  const [daTuDongThamGia, setDaTuDongThamGia] = useState(false);

  const maLopNhap = maLop.trim().toUpperCase();
  const lopGiangVienCuaMa = useMemo(
    () => danhSachLopGiangVien.find(lop => String(lop.maLop || '').toUpperCase() === maLopNhap),
    [danhSachLopGiangVien, maLopNhap]
  );

  function moTrangQuanLyLop(lop) {
    navigate(`/lop-hoc/${lop.maLopHoc}/quan-ly?tab=thanh-vien`, { replace: true });
  }

  // Nếu route đổi (VD: bấm nút "Thanh toán" trong alert lỗi tự chuyển tab bằng
  // navigate), đồng bộ lại tab hiển thị theo pathname hiện tại.
  useEffect(() => {
    setTab(location.pathname === '/lop-hoc/thanh-toan' ? 'thanh-toan' : 'tham-gia');
  }, [location.pathname]);

  useEffect(() => {
    let daHuy = false;

    Promise.all([
      layLopGiangVien().catch(() => []),
      layLoiMoiCuaToi().catch(() => []),
    ])
      .then(([dsLopGiangVien, dsLoiMoi]) => {
        if (daHuy) return;
        setDanhSachLopGiangVien(dsLopGiangVien || []);
        setDanhSachLoiMoi(dsLoiMoi || []);

        const maDangNhap = (searchParams.get('ma') || '').trim().toUpperCase();
        const lopGiangVien = (dsLopGiangVien || []).find(lop => String(lop.maLop || '').toUpperCase() === maDangNhap);
        if (lopGiangVien) {
          moTrangQuanLyLop(lopGiangVien);
        }
      })
      .finally(() => {
        if (!daHuy) setDangXacDinhLopGiangVien(false);
      });

    return () => { daHuy = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [navigate]);

  /** Chuyển sang tab "Thanh toán phí lớp học", GIỮ NGUYÊN mã lớp đang có trong ô nhập. */
  function moTabThanhToan() {
    setLoiThanhToan('');
    setTab('thanh-toan');
    navigate(`/lop-hoc/thanh-toan${maLopNhap ? `?ma=${maLopNhap}` : ''}`, { replace: true });
  }

  /** Logic tham gia dùng chung cho cả bấm nút thủ công và tự động sau khi thanh toán. */
  async function thucHienThamGia(ma) {
    setLoi(''); setMaLoiCode(null); setMaKhoaHocCanMua(null);
    setDangXuLy(true);
    try {
      const lop = await thamGiaBangMa(ma);
      navigate(`/lop-hoc/${lop.maLopHoc}`);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
      setMaLoiCode(err?.response?.data?.maLoi || null);
      setMaKhoaHocCanMua(err?.response?.data?.duLieu?.maKhoaHoc ?? null);
    } finally {
      setDangXuLy(false);
    }
  }

  async function xuLyThamGia(e) {
    e.preventDefault();
    if (!maLopNhap || dangXacDinhLopGiangVien) return;

    if (lopGiangVienCuaMa) {
      moTrangQuanLyLop(lopGiangVienCuaMa);
      return;
    }

    thucHienThamGia(maLopNhap);
  }
  // Tự động tham gia khi vào trang với ?tuDong=1 (từ nút "Vào Lớp Học Ngay" sau
  // khi thanh toán phí lớp thành công). Chờ xác định xong danh sách lớp giảng
  // viên (tránh vô tình tự tham gia vào lớp mình đang phụ trách), chỉ chạy 1 lần.
  useEffect(() => {
    if (!tuDongThamGia || daTuDongThamGia) return;
    if (dangXacDinhLopGiangVien) return;
    if (tab !== 'tham-gia' || maLopNhap.length !== 8) return;
    if (lopGiangVienCuaMa) return; // effect khác đã tự chuyển sang trang quản lý lớp

    setDaTuDongThamGia(true);
    thucHienThamGia(maLopNhap);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tuDongThamGia, daTuDongThamGia, dangXacDinhLopGiangVien, tab, maLopNhap, lopGiangVienCuaMa]);

  async function xuLyChapNhan(maLoiMoi) {
    setLoi(''); setMaLoiCode(null); setMaKhoaHocCanMua(null);
    try {
      const lop = await chapNhanLoiMoi(maLoiMoi);
      navigate(`/lop-hoc/${lop.maLopHoc}`);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
      setMaLoiCode(err?.response?.data?.maLoi || null);
      setMaKhoaHocCanMua(err?.response?.data?.duLieu?.maKhoaHoc ?? null);
    }
  }

  async function xuLyTuChoi(maLoiMoi) {
    if (!window.confirm('Bạn chắc chắn muốn từ chối lời mời này?')) return;
    try {
      await tuChoiLoiMoi(maLoiMoi);
      setDanhSachLoiMoi(ds => ds.filter(m => m.maLoiMoi !== maLoiMoi));
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    }
  }

  async function xuLyThanhToan(e) {
    e.preventDefault();
    if (!maLopNhap) {
      setLoiThanhToan('Vui lòng nhập mã lớp hợp lệ (VD: 8RUV99E6).');
      return;
    }

    setLoiThanhToan('');
    setDangTaoDon(true);
    try {
      const don = await taoDonPhiLopHoc(maLopNhap);
      if (don?.urlThanhToan) {
        window.location.href = don.urlThanhToan;
        return;
      }
      setLoiThanhToan('Không nhận được đường dẫn thanh toán từ hệ thống.');
    } catch (err) {
      setLoiThanhToan(layThongBaoLoi(err));
    } finally {
      setDangTaoDon(false);
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title">
          <i className={tab === 'thanh-toan' ? 'ti ti-receipt-2' : 'ti ti-door-enter'} aria-hidden="true"></i>{' '}
          {tab === 'thanh-toan' ? 'Thanh toán phí lớp học' : 'Tham gia lớp học'}
        </span>
        <button className="btn btn-primary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
      </div>

      <div style={{ padding: 24, maxWidth: 600, margin: '0 auto', width: '100%' }}>
        {/* Tab switcher — 1 trang, 2 chế độ, dùng chung ô mã lớp */}
        <div className="tabs" style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
          <button
            type="button"
            className={`btn btn-sm ${tab === 'tham-gia' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => { setTab('tham-gia'); navigate(`/lop-hoc/tham-gia${maLopNhap ? `?ma=${maLopNhap}` : ''}`, { replace: true }); }}
          >
            <i className="ti ti-door-enter" aria-hidden="true"></i> Tham gia lớp
          </button>
          <button
            type="button"
            className={`btn btn-sm ${tab === 'thanh-toan' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={moTabThanhToan}
          >
            <i className="ti ti-receipt-2" aria-hidden="true"></i> Thanh toán phí lớp
          </button>
        </div>

        {tab === 'tham-gia' && (
          <>
            {loi && (
              <div className="alert alert-danger" style={{ display: 'flex', flexDirection: 'column', gap: 8, alignItems: 'flex-start' }}>
                <span>{loi}</span>
                {maLoiCode === 'CHUA_SO_HUU_KHOA_HOC_GOC' && (
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => navigate(maKhoaHocCanMua ? `/khoa-hoc/${maKhoaHocCanMua}` : '/cho-khoa-hoc')}
                  >
                    <i className="ti ti-shopping-cart" aria-hidden="true"></i> Mua khóa học ngay
                  </button>
                )}
                {maLoiCode === 'CAN_THANH_TOAN_PHI_LOP' && (
                  // Mã lớp vừa nhập ở tab này (maLopNhap) được giữ nguyên trong state
                  // dùng chung, nên khi mở tab Thanh toán, ô nhập đã có sẵn mã lớp,
                  // không cần gõ lại.
                  <button className="btn btn-primary btn-sm" onClick={moTabThanhToan}>
                    <i className="ti ti-credit-card" aria-hidden="true"></i> Thanh toán phí lớp học
                  </button>
                )}
              </div>
            )}

            {lopGiangVienCuaMa && (
              <div className="alert alert-warning" style={{ display: 'flex', flexDirection: 'column', gap: 8, alignItems: 'flex-start' }}>
                <span>Bạn đang là giảng viên của lớp này nên không thể tham gia như học viên hoặc thanh toán phí lớp học.</span>
                <button className="btn btn-primary btn-sm" onClick={() => moTrangQuanLyLop(lopGiangVienCuaMa)}>
                  <i className="ti ti-settings" aria-hidden="true"></i> Đi đến quản lý lớp
                </button>
              </div>
            )}

            <div className="card">
              <div className="card-title">Nhập mã lớp</div>
              <p style={{ fontSize: 13, color: 'var(--text-2)', marginBottom: 14 }}>
                Bạn có mã lớp 8 ký tự do Giảng viên cung cấp? Nhập vào đây để tham gia.
              </p>
              <form onSubmit={xuLyThamGia} style={{ display: 'flex', gap: 8 }}>
                <input
                  className="form-input"
                  placeholder="VD: ABCD1234"
                  value={maLop}
                  onChange={e => setMaLop(e.target.value.toUpperCase())}
                  maxLength={8}
                  style={{ textTransform: 'uppercase', letterSpacing: 3, fontFamily: 'monospace', fontSize: 16 }}
                />
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={dangXuLy || dangXacDinhLopGiangVien || maLopNhap.length !== 8 || Boolean(lopGiangVienCuaMa)}
                >
                  {dangXacDinhLopGiangVien ? 'Đang kiểm tra...' : dangXuLy ? 'Đang tham gia...' : 'Tham gia'}
                </button>
              </form>
              {lopGiangVienCuaMa && (
                <p style={{ fontSize: 12, color: 'var(--warning, #d97706)', marginTop: 10 }}>
                  Mã này thuộc lớp do bạn phụ trách, hệ thống sẽ chuyển sang trang quản lý lớp thay vì ghi danh.
                </p>
              )}
            </div>

            {danhSachLoiMoi.length > 0 && (
              <div className="card">
                <div className="card-title">Lời mời đang chờ xác nhận ({danhSachLoiMoi.length})</div>
                {danhSachLoiMoi.map(m => (
                  <div key={m.maLoiMoi} className="invite-row">
                    <div className="invite-email" style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                      <span style={{ fontWeight: 500, fontSize: 13 }}>{m.tenKhoaHoc || 'Lớp học'}</span>
                      <span style={{ fontSize: 12, color: 'var(--text-2)' }}>GV: {m.tenGiangVien || '—'} · Mã: {m.maLop}</span>
                    </div>
                    <span className="invite-date">HH: {new Date(m.ngayHetHan).toLocaleDateString('vi-VN')}</span>
                    <div style={{ display: 'flex', gap: 6 }}>
                      <button className="btn btn-success btn-sm" onClick={() => xuLyChapNhan(m.maLoiMoi)}>Đồng ý</button>
                      <button className="btn btn-danger btn-sm" onClick={() => xuLyTuChoi(m.maLoiMoi)}>Từ chối</button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {danhSachLoiMoi.length === 0 && (
              <div className="empty-state" style={{ marginTop: 12 }}>
                <i className="ti ti-mail" style={{ fontSize: 28 }} aria-hidden="true"></i>
                <p style={{ marginTop: 6 }}>Không có lời mời nào đang chờ xác nhận.</p>
              </div>
            )}
          </>
        )}

        {tab === 'thanh-toan' && (
          <>
            {loiThanhToan && <div className="alert alert-danger">{loiThanhToan}</div>}

            <div className="card">
              <div className="card-title">Nhập mã lớp</div>
              <p style={{ fontSize: 13, color: 'var(--text-2)', marginBottom: 14 }}>
                Nhập mã lớp (VD: 8RUV99E6) để tạo đơn thanh toán.
              </p>

              <form onSubmit={xuLyThanhToan} style={{ display: 'grid', gap: 12 }}>
                <label style={{ display: 'grid', gap: 6 }}>
                  <span style={{ fontSize: 13, color: 'var(--text-3)' }}>Mã lớp</span>
                  <input
                    className="form-input"
                    placeholder="VD: 8RUV99E6"
                    // Ô nhập dùng chung state `maLop` với tab Tham gia: nếu người
                    // dùng vừa gõ/bị chặn ở tab Tham gia rồi bấm sang đây, mã lớp
                    // đã được đổ sẵn, không phải nhập lại.
                    value={maLop}
                    onChange={(e) => setMaLop(e.target.value.toUpperCase())}
                    style={{ fontSize: 16, letterSpacing: 1, textTransform: 'uppercase' }}
                  />
                </label>

                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                  <button type="submit" className="btn btn-primary" disabled={dangTaoDon}>
                    {dangTaoDon ? 'Đang tạo đơn...' : 'Thanh toán bằng VNPay'}
                  </button>
                </div>
              </form>
            </div>
          </>
        )}
      </div>
    </div>
  );
}