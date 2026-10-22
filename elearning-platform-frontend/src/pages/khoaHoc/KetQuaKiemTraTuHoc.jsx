import { useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import CauHoiKetQua from '../../components/quiz/CauHoiKetQua';

const RING_R = 44;
const RING_C = 2 * Math.PI * RING_R;

function VongTronDiem({ diem }) {
  const phanTram = Math.max(0, Math.min(100, diem));
  const lech = RING_C - (phanTram / 100) * RING_C;
  return (
    <div style={{ position: 'relative', flexShrink: 0 }}>
      <svg width="108" height="108" viewBox="0 0 108 108">
        <circle cx="54" cy="54" r={RING_R} fill="none" stroke="rgba(255,255,255,.28)" strokeWidth="9" />
        <circle
          cx="54" cy="54" r={RING_R} fill="none" stroke="#ffffff" strokeWidth="9" strokeLinecap="round"
          strokeDasharray={RING_C} strokeDashoffset={lech} transform="rotate(-90 54 54)"
        />
      </svg>
      <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', textAlign: 'center' }}>
        <div style={{ fontSize: 26, fontWeight: 700, lineHeight: 1 }}>{diem}%</div>
      </div>
    </div>
  );
}

// Bản sao gần như y hệt KetQuaKiemTra.jsx (lopHoc), chỉ khác 2 nút điều
// hướng cuối trang trỏ về khóa học tự học thay vì lớp học.
export default function KetQuaKiemTraTuHoc() {
  const { maKhoaHoc, maBaiHoc } = useParams();
  const navigate = useNavigate();
  const { state } = useLocation();
  const ketQua = state?.ketQua;
  const [chiXemCauSai, setChiXemCauSai] = useState(false);

  const danhSachHienThi = useMemo(() => {
    if (!ketQua) return [];
    return chiXemCauSai ? ketQua.chiTietCauHoi.filter(c => !c.dungCauNay) : ketQua.chiTietCauHoi;
  }, [ketQua, chiXemCauSai]);

  if (!ketQua) {
    return (
      <div className="empty-state">
        <p>Không tìm thấy kết quả bài làm.</p>
        <button className="btn btn-secondary btn-sm" style={{ marginTop: 12 }} onClick={() => navigate(`/khoa-hoc-cua-toi/${maKhoaHoc}/hoc`)}>
          Quay lại khóa học
        </button>
      </div>
    );
  }

  const soCauDung = ketQua.chiTietCauHoi.filter(c => c.dungCauNay).length;
  const soCauSai = ketQua.chiTietCauHoi.length - soCauDung;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-trophy" aria-hidden="true"></i> Kết quả bài kiểm tra</span>
      </div>

      <div className="sc-body" style={{ maxWidth: 720, width: '100%', margin: '0 auto' }}>
        <div className={`kq-hero ${ketQua.datYeuCau ? 'pass' : 'fail'}`}>
          <VongTronDiem diem={ketQua.diem} />
          <div>
            <div className="kq-hero-sub">{deBaiTitle(ketQua)}</div>
            <div className="kq-hero-pill">
              <i className={`ti ${ketQua.datYeuCau ? 'ti-circle-check' : 'ti-circle-x'}`} aria-hidden="true"></i>
              {ketQua.datYeuCau ? 'Đạt yêu cầu' : 'Chưa đạt yêu cầu'}
            </div>
            <div className="kq-hero-sub" style={{ marginTop: 8 }}>Điểm tối thiểu để đạt: {ketQua.diemToiThieu}%</div>
          </div>
        </div>

        <div className="kq-stats">
          <div className="kq-stat">
            <div className="kq-stat-num">{ketQua.chiTietCauHoi.length}</div>
            <div className="kq-stat-label">Tổng số câu</div>
          </div>
          <div className="kq-stat correct">
            <div className="kq-stat-num">{soCauDung}</div>
            <div className="kq-stat-label">Câu đúng</div>
          </div>
          <div className="kq-stat wrong">
            <div className="kq-stat-num">{soCauSai}</div>
            <div className="kq-stat-label">Câu sai</div>
          </div>
        </div>

        <div className="card sm">
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
            <div className="card-title" style={{ flex: 1 }}>Chi tiết từng câu</div>
            {soCauSai > 0 && (
              <span
                className={`dd-filter ${chiXemCauSai ? 'active' : ''}`}
                onClick={() => setChiXemCauSai(v => !v)}
              >
                <i className="ti ti-filter" aria-hidden="true"></i> Chỉ xem câu sai
              </span>
            )}
          </div>
          {danhSachHienThi.map((c) => {
            const idx = ketQua.chiTietCauHoi.indexOf(c);
            return (
              <CauHoiKetQua key={c.maCauHoi} c={c} soThuTu={idx + 1} />
            );
          })}
          {danhSachHienThi.length === 0 && (
            <p style={{ fontSize: 13, color: 'var(--text-3)', marginTop: 10 }}>Không có câu nào trong bộ lọc này.</p>
          )}
        </div>

        <div className="action-bar">
          <button className="btn btn-secondary" onClick={() => navigate(`/khoa-hoc-cua-toi/${maKhoaHoc}/hoc`)}>
            Quay lại khóa học
          </button>
          <button className="btn btn-primary"
            onClick={() => navigate(`/khoa-hoc-cua-toi/${maKhoaHoc}/quiz/${maBaiHoc}`)}>
            Làm lại
          </button>
        </div>
      </div>
    </div>
  );
}

function deBaiTitle(ketQua) {
  const dung = ketQua.chiTietCauHoi.filter(c => c.dungCauNay).length;
  return `Bạn trả lời đúng ${dung}/${ketQua.chiTietCauHoi.length} câu hỏi`;
}
