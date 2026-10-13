import { useEffect, useState } from 'react';
import { layThongBaoLoi } from '../../../utils/layThongBaoLoi';

import {
  layGiaoDichDoanhThuHeThong,
  layTongQuanDoanhThuHeThong,
  layXuHuongDoanhThuHeThong,
  taiXuongCsvDoanhThuHeThong,
} from '../../../services/taiChinhService';

import BieuDoDoanhThu from '../../../components/BieuDoDoanhThu';
import TongQuanDoanhThu from '../../../components/TongQuanDoanhThu';

const dinhDangTien = (n) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(n || 0);

function formatNgay(value) {
  if (!value) return 'N/A';

  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value));
}

export default function TabDoanhThuHeThong() {
  const [tuNgayLoc, setTuNgayLoc] = useState('');
  const [denNgayLoc, setDenNgayLoc] = useState('');

  const [maKhoaHocLoc, setMaKhoaHocLoc] = useState('');
  const [maGiangVienLoc, setMaGiangVienLoc] = useState('');

  const [xuHuong, setXuHuong] = useState(null);
  const [giaoDich, setGiaoDich] = useState(null);

  const [dangTai, setDangTai] = useState(true);
  const [dangXuatCsv, setDangXuatCsv] = useState(false);

  const [loi, setLoi] = useState('');

  useEffect(() => {
    let daHuy = false;

    setDangTai(true);

    const boLoc = {
      tuNgay: tuNgayLoc || undefined,
      denNgay: denNgayLoc || undefined,
      maKhoaHoc: maKhoaHocLoc || undefined,
      maGiangVien: maGiangVienLoc || undefined,
    };

    Promise.all([
      layXuHuongDoanhThuHeThong(boLoc),
      layGiaoDichDoanhThuHeThong(boLoc),
    ])
      .then(([xh, gd]) => {
        if (daHuy) return;

        setXuHuong(xh);
        setGiaoDich(gd);
      })
      .catch((err) => {
        if (!daHuy) {
          setLoi(layThongBaoLoi(err));
        }
      })
      .finally(() => {
        if (!daHuy) {
          setDangTai(false);
        }
      });

    return () => {
      daHuy = true;
    };
  }, [tuNgayLoc, denNgayLoc, maKhoaHocLoc, maGiangVienLoc]);

  async function xuLyXuatCsv() {
    setDangXuatCsv(true);

    try {
      await taiXuongCsvDoanhThuHeThong({
        tuNgay: tuNgayLoc || undefined,
        denNgay: denNgayLoc || undefined,
        maKhoaHoc: maKhoaHocLoc || undefined,
        maGiangVien: maGiangVienLoc || undefined,
      });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuatCsv(false);
    }
  }

  return (
    <>
      {/* ===== Tổng quan doanh thu ===== */}
      <div className="card tc-card" style={{ marginBottom: 20 }}>
        <div className="tc-card-header">
          <div>
            <div className="tc-card-title">
              <i
                className="ti ti-layout-dashboard"
                aria-hidden="true"
              />
              Tổng quan doanh thu hệ thống
            </div>

            <div className="tc-card-subtitle">
              Theo dõi doanh thu theo thời gian và các giao dịch phát sinh
            </div>
          </div>
        </div>

        <TongQuanDoanhThu
          layTongQuan={layTongQuanDoanhThuHeThong}
          extraParams={{}}
        />
      </div>

      {/* ===== Chi tiết doanh thu ===== */}
      <div className="card tc-card">
        <div className="tc-card-header">
          <div>
            <div className="tc-card-title">
              <i className="ti ti-chart-bar" aria-hidden="true" />
              Báo cáo doanh thu
            </div>

            <div className="tc-card-subtitle">
              Lọc theo thời gian, khóa học hoặc giảng viên để xem chi tiết
            </div>
          </div>

          <button
            className="btn btn-secondary btn-sm"
            disabled={dangXuatCsv}
            onClick={xuLyXuatCsv}
          >
            <i
              className="ti ti-file-spreadsheet"
              aria-hidden="true"
            />

            {dangXuatCsv ? 'Đang xuất...' : 'Xuất CSV'}
          </button>
        </div>

        {loi && <div className="alert alert-danger">{loi}</div>}

        {/* ===== Bộ lọc ===== */}
        <div className="tc-filter-grid">
          <div className="tc-filter-group">
            <label>Từ ngày</label>

            <input
              type="date"
              className="form-input"
              value={tuNgayLoc}
              onChange={(e) => setTuNgayLoc(e.target.value)}
            />
          </div>

          <div className="tc-filter-group">
            <label>Đến ngày</label>

            <input
              type="date"
              className="form-input"
              value={denNgayLoc}
              onChange={(e) => setDenNgayLoc(e.target.value)}
            />
          </div>

          <div className="tc-filter-group">
            <label>Mã khóa học</label>

            <input
              type="number"
              className="form-input"
              placeholder="Tất cả khóa học"
              value={maKhoaHocLoc}
              onChange={(e) => setMaKhoaHocLoc(e.target.value)}
            />
          </div>

          <div className="tc-filter-group">
            <label>Mã giảng viên</label>

            <input
              type="number"
              className="form-input"
              placeholder="Tất cả giảng viên"
              value={maGiangVienLoc}
              onChange={(e) => setMaGiangVienLoc(e.target.value)}
            />
          </div>
        </div>

        {dangTai ? (
          <p className="tc-loading">Đang tải dữ liệu...</p>
        ) : (
          <>
            {/* ===== Biểu đồ ===== */}
            <BieuDoDoanhThu diem={xuHuong?.diem} />

            {/* ===== Bảng giao dịch ===== */}
            <div className="tc-overview-title" style={{ marginTop: 22 }}>
              Chi tiết giao dịch ({giaoDich?.length || 0})
            </div>

            <div className="tc-table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Ngày</th>
                    <th>Loại</th>
                    <th>Khóa học</th>
                    <th>Lớp</th>
                    <th>Giảng viên</th>
                    <th>Số tiền</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>

                <tbody>
                  {(!giaoDich || giaoDich.length === 0) && (
                    <tr>
                      <td colSpan={7} className="tc-empty">
                        Không có giao dịch nào khớp bộ lọc
                      </td>
                    </tr>
                  )}

                  {giaoDich?.map((gd) => (
                    <tr key={`${gd.loai}-${gd.maPhieu}`}>
                      <td
                        style={{
                          fontSize: 12.5,
                          color: 'var(--text-3)',
                        }}
                      >
                        {formatNgay(gd.ngayGiaoDich)}
                      </td>

                      <td>
                        {gd.loai === 'KHOA_HOC'
                          ? 'Mua khóa học'
                          : 'Phí lớp học'}
                      </td>

                      <td>{gd.tenKhoaHoc}</td>

                      <td>{gd.maLop || '—'}</td>

                      <td>{gd.tenGiangVien || '—'}</td>

                      <td style={{ fontWeight: 700 }}>
                        {dinhDangTien(gd.soTien)}
                      </td>

                      <td>
                        <span
                          className={`badge ${
                            gd.trangThai === 'THANH_CONG'
                              ? 'badge-green'
                              : 'badge-red'
                          }`}
                        >
                          {gd.trangThai}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>
    </>
  );
}