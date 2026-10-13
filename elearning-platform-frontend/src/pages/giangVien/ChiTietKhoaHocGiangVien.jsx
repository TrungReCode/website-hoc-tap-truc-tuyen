import { useEffect, useState, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
// Thay đổi API thành layChiTietChoTacGia để lấy full dữ liệu không bị che URL
import { layChiTietChoTacGia } from '../../services/khoaHocService';
import { layDanhGiaTheoKhoaHoc } from '../../services/danhGiaService';
import { layLopTheoKhoaHoc } from '../../services/lopHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';

import XemTepTinModal from '../../components/XemTepTinModal'; 
import '../../components/XemTepTinModal.css';

const NHAN_LOAI_BAI_HOC = { VIDEO: 'ti-player-play', PDF: 'ti-file-text', DOC: 'ti-file-text', QUIZ: 'ti-help-circle' };

function dinhDangThoiLuong(giay) {
  if (!giay) return '';
  const phut = Math.floor(giay / 60);
  const giayConLai = Math.round(giay % 60);
  return `${phut}:${String(giayConLai).padStart(2, '0')}`;
}

export default function ChiTietKhoaHocGiangVien() {
  const { maKhoaHoc } = useParams();
  const navigate = useNavigate();

  const [khoaHoc, setKhoaHoc] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [tabDangChon, setTabDangChon] = useState('giao-trinh');
  
  const [baiHocXemThu, setBaiHocXemThu] = useState(null);

  useEffect(() => {
    setDangTai(true);
    // Sử dụng API dành cho tác giả
    layChiTietChoTacGia(maKhoaHoc)
      .then(setKhoaHoc)
      .catch((err) => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, [maKhoaHoc]);

  if (dangTai) return <div className="empty-state">Đang tải dữ liệu khóa học...</div>;
  if (loi) return <div className="alert alert-danger" style={{ margin: 20 }}>{loi}</div>;
  if (!khoaHoc) return null;

  const tongSoBaiHoc = khoaHoc.danhSachChuong.reduce((tong, c) => tong + c.danhSachBaiHoc.length, 0);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <span className="t-title">
          <i className="ti ti-book-open" aria-hidden="true"></i> {khoaHoc.tieuDe}
          <span className="badge badge-blue" style={{ marginLeft: 12 }}>Góc nhìn Giảng viên</span>
        </span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/giang-vien/khoa-hoc')}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i> Quay lại danh sách
        </button>
      </div>

      <div className="sc-split">
        <div className="split-left">
          <h2 style={{ fontSize: 18, fontWeight: 500, marginBottom: 6 }}>{khoaHoc.tieuDe}</h2>
          <p style={{ fontSize: 13, color: 'var(--text-2)', marginBottom: 14 }}>{khoaHoc.mieuTa || 'Chưa có mô tả'}</p>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 16 }}>
            <span className="star">
              {'★'.repeat(Math.round(khoaHoc.diemTrungBinh || 0))}
            </span>
            <span style={{ fontSize: 12, color: 'var(--text-3)' }}>
              {khoaHoc.diemTrungBinh ? khoaHoc.diemTrungBinh.toFixed(1) : 'Chưa có đánh giá'} ({khoaHoc.soLuongDanhGia} đánh giá)
            </span>
            <span style={{ fontSize: 12, color: 'var(--text-3)' }}>•</span>
            <span style={{ fontSize: 12, color: 'var(--text-3)' }}>
              Trạng thái: <strong>{khoaHoc.trangThai}</strong>
            </span>
          </div>

          {khoaHoc.videoGioiThieu ? (
            <div className="video-player" style={{ height: 200, marginBottom: 16 }}>
              <div className="vp-title">{tongSoBaiHoc} bài học · {khoaHoc.danhSachChuong.length} chương</div>
              <div className="vp-btn">
                <i className="ti ti-player-play" aria-hidden="true"></i>
              </div>
            </div>
          ) : khoaHoc.anhBia ? (
            <div style={{ height: 200, marginBottom: 16, borderRadius: 8, overflow: 'hidden' }}>
              <img src={khoaHoc.anhBia} alt={khoaHoc.tieuDe} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
          ) : (
            <div style={{ height: 200, marginBottom: 16, borderRadius: 8, backgroundColor: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary)' }}>
              <i className="ti ti-photo" style={{ fontSize: 48 }} aria-hidden="true"></i>
            </div>
          )}

          <div className="tab-row">
            <div className={`tab-item ${tabDangChon === 'giao-trinh' ? 'active' : ''}`} onClick={() => setTabDangChon('giao-trinh')}>
              Giáo trình
            </div>
            <div className={`tab-item ${tabDangChon === 'danh-gia' ? 'active' : ''}`} onClick={() => setTabDangChon('danh-gia')}>
              Đánh giá của học viên
            </div>
            <div className={`tab-item ${tabDangChon === 'lop-hoc' ? 'active' : ''}`} onClick={() => setTabDangChon('lop-hoc')}>
              Lớp học đang sử dụng
            </div>
          </div>

          {tabDangChon === 'giao-trinh' && <TabGiaoTrinh khoaHoc={khoaHoc} onXemThu={setBaiHocXemThu} />}
          {tabDangChon === 'danh-gia' && <TabDanhGia maKhoaHoc={Number(maKhoaHoc)} />}
          {tabDangChon === 'lop-hoc' && <TabLopHocSuDung maKhoaHoc={Number(maKhoaHoc)} />}
        </div>

        <div className="split-right">
          <div className="card sm">
            <div style={{ fontSize: 22, fontWeight: 600, color: 'var(--primary)', marginBottom: 4 }}>
              {Number(khoaHoc.gia) === 0 ? 'Miễn phí' : `${Number(khoaHoc.gia).toLocaleString('vi-VN')} VNĐ`}
            </div>
            
            {/* Đổi thành nút Chỉnh sửa vì đây là khóa của chính họ */}
            {khoaHoc.trangThai !== 'CHO_KIEM_DUYET' && khoaHoc.trangThai !== 'CONG_KHAI' && (
              <button 
                className="btn btn-primary" 
                style={{ width: '100%', marginBottom: 8, marginTop: 10 }} 
                onClick={() => navigate(`/giang-vien/khoa-hoc/sua/${maKhoaHoc}`)}
              >
                <i className="ti ti-edit" aria-hidden="true"></i> Chỉnh sửa khóa học
              </button>
            )}
            {khoaHoc.trangThai === 'CONG_KHAI' && (
              <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 10, marginBottom: 8 }}>
                Khóa học đang công khai nên không thể sửa trực tiếp. Vui lòng lưu trữ khóa học trước khi chỉnh sửa.
              </div>
            )}
            {khoaHoc.trangThai === 'CHO_KIEM_DUYET' && (
              <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 10, marginBottom: 8 }}>
                Khóa học đang chờ kiểm duyệt nên không thể sửa trực tiếp. Vui lòng rút lại yêu cầu duyệt trước.
              </div>
            )}

            {khoaHoc.phiDayHoc != null && (
              <div style={{ fontSize: 12, color: 'var(--text-3)', borderTop: '1px solid var(--border)', paddingTop: 10, marginTop: 6 }}>
                Giấy phép Giảng dạy: <strong>{Number(khoaHoc.phiDayHoc).toLocaleString('vi-VN')} VNĐ</strong>
              </div>
            )}
          </div>
          
          <div className="card sm">
            <div className="card-title" style={{ fontSize: 13 }}>Khóa học bao gồm</div>
            <div style={{ fontSize: 12, color: 'var(--text-2)', display: 'flex', flexDirection: 'column', gap: 6 }}>
              <span><i className="ti ti-video" aria-hidden="true"></i> {tongSoBaiHoc} bài học</span>
              <span><i className="ti ti-certificate" aria-hidden="true"></i> Danh mục: {khoaHoc.tenDanhMuc}</span>
            </div>
          </div>
        </div>
      </div>

      {baiHocXemThu && (
        <XemTepTinModal
          baiHoc={baiHocXemThu}
          onClose={() => setBaiHocXemThu(null)}
          xemThu
        />
      )}
    </div>
  );
}

