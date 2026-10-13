import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import { layTongQuanTaiChinh } from '../../services/taiChinhService';

import DashboardShell, {
  layNavTheoVaiTro,
} from '../../components/DashboardShell';

import TabDoiSoat from './tai-chinh/TabDoiSoat';
import TabHoanTien from './tai-chinh/TabHoanTien';
import TabDoanhThuHeThong from './tai-chinh/TabDoanhThuHeThong';

import './BangDieuKhienTaiChinh.css';

const dinhDangTien = (n) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(n || 0);

export default function BangDieuKhienTaiChinh() {
  // FIX: đọc tab ban đầu từ query string (?tab=...) — cho phép trang Tổng
  // quan điều hướng thẳng tới đúng tab (đối soát/hoàn tiền/doanh thu).
  const [searchParams] = useSearchParams();
  const [tab, setTab] = useState(searchParams.get('tab') || 'doi-soat');

  const [tongQuan, setTongQuan] = useState(null);
  const [dangTaiTongQuan, setDangTaiTongQuan] = useState(true);
  const [loi, setLoi] = useState('');

  async function taiTongQuan() {
    try {
      setDangTaiTongQuan(true);
      setTongQuan(await layTongQuanTaiChinh());
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangTaiTongQuan(false);
    }
  }

  useEffect(() => {
    taiTongQuan();
  }, []);

  const { navItems, roleLabel } = layNavTheoVaiTro();

  return (
    <DashboardShell
      roleLabel={roleLabel}
      navItems={navItems}
      crumb="Đối soát giải ngân"
    >
      <div className="ds-page-head">
        <div>
          <h1>Đối soát giải ngân</h1>
          <p>
            Doanh thu, đối soát cho giảng viên và yêu cầu hoàn tiền
          </p>
        </div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}

      {dangTaiTongQuan && (
        <p className="tc-loading">Đang tải dữ liệu...</p>
      )}

      {tongQuan && (
        <>
          {/* ===== GIAO DỊCH ===== */}
          <div className="tc-overview-group">
            <div className="tc-overview-title">Giao dịch</div>

            <div className="stats-grid tc-overview-grid">
              <div className="stat-card">
                <div className="stat-label">
                  Giao dịch thành công
                </div>

                <div className="stat-value tc-text-success">
                  {tongQuan.soGiaoDichThanhCong}
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-label">
                  Giao dịch chờ xác nhận
                </div>

                <div className="stat-value tc-text-warning">
                  {tongQuan.soGiaoDichChoXacNhan}
                </div>
              </div>

              <div
                className={`stat-card ${
                  tongQuan.soPhieuChoXacNhan > 0
                    ? 'tc-stat-alert'
                    : ''
                }`}
              >
                <div className="stat-label">
                  Phiếu đối soát chờ xử lý
                </div>

                <div
                  className={`stat-value ${
                    tongQuan.soPhieuChoXacNhan > 0
                      ? 'tc-text-danger'
                      : ''
                  }`}
                >
                  {tongQuan.soPhieuChoXacNhan}
                </div>

                {tongQuan.soPhieuChoXacNhan > 0 && (
                  <div className="stat-change down">
                    Cần xử lý sớm
                  </div>
                )}
              </div>

              <div className="stat-card">
                <div className="stat-label">
                  Phiếu đang xử lý
                </div>

                <div className="stat-value tc-text-warning">
                  {tongQuan.soPhieuDangXuLy}
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-label">Phiếu hoàn tất</div>

                <div className="stat-value tc-text-success">
                  {tongQuan.soPhieuHoanTat}
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-label">Phiếu thất bại</div>

                <div className="stat-value tc-text-danger">
                  {tongQuan.soPhieuThatBai}
                </div>
              </div>

              <div
                className={`stat-card ${
                  tongQuan.soYeuCauHoanTienChoXuLy > 0
                    ? 'tc-stat-alert'
                    : ''
                }`}
              >
                <div className="stat-label">
                  Yêu cầu hoàn tiền chờ duyệt
                </div>

                <div
                  className={`stat-value ${
                    tongQuan.soYeuCauHoanTienChoXuLy > 0
                      ? 'tc-text-danger'
                      : ''
                  }`}
                >
                  {tongQuan.soYeuCauHoanTienChoXuLy}
                </div>

                {tongQuan.soYeuCauHoanTienChoXuLy > 0 && (
                  <div className="stat-change down">
                    Cần xử lý sớm
                  </div>
                )}
              </div>
            </div>
          </div>
        </>
      )}

      {/* ===== TAB BAR ===== */}
      <div className="tc-tabbar">
        <button
          className={`btn btn-sm ${
            tab === 'doi-soat'
              ? 'btn-primary'
              : 'btn-secondary'
          }`}
          onClick={() => setTab('doi-soat')}
        >
          <i className="ti ti-receipt-2" aria-hidden="true" />
          Đối soát giảng viên
        </button>

        <button
          className={`btn btn-sm ${
            tab === 'hoan-tien'
              ? 'btn-primary'
              : 'btn-secondary'
          }`}
          onClick={() => setTab('hoan-tien')}
        >
          <i
            className="ti ti-receipt-refund"
            aria-hidden="true"
          />
          Yêu cầu hoàn tiền

          {tongQuan?.soYeuCauHoanTienChoXuLy > 0 && (
            <span className="badge badge-red tc-tab-badge">
              {tongQuan.soYeuCauHoanTienChoXuLy}
            </span>
          )}
        </button>

        <button
          className={`btn btn-sm ${
            tab === 'doanh-thu'
              ? 'btn-primary'
              : 'btn-secondary'
          }`}
          onClick={() => setTab('doanh-thu')}
        >
          <i className="ti ti-chart-bar" aria-hidden="true" />
          Doanh thu
        </button>
      </div>

      {/* ===== TAB CONTENT ===== */}
      {tab === 'doi-soat' && (
        <TabDoiSoat onThayDoi={taiTongQuan} />
      )}

      {tab === 'hoan-tien' && (
        <TabHoanTien onThayDoi={taiTongQuan} />
      )}

      {tab === 'doanh-thu' && <TabDoanhThuHeThong />}
    </DashboardShell>
  );
}
