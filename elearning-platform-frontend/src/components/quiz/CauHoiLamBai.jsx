import { daTraLoi, traLoiTrong } from '../../utils/cauHoi';
import './quiz.css';

const CHU_CAI = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

/**
 * Hiển thị 1 câu hỏi cho học viên đang làm bài — dùng chung cho quiz của lớp học
 * (đề gốc, đề bổ sung) và quiz tự học. Tự chọn giao diện theo cauHoi.loaiCauHoi:
 * chọn phương án, trả lời ngắn, sắp xếp, ghép cặp.
 *
 * traLoi: { ids, vanBan, capGhep } (xem utils/cauHoi.js). onChange nhận đối tượng
 * traLoi MỚI của câu này (không phải chỉ phần thay đổi).
 */
export default function CauHoiLamBai({ cauHoi, soThuTu, traLoi, onChange }) {
  const tl = traLoi || traLoiTrong();
  const xong = daTraLoi(cauHoi, traLoi);
  const loai = cauHoi.loaiCauHoi;

  return (
    <div id={`cau-hoi-${cauHoi.maCauHoi}`} className="card sm" style={{ marginBottom: 12, scrollMarginTop: 140 }}>
      <div style={{ fontSize: 14, fontWeight: 500, marginBottom: 10, display: 'flex', gap: 8 }}>
        <span>{soThuTu}. {cauHoi.noiDung}</span>
        {!xong && <span className="badge badge-amber" style={{ flexShrink: 0 }}>Chưa trả lời</span>}
      </div>
      {loai === 'TRA_LOI_NGAN' && <TraLoiNgan tl={tl} onChange={onChange} />}
      {loai === 'SAP_XEP' && <SapXep cauHoi={cauHoi} tl={tl} onChange={onChange} />}
      {loai === 'GHEP_CAP' && <GhepCap cauHoi={cauHoi} tl={tl} onChange={onChange} />}
      {loai !== 'TRA_LOI_NGAN' && loai !== 'SAP_XEP' && loai !== 'GHEP_CAP' && (
        <LuaChon cauHoi={cauHoi} tl={tl} onChange={onChange} />
      )}
    </div>
  );
}

function LuaChon({ cauHoi, tl, onChange }) {
  const nhieu = cauHoi.loaiCauHoi === 'NHIEU_DAP_AN';
  const daChon = tl.ids || [];

  function chon(maDapAn) {
    const ids = nhieu
      ? (daChon.includes(maDapAn) ? daChon.filter((x) => x !== maDapAn) : [...daChon, maDapAn])
      : [maDapAn];
    onChange({ ...tl, ids });
  }

  return (
    <>
      {nhieu && <div className="qz-tip">Chọn tất cả các đáp án đúng.</div>}
      {cauHoi.dapAn.map((d, di) => {
        const daChonNay = daChon.includes(d.id);
        return (
          <div
            key={d.id}
            className={`quiz-option ${daChonNay ? 'selected' : ''}`}
            role={nhieu ? 'checkbox' : 'radio'}
            aria-checked={daChonNay}
            tabIndex={0}
            onClick={() => chon(d.id)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                chon(d.id);
              }
            }}
          >
            <span className="opt-letter">{CHU_CAI[di] || String(di + 1)}</span>
            {d.noiDung}
          </div>
        );
      })}
    </>
  );
}

function TraLoiNgan({ tl, onChange }) {
  return (
    <>
      <input
        className="form-input"
        type="text"
        maxLength={1000}
        placeholder="Nhập câu trả lời của bạn"
        value={tl.vanBan || ''}
        onChange={(e) => onChange({ ...tl, vanBan: e.target.value })}
      />
      <div className="qz-tip" style={{ marginTop: 6 }}>Không phân biệt chữ hoa/thường. Nhớ gõ đúng dấu tiếng Việt.</div>
    </>
  );
}

function SapXep({ cauHoi, tl, onChange }) {
  const daSapXep = (tl.ids || []).length === cauHoi.dapAn.length;
  const thuTu = daSapXep ? tl.ids : cauHoi.dapAn.map((d) => d.id);
  const theoId = new Map(cauHoi.dapAn.map((d) => [d.id, d]));

  function chuyen(i, huong) {
    const j = i + huong;
    if (j < 0 || j >= thuTu.length) return;
    const moi = [...thuTu];
    [moi[i], moi[j]] = [moi[j], moi[i]];
    onChange({ ...tl, ids: moi });
  }

  return (
    <>
      <div className="qz-tip">Dùng nút mũi tên để sắp xếp các mục theo đúng thứ tự.</div>
      {thuTu.map((id, i) => (
        <div key={id} className="qz-sx-item">
          <span className="qz-sx-num">{i + 1}</span>
          <span>{theoId.get(id)?.noiDung}</span>
          <span className="qz-sx-btns">
            <button type="button" className="qz-btn-mui" aria-label="Chuyển lên" disabled={i === 0} onClick={() => chuyen(i, -1)}>
              <i className="ti ti-arrow-up" aria-hidden="true"></i>
            </button>
            <button type="button" className="qz-btn-mui" aria-label="Chuyển xuống" disabled={i === thuTu.length - 1} onClick={() => chuyen(i, 1)}>
              <i className="ti ti-arrow-down" aria-hidden="true"></i>
            </button>
          </span>
        </div>
      ))}
      {!daSapXep && (
        <button type="button" className="btn btn-secondary btn-sm" onClick={() => onChange({ ...tl, ids: thuTu })}>
          Giữ thứ tự hiện tại
        </button>
      )}
    </>
  );
}

function GhepCap({ cauHoi, tl, onChange }) {
  const capGhep = tl.capGhep || {};
  const luaChon = cauHoi.luaChonGhep || [];
  const daDung = new Set(Object.values(capGhep).filter(Boolean));

  function chon(maDapAn, giaTri) {
    const moi = { ...capGhep };
    if (giaTri) moi[maDapAn] = giaTri;
    else delete moi[maDapAn];
    onChange({ ...tl, capGhep: moi });
  }

  return (
    <>
      <div className="qz-tip">Chọn mục ở vế phải tương ứng với từng mục ở vế trái (mỗi mục vế phải chỉ dùng một lần).</div>
      {cauHoi.dapAn.map((d) => (
        <div key={d.id} className="qz-gc-row">
          <div className="qz-gc-trai">{d.noiDung}</div>
          <i className="ti ti-arrow-right" aria-hidden="true"></i>
          <select
            className="form-input qz-gc-select"
            aria-label={`Ghép với: ${d.noiDung}`}
            value={capGhep[d.id] || ''}
            onChange={(e) => chon(d.id, e.target.value)}
          >
            <option value="">— Chọn —</option>
            {luaChon.map((t) => (
              <option key={t} value={t} disabled={daDung.has(t) && capGhep[d.id] !== t}>{t}</option>
            ))}
          </select>
        </div>
      ))}
    </>
  );
}
