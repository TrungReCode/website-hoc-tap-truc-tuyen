import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  layGiaoTrinhGiangVien, capNhatCauHinhBaiGoc, themBaiBoSung, xoaBaiBoSung, capNhatBaiBoSung,
  themCauHoiBoSung, layCauHoiBoSung, xoaCauHoiBoSung,
} from '../../../services/giaoTrinhLaiService';
import { layDeBaiGoc } from '../../../services/quizLopService';
import { layThongBaoLoi } from '../../../utils/layThongBaoLoi';
import ThanhTienDoTaiLen from '../../../components/ThanhTienDoTaiLen';

// 1. IMPORT MODAL XEM TỆP TIN
import XemTepTinModal from '../../../components/XemTepTinModal';
import '../../../components/XemTepTinModal.css';

const BIEU_TUONG = { VIDEO: 'ti-player-play', PDF: 'ti-file-text', DOC: 'ti-file-text', QUIZ: 'ti-help-circle', LINK: 'ti-link', ZIP: 'ti-file-zip' };

export default function GiaoTrinhLaiGiangVien() {
  const { maLopHoc } = useParams();
  const navigate = useNavigate();
  const [danhSach, setDanhSach] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [hienFormThemBai, setHienFormThemBai] = useState(false);

  // 2. STATE QUẢN LÝ BÀI HỌC ĐANG XEM TRƯỚC
  const [baiHocDangXem, setBaiHocDangXem] = useState(null);
  // Quiz bổ sung đang được quản lý câu hỏi
  const [quizDangQuanLy, setQuizDangQuanLy] = useState(null);
  // Quiz GỐC đang được xem trước (chỉ xem, không sửa — câu hỏi thuộc khóa học gốc)
  const [deGocDangXem, setDeGocDangXem] = useState(null);

  function taiLai() {
    setDangTai(true);
    layGiaoTrinhGiangVien(maLopHoc).then(setDanhSach).catch(e => setLoi(layThongBaoLoi(e))).finally(() => setDangTai(false));
  }
  useEffect(taiLai, [maLopHoc]);

  async function xuLyToggleAn(muc) {
    if (muc.loaiNguon !== 'GOC') return;
    try {
      await capNhatCauHinhBaiGoc(maLopHoc, muc.maThamChieu, { anBaiHoc: muc.anHien, viTriMoi: null });
      taiLai();
    } catch (e) { setLoi(layThongBaoLoi(e)); }
  }

  async function xuLyToggleAnBoSung(muc) {
    try {
      // Bài bổ sung dùng thẳng cờ anHien (không nghịch đảo như anBaiHoc của bài gốc)
      // nên chỉ cần gửi giá trị đảo ngược để bật/tắt hiển thị.
      await capNhatBaiBoSung(muc.maThamChieu, { tieuDe: muc.tieuDe, moTa: muc.moTa, anHien: !muc.anHien });
      taiLai();
    } catch (e) { setLoi(layThongBaoLoi(e)); }
  }

  async function xuLyDiChuyen(muc, huong) {
    const idx = danhSach.findIndex(m => m.maMuc === muc.maMuc);
    if (huong === 'len' && idx === 0) return;
    if (huong === 'xuong' && idx === danhSach.length - 1) return;

    // Fractional indexing: đặt mục đang di chuyển vào giữa "hàng xóm" (mục nó
    // vừa vượt qua) và "hàng xóm của hàng xóm" (mục xa hơn 1 bậc theo hướng
    // di chuyển) — không đụng tới viTri của 2 mục còn lại nên không cần cập
    // nhật chúng, tránh việc 2 mục bị trùng y hệt vị trí như trước đây.
    const hangXom = danhSach[huong === 'len' ? idx - 1 : idx + 1];
    const xaHon = danhSach[huong === 'len' ? idx - 2 : idx + 2];

    let viTriSau, viTriTruoc;
    if (huong === 'len') {
      viTriTruoc = hangXom.viTri;
      viTriSau = xaHon ? xaHon.viTri : null;
    } else {
      viTriSau = hangXom.viTri;
      viTriTruoc = xaHon ? xaHon.viTri : null;
    }

    try {
      if (muc.loaiNguon === 'GOC') {
        const viTriMoi = tinhViTriGiua(viTriSau, viTriTruoc);
        // Lưu ý: anBaiHoc (BE) và anHien (FE) là 2 mặt đối lập của cùng 1 cờ,
        // nên phải gửi !anHien để GIỮ NGUYÊN trạng thái ẩn/hiện khi di chuyển.
        await capNhatCauHinhBaiGoc(maLopHoc, muc.maThamChieu, { anBaiHoc: !muc.anHien, viTriMoi });
      } else {
        await capNhatBaiBoSung(muc.maThamChieu, {
          tieuDe: muc.tieuDe, moTa: muc.moTa, anHien: muc.anHien,
          viTriSau, viTriTruoc,
        });
      }
      taiLai();
    } catch (e) { setLoi(layThongBaoLoi(e)); }
  }

  function tinhViTriGiua(sau, truoc) {
    if (sau == null && truoc == null) return 0;
    if (sau == null) return truoc - 1;
    if (truoc == null) return sau + 1;
    return (sau + truoc) / 2;
  }

  async function xuLyXoaBoSung(muc) {
    if (!window.confirm('Xóa bài học bổ sung này?')) return;
    try { await xoaBaiBoSung(muc.maThamChieu); taiLai(); }
    catch (e) { setLoi(layThongBaoLoi(e)); }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/lop-hoc/${maLopHoc}/quan-ly`)}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i>
        </button>
        <span className="t-title">Giáo trình lai</span>
        <button className="btn btn-primary btn-sm" onClick={() => setHienFormThemBai(true)}>
          <i className="ti ti-plus" aria-hidden="true"></i> Thêm bài bổ sung
        </button>
      </div>

      <div className="sc-body">
        {loi && <div className="alert alert-danger">{loi}</div>}
        <p style={{ fontSize: 12, color: 'var(--text-3)', marginBottom: 12 }}>
          Bài <span style={{ background: '#e0f2fe', color: '#075985', padding: '1px 6px', borderRadius: 4 }}>GỐC</span> lấy từ khóa học gốc.
          Bài <span style={{ background: '#fce7f3', color: '#9d174d', padding: '1px 6px', borderRadius: 4 }}>BỔ SUNG</span> do bạn tự thêm vào lớp.
        </p>

        {dangTai ? <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
        : danhSach.map((muc, idx) => (
          <div key={muc.maMuc} className="drag-item" style={{ opacity: !muc.anHien ? 0.5 : 1 }}>
            <span className="drag-handle"><i className="ti ti-grip-vertical" aria-hidden="true"></i></span>
            <span className={`di-type ${muc.loaiNguon === 'GOC' ? 'orig' : 'added'}`}>
              {muc.loaiNguon === 'GOC' ? 'GỐC' : 'BỔ SUNG'}
            </span>

            {/* 3. THÊM SỰ KIỆN ONCLICK ĐỂ XEM FILE (Quiz bổ sung mở panel quản lý câu hỏi thay vì xem file) */}
            <div
              style={{ flex: 1, display: 'flex', alignItems: 'center', cursor: 'pointer' }}
              onClick={() => {
                if (muc.loai === 'QUIZ' && muc.loaiNguon === 'BO_SUNG') {
                  setQuizDangQuanLy(muc);
                } else if (muc.loai === 'QUIZ' && muc.loaiNguon === 'GOC') {
                  setDeGocDangXem(muc);
                } else if (muc.loai === 'LINK') {
                  window.open(muc.duongDan || muc.duongDanLink, '_blank');
                } else {
                  setBaiHocDangXem(muc);
                }
              }}
            >
              <i className={`ti ${BIEU_TUONG[muc.loai] || 'ti-file'}`} style={{ color: 'var(--primary)', marginRight: 8 }} aria-hidden="true"></i>
              <span style={{ fontSize: 13, color: 'var(--primary)' }}>{muc.tieuDe}</span>
            </div>

            {!muc.anHien && <span className="badge badge-gray">Ẩn</span>}
            <div className="di-actions">
              {muc.loaiNguon === 'GOC' && (
                <>
                  <button className="btn btn-secondary btn-sm" title={muc.anHien ? 'Ẩn bài học này' : 'Hiện lại'} onClick={() => xuLyToggleAn(muc)}>
                    <i className={`ti ${muc.anHien ? 'ti-eye-off' : 'ti-eye'}`} aria-hidden="true"></i>
                  </button>
                  <button className="btn btn-secondary btn-sm" disabled={idx === 0} onClick={() => xuLyDiChuyen(muc, 'len')}>
                    <i className="ti ti-chevron-up" aria-hidden="true"></i>
                  </button>
                  <button className="btn btn-secondary btn-sm" disabled={idx === danhSach.length - 1} onClick={() => xuLyDiChuyen(muc, 'xuong')}>
                    <i className="ti ti-chevron-down" aria-hidden="true"></i>
                  </button>
                </>
              )}
              {muc.loaiNguon === 'BO_SUNG' && (
                <>
                  {muc.loai === 'QUIZ' && (
                    <button className="btn btn-secondary btn-sm" onClick={() => setQuizDangQuanLy(muc)}>
                      <i className="ti ti-list-details" aria-hidden="true"></i> Câu hỏi
                    </button>
                  )}
                  <button className="btn btn-secondary btn-sm" title={muc.anHien ? 'Ẩn bài học này' : 'Hiện lại'} onClick={() => xuLyToggleAnBoSung(muc)}>
                    <i className={`ti ${muc.anHien ? 'ti-eye-off' : 'ti-eye'}`} aria-hidden="true"></i>
                  </button>
                  <button className="btn btn-secondary btn-sm" disabled={idx === 0} onClick={() => xuLyDiChuyen(muc, 'len')}>
                    <i className="ti ti-chevron-up" aria-hidden="true"></i>
                  </button>
                  <button className="btn btn-secondary btn-sm" disabled={idx === danhSach.length - 1} onClick={() => xuLyDiChuyen(muc, 'xuong')}>
                    <i className="ti ti-chevron-down" aria-hidden="true"></i>
                  </button>
                  <button className="btn btn-danger btn-sm" onClick={() => xuLyXoaBoSung(muc)}>
                    <i className="ti ti-trash" aria-hidden="true"></i>
                  </button>
                </>
              )}
            </div>
          </div>
        ))}

        {hienFormThemBai && (
          <FormThemBaiBoSung
            maLopHoc={maLopHoc}
            viTriCuoiDanh={danhSach.length > 0 ? danhSach[danhSach.length - 1].viTri : null}
            onXong={(mucMoi) => {
              setHienFormThemBai(false);
              taiLai();
              // Nếu vừa tạo 1 quiz mới, mở luôn panel quản lý câu hỏi để giảng viên thêm câu hỏi ngay
              if (mucMoi?.loai === 'QUIZ') setQuizDangQuanLy(mucMoi);
            }}
            onHuy={() => setHienFormThemBai(false)}
          />
        )}
      </div>

      {/* 4. CHÈN MODAL VÀO CUỐI COMPONENT */}
      {baiHocDangXem && (
        <XemTepTinModal
          baiHoc={baiHocDangXem}
          onClose={() => setBaiHocDangXem(null)}
          xemThu
        />
      )}

      {quizDangQuanLy && (
        <QuanLyCauHoiBoSung
          muc={quizDangQuanLy}
          onClose={() => setQuizDangQuanLy(null)}
        />
      )}

      {deGocDangXem && (
        <XemDeGoc
          maLopHoc={maLopHoc}
          muc={deGocDangXem}
          onClose={() => setDeGocDangXem(null)}
        />
      )}
    </div>
  );
}

/* ══════════════════════ Xem trước đề — Quiz thuộc bài GỐC ══════════════════════ */

function XemDeGoc({ maLopHoc, muc, onClose }) {
  const [deBai, setDeBai] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    layDeBaiGoc(maLopHoc, muc.maThamChieu)
      .then(setDeBai)
      .catch(e => setLoi(layThongBaoLoi(e)))
      .finally(() => setDangTai(false));
  }, [maLopHoc, muc.maThamChieu]);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-section">
            <i className="ti ti-help-circle" aria-hidden="true"></i>
            <span>Xem trước đề — {muc.tieuDe}</span>
          </div>
          <button className="btn-close" onClick={onClose} title="Đóng (Esc)">
            <i className="ti ti-x" aria-hidden="true"></i>
          </button>
        </div>

        <div className="modal-body" style={{ display: 'grid', gap: 12 }}>
          {loi && <div className="alert alert-danger">{loi}</div>}
          {dangTai && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải đề bài...</p>}

          {deBai && (
            <>
              <div className="alert alert-info" style={{ fontSize: 12.5 }}>
                <i className="ti ti-info-circle" aria-hidden="true"></i>
                {' '}Bài kiểm tra thuộc khóa học gốc — chỉ xem trước câu hỏi/phương án ở đây.
                Đáp án đúng được ẩn giống như trải nghiệm của học viên khi làm bài
                (điểm đạt tối thiểu: {deBai.diemToiThieu}%
                {deBai.thoiGian ? `, thời gian: ${deBai.thoiGian} phút` : ', không giới hạn thời gian'}).
                Muốn sửa câu hỏi/đáp án, vào phần quản lý nội dung khóa học gốc.
              </div>

              {deBai.danhSachCauHoi.length === 0 && (
                <div className="empty-state">
                  <i className="ti ti-help-circle" style={{ fontSize: 26 }} aria-hidden="true"></i>
                  <p style={{ marginTop: 6, fontSize: 13 }}>Bài kiểm tra này chưa có câu hỏi nào.</p>
                </div>
              )}

              {deBai.danhSachCauHoi.map((c, idx) => (
                <div key={c.maCauHoi} style={{ border: '1px solid var(--border)', borderRadius: 10, padding: 12 }}>
                  <div style={{ fontWeight: 500, fontSize: 13.5 }}>{idx + 1}. {c.noiDung}</div>
                  <div style={{ marginTop: 8, display: 'grid', gap: 4 }}>
                    {c.dapAn.map(d => (
                      <div key={d.id} style={{ fontSize: 13, padding: '4px 8px' }}>{d.noiDung}</div>
                    ))}
                  </div>
                </div>
              ))}
            </>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>
            <i className="ti ti-x" aria-hidden="true"></i> Đóng
          </button>
        </div>
      </div>
    </div>
  );
}

function FormThemBaiBoSung({ maLopHoc, viTriCuoiDanh, onXong, onHuy }) {
  const [tieuDe, setTieuDe] = useState('');
  const [loai, setLoai] = useState('VIDEO');
  const [link, setLink] = useState('');
  const [file, setFile] = useState(null);
  const [diemToiThieu, setDiemToiThieu] = useState(70);
  const [thoiGian, setThoiGian] = useState('');
  const [dangTai, setDangTai] = useState(false);
  const [tienDoTai, setTienDoTai] = useState(null);
  const [loi, setLoi] = useState('');

  async function xuLyGui(e) {
    e.preventDefault();
    if (!tieuDe.trim()) return;
    if (['VIDEO', 'PDF', 'DOC', 'ZIP'].includes(loai) && !file) { setLoi('Vui lòng chọn tệp'); return; }
    if (loai === 'LINK' && !link.trim()) { setLoi('Vui lòng nhập URL'); return; }
    setLoi('');
    setDangTai(true);
    setTienDoTai(['VIDEO', 'PDF', 'DOC', 'ZIP'].includes(loai) ? 0 : null);
    try {
      const mucMoi = await themBaiBoSung(maLopHoc, {
        tieuDe, loai, anHien: true,
        viTriSau: viTriCuoiDanh, viTriTruoc: null,
        duongDanLink: loai === 'LINK' ? link : undefined,
        diemToiThieu: loai === 'QUIZ' ? Number(diemToiThieu) : undefined,
        thoiGian: loai === 'QUIZ' && thoiGian ? Number(thoiGian) : undefined,
      }, file, (phanTram) => setTienDoTai(phanTram >= 100 ? null : phanTram));
      onXong(mucMoi);
    } catch (e) { setLoi(layThongBaoLoi(e)); }
    finally { setDangTai(false); setTienDoTai(null); }
  }

  return (
    <form onSubmit={xuLyGui} className="card sm" style={{ marginTop: 14, background: 'var(--surface)' }}>
      {loi && <div className="alert alert-danger">{loi}</div>}
      <div className="form-group">
        <label className="form-label">Tiêu đề bài học bổ sung</label>
        <input className="form-input" value={tieuDe} onChange={e => setTieuDe(e.target.value)} required />
      </div>
      <div className="form-group">
        <label className="form-label">Loại</label>
        <select className="form-input" value={loai} onChange={e => setLoai(e.target.value)}>
          {['VIDEO', 'PDF', 'DOC', 'LINK', 'ZIP', 'QUIZ'].map(l => <option key={l} value={l}>{l === 'QUIZ' ? 'QUIZ (Bài kiểm tra)' : l}</option>)}
        </select>
      </div>
      {loai === 'LINK' && (
        <div className="form-group">
          <label className="form-label">URL</label>
          <input className="form-input" type="url" value={link} onChange={e => setLink(e.target.value)} placeholder="https://..." />
        </div>
      )}
      {loai === 'QUIZ' && (
        <>
          <div className="form-group">
            <label className="form-label">Điểm tối thiểu để đạt (%)</label>
            <input className="form-input" type="number" min={0} max={100} value={diemToiThieu}
              onChange={e => setDiemToiThieu(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Thời gian làm bài (phút, để trống nếu không giới hạn)</label>
            <input className="form-input" type="number" min={1} value={thoiGian}
              onChange={e => setThoiGian(e.target.value)} placeholder="Không giới hạn" />
          </div>
          <div className="alert alert-info" style={{ fontSize: 12.5 }}>
            <i className="ti ti-info-circle" aria-hidden="true"></i>
            {' '}Sau khi tạo, bạn sẽ được đưa tới màn hình thêm câu hỏi cho bài kiểm tra này.
          </div>
        </>
      )}
      {['VIDEO', 'PDF', 'DOC', 'ZIP'].includes(loai) && (
        <div className="form-group">
          <label className="form-label">Tệp</label>
          <div className="upload-zone" onClick={() => document.getElementById('tep-bo-sung').click()}>
            <i className="ti ti-upload" style={{ fontSize: 20 }} aria-hidden="true"></i>
            <div style={{ marginTop: 6, fontSize: 12 }}>{file ? file.name : 'Bấm để chọn tệp'}</div>
          </div>
          <input id="tep-bo-sung" type="file" style={{ display: 'none' }} onChange={e => setFile(e.target.files?.[0] || null)} />
        </div>
      )}
      {dangTai && ['VIDEO', 'PDF', 'DOC', 'ZIP'].includes(loai) && (
        <ThanhTienDoTaiLen phanTram={tienDoTai} nhan={tienDoTai == null ? 'Đang xử lý trên máy chủ...' : 'Đang tải lên...'} />
      )}
      <div className="btn-row">
        <button type="submit" className="btn btn-primary btn-sm" disabled={dangTai}>{dangTai ? 'Đang tải...' : 'Thêm bài học'}</button>
        <button type="button" className="btn btn-secondary btn-sm" onClick={onHuy}>Hủy</button>
      </div>
    </form>
  );
}

/* ══════════════════════ Quản lý câu hỏi — Quiz bổ sung ══════════════════════ */

function CauHoiRong(viTri) {
  return {
    noiDung: '',
    loaiCauHoi: 'MOT_DAP_AN',
    viTri,
    dapAn: [
      { id: 1, noiDung: '', dapAnDung: true },
      { id: 2, noiDung: '', dapAnDung: false },
    ],
  };
}

function QuanLyCauHoiBoSung({ muc, onClose }) {
  const [dsCauHoi, setDsCauHoi] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [hienFormMoi, setHienFormMoi] = useState(false);
  const [cauMoi, setCauMoi] = useState(() => CauHoiRong(1));
  const [dangGui, setDangGui] = useState(false);

  function taiLai() {
    setDangTai(true);
    layCauHoiBoSung(muc.maThamChieu)
      .then(setDsCauHoi)
      .catch(e => setLoi(layThongBaoLoi(e)))
      .finally(() => setDangTai(false));
  }
  useEffect(taiLai, [muc.maThamChieu]);

  function moFormThemCau() {
    setCauMoi(CauHoiRong(dsCauHoi.length + 1));
    setHienFormMoi(true);
  }

  function capNhatDapAn(idx, patch) {
    setCauMoi(prev => ({
      ...prev,
      dapAn: prev.dapAn.map((d, i) => (i === idx ? { ...d, ...patch } : d)),
    }));
  }

  function chonDapAnDung(idx) {
    setCauMoi(prev => ({
      ...prev,
      dapAn: prev.dapAn.map((d, i) => ({
        ...d,
        dapAnDung: prev.loaiCauHoi === 'NHIEU_DAP_AN' ? (i === idx ? !d.dapAnDung : d.dapAnDung) : i === idx,
      })),
    }));
  }

  function themDapAn() {
    setCauMoi(prev => ({
      ...prev,
      dapAn: [...prev.dapAn, { id: prev.dapAn.length + 1, noiDung: '', dapAnDung: false }],
    }));
  }

  function xoaDapAn(idx) {
    setCauMoi(prev => {
      if (prev.dapAn.length <= 2) return prev; // tối thiểu 2 phương án
      return { ...prev, dapAn: prev.dapAn.filter((_, i) => i !== idx) };
    });
  }

  async function xuLyGuiCauHoi(e) {
    e.preventDefault();
    setLoi('');
    if (!cauMoi.noiDung.trim()) { setLoi('Vui lòng nhập nội dung câu hỏi'); return; }
    if (cauMoi.dapAn.some(d => !d.noiDung.trim())) { setLoi('Vui lòng nhập đầy đủ nội dung các phương án'); return; }
    if (!cauMoi.dapAn.some(d => d.dapAnDung)) { setLoi('Cần chọn ít nhất 1 đáp án đúng'); return; }

    setDangGui(true);
    try {
      await themCauHoiBoSung(muc.maThamChieu, {
        noiDung: cauMoi.noiDung.trim(),
        viTri: cauMoi.viTri,
        loaiCauHoi: cauMoi.loaiCauHoi,
        dapAn: cauMoi.dapAn,
      });
      setHienFormMoi(false);
      taiLai();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  }

  async function xuLyXoaCauHoi(maCauHoiBaiBoSung) {
    if (!window.confirm('Xóa câu hỏi này?')) return;
    try {
      await xoaCauHoiBoSung(maCauHoiBaiBoSung);
      taiLai();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-section">
            <i className="ti ti-help-circle" aria-hidden="true"></i>
            <span>Câu hỏi — {muc.tieuDe}</span>
          </div>
          <button className="btn-close" onClick={onClose} title="Đóng (Esc)">
            <i className="ti ti-x" aria-hidden="true"></i>
          </button>
        </div>

        <div className="modal-body" style={{ display: 'grid', gap: 12 }}>
          {loi && <div className="alert alert-danger">{loi}</div>}
          {dangTai && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải câu hỏi...</p>}

          {!dangTai && dsCauHoi.length === 0 && !hienFormMoi && (
            <div className="empty-state">
              <i className="ti ti-help-circle" style={{ fontSize: 26 }} aria-hidden="true"></i>
              <p style={{ marginTop: 6, fontSize: 13 }}>Bài kiểm tra này chưa có câu hỏi nào.</p>
            </div>
          )}

          {dsCauHoi.map((c, idx) => (
            <div key={c.maCauHoiBaiBoSung} style={{ border: '1px solid var(--border)', borderRadius: 10, padding: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                <div style={{ fontWeight: 500, fontSize: 13.5 }}>{idx + 1}. {c.noiDung}</div>
                <button className="btn btn-danger btn-sm" onClick={() => xuLyXoaCauHoi(c.maCauHoiBaiBoSung)}>
                  <i className="ti ti-trash" aria-hidden="true"></i>
                </button>
              </div>
              <div style={{ marginTop: 8, display: 'grid', gap: 4 }}>
                {c.dapAn.map(d => (
                  <div key={d.id} style={{
                    fontSize: 13, padding: '4px 8px', borderRadius: 4,
                    background: d.dapAnDung ? '#dcfce7' : 'transparent',
                    color: d.dapAnDung ? '#15803d' : 'var(--text)',
                  }}>
                    {d.dapAnDung && <i className="ti ti-check" aria-hidden="true"></i>} {d.noiDung}
                  </div>
                ))}
              </div>
            </div>
          ))}

          {hienFormMoi ? (
            <form onSubmit={xuLyGuiCauHoi} style={{ border: '1px solid var(--border)', borderRadius: 10, padding: 12, background: 'var(--surface)' }}>
              <div className="form-group">
                <label className="form-label">Nội dung câu hỏi</label>
                <input className="form-input" value={cauMoi.noiDung}
                  onChange={e => setCauMoi(prev => ({ ...prev, noiDung: e.target.value }))} required />
              </div>
              <div className="form-group">
                <label className="form-label">Loại câu hỏi</label>
                <select className="form-input" value={cauMoi.loaiCauHoi}
                  onChange={e => setCauMoi(prev => ({ ...prev, loaiCauHoi: e.target.value }))}>
                  <option value="MOT_DAP_AN">Một đáp án đúng</option>
                  <option value="NHIEU_DAP_AN">Nhiều đáp án đúng</option>
                  <option value="DUNG_SAI">Đúng / Sai</option>
                </select>
              </div>

              <label className="form-label">Phương án trả lời</label>
              {cauMoi.dapAn.map((d, idx) => (
                <div key={d.id} style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 6 }}>
                  <input
                    type={cauMoi.loaiCauHoi === 'NHIEU_DAP_AN' ? 'checkbox' : 'radio'}
                    checked={d.dapAnDung}
                    onChange={() => chonDapAnDung(idx)}
                  />
                  <input className="form-input" style={{ flex: 1 }} value={d.noiDung}
                    onChange={e => capNhatDapAn(idx, { noiDung: e.target.value })}
                    placeholder={`Phương án ${idx + 1}`} />
                  {cauMoi.dapAn.length > 2 && (
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => xoaDapAn(idx)}>
                      <i className="ti ti-x" aria-hidden="true"></i>
                    </button>
                  )}
                </div>
              ))}
              {cauMoi.loaiCauHoi !== 'DUNG_SAI' && (
                <button type="button" className="btn btn-secondary btn-sm" onClick={themDapAn} style={{ marginBottom: 10 }}>
                  <i className="ti ti-plus" aria-hidden="true"></i> Thêm phương án
                </button>
              )}

              <div className="btn-row">
                <button type="submit" className="btn btn-primary btn-sm" disabled={dangGui}>
                  {dangGui ? 'Đang lưu...' : 'Lưu câu hỏi'}
                </button>
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => setHienFormMoi(false)}>Hủy</button>
              </div>
            </form>
          ) : (
            <button className="btn btn-primary btn-sm" onClick={moFormThemCau}>
              <i className="ti ti-plus" aria-hidden="true"></i> Thêm câu hỏi
            </button>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>
            <i className="ti ti-x" aria-hidden="true"></i> Đóng
          </button>
        </div>
      </div>
    </div>
  );
}