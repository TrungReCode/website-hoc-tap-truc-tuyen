import { useEffect, useRef, useState } from 'react';
import * as pdfjsLib from 'pdfjs-dist';

// Worker tải qua CDN (khớp đúng version đã khai trong package.json) — tránh
// phải cấu hình Vite bundling riêng cho worker, cùng cách dự án đã dùng CDN
// cho Tabler Icons.
pdfjsLib.GlobalWorkerOptions.workerSrc =
  `https://cdn.jsdelivr.net/npm/pdfjs-dist@${pdfjsLib.version}/build/pdf.worker.min.mjs`;

/**
 * Xem PDF bằng canvas (PDF.js) thay vì <iframe src=...> — lý do bắt buộc
 * phải đổi: <iframe> không có API đọc/set số trang đang xem từ React, nên
 * không thể lưu/khôi phục "vị trí dừng" (tien_do_hoc.vi_tri_dung). Với
 * PDF.js, "vị trí dừng" = số trang hiện tại.
 *
 * @param duongDan     URL file PDF
 * @param trangBatDau  số trang mở sẵn khi vào lại (từ baiHoc.viTriDung), 1-based
 * @param onDoiTrang   gọi mỗi khi đổi trang — cha tự debounce trước khi lưu API
 */
export default function TrinhXemPdf({ duongDan, trangBatDau, onDoiTrang }) {
  const canvasRef = useRef(null);
  const pdfRef = useRef(null);
  const [trangHienTai, setTrangHienTai] = useState(Math.max(1, trangBatDau || 1));
  const [tongSoTrang, setTongSoTrang] = useState(0);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [zoom, setZoom] = useState(1.2);

  // Tải file PDF 1 lần khi đường dẫn đổi
  useEffect(() => {
    let daHuy = false;
    setDangTai(true);
    setLoi('');
    pdfjsLib.getDocument(duongDan).promise
      .then(pdf => {
        if (daHuy) return;
        pdfRef.current = pdf;
        setTongSoTrang(pdf.numPages);
        // Không cho vượt quá tổng số trang thật (vd. tài liệu đã bị thay bằng bản ngắn hơn)
        setTrangHienTai(prev => Math.min(Math.max(1, prev), pdf.numPages));
      })
      .catch(() => { if (!daHuy) setLoi('Không thể tải file PDF. Vui lòng thử lại.'); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [duongDan]);

  // Render lại canvas mỗi khi đổi trang hoặc zoom
  useEffect(() => {
    if (!pdfRef.current || !canvasRef.current) return;
    let daHuy = false;

    pdfRef.current.getPage(trangHienTai).then(page => {
      if (daHuy) return;
      const viewport = page.getViewport({ scale: zoom });
      const canvas = canvasRef.current;
      const ctx = canvas.getContext('2d');
      canvas.width = viewport.width;
      canvas.height = viewport.height;
      page.render({ canvasContext: ctx, viewport });
    });

    return () => { daHuy = true; };
  }, [trangHienTai, zoom]);

  function doiTrang(soTrangMoi) {
    const clamp = Math.min(Math.max(1, soTrangMoi), tongSoTrang || 1);
    setTrangHienTai(clamp);
    onDoiTrang?.(clamp);
  }

  if (loi) {
    return (
      <div className="alert alert-danger" style={{ margin: 16 }}>
        {loi} — <a href={duongDan} target="_blank" rel="noreferrer">Mở file trực tiếp</a>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', height: '100%' }}>
      <div
        style={{
          display: 'flex', alignItems: 'center', gap: 10, padding: '8px 12px',
          borderBottom: '1px solid var(--border)', width: '100%', justifyContent: 'center', flexWrap: 'wrap',
        }}
      >
        <button className="btn btn-secondary btn-sm" onClick={() => doiTrang(trangHienTai - 1)} disabled={trangHienTai <= 1}>
          <i className="ti ti-chevron-left" aria-hidden="true"></i>
        </button>
        <span style={{ fontSize: 13 }}>
          Trang{' '}
          <input
            type="number" min={1} max={tongSoTrang || 1} value={trangHienTai}
            onChange={e => doiTrang(Number(e.target.value) || 1)}
            style={{ width: 50, textAlign: 'center' }}
          />{' '}
          / {tongSoTrang || '…'}
        </span>
        <button className="btn btn-secondary btn-sm" onClick={() => doiTrang(trangHienTai + 1)} disabled={trangHienTai >= tongSoTrang}>
          <i className="ti ti-chevron-right" aria-hidden="true"></i>
        </button>
        <span style={{ width: 1, height: 20, background: 'var(--border)', margin: '0 4px' }} />
        <button className="btn btn-secondary btn-sm" onClick={() => setZoom(z => Math.max(0.6, z - 0.2))}>
          <i className="ti ti-zoom-out" aria-hidden="true"></i>
        </button>
        <button className="btn btn-secondary btn-sm" onClick={() => setZoom(z => Math.min(2.4, z + 0.2))}>
          <i className="ti ti-zoom-in" aria-hidden="true"></i>
        </button>
      </div>

      <div style={{ flex: 1, overflow: 'auto', width: '100%', display: 'flex', justifyContent: 'center', padding: 12 }}>
        {dangTai
          ? <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải PDF...</p>
          : <canvas ref={canvasRef} style={{ boxShadow: '0 1px 6px rgba(0,0,0,.15)' }} />}
      </div>
    </div>
  );
}
