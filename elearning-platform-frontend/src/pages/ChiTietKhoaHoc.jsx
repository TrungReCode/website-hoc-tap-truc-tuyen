import { useEffect, useState, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { layChiTietCongKhai } from '../services/khoaHocService';
import { layDanhGiaTheoKhoaHoc, layDanhGiaCuaToi, taoDanhGia, capNhatDanhGia, xoaDanhGia } from '../services/danhGiaService';
import { taoDonMuaKhoaHoc, kiemTraSoHuu } from '../services/thanhToanService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

// 1. IMPORT MODAL XEM TỆP TIN VÀ CSS
import XemTepTinModal from '../components/XemTepTinModal';
import '../components/XemTepTinModal.css';

const NHAN_LOAI_BAI_HOC = { VIDEO: 'ti-player-play', PDF: 'ti-file-text', DOC: 'ti-file-text', QUIZ: 'ti-help-circle' };

function dinhDangThoiLuong(giay) {
  if (!giay) return '';
  const phut = Math.floor(giay / 60);
  const giayConLai = Math.round(giay % 60);
  return `${phut}:${String(giayConLai).padStart(2, '0')}`;
}

export default function ChiTietKhoaHoc() {
  const { maKhoaHoc } = useParams();
  const navigate = useNavigate();
  // FIX (Vấn đề #2): "accessToken" không còn đọc được từ localStorage —
  // dùng cờ hiển thị "daDangNhap" thay thế (xem TuyenDuongRieng.jsx).
  const daDangNhap = !!localStorage.getItem('daDangNhap');
  const maNguoiDung = Number(localStorage.getItem('maNguoiDung'));
  const [khoaHoc, setKhoaHoc] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [tabDangChon, setTabDangChon] = useState('giao-trinh');
  const [dangThanhToan, setDangThanhToan] = useState(null);
  // 2. STATE LƯU BÀI HỌC ĐANG XEM THỬ
  const [baiHocXemThu, setBaiHocXemThu] = useState(null);

  const [daMuaCaNhan, setDaMuaCaNhan] = useState(false);
  const [daMuaDayHoc, setDaMuaDayHoc] = useState(false);

  useEffect(() => {
    if (!maKhoaHoc || !daDangNhap) return;

    const checkQuyen = async () => {
      try {
        const [caNhan, dayHoc] = await Promise.all([
          kiemTraSoHuu(maKhoaHoc, 'CA_NHAN'),
          kiemTraSoHuu(maKhoaHoc, 'DAY_HOC'),
        ]);

        setDaMuaCaNhan(caNhan);
        setDaMuaDayHoc(dayHoc);
      } catch (err) {
        console.error("Lỗi kiểm tra quyền:", err);
      }
    };

    checkQuyen();
  }, [maKhoaHoc, daDangNhap]);

  useEffect(() => {
    if (!maKhoaHoc) return;

    const loadKhoaHoc = async () => {
      try {
        setDangTai(true);

        const data = await layChiTietCongKhai(maKhoaHoc);

        setKhoaHoc(data);
      } catch (err) {
        console.error(err);
        setLoi(layThongBaoLoi(err));
      } finally {
        setDangTai(false);
      }
    };

    loadKhoaHoc();
  }, [maKhoaHoc]);

  const laTacGia =
    daDangNhap &&
    khoaHoc &&
    Number(khoaHoc.maTacGia) === maNguoiDung;

  if (dangTai) return <div className="empty-state">Đang tải khóa học...</div>;
  if (loi) return <div className="alert alert-danger" style={{ margin: 20 }}>{loi}</div>;
  if (!khoaHoc) return null;

  async function xuLyMuaKhoaHoc(loaiGiayPhep) {
    if (!daDangNhap) {
      navigate('/dang-nhap');
      return;
    }

    setDangThanhToan(loaiGiayPhep);
    setLoi('');

    // Phí dạy học độc lập với giá cá nhân — khóa học có thể miễn phí cho học viên
    // nhưng vẫn thu phí giấy phép giảng dạy (hoặc ngược lại)
    const soTienThanhToan = loaiGiayPhep === 'CA_NHAN'
      ? Number(khoaHoc.gia)
      : Number(khoaHoc.phiDayHoc) || 0;

    try {
      const don = await taoDonMuaKhoaHoc(Number(maKhoaHoc), { loaiGiayPhep });

      if (soTienThanhToan === 0) {
        if (loaiGiayPhep === 'CA_NHAN') setDaMuaCaNhan(true);
        if (loaiGiayPhep === 'DAY_HOC') setDaMuaDayHoc(true);
        alert(`Đăng ký ${loaiGiayPhep === 'CA_NHAN' ? 'khóa học' : 'quyền giảng dạy'} miễn phí thành công!`);
      }
      else if (don?.urlThanhToan) {
        window.location.href = don.urlThanhToan;
      }
      else {
        setLoi('Không nhận được đường dẫn thanh toán từ hệ thống.');
      }
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangThanhToan(null);
    }
  }

  const tongSoBaiHoc = khoaHoc.danhSachChuong.reduce((tong, c) => tong + c.danhSachBaiHoc.length, 0);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title">
          <i className="ti ti-book-open" aria-hidden="true"></i> {khoaHoc.tieuDe}
        </span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/cho-khoa-hoc')}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i> Chợ khóa học
        </button>
      </div>

      <div className="sc-split">
        <div className="split-left">
          <h2 style={{ fontSize: 18, fontWeight: 500, marginBottom: 6 }}>{khoaHoc.tieuDe}</h2>
          <p style={{ fontSize: 13, color: 'var(--text-2)', marginBottom: 14 }}>{khoaHoc.mieuTa}</p>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 16 }}>
            <div className="avatar" style={{ width: 32, height: 32, fontSize: 12 }}>
              {(khoaHoc.tenTacGia || '?').charAt(0).toUpperCase()}
            </div>
            <span style={{ fontSize: 13 }}>
              Giảng viên: <strong>{khoaHoc.tenTacGia || 'Đang cập nhật'}</strong>
            </span>
            <span className="star" style={{ marginLeft: 4 }}>
              {'★'.repeat(Math.round(khoaHoc.diemTrungBinh || 0))}
            </span>
            <span style={{ fontSize: 12, color: 'var(--text-3)' }}>
              {khoaHoc.diemTrungBinh ? khoaHoc.diemTrungBinh.toFixed(1) : 'Chưa có đánh giá'} ({khoaHoc.soLuongDanhGia} đánh giá)
            </span>
          </div>

          {khoaHoc.videoGioiThieu ? (
            <div className="video-player" style={{ height: 200, marginBottom: 16 }}>
              <div className="vp-title">{tongSoBaiHoc} bài học · {khoaHoc.danhSachChuong.length} chương</div>
              <div className="vp-btn">
                <i className="ti ti-player-play" aria-hidden="true"></i>
              </div>
            </div>
          ) : khoaHoc.anhBia ? (
            <div style={{ height: 200, marginBottom: 16, borderRadius: 8, overflow: 'hidden' }}>
              <img src={khoaHoc.anhBia} alt={khoaHoc.tieuDe} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
          ) : (
            <div style={{ height: 200, marginBottom: 16, borderRadius: 8, backgroundColor: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary)' }}>
              <i className="ti ti-book-2" style={{ fontSize: 48 }} aria-hidden="true"></i>
            </div>
          )}

          <div className="tab-row">
            <div className={`tab-item ${tabDangChon === 'giao-trinh' ? 'active' : ''}`} onClick={() => setTabDangChon('giao-trinh')}>
              Giáo trình
            </div>
            <div className={`tab-item ${tabDangChon === 'danh-gia' ? 'active' : ''}`} onClick={() => setTabDangChon('danh-gia')}>
              Đánh giá
            </div>
            <div className={`tab-item ${tabDangChon === 'giang-vien' ? 'active' : ''}`} onClick={() => setTabDangChon('giang-vien')}>
              Về giảng viên
            </div>
          </div>

          {/* 3. TRUYỀN HÀM XỬ LÝ CLICK XUỐNG TAB GIÁO TRÌNH */}
          {tabDangChon === 'giao-trinh' && <TabGiaoTrinh khoaHoc={khoaHoc} onXemThu={setBaiHocXemThu} />}
          {tabDangChon === 'danh-gia' && <TabDanhGia maKhoaHoc={Number(maKhoaHoc)} daDangNhap={daDangNhap} navigate={navigate} />}
          {tabDangChon === 'giang-vien' && (
            <div className="card sm">
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 10 }}>
                <div className="avatar" style={{ width: 44, height: 44, fontSize: 16 }}>
                  {(khoaHoc.tenTacGia || '?').charAt(0).toUpperCase()}
                </div>
                <div style={{ fontSize: 15, fontWeight: 500 }}>{khoaHoc.tenTacGia || 'Đang cập nhật'}</div>
              </div>
              <p style={{ fontSize: 13, color: 'var(--text-2)' }}>
                Tác giả của khóa học "{khoaHoc.tieuDe}", thuộc danh mục {khoaHoc.tenDanhMuc}.
              </p>
            </div>
          )}
        </div>

        <div className="split-right">

          {/* ===== GIÁ KHÓA HỌC (LUÔN HIỂN THỊ) ===== */}
          <div className="card sm">
            <div className="card-title" style={{ fontSize: 13 }}>Chọn loại giấy phép</div>

            <div className="card sm" style={{ border: '2px solid var(--primary)', marginBottom: khoaHoc.phiDayHoc != null ? 10 : 0 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                <span style={{ fontSize: 13, fontWeight: 600 }}><i className="ti ti-user" aria-hidden="true"></i> Giấy phép cá nhân</span>
                <span style={{ fontSize: 18, fontWeight: 700, color: 'var(--primary)' }}>
                  {Number(khoaHoc.gia) === 0 ? 'Miễn phí' : `${Number(khoaHoc.gia).toLocaleString('vi-VN')} đ`}
                </span>
              </div>
              <div style={{ fontSize: 11.5, color: 'var(--text-3)', marginTop: 4 }}>Học trọn đời cho 1 tài khoản học viên</div>
            </div>

            {khoaHoc.phiDayHoc != null && (
              <div className="card sm" style={{ marginBottom: 0 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                  <span style={{ fontSize: 13, fontWeight: 600 }}><i className="ti ti-chalkboard" aria-hidden="true"></i> Giấy phép giảng dạy</span>
                  <span style={{ fontSize: 15, fontWeight: 700 }}>
                    {Number(khoaHoc.phiDayHoc) === 0 ? 'Miễn phí' : `${Number(khoaHoc.phiDayHoc).toLocaleString('vi-VN')} đ`}
                  </span>
                </div>
                <div style={{ fontSize: 11.5, color: 'var(--text-3)', marginTop: 4 }}>Cho phép mở lớp học riêng dựa trên khóa học này</div>
              </div>
            )}

            {/* ===== NÚT MUA (CHỈ KHÁCH) ===== */}
            {!laTacGia && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 14 }}>

                {/* NÚT: MUA/ĐĂNG KÝ CÁ NHÂN */}
                <button
                  className="btn btn-accent"
                  disabled={dangThanhToan !== null || daMuaCaNhan}
                  onClick={() => xuLyMuaKhoaHoc('CA_NHAN')}
                >
                  {daMuaCaNhan
                    ? 'Đã sở hữu'
                    : dangThanhToan === 'CA_NHAN'
                      ? 'Đang xử lý...'
                      : Number(khoaHoc.gia) === 0 ? 'Đăng ký học miễn phí' : 'Mua ngay'}
                </button>

                {/* ===== NÚT: MUA/ĐĂNG KÝ QUYỀN GIẢNG DẠY ===== */}
                {/* Chỉ hiện khi tác giả đã cấu hình phí dạy học (phiDayHoc != null) — độc lập với giá cá nhân */}
                {khoaHoc.phiDayHoc != null && (
                  <button
                    className="btn btn-secondary"
                    disabled={dangThanhToan !== null || daMuaDayHoc}
                    onClick={() => xuLyMuaKhoaHoc('DAY_HOC')}
                  >
                    {daMuaDayHoc
                      ? 'Đã sở hữu quyền dạy'
                      : dangThanhToan === 'DAY_HOC'
                        ? 'Đang xử lý...'
                        : Number(khoaHoc.phiDayHoc) === 0 ? 'Đăng ký quyền dạy học' : 'Mua quyền giảng dạy'}
                  </button>
                )}
              </div>
            )}
          </div>

          {/* ===== TÁC GIẢ ===== */}
          {laTacGia && (
            <div className="alert alert-info" style={{ marginTop: 8, fontSize: 13 }}>
              Đây là khóa học do bạn tạo và đăng bán.
            </div>
          )}

          {/* ===== INFO BOX LUÔN HIỂN THỊ ===== */}
          <div className="card sm">
            <div className="card-title" style={{ fontSize: 13 }}>
              Khóa học bao gồm
            </div>

            <div style={{
              fontSize: 12,
              color: 'var(--text-2)',
              display: 'flex',
              flexDirection: 'column',
              gap: 6,
            }}>
              <span><i className="ti ti-video" /> {tongSoBaiHoc} bài học</span>
              <span><i className="ti ti-certificate" /> Danh mục: {khoaHoc.tenDanhMuc}</span>
            </div>
          </div>
        </div>
      </div>

      {/* 4. GỌI MODAL Ở NGOÀI CÙNG HIỂN THỊ FILE */}
      {baiHocXemThu && (
        <XemTepTinModal
          baiHoc={baiHocXemThu}
          onClose={() => setBaiHocXemThu(null)}
          xemThu
        />
      )}

    </div>
  );
}

