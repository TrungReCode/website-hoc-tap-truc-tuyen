import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { layThongBao, docTatCa, docMot, demChuaDoc } from '../services/traoDoiService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

const LOAI_ICON = {
  BAI_DANG_MOI: 'ti-speakerphone',
  BINH_LUAN_MOI: 'ti-message-2',
  GIAI_DAP: 'ti-circle-check',
  THANH_TOAN_THANH_CONG: 'ti-credit-card',
  HOAN_TIEN_THANH_CONG: 'ti-receipt-refund',
  HOAN_TIEN_TU_CHOI: 'ti-receipt-off',
  KHOA_HOC_DA_DUYET: 'ti-rosette-discount-check',
  KHOA_HOC_BI_TU_CHOI: 'ti-circle-x',
  KHOA_HOC_YEU_CAU_BO_SUNG: 'ti-edit',
  LOP_HOC_DA_HUY: 'ti-calendar-off',
  DANH_GIA_BI_XU_LY: 'ti-message-off',
  TAI_KHOAN_BI_KHOA: 'ti-lock',
};

const LOAI_MAU = {
  BAI_DANG_MOI: 'var(--primary)',
  BINH_LUAN_MOI: '#7c3aed',
  GIAI_DAP: 'var(--success)',
  THANH_TOAN_THANH_CONG: 'var(--success)',
  HOAN_TIEN_THANH_CONG: 'var(--primary)',
  HOAN_TIEN_TU_CHOI: 'var(--danger)',
  KHOA_HOC_DA_DUYET: 'var(--success)',
  KHOA_HOC_BI_TU_CHOI: 'var(--danger)',
  KHOA_HOC_YEU_CAU_BO_SUNG: '#d97706',
  LOP_HOC_DA_HUY: 'var(--danger)',
  DANH_GIA_BI_XU_LY: 'var(--text-3)',
  TAI_KHOAN_BI_KHOA: 'var(--danger)',
};

// Màu/icon mặc định cho các loại chưa được khai báo (phòng khi backend thêm
// LoaiThongBao mới mà quên cập nhật 2 bảng tra cứu ở trên).
const MAU_MAC_DINH = 'var(--text-3)';

