import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

import BangDieuKhienCaoCap from './admin/BangDieuKhienCaoCap';
import BangDieuKhienNoiDung from './admin/BangDieuKhienNoiDung';
import BangDieuKhienTaiChinh from './admin/BangDieuKhienTaiChinh';

import { layLopHocVien } from '../services/lopHocService';
import { layKhoaHocDaMua } from '../services/khoaHocService';
import { dangXuat as dangXuatApi } from '../services/xacThucService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';
import NutThongBao from '../components/NutThongBao';

import './TrangChu.css';

const NHAN_TRANG_THAI_LOP = {
  SAP_DIEN_RA: { text: 'Sắp diễn ra', mau: 'badge-amber' },
  HOAT_DONG: { text: 'Đang hoạt động', mau: 'badge-green' },
  DA_DONG: { text: 'Đã đóng', mau: 'badge-gray' },
};

export default function TrangChu() {
  const navigate = useNavigate();

  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userRole, setUserRole] = useState(null);
  const [userInfo, setUserInfo] = useState({
    hoTen: '',
    chuCaiDau: '',
  });

  const [dsLopHocVien, setDsLopHocVien] = useState([]);
  const [dangTaiLop, setDangTaiLop] = useState(true);
  const [loiLop, setLoiLop] = useState('');

  const [dsKhoaHocDaMua, setDsKhoaHocDaMua] = useState([]);
  const [dangTaiKhoaHoc, setDangTaiKhoaHoc] = useState(true);
  const [loiKhoaHoc, setLoiKhoaHoc] = useState('');

  useEffect(() => {
    // FIX (Vấn đề #2): "accessToken" không còn đọc được từ localStorage —
    // dùng cờ hiển thị "daDangNhap" thay thế (xem TuyenDuongRieng.jsx).
    const daDangNhap = localStorage.getItem('daDangNhap');
    const role = localStorage.getItem('vaiTro');
    const name = localStorage.getItem('hoTen') || 'Khách';

    if (daDangNhap) {
      setIsLoggedIn(true);
      setUserRole(role);
      setUserInfo({
        hoTen: name,
        chuCaiDau: name.charAt(0).toUpperCase(),
      });
    }
  }, []);

  const laAdmin =
    userRole === 'QTV_CAO_CAP' ||
    userRole === 'QTV_NOI_DUNG' ||
    userRole === 'QTV_TAI_CHINH';

  useEffect(() => {
    if (!isLoggedIn || laAdmin) {
      setDangTaiLop(false);
      setDangTaiKhoaHoc(false);
      return;
    }

    let daHuy = false;

    setDangTaiLop(true);
    setLoiLop('');

    layLopHocVien()
      .then((ds) => {
        if (!daHuy) setDsLopHocVien(ds || []);
      })
      .catch((err) => {
        if (!daHuy) setLoiLop(layThongBaoLoi(err));
      })
      .finally(() => {
        if (!daHuy) setDangTaiLop(false);
      });

    setDangTaiKhoaHoc(true);
    setLoiKhoaHoc('');

    layKhoaHocDaMua()
      .then((ds) => {
        if (!daHuy) setDsKhoaHocDaMua(ds || []);
      })
      .catch((err) => {
        if (!daHuy) setLoiKhoaHoc(layThongBaoLoi(err));
      })
      .finally(() => {
        if (!daHuy) setDangTaiKhoaHoc(false);
      });

    return () => {
      daHuy = true;
    };
  }, [isLoggedIn, laAdmin]);

  const handleDangXuat = async () => {
    // FIX (Vấn đề #2): trước đây chỉ localStorage.clear() — token nằm trong
    // JS-readable storage nên xoá ở client là đủ. Giờ token nằm trong cookie
    // HttpOnly, JS KHÔNG thể tự xoá được — bắt buộc phải gọi API để backend
    // trả về Set-Cookie xoá cookie (Max-Age=0) VÀ thu hồi refresh token
    // trong DB, nếu không phiên đăng nhập vẫn còn hiệu lực dù đã "đăng xuất".
    try {
      await dangXuatApi();
    } catch {
      // Kể cả khi gọi API thất bại (mất mạng, hết hạn sẵn...) vẫn tiếp tục
      // dọn phía client và điều hướng — không để người dùng kẹt ở trạng thái
      // bấm "Đăng xuất" mà không có phản hồi gì.
    }
    localStorage.clear();
    setIsLoggedIn(false);
    setUserRole(null);
    navigate('/dang-nhap');
  };

  // ================= ADMIN =================
  if (isLoggedIn) {
    if (userRole === 'QTV_CAO_CAP') return <BangDieuKhienCaoCap />;
    if (userRole === 'QTV_NOI_DUNG') return <BangDieuKhienNoiDung />;
    if (userRole === 'QTV_TAI_CHINH') return <BangDieuKhienTaiChinh />;
  }

  // ================= GUEST =================
  const renderGuestView = () => (
    <div className="guest-view">
      <div className="mp-hero">
        <h1>
          Học mọi lúc – Phát triển sự nghiệp cùng nền tảng E-Learning
        </h1>

        <p>
          Khám phá khóa học chất lượng, tham gia lớp học trực tuyến và nhận chứng chỉ hoàn thành ngay trên hệ thống.
        </p>

        <div className="mp-hero-actions">
          <button
            className="btn btn-accent"
            onClick={() => navigate('/dang-ky')}
          >
            Đăng ký ngay
          </button>

          <button
            className="btn btn-outline-light"
            onClick={() => navigate('/dang-nhap')}
          >
            Đăng nhập
          </button>

          <button
            className="btn btn-outline-light"
            onClick={() => navigate('/cho-khoa-hoc')}
          >
            Khám phá khóa học
          </button>

          <button
            className="btn btn-outline-light"
            onClick={() => navigate('/danh-sach-lop-hoc')}
          >
            Xem lớp học đang mở
          </button>
        </div>
      </div>

      <div className="grid-3">
        <div className="card">
          <h3 className="card-title">🎓 Học theo lộ trình</h3>
          <p className="card-sub">
            Theo dõi tiến độ học tập và hoàn thành từng bài học dễ dàng.
          </p>
        </div>

        <div className="card">
          <h3 className="card-title">👨‍🏫 Lớp học trực tuyến</h3>
          <p className="card-sub">
            Tham gia lớp học thời gian thực với giảng viên và học viên khác.
          </p>
        </div>

        <div className="card">
          <h3 className="card-title">🏆 Chứng chỉ hoàn thành</h3>
          <p className="card-sub">
            Nhận chứng chỉ hoặc giấy xác nhận khi hoàn thành khóa học/lớp học.
          </p>
        </div>
      </div>
    </div>
  );

  // ================= USER =================
  const renderUserView = () => (
    <div className="user-view">
      <div className="user-header">
        <div>
          <h2>Xin chào, {userInfo.hoTen}! 👋</h2>
          <p className="user-subtitle">
            Chúc bạn có một ngày học tập hiệu quả.
          </p>
        </div>

        <div className="user-actions">
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => navigate('/ho-so')}
          >
            <i className="ti ti-user-circle" />
            Hồ sơ
          </button>

          <NutThongBao />

          <button
            className="btn btn-secondary btn-sm"
            onClick={() => navigate('/thanh-toan/lich-su')}
          >
            <i className="ti ti-receipt" />
            Giao dịch
          </button>

          <button
            className="btn btn-secondary btn-sm"
            onClick={() => navigate('/chung-chi-cua-toi')}
          >
            <i className="ti ti-certificate" />
            Chứng chỉ
          </button>

          <button
            className="btn btn-accent btn-sm"
            onClick={handleDangXuat}
          >
            <i className="ti ti-logout" />
            Đăng xuất
          </button>
        </div>
      </div>

      {/* ===== QUICK ACTIONS ===== */}
      <div className="grid-3 tc-actions-grid">
        <div className="card tc-action-card">
          <div className="tc-action-icon">🛒</div>

          <h4 className="card-title">Chợ khóa học</h4>

          <p className="card-sub">
            Khám phá và mua các khóa học để bắt đầu học tập.
          </p>

          <button
            className="btn btn-primary tc-action-btn"
            onClick={() => navigate('/cho-khoa-hoc')}
          >
            Truy cập
          </button>
        </div>

        <div className="card tc-action-card">
          <div className="tc-action-icon">👨‍🏫</div>

          <h4 className="card-title">Không gian Giảng viên</h4>

          <p className="card-sub">
            Tạo môi trường học tập với các khóa học và lớp học.
          </p>

          <button
            className="btn btn-primary tc-action-btn"
            onClick={() => navigate('/giang-vien')}
          >
            Truy cập
          </button>
        </div>

        <div className="card tc-action-card">
          <div className="tc-action-icon">📚</div>

          <h4 className="card-title">Lớp học</h4>

          <p className="card-sub">
            Tham gia các lớp học sắp diễn ra hoặc đang hoạt động.
          </p>

          <button
            className="btn btn-primary tc-action-btn"
            onClick={() => navigate('/danh-sach-lop-hoc')}
          >
            Xem lớp mở
          </button>
        </div>
      </div>

      {/* ===== KHÓA HỌC ===== */}
      <section className="tc-section">
        <div className="section-title">
          <h3>Khóa học của tôi đã mua</h3>
          {dsKhoaHocDaMua.length > 0 && (
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => navigate('/khoa-hoc-cua-toi/da-mua')}
            >
              Xem tất cả ({dsKhoaHocDaMua.length}) khóa học
            </button>
          )}
        </div>

        {dangTaiKhoaHoc && (
          <div className="card empty-state">
            <p>Đang tải danh sách khóa học...</p>
          </div>
        )}

        {!dangTaiKhoaHoc && loiKhoaHoc && (
          <div className="alert alert-danger">{loiKhoaHoc}</div>
        )}

        {!dangTaiKhoaHoc &&
          !loiKhoaHoc &&
          dsKhoaHocDaMua.length === 0 && (
            <div className="card empty-state">
              <p>Bạn chưa mua khóa học nào. Khám phá ngay!</p>
              <button
                className="btn btn-primary"
                onClick={() => navigate('/cho-khoa-hoc')}
              >
                Xem khóa học
              </button>
            </div>
          )}

        {!dangTaiKhoaHoc &&
          !loiKhoaHoc &&
          dsKhoaHocDaMua.length > 0 && (
            <div className="tc-card-grid">
              {dsKhoaHocDaMua.map((kh) => (
                <div
                  key={kh.maKhoaHoc}
                  className="card sm tc-learn-card"
                  role="button"
                  tabIndex={0}
                  onClick={() =>
                    navigate(`/khoa-hoc-cua-toi/${kh.maKhoaHoc}/hoc`)
                  }
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      navigate(`/khoa-hoc-cua-toi/${kh.maKhoaHoc}/hoc`);
                    }
                  }}
                >
                  {kh.anhBia && (
                    <img
                      src={kh.anhBia}
                      alt={kh.tieuDe}
                      className="tc-learn-thumb"
                    />
                  )}

                  <div className="tc-learn-body">
                    <h4 className="tc-learn-title">{kh.tieuDe}</h4>

                    <p className="tc-learn-meta">
                      GV: {kh.tenTacGia || '—'} · {kh.soBaiHoc ?? 0} bài học
                    </p>

                    <button
                      className="btn btn-primary btn-sm"
                      onClick={(e) => {
                        e.stopPropagation();
                        navigate(`/khoa-hoc-cua-toi/${kh.maKhoaHoc}/hoc`);
                      }}
                    >
                      Vào học
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
      </section>

      {/* ===== LỚP HỌC ===== */}
      <section className="tc-section">
        <div className="section-title">
          <h3>Lớp học của tôi đã tham gia</h3>

          {dsLopHocVien.length > 0 && (
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => navigate('/lop-hoc-da-mua')}
            >
              Xem tất cả ({dsLopHocVien.length} lớp)
            </button>
          )}
        </div>

        {dangTaiLop && (
          <div className="card empty-state">
            <p>Đang tải danh sách lớp học...</p>
          </div>
        )}

        {!dangTaiLop && loiLop && (
          <div className="alert alert-danger">{loiLop}</div>
        )}

        {!dangTaiLop &&
          !loiLop &&
          dsLopHocVien.length === 0 && (
            <div className="card empty-state">
              <p>Bạn chưa tham gia lớp học nào.</p>
              <button
                className="btn btn-primary"
                onClick={() => navigate('/danh-sach-lop-hoc')}
              >
                Tham gia lớp học
              </button>
            </div>
          )}

        {!dangTaiLop &&
          !loiLop &&
          dsLopHocVien.length > 0 && (
            <div className="tc-card-grid">
              {dsLopHocVien.map((lop) => {
                const nhan =
                  NHAN_TRANG_THAI_LOP[lop.trangThai] || {
                    text: lop.trangThai,
                    mau: 'badge-gray',
                  };

                const phanTramHienThi =
                  typeof lop.tienDoPhanTramHocVien === 'number'
                    ? lop.tienDoPhanTramHocVien
                    : typeof lop.tiLeHoanThanh === 'number'
                      ? lop.tiLeHoanThanh
                      : 0;

                return (
                  <div
                    key={lop.maLopHoc}
                    className="card sm tc-learn-card"
                    role="button"
                    tabIndex={0}
                    onClick={() => navigate(`/lop-hoc/${lop.maLopHoc}`)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        navigate(`/lop-hoc/${lop.maLopHoc}`);
                      }
                    }}
                  >
                    {lop.anhBiaKhoaHoc && (
                      <img
                        src={lop.anhBiaKhoaHoc}
                        alt={lop.tenKhoaHoc}
                        className="tc-learn-thumb"
                      />
                    )}

                    <div className="tc-learn-body">
                      <div className="tc-card-top">
                        <h4 className="tc-learn-title">
                          {lop.tenKhoaHoc || 'Lớp học'}
                        </h4>

                        <span className={`badge ${nhan.mau}`}>
                          {nhan.text}
                        </span>
                      </div>

                      <p className="tc-learn-meta">
                        GV: {lop.tenGiangVien || '—'} · Mã: {lop.maLop}
                      </p>

                      <div className="tc-progress-block">
                        <div className="progress-bar">
                          <div
                            className="progress-fill"
                            style={{
                              width: `${Math.max(
                                0,
                                Math.min(100, phanTramHienThi)
                              )}%`,
                            }}
                          />
                        </div>

                        <div className="tc-progress-text">
                          Hoàn thành {phanTramHienThi}%
                        </div>
                      </div>

                      <button
                        className="btn btn-primary btn-sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/lop-hoc/${lop.maLopHoc}`);
                        }}
                      >
                        Vào học
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
      </section>
    </div>
  );

  return (
    <div className="trang-chu-container">
      {!isLoggedIn ? renderGuestView() : renderUserView()}
    </div>
  );
}