import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { layGiaoTrinhHocVien, layGiaoTrinhGiangVien } from '../../services/giaoTrinhLaiService';
import { layLopHocVien, layChiTietGiangVien, layDanhGiaLop, layDanhGiaCuaToiLop, taoDanhGiaLop, capNhatDanhGiaLop } from '../../services/lopHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import { capNhatTienDoBaiHoc } from '../../services/lopHocService';
import { xemTienDoLop, capGiayLopHoc } from '../../services/chungChiService';
import XemTepTinModal from '../../components/XemTepTinModal';
import '../../components/XemTepTinModal.css';

const BIEU_TUONG_LOAI = {
  VIDEO: 'ti-player-play',
  PDF: 'ti-file-text',
  DOC: 'ti-file-text',
  QUIZ: 'ti-help-circle',
  LINK: 'ti-link',
  ZIP: 'ti-file-zip',
};

const NHAN_TRANG_THAI_LOP = {
  SAP_DIEN_RA: { text: 'Sắp diễn ra', mau: 'badge-amber' },
  HOAT_DONG: { text: 'Đang hoạt động', mau: 'badge-green' },
  DA_DONG: { text: 'Đã đóng', mau: 'badge-gray' },
};

const RING_R = 30;
const RING_C = 2 * Math.PI * RING_R;

function VongTronTienDo({ phanTram }) {
  const daHoanThanh = Math.max(0, Math.min(100, phanTram));
  const lech = RING_C - (daHoanThanh / 100) * RING_C;
  return (
    <div className="lh-ring-wrap">
      <svg width="76" height="76" viewBox="0 0 76 76">
        <circle cx="38" cy="38" r={RING_R} fill="none" stroke="rgba(255,255,255,.25)" strokeWidth="7" />
        <circle
          cx="38" cy="38" r={RING_R} fill="none" stroke="#ffffff" strokeWidth="7" strokeLinecap="round"
          strokeDasharray={RING_C} strokeDashoffset={lech} transform="rotate(-90 38 38)"
        />
      </svg>
      <span className="lh-ring-pct">{daHoanThanh}%</span>
      <span className="lh-ring-label">Hoàn thành</span>
    </div>
  );
}

// Nhóm các mục theo chương gốc để hiển thị đẹp hơn — dùng chung cho cả 2 vai trò
function nhomTheoChuong(giaoTrinh) {
  const nhom = [];
  giaoTrinh.forEach(muc => {
    const tenChuong = muc.loaiNguon === 'GOC' ? (muc.tenChuongGoc || 'Nội dung gốc') : 'Bài bổ sung của lớp';
    const n = nhom.find(x => x.ten === tenChuong);
    if (n) n.dsMuc.push(muc);
    else nhom.push({ ten: tenChuong, dsMuc: [muc] });
  });
  return nhom;
}

/**
 * Hiển thị khi lớp còn ở trạng thái SAP_DIEN_RA — học viên đã ghi danh/trả
 * phí nhưng nội dung bài học chỉ mở khi lớp chính thức khai giảng (backend
 * GiaoTrinhLaiDichVu.layGiaoTrinhLaiChoHocVien đã trả rỗng cho tới lúc đó).
 */
function TheChoKhaiGiang({ ngayBatDau }) {
  const ngay = ngayBatDau ? new Date(ngayBatDau) : null;
  const soNgayConLai = ngay ? Math.max(0, Math.ceil((ngay - new Date()) / (1000 * 60 * 60 * 24))) : null;

  return (
    <div className="card" style={{ textAlign: 'center', padding: '36px 20px' }}>
      <i className="ti ti-clock-hour-4" style={{ fontSize: 40, color: 'var(--warning, #d97706)' }} aria-hidden="true"></i>
      <div style={{ fontSize: 17, fontWeight: 600, marginTop: 12 }}>Lớp học sắp khai giảng</div>
      <p style={{ fontSize: 13, color: 'var(--text-2)', marginTop: 6, maxWidth: 420, marginLeft: 'auto', marginRight: 'auto' }}>
        Bạn đã ghi danh thành công. Nội dung bài học, video và bài kiểm tra sẽ được mở khi lớp chính thức bắt đầu.
      </p>
      {ngay && (
        <div style={{ marginTop: 18, display: 'inline-flex', gap: 24, alignItems: 'center' }}>
          <div>
            <div style={{ fontSize: 24, fontWeight: 700, color: 'var(--primary)' }}>{soNgayConLai}</div>
            <div style={{ fontSize: 11, color: 'var(--text-3)' }}>ngày nữa</div>
          </div>
          <div style={{ width: 1, height: 30, background: 'var(--border)' }} />
          <div>
            <div style={{ fontSize: 15, fontWeight: 600 }}>{ngay.toLocaleDateString('vi-VN')}</div>
            <div style={{ fontSize: 11, color: 'var(--text-3)' }}>Ngày khai giảng</div>
          </div>
        </div>
      )}
    </div>
  );
}

