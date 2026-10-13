import { useRef, useState } from 'react';
import './XemTepTinModal.css';
import TrinhXemPdf from './TrinhXemPdf';
import TrinhXemDocx from './TrinhXemDocx';

// Khoảng cách tối thiểu giữa 2 lần gọi API lưu vị trí xem video — timeupdate
// bắn rất dày (~4 lần/giây), không thể gọi API mỗi lần.
const KHOANG_CACH_LUU_VI_TRI_MS = 5000;

export default function XemTepTinModal({ baiHoc, onClose, onHoanThanh, onCapNhatViTri, hienNutDanhDauDaDocXong = false, xemThu = false }) {
    const [dangTai, setDangTai] = useState(false);
    // State quản lý việc đã bấm xác nhận/xem xong chưa (tránh gọi API nhiều lần)
    const [daXacNhan, setDaXacNhan] = useState(baiHoc?.hoanThanh || false);

    if (!baiHoc) return null;

    // Hàm gọi khi học viên xem hết video hoặc tự bấm nút xác nhận
    const xuLyHoanThanh = () => {
        if (!daXacNhan) {
            setDaXacNhan(true);
            if (onHoanThanh) onHoanThanh();
        }
    };

    const getTepTinInfo = () => {
        if (!baiHoc.duongDan && baiHoc.loai !== 'QUIZ') return null;

        const kichThuoc = baiHoc.metadata?.kichThuocByte || 0;
        const thoiLuongGiay = baiHoc.metadata?.thoiLuongGiay || 0;

        return {
            ten: baiHoc.tieuDe || 'File',
            duongDan: baiHoc.duongDan || '',
            loaiTap: baiHoc.duoiTep || baiHoc.loai || 'UNKNOWN',
            size: kichThuoc,
            thoiLuong: Math.round(thoiLuongGiay / 60), 
        };
    };

    const info = getTepTinInfo();

    const handleDownload = async () => {
        if (!info?.duongDan) return;
        setDangTai(true);
        try {
            const response = await fetch(info.duongDan);
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = info.ten;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (err) {
            console.error('Lỗi tải file:', err);
            alert('Không thể tải file. Vui lòng thử lại.');
        } finally {
            setDangTai(false);
        }
    };

    const renderPreview = () => {
        if (!info) {
            return <div className="preview-empty">Không có file để hiển thị</div>;
        }

        const loaiBaiHoc = baiHoc.loai?.toUpperCase() || '';

        // 1. Nếu là Video - Truyền hàm xuLyHoanThanh vào prop onEnded
        if (loaiBaiHoc === 'VIDEO') {
            return (
                <PreviewVideo
                    duongDan={info.duongDan}
                    onEnded={xuLyHoanThanh}
                    viTriDung={baiHoc.viTriDung}
                    onCapNhatViTri={onCapNhatViTri}
                />
            );
        } 
        
        // 2. Nếu là Quiz — hiển thị đầy đủ câu hỏi/đáp án cho GIẢNG VIÊN
        // (xem lại nội dung khóa học của mình) và QUẢN TRỊ VIÊN (kiểm duyệt) —
        // 2 nguồn dữ liệu này (layChiTietChoTacGia, moHoSoKiemDuyet) đều trả
        // dapAnDung đầy đủ. Nếu modal này được mở từ luồng công khai/học
        // viên (dapAnDung đã bị backend ẩn = null), phần đánh dấu đáp án
        // đúng tự động không hiện gì — KHÔNG được tự suy luận/đánh dấu đáp
        // án ở phía frontend để tránh lộ lại đáp án cho học viên.
        if (loaiBaiHoc === 'QUIZ') {
            const cauHois = baiHoc.baiKiemTra?.danhSachCauHoi || [];
            if (cauHois.length === 0) {
                return <div className="preview-info">Bài kiểm tra này chưa có câu hỏi nào.</div>;
            }
            return (
                <div className="preview-quiz-readonly">
                    <p className="preview-quiz-note">
                        <i className="ti ti-eye" aria-hidden="true"></i> Xem trước nội dung — đây không phải giao diện làm bài.
                    </p>
                    {cauHois.map((c, idx) => (
                        <div key={c.maCauHoi} className="preview-quiz-question">
                            <div className="preview-quiz-question-text">{idx + 1}. {c.noiDung}</div>
                            {c.dapAn?.map(d => (
                                <div
                                    key={d.id}
                                    className={`preview-quiz-option ${d.dapAnDung ? 'correct' : ''}`}
                                >
                                    {d.dapAnDung && <i className="ti ti-check" aria-hidden="true"></i>} {d.noiDung}
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
            );
        } 
        
        // 3. Nếu là các loại tài liệu (PDF, Word, Excel, PowerPoint...)
        if (['PDF', 'DOC', 'DOCX', 'XLS', 'XLSX', 'PPT', 'PPTX', 'DOCUMENT'].includes(loaiBaiHoc)) {
            return (
                <PreviewDocument
                    duongDan={info.duongDan}
                    loaiTap={info.loaiTap}
                    viTriDung={baiHoc.viTriDung}
                    onCapNhatViTri={onCapNhatViTri}
                />
            );
        } 
        
        // 4. Các trường hợp không xác định khác
        return <div className="preview-empty">Không hỗ trợ preview cho loại file này ({loaiBaiHoc})</div>;
    };

    const formatSize = (bytes) => {
        if (!bytes) return '—';
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <div className="modal-title-section">
                        <i className={`ti ${baiHoc.loai === 'VIDEO' ? 'ti-player-play' : baiHoc.loai === 'QUIZ' ? 'ti-help-circle' : 'ti-file-text'}`} aria-hidden="true"></i>
                        <span>{baiHoc.tieuDe}</span>
                    </div>
                    <button className="btn-close" onClick={onClose} title="Đóng (Esc)">
                        <i className="ti ti-x" aria-hidden="true"></i>
                    </button>
                </div>

                {info && (
                    <div className="file-info">
                        <div className="info-row">
                            <span className="info-label">Loại:</span>
                            <span className="badge badge-blue">{baiHoc.loai}</span>
                        </div>
                        {info.size > 0 && (
                            <div className="info-row">
                                <span className="info-label">Kích thước:</span>
                                <span>{formatSize(info.size)}</span>
                            </div>
                        )}
                        {baiHoc.loai === 'VIDEO' && info.thoiLuong > 0 && (
                            <div className="info-row">
                                <span className="info-label">Thời lượng:</span>
                                <span>{info.thoiLuong} phút</span>
                            </div>
                        )}
                    </div>
                )}

                {/* Thông báo nhắc nhở xem video — chỉ có ý nghĩa khi đang HỌC
                    thật (có ghi nhận tiến độ). Ở chế độ xem thử (giảng viên
                    xem trước, học viên xem thử miễn phí, admin kiểm duyệt...)
                    không có tiến độ nào để ghi nhận cả, hiện thanh này chỉ
                    gây hiểu lầm. */}
                {baiHoc.loai === 'VIDEO' && !daXacNhan && !xemThu && (
                    <div style={{ padding: '8px 16px', background: 'var(--amber-light, #fef3c7)', color: 'var(--amber-dark, #92400e)', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <i className="ti ti-info-circle" aria-hidden="true"></i> Xem hết video để ghi nhận tiến độ.
                    </div>
                )}

                <div className="modal-body">{renderPreview()}</div>

                <div className="modal-footer" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    
                    {/* Hiển thị nút bấm hoặc trạng thái hoàn thành đối với tài liệu (không phải Video/Quiz) */}
                    <div>
                        {/* Cùng lý do với thanh thông báo video ở trên: nút
                            "Đánh dấu đã đọc xong" cũng không có ý nghĩa gì ở
                            chế độ xem thử — thêm !xemThu để chắc chắn, không
                            phụ thuộc hoàn toàn vào việc nơi gọi có lỡ quên
                            truyền/truyền nhầm hienNutDanhDauDaDocXong hay không. */}
                        {hienNutDanhDauDaDocXong && !xemThu && baiHoc.loai !== 'VIDEO' && baiHoc.loai !== 'QUIZ' && (
                            !daXacNhan ? (
                                <button className="btn btn-primary btn-sm" onClick={xuLyHoanThanh}>
                                    <i className="ti ti-check" aria-hidden="true"></i> Đánh dấu đã đọc xong
                                </button>
                            ) : (
                                <span style={{ color: 'var(--success, #10b981)', fontWeight: 500, display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px' }}>
                                    <i className="ti ti-circle-check" aria-hidden="true"></i> Bạn đã hoàn thành
                                </span>
                            )
                        )}

                        {/* Báo trạng thái hoàn thành cho Video */}
                        {baiHoc.loai === 'VIDEO' && daXacNhan && (
                            <span style={{ color: 'var(--success, #10b981)', fontWeight: 500, display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px' }}>
                                <i className="ti ti-circle-check" aria-hidden="true"></i> Bạn đã hoàn thành
                            </span>
                        )}
                    </div>

                    <button className="btn btn-secondary" onClick={onClose}>
                        <i className="ti ti-x" aria-hidden="true"></i> Đóng
                    </button>
                </div>
            </div>
        </div>
    );
}

// Bổ sung prop onEnded + viTriDung/onCapNhatViTri vào PreviewVideo
function PreviewVideo({ duongDan, onEnded, viTriDung, onCapNhatViTri }) {
    const videoRef = useRef(null);
    const daTuaViTriBanDau = useRef(false);
    const thoiDiemLuuGanNhat = useRef(0);

    // Tua tới vị trí đã lưu — chỉ tua đúng 1 lần lúc metadata sẵn sàng,
    // tránh giật lại vị trí cũ nếu component re-render trong lúc đang xem.
    function xuLySanSang() {
        if (daTuaViTriBanDau.current) return;
        daTuaViTriBanDau.current = true;
        if (viTriDung && videoRef.current) {
            videoRef.current.currentTime = Number(viTriDung);
        }
    }

    function xuLyCapNhatThoiGian() {
        if (!onCapNhatViTri || !videoRef.current) return;
        const bayGio = Date.now();
        if (bayGio - thoiDiemLuuGanNhat.current < KHOANG_CACH_LUU_VI_TRI_MS) return;
        thoiDiemLuuGanNhat.current = bayGio;
        onCapNhatViTri(Math.floor(videoRef.current.currentTime));
    }

    function xuLyTamDung() {
        // Luôn lưu ngay khi tạm dừng (không chờ đủ 5s) — đây là lúc người
        // học nhiều khả năng sắp rời đi, nên độ chính xác quan trọng hơn.
        if (onCapNhatViTri && videoRef.current) {
            onCapNhatViTri(Math.floor(videoRef.current.currentTime));
        }
    }

    return (
        <div className="video-container" style={{ width: '100%', backgroundColor: '#000', borderRadius: '8px', overflow: 'hidden' }}>
            <video
                ref={videoRef}
                controls
                width="100%"
                controlsList="nodownload"
                style={{ display: 'block', maxHeight: '70vh' }}
                onLoadedMetadata={xuLySanSang}
                onTimeUpdate={xuLyCapNhatThoiGian}
                onPause={xuLyTamDung}
                onEnded={onEnded}
            >
                <source src={duongDan} type="video/mp4" />
                <source src={duongDan} type="video/webm" /> 
                Trình duyệt của bạn không hỗ trợ thẻ video.
            </video>
        </div>
    );
}

function PreviewDocument({ duongDan, loaiTap, viTriDung, onCapNhatViTri }) {
    if (!duongDan) {
        return <div className="preview-empty">Không có URL tài liệu</div>;
    }

    const loai = loaiTap?.toUpperCase() || '';
    const urlKhongQuery = duongDan.split('?')[0].toLowerCase();
    
    const kiemTraDuoi = (mangDuoi) => {
        return mangDuoi.some(duoi => urlKhongQuery.endsWith(duoi));
    };

    if (loai === 'PDF' || kiemTraDuoi(['.pdf'])) {
        // PDF.js thay cho <iframe src=...> — iframe không có API đọc/set số
        // trang từ React nên không lưu/khôi phục được vị trí dừng.
        return (
            <div className="document-container" style={{ height: 600 }}>
                <TrinhXemPdf duongDan={duongDan} trangBatDau={viTriDung} onDoiTrang={onCapNhatViTri} />
            </div>
        );
    }

    if (['JPG', 'JPEG', 'PNG', 'GIF', 'WEBP'].includes(loai) || kiemTraDuoi(['.jpg', '.jpeg', '.png', '.gif', '.webp'])) {
        return (
            <div className="image-container">
                <img src={duongDan} alt="Hình ảnh tài liệu" style={{ maxWidth: '100%', height: 'auto' }} />
            </div>
        );
    }

    if (loai === 'DOCX' || kiemTraDuoi(['.docx'])) {
        // docx-preview render trực tiếp cùng-origin nên đọc/set được vị trí
        // cuộn — khác Google Viewer bên dưới (iframe khác gốc, không có
        // cách nào đọc lại vị trí đang xem).
        return (
            <div className="document-container" style={{ height: 600 }}>
                <TrinhXemDocx duongDan={duongDan} viTriDung={viTriDung} onCapNhatViTri={onCapNhatViTri} />
            </div>
        );
    }

    if (['DOC', 'XLS', 'XLSX', 'PPT', 'PPTX'].includes(loai) || kiemTraDuoi(['.doc', '.xls', '.xlsx', '.ppt', '.pptx'])) {
        // Chưa có cách lưu vị trí dừng cho các định dạng này — Google Viewer
        // hiển thị qua iframe khác gốc, trình duyệt chặn hoàn toàn việc đọc
        // lại vị trí cuộn/trang từ phía React (khác .docx ở nhánh trên).
        const encodedUrl = encodeURIComponent(duongDan);
        return (
            <div className="document-container">
                <iframe
                    src={`https://docs.google.com/gview?url=${encodedUrl}&embedded=true`}
                    width="100%"
                    height="600"
                    frameBorder="0"
                    title="Google Document Preview"
                ></iframe>
                <p style={{ fontSize: 11, color: 'var(--text-3)', textAlign: 'center', margin: '6px 0 0' }}>
                    Định dạng này chưa hỗ trợ lưu vị trí xem dở — lần sau sẽ mở lại từ đầu.
                </p>
            </div>
        );
    }

    return (
        <div className="preview-empty">
            <p>Không thể preview loại file này ({loaiTap})</p>
            <p style={{ fontSize: 12, color: 'var(--text-2)' }}>
                <a href={duongDan} target="_blank" rel="noopener noreferrer" style={{ color: 'var(--primary)' }}>
                    Mở trong tab mới
                </a>
            </p>
        </div>
    );
}