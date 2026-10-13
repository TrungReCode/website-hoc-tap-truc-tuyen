import { useEffect, useState, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { moHoSoKiemDuyet, dongHoSoKiemDuyet, duyetKhoaHoc } from '../../services/duyetKhoaHocService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import XemTepTinModal from '../../components/XemTepTinModal';
import '../../components/XemTepTinModal.css';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';

const NHAN_TRANG_THAI = {
  CHO_KIEM_DUYET: { nhan: 'Chờ duyệt', lop: 'badge-amber' },
  CHO_BO_SUNG: { nhan: 'Chờ giảng viên bổ sung', lop: 'badge-amber' },
  CONG_KHAI: { nhan: 'Công khai', lop: 'badge-green' },
  NHAP: { nhan: 'Nháp', lop: 'badge-gray' },
  LUU_TRU: { nhan: 'Lưu trữ', lop: 'badge-gray' },
  NOI_BO: { nhan: 'Nội bộ', lop: 'badge-red' },
};

export default function KiemDuyetKhoaHoc() {
  const { maKhoaHoc } = useParams();
  const navigate = useNavigate();

  const [khoaHoc, setKhoaHoc] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [hoSoBiKhoa, setHoSoBiKhoa] = useState(false);
  const [hanhDongDangChon, setHanhDongDangChon] = useState(null); // null | 'TU_CHOI' | 'YEU_CAU_BO_SUNG'
  const [ghiChu, setGhiChu] = useState('');
  const [dangXuLy, setDangXuLy] = useState(false);
  const daMoHoSo = useRef(false);
  const [baiHocDangXem, setBaiHocDangXem] = useState(null);

  useEffect(() => {
    setDangTai(true);
    moHoSoKiemDuyet(maKhoaHoc)
      .then((kh) => {
        setKhoaHoc(kh);
        daMoHoSo.current = true;
      })
      .catch((err) => {
        if (err?.response?.status === 409) {
          setHoSoBiKhoa(true);
        }
        setLoi(layThongBaoLoi(err));
      })
      .finally(() => setDangTai(false));

    // Nhả khóa Redis khi rời trang (điều hướng đi nơi khác, không quyết định)
    return () => {
      if (daMoHoSo.current) {
        dongHoSoKiemDuyet(maKhoaHoc).catch(() => { });
      }
    };
  }, [maKhoaHoc]);

  async function xuLyQuyetDinh(hanhDong) {
    if ((hanhDong === 'TU_CHOI' || hanhDong === 'YEU_CAU_BO_SUNG') && !ghiChu.trim()) {
      setLoi(
        hanhDong === 'TU_CHOI'
          ? 'Vui lòng nhập lý do từ chối để Giảng viên biết cách chỉnh sửa'
          : 'Vui lòng nhập ghi chú để Giảng viên biết cần bổ sung những gì'
      );
      return;
    }
    setLoi('');
    setDangXuLy(true);
    try {
      await duyetKhoaHoc(maKhoaHoc, { hanhDong, ghiChu: hanhDong === 'DUYET' ? null : ghiChu });
      daMoHoSo.current = false; // backend đã tự nhả khóa sau khi quyết định
      navigate('/admin/kiem-duyet');
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  }

  if (dangTai) return <div className="empty-state">Đang mở hồ sơ kiểm duyệt...</div>;

  const { navItems, roleLabel } = layNavTheoVaiTro();

  if (hoSoBiKhoa) {
    return (
      <DashboardShell roleLabel={roleLabel} navItems={navItems} crumb={<>Hàng chờ kiểm duyệt / <b>Đã bị khóa</b></>}>
        <div style={{ padding: 40, textAlign: 'center' }}>
          <i className="ti ti-lock" style={{ fontSize: 32, color: 'var(--warning)' }} aria-hidden="true"></i>
          <p style={{ marginTop: 10, fontSize: 14 }}>{loi}</p>
          <button className="btn btn-secondary" style={{ marginTop: 14 }} onClick={() => navigate('/admin/kiem-duyet')}>
            Quay lại danh sách
          </button>
        </div>
      </DashboardShell>
    );
  }

  if (!khoaHoc) return <div className="alert alert-danger" style={{ margin: 20 }}>{loi}</div>;

  const tongSoBaiHoc = khoaHoc.danhSachChuong.reduce((t, c) => t + c.danhSachBaiHoc.length, 0);
  const dangChoDuyet = khoaHoc.trangThai === 'CHO_KIEM_DUYET';
  const nhanTrangThai = NHAN_TRANG_THAI[khoaHoc.trangThai] || { nhan: khoaHoc.trangThai, lop: 'badge-gray' };

  return (
    <DashboardShell
      roleLabel={roleLabel}
      navItems={navItems}
      crumb={<>{dangChoDuyet ? 'Hàng chờ kiểm duyệt' : 'Xem nội dung khóa học'} / <b>{khoaHoc.tieuDe}</b></>}
      actions={
        dangChoDuyet
          ? <span className="badge badge-amber">Đang giữ chỗ — hồ sơ tạm khóa với Admin khác</span>
          : <span className={`badge ${nhanTrangThai.lop}`}>{nhanTrangThai.nhan} — chỉ xem, không thể duyệt</span>
      }
    >
      <div className="sc-split" style={{ minHeight: 'calc(100vh - 160px)' }}>
        <div className="split-left">
          {loi && <div className="alert alert-danger">{loi}</div>}

          <div className="card">
            <div className="card-title">{khoaHoc.tieuDe}</div>
            <p style={{ fontSize: 13, color: 'var(--text-2)', marginBottom: 10 }}>{khoaHoc.mieuTa || 'Không có mô tả.'}</p>
            <div style={{ display: 'flex', gap: 16, fontSize: 13, flexWrap: 'wrap' }}>
              <span><i className="ti ti-user" aria-hidden="true"></i> Tác giả: <strong>{khoaHoc.tenTacGia || '—'}</strong></span>
              <span><i className="ti ti-category" aria-hidden="true"></i> {khoaHoc.tenDanhMuc}</span>
              <span><i className="ti ti-coin" aria-hidden="true"></i> Giá khóa học: {Number(khoaHoc.gia).toLocaleString('vi-VN')} VNĐ</span>
              <span><i className="ti ti-chalkboard" aria-hidden="true"></i> Phí mở lớp: {khoaHoc.phiDayHoc != null ? Number(khoaHoc.phiDayHoc).toLocaleString('vi-VN') + ' VNĐ' : '—'}</span>
              <span><i className="ti ti-list-details" aria-hidden="true"></i> {khoaHoc.danhSachChuong.length} chương, {tongSoBaiHoc} bài học</span>
            </div>
          </div>

          <div className="card">
            <div className="card-title">Giáo trình</div>
            {khoaHoc.danhSachChuong.map((chuong) => (
              <div key={chuong.maChuong} style={{ marginBottom: 8 }}>
                <div style={{ fontSize: 13, fontWeight: 600, padding: '6px 4px' }}>{chuong.tieuDe}</div>
                {chuong.danhSachBaiHoc.map((b) => (
                  <div key={b.maBaiHoc} className="curr-item" onClick={() => setBaiHocDangXem(b)} style={{ cursor: 'pointer' }}>
                    <div className="ci-icon">
                      <i
                        className={`ti ${b.loai === 'VIDEO' ? 'ti-player-play' : b.loai === 'QUIZ' ? 'ti-help-circle' : 'ti-file-text'}`}
                        aria-hidden="true"
                      ></i>
                    </div>
                    <div className="ci-title">{b.tieuDe}</div>
                    <span className="badge badge-blue">{b.loai}</span>
                    {b.xemThu && <span className="badge badge-green">Xem</span>}
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>

        <div className="split-right">
          <div className="card sm">
            <div className="card-title" style={{ fontSize: 13 }}>Quyết định</div>
            {!dangChoDuyet ? (
              <div style={{ padding: '10px 0', fontSize: 13, color: 'var(--text-3)' }}>
                <i className="ti ti-info-circle" aria-hidden="true"></i> Khóa học này không ở trạng thái chờ duyệt —
                chỉ xem nội dung, không có hành động xét duyệt nào áp dụng được ở đây.
              </div>
            ) : !hanhDongDangChon ? (
              <div className="btn-row" style={{ marginTop: 0, flexDirection: 'column' }}>
                <button className="btn btn-success" style={{ width: '100%' }} disabled={dangXuLy} onClick={() => xuLyQuyetDinh('DUYET')}>
                  <i className="ti ti-circle-check" aria-hidden="true"></i> Duyệt - công khai khóa học
                </button>
                <button
                  className="btn btn-secondary"
                  style={{ width: '100%', color: 'var(--warning)', borderColor: 'var(--warning)' }}
                  disabled={dangXuLy}
                  onClick={() => { setGhiChu(''); setHanhDongDangChon('YEU_CAU_BO_SUNG'); }}
                >
                  <i className="ti ti-message-circle-question" aria-hidden="true"></i> Yêu cầu bổ sung
                </button>
                <button className="btn btn-danger" style={{ width: '100%' }} disabled={dangXuLy} onClick={() => { setGhiChu(''); setHanhDongDangChon('TU_CHOI'); }}>
                  <i className="ti ti-circle-x" aria-hidden="true"></i> Từ chối
                </button>
              </div>
            ) : (
              <div>
                <div className="form-group">
                  <label className="form-label">
                    {hanhDongDangChon === 'TU_CHOI'
                      ? 'Lý do từ chối (Giảng viên sẽ nhìn thấy)'
                      : 'Ghi chú cần bổ sung (Giảng viên sẽ nhìn thấy)'}
                  </label>
                  <textarea
                    className="form-input"
                    style={{ height: 90, padding: 8 }}
                    value={ghiChu}
                    onChange={(e) => setGhiChu(e.target.value)}
                    placeholder={
                      hanhDongDangChon === 'TU_CHOI'
                        ? 'Vd: Video bài 3 bị lỗi âm thanh, vui lòng tải lại.'
                        : 'Vd: Vui lòng bổ sung mô tả chi tiết hơn cho chương 2 trước khi duyệt.'
                    }
                  />
                </div>
                <div className="btn-row" style={{ flexDirection: 'column' }}>
                  <button
                    className={hanhDongDangChon === 'TU_CHOI' ? 'btn btn-danger' : 'btn btn-success'}
                    style={{ width: '100%' }}
                    disabled={dangXuLy}
                    onClick={() => xuLyQuyetDinh(hanhDongDangChon)}
                  >
                    {dangXuLy ? 'Đang gửi...' : hanhDongDangChon === 'TU_CHOI' ? 'Xác nhận từ chối' : 'Gửi yêu cầu bổ sung'}
                  </button>
                  <button className="btn btn-secondary" style={{ width: '100%' }} onClick={() => setHanhDongDangChon(null)}>
                    Hủy
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
      {baiHocDangXem && (
        <XemTepTinModal
          baiHoc={baiHocDangXem}
          onClose={() => setBaiHocDangXem(null)}
          xemThu
        />
      )}
    </DashboardShell>
  );
}
