import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { capNhatTienDoBaiGoc, capNhatTienDoBaiBoSung } from '../../services/giaoTrinhLaiService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';

/** Trang xem học liệu (video/PDF/DOC/LINK) — cả bài gốc lẫn bài bổ sung. */
export default function XemBaiGiang() {
  const { maLopHoc, loaiNguon, maThamChieu } = useParams();
  const navigate = useNavigate();
  const [hoanThanhDaGui, setHoanThanhDaGui] = useState(false);
  const videoRef = useRef(null);

  // Tên và URL được truyền qua location.state từ trang GiaoDienLopHoc để tránh gọi thêm API
  const { tieuDe = 'Bài giảng', loai = 'VIDEO', duongDan = '' } = window.history.state?.usr || {};

  function danhDauHoanThanh() {
    if (hoanThanhDaGui) return;
    setHoanThanhDaGui(true);
    const yc = { hoanThanh: true };
    if (loaiNguon === 'goc') {
      capNhatTienDoBaiGoc(maLopHoc, maThamChieu, yc).catch(() => {});
    } else {
      capNhatTienDoBaiBoSung(maThamChieu, yc).catch(() => {});
    }
  }

  function xuLyTimeUpdate() {
    const v = videoRef.current;
    if (!v || hoanThanhDaGui) return;
    if (v.duration > 0 && v.currentTime / v.duration >= 0.8) {
      danhDauHoanThanh();
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <button className="btn btn-secondary btn-sm" onClick={() => navigate(-1)}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i>
        </button>
        <span className="t-title">{tieuDe}</span>
        {hoanThanhDaGui && <span className="t-badge green"><i className="ti ti-check" aria-hidden="true"></i> Đã hoàn thành</span>}
      </div>

      <div className="sc-body">
        {loai === 'VIDEO' && duongDan && (
          <video
            ref={videoRef}
            src={duongDan}
            controls
            onTimeUpdate={xuLyTimeUpdate}
            onEnded={danhDauHoanThanh}
            style={{ width: '100%', maxHeight: 480, background: '#000', borderRadius: 8 }}
          />
        )}
        {loai === 'PDF' && duongDan && (
          <iframe
            src={duongDan}
            title={tieuDe}
            style={{ width: '100%', height: 600, border: 'none', borderRadius: 8 }}
            onLoad={danhDauHoanThanh}
          />
        )}
        {loai === 'LINK' && duongDan && (
          <div className="card" style={{ textAlign: 'center', padding: 40 }}>
            <i className="ti ti-external-link" style={{ fontSize: 36, color: 'var(--primary)' }} aria-hidden="true"></i>
            <p style={{ margin: '12px 0', fontSize: 14 }}>Tài nguyên bên ngoài: <strong>{tieuDe}</strong></p>
            <a href={duongDan} target="_blank" rel="noopener noreferrer" className="btn btn-primary" onClick={danhDauHoanThanh}>
              Mở liên kết
            </a>
          </div>
        )}
        {(loai === 'DOC' || loai === 'ZIP') && duongDan && (
          <div className="card" style={{ textAlign: 'center', padding: 40 }}>
            <i className="ti ti-download" style={{ fontSize: 36, color: 'var(--primary)' }} aria-hidden="true"></i>
            <p style={{ margin: '12px 0', fontSize: 14 }}>Tải tệp đính kèm: <strong>{tieuDe}</strong></p>
            <a href={duongDan} target="_blank" rel="noopener noreferrer" className="btn btn-primary" onClick={danhDauHoanThanh}>
              Tải xuống
            </a>
          </div>
        )}
        {!duongDan && (
          <div className="empty-state">Nội dung chưa sẵn sàng.</div>
        )}
      </div>
    </div>
  );
}
