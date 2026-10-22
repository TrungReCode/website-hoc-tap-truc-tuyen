import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { layDeBaiGoc, layDeBoSung, nopBaiGoc, nopBaiBoSung } from '../../services/quizLopService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import CauHoiLamBai from '../../components/quiz/CauHoiLamBai';
import { daTraLoi, taoYeuCauTraLoi } from '../../utils/cauHoi';

export default function LamBaiKiemTra() {
  const { maLopHoc, loaiNguon, maThamChieu } = useParams();
  const navigate = useNavigate();
  const [deBai, setDeBai] = useState(null);
  // Bài làm theo từng câu: { [maCauHoi]: { ids, vanBan, capGhep } } — xem utils/cauHoi.js
  const [traLoiTheoCau, setTraLoiTheoCau] = useState({});
  const [giayConLai, setGiayConLai] = useState(null);
  const [dangNop, setDangNop] = useState(false);
  const [loi, setLoi] = useState('');
  const intervalRef = useRef(null);
  const daNopeRef = useRef(false);

  useEffect(() => {
    const fn = loaiNguon === 'goc' ? layDeBaiGoc(maLopHoc, maThamChieu) : layDeBoSung(maLopHoc, maThamChieu);
    fn.then(de => {
      setDeBai(de);
      // FIX: dùng giayConLai do backend tính sẵn (mốc thời gian lưu ở
      // Redis) thay vì tự suy ra thoiGian*60 — trước đây thoát ra vào lại
      // trang làm bài là đồng hồ đếm ngược reset về full thời gian.
      if (de.giayConLai !== null && de.giayConLai !== undefined) setGiayConLai(de.giayConLai);
    }).catch(err => setLoi(layThongBaoLoi(err)));
  }, [maLopHoc, maThamChieu, loaiNguon]);

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
  }, [giayConLai !== null]);

  function capNhatTraLoi(maCauHoi, traLoiMoi) {
    setTraLoiTheoCau(prev => ({ ...prev, [maCauHoi]: traLoiMoi }));
  }

  function cuonDenCauHoi(maCauHoi) {
    document.getElementById(`cau-hoi-${maCauHoi}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  async function xuLyNopBai(tuDong = false) {
    if (daNopeRef.current) return;

    const soCauChuaTraLoi = (deBai?.danhSachCauHoi || []).filter(c => !daTraLoi(c, traLoiTheoCau[c.maCauHoi])).length;
    if (!tuDong && soCauChuaTraLoi > 0) {
      const dongY = confirm(`Bạn còn ${soCauChuaTraLoi} câu chưa trả lời. Vẫn nộp bài?`);
      if (!dongY) return;
    }

    daNopeRef.current = true;
    clearInterval(intervalRef.current);
    setDangNop(true);
    const cauTraLoi = (deBai?.danhSachCauHoi || []).map(c => taoYeuCauTraLoi(c, traLoiTheoCau[c.maCauHoi]));
    try {
      const fn = loaiNguon === 'goc'
        ? nopBaiGoc(maLopHoc, maThamChieu, cauTraLoi)
        : nopBaiBoSung(maLopHoc, maThamChieu, cauTraLoi);
      const ketQua = await fn;
      navigate(`/lop-hoc/${maLopHoc}/quiz/${loaiNguon}/${maThamChieu}/ket-qua`, { state: { ketQua } });
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
  const soCauDaTraLoi = deBai?.danhSachCauHoi?.filter(c => daTraLoi(c, traLoiTheoCau[c.maCauHoi])).length || 0;
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
          {/* Thanh tiến độ trả lời */}
          <div className="qz-progress-wrap">
            <div className="qz-progress-bar">
              <div className="qz-progress-fill" style={{ width: `${phanTramTraLoi}%` }} />
            </div>
            <span className="qz-progress-text">{soCauDaTraLoi}/{tongSoCau} câu đã trả lời</span>
          </div>

          {/* Điều hướng nhanh tới từng câu */}
          <div className="qz-nav">
            {deBai.danhSachCauHoi.map((c, idx) => (
              <div
                key={c.maCauHoi}
                className={`qz-nav-chip ${daTraLoi(c, traLoiTheoCau[c.maCauHoi]) ? 'answered' : ''}`}
                role="button"
                tabIndex={0}
                aria-label={`Đi tới câu ${idx + 1}`}
                onClick={() => cuonDenCauHoi(c.maCauHoi)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    cuonDenCauHoi(c.maCauHoi);
                  }
                }}
                title={`Câu ${idx + 1}${daTraLoi(c, traLoiTheoCau[c.maCauHoi]) ? ' — đã trả lời' : ' — chưa trả lời'}`}
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

            {deBai.danhSachCauHoi.map((c, idx) => (
              <CauHoiLamBai
                key={c.maCauHoi}
                cauHoi={c}
                soThuTu={idx + 1}
                traLoi={traLoiTheoCau[c.maCauHoi]}
                onChange={(traLoiMoi) => capNhatTraLoi(c.maCauHoi, traLoiMoi)}
              />
            ))}

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
