import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import {
  layDanhSachBaiDang, layChiTietBaiDang, taoBaiDang, capNhatBaiDang, xoaBaiDang, danhDauGiaiDap,
  layBinhLuan, taoBinhLuan, xoaBinhLuan, taiLenTepDinhKem, nopBaiTap, chamDiem, layDanhSachNopBai,
} from '../../services/traoDoiService';
import { layLopHocVien, layChiTietGiangVien } from '../../services/lopHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import ThanhTienDoTaiLen from '../../components/ThanhTienDoTaiLen';

const LOAI_NHAN = { THONG_BAO: 'Thông báo', BAI_TAP: 'Bài tập', HOI_DAP: 'Hỏi đáp', TAI_LIEU: 'Tài liệu hỗ trợ', THAO_LUAN: 'Thảo luận chuyên đề' };
const LOAI_BADGE = { THONG_BAO: 'badge-blue', BAI_TAP: 'badge-red', HOI_DAP: 'badge-amber', TAI_LIEU: 'badge-green', THAO_LUAN: 'badge-gray' };
const LOAI_ICON  = { THONG_BAO: 'ti-speakerphone', BAI_TAP: 'ti-clipboard-list', HOI_DAP: 'ti-help-circle', TAI_LIEU: 'ti-paperclip', THAO_LUAN: 'ti-messages' };

