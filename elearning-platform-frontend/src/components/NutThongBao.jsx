import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { demChuaDoc } from '../services/traoDoiService';

const CHU_KY_LAM_MOI_MS = 45_000; // 45 giây — đủ nhanh để cảm thấy "gần real-time" mà không dội API

/**
 * Nút chuông thông báo dùng chung (trang chủ học viên + DashboardShell của
 * giảng viên/QTV): hiện số thông báo chưa đọc và tự làm mới định kỳ, thay vì
 * chỉ tải số này bên trong chính trang /thong-bao (lúc đó người dùng đã ở
 * trong trang rồi nên số đếm không còn tác dụng báo hiệu).
 */
export default function NutThongBao({ className = 'btn btn-secondary btn-sm' }) {
  const navigate = useNavigate();
  const [soChuaDoc, setSoChuaDoc] = useState(0);

  useEffect(() => {
    let daHuy = false;
    const taiSo = () => demChuaDoc().then(so => { if (!daHuy) setSoChuaDoc(so || 0); });

    taiSo();
    const timer = setInterval(taiSo, CHU_KY_LAM_MOI_MS);
    return () => { daHuy = true; clearInterval(timer); };
  }, []);

  return (
    <button type="button" className={className} onClick={() => navigate('/thong-bao')}>
      <i className="ti ti-bell" aria-hidden="true"></i> Thông báo
      {soChuaDoc > 0 && (
        <span className="notif-badge" style={{ marginLeft: 6 }}>
          {soChuaDoc > 99 ? '99+' : soChuaDoc}
        </span>
      )}
    </button>
  );
}
