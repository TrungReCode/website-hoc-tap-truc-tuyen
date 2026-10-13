import { useEffect, useState } from 'react';
import BoLocThoiGianNhanh from './BoLocThoiGianNhanh';
import BieuDoTron from './BieuDoTron';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

const NHAN_LOAI = { KHOA_HOC: 'Mua khóa học', LOP_HOC: 'Phí lớp học' };

function dinhDangTien(so) {
  return `${Number(so || 0).toLocaleString('vi-VN')} đ`;
}

/**
 * Màn Hình Tổng Quan doanh thu — dùng chung cho giảng viên và admin
 * (QTV Cao cấp/QTV Tài chính). Khác biệt giữa 2 vai trò chỉ nằm ở hàm
 * `layTongQuan` được truyền vào và các bộ lọc bổ sung (`boLocBoSung`).
 *
 * @param layTongQuan   (params) => Promise<PhanHoiTongQuanDoanhThu>
 * @param extraParams   object — filter bổ sung riêng của từng vai trò (vd. maKhoaHoc, maGiangVien phía admin)
 * @param boLocBoSung   React node — UI filter bổ sung hiển thị cạnh bộ lọc thời gian
 */
export default function TongQuanDoanhThu({ layTongQuan, extraParams, boLocBoSung }) {
  const [tuNgay, setTuNgay] = useState('');
  const [denNgay, setDenNgay] = useState('');
  const [duLieu, setDuLieu] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    let daHuy = false;
    setDangTai(true);
    setLoi('');
    layTongQuan({ tuNgay: tuNgay || undefined, denNgay: denNgay || undefined, ...extraParams })
      .then((kq) => { if (!daHuy) setDuLieu(kq); })
      .catch((err) => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tuNgay, denNgay, JSON.stringify(extraParams)]);

  return (
    <div>
      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', alignItems: 'flex-end', marginBottom: 4 }}>
        <BoLocThoiGianNhanh tuNgay={tuNgay} denNgay={denNgay} onDoiKhoang={(t, d) => { setTuNgay(t); setDenNgay(d); }} />
        {boLocBoSung}
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}

      {dangTai ? (
        <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
      ) : (
        <>
          {/* Thẻ KPI dòng tiền */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14, marginBottom: 20 }}>
            <div className="card sm" style={{ borderLeft: '4px solid var(--primary)' }}>
              <div style={{ fontSize: 12, color: 'var(--text-3)' }}>Tổng doanh thu ghi nhận</div>
              <div style={{ fontSize: 20, fontWeight: 700, marginTop: 4 }}>{dinhDangTien(duLieu?.tongDoanhThuGhiNhan)}</div>
              <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 2 }}>{duLieu?.soLuongGiaoDich || 0} giao dịch</div>
            </div>
            <div className="card sm" style={{ borderLeft: '4px solid #10b981' }}>
              <div style={{ fontSize: 12, color: 'var(--text-3)' }}>Doanh thu thực thu</div>
              <div style={{ fontSize: 20, fontWeight: 700, marginTop: 4, color: '#10b981' }}>{dinhDangTien(duLieu?.doanhThuThucThu)}</div>
              <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 2 }}>Đã giải ngân — tiền đã về tài khoản</div>
            </div>
            <div className="card sm" style={{ borderLeft: '4px solid var(--warning, #f59e0b)' }}>
              <div style={{ fontSize: 12, color: 'var(--text-3)' }}>Chờ đối soát / chưa thu</div>
              <div style={{ fontSize: 20, fontWeight: 700, marginTop: 4, color: 'var(--warning, #f59e0b)' }}>{dinhDangTien(duLieu?.doanhThuChoDoiSoat)}</div>
              <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 2 }}>Đang trong chu kỳ ký quỹ</div>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            {/* Biểu đồ phân bổ theo loại giao dịch */}
            <div className="card">
              <div className="card-title" style={{ marginBottom: 12 }}>
                <i className="ti ti-chart-pie" aria-hidden="true"></i> Phân bổ theo loại giao dịch
              </div>
              <BieuDoTron
                duLieu={(duLieu?.phanBoTheoLoai || []).map((p) => ({ nhan: NHAN_LOAI[p.loai] || p.loai, giaTri: p.soTien }))}
                dinhDangGiaTri={dinhDangTien}
              />
            </div>

            {/* Cảnh báo bất thường */}
            
          </div>
        </>
      )}
    </div>
  );
}
