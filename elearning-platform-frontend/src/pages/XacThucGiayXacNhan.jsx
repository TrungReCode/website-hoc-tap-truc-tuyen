import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { xacThucGiay } from '../services/chungChiService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';

function dinhDangNgay(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

/**
 * Trang công khai — KHÔNG yêu cầu đăng nhập, đúng URL đã in trong mã QR
 * trên giấy PDF (xem TaoFilePdf.java / GiayXacNhanDichVuImpl.renderVaLuuFile).
 */
export default function XacThucGiayXacNhan() {
  const { maXacThuc } = useParams();
  const navigate = useNavigate();
  const [giay, setGiay] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [khongTimThay, setKhongTimThay] = useState(false);

  useEffect(() => {
    let daHuy = false;
    setDangTai(true);
    setKhongTimThay(false);
    xacThucGiay(maXacThuc)
      .then(g => { if (!daHuy) setGiay(g); })
      .catch(() => { if (!daHuy) setKhongTimThay(true); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
  }, [maXacThuc]);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '40px 16px' }}>
      <div style={{ maxWidth: 480, width: '100%' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <i className="ti ti-school" style={{ fontSize: 32, color: 'var(--brand)' }} aria-hidden="true"></i>
          <div style={{ fontWeight: 700, fontSize: 15, marginTop: 6 }}>WEBSITE HỌC TẬP TRỰC TUYẾN</div>
          <div style={{ fontSize: 12, color: 'var(--text-3)' }}>Hệ thống tra cứu Giấy xác nhận</div>
        </div>

        {dangTai && (
          <p style={{ textAlign: 'center', fontSize: 13, color: 'var(--text-3)' }}>Đang tra cứu mã xác thực...</p>
        )}

        {!dangTai && khongTimThay && (
          <div className="card sm" style={{ textAlign: 'center', padding: 28 }}>
            <i className="ti ti-shield-x" style={{ fontSize: 40, color: 'var(--danger)' }} aria-hidden="true"></i>
            <div style={{ fontWeight: 700, fontSize: 16, marginTop: 10 }}>Không tìm thấy</div>
            <p style={{ fontSize: 13, color: 'var(--text-3)', marginTop: 6 }}>
              Mã xác thực <b>{maXacThuc}</b> không tồn tại trong hệ thống, hoặc đã bị thu hồi.
              Vui lòng kiểm tra lại mã, hoặc quét lại mã QR trên giấy gốc.
            </p>
          </div>
        )}

        {!dangTai && giay && (
          <div className="card sm" style={{ padding: 0, overflow: 'hidden' }}>
            <div
              style={{
                background: giay.trangThai === 'HOP_LE' ? 'var(--success-bg, #dcfce7)' : 'var(--danger-bg, #fee2e2)',
                padding: '16px 20px',
                display: 'flex', alignItems: 'center', gap: 10,
              }}
            >
              <i
                className={`ti ${giay.trangThai === 'HOP_LE' ? 'ti-circle-check' : 'ti-circle-x'}`}
                style={{ fontSize: 24, color: giay.trangThai === 'HOP_LE' ? '#15803d' : '#b91c1c' }}
                aria-hidden="true"
              ></i>
              <div>
                <div style={{ fontWeight: 700, color: giay.trangThai === 'HOP_LE' ? '#15803d' : '#b91c1c' }}>
                  {giay.trangThai === 'HOP_LE' ? 'Giấy hợp lệ' : 'Giấy đã bị thu hồi'}
                </div>
                <div style={{ fontSize: 11, color: 'var(--text-3)' }}>Mã xác thực: {giay.maXacThuc}</div>
              </div>
            </div>

            <div style={{ padding: 20, display: 'grid', gap: 12 }}>
              <div>
                <div style={{ fontSize: 11, color: 'var(--text-3)' }}>Loại</div>
                <div style={{ fontWeight: 600 }}>
                  {giay.loai === 'LOP_HOC' ? 'Giấy xác nhận hoàn thành lớp học' : 'Giấy xác nhận hoàn thành khóa học'}
                </div>
              </div>

              <div style={{ display: 'flex', gap: 24 }}>
                <div>
                  <div style={{ fontSize: 11, color: 'var(--text-3)' }}>Tỷ lệ hoàn thành</div>
                  <div style={{ fontWeight: 700, fontSize: 18 }}>{Number(giay.phanTramHoanThanh).toFixed(0)}%</div>
                </div>
                <div>
                  <div style={{ fontSize: 11, color: 'var(--text-3)' }}>Điểm trung bình</div>
                  <div style={{ fontWeight: 700, fontSize: 18 }}>
                    {giay.diemTrungBinh != null ? Number(giay.diemTrungBinh).toFixed(1) : '—'}
                  </div>
                </div>
              </div>

              <div>
                <div style={{ fontSize: 11, color: 'var(--text-3)' }}>Ngày cấp</div>
                <div style={{ fontWeight: 600 }}>{dinhDangNgay(giay.ngayCap)}</div>
              </div>

              {giay.filePdfUrl && (
                <a
                  className="btn btn-primary"
                  href={giay.filePdfUrl}
                  target="_blank"
                  rel="noreferrer"
                  style={{ width: '100%', justifyContent: 'center' }}
                >
                  <i className="ti ti-file-download" aria-hidden="true"></i> Xem file gốc (PDF)
                </a>
              )}
            </div>
          </div>
        )}

        <div style={{ textAlign: 'center', marginTop: 20 }}>
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/chung-chi-cua-toi')}>
            Quay lại
          </button>
        </div>
      </div>
    </div>
  );
}