// 5. CẬP NHẬT TAB GIÁO TRÌNH (Nhận props onXemThu)
function TabGiaoTrinh({ khoaHoc, onXemThu }) {
  return (
    <div className="card sm">
      {khoaHoc.danhSachChuong.length === 0 && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Giáo trình đang được cập nhật.</p>}
      {khoaHoc.danhSachChuong.map((chuong) => (
        <div key={chuong.maChuong} style={{ marginBottom: 10 }}>
          <div style={{ fontSize: 13, fontWeight: 600, padding: '6px 4px' }}>{chuong.tieuDe}</div>
          {chuong.danhSachBaiHoc.map((b) => (

            // XỬ LÝ CLICK TẠI ĐÂY: Nếu có quyền xem thử thì đổi trỏ chuột và gán sự kiện click
            <div
              key={b.maBaiHoc}
              className="curr-item"
              style={{ cursor: b.xemThu ? 'pointer' : 'default' }}
              onClick={() => {
                if (b.xemThu) onXemThu(b);
              }}
            >
              <div className={`ci-icon ${b.xemThu ? 'done' : ''}`}>
                <i className={`ti ${NHAN_LOAI_BAI_HOC[b.loai] || 'ti-file'}`} aria-hidden="true"></i>
              </div>
              <div className="ci-title">{b.tieuDe}</div>
              {b.xemThu ? (
                <span className="badge badge-green">Xem thử</span>
              ) : (
                <i className="ti ti-lock" style={{ color: 'var(--text-3)' }} aria-hidden="true"></i>
              )}
              {b.metadata?.thoiLuongGiay && <span className="ci-dur">{dinhDangThoiLuong(b.metadata.thoiLuongGiay)}</span>}
            </div>

          ))}
        </div>
      ))}
    </div>
  );
}