function TabGiaoTrinh({ khoaHoc, onXemThu }) {
  return (
    <div className="card sm">
      {khoaHoc.danhSachChuong.length === 0 && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Khóa học chưa có nội dung.</p>}
      {khoaHoc.danhSachChuong.map((chuong) => (
        <div key={chuong.maChuong} style={{ marginBottom: 10 }}>
          <div style={{ fontSize: 13, fontWeight: 600, padding: '6px 4px' }}>{chuong.tieuDe}</div>
          {chuong.danhSachBaiHoc.map((b) => (
            <div 
              key={b.maBaiHoc} 
              className="curr-item" 
              style={{ cursor: 'pointer' }} /* Tác giả luôn click được */
              onClick={() => onXemThu(b)} /* Bỏ chặn kiểm tra b.xemThu */
            >
              <div className={`ci-icon ${b.xemThu ? 'done' : ''}`}>
                <i className={`ti ${NHAN_LOAI_BAI_HOC[b.loai] || 'ti-file'}`} aria-hidden="true"></i>
              </div>
              <div className="ci-title">{b.tieuDe}</div>
              
              {/* Vẫn hiện badge/ổ khóa để tác giả biết bài nào đang được Public/Private */}
              {b.xemThu ? (
                <span className="badge badge-green">Học viên được xem thử</span>
              ) : (
                <i className="ti ti-lock" style={{ color: 'var(--text-3)' }} title="Bài học bị khóa với người chưa mua" aria-hidden="true"></i>
              )}
              {b.metadata?.thoiLuongGiay && <span className="ci-dur">{dinhDangThoiLuong(b.metadata.thoiLuongGiay)}</span>}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}

// Giảng viên chỉ xem đánh giá, không tự review khóa của mình
function TabDanhGia({ maKhoaHoc }) {
  const [danhSach, setDanhSach] = useState({ content: [], totalElements: 0 });

  useEffect(() => {
    layDanhGiaTheoKhoaHoc(maKhoaHoc).then(setDanhSach).catch(() => { });
  }, [maKhoaHoc]);

  return (
    <div>
      <div style={{ fontSize: 13, color: 'var(--text-2)', margin: '14px 4px' }}>{danhSach.totalElements} đánh giá từ học viên</div>
      {danhSach.content.length === 0 && <div className="empty-state">Chưa có đánh giá nào.</div>}
      {danhSach.content.map((d) => (
        <div key={d.maDanhGia} className="forum-post">
          <div className="fp-header">
            <div className="fp-avatar">{(d.tenNguoiDung || '?').charAt(0).toUpperCase()}</div>
            <div className="fp-name">{d.tenNguoiDung || 'Học viên'}</div>
            <span className="star" style={{ marginLeft: 8 }}>{'★'.repeat(d.soSao)}</span>
            <div className="fp-time">{new Date(d.ngayTao).toLocaleDateString('vi-VN')}</div>
          </div>
          {d.nhanXet && <div className="fp-body">{d.nhanXet}</div>}
        </div>
      ))}
    </div>
  );
}

const NHAN_TRANG_THAI_LOP = {
  SAP_DIEN_RA: { nhan: 'Sắp diễn ra', lop: 'badge-amber' },
  HOAT_DONG: { nhan: 'Đang hoạt động', lop: 'badge-green' },
  DA_DONG: { nhan: 'Đã đóng', lop: 'badge-gray' },
};

// Tác giả xem toàn bộ lớp học (của mọi giảng viên giữ Giấy phép Giảng dạy)
// đang được xây dựng dựa trên khóa học của mình.
function TabLopHocSuDung({ maKhoaHoc }) {
  const [danhSach, setDanhSach] = useState(null);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    layLopTheoKhoaHoc(maKhoaHoc)
      .then(setDanhSach)
      .catch((err) => setLoi(layThongBaoLoi(err)));
  }, [maKhoaHoc]);

  if (loi) return <div className="alert alert-danger" style={{ marginTop: 14 }}>{loi}</div>;
  if (danhSach === null) return <p style={{ fontSize: 13, color: 'var(--text-3)', margin: '14px 4px' }}>Đang tải...</p>;

  return (
    <div>
      <div style={{ fontSize: 13, color: 'var(--text-2)', margin: '14px 4px' }}>
        {danhSach.length} lớp học đang xây dựng dựa trên khóa học này
      </div>
      {danhSach.length === 0 && (
        <div className="empty-state">
          Chưa có lớp học nào — kể cả của bạn lẫn giảng viên khác đang giữ Giấy phép Giảng dạy.
        </div>
      )}
      {danhSach.map((lop) => {
        const tt = NHAN_TRANG_THAI_LOP[lop.trangThai] || { nhan: lop.trangThai, lop: 'badge-gray' };
        return (
          <div key={lop.maLopHoc} className="card sm" style={{ marginBottom: 10 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 10, flexWrap: 'wrap' }}>
              <div>
                <div style={{ fontWeight: 600, fontSize: 14 }}>
                  Lớp {lop.maLop} <span className={`badge ${tt.lop}`} style={{ marginLeft: 6 }}>{tt.nhan}</span>
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 4 }}>
                  Giảng viên phụ trách: <strong>{lop.tenGiangVien || `#${lop.maGiangVien}`}</strong>
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 2 }}>
                  Khai giảng: {lop.ngayBatDau ? new Date(lop.ngayBatDau).toLocaleDateString('vi-VN') : '—'}
                  {lop.ngayKetThuc && <> · Kết thúc: {new Date(lop.ngayKetThuc).toLocaleDateString('vi-VN')}</>}
                </div>
              </div>
              <div style={{ textAlign: 'right', fontSize: 12, color: 'var(--text-3)' }}>
                <div>{lop.soThanhVien}/{lop.siSo} học viên</div>
                {lop.diemTrungBinh != null && (
                  <div style={{ marginTop: 4 }}>
                    <span className="star">{'★'.repeat(Math.round(lop.diemTrungBinh))}</span>
                    {' '}({lop.soLuongDanhGia})
                  </div>
                )}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}