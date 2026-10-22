import { useId } from 'react';
import { LOAI_CAU_HOI, dapAnMacDinh } from '../../utils/cauHoi';
import './quiz.css';

const DANG_CHON_PHUONG_AN = ['MOT_DAP_AN', 'NHIEU_DAP_AN'];
const DANG_MOI_DONG_DEU_DUNG = ['TRA_LOI_NGAN', 'SAP_XEP', 'GHEP_CAP'];

/**
 * Trình soạn 1 câu hỏi cho giảng viên — dùng chung cho quiz khóa học gốc và quiz bổ sung.
 * Controlled: value = { noiDung, loaiCauHoi, dapAn: [...] }, onChange(valueMoi).
 * Kiểm tra hợp lệ và dựng body gửi backend nằm ở utils/cauHoi.js
 * (kiemTraSoanThao, taoPayloadCauHoi).
 */
export default function BoSoanCauHoi({ value, onChange, disabled = false }) {
  const nhom = useId();
  const { noiDung, loaiCauHoi, dapAn } = value;

  const capNhat = (phan) => onChange({ ...value, ...phan });
  const suaDong = (i, phan) => capNhat({ dapAn: dapAn.map((d, idx) => (idx === i ? { ...d, ...phan } : d)) });
  const xoaDong = (i) => capNhat({ dapAn: dapAn.filter((_, idx) => idx !== i) });
  const chuyenDong = (i, huong) => {
    const j = i + huong;
    if (j < 0 || j >= dapAn.length) return;
    const moi = [...dapAn];
    [moi[i], moi[j]] = [moi[j], moi[i]];
    capNhat({ dapAn: moi });
  };
  const themDong = () => {
    const dongMoi = { noiDung: '', dapAnDung: DANG_MOI_DONG_DEU_DUNG.includes(loaiCauHoi) };
    if (loaiCauHoi === 'GHEP_CAP') dongMoi.noiDungGhep = '';
    capNhat({ dapAn: [...dapAn, dongMoi] });
  };
  const chonMotDapAnDung = (i) => capNhat({ dapAn: dapAn.map((d, idx) => ({ ...d, dapAnDung: idx === i })) });

  function doiLoai(moi) {
    if (moi === loaiCauHoi) return;
    if (DANG_CHON_PHUONG_AN.includes(loaiCauHoi) && DANG_CHON_PHUONG_AN.includes(moi)) {
      // Chuyển qua lại giữa "1 đáp án" và "nhiều đáp án": giữ nội dung đã gõ; riêng "1 đáp án"
      // chỉ giữ lại đáp án đúng ĐẦU TIÊN.
      let daGap = false;
      const dapAnMoi = dapAn.map((d) => {
        if (moi !== 'MOT_DAP_AN') return d;
        const giu = d.dapAnDung && !daGap;
        if (giu) daGap = true;
        return { ...d, dapAnDung: giu };
      });
      onChange({ ...value, loaiCauHoi: moi, dapAn: dapAnMoi });
    } else {
      onChange({ ...value, loaiCauHoi: moi, dapAn: dapAnMacDinh(moi) });
    }
  }

  return (
    <>
      <div className="form-group">
        <label className="form-label">Nội dung câu hỏi</label>
        <input
          className="form-input"
          type="text"
          value={noiDung}
          disabled={disabled}
          onChange={(e) => capNhat({ noiDung: e.target.value })}
          placeholder="Nhập câu hỏi"
        />
      </div>
      <div className="form-group">
        <label className="form-label">Dạng câu hỏi</label>
        <select className="form-input" value={loaiCauHoi} disabled={disabled} onChange={(e) => doiLoai(e.target.value)}>
          {LOAI_CAU_HOI.map((l) => (
            <option key={l.ma} value={l.ma}>{l.nhan}</option>
          ))}
        </select>
      </div>

      {DANG_CHON_PHUONG_AN.includes(loaiCauHoi) && (
        <>
          <div className="qz-tip">
            {loaiCauHoi === 'MOT_DAP_AN' ? 'Chọn vòng tròn ở đáp án đúng duy nhất.' : 'Tích vào tất cả các đáp án đúng.'}
          </div>
          {dapAn.map((d, i) => (
            <div key={i} className="qz-edit-row">
              <input
                type={loaiCauHoi === 'NHIEU_DAP_AN' ? 'checkbox' : 'radio'}
                name={nhom}
                checked={!!d.dapAnDung}
                disabled={disabled}
                title="Đánh dấu là đáp án đúng"
                onChange={() => (loaiCauHoi === 'NHIEU_DAP_AN' ? suaDong(i, { dapAnDung: !d.dapAnDung }) : chonMotDapAnDung(i))}
              />
              <input
                className="form-input"
                type="text"
                placeholder={`Phương án ${i + 1}`}
                value={d.noiDung}
                disabled={disabled}
                onChange={(e) => suaDong(i, { noiDung: e.target.value })}
              />
              {dapAn.length > 2 && (
                <button type="button" className="qz-btn-mui" aria-label="Xóa phương án" disabled={disabled} onClick={() => xoaDong(i)}>
                  <i className="ti ti-trash" aria-hidden="true"></i>
                </button>
              )}
            </div>
          ))}
          <ThemDong disabled={disabled} onClick={themDong}>Thêm phương án</ThemDong>
        </>
      )}

      {loaiCauHoi === 'DUNG_SAI' && (
        <>
          <div className="qz-tip">Chọn vòng tròn ở đáp án đúng.</div>
          {dapAn.map((d, i) => (
            <div key={i} className="qz-edit-row">
              <input
                type="radio"
                name={nhom}
                checked={!!d.dapAnDung}
                disabled={disabled}
                title="Đánh dấu là đáp án đúng"
                onChange={() => chonMotDapAnDung(i)}
              />
              <input className="form-input" type="text" value={d.noiDung} readOnly />
            </div>
          ))}
        </>
      )}

      {loaiCauHoi === 'TRA_LOI_NGAN' && (
        <>
          <div className="qz-tip">
            Học viên tự gõ câu trả lời; đúng nếu khớp một trong các đáp án dưới đây. Không phân biệt hoa/thường và
            khoảng trắng thừa. Dấu tiếng Việt có ý nghĩa — muốn chấp nhận cả cách gõ không dấu, hãy thêm nó thành một đáp án nữa.
          </div>
          {dapAn.map((d, i) => (
            <div key={i} className="qz-edit-row">
              <input
                className="form-input"
                type="text"
                placeholder={i === 0 ? 'Đáp án chính' : 'Cách viết khác được chấp nhận'}
                value={d.noiDung}
                disabled={disabled}
                onChange={(e) => suaDong(i, { noiDung: e.target.value })}
              />
              {dapAn.length > 1 && (
                <button type="button" className="qz-btn-mui" aria-label="Xóa đáp án" disabled={disabled} onClick={() => xoaDong(i)}>
                  <i className="ti ti-trash" aria-hidden="true"></i>
                </button>
              )}
            </div>
          ))}
          <ThemDong disabled={disabled} onClick={themDong}>Thêm cách viết khác</ThemDong>
        </>
      )}

      {loaiCauHoi === 'SAP_XEP' && (
        <>
          <div className="qz-tip">Nhập các mục theo ĐÚNG thứ tự. Học viên sẽ thấy các mục bị xáo trộn và phải sắp xếp lại.</div>
          {dapAn.map((d, i) => (
            <div key={i} className="qz-edit-row">
              <span className="qz-edit-num">{i + 1}</span>
              <input
                className="form-input"
                type="text"
                placeholder={`Mục thứ ${i + 1}`}
                value={d.noiDung}
                disabled={disabled}
                onChange={(e) => suaDong(i, { noiDung: e.target.value })}
              />
              <button type="button" className="qz-btn-mui" aria-label="Chuyển lên" disabled={disabled || i === 0} onClick={() => chuyenDong(i, -1)}>
                <i className="ti ti-arrow-up" aria-hidden="true"></i>
              </button>
              <button type="button" className="qz-btn-mui" aria-label="Chuyển xuống" disabled={disabled || i === dapAn.length - 1} onClick={() => chuyenDong(i, 1)}>
                <i className="ti ti-arrow-down" aria-hidden="true"></i>
              </button>
              {dapAn.length > 2 && (
                <button type="button" className="qz-btn-mui" aria-label="Xóa mục" disabled={disabled} onClick={() => xoaDong(i)}>
                  <i className="ti ti-trash" aria-hidden="true"></i>
                </button>
              )}
            </div>
          ))}
          <ThemDong disabled={disabled} onClick={themDong}>Thêm mục</ThemDong>
        </>
      )}

      {loaiCauHoi === 'GHEP_CAP' && (
        <>
          <div className="qz-tip">Mỗi dòng là một cặp đúng (vế trái ↔ vế phải). Học viên sẽ thấy vế phải bị xáo trộn và phải nối lại.</div>
          {dapAn.map((d, i) => (
            <div key={i} className="qz-edit-row">
              <input
                className="form-input"
                type="text"
                placeholder="Vế trái"
                value={d.noiDung}
                disabled={disabled}
                onChange={(e) => suaDong(i, { noiDung: e.target.value })}
              />
              <i className="ti ti-arrows-left-right qz-edit-noi" aria-hidden="true"></i>
              <input
                className="form-input"
                type="text"
                placeholder="Vế phải"
                value={d.noiDungGhep || ''}
                disabled={disabled}
                onChange={(e) => suaDong(i, { noiDungGhep: e.target.value })}
              />
              {dapAn.length > 2 && (
                <button type="button" className="qz-btn-mui" aria-label="Xóa cặp" disabled={disabled} onClick={() => xoaDong(i)}>
                  <i className="ti ti-trash" aria-hidden="true"></i>
                </button>
              )}
            </div>
          ))}
          <ThemDong disabled={disabled} onClick={themDong}>Thêm cặp</ThemDong>
        </>
      )}
    </>
  );
}

function ThemDong({ onClick, disabled, children }) {
  return (
    <button type="button" className="btn btn-secondary btn-sm" disabled={disabled} onClick={onClick} style={{ marginTop: 4 }}>
      <i className="ti ti-plus" aria-hidden="true"></i> {children}
    </button>
  );
}
