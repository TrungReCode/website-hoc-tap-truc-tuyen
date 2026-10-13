import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { layChiTietChoHocVien } from '../../services/khoaHocService';
import { xemTienDoCaNhan, capNhatTienDoTuHoc, capGiayCaNhan } from '../../services/chungChiService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import XemTepTinModal from '../../components/XemTepTinModal';

const BIEU_TUONG_LOAI = {
  VIDEO: 'ti-player-play',
  PDF: 'ti-file-text',
  DOC: 'ti-file-text',
  QUIZ: 'ti-help-circle',
  LINK: 'ti-link',
  ZIP: 'ti-file-zip',
};

export default function HocKhoaHoc() {
  const { maKhoaHoc } = useParams();
  const navigate = useNavigate();
  const [khoaHoc, setKhoaHoc] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [baiHocDangXem, setBaiHocDangXem] = useState(null);

  // Cache vị trí dừng mới nhất theo maBaiHoc, cập nhật ĐỒNG BỘ qua ref — xem
  // giải thích chi tiết ở GiaoDienLopHoc.jsx (cùng vấn đề, cùng cách sửa).
  const viTriMoiNhatRef = useRef({});

  const [tienDo, setTienDo] = useState(null);
  const [dangCapGiay, setDangCapGiay] = useState(false);
  const [loiCapGiay, setLoiCapGiay] = useState('');
  const [giayDaCap, setGiayDaCap] = useState(null);

  useEffect(() => {
    let daHuy = false;
    setDangTai(true);
    setLoi('');
    layChiTietChoHocVien(maKhoaHoc)
      .then(kh => { if (!daHuy) setKhoaHoc(kh); })
      .catch(err => { if (!daHuy) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (!daHuy) setDangTai(false); });
    return () => { daHuy = true; };
  }, [maKhoaHoc]);

  function taiLaiTienDo() {
    xemTienDoCaNhan(maKhoaHoc).then(setTienDo).catch(() => {}); // im lặng — không phải lỗi nghiêm trọng nếu tải tiến độ thất bại
  }

  useEffect(() => {
    if (!dangTai && khoaHoc) taiLaiTienDo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dangTai, khoaHoc]);

  function moBaiHoc(baiHoc) {
    if (baiHoc.loai === 'QUIZ') {
      // Điều hướng sang trang làm bài thật (có chấm điểm, lưu kết quả, cập
      // nhật tiến độ) thay vì hiển thị tĩnh — xem LamBaiKiemTraTuHoc.jsx.
      navigate(`/khoa-hoc-cua-toi/${maKhoaHoc}/quiz/${baiHoc.maBaiHoc}`);
    } else {
      // Ưu tiên vị trí dừng mới nhất trong ref (nếu đã từng xem trong phiên
      // này) — đảm bảo đúng ngay cả khi state `khoaHoc` chưa kịp re-render.
      const viTriMoiNhat = viTriMoiNhatRef.current[baiHoc.maBaiHoc];
      setBaiHocDangXem(viTriMoiNhat != null ? { ...baiHoc, viTriDung: viTriMoiNhat } : baiHoc);
    }
  }

  async function xuLyHoanThanhBaiHoc(baiHoc) {
    try {
      await capNhatTienDoTuHoc(baiHoc.maBaiHoc, { hoanThanh: true });
      taiLaiTienDo();
      // FIX: cập nhật lạc quan hoanThanh vào state `khoaHoc` ngay — cùng lý
      // do đã sửa ở xuLyCapNhatViTri bên dưới (và ở GiaoDienLopHoc.jsx):
      // không patch state cục bộ thì dấu tích chỉ hiện sau khi rời trang
      // quay lại (khoaHoc được fetch lại từ server), không hiện ngay lập tức.
      setKhoaHoc(prev => prev && {
        ...prev,
        danhSachChuong: prev.danhSachChuong?.map(chuong => ({
          ...chuong,
          danhSachBaiHoc: chuong.danhSachBaiHoc?.map(bh =>
            bh.maBaiHoc === baiHoc.maBaiHoc ? { ...bh, hoanThanh: true } : bh
          ),
        })),
      });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setBaiHocDangXem(null);
    }
  }

  // Lưu vị trí dừng — không cần chờ, không cần báo lỗi ồn ào (chỉ log),
  // vì đây là tiện ích phụ, lỗi ở đây không nên chặn trải nghiệm xem.
  function xuLyCapNhatViTri(maBaiHoc, viTriMoi) {
    // Ghi vào ref TRƯỚC TIÊN, đồng bộ ngay lập tức (không đợi setState/re-render)
    // — nguồn dữ liệu đáng tin cậy nhất khi mở lại bài học trong cùng phiên.
    viTriMoiNhatRef.current[maBaiHoc] = viTriMoi;

    capNhatTienDoTuHoc(maBaiHoc, { viTriDung: viTriMoi }).catch(err => {
      console.error('Lưu vị trí dừng thất bại:', err);
    });

    setKhoaHoc(prev => prev && {
      ...prev,
      danhSachChuong: prev.danhSachChuong?.map(chuong => ({
        ...chuong,
        danhSachBaiHoc: chuong.danhSachBaiHoc?.map(bh =>
          bh.maBaiHoc === maBaiHoc ? { ...bh, viTriDung: viTriMoi } : bh
        ),
      })),
    });
  }

  async function xuLyNhanGiay() {
    setDangCapGiay(true);
    setLoiCapGiay('');
    try {
      const giay = await capGiayCaNhan(maKhoaHoc);
      setGiayDaCap(giay);
    } catch (err) {
      setLoiCapGiay(layThongBaoLoi(err));
    } finally {
      setDangCapGiay(false);
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <div className="topbar">
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/khoa-hoc-cua-toi/da-mua')}>
          <i className="ti ti-arrow-left" aria-hidden="true"></i>
        </button>
        <span className="t-title"><i className="ti ti-school" aria-hidden="true"></i> {khoaHoc?.tieuDe || 'Khóa học'}</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/trang-chu')}>Trang chủ</button>
      </div>

      <div style={{ padding: 20, maxWidth: 860, width: '100%', margin: '0 auto' }}>
        {loi && <div className="alert alert-danger">{loi}</div>}

        {dangTai && <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải giáo trình...</p>}

        {!dangTai && khoaHoc && (
          <>
            <div className="card sm" style={{ marginBottom: 16, display: 'flex', gap: 14, alignItems: 'center' }}>
              {khoaHoc.anhBia && (
                <img src={khoaHoc.anhBia} alt={khoaHoc.tieuDe} style={{ width: 96, height: 64, objectFit: 'cover', borderRadius: 6 }} />
              )}
              <div>
                <div style={{ fontWeight: 600 }}>{khoaHoc.tieuDe}</div>
                <div style={{ fontSize: 12, color: 'var(--text-3)' }}>GV: {khoaHoc.tenTacGia || '—'}</div>
              </div>
            </div>

            {tienDo && (
              <div className="card sm" style={{ marginBottom: 16 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                  <span style={{ fontSize: 13, fontWeight: 600 }}>
                    <i className="ti ti-certificate" aria-hidden="true"></i> Tiến độ hoàn thành
                  </span>
                  <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--primary)' }}>
                    {Number(tienDo.phanTramHoanThanh).toFixed(0)}%
                  </span>
                </div>
                <div style={{ height: 8, borderRadius: 4, background: 'var(--border)', overflow: 'hidden', marginBottom: 8 }}>
                  <div
                    style={{
                      height: '100%', borderRadius: 4, background: 'var(--primary)',
                      width: `${Math.min(100, Number(tienDo.phanTramHoanThanh))}%`,
                    }}
                  />
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-3)', marginBottom: 10 }}>
                  {tienDo.baiHoanThanh}/{tienDo.tongBaiHoc} bài học
                  {tienDo.tongQuiz > 0 && ` · ${tienDo.quizDat}/${tienDo.tongQuiz} bài kiểm tra đạt`}
                </div>

                {loiCapGiay && <div className="alert alert-danger" style={{ marginBottom: 8 }}>{loiCapGiay}</div>}

                {giayDaCap ? (
                  <a className="btn btn-primary btn-sm" href={giayDaCap.filePdfUrl} target="_blank" rel="noreferrer">
                    <i className="ti ti-download" aria-hidden="true"></i> Tải chứng chỉ (PDF)
                  </a>
                ) : tienDo.daDuDieuKien ? (
                  <button className="btn btn-primary btn-sm" onClick={xuLyNhanGiay} disabled={dangCapGiay}>
                    <i className="ti ti-certificate" aria-hidden="true"></i>
                    {dangCapGiay ? 'Đang xử lý...' : 'Nhận chứng chỉ hoàn thành'}
                  </button>
                ) : (
                  <p style={{ fontSize: 11, color: 'var(--text-3)', margin: 0 }}>
                    Hoàn thành 100% bài học và đạt mọi bài kiểm tra để đủ điều kiện nhận chứng chỉ.
                  </p>
                )}
              </div>
            )}

            {(!khoaHoc.danhSachChuong || khoaHoc.danhSachChuong.length === 0) && (
              <div className="empty-state">Giáo trình đang được cập nhật.</div>
            )}

            {khoaHoc.danhSachChuong?.map(chuong => (
              <div key={chuong.maChuong} className="card sm" style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 8 }}>{chuong.tieuDe}</div>
                {chuong.danhSachBaiHoc?.map(baiHoc => (
                  <div key={baiHoc.maBaiHoc} className="curr-item" onClick={() => moBaiHoc(baiHoc)}>
                    <div className={`ci-icon${baiHoc.hoanThanh ? ' done' : ''}`}>
                      <i className={`ti ${BIEU_TUONG_LOAI[baiHoc.loai] || 'ti-file'}`} aria-hidden="true"></i>
                    </div>
                    <div className="ci-title">{baiHoc.tieuDe}</div>
                    {/* FIX: trước đây trang tự học không hiện dấu tích hoàn
                        thành cho từng bài, dù backend đã trả sẵn hoanThanh —
                        chỉ thiếu render ở đây. Dùng đúng class ci-* sẵn có
                        của .curr-item (không dùng ll-check — class đó chỉ
                        lên màu khi nằm trong .lh-lesson của trang lớp học,
                        khác cấu trúc CSS ở trang tự học này). */}
                    {baiHoc.hoanThanh && (
                      <i className="ti ti-circle-check ci-check" aria-hidden="true"></i>
                    )}
                  </div>
                ))}
                {(!chuong.danhSachBaiHoc || chuong.danhSachBaiHoc.length === 0) && (
                  <div style={{ fontSize: 12, color: 'var(--text-3)' }}>Chương này chưa có bài học.</div>
                )}
              </div>
            ))}
          </>
        )}
      </div>

      {baiHocDangXem && (
        <XemTepTinModal
          baiHoc={baiHocDangXem}
          onClose={() => setBaiHocDangXem(null)}
          onHoanThanh={() => xuLyHoanThanhBaiHoc(baiHocDangXem)}
          onCapNhatViTri={(viTriMoi) => xuLyCapNhatViTri(baiHocDangXem.maBaiHoc, viTriMoi)}
          hienNutDanhDauDaDocXong
        />
      )}
    </div>
  );
}