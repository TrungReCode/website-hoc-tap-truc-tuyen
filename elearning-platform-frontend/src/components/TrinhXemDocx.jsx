import { useEffect, useRef, useState } from 'react';
import { renderAsync } from 'docx-preview';

// Cùng ngưỡng debounce với video/PDF — tránh gọi API lưu vị trí mỗi lần
// cuộn (sự kiện scroll bắn rất dày).
const KHOANG_CACH_LUU_VI_TRI_MS = 2000;

/**
 * Xem file .docx bằng docx-preview — render trực tiếp thành DOM CÙNG-ORIGIN
 * (khác hẳn Google Docs Viewer nhúng qua <iframe>, vốn là nội dung khác
 * gốc nên trình duyệt chặn hoàn toàn việc đọc/set vị trí cuộn từ React).
 * Nhờ vậy "vị trí dừng" ở đây theo dõi được thật — tính theo % đã cuộn
 * (0-100), lưu vào đúng cột tien_do_hoc.vi_tri_dung như video/PDF.
 *
 * Giới hạn đã biết: chỉ áp dụng cho .docx (Word mở bằng OOXML). File .doc
 * nhị phân cũ, .xls/.xlsx, .ppt/.pptx vẫn phải dùng Google Viewer như cũ,
 * không lưu được vị trí — không có thư viện tương đương nhẹ và đáng tin
 * cậy để làm tương tự cho các định dạng đó trong phạm vi hiện tại.
 */
export default function TrinhXemDocx({ duongDan, viTriDung, onCapNhatViTri }) {
  const vungCuonRef = useRef(null);
  const daKhoiPhucViTri = useRef(false);
  const thoiDiemLuuGanNhat = useRef(0);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    let daHuy = false;
    setDangTai(true);
    setLoi('');
    daKhoiPhucViTri.current = false;

    fetch(duongDan)
      .then(r => {
        if (!r.ok) throw new Error('Không tải được file');
        return r.blob();
      })
      .then(blob => {
        if (daHuy || !vungCuonRef.current) return;
        vungCuonRef.current.innerHTML = '';
        return renderAsync(blob, vungCuonRef.current, undefined, {
          inWrapper: true,
          breakPages: true,
          ignoreLastRenderedPageBreak: false,
        });
      })
      .then(() => {
        if (daHuy || !vungCuonRef.current) return;
        // Khôi phục vị trí cuộn (%) — chỉ 1 lần lúc vừa render xong.
        if (viTriDung && !daKhoiPhucViTri.current) {
          daKhoiPhucViTri.current = true;
          const el = vungCuonRef.current;
          const cuonToiDa = el.scrollHeight - el.clientHeight;
          if (cuonToiDa > 0) {
            el.scrollTop = (Number(viTriDung) / 100) * cuonToiDa;
          }
        }
      })
      .catch(err => {
        if (!daHuy) setLoi(err.message || 'Không thể hiển thị file .docx này.');
      })
      .finally(() => { if (!daHuy) setDangTai(false); });

    return () => { daHuy = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [duongDan]);

  function xuLyCuon() {
    if (!onCapNhatViTri || !vungCuonRef.current) return;
    const bayGio = Date.now();
    if (bayGio - thoiDiemLuuGanNhat.current < KHOANG_CACH_LUU_VI_TRI_MS) return;
    thoiDiemLuuGanNhat.current = bayGio;

    const el = vungCuonRef.current;
    const cuonToiDa = el.scrollHeight - el.clientHeight;
    if (cuonToiDa <= 0) return; // nội dung ngắn hơn khung nhìn, không có gì để cuộn
    const phanTram = Math.round((el.scrollTop / cuonToiDa) * 100);
    onCapNhatViTri(Math.min(100, Math.max(0, phanTram)));
  }

  if (loi) {
    return (
      <div className="alert alert-danger" style={{ margin: 16 }}>
        {loi} — <a href={duongDan} target="_blank" rel="noreferrer">Mở file trực tiếp</a>
      </div>
    );
  }

  return (
    <div style={{ position: 'relative', height: '100%' }}>
      {dangTai && (
        <p style={{ textAlign: 'center', fontSize: 13, color: 'var(--text-3)', padding: 16 }}>
          Đang tải tài liệu...
        </p>
      )}
      <div
        ref={vungCuonRef}
        onScroll={xuLyCuon}
        style={{
          height: '100%', overflow: 'auto', background: '#525659', padding: '16px 0',
          display: dangTai ? 'none' : 'block',
        }}
      />
    </div>
  );
}