export default function TrangThongBao() {
  const navigate = useNavigate();
  const [danhSach, setDanhSach]  = useState([]);
  const [trang,    setTrang]     = useState(0);
  const [hetTrang, setHetTrang]  = useState(false);
  const [dangTai,  setDangTai]   = useState(false);
  const [loi,      setLoi]       = useState('');
  const [soChuaDoc, setSoChuaDoc] = useState(0);

  useEffect(() => {
    demChuaDoc().then(setSoChuaDoc);
  }, []);

  useEffect(() => {
    setDangTai(true);
    layThongBao({ page: trang, size: 20 })
      .then(res => {
        setDanhSach(prev => trang === 0 ? res.noiDung : [...prev, ...res.noiDung]);
        setHetTrang(res.laTrangCuoi);
      })
      .catch(err => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, [trang]);

  async function xuLyDocTatCa() {
    try {
      await docTatCa();
      setDanhSach(prev => prev.map(tb => ({ ...tb, trangThaiDoc: true })));
      setSoChuaDoc(0);
    } catch (err) {
      alert(layThongBaoLoi(err));
    }
  }

  async function xuLyNhanThongBao(tb) {
    // Đánh dấu đã đọc
    if (!tb.trangThaiDoc) {
      try {
        await docMot(tb.maThongBao);
        setDanhSach(prev => prev.map(t =>
          t.maThongBao === tb.maThongBao ? { ...t, trangThaiDoc: true } : t));
        setSoChuaDoc(c => Math.max(0, c - 1));
      } catch { /* bỏ qua lỗi */ }
    }

    // Điều hướng tới nơi liên quan — khớp với duLieuDong mà từng LoaiThongBao
    // thực sự gửi kèm ở backend (xem DuyetKhoaHocDichVuImpl, TaoLopHocDichVuImpl,
    // DanhGiaKhoaHocDichVuImpl, ThanhToanDichVuImpl, TaiChinhDichVuImpl, QuanTriDichVuImpl).
    const d = tb.duLieuDong || {};
    if (d.maLopHoc && tb.loai !== 'LOP_HOC_DA_HUY') {
      // Bài đăng mới / bình luận mới / giải đáp → mở đúng bài đăng trong diễn đàn lớp
      navigate(`/lop-hoc/${d.maLopHoc}/dien-dan`, { state: { maBaiDangMo: d.maBaiDang } });
    } else if (d.maGiaoDich) {
      // Thanh toán thành công / hoàn tiền thành công / hoàn tiền bị từ chối
      navigate('/thanh-toan/lich-su');
    } else if (tb.loai === 'LOP_HOC_DA_HUY') {
      // Lớp đã bị hủy trước khi khai giảng — diễn đàn của lớp không còn ý nghĩa,
      // đưa người dùng tới lịch sử giao dịch để xem tình trạng hoàn tiền thay vì vào diễn đàn.
      navigate('/thanh-toan/lich-su');
    } else if (tb.loai === 'KHOA_HOC_DA_DUYET' || tb.loai === 'KHOA_HOC_BI_TU_CHOI' || tb.loai === 'KHOA_HOC_YEU_CAU_BO_SUNG') {
      // Gửi cho giảng viên (tác giả khóa học) → trang quản lý khóa học của giảng viên
      if (d.maKhoaHoc) navigate(`/giang-vien/khoa-hoc/${d.maKhoaHoc}`);
    } else if (tb.loai === 'DANH_GIA_BI_XU_LY') {
      // Gửi cho học viên đã viết đánh giá bị ẩn/xóa → trang chi tiết khóa học công khai
      if (d.maKhoaHoc) navigate(`/khoa-hoc/${d.maKhoaHoc}`);
    }
    // TAI_KHOAN_BI_KHOA: không có nơi điều hướng phù hợp (tài khoản đã bị khóa
    // ngay lập tức nên các trang khác cũng sẽ từ chối truy cập) — chỉ đánh dấu đã đọc.
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>

      {/* Topbar */}
      <div className="topbar">
        <button className="btn btn-secondary btn-sm" onClick={() => navigate(-1)}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i>
        </button>
        <span className="t-title">
          <i className="ti ti-bell" aria-hidden="true"></i>
          Thông báo
          {soChuaDoc > 0 && (
            <span className="notif-badge" style={{ marginLeft: 6 }}>{soChuaDoc}</span>
          )}
        </span>
        {soChuaDoc > 0 && (
          <button className="btn btn-accent btn-sm" onClick={xuLyDocTatCa}>
            <i className="ti ti-checks" aria-hidden="true"></i> Đọc tất cả
          </button>
        )}
      </div>

      {/* Danh sách thông báo */}
      <div style={{ flex: 1, overflowY: 'auto', padding: 16, maxWidth: 640, margin: '0 auto', width: '100%' }}>
        {loi && <div className="alert alert-danger">{loi}</div>}

        {danhSach.length === 0 && !dangTai && (
          <div className="card" style={{ textAlign: 'center', padding: 40 }}>
            <i className="ti ti-bell-off" style={{ fontSize: 40, color: 'var(--text-3)' }} aria-hidden="true"></i>
            <p style={{ marginTop: 12, color: 'var(--text-3)', fontSize: 13 }}>Chưa có thông báo nào.</p>
          </div>
        )}

        {danhSach.map(tb => (
          <div key={tb.maThongBao}
            onClick={() => xuLyNhanThongBao(tb)}
            style={{
              display: 'flex', gap: 12, padding: '14px 16px',
              borderRadius: 10, marginBottom: 8, cursor: 'pointer',
              background: tb.trangThaiDoc ? 'var(--white)' : 'var(--primary-light)',
              border: `1px solid ${tb.trangThaiDoc ? 'var(--border)' : 'rgba(26,111,212,.2)'}`,
              transition: 'background .15s',
            }}>

            {/* Icon loại */}
            <div style={{
              width: 36, height: 36, borderRadius: '50%', flexShrink: 0,
              background: (LOAI_MAU[tb.loai] || MAU_MAC_DINH) + '18',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: LOAI_MAU[tb.loai] || MAU_MAC_DINH, fontSize: 16,
            }}>
              <i className={`ti ${LOAI_ICON[tb.loai] || 'ti-bell'}`} aria-hidden="true"></i>
            </div>

            {/* Nội dung */}
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13, fontWeight: tb.trangThaiDoc ? 400 : 600, color: 'var(--text)', marginBottom: 2, display: 'flex', alignItems: 'center', gap: 6 }}>
                {tb.tieuDe}
                {tb.thongBaoTuongTac && (
                  <span className="badge badge-amber" style={{ fontSize: 10 }}>Cần xử lý</span>
                )}
              </div>
              {tb.noiDung && (
                <div style={{ fontSize: 12, color: 'var(--text-2)', marginBottom: 4 }}>{tb.noiDung}</div>
              )}
              <div style={{ fontSize: 11, color: 'var(--text-3)' }}>
                {new Date(tb.ngayTao).toLocaleString('vi-VN')}
              </div>
            </div>

            {/* Chấm chưa đọc */}
            {!tb.trangThaiDoc && (
              <div style={{
                width: 8, height: 8, borderRadius: '50%',
                background: 'var(--primary)', flexShrink: 0, alignSelf: 'center',
              }} />
            )}
          </div>
        ))}

        {dangTai && <p style={{ textAlign: 'center', color: 'var(--text-3)', fontSize: 13, marginTop: 20 }}>Đang tải...</p>}

        {!hetTrang && !dangTai && danhSach.length > 0 && (
          <button className="btn btn-secondary btn-sm" style={{ width: '100%', marginTop: 8 }}
            onClick={() => setTrang(t => t + 1)}>
            Xem thêm
          </button>
        )}
      </div>
    </div>
  );
}