export default function DienDanLopHoc() {
  const { maLopHoc } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const maBaiDangMoTuThongBao = location.state?.maBaiDangMo;

  const maNguoiDungHienTai = (() => {
    const s = localStorage.getItem('maNguoiDung');
    return s ? Number(s) : null;
  })();

  // ── Vai trò trong lớp này + ngữ cảnh ──
  const [laGiangVien, setLaGiangVien] = useState(false);
  const [dangXacDinhVaiTro, setDangXacDinhVaiTro] = useState(true);
  const [ngoiCanh, setNgoiCanh] = useState(null);

  useEffect(() => {
    let daHuy = false;
    setDangXacDinhVaiTro(true);
    async function taiNgoiCanhVaVaiTro() {
      try {
        const lop = await layChiTietGiangVien(maLopHoc);
        if (!daHuy) { 
          setLaGiangVien(true); 
          setNgoiCanh(lop); 
          setLoaiBD('THONG_BAO'); // Cập nhật mặc định cho giảng viên
        }
      } catch {
        try {
          const lop = (await layLopHocVien()).find(l => String(l.maLopHoc) === String(maLopHoc));
          if (!daHuy) { 
            setLaGiangVien(false); 
            if (lop) setNgoiCanh(lop); 
          }
        } catch {
          // Lỗi lấy ngữ cảnh
        }
      } finally {
        if (!daHuy) setDangXacDinhVaiTro(false);
      }
    }
    taiNgoiCanhVaVaiTro();
    return () => { daHuy = true; };
  }, [maLopHoc]);

  // ── State danh sách bài đăng ──
  const [danhSach,  setDanhSach]  = useState([]);
  const [trang,     setTrang]     = useState(0);
  const [hetTrang,  setHetTrang]  = useState(false);
  const [locLoai,   setLocLoai]   = useState('');
  const [dangTai,   setDangTai]   = useState(false);
  const [loiDanhSach, setLoiDanhSach] = useState('');

  // ── State bài đang mở ──
  const [baiMo,         setBaiMo]         = useState(null);
  const [dsBinhLuan,    setDsBinhLuan]    = useState([]);
  const [dangTaiBL,     setDangTaiBL]     = useState(false);

  // ── State sửa bài đăng ──
  const [dangSuaBai,  setDangSuaBai]  = useState(false);
  const [tieuDeSua,   setTieuDeSua]   = useState('');
  const [noiDungSua,  setNoiDungSua]  = useState('');
  const [dangLuuSua,  setDangLuuSua]  = useState(false);
  const [loiSua,      setLoiSua]      = useState('');

  // ── State form tạo bài mới ──
  const [hieuForm,  setHieuForm]  = useState(false);
  const [loaiBD,    setLoaiBD]    = useState('HOI_DAP');
  const [tieuDe,    setTieuDe]    = useState('');
  const [noiDung,   setNoiDung]   = useState('');
  const [dangGui,   setDangGui]   = useState(false);
  const [loiForm,   setLoiForm]   = useState('');
  const [tepDinhKem,   setTepDinhKem]   = useState([]);
  const [dangTaiTep,   setDangTaiTep]   = useState(false);
  const [tienDoTep,    setTienDoTep]    = useState(null);

  // ── State form bình luận ──
  const [noiDungBL,    setNoiDungBL]    = useState('');
  const [maChaReply,   setMaChaReply]   = useState(null);
  const [noiDungReply, setNoiDungReply] = useState('');
  const [dangGuiBL,    setDangGuiBL]    = useState(false);

  const cuoiDanhSachRef = useRef(null);

  // ── Load danh sách bài đăng ──
  useEffect(() => {
    setDanhSach([]);
    setTrang(0);
    setHetTrang(false);
  }, [locLoai]);

  useEffect(() => {
    setDangTai(true);
    setLoiDanhSach('');
    layDanhSachBaiDang(maLopHoc, { loai: locLoai || undefined, page: trang, size: 10 })
      .then(res => {
        setDanhSach(prev => trang === 0 ? res.noiDung : [...prev, ...res.noiDung]);
        setHetTrang(res.laTrangCuoi);
      })
      .catch(err => setLoiDanhSach(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, [maLopHoc, locLoai, trang]);

  // ── Load bình luận khi mở bài ──
  useEffect(() => {
    if (!baiMo) return;
    setDangTaiBL(true);
    setDangSuaBai(false);
    layBinhLuan(baiMo.maBaiDang)
      .then(setDsBinhLuan)
      .catch(() => setDsBinhLuan([]))
      .finally(() => setDangTaiBL(false));
  }, [baiMo?.maBaiDang]);

  async function xuLyChonTepDinhKem(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setDangTaiTep(true);
    setTienDoTep(0);
    setLoiForm('');
    try {
      const tep = await taiLenTepDinhKem(maLopHoc, file, (p) => setTienDoTep(p >= 100 ? null : p));
      setTepDinhKem(prev => [...prev, tep]);
    } catch (err) {
      setLoiForm(layThongBaoLoi(err));
    } finally {
      setDangTaiTep(false);
      setTienDoTep(null);
      e.target.value = '';
    }
  }

  function xuLyXoaTepDinhKem(idx) {
    setTepDinhKem(prev => prev.filter((_, i) => i !== idx));
  }

  async function xuLyTaoBai(e) {
    e.preventDefault();
    if (!tieuDe.trim()) { setLoiForm('Tiêu đề không được để trống'); return; }
    setDangGui(true); setLoiForm('');
    try {
      const baiMoi = await taoBaiDang(maLopHoc, { loai: loaiBD, tieuDe: tieuDe.trim(), noiDung, tepDinhKem });
      setDanhSach(prev => [baiMoi, ...prev]);
      // Khôi phục form sau khi đăng thành công (Giảng viên: THÔNG BÁO, Học viên: HỎI ĐÁP)
      setHieuForm(false); setTieuDe(''); setNoiDung(''); setLoaiBD(laGiangVien ? 'THONG_BAO' : 'HOI_DAP'); setTepDinhKem([]);
    } catch (err) {
      setLoiForm(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  }

  // (Các hàm xử lý khác giữ nguyên: xoá, sửa bài, giải đáp, bình luận...)
  async function xuLyXoaBai(maBaiDang) {
    if (!confirm('Xoá bài đăng này?')) return;
    try {
      await xoaBaiDang(maBaiDang);
      setDanhSach(prev => prev.filter(b => b.maBaiDang !== maBaiDang));
      if (baiMo?.maBaiDang === maBaiDang) setBaiMo(null);
    } catch (err) {
      alert(layThongBaoLoi(err));
    }
  }

  function batDauSuaBai() {
    setTieuDeSua(baiMo.tieuDe);
    setNoiDungSua(baiMo.noiDung || '');
    setLoiSua('');
    setDangSuaBai(true);
  }

  async function xuLyLuuSuaBai(e) {
    e.preventDefault();
    if (!tieuDeSua.trim()) { setLoiSua('Tiêu đề không được để trống'); return; }
    setDangLuuSua(true);
    setLoiSua('');
    try {
      const capNhat = await capNhatBaiDang(baiMo.maBaiDang, {
        tieuDe: tieuDeSua.trim(), noiDung: noiDungSua, tepDinhKem: baiMo.tepDinhKem || [],
      });
      setBaiMo(capNhat);
      setDanhSach(prev => prev.map(b => b.maBaiDang === capNhat.maBaiDang ? capNhat : b));
      setDangSuaBai(false);
    } catch (err) {
      setLoiSua(layThongBaoLoi(err));
    } finally {
      setDangLuuSua(false);
    }
  }

  async function xuLyGiaiDap(maBaiDang, giaiDap) {
    try {
      const cap = await danhDauGiaiDap(maBaiDang, giaiDap);
      setDanhSach(prev => prev.map(b => b.maBaiDang === maBaiDang ? cap : b));
      if (baiMo?.maBaiDang === maBaiDang) setBaiMo(cap);
    } catch (err) {
      alert(layThongBaoLoi(err));
    }
  }

  async function xuLyGuiBinhLuan(e) {
    e.preventDefault();
    if (!noiDungBL.trim()) return;
    setDangGuiBL(true);
    try {
      const bl = await taoBinhLuan(baiMo.maBaiDang, { noiDung: noiDungBL.trim() });
      setDsBinhLuan(prev => [...prev, { ...bl, danhSachReply: [] }]);
      setNoiDungBL('');
      setDanhSach(prev => prev.map(b => b.maBaiDang === baiMo.maBaiDang
        ? { ...b, soBinhLuan: b.soBinhLuan + 1 } : b));
    } catch (err) {
      alert(layThongBaoLoi(err));
    } finally {
      setDangGuiBL(false);
    }
  }

  async function xuLyGuiReply(maCha) {
    if (!noiDungReply.trim()) return;
    setDangGuiBL(true);
    try {
      const bl = await taoBinhLuan(baiMo.maBaiDang, { noiDung: noiDungReply.trim(), maCha });
      setDsBinhLuan(prev => prev.map(parent =>
        parent.maBinhLuan === maCha
          ? { ...parent, danhSachReply: [...(parent.danhSachReply || []), bl] }
          : parent));
      setMaChaReply(null); setNoiDungReply('');
    } catch (err) {
      alert(layThongBaoLoi(err));
    } finally {
      setDangGuiBL(false);
    }
  }

  async function xuLyXoaBL(maBinhLuan, maCha) {
    if (!confirm('Xoá bình luận này?')) return;
    try {
      await xoaBinhLuan(maBinhLuan);
      if (maCha) {
        setDsBinhLuan(prev => prev.map(p => p.maBinhLuan === maCha
          ? { ...p, danhSachReply: p.danhSachReply.filter(r => r.maBinhLuan !== maBinhLuan) }
          : p));
      } else {
        setDsBinhLuan(prev => prev.filter(b => b.maBinhLuan !== maBinhLuan));
      }
    } catch (err) {
      alert(layThongBaoLoi(err));
    }
  }

  useEffect(() => {
    if (!maBaiDangMoTuThongBao) return;
    let daHuy = false;
    layChiTietBaiDang(maBaiDangMoTuThongBao)
      .then(b => { if (!daHuy) setBaiMo(b); })
      .catch(() => {});
    return () => { daHuy = true; };
  }, [maBaiDangMoTuThongBao]);

  function diaChi(loai) {
    return locLoai === loai ? '' : loai;
  }

  // ────────────────── RENDER ──────────────────
  return (
    <div className="dd-shell">
      {/* ... Thanh ngữ cảnh ... */}
      <div className="dd-context">
        <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/lop-hoc/${maLopHoc}`)}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i>
        </button>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div className="dd-title">
            <i className="ti ti-messages" aria-hidden="true"></i>
            {ngoiCanh?.tenKhoaHoc || 'Diễn đàn lớp học'}
          </div>
          {ngoiCanh && <div className="dd-sub">Mã lớp {ngoiCanh.maLop} · {ngoiCanh.tenGiangVien}</div>}
        </div>
        {!dangXacDinhVaiTro && (
          <span className={`t-badge ${laGiangVien ? 'amber' : ''}`}>
            <i className={`ti ${laGiangVien ? 'ti-chalkboard' : 'ti-user'}`} aria-hidden="true"></i>
            {laGiangVien ? 'Giảng viên' : 'Học viên'}
          </span>
        )}
      </div>

      <div style={{ display: 'flex', gap: 0, flex: 1, overflow: 'hidden' }}>
        <div style={{ flex: 1, borderRight: '1px solid var(--border)', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>

          <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--border)', flexShrink: 0 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 10 }}>
              <span style={{ fontWeight: 600, fontSize: 14 }}>Bài đăng</span>
              <button className="btn btn-primary btn-sm" style={{ marginLeft: 'auto' }}
                onClick={() => setHieuForm(!hieuForm)}>
                <i className="ti ti-plus" aria-hidden="true"></i> Đăng bài
              </button>
            </div>

            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
              {['', 'THONG_BAO', 'BAI_TAP', 'HOI_DAP', 'TAI_LIEU', 'THAO_LUAN'].map(l => (
                <span key={l} className={`dd-filter ${locLoai === l ? 'active' : ''}`}
                  onClick={() => setLocLoai(diaChi(l))}>
                  {l && <i className={`ti ${LOAI_ICON[l]}`} aria-hidden="true"></i>}
                  {l ? LOAI_NHAN[l] : 'Tất cả'}
                </span>
              ))}
            </div>
          </div>

          {/* Form tạo bài */}
          {hieuForm && (
            <div className="dd-composer">
              {loiForm && <div className="alert alert-danger" style={{ marginBottom: 10 }}>{loiForm}</div>}
              <div style={{ display: 'flex', gap: 8, marginBottom: 10 }}>
                {/* Đã xoá HOI_DAP khỏi mảng tuỳ chọn của Giảng viên */}
                {(laGiangVien
                  ? ['THONG_BAO', 'BAI_TAP', 'TAI_LIEU', 'THAO_LUAN']
                  : ['HOI_DAP', 'TAI_LIEU', 'THAO_LUAN']
                ).map(l => (
                  <button key={l} className={`btn btn-sm ${loaiBD === l ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => setLoaiBD(l)}>
                    <i className={`ti ${LOAI_ICON[l]}`} aria-hidden="true"></i> {LOAI_NHAN[l]}
                  </button>
                ))}
              </div>
              <div className="form-group">
                <input className="form-input" placeholder="Tiêu đề bài đăng..."
                  value={tieuDe} onChange={e => setTieuDe(e.target.value)} />
              </div>
              <div className="form-group">
                <textarea className="form-input" rows={3}
                  style={{ height: 'auto', resize: 'vertical', padding: '8px 10px' }}
                  placeholder="Nội dung (tuỳ chọn)..."
                  value={noiDung} onChange={e => setNoiDung(e.target.value)} />
              </div>

              <div className="form-group">
                <label className="form-label">Tệp đính kèm (tuỳ chọn, tối đa 20MB/tệp)</label>
                {tepDinhKem.map((tep, idx) => (
                  <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12.5, padding: '4px 0' }}>
                    <i className="ti ti-paperclip" aria-hidden="true"></i>
                    <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tep.ten}</span>
                    <button type="button" onClick={() => xuLyXoaTepDinhKem(idx)}
                      style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--danger)' }}>
                      <i className="ti ti-x" aria-hidden="true"></i>
                    </button>
                  </div>
                ))}
                <label className="btn btn-secondary btn-sm" style={{ display: 'inline-flex', cursor: 'pointer' }}>
                  <i className="ti ti-upload" aria-hidden="true"></i>
                  {dangTaiTep ? ' Đang tải lên...' : ' Đính kèm tệp'}
                  <input type="file" style={{ display: 'none' }} disabled={dangTaiTep} onChange={xuLyChonTepDinhKem} />
                </label>
                {dangTaiTep && <ThanhTienDoTaiLen phanTram={tienDoTep} nhan={tienDoTep == null ? 'Đang xử lý...' : 'Đang tải lên...'} />}
              </div>

              <div style={{ display: 'flex', gap: 8 }}>
                <button className="btn btn-primary btn-sm" onClick={xuLyTaoBai} disabled={dangGui}>
                  {dangGui ? 'Đang gửi...' : 'Đăng bài'}
                </button>
                <button className="btn btn-secondary btn-sm" onClick={() => setHieuForm(false)}>Huỷ</button>
              </div>
            </div>
          )}

          {/* ... Phần render danh sách bài và chi tiết giữ nguyên ... */}
          <div style={{ flex: 1, overflowY: 'auto', padding: 12 }}>
            {loiDanhSach && <div className="alert alert-danger">{loiDanhSach}</div>}

            {danhSach.length === 0 && !dangTai && (
              <div className="empty-state" style={{ marginTop: 40 }}>
                <i className="ti ti-message-off" style={{ fontSize: 32 }} aria-hidden="true"></i>
                <p style={{ marginTop: 8, fontSize: 13 }}>Chưa có bài đăng nào. Hãy là người đầu tiên đăng bài!</p>
              </div>
            )}

            {danhSach.map(b => (
              <div key={b.maBaiDang}
                className={`dd-post ${baiMo?.maBaiDang === b.maBaiDang ? 'active' : ''}`}
                onClick={() => setBaiMo(b)}>

                <div style={{ display: 'flex', alignItems: 'flex-start', gap: 8 }}>
                  <div className="avatar" style={{ width: 28, height: 28, fontSize: 11, flexShrink: 0 }}>
                    {b.tenNguoiDang?.[0]?.toUpperCase() || '?'}
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
                      <span className={`badge ${LOAI_BADGE[b.loai]}`}>
                        <i className={`ti ${LOAI_ICON[b.loai]}`} aria-hidden="true" style={{ marginRight: 3 }}></i>
                        {LOAI_NHAN[b.loai]}
                      </span>
                      {b.giaiDap && (
                        <span className="badge badge-green">
                          <i className="ti ti-circle-check" aria-hidden="true" style={{ marginRight: 3 }}></i>
                          Đã giải đáp
                        </span>
                      )}
                    </div>
                    <div className="dd-post-title">{b.tieuDe}</div>
                    <div className="dd-post-meta">
                      <span>{b.tenNguoiDang || 'Ẩn danh'}</span>
                      <span><i className="ti ti-message-2" aria-hidden="true"></i> {b.soBinhLuan}</span>
                      <span>{new Date(b.ngayTao).toLocaleDateString('vi-VN')}</span>
                    </div>
                  </div>
                </div>
              </div>
            ))}

            {dangTai && <p style={{ textAlign: 'center', color: 'var(--text-3)', fontSize: 13 }}>Đang tải...</p>}

            {!hetTrang && !dangTai && danhSach.length > 0 && (
              <button className="btn btn-secondary btn-sm" style={{ width: '100%', marginTop: 8 }}
                onClick={() => setTrang(t => t + 1)}>
                Xem thêm
              </button>
            )}
            <div ref={cuoiDanhSachRef} />
          </div>
        </div>

        {baiMo ? (
          <div style={{ width: 420, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
            <div style={{ padding: '12px 16px', borderBottom: '1px solid var(--border)', display: 'flex', alignItems: 'center', gap: 8, flexShrink: 0 }}>
              <span className={`badge ${LOAI_BADGE[baiMo.loai]}`}>
                <i className={`ti ${LOAI_ICON[baiMo.loai]}`} aria-hidden="true"></i>
              </span>
              <span style={{ flex: 1, fontWeight: 500, fontSize: 13 }}>{baiMo.tieuDe}</span>
              <button className="btn btn-secondary btn-sm" onClick={() => setBaiMo(null)}>
                <i className="ti ti-x" aria-hidden="true"></i>
              </button>
            </div>

            <div style={{ padding: 16, borderBottom: '1px solid var(--border)', flexShrink: 0 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
                <div className="avatar" style={{ width: 28, height: 28, fontSize: 11 }}>
                  {baiMo.tenNguoiDang?.[0]?.toUpperCase() || '?'}
                </div>
                <div>
                  <div style={{ fontSize: 12, fontWeight: 500 }}>{baiMo.tenNguoiDang || 'Ẩn danh'}</div>
                  <div style={{ fontSize: 11, color: 'var(--text-3)' }}>
                    {new Date(baiMo.ngayTao).toLocaleString('vi-VN')}
                  </div>
                </div>

                {laGiangVien && baiMo.loai === 'HOI_DAP' && (
                  <button
                    className={`btn btn-sm ${baiMo.giaiDap ? 'btn-secondary' : 'btn-success'}`}
                    style={{ marginLeft: 'auto' }}
                    onClick={() => xuLyGiaiDap(baiMo.maBaiDang, !baiMo.giaiDap)}>
                    <i className={`ti ${baiMo.giaiDap ? 'ti-x' : 'ti-check'}`} aria-hidden="true"></i>
                    {baiMo.giaiDap ? 'Bỏ giải đáp' : 'Đánh dấu giải đáp'}
                  </button>
                )}

                {(baiMo.maNguoiDang === maNguoiDungHienTai || laGiangVien) && !dangSuaBai && (
                  <button className="btn btn-secondary btn-sm"
                    style={{ marginLeft: laGiangVien && baiMo.loai === 'HOI_DAP' ? 4 : 'auto' }}
                    onClick={batDauSuaBai}>
                    <i className="ti ti-pencil" aria-hidden="true"></i>
                  </button>
                )}

                {(baiMo.maNguoiDang === maNguoiDungHienTai || laGiangVien) && !dangSuaBai && (
                  <button className="btn btn-danger btn-sm"
                    onClick={() => xuLyXoaBai(baiMo.maBaiDang)}>
                    <i className="ti ti-trash" aria-hidden="true"></i>
                  </button>
                )}
              </div>

              {dangSuaBai ? (
                <form onSubmit={xuLyLuuSuaBai}>
                  {loiSua && <div className="alert alert-danger" style={{ marginBottom: 10 }}>{loiSua}</div>}
                  <div className="form-group">
                    <input className="form-input" value={tieuDeSua} onChange={e => setTieuDeSua(e.target.value)}
                      placeholder="Tiêu đề bài đăng..." />
                  </div>
                  <div className="form-group">
                    <textarea className="form-input" rows={3}
                      style={{ height: 'auto', resize: 'vertical', padding: '8px 10px' }}
                      value={noiDungSua} onChange={e => setNoiDungSua(e.target.value)}
                      placeholder="Nội dung (tuỳ chọn)..." />
                  </div>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn btn-primary btn-sm" type="submit" disabled={dangLuuSua}>
                      {dangLuuSua ? 'Đang lưu...' : 'Lưu thay đổi'}
                    </button>
                    <button className="btn btn-secondary btn-sm" type="button" onClick={() => setDangSuaBai(false)}>
                      Huỷ
                    </button>
                  </div>
                </form>
              ) : (
                <>
                  {baiMo.noiDung && (
                    <div style={{ fontSize: 13, color: 'var(--text)', lineHeight: 1.6, whiteSpace: 'pre-wrap' }}>
                      {baiMo.noiDung}
                    </div>
                  )}

                  {baiMo.tepDinhKem?.length > 0 && (
                    <div style={{ marginTop: 10, display: 'grid', gap: 6 }}>
                      {baiMo.tepDinhKem.map((tep, idx) => (
                        <a key={idx} href={tep.duongDan} target="_blank" rel="noreferrer"
                          style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12.5, padding: '6px 10px', border: '1px solid var(--border)', borderRadius: 8, color: 'var(--primary)', textDecoration: 'none' }}>
                          <i className={`ti ${tep.loai === 'IMAGE' ? 'ti-photo' : tep.loai === 'VIDEO' ? 'ti-player-play' : tep.loai === 'PDF' ? 'ti-file-text' : 'ti-file'}`} aria-hidden="true"></i>
                          {tep.ten}
                          <i className="ti ti-external-link" style={{ marginLeft: 'auto' }} aria-hidden="true"></i>
                        </a>
                      ))}
                    </div>
                  )}

                  {baiMo.giaiDap && (
                    <div className="alert alert-success" style={{ marginTop: 10 }}>
                      <i className="ti ti-circle-check" aria-hidden="true"></i>
                      Câu hỏi này đã được giải đáp.
                    </div>
                  )}
                </>
              )}
            </div>

            {baiMo.loai === 'BAI_TAP' ? (
              <PhanBaiTap
                baiMo={baiMo}
                dsBinhLuan={dsBinhLuan}
                setDsBinhLuan={setDsBinhLuan}
                laGiangVien={laGiangVien}
                maNguoiDungHienTai={maNguoiDungHienTai}
                maLopHoc={maLopHoc}
                dangTaiBL={dangTaiBL}
              />
            ) : (
              <>
                <div style={{ flex: 1, overflowY: 'auto', padding: '12px 16px' }}>
                  <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginBottom: 10, textTransform: 'uppercase', letterSpacing: 0.4 }}>
                    Bình luận ({dsBinhLuan.length})
                  </div>

                  {dangTaiBL && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>}

                  {dsBinhLuan.map(bl => (
                    <div key={bl.maBinhLuan} style={{ marginBottom: 14 }}>
                      <ItemBinhLuan bl={bl} maCha={null}
                        maNguoiDungHienTai={maNguoiDungHienTai}
                        laGiangVien={laGiangVien}
                        onXoa={xuLyXoaBL}
                        onReply={() => setMaChaReply(bl.maBinhLuan === maChaReply ? null : bl.maBinhLuan)} />

                      {maChaReply === bl.maBinhLuan && (
                        <div style={{ marginLeft: 32, marginTop: 8 }}>
                          <div style={{ display: 'flex', gap: 6 }}>
                            <input className="form-input" style={{ flex: 1, height: 32, fontSize: 12 }}
                              placeholder="Viết reply..."
                              value={noiDungReply}
                              onChange={e => setNoiDungReply(e.target.value)}
                              onKeyDown={e => { if (e.key === 'Enter') xuLyGuiReply(bl.maBinhLuan); }} />
                            <button className="btn btn-primary btn-sm" disabled={dangGuiBL}
                              onClick={() => xuLyGuiReply(bl.maBinhLuan)}>
                              Gửi
                            </button>
                            <button className="btn btn-secondary btn-sm"
                              onClick={() => { setMaChaReply(null); setNoiDungReply(''); }}>
                              Huỷ
                            </button>
                          </div>
                        </div>
                      )}

                      {(bl.danhSachReply || []).map(reply => (
                        <div key={reply.maBinhLuan} style={{ marginLeft: 32, marginTop: 8 }}>
                          <ItemBinhLuan bl={reply} maCha={bl.maBinhLuan}
                            maNguoiDungHienTai={maNguoiDungHienTai}
                            laGiangVien={laGiangVien}
                            onXoa={xuLyXoaBL}
                            onReply={null} />
                        </div>
                      ))}
                    </div>
                  ))}
                </div>

                <div style={{ padding: '10px 16px', borderTop: '1px solid var(--border)', flexShrink: 0 }}>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <textarea className="form-input"
                      style={{ flex: 1, height: 60, resize: 'none', padding: '8px 10px', fontSize: 13 }}
                      placeholder="Viết bình luận..."
                      value={noiDungBL}
                      onChange={e => setNoiDungBL(e.target.value)}
                    />
                    <button className="btn btn-primary btn-sm" disabled={dangGuiBL || !noiDungBL.trim()}
                      onClick={xuLyGuiBinhLuan}>
                      <i className="ti ti-send" aria-hidden="true"></i>
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        ) : (
          <div style={{ width: 420, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <div style={{ textAlign: 'center', color: 'var(--text-3)' }}>
              <i className="ti ti-message-circle" style={{ fontSize: 40 }} aria-hidden="true"></i>
              <p style={{ marginTop: 8, fontSize: 13 }}>Chọn bài đăng để xem chi tiết</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function ItemBinhLuan({ bl, maCha, maNguoiDungHienTai, laGiangVien, onXoa, onReply }) {
  return (
    <div style={{ display: 'flex', gap: 8 }}>
      <div className="avatar" style={{ width: 26, height: 26, fontSize: 10, flexShrink: 0 }}>
        {bl.tenNguoiDung?.[0]?.toUpperCase() || '?'}
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ background: 'var(--surface)', borderRadius: 8, padding: '7px 10px' }}>
          <div style={{ fontSize: 11, fontWeight: 600, marginBottom: 3 }}>{bl.tenNguoiDung || 'Ẩn danh'}</div>
          <div style={{ fontSize: 13, color: 'var(--text)', lineHeight: 1.5 }}>{bl.noiDung}</div>
        </div>
        <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 3, display: 'flex', gap: 10 }}>
          <span>{new Date(bl.ngayTao).toLocaleString('vi-VN')}</span>
          {onReply && (
            <button style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 11, color: 'var(--primary)', padding: 0 }}
              onClick={onReply}>
              Reply
            </button>
          )}
          {(bl.maNguoiDung === maNguoiDungHienTai || laGiangVien) && (
            <button style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 11, color: 'var(--danger)', padding: 0 }}
              onClick={() => onXoa(bl.maBinhLuan, maCha)}>
              Xoá
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

function TepDinhKemList({ danhSach }) {
  if (!danhSach?.length) return null;
  return (
    <div style={{ marginTop: 8, display: 'grid', gap: 6 }}>
      {danhSach.map((tep, idx) => (
        <a key={idx} href={tep.duongDan} target="_blank" rel="noreferrer"
          style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12.5, padding: '6px 10px', border: '1px solid var(--border)', borderRadius: 8, color: 'var(--primary)', textDecoration: 'none' }}>
          <i className={`ti ${tep.loai === 'IMAGE' ? 'ti-photo' : tep.loai === 'VIDEO' ? 'ti-player-play' : tep.loai === 'PDF' ? 'ti-file-text' : 'ti-file'}`} aria-hidden="true"></i>
          {tep.ten}
          <i className="ti ti-external-link" style={{ marginLeft: 'auto' }} aria-hidden="true"></i>
        </a>
      ))}
    </div>
  );
}