export default function GiaoDienLopHoc() {
  const { maLopHoc } = useParams();
  const navigate = useNavigate();
  const [vaiTroTrongLop, setVaiTroTrongLop] = useState(null); // 'GIANG_VIEN' | 'HOC_VIEN' | null (đang xác định)
  const [loi, setLoi] = useState('');

  useEffect(() => {
    let daHuy = false;
    setVaiTroTrongLop(null);
    setLoi('');

    async function xacDinhVaiTro() {
      try {
        // BƯỚC 1: Bất chấp LocalStorage, luôn thử gọi API của Giảng viên trước
        await layChiTietGiangVien(maLopHoc);

        // BƯỚC 2: Nếu Backend trả về dữ liệu thành công (HTTP 200)
        // => Xác nhận ngữ cảnh hiện tại: Người dùng là GIẢNG VIÊN của lớp này
        if (!daHuy) setVaiTroTrongLop('GIANG_VIEN');

      } catch (err) {
        // BƯỚC 3: Nếu Backend từ chối (bắn lỗi 403 Không có quyền tác giả)
        // => Họ không phải Giảng viên. Chuyển sang ngữ cảnh HỌC VIÊN.
        // (Nếu họ cũng chưa mua lớp, API bên GiaoDienHocVien sẽ tự động xử lý báo lỗi sau).
        if (!daHuy) setVaiTroTrongLop('HOC_VIEN');
      }
    }

    xacDinhVaiTro();
    return () => { daHuy = true; };
  }, [maLopHoc]);

  if (loi) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <div className="topbar">
          <span className="t-title"><i className="ti ti-school" aria-hidden="true"></i> Giao diện lớp học</span>
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
        </div>
        <div style={{ padding: 20 }}><div className="alert alert-danger">{loi}</div></div>
      </div>
    );
  }

  if (vaiTroTrongLop === null) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <div className="topbar">
          <span className="t-title"><i className="ti ti-school" aria-hidden="true"></i> Giao diện lớp học</span>
        </div>
        <p style={{ fontSize: 13, color: 'var(--text-3)', padding: 20 }}>Đang xác định vai trò của bạn trong lớp...</p>
      </div>
    );
  }

  return vaiTroTrongLop === 'GIANG_VIEN'
    ? <GiaoDienGiangVien maLopHoc={maLopHoc} />
    : <GiaoDienHocVien maLopHoc={maLopHoc} />;
}

/* ============================= HỌC VIÊN ============================= */