function TabDanhGia({ maKhoaHoc, daDangNhap, navigate }) {
  const [danhSach, setDanhSach] = useState({ content: [], totalElements: 0 });
  const [danhGiaCuaToi, setDanhGiaCuaToi] = useState(null);
  const [soSaoChon, setSoSaoChon] = useState(0);
  const [nhanXet, setNhanXet] = useState('');
  const [dangGui, setDangGui] = useState(false);
  const [loi, setLoi] = useState('');

  const taiLai = useCallback(() => {
    layDanhGiaTheoKhoaHoc(maKhoaHoc).then(setDanhSach).catch(() => { });
    if (daDangNhap) {
      layDanhGiaCuaToi(maKhoaHoc).then((d) => {
        setDanhGiaCuaToi(d);
        if (d) {
          setSoSaoChon(d.soSao);
          setNhanXet(d.nhanXet || '');
        }
      });
    }
  }, [maKhoaHoc, daDangNhap]);

  useEffect(() => {
    taiLai();
  }, [taiLai]);

  async function xuLyGui(e) {
    e.preventDefault();
    if (soSaoChon === 0) {
      setLoi('Vui lòng chọn số sao đánh giá');
      return;
    }
    setLoi('');
    setDangGui(true);
    try {
      if (danhGiaCuaToi) {
        await capNhatDanhGia(danhGiaCuaToi.maDanhGia, { soSao: soSaoChon, nhanXet });
      } else {
        await taoDanhGia(maKhoaHoc, { soSao: soSaoChon, nhanXet });
      }
      taiLai();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  }

  async function xuLyXoa() {
    if (!danhGiaCuaToi || !window.confirm('Xóa đánh giá của bạn?')) return;
    try {
      await xoaDanhGia(danhGiaCuaToi.maDanhGia);
      setDanhGiaCuaToi(null);
      setSoSaoChon(0);
      setNhanXet('');
      taiLai();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    }
  }

  return (
    <div>
      <div className="card sm">
        <div className="card-title" style={{ fontSize: 13 }}>{danhGiaCuaToi ? 'Chỉnh sửa đánh giá của bạn' : 'Viết đánh giá'}</div>
        {danhGiaCuaToi?.an && (
          <div className="alert alert-warning" style={{ fontSize: 12 }}>
            Đánh giá này hiện đang bị ẩn khỏi công khai do vi phạm tiêu chuẩn cộng đồng (xem chi tiết trong mục Thông báo).
          </div>
        )}
        {!daDangNhap ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>
            <span style={{ color: 'var(--primary)', cursor: 'pointer' }} onClick={() => navigate('/dang-nhap')}>
              Đăng nhập
            </span>{' '}
            để viết đánh giá cho khóa học này.
          </p>
        ) : (
          <form onSubmit={xuLyGui}>
            {loi && <div className="alert alert-danger">{loi}</div>}
            <div className="star-input">
              {[1, 2, 3, 4, 5].map((sao) => (
                <span key={sao} className={sao <= soSaoChon ? 'lit' : ''} onClick={() => setSoSaoChon(sao)}>
                  ★
                </span>
              ))}
            </div>
            <textarea
              className="form-input"
              style={{ height: 70, padding: 8, marginBottom: 10 }}
              placeholder="Chia sẻ cảm nhận của bạn về khóa học (không bắt buộc)"
              value={nhanXet}
              onChange={(e) => setNhanXet(e.target.value)}
              maxLength={1000}
            />
            <div className="btn-row" style={{ marginTop: 0 }}>
              <button type="submit" className="btn btn-primary btn-sm" disabled={dangGui}>
                {dangGui ? 'Đang gửi...' : danhGiaCuaToi ? 'Cập nhật đánh giá' : 'Gửi đánh giá'}
              </button>
              {danhGiaCuaToi && (
                <button type="button" className="btn btn-danger btn-sm" onClick={xuLyXoa}>
                  Xóa đánh giá
                </button>
              )}
            </div>
          </form>
        )}
      </div>

      <div style={{ fontSize: 13, color: 'var(--text-2)', margin: '14px 4px' }}>{danhSach.totalElements} đánh giá</div>
      {danhSach.content.map((d) => (
        <div key={d.maDanhGia} className="forum-post">
          <div className="fp-header">
            <div className="fp-avatar">{(d.tenNguoiDung || '?').charAt(0).toUpperCase()}</div>
            <div className="fp-name">{d.tenNguoiDung || 'Học viên'}</div>
            <span className="star" style={{ marginLeft: 8 }}>{'★'.repeat(d.soSao)}</span>
            <div className="fp-time">{new Date(d.ngayTao).toLocaleDateString('vi-VN')}</div>
          </div>
          {d.nhanXet && <div className="fp-body">{d.nhanXet}</div>}
        </div>
      ))}
    </div>
  );
}