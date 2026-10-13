import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { timKiemLopHocCongKhai } from '../services/lopHocService';
import { layThongBaoLoi } from '../utils/layThongBaoLoi';
import NganKeoChiTiet from '../components/NganKeoChiTiet';

const TUY_CHON_SAP_XEP = [
  { nhan: 'Phí tăng dần', gia_tri: 'phiLopHoc,asc' },
  { nhan: 'Phí giảm dần', gia_tri: 'phiLopHoc,desc' },
];

const TUY_CHON_GIA = [
  { nhan: 'Tất cả', gia_tri: '' },
  { nhan: 'Miễn phí', gia_tri: 'MIEN_PHI' },
  { nhan: 'Có phí', gia_tri: 'CO_PHI' },
];

const NHAN_TRANG_THAI = {
  SAP_DIEN_RA: { nhan: 'Sắp khai giảng', mau: 'var(--warning, #d97706)' },
  HOAT_DONG: { nhan: 'Đang hoạt động', mau: 'var(--success, #16a34a)' },
};

export default function DanhSachLopHoc() {
  const navigate = useNavigate();
  const [hoTen] = useState(localStorage.getItem('hoTen'));
  const maNguoiDung = localStorage.getItem('maNguoiDung');
  const [tuKhoa, setTuKhoa] = useState('');
  const [oTimKiem, setOTimKiem] = useState('');
  const [sapXep, setSapXep] = useState(TUY_CHON_SAP_XEP[0].gia_tri);
  const [giaLoc, setGiaLoc] = useState('');
  const [trang, setTrang] = useState({ content: [], totalPages: 0, totalElements: 0, number: 0 });
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [lopXemTongQuan, setLopXemTongQuan] = useState(null);

  const taiLopHoc = useCallback(
    async (trangSo = 0) => {
      setDangTai(true);
      setLoi('');
      try {
        const ketQua = await timKiemLopHocCongKhai({
          tuKhoa, gia: giaLoc, page: trangSo, size: 12, sort: sapXep,
        });
        setTrang(ketQua);
      } catch (err) {
        setLoi(layThongBaoLoi(err));
      } finally {
        setDangTai(false);
      }
    },
    [tuKhoa, giaLoc, sapXep]
  );

  useEffect(() => {
    taiLopHoc(0);
  }, [taiLopHoc]);

  function xuLyTimKiem(e) {
    e.preventDefault();
    setTuKhoa(oTimKiem.trim());
  }

  function xemLop(lop) {
    if (maNguoiDung && String(lop.maGiangVien) === String(maNguoiDung)) {
      navigate(`/lop-hoc/${lop.maLopHoc}/quan-ly?tab=thanh-vien`);
      return;
    }

    // FIX: trước đây bấm vào thẻ lớp là điều hướng THẲNG sang trang
    // "Tham gia lớp học" — trang đó yêu cầu đăng nhập, nên khách chưa
    // đăng nhập bị đá thẳng về /dang-nhap ngay khi mới chỉ muốn XEM, chưa
    // hề bấm "tham gia". Giờ chỉ mở modal tổng quan (dữ liệu đã có sẵn từ
    // danh sách công khai, không cần gọi thêm API) — việc điều hướng sang
    // luồng tham gia (có yêu cầu đăng nhập) chỉ xảy ra khi người dùng chủ
    // động bấm nút "Tham gia lớp học" bên trong modal.
    setLopXemTongQuan(lop);
  }

  function xuLyThamGia(lop) {
    if (!maNguoiDung) {
      navigate(`/dang-nhap?tiep=${encodeURIComponent(`/lop-hoc/tham-gia?ma=${lop.maLop}`)}`);
      return;
    }
    // Điều hướng sang trang "Tham gia lớp học", tự điền sẵn mã lớp để
    // đi qua đúng luồng kiểm tra khóa kép (sở hữu khóa gốc + phí lớp)
    // thay vì gọi thẳng API ghi danh từ đây.
    navigate(`/lop-hoc/tham-gia?ma=${lop.maLop}`);
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="global-header">
        <div style={{ fontSize: 16, fontWeight: 600, color: 'var(--primary)', cursor: 'pointer' }} onClick={() => navigate('/trang-chu')}>
          <i className="ti ti-school" aria-hidden="true"></i> EduPlatform
        </div>
        <form onSubmit={xuLyTimKiem} style={{ flex: 1, maxWidth: 320 }}>
          <input
            className="gh-search"
            placeholder="Tìm theo tên khóa học..."
            value={oTimKiem}
            onChange={(e) => setOTimKiem(e.target.value)}
          />
        </form>
        <div className="gh-spacer" />
        {hoTen ? (
          <div className="avatar" title={hoTen} style={{ cursor: 'pointer' }} onClick={() => navigate('/trang-chu')}>
            {hoTen.charAt(0).toUpperCase()}
          </div>
        ) : (
          <button className="btn btn-primary btn-sm" onClick={() => navigate('/dang-nhap')}>
            Đăng nhập
          </button>
        )}
      </div>

      <div style={{ padding: '16px 20px 0 20px' }}>
        <div className="mp-hero">
          <h1>Các lớp học đang mở - tham gia cùng giảng viên và học viên khác theo lịch cố định.</h1>
          <div className="mp-hero-actions">
            <button className="btn btn-accent" onClick={() => navigate('/lop-hoc/tham-gia')}>
              <i className="ti ti-door-enter" aria-hidden="true"></i> Có mã lớp? Tham gia ngay
            </button>
            {!hoTen && (
              <button className="btn btn-outline-light" onClick={() => navigate('/dang-ky')}>
                Đăng ký miễn phí
              </button>
            )}
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        <div style={{ width: 200, borderRight: '1px solid var(--border)', padding: 16, overflowY: 'auto', flexShrink: 0 }}>
          <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginBottom: 8 }}>SẮP XẾP</div>
          <select className="form-input" style={{ fontSize: 12, height: 30 }} value={sapXep} onChange={(e) => setSapXep(e.target.value)}>
            {TUY_CHON_SAP_XEP.map((t) => (
              <option key={t.gia_tri} value={t.gia_tri}>
                {t.nhan}
              </option>
            ))}
          </select>

          <div style={{ marginTop: 16, fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginBottom: 8 }}>PHÍ LỚP HỌC</div>
          <select className="form-input" style={{ fontSize: 12, height: 30 }} value={giaLoc} onChange={(e) => setGiaLoc(e.target.value)}>
            {TUY_CHON_GIA.map((t) => (
              <option key={t.gia_tri} value={t.gia_tri}>
                {t.nhan}
              </option>
            ))}
          </select>
        </div>

        <div style={{ flex: 1, padding: 16, overflowY: 'auto' }}>
          {loi && <div className="alert alert-danger">{loi}</div>}

          {!dangTai && (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <span style={{ fontSize: 13, color: 'var(--text-2)' }}>
                Hiển thị <strong>{trang.content.length}</strong> / {trang.totalElements} lớp học
              </span>
            </div>
          )}

          {dangTai ? (
            <div className="empty-state">Đang tải danh sách lớp học...</div>
          ) : trang.content.length === 0 ? (
            <div className="empty-state">
              <i className="ti ti-mood-empty" style={{ fontSize: 32 }} aria-hidden="true"></i>
              <p style={{ marginTop: 8 }}>Không tìm thấy lớp học phù hợp</p>
            </div>
          ) : (
            <div className="grid-3" style={{ gap: 14 }}>
              {trang.content.map((lop) => {
                const conCho = lop.siSo == null || lop.soThanhVien < lop.siSo;
                const trangThai = NHAN_TRANG_THAI[lop.trangThai] || { nhan: lop.trangThai, mau: 'var(--text-3)' };
                const laLopCuaToi = maNguoiDung && String(lop.maGiangVien) === String(maNguoiDung);
                return (
                  <div key={lop.maLopHoc} className="course-card" onClick={() => xemLop(lop)}>
                    <div className="course-thumb">
                      {lop.anhBiaKhoaHoc ? (
                        <img src={lop.anhBiaKhoaHoc} alt={lop.tenKhoaHoc} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      ) : (
                        <i className="ti ti-chalkboard" aria-hidden="true"></i>
                      )}
                    </div>
                    <div className="course-info">
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                        <span style={{ fontSize: 11, fontWeight: 700, color: trangThai.mau }}>{trangThai.nhan}</span>
                        <span style={{ fontSize: 11, fontFamily: 'monospace', color: 'var(--text-3)' }}>#{lop.maLop}</span>
                      </div>
                      {laLopCuaToi && (
                        <div style={{ marginBottom: 6 }}>
                          <span className="badge badge-green" style={{ fontSize: 11 }}>
                            Đây là lớp do bạn mở
                          </span>
                        </div>
                      )}
                      <div className="course-title">{lop.tenKhoaHoc || 'Khóa học'}</div>
                      <div className="course-meta">Giảng viên: {lop.tenGiangVien || 'Đang cập nhật'}</div>
                      <div className="course-meta">
                        Khai giảng: {lop.ngayBatDau ? new Date(lop.ngayBatDau).toLocaleDateString('vi-VN') : '—'}
                      </div>
                      <div className="star-row">
                        <span className="star">{'★'.repeat(Math.round(lop.diemTrungBinh || 0))}{'☆'.repeat(5 - Math.round(lop.diemTrungBinh || 0))}</span>
                        <span style={{ fontSize: 11, color: 'var(--text-3)' }}>
                          {lop.diemTrungBinh ? lop.diemTrungBinh.toFixed(1) : 'Mới'} ({lop.soLuongDanhGia || 0})
                        </span>
                      </div>
                      <div style={{ marginTop: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div className="course-price" style={{ ...(Number(lop.phiLopHoc) === 0 ? { color: 'var(--success)' } : {}), fontSize: 14 }}>
                          {Number(lop.phiLopHoc) === 0 ? 'Miễn phí' : `${Number(lop.phiLopHoc).toLocaleString('vi-VN')} đ`}
                        </div>
                        <span style={{ fontSize: 11, color: conCho ? 'var(--text-2)' : 'var(--danger, #dc2626)', fontWeight: conCho ? 400 : 600 }}>
                          {lop.siSo == null ? 'Không giới hạn' : conCho ? `Còn ${lop.siSo - lop.soThanhVien}/${lop.siSo} chỗ` : 'Đã đủ sĩ số'}
                        </span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {trang.totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginTop: 20 }}>
              <button className="btn btn-secondary btn-sm" disabled={trang.number === 0} onClick={() => taiLopHoc(trang.number - 1)}>
                ‹
              </button>
              {Array.from({ length: trang.totalPages }, (_, i) => i)
                .slice(Math.max(0, trang.number - 2), trang.number + 3)
                .map((i) => (
                  <button key={i} className={`btn btn-sm ${i === trang.number ? 'btn-primary' : 'btn-secondary'}`} onClick={() => taiLopHoc(i)}>
                    {i + 1}
                  </button>
                ))}
              <button className="btn btn-secondary btn-sm" disabled={trang.number >= trang.totalPages - 1} onClick={() => taiLopHoc(trang.number + 1)}>
                ›
              </button>
            </div>
          )}
        </div>
      </div>

      <NganKeoChiTiet
        mo={!!lopXemTongQuan}
        onDong={() => setLopXemTongQuan(null)}
        tieuDe="Tổng quan lớp học"
        rongPx={440}
      >
        {lopXemTongQuan && (
          <div>
            {lopXemTongQuan.anhBiaKhoaHoc && (
              <img
                src={lopXemTongQuan.anhBiaKhoaHoc}
                alt=""
                style={{ width: '100%', borderRadius: 8, marginBottom: 14, maxHeight: 160, objectFit: 'cover' }}
              />
            )}

            <h3 style={{ margin: '0 0 4px' }}>{lopXemTongQuan.tenKhoaHoc}</h3>
            <div style={{ fontSize: 13, color: 'var(--text-3)', marginBottom: 14 }}>
              Mã lớp: <strong>{lopXemTongQuan.maLop}</strong> · Giảng viên: {lopXemTongQuan.tenGiangVien || '—'}
            </div>

            <div style={{ display: 'grid', gap: 8, fontSize: 14, marginBottom: 18 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-3)' }}>Học phí</span>
                <strong>
                  {lopXemTongQuan.phiLopHoc > 0
                    ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(lopXemTongQuan.phiLopHoc)
                    : 'Miễn phí'}
                </strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-3)' }}>Khai giảng</span>
                <span>{lopXemTongQuan.ngayBatDau ? new Date(lopXemTongQuan.ngayBatDau).toLocaleDateString('vi-VN') : '—'}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-3)' }}>Kết thúc dự kiến</span>
                <span>{lopXemTongQuan.ngayKetThuc ? new Date(lopXemTongQuan.ngayKetThuc).toLocaleDateString('vi-VN') : '—'}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-3)' }}>Sĩ số</span>
                <span>{lopXemTongQuan.soThanhVien ?? 0}{lopXemTongQuan.siSo ? ` / ${lopXemTongQuan.siSo}` : ''}</span>
              </div>
              {lopXemTongQuan.diemTrungBinh != null && (
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-3)' }}>Đánh giá</span>
                  <span>⭐ {lopXemTongQuan.diemTrungBinh.toFixed(1)} ({lopXemTongQuan.soLuongDanhGia ?? 0})</span>
                </div>
              )}
            </div>

            <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => xuLyThamGia(lopXemTongQuan)}>
              {maNguoiDung ? 'Tham gia lớp học' : 'Đăng nhập để tham gia'}
            </button>
          </div>
        )}
      </NganKeoChiTiet>
    </div>
  );
}