/**
 * Nộp bài / chấm điểm kiểu Google Classroom cho bài đăng loại BAI_TAP.
 * dsBinhLuan ở đây thực chất là danh sách "bài nộp" — với học viên, backend
 * chỉ trả về bài nộp của chính họ (nếu có); với giảng viên, trả về toàn bộ.
 */
function PhanBaiTap({ baiMo, dsBinhLuan, setDsBinhLuan, laGiangVien, maNguoiDungHienTai, maLopHoc, dangTaiBL }) {
  const baiNopCuaToi = !laGiangVien ? dsBinhLuan.find(bl => bl.maNguoiDung === maNguoiDungHienTai) : null;

  // ── Giảng viên: trạng thái nộp bài của toàn bộ học viên trong lớp ──
  const [dsTrangThaiNop, setDsTrangThaiNop] = useState([]);
  const [dangTaiTrangThaiNop, setDangTaiTrangThaiNop] = useState(false);
  const [loiTrangThaiNop, setLoiTrangThaiNop] = useState('');
  const [hienDsChuaNop, setHienDsChuaNop] = useState(true);

  useEffect(() => {
    if (!laGiangVien || !baiMo?.maBaiDang) return;
    let huy = false;
    setDangTaiTrangThaiNop(true);
    setLoiTrangThaiNop('');
    layDanhSachNopBai(baiMo.maBaiDang)
      .then(ds => { if (!huy) setDsTrangThaiNop(ds); })
      .catch(err => { if (!huy) setLoiTrangThaiNop(layThongBaoLoi(err)); })
      .finally(() => { if (!huy) setDangTaiTrangThaiNop(false); });
    return () => { huy = true; };
  }, [laGiangVien, baiMo?.maBaiDang]);

  const soLuongChuaNop = dsTrangThaiNop.filter(t => t.trangThai === 'CHUA_NOP').length;
  const soLuongDaNop = dsTrangThaiNop.length - soLuongChuaNop;

  const [dangSuaNop, setDangSuaNop] = useState(false);
  const [ghiChuNop, setGhiChuNop] = useState('');
  const [tepNop, setTepNop] = useState([]);
  const [dangTaiTepNop, setDangTaiTepNop] = useState(false);
  const [tienDoTepNop, setTienDoTepNop] = useState(null);
  const [dangGuiNop, setDangGuiNop] = useState(false);
  const [loiNop, setLoiNop] = useState('');

  const [maDangCham, setMaDangCham] = useState(null);
  const [diemForm, setDiemForm] = useState('');
  const [diemToiDaForm, setDiemToiDaForm] = useState('10');
  const [nhanXetForm, setNhanXetForm] = useState('');
  const [dangGuiCham, setDangGuiCham] = useState(false);
  const [loiCham, setLoiCham] = useState('');

  function batDauNopHoacSuaBai() {
    if (baiNopCuaToi) {
      setGhiChuNop(baiNopCuaToi.noiDung || '');
      setTepNop(baiNopCuaToi.tepDinhKemNopBai || []);
    } else {
      setGhiChuNop('');
      setTepNop([]);
    }
    setLoiNop('');
    setDangSuaNop(true);
  }

  async function xuLyChonTep(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setDangTaiTepNop(true);
    setTienDoTepNop(0);
    setLoiNop('');
    try {
      const tep = await taiLenTepDinhKem(maLopHoc, file, (p) => setTienDoTepNop(p >= 100 ? null : p));
      setTepNop(prev => [...prev, tep]);
    } catch (err) {
      setLoiNop(layThongBaoLoi(err));
    } finally {
      setDangTaiTepNop(false);
      setTienDoTepNop(null);
      e.target.value = '';
    }
  }

  async function xuLyNop() {
    if (!ghiChuNop.trim() && tepNop.length === 0) {
      setLoiNop('Vui lòng nhập ghi chú hoặc đính kèm ít nhất 1 tệp bài làm');
      return;
    }
    setDangGuiNop(true);
    setLoiNop('');
    try {
      const ket = await nopBaiTap(baiMo.maBaiDang, { ghiChu: ghiChuNop.trim() || null, tepDinhKem: tepNop });
      setDsBinhLuan(prev => {
        const conLai = prev.filter(bl => bl.maNguoiDung !== maNguoiDungHienTai);
        return [...conLai, ket];
      });
      setDangSuaNop(false);
    } catch (err) {
      setLoiNop(layThongBaoLoi(err));
    } finally {
      setDangGuiNop(false);
    }
  }

  function batDauCham(baiNop) {
    const diemDaCham = baiNop.danhSachReply?.find(r => r.loaiBinhLuan === 'CHAM_DIEM');
    setDiemForm(diemDaCham ? String(diemDaCham.diem) : '');
    setDiemToiDaForm(diemDaCham ? String(diemDaCham.diemToiDa) : '10');
    setNhanXetForm(diemDaCham ? diemDaCham.noiDung || '' : '');
    setLoiCham('');
    setMaDangCham(baiNop.maBinhLuan);
  }

  async function xuLyGuiCham(maBinhLuanBaiNop) {
    const diem = Number(diemForm);
    const diemToiDa = Number(diemToiDaForm);
    if (diemForm === '' || Number.isNaN(diem) || diem < 0) { setLoiCham('Vui lòng nhập điểm hợp lệ'); return; }
    if (diemToiDaForm === '' || Number.isNaN(diemToiDa) || diemToiDa <= 0) { setLoiCham('Vui lòng nhập điểm tối đa hợp lệ'); return; }
    if (diem > diemToiDa) { setLoiCham('Điểm không được lớn hơn điểm tối đa'); return; }

    setDangGuiCham(true);
    setLoiCham('');
    try {
      const ket = await chamDiem(maBinhLuanBaiNop, { diem, diemToiDa, nhanXet: nhanXetForm.trim() || null });
      setDsBinhLuan(prev => prev.map(bl => bl.maBinhLuan === ket.maBinhLuan ? ket : bl));
      setMaDangCham(null);
      if (laGiangVien && baiMo?.maBaiDang) {
        layDanhSachNopBai(baiMo.maBaiDang).then(setDsTrangThaiNop).catch(() => {});
      }
    } catch (err) {
      setLoiCham(layThongBaoLoi(err));
    } finally {
      setDangGuiCham(false);
    }
  }

  if (dangTaiBL) {
    return <div style={{ flex: 1, padding: 16 }}><p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p></div>;
  }

  // ── Giao diện Giảng viên: danh sách toàn bộ bài nộp ──
  if (laGiangVien) {
    return (
      <div style={{ flex: 1, overflowY: 'auto', padding: '12px 16px' }}>

        {/* Tổng quan trạng thái nộp bài của cả lớp — đối chiếu thành viên lớp
            với các bài nộp hiện có, không cần bảng/cột CSDL mới. */}
        {dangTaiTrangThaiNop ? (
          <p style={{ fontSize: 12.5, color: 'var(--text-3)', marginBottom: 12 }}>Đang tải trạng thái nộp bài...</p>
        ) : loiTrangThaiNop ? (
          <div className="alert alert-danger" style={{ fontSize: 12.5, marginBottom: 12 }}>{loiTrangThaiNop}</div>
        ) : dsTrangThaiNop.length > 0 && (
          <div className="card sm" style={{ marginBottom: 14 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
              <span className="badge badge-green">
                <i className="ti ti-circle-check" aria-hidden="true"></i> Đã nộp: {soLuongDaNop}/{dsTrangThaiNop.length}
              </span>
              {soLuongChuaNop > 0 && (
                <span className="badge badge-red">
                  <i className="ti ti-clock-exclamation" aria-hidden="true"></i> Chưa nộp: {soLuongChuaNop}
                </span>
              )}
              {soLuongChuaNop > 0 && (
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ marginLeft: 'auto' }}
                  onClick={() => setHienDsChuaNop(v => !v)}
                >
                  {hienDsChuaNop ? 'Ẩn danh sách chưa nộp' : 'Xem danh sách chưa nộp'}
                </button>
              )}
            </div>

            {soLuongChuaNop > 0 && hienDsChuaNop && (
              <div style={{ marginTop: 10, paddingTop: 10, borderTop: '1px solid var(--border)' }}>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                  {dsTrangThaiNop.filter(t => t.trangThai === 'CHUA_NOP').map(hv => (
                    <div key={hv.maHocVien} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12.5, padding: '5px 10px', border: '1px solid var(--border)', borderRadius: 20 }}>
                      <div className="avatar" style={{ width: 20, height: 20, fontSize: 9.5 }}>
                        {hv.tenHocVien?.[0]?.toUpperCase() || '?'}
                      </div>
                      {hv.tenHocVien || 'Học viên'}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginBottom: 10, textTransform: 'uppercase', letterSpacing: 0.4 }}>
          Bài nộp ({dsBinhLuan.length})
        </div>

        {dsBinhLuan.length === 0 && (
          <div className="empty-state">Chưa có học viên nào nộp bài.</div>
        )}

        {dsBinhLuan.map(baiNop => {
          const diemDaCham = baiNop.danhSachReply?.find(r => r.loaiBinhLuan === 'CHAM_DIEM');
          return (
            <div key={baiNop.maBinhLuan} className="card sm" style={{ marginBottom: 10 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
                <div className="avatar" style={{ width: 26, height: 26, fontSize: 10 }}>
                  {baiNop.tenNguoiDung?.[0]?.toUpperCase() || '?'}
                </div>
                <strong style={{ fontSize: 13 }}>{baiNop.tenNguoiDung || 'Ẩn danh'}</strong>
                <span style={{ fontSize: 11, color: 'var(--text-3)', marginLeft: 'auto' }}>
                  {new Date(baiNop.ngayTao).toLocaleString('vi-VN')}
                </span>
              </div>

              {baiNop.noiDung && <div style={{ fontSize: 13, whiteSpace: 'pre-wrap' }}>{baiNop.noiDung}</div>}
              <TepDinhKemList danhSach={baiNop.tepDinhKemNopBai} />

              {diemDaCham && maDangCham !== baiNop.maBinhLuan && (
                <div className="alert alert-success" style={{ marginTop: 10, fontSize: 12.5 }}>
                  <strong>Điểm: {String(diemDaCham.diem)}/{String(diemDaCham.diemToiDa)}</strong>
                  {diemDaCham.noiDung && <div style={{ marginTop: 4 }}>{diemDaCham.noiDung}</div>}
                </div>
              )}

              {maDangCham === baiNop.maBinhLuan ? (
                <div style={{ marginTop: 10, borderTop: '1px solid var(--border)', paddingTop: 10 }}>
                  {loiCham && <div className="alert alert-danger" style={{ fontSize: 12 }}>{loiCham}</div>}
                  <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                    <input className="form-input" style={{ width: 70 }} type="number" min="0" step="0.1"
                      placeholder="Điểm" value={diemForm} onChange={e => setDiemForm(e.target.value)} />
                    <span style={{ alignSelf: 'center' }}>/</span>
                    <input className="form-input" style={{ width: 70 }} type="number" min="0.01" step="0.1"
                      placeholder="Tối đa" value={diemToiDaForm} onChange={e => setDiemToiDaForm(e.target.value)} />
                  </div>
                  <textarea className="form-input" style={{ height: 60, resize: 'vertical', padding: '8px 10px', fontSize: 12.5, marginBottom: 8 }}
                    placeholder="Nhận xét (tuỳ chọn)..." value={nhanXetForm} onChange={e => setNhanXetForm(e.target.value)} />
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn btn-primary btn-sm" disabled={dangGuiCham}
                      onClick={() => xuLyGuiCham(baiNop.maBinhLuan)}>
                      {dangGuiCham ? 'Đang lưu...' : 'Lưu điểm'}
                    </button>
                    <button className="btn btn-secondary btn-sm" onClick={() => setMaDangCham(null)}>Huỷ</button>
                  </div>
                </div>
              ) : (
                <button className="btn btn-secondary btn-sm" style={{ marginTop: 10 }} onClick={() => batDauCham(baiNop)}>
                  <i className="ti ti-checkbox" aria-hidden="true"></i> {diemDaCham ? 'Sửa điểm' : 'Chấm điểm'}
                </button>
              )}
            </div>
          );
        })}
      </div>
    );
  }

  // ── Giao diện Học viên: bài nộp của chính mình ──
  const diemDaCham = baiNopCuaToi?.danhSachReply?.find(r => r.loaiBinhLuan === 'CHAM_DIEM');

  return (
    <div style={{ flex: 1, overflowY: 'auto', padding: '12px 16px' }}>
      <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginBottom: 10, textTransform: 'uppercase', letterSpacing: 0.4 }}>
        Bài làm của bạn
      </div>

      {!dangSuaNop && !baiNopCuaToi && (
        <div>
          <p style={{ fontSize: 13, color: 'var(--text-3)', marginBottom: 10 }}>Bạn chưa nộp bài cho bài tập này.</p>
          <button className="btn btn-primary btn-sm" onClick={batDauNopHoacSuaBai}>
            <i className="ti ti-upload" aria-hidden="true"></i> Nộp bài
          </button>
        </div>
      )}

      {!dangSuaNop && baiNopCuaToi && (
        <div className="card sm">
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
            <span className="badge badge-green">
              <i className="ti ti-circle-check" aria-hidden="true"></i> Đã nộp
            </span>
            <span style={{ fontSize: 11, color: 'var(--text-3)', marginLeft: 'auto' }}>
              {new Date(baiNopCuaToi.ngayTao).toLocaleString('vi-VN')}
            </span>
          </div>
          {baiNopCuaToi.noiDung && <div style={{ fontSize: 13, whiteSpace: 'pre-wrap' }}>{baiNopCuaToi.noiDung}</div>}
          <TepDinhKemList danhSach={baiNopCuaToi.tepDinhKemNopBai} />

          {diemDaCham ? (
            <div className="alert alert-success" style={{ marginTop: 10, fontSize: 12.5 }}>
              <strong>Điểm: {String(diemDaCham.diem)}/{String(diemDaCham.diemToiDa)}</strong>
              {diemDaCham.noiDung && <div style={{ marginTop: 4 }}>{diemDaCham.noiDung}</div>}
            </div>
          ) : (
            <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 8 }}>Chưa được chấm điểm.</div>
          )}

          <button className="btn btn-secondary btn-sm" style={{ marginTop: 10 }} onClick={batDauNopHoacSuaBai}>
            <i className="ti ti-refresh" aria-hidden="true"></i> Nộp lại
          </button>
        </div>
      )}

      {dangSuaNop && (
        <div className="card sm">
          {loiNop && <div className="alert alert-danger" style={{ fontSize: 12.5 }}>{loiNop}</div>}
          <div className="form-group">
            <textarea className="form-input" style={{ height: 80, resize: 'vertical', padding: '8px 10px', fontSize: 13 }}
              placeholder="Ghi chú cho bài làm (tuỳ chọn)..."
              value={ghiChuNop} onChange={e => setGhiChuNop(e.target.value)} />
          </div>
          <div className="form-group">
            {tepNop.map((tep, idx) => (
              <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12.5, padding: '4px 0' }}>
                <i className="ti ti-paperclip" aria-hidden="true"></i>
                <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tep.ten}</span>
                <button type="button" onClick={() => setTepNop(prev => prev.filter((_, i) => i !== idx))}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--danger)' }}>
                  <i className="ti ti-x" aria-hidden="true"></i>
                </button>
              </div>
            ))}
            <label className="btn btn-secondary btn-sm" style={{ display: 'inline-flex', cursor: 'pointer' }}>
              <i className="ti ti-upload" aria-hidden="true"></i>
              {dangTaiTepNop ? ' Đang tải lên...' : ' Đính kèm bài làm'}
              <input type="file" style={{ display: 'none' }} disabled={dangTaiTepNop} onChange={xuLyChonTep} />
            </label>
            {dangTaiTepNop && <ThanhTienDoTaiLen phanTram={tienDoTepNop} nhan={tienDoTepNop == null ? 'Đang xử lý...' : 'Đang tải lên...'} />}
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-primary btn-sm" disabled={dangGuiNop} onClick={xuLyNop}>
              {dangGuiNop ? 'Đang nộp...' : 'Nộp bài'}
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => setDangSuaNop(false)}>Huỷ</button>
          </div>
        </div>
      )}
    </div>
  );
}