function GiaoDienHocVien({ maLopHoc }) {
  const navigate = useNavigate();
  const [giaoTrinh, setGiaoTrinh] = useState([]);
  const [thongTinLop, setThongTinLop] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [baiHocDangXem, setBaiHocDangXem] = useState(null);

  // Cache vị trí dừng mới nhất theo maMuc, cập nhật ĐỒNG BỘ qua ref (không chờ
  // React re-render). Dù setGiaoTrinh ở dưới đã patch state, vẫn giữ thêm ref
  // này làm lớp bảo hiểm: đọc trực tiếp từ ref lúc mở lại bài học đảm bảo luôn
  // lấy đúng giá trị mới nhất, không phụ thuộc giaoTrinh đã kịp re-render hay
  // đóng/mở modal có xảy ra trong cùng 1 chu kỳ render hay không.
  const viTriMoiNhatRef = useRef({});

  const [tienDoGiay, setTienDoGiay] = useState(null);
  const [dangCapGiay, setDangCapGiay] = useState(false);
  const [loiCapGiay, setLoiCapGiay] = useState('');
  const [giayDaCap, setGiayDaCap] = useState(null);

  useEffect(() => {
    let daHuy = false;
    setDangTai(true);
    setLoi('');
    Promise.all([
      layGiaoTrinhHocVien(maLopHoc),
      layLopHocVien().catch(() => []), // banner thông tin lớp — không chặn trang nếu lỗi
    ])
      .then(([ds, dsLop]) => {
        if (daHuy) return;
        setGiaoTrinh(ds || []);
        const lop = (dsLop || []).find(l => String(l.maLopHoc) === String(maLopHoc));
        setThongTinLop(lop || null);
      })
      .catch(err => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
  }, [maLopHoc]);

  const nhomChuong = useMemo(() => nhomTheoChuong(giaoTrinh), [giaoTrinh]);

  const tongSoMuc = giaoTrinh.length;
  const soHoanThanh = giaoTrinh.filter(m => m.hoanThanh).length;
  const soQuiz = giaoTrinh.filter(m => m.loai === 'QUIZ').length;
  const soBoSung = giaoTrinh.filter(m => m.loaiNguon === 'BO_SUNG').length;
  const tiLeHoanThanh = tongSoMuc > 0 ? Math.round((soHoanThanh / tongSoMuc) * 100) : 0;
  const mucTiepTheo = giaoTrinh.find(m => !m.hoanThanh);

  function diDenBaiHoc(muc) {
    if (muc.loai === 'QUIZ') {
      // Quiz thì vẫn chuyển trang vì nó là form làm bài
      navigate(`/lop-hoc/${maLopHoc}/quiz/${muc.loaiNguon === 'GOC' ? 'goc' : 'bo-sung'}/${muc.maThamChieu}`);
    } else {
      // Video, PDF, DOC... thì mở Modal xem trực tiếp.
      // Ưu tiên vị trí dừng mới nhất trong ref (nếu đã từng xem trong phiên
      // này) thay vì viTriDung có sẵn trên `muc` — tránh mọi khả năng dữ
      // liệu chưa kịp đồng bộ vào state `giaoTrinh` do timing re-render.
      const viTriMoiNhat = viTriMoiNhatRef.current[muc.maMuc];
      setBaiHocDangXem(viTriMoiNhat != null ? { ...muc, viTriDung: viTriMoiNhat } : muc);
    }
  }

  // THÊM MỚI: Hàm xử lý khi nhận được tín hiệu "Đã xem hết" từ Modal
  async function xuLyHoanThanhBaiHoc(muc) {
    if (muc.hoanThanh) return; // Nếu đã hoàn thành trước đó rồi thì bỏ qua

    try {
      await capNhatTienDoBaiHoc(maLopHoc, muc.maThamChieu, muc.loaiNguon, {
        hoanThanh: true,
        viTriDung: null
      });

      // Cập nhật dấu tick xanh trên giao diện
      setGiaoTrinh(prev => prev.map(item =>
        item.maMuc === muc.maMuc ? { ...item, hoanThanh: true } : item
      ));

      // FIX: thiếu dòng này trước đây — dấu tick từng bài đã cập nhật đúng
      // ở trên, nhưng thanh % tổng + nút "Nhận giấy xác nhận" (phụ thuộc
      // tienDoGiay.daDuDieuKien, tính từ API riêng) vẫn giữ giá trị CŨ vì
      // không có gì gọi lại API tiến độ. Học viên hoàn thành bài CUỐI CÙNG
      // (đủ điều kiện nhận giấy) vẫn không thấy nút hiện ra cho tới khi tải
      // lại trang. Gọi lại đúng ngay sau khi đánh dấu hoàn thành thành công.
      taiLaiTienDoGiay();
    } catch (error) {
      console.error("Lỗi cập nhật tiến độ:", error);
    }
  }

  // Lưu vị trí dừng (giây với video, số trang với PDF) — gọi định kỳ trong
  // lúc xem, không chặn giao diện nếu lỗi, chỉ log lại để không làm phiền.
  function xuLyCapNhatViTri(muc, viTriMoi) {
    // Ghi vào ref TRƯỚC TIÊN, đồng bộ ngay lập tức (không đợi setState/re-render)
    // — đây là nguồn dữ liệu ĐÁNG TIN CẬY NHẤT khi mở lại bài học trong cùng phiên.
    viTriMoiNhatRef.current[muc.maMuc] = viTriMoi;

    capNhatTienDoBaiHoc(maLopHoc, muc.maThamChieu, muc.loaiNguon, {
      hoanThanh: muc.hoanThanh ?? false,
      viTriDung: viTriMoi,
    }).catch(error => {
      console.error('Lưu vị trí dừng thất bại:', error);
    });

    // Đồng thời patch state `giaoTrinh` để UI (nếu có hiển thị vị trí dừng ở
    // đâu đó khác ngoài modal) cũng phản ánh đúng, không chỉ riêng ref.
    setGiaoTrinh(prev => prev.map(item =>
      item.maMuc === muc.maMuc ? { ...item, viTriDung: viTriMoi } : item
    ));
  }

  function taiLaiTienDoGiay() {
    xemTienDoLop(maLopHoc).then(setTienDoGiay).catch(() => { }); // im lặng — không chặn trang nếu tải thất bại
  }

  useEffect(() => {
    if (!dangTai) taiLaiTienDoGiay();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dangTai]);

  async function xuLyNhanGiayLop() {
    setDangCapGiay(true);
    setLoiCapGiay('');
    try {
      const giay = await capGiayLopHoc(maLopHoc);
      setGiayDaCap(giay);
    } catch (err) {
      setLoiCapGiay(layThongBaoLoi(err));
    } finally {
      setDangCapGiay(false);
    }
  }

  const nhanTrangThai = thongTinLop && NHAN_TRANG_THAI_LOP[thongTinLop.trangThai];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-school" aria-hidden="true"></i> Giao diện lớp học</span>
        <span className="t-badge"><i className="ti ti-user" aria-hidden="true"></i> Học viên</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/lop-hoc/${maLopHoc}/dien-dan`)}>
          <i className="ti ti-messages" aria-hidden="true"></i> Diễn đàn
        </button>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
      </div>

      <div style={{ padding: 20, maxWidth: 900, width: '100%', margin: '0 auto' }}>
        {loi && <div className="alert alert-danger">{loi}</div>}

        {dangTai ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải giáo trình...</p>
        ) : (
          <>
            {/* ── Hero: thông tin lớp + tiến độ ── */}
            <div
              className={`lh-hero${thongTinLop?.anhBiaKhoaHoc ? ' has-cover' : ''}`}
              style={thongTinLop?.anhBiaKhoaHoc ? { backgroundImage: `url(${thongTinLop.anhBiaKhoaHoc})` } : undefined}
            >
              <div className="lh-hero-body">
                <div className="lh-hero-eyebrow">
                  <i className="ti ti-users-group" aria-hidden="true"></i>
                  {thongTinLop ? `Mã lớp ${thongTinLop.maLop}` : 'Lớp học'}
                </div>
                <div className="lh-hero-title">{thongTinLop?.tenKhoaHoc || 'Đang tải thông tin lớp...'}</div>
                <div className="lh-hero-meta">
                  <span><i className="ti ti-user" aria-hidden="true"></i> GV: {thongTinLop?.tenGiangVien || '—'}</span>
                  {/* Lớp chưa khai giảng: backend đã gate rỗng nên tongSoMuc luôn = 0 —
                      ẩn hẳn dòng "bài học" thay vì hiện "0 bài học" gây hiểu lầm,
                      nhất quán với thẻ chờ khai giảng bên dưới. */}
                  {thongTinLop?.trangThai !== 'SAP_DIEN_RA' && (
                    <span><i className="ti ti-book-2" aria-hidden="true"></i> {tongSoMuc} bài học</span>
                  )}
                  {thongTinLop?.diemTrungBinh > 0 && (
                    <span><i className="ti ti-star" aria-hidden="true"></i> {thongTinLop.diemTrungBinh.toFixed(1)} ({thongTinLop.soLuongDanhGia})</span>
                  )}
                </div>
                {nhanTrangThai && (
                  <div className="lh-hero-badges">
                    <span className="badge">{nhanTrangThai.text}</span>
                  </div>
                )}
              </div>
              <VongTronTienDo phanTram={tiLeHoanThanh} />
            </div>

            {/* ── Lớp chưa khai giảng: khoá nội dung, chỉ hiện thẻ chờ ── */}
            {thongTinLop?.trangThai === 'SAP_DIEN_RA' ? (
              <TheChoKhaiGiang ngayBatDau={thongTinLop?.ngayBatDau} />
            ) : (
              <>
                {/* ── Tiếp tục học ── */}
                {mucTiepTheo && (
                  <div className="lh-continue" onClick={() => diDenBaiHoc(mucTiepTheo)}>
                    <div className="lc-icon">
                      <i className={`ti ${BIEU_TUONG_LOAI[mucTiepTheo.loai] || 'ti-file'}`} aria-hidden="true"></i>
                    </div>
                    <div className="lc-body">
                      <div className="lc-eyebrow">Tiếp tục học</div>
                      <div className="lc-title">{mucTiepTheo.tieuDe}</div>
                    </div>
                    <button className="btn btn-primary btn-sm">
                      Vào học <i className="ti ti-arrow-right" aria-hidden="true"></i>
                    </button>
                  </div>
                )}
                {!mucTiepTheo && tongSoMuc > 0 && (
                  <div className="alert alert-success">
                    <i className="ti ti-confetti" aria-hidden="true"></i>
                    Bạn đã hoàn thành toàn bộ nội dung của lớp học này!
                  </div>
                )}

                {/* ── Giấy xác nhận hoàn thành lớp học ── */}
                {tienDoGiay && (
                  <div className="card sm" style={{ marginBottom: 16 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                      <span style={{ fontSize: 13, fontWeight: 600 }}>
                        <i className="ti ti-certificate" aria-hidden="true"></i> Giấy xác nhận hoàn thành lớp học
                      </span>
                      <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--brand)' }}>
                        {Number(tienDoGiay.phanTramHoanThanh).toFixed(0)}%
                      </span>
                    </div>
                    <div style={{ fontSize: 12, color: 'var(--text-3)', marginBottom: 10 }}>
                      {tienDoGiay.baiHoanThanh}/{tienDoGiay.tongBaiHoc} bài học (gồm bài bổ sung)
                      {tienDoGiay.tongQuiz > 0 && ` · ${tienDoGiay.quizDat}/${tienDoGiay.tongQuiz} bài kiểm tra đạt`}
                    </div>

                    {loiCapGiay && <div className="alert alert-danger" style={{ marginBottom: 8 }}>{loiCapGiay}</div>}

                    {giayDaCap ? (
                      <a className="btn btn-primary btn-sm" href={giayDaCap.filePdfUrl} target="_blank" rel="noreferrer">
                        <i className="ti ti-download" aria-hidden="true"></i> Tải giấy xác nhận (PDF)
                      </a>
                    ) : tienDoGiay.daDuDieuKien ? (
                      <button className="btn btn-primary btn-sm" onClick={xuLyNhanGiayLop} disabled={dangCapGiay}>
                        <i className="ti ti-certificate" aria-hidden="true"></i>
                        {dangCapGiay ? 'Đang xử lý...' : 'Nhận giấy xác nhận'}
                      </button>
                    ) : (
                      <p style={{ fontSize: 11, color: 'var(--text-3)', margin: 0 }}>
                        Cần hoàn thành 100% học liệu của lớp (gồm bài bổ sung) và đạt mọi bài kiểm tra để đủ điều kiện.
                      </p>
                    )}
                  </div>
                )}

                {/* ── Thống kê nhanh ── */}
                {tongSoMuc > 0 && (
                  <div className="lh-stats">
                    <div className="lh-stat">
                      <div className="lh-stat-num">{tongSoMuc}</div>
                      <div className="lh-stat-label">Tổng số bài học</div>
                    </div>
                    <div className="lh-stat done">
                      <div className="lh-stat-num">{soHoanThanh}</div>
                      <div className="lh-stat-label">Đã hoàn thành</div>
                    </div>
                    <div className="lh-stat quiz">
                      <div className="lh-stat-num">{soQuiz}</div>
                      <div className="lh-stat-label">Bài kiểm tra</div>
                    </div>
                    <div className="lh-stat">
                      <div className="lh-stat-num">{soBoSung}</div>
                      <div className="lh-stat-label">Tài liệu bổ sung</div>
                    </div>
                  </div>
                )}

                {/* ── Giáo trình dạng timeline ── */}
                {giaoTrinh.length === 0 ? (
                  <div className="empty-state">
                    <i className="ti ti-books" style={{ fontSize: 28 }} aria-hidden="true"></i>
                    <p style={{ marginTop: 6 }}>Giáo trình đang được cập nhật.</p>
                  </div>
                ) : (
                  <div className="lh-timeline">
                    {nhomChuong.map((nhom, idx) => {
                      const soLuongHoanThanh = nhom.dsMuc.filter(m => m.hoanThanh).length;
                      return (
                        <div key={idx} className="lh-chapter">
                          <div className={`lh-chapter-node${nhom.ten === 'Tài liệu bổ sung của lớp' ? ' alt' : ''}`}>
                            {nhom.ten === 'Tài liệu bổ sung của lớp' ? <i className="ti ti-plus" aria-hidden="true"></i> : idx + 1}
                          </div>
                          <div className="lh-chapter-title">
                            {nhom.ten}
                            <span className="count">{soLuongHoanThanh}/{nhom.dsMuc.length} hoàn thành</span>
                          </div>
                          {nhom.dsMuc.map(muc => (
                            <div
                              key={muc.maMuc}
                              className={`lh-lesson${muc.hoanThanh ? ' done' : ''}`}
                              onClick={() => diDenBaiHoc(muc)}
                            >
                              <div className="ll-icon">
                                <i className={`ti ${BIEU_TUONG_LOAI[muc.loai] || 'ti-file'}`} aria-hidden="true"></i>
                              </div>
                              <div className="ll-title">{muc.tieuDe}</div>
                              <div className="ll-tags">
                                {muc.loaiNguon === 'BO_SUNG' && <span className="badge badge-blue">Bổ sung</span>}
                                {muc.loai === 'QUIZ' && <span className="badge badge-amber">Quiz</span>}
                                {muc.hoanThanh
                                  ? <i className="ti ti-circle-check ll-check" aria-hidden="true"></i>
                                  : <i className="ti ti-chevron-right" style={{ color: 'var(--text-3)' }} aria-hidden="true"></i>}
                              </div>
                            </div>
                          ))}
                        </div>
                      );
                    })}
                  </div>
                )}

                <DanhGiaLop maLopHoc={maLopHoc} />
              </>
            )}
          </>
        )}
      </div>
      {/* 3. THÊM MODAL VÀO CUỐI COMPONENT (Trước thẻ đóng </div> ngoài cùng) */}
      {baiHocDangXem && (
        <XemTepTinModal
          baiHoc={baiHocDangXem}
          onClose={() => setBaiHocDangXem(null)}
          onHoanThanh={() => xuLyHoanThanhBaiHoc(baiHocDangXem)}
          onCapNhatViTri={(viTriMoi) => xuLyCapNhatViTri(baiHocDangXem, viTriMoi)}
          hienNutDanhDauDaDocXong
        />
      )}
    </div>
  );
}

/* ══════════════════════ Đánh giá lớp học (Học viên) ══════════════════════ */

function ChonSao({ giaTri, onChon, doNet }) {
  return (
    <div style={{ display: 'flex', gap: 4 }}>
      {[1, 2, 3, 4, 5].map(n => (
        <i
          key={n}
          className="ti ti-star-filled"
          aria-hidden="true"
          onClick={onChon ? () => onChon(n) : undefined}
          style={{
            fontSize: doNet || 22,
            color: n <= giaTri ? '#f59e0b' : 'var(--border)',
            cursor: onChon ? 'pointer' : 'default',
          }}
        />
      ))}
    </div>
  );
}

function DanhGiaLop({ maLopHoc }) {
  const [danhGiaCuaToi, setDanhGiaCuaToi] = useState(null); // null = chưa có
  const [dangTai, setDangTai] = useState(true);
  const [dsDanhGia, setDsDanhGia] = useState([]);

  const [dangSua, setDangSua] = useState(false);
  const [soSao, setSoSao] = useState(5);
  const [nhanXet, setNhanXet] = useState('');
  const [dangGui, setDangGui] = useState(false);
  const [loi, setLoi] = useState('');
  const [thanhCong, setThanhCong] = useState(false);

  useEffect(() => {
    let daHuy = false;
    Promise.all([
      layDanhGiaCuaToiLop(maLopHoc).catch(() => null),
      layDanhGiaLop(maLopHoc, { size: 5 }).catch(() => null),
    ]).then(([cuaToi, ds]) => {
      if (daHuy) return;
      setDanhGiaCuaToi(cuaToi || null);
      if (cuaToi) { setSoSao(cuaToi.soSao); setNhanXet(cuaToi.nhanXet || ''); }
      setDsDanhGia(ds?.content || []);
    }).finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
  }, [maLopHoc]);

  async function xuLyGui(e) {
    e.preventDefault();
    setDangGui(true);
    setLoi('');
    setThanhCong(false);
    try {
      const ketQua = danhGiaCuaToi
        ? await capNhatDanhGiaLop(danhGiaCuaToi.maDanhGia, { soSao, nhanXet: nhanXet.trim() })
        : await taoDanhGiaLop(maLopHoc, { soSao, nhanXet: nhanXet.trim() });
      setDanhGiaCuaToi(ketQua);
      setDangSua(false);
      setThanhCong(true);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  }

  if (dangTai) return null;

  return (
    <div className="card sm" style={{ marginTop: 20 }}>
      <div className="card-title"><i className="ti ti-star" aria-hidden="true"></i> Đánh giá lớp học</div>

      {loi && <div className="alert alert-danger">{loi}</div>}
      {thanhCong && <div className="alert alert-success">Cảm ơn bạn đã đánh giá lớp học!</div>}

      {danhGiaCuaToi && !dangSua ? (
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12, justifyContent: 'space-between' }}>
          <div>
            <ChonSao giaTri={danhGiaCuaToi.soSao} />
            {danhGiaCuaToi.nhanXet && (
              <p style={{ fontSize: 13, color: 'var(--text-2)', marginTop: 8 }}>{danhGiaCuaToi.nhanXet}</p>
            )}
          </div>
          <button className="btn btn-secondary btn-sm" onClick={() => setDangSua(true)}>Sửa đánh giá</button>
        </div>
      ) : (
        <form onSubmit={xuLyGui}>
          <ChonSao giaTri={soSao} onChon={setSoSao} doNet={26} />
          <div className="form-group" style={{ marginTop: 12 }}>
            <textarea className="form-input" style={{ height: 'auto', padding: '8px 10px', resize: 'vertical' }}
              rows={3} maxLength={1000} value={nhanXet} onChange={e => setNhanXet(e.target.value)}
              placeholder="Chia sẻ trải nghiệm của bạn về lớp học này..." />
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-primary btn-sm" type="submit" disabled={dangGui}>
              {dangGui ? 'Đang gửi...' : danhGiaCuaToi ? 'Cập nhật đánh giá' : 'Gửi đánh giá'}
            </button>
            {danhGiaCuaToi && (
              <button className="btn btn-secondary btn-sm" type="button" onClick={() => setDangSua(false)}>Hủy</button>
            )}
          </div>
        </form>
      )}

      {dsDanhGia.length > 0 && (
        <div style={{ marginTop: 18, paddingTop: 14, borderTop: '1px solid var(--border)' }}>
          <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', marginBottom: 10 }}>
            Đánh giá gần đây
          </div>
          {dsDanhGia.map(d => (
            <div key={d.maDanhGia} style={{ display: 'flex', gap: 10, marginBottom: 12 }}>
              <div className="avatar" style={{ width: 28, height: 28, fontSize: 11, flexShrink: 0 }}>
                {d.tenHocVien?.[0]?.toUpperCase() || '?'}
              </div>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span style={{ fontSize: 12.5, fontWeight: 600 }}>{d.tenHocVien || 'Học viên'}</span>
                  <ChonSao giaTri={d.soSao} doNet={12} />
                </div>
                {d.nhanXet && <p style={{ fontSize: 12.5, color: 'var(--text-2)', marginTop: 3 }}>{d.nhanXet}</p>}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

/* ============================= GIẢNG VIÊN ============================= */

function GiaoDienGiangVien({ maLopHoc }) {
  const navigate = useNavigate();
  const [thongTinLop, setThongTinLop] = useState(null);
  const [giaoTrinh, setGiaoTrinh] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [baiHocDangXem, setBaiHocDangXem] = useState(null);

  useEffect(() => {
    let daHuy = false;
    setDangTai(true);
    setLoi('');
    Promise.all([layChiTietGiangVien(maLopHoc), layGiaoTrinhGiangVien(maLopHoc)])
      .then(([lop, ds]) => {
        if (daHuy) return;
        setThongTinLop(lop);
        setGiaoTrinh(ds || []);
      })
      .catch(err => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
  }, [maLopHoc]);

  const nhomChuong = useMemo(() => nhomTheoChuong(giaoTrinh), [giaoTrinh]);
  const soQuiz = giaoTrinh.filter(m => m.loai === 'QUIZ').length;
  const soBoSung = giaoTrinh.filter(m => m.loaiNguon === 'BO_SUNG').length;
  const soAn = giaoTrinh.filter(m => !m.anHien).length;
  const nhanTrangThai = thongTinLop && NHAN_TRANG_THAI_LOP[thongTinLop.trangThai];

  function xemTaiNguyen(muc) {
    if (muc.loai === 'QUIZ') {
      // Nếu là Quiz, chuyển sang màn hình làm bài/xem trước Quiz
      navigate(`/lop-hoc/${maLopHoc}/quiz/${muc.loaiNguon === 'GOC' ? 'goc' : 'bo-sung'}/${muc.maThamChieu}`);
    } else {
      // Nếu là Video, PDF, DOC... thì bật Modal lên để xem luôn
      setBaiHocDangXem(muc);
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-school" aria-hidden="true"></i> Giao diện lớp học</span>
        <span className="t-badge amber"><i className="ti ti-chalkboard" aria-hidden="true"></i> Giảng viên</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/lop-hoc/${maLopHoc}/dien-dan`)}>
          <i className="ti ti-messages" aria-hidden="true"></i> Diễn đàn
        </button>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/lop-hoc/giang-vien')}>Danh sách lớp</button>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
      </div>

      <div style={{ padding: 20, maxWidth: 900, width: '100%', margin: '0 auto' }}>
        {loi && <div className="alert alert-danger">{loi}</div>}

        {dangTai ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải thông tin lớp...</p>
        ) : (
          <>
            {/* ── Hero: thông tin lớp + sĩ số ── */}
            <div
              className={`lh-hero${thongTinLop?.anhBiaKhoaHoc ? ' has-cover' : ''}`}
              style={thongTinLop?.anhBiaKhoaHoc ? { backgroundImage: `url(${thongTinLop.anhBiaKhoaHoc})` } : undefined}
            >
              <div className="lh-hero-body">
                <div className="lh-hero-eyebrow">
                  <i className="ti ti-users-group" aria-hidden="true"></i>
                  {thongTinLop ? `Mã lớp ${thongTinLop.maLop}` : 'Lớp học'}
                </div>
                <div className="lh-hero-title">{thongTinLop?.tenKhoaHoc || '(Lớp độc lập)'}</div>
                <div className="lh-hero-meta">
                  <span><i className="ti ti-users" aria-hidden="true"></i> {thongTinLop?.soThanhVien ?? 0}/{thongTinLop?.siSo || '∞'} thành viên</span>
                  <span><i className="ti ti-book-2" aria-hidden="true"></i> {giaoTrinh.length} bài học</span>
                  <span><i className="ti ti-cash" aria-hidden="true"></i> {Number(thongTinLop?.phiLopHoc) === 0 ? 'Miễn phí' : `${Number(thongTinLop?.phiLopHoc || 0).toLocaleString('vi-VN')} đ`}</span>
                </div>
                {nhanTrangThai && (
                  <div className="lh-hero-badges">
                    <span className="badge">{nhanTrangThai.text}</span>
                  </div>
                )}
              </div>
              <div className="lh-ring-wrap">
                <div style={{ width: 76, height: 76, borderRadius: '50%', background: 'rgba(255,255,255,.16)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <i className="ti ti-star" style={{ fontSize: 22 }} aria-hidden="true"></i>
                </div>
                <span className="lh-ring-label">{thongTinLop?.diemTrungBinh ? `${thongTinLop.diemTrungBinh.toFixed(1)} ★ (${thongTinLop.soLuongDanhGia})` : 'Chưa có đánh giá'}</span>
              </div>
            </div>

            {/* ── Thao tác chuyển trang cấu hình lớp học dành cho giảng viên ── */}
            <div style={{ marginTop: -8, marginBottom: 20 }}>
              <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/lop-hoc/${maLopHoc}/quan-ly`)}>
                <i className="ti ti-settings" aria-hidden="true"></i> Cấu hình lớp học
              </button>
              <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/lop-hoc/${maLopHoc}/quan-ly?tab=thanh-vien`)}>
                <i className="ti ti-users"></i> Danh sách học viên
              </button>
            </div>

            {/* ── Thống kê nhanh ── */}
            {giaoTrinh.length > 0 && (
              <div className="lh-stats">
                <div className="lh-stat">
                  <div className="lh-stat-num">{giaoTrinh.length}</div>
                  <div className="lh-stat-label">Tổng số bài học</div>
                </div>
                <div className="lh-stat quiz">
                  <div className="lh-stat-num">{soQuiz}</div>
                  <div className="lh-stat-label">Bài kiểm tra</div>
                </div>
                <div className="lh-stat">
                  <div className="lh-stat-num">{soBoSung}</div>
                  <div className="lh-stat-label">Tài liệu bổ sung</div>
                </div>
                <div className="lh-stat">
                  <div className="lh-stat-num">{soAn}</div>
                  <div className="lh-stat-label">Đang ẩn với học viên</div>
                </div>
              </div>
            )}

            {/* ── Giáo trình dạng timeline (chỉ xem — chỉnh sửa tại "Giáo trình lai") ── */}
            {giaoTrinh.length === 0 ? (
              <div className="empty-state">
                <i className="ti ti-books" style={{ fontSize: 28 }} aria-hidden="true"></i>
                <p style={{ marginTop: 6 }}>Lớp học chưa có nội dung nào. Vào "Giáo trình lai" để thêm bài học.</p>
              </div>
            ) : (
              <div className="lh-timeline">
                {nhomChuong.map((nhom, idx) => (
                  <div key={idx} className="lh-chapter">
                    <div className={`lh-chapter-node${nhom.ten === 'Bài bổ sung của lớp' ? ' alt' : ''}`}>
                      {nhom.ten === 'Bài bổ sung của lớp' ? <i className="ti ti-plus" aria-hidden="true"></i> : idx + 1}
                    </div>
                    <div className="lh-chapter-title">
                      {nhom.ten}
                      <span className="count">{nhom.dsMuc.length} bài học</span>
                    </div>
                    {nhom.dsMuc.map(muc => (
                      <div
                        key={muc.maMuc}
                        className="lh-lesson"
                        style={!muc.anHien ? { opacity: .55 } : undefined}

                        // 3. THAY THẾ CHỖ NÀY: GỌI HÀM XEM TÀI NGUYÊN
                        onClick={() => xemTaiNguyen(muc)}
                      >
                        <div className="ll-icon">
                          <i className={`ti ${BIEU_TUONG_LOAI[muc.loai] || 'ti-file'}`} aria-hidden="true"></i>
                        </div>
                        <div className="ll-title">{muc.tieuDe}</div>
                        <div className="ll-tags">
                          {muc.loaiNguon === 'BO_SUNG' && <span className="badge badge-blue">Bổ sung</span>}
                          {muc.loai === 'QUIZ' && <span className="badge badge-amber">Quiz</span>}
                          {!muc.anHien && <span className="badge badge-gray">Đang ẩn</span>}
                        </div>
                      </div>
                    ))}
                  </div>
                ))}
              </div>
            )}
          </>
        )}
        {/* BỔ SUNG ĐOẠN NÀY ĐỂ HIỂN THỊ MODAL */}
        {baiHocDangXem && (
          <XemTepTinModal
            baiHoc={baiHocDangXem}
            onClose={() => setBaiHocDangXem(null)}
            hienNutDanhDauDaDocXong
          />
        )}
      </div>
    </div>
  );
}