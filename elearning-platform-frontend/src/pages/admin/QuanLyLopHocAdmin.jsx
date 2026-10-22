import { useEffect, useState, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { layDanhSachLopHocChoAdmin } from '../../services/lopHocService';
import { layGiaoTrinhChoAdmin, layDeBaiQuizChoAdmin } from '../../services/giaoTrinhLaiService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';
import XemTepTinModal from '../../components/XemTepTinModal';
import DapAnXemTruoc from '../../components/quiz/DapAnXemTruoc';
import '../../components/XemTepTinModal.css';

const TABS = [
  { gia_tri: '', nhan: 'Tất cả' },
  { gia_tri: 'SAP_DIEN_RA', nhan: 'Sắp diễn ra' },
  { gia_tri: 'HOAT_DONG', nhan: 'Đang hoạt động' },
  { gia_tri: 'DA_DONG', nhan: 'Đã đóng' },
];

const NHAN_TRANG_THAI = {
  SAP_DIEN_RA: { nhan: 'Sắp diễn ra', lop: 'badge-blue' },
  HOAT_DONG: { nhan: 'Đang hoạt động', lop: 'badge-green' },
  DA_DONG: { nhan: 'Đã đóng', lop: 'badge-gray' },
};

export default function QuanLyLopHocAdmin() {
  const navigate = useNavigate();
  // FIX: đọc tab trạng thái ban đầu từ query string (?trangThai=...) — cho
  // phép trang Tổng quan điều hướng thẳng tới đúng tab lọc sẵn.
  const [searchParams] = useSearchParams();
  const [trang, setTrang] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 });
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [tabDangChon, setTabDangChon] = useState(searchParams.get('trangThai') || '');
  const [oTimKiem, setOTimKiem] = useState('');
  const [maLop, setMaLop] = useState('');

  // Xem giáo trình lớp học (giám sát nội dung, kể cả bài học bổ sung)
  const [lopDangXem, setLopDangXem] = useState(null); // { maLopHoc, maLop }
  const [giaoTrinh, setGiaoTrinh] = useState(null);
  const [dangTaiGiaoTrinh, setDangTaiGiaoTrinh] = useState(false);
  const [loiGiaoTrinh, setLoiGiaoTrinh] = useState('');
  const [mucDangXem, setMucDangXem] = useState(null); // mục VIDEO/PDF/DOC đang preview qua XemTepTinModal
  const [quizDangXem, setQuizDangXem] = useState(null); // { loaiNguon, maThamChieu, tieuDe } — mục QUIZ đang xem chi tiết
  const [deQuiz, setDeQuiz] = useState(null);
  const [dangTaiQuiz, setDangTaiQuiz] = useState(false);
  const [loiQuiz, setLoiQuiz] = useState('');

  function moQuiz(m) {
    setQuizDangXem(m);
    setDeQuiz(null);
    setLoiQuiz('');
    setDangTaiQuiz(true);
    layDeBaiQuizChoAdmin(m.loaiNguon, m.maThamChieu)
      .then(setDeQuiz)
      .catch((err) => setLoiQuiz(layThongBaoLoi(err)))
      .finally(() => setDangTaiQuiz(false));
  }

  function moGiaoTrinh(lop) {
    setLopDangXem(lop);
    setGiaoTrinh(null);
    setLoiGiaoTrinh('');
    setDangTaiGiaoTrinh(true);
    layGiaoTrinhChoAdmin(lop.maLopHoc)
      .then(setGiaoTrinh)
      .catch((err) => setLoiGiaoTrinh(layThongBaoLoi(err)))
      .finally(() => setDangTaiGiaoTrinh(false));
  }

  const taiLai = useCallback((trangSo = 0, trangThai = tabDangChon, ml = maLop) => {
    setDangTai(true);
    layDanhSachLopHocChoAdmin({ page: trangSo, trangThai, maLop: ml })
      .then(setTrang)
      .catch((err) => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tabDangChon, maLop]);

  useEffect(() => taiLai(0), [taiLai]);

  function xuLyChonTab(giaTri) {
    setTabDangChon(giaTri);
    taiLai(0, giaTri, maLop);
  }

  function xuLyTimKiem(e) {
    e.preventDefault();
    setMaLop(oTimKiem.trim());
    taiLai(0, tabDangChon, oTimKiem.trim());
  }

  const { navItems, roleLabel } = layNavTheoVaiTro();

  return (
    <>
    <DashboardShell
      roleLabel={roleLabel}
      navItems={navItems}
      crumb="Lớp học"
      actions={<span className="badge badge-amber">{trang.totalElements} lớp học</span>}
    >
      <div className="ds-page-head">
        <div><h1>Quản lý lớp học</h1><p>Toàn bộ lớp học đang hoạt động trên nền tảng</p></div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12, marginBottom: 14 }}>
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            {TABS.map((t) => (
              <button
                key={t.gia_tri}
                className={`btn btn-sm ${tabDangChon === t.gia_tri ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => xuLyChonTab(t.gia_tri)}
              >
                {t.nhan}
              </button>
            ))}
          </div>

          <form onSubmit={xuLyTimKiem} style={{ display: 'flex', gap: 6 }}>
            <input
              className="form-input"
              style={{ width: 200 }}
              placeholder="Tìm theo mã lớp..."
              value={oTimKiem}
              onChange={(e) => setOTimKiem(e.target.value)}
            />
            <button type="submit" className="btn btn-secondary btn-sm">
              <i className="ti ti-search" aria-hidden="true"></i>
            </button>
          </form>
        </div>

        {dangTai ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
        ) : trang.content.length === 0 ? (
          <div className="empty-state">
            <i className="ti ti-chalkboard-off" style={{ fontSize: 32 }} aria-hidden="true"></i>
            <p style={{ marginTop: 8 }}>Không có lớp học nào khớp bộ lọc hiện tại.</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã lớp</th>
                <th>Khóa học</th>
                <th>Giảng viên</th>
                <th>Trạng thái</th>
                <th>Sĩ số</th>
                <th>Thành viên</th>
                <th>Đánh giá</th>
                <th>Bắt đầu</th>
                <th>Kết thúc</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {trang.content.map((l) => {
                const nhan = NHAN_TRANG_THAI[l.trangThai] || { nhan: l.trangThai, lop: 'badge-gray' };
                return (
                  <tr key={l.maLopHoc}>
                    <td>{l.maLop}</td>
                    <td>{l.tenKhoaHoc || '—'}</td>
                    <td>{l.tenGiangVien || '—'}</td>
                    <td><span className={`badge ${nhan.lop}`}>{nhan.nhan}</span></td>
                    <td>{l.siSo ?? '—'}</td>
                    <td>{l.soThanhVien}</td>
                    <td>{l.diemTrungBinh != null ? `${l.diemTrungBinh.toFixed(1)} ⭐ (${l.soLuongDanhGia})` : '—'}</td>
                    <td>{l.ngayBatDau ? new Date(l.ngayBatDau).toLocaleDateString('vi-VN') : '—'}</td>
                    <td>{l.ngayKetThuc ? new Date(l.ngayKetThuc).toLocaleDateString('vi-VN') : '—'}</td>
                    <td>
                      <button className="btn btn-secondary btn-sm" onClick={() => moGiaoTrinh(l)}>
                        <i className="ti ti-list-details" aria-hidden="true"></i> Giáo trình
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}

        {trang.totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginTop: 20 }}>
            <button className="btn btn-secondary btn-sm" disabled={trang.number === 0} onClick={() => taiLai(trang.number - 1)}>
              ‹
            </button>
            {Array.from({ length: trang.totalPages }, (_, i) => i).map((i) => (
              <button key={i} className={`btn btn-sm ${i === trang.number ? 'btn-primary' : 'btn-secondary'}`} onClick={() => taiLai(i)}>
                {i + 1}
              </button>
            ))}
            <button className="btn btn-secondary btn-sm" disabled={trang.number >= trang.totalPages - 1} onClick={() => taiLai(trang.number + 1)}>
              ›
            </button>
          </div>
        )}
    </DashboardShell>

    {lopDangXem && (
      <div className="modal-overlay" onClick={() => setLopDangXem(null)}>
        <div className="modal-content" style={{ maxWidth: 640 }} onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <div className="modal-title-section">
              <i className="ti ti-list-details" aria-hidden="true"></i>
              <span>Giáo trình lớp {lopDangXem.maLop}</span>
            </div>
            <button className="btn-close" onClick={() => setLopDangXem(null)} title="Đóng (Esc)">
              <i className="ti ti-x" aria-hidden="true"></i>
            </button>
          </div>
          <div className="modal-body">
            {dangTaiGiaoTrinh && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>}
            {loiGiaoTrinh && <div className="alert alert-danger">{loiGiaoTrinh}</div>}
            {giaoTrinh && giaoTrinh.length === 0 && (
              <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Lớp học này chưa có nội dung nào trong giáo trình.</p>
            )}
            {giaoTrinh && giaoTrinh.length > 0 && (
              <div style={{ display: 'grid', gap: 6 }}>
                {giaoTrinh.map((m) => (
                  <div
                    key={m.maMuc}
                    className="curr-item"
                    style={{
                      display: 'flex', alignItems: 'center', gap: 8, padding: '8px 10px',
                      border: '1px solid var(--border)', borderRadius: 6,
                      cursor: 'pointer',
                      opacity: m.anHien ? 1 : 0.55,
                    }}
                    onClick={() => m.loai === 'QUIZ' ? moQuiz(m) : setMucDangXem(m)}
                  >
                    <i className={`ti ${
                      m.loai === 'VIDEO' ? 'ti-player-play' : m.loai === 'QUIZ' ? 'ti-help-circle' : 'ti-file-text'
                    }`} aria-hidden="true"></i>
                    <span style={{ flex: 1, fontSize: 13 }}>
                      {m.tenChuongGoc && <span style={{ color: 'var(--text-3)' }}>{m.tenChuongGoc} · </span>}
                      {m.tieuDe}
                    </span>
                    <span className="badge badge-blue">{m.loai}</span>
                    {m.loaiNguon === 'BO_SUNG' && <span className="badge badge-amber">Bổ sung</span>}
                    {!m.anHien && <span className="badge badge-gray">Đang ẩn</span>}
                  </div>
                ))}
                <p style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 6 }}>
                  Nhấp vào mỗi mục để xem trước nội dung — bao gồm cả câu hỏi và đáp án đúng của Quiz.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    )}

    {mucDangXem && (
      <XemTepTinModal baiHoc={mucDangXem} onClose={() => setMucDangXem(null)} />
    )}

    {quizDangXem && (
      <div className="modal-overlay" onClick={() => setQuizDangXem(null)}>
        <div className="modal-content" style={{ maxWidth: 560 }} onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <div className="modal-title-section">
              <i className="ti ti-help-circle" aria-hidden="true"></i>
              <span>{quizDangXem.tieuDe}</span>
            </div>
            <button className="btn-close" onClick={() => setQuizDangXem(null)} title="Đóng (Esc)">
              <i className="ti ti-x" aria-hidden="true"></i>
            </button>
          </div>
          <div className="modal-body">
            {dangTaiQuiz && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>}
            {loiQuiz && <div className="alert alert-danger">{loiQuiz}</div>}
            {deQuiz && (
              <div className="preview-quiz-readonly" style={{ padding: 0, maxHeight: 460 }}>
                <p className="preview-quiz-note">
                  <i className="ti ti-eye" aria-hidden="true"></i> Xem để kiểm duyệt nội dung - đáp án đúng được đánh dấu.
                  {deQuiz.thoiGian && ` Thời gian làm bài: ${deQuiz.thoiGian} phút. `}
                  Điểm đạt: {deQuiz.diemToiThieu}%.
                </p>
                {deQuiz.danhSachCauHoi.length === 0 && (
                  <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Bài kiểm tra này chưa có câu hỏi nào.</p>
                )}
                {deQuiz.danhSachCauHoi.map((c, idx) => (
                  <div key={c.maCauHoi} className="preview-quiz-question">
                    <div className="preview-quiz-question-text">{idx + 1}. {c.noiDung}</div>
                    <DapAnXemTruoc cauHoi={c} />
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    )}
  </>
  );
}