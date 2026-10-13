import { useEffect, useState, useCallback } from 'react';
import { xemNhatKy } from '../../services/adminService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';

// Tô màu badge theo mức độ nhạy cảm của hành động — chỉ để dễ quét mắt,
// không ảnh hưởng logic. Hành động lạ chưa có trong map vẫn hiển thị được
// (rơi vào nhánh mặc định 'badge-gray'), vì đây chỉ là gợi ý UI.
const MAU_HANH_DONG = {
  KHOA_TAI_KHOAN: 'badge-red',
  THU_HOI_QUYEN_QUAN_TRI: 'badge-red',
  MO_KHOA_TAI_KHOAN: 'badge-green',
  TAO_TAI_KHOAN_ADMIN: 'badge-blue',
  CAP_NHAT_CAU_HINH_HE_THONG: 'badge-amber',
};

function ngayHomNayISO() {
  const d = new Date();
  const nam = d.getFullYear();
  const thang = String(d.getMonth() + 1).padStart(2, '0');
  const ngay = String(d.getDate()).padStart(2, '0');
  return `${nam}-${thang}-${ngay}`; // yyyy-MM-dd theo giờ địa phương
}

export default function NhatKyHeThong() {
  const [ngay, setNgay] = useState(ngayHomNayISO());
  const [soDong, setSoDong] = useState(200);
  const [oTuKhoa, setOTuKhoa] = useState('');
  const [tuKhoa, setTuKhoa] = useState('');
  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  const taiLai = useCallback(() => {
    setDangTai(true);
    setLoi('');
    xemNhatKy({ ngay, soDong, tuKhoa })
      .then(setKetQua)
      .catch((err) => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, [ngay, soDong, tuKhoa]);

  useEffect(() => {
    taiLai();
  }, [taiLai]);

  function xuLyTimKiem(e) {
    e.preventDefault();
    setTuKhoa(oTuKhoa.trim());
  }

  const { navItems, roleLabel } = layNavTheoVaiTro();
  const danhSach = ketQua?.danhSach ?? [];
  // Hiển thị mới nhất lên đầu — backend trả về theo thứ tự ghi trong file (cũ → mới)
  const danhSachHienThi = [...danhSach].reverse();

  return (
    <DashboardShell
      roleLabel={roleLabel}
      navItems={navItems}
      crumb="Nhật ký hệ thống"
      actions={<span className="badge badge-amber">{ketQua?.soDongTraVe ?? 0} dòng</span>}
    >
      <div className="ds-page-head">
        <div>
          <h1>Nhật ký hệ thống</h1>
          <p>Nhật ký hành động quản trị (khóa/mở khóa tài khoản, cấu hình hệ thống...) — chỉ QTV cấp cao xem được.</p>
        </div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}

      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'flex-end', marginBottom: 14 }}>
        <div>
          <label className="form-label" style={{ display: 'block', fontSize: 12 }}>Ngày</label>
          <input
            type="date"
            className="form-input"
            value={ngay}
            max={ngayHomNayISO()}
            onChange={(e) => setNgay(e.target.value)}
          />
        </div>

        <div>
          <label className="form-label" style={{ display: 'block', fontSize: 12 }}>Số dòng gần nhất</label>
          <select className="form-input" value={soDong} onChange={(e) => setSoDong(Number(e.target.value))}>
            {[50, 100, 200, 500, 1000].map((n) => (
              <option key={n} value={n}>{n}</option>
            ))}
          </select>
        </div>

        <form onSubmit={xuLyTimKiem} style={{ display: 'flex', gap: 6, flex: 1, minWidth: 240 }}>
          <input
            className="form-input"
            style={{ flex: 1 }}
            placeholder="Lọc theo từ khóa — vd: KHOA_TAI_KHOAN, nguoi_dung#42..."
            value={oTuKhoa}
            onChange={(e) => setOTuKhoa(e.target.value)}
          />
          <button type="submit" className="btn btn-secondary btn-sm">
            <i className="ti ti-search" aria-hidden="true"></i>
          </button>
        </form>

        <button className="btn btn-secondary btn-sm" onClick={taiLai} disabled={dangTai}>
          <i className="ti ti-refresh" aria-hidden="true"></i> Tải lại
        </button>
      </div>

      {dangTai ? (
        <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
      ) : ketQua && !ketQua.coFileLog ? (
        <div className="empty-state">
          <i className="ti ti-file-off" style={{ fontSize: 32 }} aria-hidden="true"></i>
          <p style={{ marginTop: 8 }}>Không có file nhật ký cho ngày {ngay} (có thể ngoài phạm vi 90 ngày lưu trữ).</p>
        </div>
      ) : danhSachHienThi.length === 0 ? (
        <div className="empty-state">
          <i className="ti ti-list-search" style={{ fontSize: 32 }} aria-hidden="true"></i>
          <p style={{ marginTop: 8 }}>Không có dòng nhật ký nào khớp bộ lọc hiện tại.</p>
        </div>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: 180 }}>Thời gian</th>
              <th style={{ width: 140 }}>Người thực hiện</th>
              <th style={{ width: 200 }}>Hành động</th>
              <th style={{ width: 140 }}>Đối tượng</th>
              <th>Chi tiết</th>
            </tr>
          </thead>
          <tbody>
            {danhSachHienThi.map((m, i) => (
              <tr key={i}>
                <td style={{ whiteSpace: 'nowrap', fontVariantNumeric: 'tabular-nums' }}>
                  {m.thoiGian ? m.thoiGian.replace('T', ' ').slice(0, 19) : '—'}
                </td>
                <td>{m.nguoiThucHien || '—'}</td>
                <td>
                  {m.hanhDong
                    ? <span className={`badge ${MAU_HANH_DONG[m.hanhDong] || 'badge-gray'}`}>{m.hanhDong}</span>
                    : '—'}
                </td>
                <td>{m.doiTuong || '—'}</td>
                <td style={{ color: 'var(--text-2)' }}>{m.chiTiet}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </DashboardShell>
  );
}