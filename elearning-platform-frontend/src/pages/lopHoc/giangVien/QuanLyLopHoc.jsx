import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import {
  layChiTietGiangVien,
  layDanhGiaLop,
  capNhatCauHinhLop
} from '../../../services/lopHocService';
import { layThanhVien } from '../../../services/ghiDanhService';
import { layThongBaoLoi } from '../../../utils/layThongBaoLoi';

const NHAN_TRANG_THAI = {
  SAP_DIEN_RA: { nhan: 'Sắp diễn ra', lop: 'badge-blue' },
  HOAT_DONG: { nhan: 'Hoạt động', lop: 'badge-green' },
  DA_DONG: { nhan: 'Đã đóng', lop: 'badge-gray' }
};

export default function QuanLyLopHoc() {
  const { maLopHoc } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [lop, setLop] = useState(null);
  const [loi, setLoi] = useState('');

  const tabBanDau = searchParams.get('tab') === 'thanh-vien' ? 'thanh-vien' : 'tong-quan';
  const [tab, setTab] = useState(tabBanDau);

  const [thanhVien, setThanhVien] = useState([]);
  const [dsDanhGia, setDsDanhGia] = useState([]);
  const [dangTaiDanhGia, setDangTaiDanhGia] = useState(true);

  // Trạng thái chỉnh sửa sĩ số
  const [dangSuaSiSo, setDangSuaSiSo] = useState(false);
  const [siSoMoi, setSiSoMoi] = useState('');
  const [dangLuuSiSo, setDangLuuSiSo] = useState(false);
  const [loiSiSo, setLoiSiSo] = useState('');

  useEffect(() => {
    layChiTietGiangVien(maLopHoc)
      .then(data => {
        setLop(data);
        setSiSoMoi(data?.siSo ?? '');
      })
      .catch(err => setLoi(layThongBaoLoi(err)));

    layThanhVien(maLopHoc)
      .then(setThanhVien)
      .catch(err => console.error('Lỗi khi tải thành viên:', err));

    layDanhGiaLop(maLopHoc, { size: 50 })
      .then(kq => setDsDanhGia(kq?.content || []))
      .catch(err => console.error('Lỗi khi tải đánh giá:', err))
      .finally(() => setDangTaiDanhGia(false));
  }, [maLopHoc]);

  // Chỉnh sĩ số lớp học
  async function xuLyCapNhatSiSo() {
    setLoiSiSo('');

    const giaTriMoi = Number(siSoMoi);
    const giaTriCu = Number(lop.siSo);
    const soDangGhiDanh = thanhVien.length;

    if (!Number.isInteger(giaTriMoi) || giaTriMoi <= 0) {
      setLoiSiSo('Sĩ số phải là số nguyên lớn hơn 0');
      return;
    }

    // Lớp đang hoạt động: chỉ được tăng sĩ số
    if (lop.trangThai === 'HOAT_DONG' && giaTriMoi < giaTriCu) {
      setLoiSiSo(
        `Lớp đang hoạt động chỉ được tăng sĩ số, không thể giảm từ ${giaTriCu} xuống ${giaTriMoi}`
      );
      return;
    }

    // Lớp sắp diễn ra: được tăng/giảm nhưng không thấp hơn số học viên đang có
    if (lop.trangThai === 'SAP_DIEN_RA' && giaTriMoi < soDangGhiDanh) {
      setLoiSiSo(
        `Không thể giảm sĩ số xuống dưới ${soDangGhiDanh} vì hiện có ${soDangGhiDanh} học viên đang ghi danh`
      );
      return;
    }

    if (giaTriMoi === giaTriCu) {
      setDangSuaSiSo(false);
      return;
    }

    setDangLuuSiSo(true);

    try {
      const lopMoi = await capNhatCauHinhLop(maLopHoc, {
        ngayBatDau: lop.ngayBatDau,
        ngayKetThuc: lop.ngayKetThuc || null,
        siSo: giaTriMoi,
        phiLopHoc: lop.phiLopHoc ?? 0,
        congKhai: lop.congKhai ?? true,
        tiLeHoanThanh: lop.tiLeHoanThanh ?? 0
      });

      setLop(lopMoi);
      setSiSoMoi(lopMoi?.siSo ?? '');
      setDangSuaSiSo(false);
      setLoiSiSo('');
    } catch (err) {
      setLoiSiSo(layThongBaoLoi(err));
    } finally {
      setDangLuuSiSo(false);
    }
  }

  function huySuaSiSo() {
    setDangSuaSiSo(false);
    setLoiSiSo('');
    setSiSoMoi(lop?.siSo ?? '');
  }

  if (loi) {
    return <div className="alert alert-danger" style={{ margin: 20 }}>{loi}</div>;
  }

  if (!lop) {
    return <div className="empty-state">Đang tải...</div>;
  }

  const tt = NHAN_TRANG_THAI[lop.trangThai] || {
    nhan: lop.trangThai,
    lop: 'badge-gray'
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/lop-hoc/${maLopHoc}`)}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i> Quay lại lớp học
        </button>

        <span className="t-title">
          <i className="ti ti-school" aria-hidden="true"></i> {lop.tenKhoaHoc || 'Lớp học'}
        </span>

        <span className={`t-badge ${tt.lop === 'badge-green' ? 'green' : tt.lop === 'badge-blue' ? '' : 'amber'}`}>
          {tt.nhan}
        </span>
      </div>

      {/* --- THANH ĐIỀU HƯỚNG TAB --- */}
      <div style={{ display: 'flex', gap: 20, padding: '0 20px', borderBottom: '1px solid var(--border)', marginTop: 10 }}>
        <div
          className={`tab-item ${tab === 'tong-quan' ? 'active' : ''}`}
          onClick={() => setTab('tong-quan')}
          style={{
            padding: '10px 0',
            cursor: 'pointer',
            borderBottom: tab === 'tong-quan' ? '2px solid var(--primary)' : 'none',
            fontWeight: tab === 'tong-quan' ? 600 : 400
          }}
        >
          Tổng quan cấu hình
        </div>

        <div
          className={`tab-item ${tab === 'thanh-vien' ? 'active' : ''}`}
          onClick={() => setTab('thanh-vien')}
          style={{
            padding: '10px 0',
            cursor: 'pointer',
            borderBottom: tab === 'thanh-vien' ? '2px solid var(--primary)' : 'none',
            fontWeight: tab === 'thanh-vien' ? 600 : 400
          }}
        >
          Thành viên ({thanhVien.length})
        </div>

        <div
          className={`tab-item ${tab === 'danh-gia' ? 'active' : ''}`}
          onClick={() => setTab('danh-gia')}
          style={{
            padding: '10px 0',
            cursor: 'pointer',
            borderBottom: tab === 'danh-gia' ? '2px solid var(--primary)' : 'none',
            fontWeight: tab === 'danh-gia' ? 600 : 400
          }}
        >
          Đánh giá ({dsDanhGia.length})
        </div>
      </div>

      <div style={{ padding: 20 }}>
        {/* TAB 1: TỔNG QUAN */}
        {tab === 'tong-quan' && (
          <>
            <div className="stats-grid" style={{ marginBottom: 20 }}>
              <div className="stat-card">
                <div className="stat-label">Mã lớp</div>
                <div style={{
                  fontSize: 22,
                  fontWeight: 600,
                  fontFamily: 'monospace',
                  letterSpacing: 2,
                  color: 'var(--primary)'
                }}>
                  {lop.maLop}
                </div>
              </div>

              {/* SĨ SỐ */}
              <div className="stat-card">
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: 10
                }}>
                  <div className="stat-label">Sĩ số lớp học</div>

                  {lop.trangThai !== 'DA_DONG' && !dangSuaSiSo && (
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => {
                        setSiSoMoi(lop.siSo ?? '');
                        setLoiSiSo('');
                        setDangSuaSiSo(true);
                      }}
                    >
                      <i className="ti ti-edit" aria-hidden="true"></i> Chỉnh sửa
                    </button>
                  )}
                </div>

                {!dangSuaSiSo ? (
                  <>
                    <div
                      className="stat-value"
                      style={{ cursor: 'pointer' }}
                      onClick={() => setTab('thanh-vien')}
                    >
                      {thanhVien.length}/{lop.siSo || '∞'}
                    </div>

                    {lop.trangThai === 'HOAT_DONG' && (
                      <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 5 }}>
                        Lớp đang hoạt động: chỉ được tăng sĩ số
                      </div>
                    )}

                    {lop.trangThai === 'SAP_DIEN_RA' && (
                      <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 5 }}>
                        Có thể tăng/giảm, tối thiểu {thanhVien.length} học viên
                      </div>
                    )}
                  </>
                ) : (
                  <div style={{ marginTop: 10 }}>
                    <input
                      className="form-input"
                      type="number"
                      min={
                        lop.trangThai === 'SAP_DIEN_RA'
                          ? Math.max(1, thanhVien.length)
                          : Math.max(1, lop.siSo || 1)
                      }
                      step="1"
                      value={siSoMoi}
                      onChange={e => setSiSoMoi(e.target.value)}
                      disabled={dangLuuSiSo}
                    />

                    <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 5 }}>
                      {lop.trangThai === 'HOAT_DONG'
                        ? `Chỉ được tăng từ ${lop.siSo} trở lên.`
                        : `Không được thấp hơn ${thanhVien.length} học viên đang ghi danh.`}
                    </div>

                    {loiSiSo && (
                      <div className="alert alert-danger" style={{ marginTop: 8, fontSize: 12 }}>
                        {loiSiSo}
                      </div>
                    )}

                    <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
                      <button
                        type="button"
                        className="btn btn-primary btn-sm"
                        onClick={xuLyCapNhatSiSo}
                        disabled={dangLuuSiSo}
                      >
                        {dangLuuSiSo ? 'Đang lưu...' : 'Lưu'}
                      </button>

                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        onClick={huySuaSiSo}
                        disabled={dangLuuSiSo}
                      >
                        Hủy
                      </button>
                    </div>
                  </div>
                )}
              </div>

              <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => setTab('danh-gia')}>
                <div className="stat-label">Đánh giá TB</div>
                <div className="stat-value">
                  {lop.diemTrungBinh ? `${lop.diemTrungBinh.toFixed(1)} ★` : '—'}
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-label">Học phí lớp</div>
                <div className="stat-value" style={{ fontSize: 16 }}>
                  {Number(lop.phiLopHoc) === 0
                    ? 'Miễn phí'
                    : `${Number(lop.phiLopHoc).toLocaleString('vi-VN')} đ`}
                </div>
              </div>
            </div>

            <div className="grid-2" style={{ marginBottom: 20 }}>
              <div
                className="card"
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/lop-hoc/${maLopHoc}/giao-trinh-lai`)}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <i className="ti ti-list-details" style={{ fontSize: 28, color: 'var(--primary)' }} aria-hidden="true"></i>
                  <div>
                    <div style={{ fontWeight: 500 }}>Giáo trình lai</div>
                    <div style={{ fontSize: 12, color: 'var(--text-2)' }}>
                      Ẩn/hiện bài gốc, thêm bài bổ sung
                    </div>
                  </div>
                  <i className="ti ti-chevron-right" style={{ marginLeft: 'auto', color: 'var(--text-3)' }} aria-hidden="true"></i>
                </div>
              </div>

              <div className="card" style={{ cursor: 'pointer' }} onClick={() => setTab('thanh-vien')}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <i className="ti ti-users" style={{ fontSize: 28, color: 'var(--primary)' }} aria-hidden="true"></i>
                  <div>
                    <div style={{ fontWeight: 500 }}>Danh sách học viên</div>
                    <div style={{ fontSize: 12, color: 'var(--text-2)' }}>
                      Xem toàn bộ học viên và tiến độ học tập
                    </div>
                  </div>
                  <i className="ti ti-chevron-right" style={{ marginLeft: 'auto', color: 'var(--text-3)' }} aria-hidden="true"></i>
                </div>
              </div>

              <div
                className="card"
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/lop-hoc/${maLopHoc}/moi-hoc-vien`)}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <i className="ti ti-mail" style={{ fontSize: 28, color: 'var(--primary)' }} aria-hidden="true"></i>
                  <div>
                    <div style={{ fontWeight: 500 }}>Mời học viên</div>
                    <div style={{ fontSize: 12, color: 'var(--text-2)' }}>
                      Gửi lời mời qua email
                    </div>
                  </div>
                  <i className="ti ti-chevron-right" style={{ marginLeft: 'auto', color: 'var(--text-3)' }} aria-hidden="true"></i>
                </div>
              </div>

              <div
                className="card"
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/lop-hoc/${maLopHoc}/dien-dan`)}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <i className="ti ti-messages" style={{ fontSize: 28, color: 'var(--primary)' }} aria-hidden="true"></i>
                  <div>
                    <div style={{ fontWeight: 500 }}>Diễn đàn lớp học</div>
                    <div style={{ fontSize: 12, color: 'var(--text-2)' }}>
                      Đăng thông báo, trả lời hỏi đáp của học viên
                    </div>
                  </div>
                  <i className="ti ti-chevron-right" style={{ marginLeft: 'auto', color: 'var(--text-3)' }} aria-hidden="true"></i>
                </div>
              </div>
            </div>

            <div className="card">
              <div className="card-title">Thông tin lớp học</div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '10px 20px',
                fontSize: 13
              }}>
                <div>
                  <span style={{ color: 'var(--text-3)' }}>Khóa học gốc:</span>{' '}
                  {lop.tenKhoaHoc || '—'}
                </div>

                <div>
                  <span style={{ color: 'var(--text-3)' }}>Giảng viên:</span>{' '}
                  {lop.tenGiangVien || '—'}
                </div>

                <div>
                  <span style={{ color: 'var(--text-3)' }}>Khai giảng:</span>{' '}
                  {lop.ngayBatDau
                    ? new Date(lop.ngayBatDau).toLocaleDateString('vi-VN')
                    : '—'}
                </div>

                <div>
                  <span style={{ color: 'var(--text-3)' }}>Kết thúc:</span>{' '}
                  {lop.ngayKetThuc
                    ? new Date(lop.ngayKetThuc).toLocaleDateString('vi-VN')
                    : 'Không xác định'}
                </div>

                <div>
                  <span style={{ color: 'var(--text-3)' }}>Chế độ:</span>{' '}
                  {lop.congKhai ? 'Công khai' : 'Riêng tư'}
                </div>

                <div>
                  <span style={{ color: 'var(--text-3)' }}>% hoàn thành tối thiểu:</span>{' '}
                  {lop.tiLeHoanThanh}%
                </div>
              </div>
            </div>
          </>
        )}

        {/* TAB 2: THÀNH VIÊN */}
        {tab === 'thanh-vien' && (
          <div className="card" style={{ padding: 0 }}>
            <div className="card-title" style={{ padding: '16px 16px 0 16px' }}>
              Danh sách học viên
            </div>

            {thanhVien.length === 0 ? (
              <div className="empty-state">Chưa có thành viên nào.</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Học viên</th>
                    <th>Ngày tham gia</th>
                    <th>Tiến độ</th>
                  </tr>
                </thead>

                <tbody>
                  {thanhVien.map(tv => (
                    <tr key={tv.maHocVien}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                          <div
                            className="avatar"
                            style={{
                              width: 30,
                              height: 30,
                              fontSize: 12.5,
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              backgroundColor: 'var(--primary-light)',
                              color: 'var(--primary)',
                              borderRadius: '50%',
                              fontWeight: 'bold',
                              flexShrink: 0
                            }}
                          >
                            {(tv.tenHocVien || '?').charAt(0).toUpperCase()}
                          </div>

                          <div>
                            <div style={{ fontSize: 13, fontWeight: 500 }}>
                              {tv.tenHocVien || 'Học viên'}
                            </div>

                            {tv.emailHocVien && (
                              <div style={{ fontSize: 11.5, color: 'var(--text-3)' }}>
                                {tv.emailHocVien}
                              </div>
                            )}
                          </div>
                        </div>
                      </td>

                      <td>
                        {tv.ngayThamGia
                          ? new Date(tv.ngayThamGia).toLocaleDateString('vi-VN')
                          : '—'}
                      </td>

                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                          <div className="progress-bar" style={{ width: 100 }}>
                            <div
                              className="progress-fill"
                              style={{ width: `${tv.tienDoPhanTram || 0}%` }}
                            />
                          </div>

                          <span style={{ fontSize: 11.5, color: 'var(--text-3)' }}>
                            {tv.tienDoPhanTram || 0}%
                          </span>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* TAB 3: ĐÁNH GIÁ */}
        {tab === 'danh-gia' && (
          <div className="card">
            <div className="card-title" style={{ marginBottom: 15 }}>
              Đánh giá lớp học

              {lop.diemTrungBinh != null && (
                <span style={{
                  fontWeight: 400,
                  fontSize: 13,
                  color: 'var(--text-3)',
                  marginLeft: 10
                }}>
                  Trung bình {lop.diemTrungBinh.toFixed(1)} ★ (
                  {lop.soLuongDanhGia || dsDanhGia.length} đánh giá)
                </span>
              )}
            </div>

            {dangTaiDanhGia ? (
              <p style={{ fontSize: 13, color: 'var(--text-3)' }}>
                Đang tải...
              </p>
            ) : dsDanhGia.length === 0 ? (
              <div className="empty-state">
                Chưa có học viên nào đánh giá lớp học này.
              </div>
            ) : (
              dsDanhGia.map(dg => (
                <div
                  key={dg.maDanhGia}
                  style={{
                    display: 'flex',
                    gap: 12,
                    padding: '12px 0',
                    borderBottom: '1px solid var(--border)'
                  }}
                >
                  <div
                    className="avatar"
                    style={{
                      width: 32,
                      height: 32,
                      fontSize: 13,
                      flexShrink: 0,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      backgroundColor: 'var(--primary-light)',
                      color: 'var(--primary)',
                      borderRadius: '50%',
                      fontWeight: 'bold'
                    }}
                  >
                    {(dg.tenHocVien || '?').charAt(0).toUpperCase()}
                  </div>

                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <strong style={{ fontSize: 13 }}>
                        {dg.tenHocVien || 'Học viên'}
                      </strong>

                      <span style={{ color: 'var(--warning, #f5a623)', fontSize: 12 }}>
                        {'★'.repeat(dg.soSao)}
                        {'☆'.repeat(5 - dg.soSao)}
                      </span>
                    </div>

                    {dg.nhanXet && (
                      <p style={{
                        fontSize: 13,
                        color: 'var(--text-2)',
                        marginTop: 4
                      }}>
                        {dg.nhanXet}
                      </p>
                    )}

                    <div style={{
                      fontSize: 11,
                      color: 'var(--text-3)',
                      marginTop: 4
                    }}>
                      {dg.ngayTao
                        ? new Date(dg.ngayTao).toLocaleDateString('vi-VN')
                        : ''}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
}