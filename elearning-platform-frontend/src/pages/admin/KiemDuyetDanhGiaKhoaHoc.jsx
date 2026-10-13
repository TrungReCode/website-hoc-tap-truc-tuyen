import { useEffect, useState, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { layChiTietCongKhai } from '../../services/khoaHocService';
import { layTatCaDanhGiaChoAdmin, anDanhGiaViPham, boAnDanhGia, xoaDanhGiaViPham } from '../../services/danhGiaQuanTriService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import '../../components/XemTepTinModal.css';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';

export default function KiemDuyetDanhGiaKhoaHoc() {
  const { maKhoaHoc } = useParams();
  const navigate = useNavigate();

  const [khoaHoc, setKhoaHoc] = useState(null);
  const [trang, setTrang] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 });
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [dangXuLyId, setDangXuLyId] = useState(null);

  // modal xử lý: { maDanhGia, loai: 'an' | 'xoa' }
  const [modalXuLy, setModalXuLy] = useState(null);
  const [lyDo, setLyDo] = useState('');

  const taiLai = useCallback((trangSo = 0) => {
    setDangTai(true);
    layTatCaDanhGiaChoAdmin(maKhoaHoc, { page: trangSo })
      .then(setTrang)
      .catch((err) => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, [maKhoaHoc]);

  useEffect(() => {
    layChiTietCongKhai(maKhoaHoc).then(setKhoaHoc).catch(() => { });
    taiLai(0);
  }, [maKhoaHoc, taiLai]);

  function moModalXuLy(maDanhGia, loai) {
    setLyDo('');
    setLoi('');
    setModalXuLy({ maDanhGia, loai });
  }

  async function xacNhanXuLy() {
    if (!lyDo.trim()) {
      setLoi('Vui lòng nhập lý do để học viên được biết');
      return;
    }
    setDangXuLyId(modalXuLy.maDanhGia);
    try {
      if (modalXuLy.loai === 'an') {
        await anDanhGiaViPham(modalXuLy.maDanhGia, lyDo.trim());
      } else {
        await xoaDanhGiaViPham(modalXuLy.maDanhGia, lyDo.trim());
      }
      setModalXuLy(null);
      taiLai(trang.number);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLyId(null);
    }
  }

  async function xuLyBoAn(maDanhGia) {
    setLoi('');
    setDangXuLyId(maDanhGia);
    try {
      await boAnDanhGia(maDanhGia);
      taiLai(trang.number);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLyId(null);
    }
  }

  const { navItems, roleLabel } = layNavTheoVaiTro();

  return (
    <DashboardShell
      roleLabel={roleLabel}
      navItems={navItems}
      crumb={<>Kiểm duyệt đánh giá / <b>{khoaHoc?.tieuDe || `Khóa học #${maKhoaHoc}`}</b></>}
      actions={<span className="badge badge-amber">{trang.totalElements} đánh giá</span>}
    >
      {loi && !modalXuLy && <div className="alert alert-danger">{loi}</div>}

        {dangTai ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
        ) : trang.content.length === 0 ? (
          <div className="empty-state">Khóa học này chưa có đánh giá nào.</div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {trang.content.map((d) => {
              const dangXuLy = dangXuLyId === d.maDanhGia;
              return (
                <div key={d.maDanhGia} className="card sm" style={{ opacity: d.an ? 0.6 : 1 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, flexWrap: 'wrap' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                        <strong style={{ fontSize: 13 }}>{d.tenNguoiDung || `Người dùng #${d.maNguoiDung}`}</strong>
                        <span style={{ color: 'var(--warning)' }}>
                          {'★'.repeat(d.soSao)}{'☆'.repeat(5 - d.soSao)}
                        </span>
                        {d.an && <span className="badge badge-red">Đã bị ẩn</span>}
                      </div>
                      <p style={{ fontSize: 13, color: 'var(--text-2)', margin: 0 }}>{d.nhanXet || <em>Không có nhận xét.</em>}</p>
                      <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 4 }}>
                        {new Date(d.ngayTao).toLocaleDateString('vi-VN')}
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                      {!d.an ? (
                        <button className="btn btn-secondary btn-sm" disabled={dangXuLy} onClick={() => moModalXuLy(d.maDanhGia, 'an')}>
                          <i className="ti ti-eye-off" aria-hidden="true"></i> Ẩn
                        </button>
                      ) : (
                        <button className="btn btn-secondary btn-sm" disabled={dangXuLy} onClick={() => xuLyBoAn(d.maDanhGia)}>
                          <i className="ti ti-eye" aria-hidden="true"></i> Bỏ ẩn
                        </button>
                      )}
                      <button className="btn btn-danger btn-sm" disabled={dangXuLy} onClick={() => moModalXuLy(d.maDanhGia, 'xoa')}>
                        <i className="ti ti-trash" aria-hidden="true"></i> Xóa
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {trang.totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginTop: 20 }}>
            <button className="btn btn-secondary btn-sm" disabled={trang.number === 0} onClick={() => taiLai(trang.number - 1)}>
              ‹
            </button>
            {Array.from({ length: trang.totalPages }, (_, i) => i).map((i) => (
              <button key={i} className={`btn btn-sm ${i === trang.number ? 'btn-primary' : 'btn-secondary'}`} onClick={() => taiLai(i)}>
                {i + 1}
              </button>
            ))}
            <button className="btn btn-secondary btn-sm" disabled={trang.number >= trang.totalPages - 1} onClick={() => taiLai(trang.number + 1)}>
              ›
            </button>
          </div>
        )}
      {modalXuLy && (
        <div className="modal-overlay" onClick={() => setModalXuLy(null)}>
          <div className="modal-content" style={{ maxWidth: 440, padding: 20 }} onClick={(e) => e.stopPropagation()}>
            <div className="card-title" style={{ marginBottom: 10 }}>
              {modalXuLy.loai === 'an' ? 'Ẩn đánh giá vi phạm' : 'Xóa đánh giá vi phạm'}
            </div>
            {loi && <div className="alert alert-danger">{loi}</div>}
            <div className="form-group">
              <label className="form-label">Lý do (học viên viết đánh giá sẽ nhận được thông báo này)</label>
              <textarea
                className="form-input"
                style={{ height: 90, padding: 8 }}
                value={lyDo}
                onChange={(e) => setLyDo(e.target.value)}
                placeholder="Vd: Nội dung đánh giá chứa ngôn từ không phù hợp."
                autoFocus
              />
            </div>
            <div className="btn-row" style={{ flexDirection: 'column' }}>
              <button
                className={modalXuLy.loai === 'an' ? 'btn btn-secondary' : 'btn btn-danger'}
                style={{ width: '100%' }}
                disabled={dangXuLyId === modalXuLy.maDanhGia}
                onClick={xacNhanXuLy}
              >
                {dangXuLyId === modalXuLy.maDanhGia ? 'Đang xử lý...' : modalXuLy.loai === 'an' ? 'Xác nhận ẩn' : 'Xác nhận xóa'}
              </button>
              <button className="btn btn-secondary" style={{ width: '100%' }} onClick={() => setModalXuLy(null)}>
                Hủy
              </button>
            </div>
          </div>
        </div>
      )}
    </DashboardShell>
  );
}
