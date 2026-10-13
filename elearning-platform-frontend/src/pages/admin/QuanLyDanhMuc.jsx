import { useEffect, useState, useCallback, Fragment } from 'react';
import { useNavigate } from 'react-router-dom';
import { layDanhSachDanhMucQuanTri, taoDanhMuc, capNhatDanhMuc, xoaDanhMuc } from '../../services/danhMucQuanTriService';
import { layThongBaoLoi } from '../../utils/layThongBaoLoi';
import '../../components/XemTepTinModal.css';
import DashboardShell, { layNavTheoVaiTro } from '../../components/DashboardShell';

export default function QuanLyDanhMuc() {
  const navigate = useNavigate();
  const [danhSach, setDanhSach] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  const [formDangMo, setFormDangMo] = useState(null); // null | 'them' | danhMucDangSua (object)
  const [dangXuLy, setDangXuLy] = useState(false);

  const taiLai = useCallback(() => {
    setDangTai(true);
    layDanhSachDanhMucQuanTri()
      .then(setDanhSach)
      .catch((err) => setLoi(layThongBaoLoi(err)))
      .finally(() => setDangTai(false));
  }, []);

  useEffect(() => { taiLai(); }, [taiLai]);

  const mucGoc = danhSach.filter((d) => !d.maDanhMucCha);
  const conCua = (maDanhMucCha) => danhSach.filter((d) => d.maDanhMucCha === maDanhMucCha);

  async function xuLyXoa(d) {
    if (d.soKhoaHoc > 0) {
      setLoi(`Không thể xóa "${d.tenDanhMuc}": vẫn còn ${d.soKhoaHoc} khóa học thuộc danh mục này.`);
      return;
    }
    if (d.soDanhMucCon > 0) {
      setLoi(`Không thể xóa "${d.tenDanhMuc}": vẫn còn ${d.soDanhMucCon} danh mục con bên trong.`);
      return;
    }
    if (!window.confirm(`Xóa danh mục "${d.tenDanhMuc}"?`)) return;
    setLoi('');
    try {
      await xoaDanhMuc(d.maDanhMuc);
      taiLai();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    }
  }

  function hangDanhMuc(d, laCon) {
    return (
      <tr key={d.maDanhMuc}>
        <td style={{ paddingLeft: laCon ? 28 : 12 }}>
          {laCon && <i className="ti ti-corner-down-right" style={{ marginRight: 6, color: 'var(--text-3)' }} aria-hidden="true"></i>}
          {d.tenDanhMuc}
        </td>
        <td>{d.soKhoaHoc}</td>
        <td>{d.soDanhMucCon}</td>
        <td style={{ textAlign: 'right' }}>
          <button className="btn btn-secondary btn-sm" onClick={() => setFormDangMo(d)}>
            <i className="ti ti-pencil" aria-hidden="true"></i> Sửa
          </button>{' '}
          <button className="btn btn-danger btn-sm" onClick={() => xuLyXoa(d)}>
            <i className="ti ti-trash" aria-hidden="true"></i>
          </button>
        </td>
      </tr>
    );
  }

  const { navItems, roleLabel } = layNavTheoVaiTro();

  return (
    <DashboardShell
      roleLabel={roleLabel}
      navItems={navItems}
      crumb="Danh mục"
      actions={<span className="badge badge-amber">{danhSach.length} danh mục</span>}
    >
      <div className="ds-page-head">
        <div><h1>Quản lý danh mục</h1><p>Danh mục hiển thị cho Giảng viên khi tạo khóa học</p></div>
        <div className="ds-actions">
          <button className="btn btn-primary btn-sm" onClick={() => setFormDangMo('them')}>
            <i className="ti ti-plus" aria-hidden="true"></i> Thêm danh mục
          </button>
        </div>
      </div>

      {loi && <div className="alert alert-danger">{loi}</div>}
        {dangTai ? (
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>Đang tải...</p>
        ) : danhSach.length === 0 ? (
          <div className="empty-state">
            <i className="ti ti-category" style={{ fontSize: 32 }} aria-hidden="true"></i>
            <p style={{ marginTop: 8 }}>Chưa có danh mục nào. Thêm danh mục đầu tiên để Giảng viên có thể chọn khi tạo khóa học.</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Tên danh mục</th>
                <th>Số khóa học</th>
                <th>Số danh mục con</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {mucGoc.map((goc) => (
                <Fragment key={goc.maDanhMuc}>
                  {hangDanhMuc(goc, false)}
                  {conCua(goc.maDanhMuc).map((con) => hangDanhMuc(con, true))}
                </Fragment>
              ))}
            </tbody>
          </table>
        )}

      {formDangMo && (
        <FormDanhMuc
          danhMuc={formDangMo === 'them' ? null : formDangMo}
          mucGocKhaDung={mucGoc.filter((g) => g.maDanhMuc !== (formDangMo === 'them' ? null : formDangMo.maDanhMuc))}
          dangXuLy={dangXuLy}
          setDangXuLy={setDangXuLy}
          setLoi={setLoi}
          onClose={() => setFormDangMo(null)}
          onThanhCong={() => { setFormDangMo(null); taiLai(); }}
        />
      )}
    </DashboardShell>
  );
}

