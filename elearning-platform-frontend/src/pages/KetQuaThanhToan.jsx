import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { xacNhanThanhToanVnpay } from '../services/thanhToanService';

/**
 * Trích mã lớp (8 ký tự) từ mã giao dịch phí lớp học.
 *
 * Backend sinh mã giao dịch cho khoản phí lớp học theo định dạng:
 *   LH-{maNguoiDung}-{maLopHoc}-{uuid12}-{maLop}
 * (xem ThanhToanDichVuImpl.taoMaGiaoDich / taoDonPhiLopHoc). Mã lớp luôn là
 * đoạn CUỐI CÙNG sau dấu '-' vì mã lớp gồm 8 ký tự chữ+số, không chứa '-'.
 */
function trichMaLopTuGiaoDich(maGiaoDich) {
  if (!maGiaoDich || !maGiaoDich.startsWith('LH-')) return null;
  const phan = maGiaoDich.split('-');
  return phan.length >= 5 ? phan[phan.length - 1] : null;
}

export default function KetQuaThanhToan() {
  const navigate = useNavigate();
  const location = useLocation(); // Lấy URL hiện tại (bao gồm cả chuỗi ?vnp_...)
  
  const [trangThai, setTrangThai] = useState('DANG_XU_LY'); // DANG_XU_LY, THANH_CONG, THAT_BAI
  const [thongBao, setThongBao] = useState('Đang đối soát giao dịch với ngân hàng, vui lòng chờ...');
  const [maGiaoDich, setMaGiaoDich] = useState('');

  useEffect(() => {
    let isMounted = true;

    async function xacNhanGiaoDich() {
      // Nếu không có tham số nào trên URL thì báo lỗi ngay
      if (!location.search) {
        setTrangThai('THAT_BAI');
        setThongBao('Không tìm thấy thông tin giao dịch.');
        return;
      }

      try {
        // Gửi chuỗi query string (location.search) về Backend
        const ketQua = await xacNhanThanhToanVnpay(location.search);
        
        if (!isMounted) return;

        setMaGiaoDich(ketQua.maGiaoDichVnpay);

        if (ketQua.thanhCong) {
          setTrangThai('THANH_CONG');
          setThongBao('Giao dịch thành công! Cảm ơn bạn đã thanh toán.');
        } else {
          setTrangThai('THAT_BAI');
          setThongBao(ketQua.message || 'Giao dịch bị từ chối hoặc thất bại.');
        }
      } catch (error) {
        if (!isMounted) return;
        setTrangThai('THAT_BAI');
        setThongBao('Có lỗi xảy ra khi xác thực giao dịch. Vui lòng liên hệ hỗ trợ.');
      }
    }

    xacNhanGiaoDich();

    return () => {
      isMounted = false;
    };
  }, [location.search]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: 'var(--bg-color, #f4f6f8)' }}>
      <div style={{ padding: 24, maxWidth: 500, width: '100%', margin: '40px auto' }}>
        <div className="card" style={{ textAlign: 'center', padding: '40px 20px', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
          
          {/* Trạng thái Đang xử lý */}
          {trangThai === 'DANG_XU_LY' && (
            <div>
              <div className="spinner-border text-primary" role="status" style={{ width: '3rem', height: '3rem', marginBottom: 20 }}>
                <span className="visually-hidden">Đang tải...</span>
              </div>
              <h4 style={{ color: 'var(--text-1)' }}>Đang xử lý thanh toán...</h4>
              <p style={{ color: 'var(--text-2)' }}>Vui lòng không đóng trình duyệt lúc này.</p>
            </div>
          )}

          {/* Trạng thái Thành công */}
          {trangThai === 'THANH_CONG' && (
            <div>
              <div style={{ fontSize: 60, color: '#28a745', marginBottom: 16 }}>
                <i className="ti ti-circle-check-filled"></i>
              </div>
              <h4 style={{ color: '#28a745', fontWeight: 'bold' }}>Thanh Toán Thành Công</h4>
              <p style={{ color: 'var(--text-2)', marginTop: 10 }}>{thongBao}</p>
              {maGiaoDich && (
                <div style={{ background: '#f8f9fa', padding: '10px', borderRadius: 8, marginTop: 16, fontSize: 14 }}>
                  Mã giao dịch: <strong>{maGiaoDich}</strong>
                </div>
              )}
              <div style={{ marginTop: 24, display: 'flex', gap: 12, justifyContent: 'center' }}>
                {maGiaoDich.startsWith('LH-') ? (
                  <button
                    className="btn btn-primary"
                    onClick={() => {
                      // Thanh toán phí lớp học chỉ thỏa "Khóa 2" (phí lớp), CHƯA tự
                      // động ghi danh (thêm ThanhVienLop) — vẫn cần gọi lại API tham
                      // gia. Trích sẵn mã lớp từ mã giao dịch, truyền qua query
                      // ?ma=...&tuDong=1 để trang Tham gia tự động thực hiện tham
                      // gia ngay khi vào, đúng như tên nút "Vào Lớp Học Ngay".
                      const maLop = trichMaLopTuGiaoDich(maGiaoDich);
                      navigate(maLop ? `/lop-hoc/tham-gia?ma=${maLop}&tuDong=1` : '/lop-hoc/tham-gia');
                    }}
                  >
                    Vào Lớp Học Ngay
                  </button>
                ) : (
                  <button className="btn btn-primary" onClick={() => navigate('/trang-chu')}>
                    Về Trang Chủ
                  </button>
                )}
              </div>
            </div>
          )}

          {/* Trạng thái Thất bại */}
          {trangThai === 'THAT_BAI' && (
            <div>
              <div style={{ fontSize: 60, color: '#dc3545', marginBottom: 16 }}>
                <i className="ti ti-alert-circle-filled"></i>
              </div>
              <h4 style={{ color: '#dc3545', fontWeight: 'bold' }}>Thanh Toán Thất Bại</h4>
              <p style={{ color: 'var(--text-2)', marginTop: 10 }}>{thongBao}</p>
              {maGiaoDich && (
                <div style={{ background: '#f8f9fa', padding: '10px', borderRadius: 8, marginTop: 16, fontSize: 14 }}>
                  Mã giao dịch: <strong>{maGiaoDich}</strong>
                </div>
              )}
              <div style={{ marginTop: 24, display: 'flex', gap: 12, justifyContent: 'center' }}>
                <button className="btn btn-secondary" onClick={() => navigate('/lop-hoc/thanh-toan')}>
                  Thử lại
                </button>
                <button className="btn btn-outline-secondary" onClick={() => navigate('/trang-chu')}>
                  Về trang chủ
                </button>
              </div>
            </div>
          )}

        </div>
      </div>
    </div>
  );
}