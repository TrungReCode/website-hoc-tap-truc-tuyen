import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { layDeBaiTuHoc, nopBaiTuHoc } from '../../services/quizTuHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';

// Bản sao gần như y hệt LamBaiKiemTra.jsx (lopHoc) nhưng dùng API tự học
// (không cần maLopHoc) — xem giải thích lý do có API riêng ở
// QuizTuHocController.java.
export default function LamBaiKiemTraTuHoc() {
  const { maKhoaHoc, maBaiHoc } = useParams();
  const navigate = useNavigate();
  const [deBai, setDeBai] = useState(null);
  const [dapAnDaChon, setDapAnDaChon] = useState({});
  const [giayConLai, setGiayConLai] = useState(null);
  const [dangNop, setDangNop] = useState(false);
  const [loi, setLoi] = useState('');
  const intervalRef = useRef(null);
  const daNopeRef = useRef(false);

  useEffect(() => {
    layDeBaiTuHoc(maBaiHoc).then(de => {
      setDeBai(de);
      // FIX: dùng giayConLai do backend tính sẵn (dựa trên mốc thời gian lưu
      // ở Redis) thay vì tự suy ra thoiGian*60 — trước đây cứ mount lại
      // component (thoát ra vào lại) là đồng hồ reset về full thời gian.
      if (de.giayConLai !== null && de.giayConLai !== undefined) setGiayConLai(de.giayConLai);
    }).catch(err => setLoi(layThongBaoLoi(err)));
  }, [maBaiHoc]);

  useEffect(() => {
    if (giayConLai === null || giayConLai <= 0) return;
    intervalRef.current = setInterval(() => {
      setGiayConLai(g => {
        if (g <= 1) {
          clearInterval(intervalRef.current);
          if (!daNopeRef.current) xuLyNopBai(true);
          return 0;
        }
        return g - 1;
      });
    }, 1000);
    return () => clearInterval(intervalRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [giayConLai !== null]);

  function chonDapAn(maCauHoi, maDapAn, loaiCauHoi) {
    setDapAnDaChon(prev => {
      const hienTai = prev[maCauHoi] || [];
      if (loaiCauHoi === 'NHIEU_DAP_AN') {
        const moi = hienTai.includes(maDapAn) ? hienTai.filter(x => x !== maDapAn) : [...hienTai, maDapAn];
        return { ...prev, [maCauHoi]: moi };
      }
      return { ...prev, [maCauHoi]: [maDapAn] };
    });
  }

  function cuonDenCauHoi(maCauHoi) {
    document.getElementById(`cau-hoi-${maCauHoi}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  async function xuLyNopBai(tuDong = false) {
    if (daNopeRef.current) return;

    const soCauChuaTraLoi = (deBai?.danhSachCauHoi || []).filter(c => !(dapAnDaChon[c.maCauHoi]?.length)).length;
    if (!tuDong && soCauChuaTraLoi > 0) {
      const dongY = confirm(`Bạn còn ${soCauChuaTraLoi} câu chưa trả lời. Vẫn nộp bài?`);
      if (!dongY) return;
    }

    daNopeRef.current = true;
    clearInterval(intervalRef.current);
    setDangNop(true);
    const cauTraLoi = (deBai?.danhSachCauHoi || []).map(c => ({
      maCauHoi: c.maCauHoi,
      dsMaDapAnDaChon: dapAnDaChon[c.maCauHoi] || [],
    }));
    try {
      const ketQua = await nopBaiTuHoc(maBaiHoc, cauTraLoi);
      navigate(`/khoa-hoc-cua-toi/${maKhoaHoc}/quiz/${maBaiHoc}/ket-qua`, { state: { ketQua } });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
      daNopeRef.current = false;
    } finally {
      setDangNop(false);
    }
  }

  function dinhDangThoiGian(giay) {
    const p = Math.floor(giay / 60), g = giay % 60;
    return `${String(p).padStart(2, '0')}:${String(g).padStart(2, '0')}`;
  }

  if (!deBai && !loi) return <div className="empty-state">Đang tải đề bài...</div>;

  const tongSoCau = deBai?.danhSachCauHoi?.length || 0;
  const soCauDaTraLoi = deBai?.danhSachCauHoi?.filter(c => dapAnDaChon[c.maCauHoi]?.length).length || 0;
  const phanTramTraLoi = tongSoCau > 0 ? Math.round((soCauDaTraLoi / tongSoCau) * 100) : 0;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title"><i className="ti ti-help-circle" aria-hidden="true"></i> {deBai?.tieuDe || 'Bài kiểm tra'}</span>
        {giayConLai !== null && (
          <span className="countdown" style={{ background: giayConLai < 60 ? 'var(--danger)' : '#1e293b' }}>
            <i className="ti ti-clock" aria-hidden="true"></i> {dinhDangThoiGian(giayConLai)}
          </span>
        )}
      </div>

      {deBai && (
        <>
          <div className="qz-progress-wrap">
            <div className="qz-progress-bar">
              <div className="qz-progress-fill" style={{ width: `${phanTramTraLoi}%` }} />
            </div>
            <span className="qz-progress-text">{soCauDaTraLoi}/{tongSoCau} câu đã trả lời</span>
          </div>

          <div className="qz-nav">
            {deBai.danhSachCauHoi.map((c, idx) => (
              <div
                key={c.maCauHoi}
                className={`qz-nav-chip ${dapAnDaChon[c.maCauHoi]?.length ? 'answered' : ''}`}
                onClick={() => cuonDenCauHoi(c.maCauHoi)}
                title={`Câu ${idx + 1}${dapAnDaChon[c.maCauHoi]?.length ? ' — đã trả lời' : ' — chưa trả lời'}`}
              >
                {idx + 1}
              </div>
            ))}
          </div>
        </>
      )}

      <div className="sc-body">
        {loi && <div className="alert alert-danger">{loi}</div>}

        {deBai && (
          <>
            <div style={{ fontSize: 13, color: 'var(--text-2)', marginBottom: 16 }}>
              {tongSoCau} câu hỏi · Điểm đạt: {deBai.diemToiThieu}%
              {deBai.thoiGian && ` · Thời gian: ${deBai.thoiGian} phút`}
            </div>

            {deBai.danhSachCauHoi.map((c, idx) => {
              const daChon = dapAnDaChon[c.maCauHoi] || [];
              return (
                <div key={c.maCauHoi} id={`cau-hoi-${c.maCauHoi}`} className="card sm" style={{ marginBottom: 12, scrollMarginTop: 140 }}>
                  <div style={{ fontSize: 14, fontWeight: 500, marginBottom: 10, display: 'flex', gap: 8 }}>
                    <span>{idx + 1}. {c.noiDung}</span>
                    {!daChon.length && <span className="badge badge-amber" style={{ flexShrink: 0 }}>Chưa trả lời</span>}
                  </div>
                  {c.dapAn.map((d, di) => {
                    const thuTu = ['A', 'B', 'C', 'D', 'E'][di] || String(di + 1);
                    const daChonNay = daChon.includes(d.id);
                    return (
                      <div
                        key={d.id}
                        className={`quiz-option ${daChonNay ? 'selected' : ''}`}
                        onClick={() => chonDapAn(c.maCauHoi, d.id, c.loaiCauHoi)}
                      >
                        <span className="opt-letter">{thuTu}</span>
                        {d.noiDung}
                      </div>
                    );
                  })}
                </div>
              );
            })}

            <div className="action-bar">
              <button className="btn btn-secondary" onClick={() => navigate(-1)} disabled={dangNop}>
                Thoát
              </button>
              <button className="btn btn-primary" onClick={() => xuLyNopBai(false)} disabled={dangNop}>
                {dangNop ? 'Đang chấm...' : 'Nộp bài'}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