function FormDanhMuc({ danhMuc, mucGocKhaDung, dangXuLy, setDangXuLy, setLoi, onClose, onThanhCong }) {
  const dangSua = !!danhMuc;
  const [tenDanhMuc, setTenDanhMuc] = useState(danhMuc?.tenDanhMuc || '');
  const [maDanhMucCha, setMaDanhMucCha] = useState(danhMuc?.maDanhMucCha ? String(danhMuc.maDanhMucCha) : '');

  // Danh mục đang có con thì không thể tự biến thành con của danh mục khác (giữ giới hạn 2 mức)
  const bienThanhConBiChan = dangSua && danhMuc.soDanhMucCon > 0;

  async function xuLyLuu(e) {
    e.preventDefault();
    if (!tenDanhMuc.trim()) {
      setLoi('Vui lòng nhập tên danh mục');
      return;
    }
    setLoi('');
    setDangXuLy(true);
    try {
      const payload = { tenDanhMuc: tenDanhMuc.trim(), maDanhMucCha: maDanhMucCha ? Number(maDanhMucCha) : null };
      if (dangSua) {
        await capNhatDanhMuc(danhMuc.maDanhMuc, payload);
      } else {
        await taoDanhMuc(payload);
      }
      onThanhCong();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 420 }}>
        <div className="modal-header">
          <div className="modal-title-section">
            <i className="ti ti-category" aria-hidden="true"></i>
            <span>{dangSua ? 'Sửa danh mục' : 'Thêm danh mục'}</span>
          </div>
          <button className="btn-close" onClick={onClose} title="Đóng (Esc)">
            <i className="ti ti-x" aria-hidden="true"></i>
          </button>
        </div>

        <form onSubmit={xuLyLuu}>
          <div className="modal-body" style={{ display: 'grid', gap: 12 }}>
            <div className="form-group">
              <label className="form-label">Tên danh mục</label>
              <input
                className="form-input"
                value={tenDanhMuc}
                onChange={(e) => setTenDanhMuc(e.target.value)}
                placeholder="Vd: Lập trình Web"
                maxLength={100}
                autoFocus
              />
            </div>

            <div className="form-group">
              <label className="form-label">Danh mục cha (tùy chọn)</label>
              <select
                className="form-input"
                value={maDanhMucCha}
                onChange={(e) => setMaDanhMucCha(e.target.value)}
                disabled={bienThanhConBiChan}
              >
                <option value="">- Không, đây là danh mục gốc -</option>
                {mucGocKhaDung.map((g) => (
                  <option key={g.maDanhMuc} value={g.maDanhMuc}>{g.tenDanhMuc}</option>
                ))}
              </select>
              {bienThanhConBiChan && (
                <p style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 4 }}>
                  Danh mục này đang có danh mục con nên phải giữ là danh mục gốc (chỉ hỗ trợ phân cấp 2 mức).
                </p>
              )}
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Hủy</button>
            <button type="submit" className="btn btn-primary" disabled={dangXuLy}>
              {dangXuLy ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}