import { useEffect, useState } from 'react';
import { layThongBaoLoi } from '../../../utils/layThongBaoLoi';

import {
  layDanhSachHoanTienKhoaHoc,
  xuLyHoanTienKhoaHoc,
  layDanhSachHoanTienLopHoc,
  xuLyHoanTienLopHoc,
} from '../../../services/taiChinhService';

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

function dichTrangThaiHoanTien(trangThai) {
  const banDo = {
    CHO_XAC_NHAN: 'Chờ xử lý',
    HOAN_TAT: 'Đã hoàn tiền',
    TU_CHOI: 'Đã từ chối',
  };

  return banDo[trangThai] || trangThai || 'Không rõ';
}

function layMauTrangThaiHoanTien(trangThai) {
  switch (trangThai) {
    case 'HOAN_TAT':
      return 'green';

    case 'CHO_XAC_NHAN':
      return 'amber';

    case 'TU_CHOI':
      return 'red';

    default:
      return 'gray';
  }
}

export default function TabHoanTien({ onThayDoi }) {
  const [loaiGiaoDich, setLoaiGiaoDich] =
    useState('KHOA_HOC');

  const [trangThaiLoc, setTrangThaiLoc] =
    useState('CHO_XAC_NHAN');

  const [trang, setTrang] = useState(0);

  const [danhSach, setDanhSach] = useState(null);

  const [dangTai, setDangTai] = useState(true);

  const [loi, setLoi] = useState('');

  useEffect(() => {
    setTrang(0);
  }, [loaiGiaoDich, trangThaiLoc]);

  async function taiDanhSach() {
    setDangTai(true);
    setLoi('');

    try {
      const lay =
        loaiGiaoDich === 'KHOA_HOC'
          ? layDanhSachHoanTienKhoaHoc
          : layDanhSachHoanTienLopHoc;

      let d = await lay({
        trangThai: trangThaiLoc,
        page: trang,
        size: 10,
      });

      // Tự động chuyển sang tab lớp học nếu không có dữ liệu khóa học
      if (
        loaiGiaoDich === 'KHOA_HOC' &&
        trangThaiLoc === 'CHO_XAC_NHAN' &&
        trang === 0 &&
        (!d?.content || d.content.length === 0)
      ) {
        const dLopHoc = await layDanhSachHoanTienLopHoc({
          trangThai: trangThaiLoc,
          page: 0,
          size: 10,
        });

        if ((dLopHoc?.content?.length || 0) > 0) {
          setLoaiGiaoDich('LOP_HOC');
          d = dLopHoc;
        }
      }

      setDanhSach(d);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangTai(false);
    }
  }

  useEffect(() => {
    taiDanhSach();
  }, [loaiGiaoDich, trangThaiLoc, trang]);

  async function xuLy(yc, trangThaiMoi) {
    let ghiChu = '';

    if (trangThaiMoi === 'TU_CHOI') {
      const nhap = window.prompt(
        'Lý do từ chối (tuỳ chọn):',
        '',
      );

      if (nhap === null) return;

      ghiChu = nhap.trim();
    } else {
      if (
        !window.confirm(
          `Xác nhận HOÀN TIỀN ${dinhDangTien(
            yc.soTien,
          )} cho "${
            yc.tenNguoiDung ||
            'người dùng #' + yc.maNguoiDung
          }"?\n\nHành động này sẽ thu hồi quyền truy cập tương ứng và không thể hoàn tác.`,
        )
      ) {
        return;
      }
    }

    try {
      const xuLyFn =
        loaiGiaoDich === 'KHOA_HOC'
          ? xuLyHoanTienKhoaHoc
          : xuLyHoanTienLopHoc;

      await xuLyFn(yc.maHoanTien, {
        trangThai: trangThaiMoi,
        ghiChu,
      });

      await taiDanhSach();

      onThayDoi?.();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    }
  }

  const tongSoTrang = danhSach?.totalPages || 0;

  return (
    <div className="card tc-card">
      <div className="tc-card-header">
        <div>
          <div className="tc-card-title">
            <i
              className="ti ti-receipt-refund"
              aria-hidden="true"
            />
            Yêu cầu hoàn tiền
          </div>

          <div className="tc-card-subtitle">
            Xem và xử lý các yêu cầu hoàn tiền từ học viên
          </div>
        </div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}

      {/* ===== Bộ lọc ===== */}
      <div className="tc-filter-grid">
        <div className="tc-filter-group">
          <label>Loại giao dịch</label>

          <select
            className="form-input"
            value={loaiGiaoDich}
            onChange={(e) =>
              setLoaiGiaoDich(e.target.value)
            }
          >
            <option value="KHOA_HOC">Khóa học</option>
            <option value="LOP_HOC">Lớp học</option>
          </select>
        </div>

        <div className="tc-filter-group">
          <label>Trạng thái</label>

          <select
            className="form-input"
            value={trangThaiLoc}
            onChange={(e) =>
              setTrangThaiLoc(e.target.value)
            }
          >
            <option value="">Tất cả</option>
            <option value="CHO_XAC_NHAN">
              Chờ xử lý
            </option>
            <option value="HOAN_TAT">
              Đã hoàn tiền
            </option>
            <option value="TU_CHOI">
              Đã từ chối
            </option>
          </select>
        </div>
      </div>

      {/* ===== Bảng dữ liệu ===== */}
      <div className="tc-table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Mã YC</th>
              <th>Người yêu cầu</th>
              <th>Đối tượng</th>
              <th>Số tiền</th>
              <th>Lý do</th>
              <th>Trạng thái</th>
              <th>Ngày yêu cầu</th>
              <th>Hành động</th>
            </tr>
          </thead>

          <tbody>
            {!dangTai && danhSach?.content?.length === 0 && (
              <tr>
                <td colSpan={8} className="tc-empty">
                  Không có yêu cầu nào
                </td>
              </tr>
            )}

            {danhSach?.content?.map((yc) => (
              <tr key={yc.maHoanTien}>
                <td style={{ fontWeight: 700 }}>
                  #{yc.maHoanTien}
                </td>

                <td>
                  <div style={{ fontWeight: 600 }}>
                    {yc.tenNguoiDung ||
                      `Người dùng #${yc.maNguoiDung ?? 'N/A'}`}
                  </div>

                  <div
                    style={{
                      fontSize: 12,
                      color: 'var(--text-3)',
                    }}
                  >
                    {yc.emailNguoiDung || ''}
                  </div>
                </td>

                <td>
                  <div>{yc.tieuDeDoiTuong}</div>

                  <div
                    style={{
                      fontSize: 11,
                      color: 'var(--text-3)',
                    }}
                  >
                    {yc.maGiaoDichVnpay}
                  </div>
                </td>

                <td style={{ fontWeight: 700 }}>
                  {dinhDangTien(yc.soTien)}
                </td>

                <td
                  style={{
                    maxWidth: 240,
                    fontSize: 12.5,
                    color: 'var(--text-2)',
                    lineHeight: 1.5,
                  }}
                >
                  {yc.lyDo}
                </td>

                <td>
                  <span
                    className={`badge badge-${layMauTrangThaiHoanTien(
                      yc.trangThai,
                    )}`}
                  >
                    {dichTrangThaiHoanTien(yc.trangThai)}
                  </span>
                </td>

                <td
                  style={{
                    fontSize: 12.5,
                    color: 'var(--text-3)',
                  }}
                >
                  {formatNgay(yc.ngayTao)}
                </td>

                <td>
                  {yc.trangThai === 'CHO_XAC_NHAN' ? (
                    <div
                      style={{
                        display: 'flex',
                        gap: 8,
                        flexWrap: 'wrap',
                      }}
                    >
                      <button
                        className="btn btn-success btn-sm"
                        onClick={() =>
                          xuLy(yc, 'HOAN_TAT')
                        }
                      >
                        Duyệt hoàn tiền
                      </button>

                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() =>
                          xuLy(yc, 'TU_CHOI')
                        }
                      >
                        Từ chối
                      </button>
                    </div>
                  ) : (
                    <span
                      style={{
                        fontSize: 12,
                        color: 'var(--text-3)',
                      }}
                    >
                      {yc.ngayXuLy
                        ? formatNgay(yc.ngayXuLy)
                        : '—'}
                    </span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* ===== Phân trang ===== */}
      {tongSoTrang > 1 && (
        <div className="tc-pagination">
          <div className="tc-pagination-info">
            Trang {trang + 1} / {tongSoTrang}
          </div>

          <div className="tc-pagination-actions">
            <button
              className="btn btn-secondary btn-sm"
              disabled={trang <= 0}
              onClick={() =>
                setTrang((t) => Math.max(0, t - 1))
              }
            >
              Trước
            </button>

            <button
              className="btn btn-secondary btn-sm"
              disabled={trang >= tongSoTrang - 1}
              onClick={() =>
                setTrang((t) =>
                  Math.min(tongSoTrang - 1, t + 1),
                )
              }
            >
              Sau
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
