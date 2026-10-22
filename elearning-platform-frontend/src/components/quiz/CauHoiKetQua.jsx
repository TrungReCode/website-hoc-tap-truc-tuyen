import { chuanHoaVanBan } from '../../utils/cauHoi';
import './quiz.css';

const CHU_CAI = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
const khongDoi = { cursor: 'default' };

/**
 * Xem lại 1 câu đã chấm (có đáp án đúng) — dùng chung cho kết quả quiz lớp học và
 * quiz tự học. c = PhanHoiCauHoiDaCham của backend.
 */
export default function CauHoiKetQua({ c, soThuTu }) {
  const loai = c.loaiCauHoi;
  return (
    <div style={{ marginBottom: 14, marginTop: 10 }}>
      <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 6, display: 'flex', alignItems: 'center', gap: 8 }}>
        <i
          className={`ti ${c.dungCauNay ? 'ti-circle-check' : 'ti-circle-x'}`}
          style={{ color: c.dungCauNay ? 'var(--success)' : 'var(--danger)' }}
          aria-hidden="true"
        ></i>
        Câu {soThuTu}: {c.noiDung}
      </div>
      {loai === 'TRA_LOI_NGAN' && <KqTraLoiNgan c={c} />}
      {loai === 'SAP_XEP' && <KqSapXep c={c} />}
      {loai === 'GHEP_CAP' && <KqGhepCap c={c} />}
      {loai !== 'TRA_LOI_NGAN' && loai !== 'SAP_XEP' && loai !== 'GHEP_CAP' && <KqLuaChon c={c} />}
    </div>
  );
}

function KqLuaChon({ c }) {
  return c.dapAn.map((d, di) => {
    const daChon = c.dsMaDapAnDaChon?.includes(d.id);
    const dungDap = d.dapAnDung;
    let cls = 'quiz-option';
    if (daChon && dungDap) cls += ' correct';
    else if (daChon && !dungDap) cls += ' wrong';
    else if (!daChon && dungDap) cls += ' correct';
    return (
      <div key={d.id} className={cls} style={khongDoi}>
        <span className="opt-letter">{CHU_CAI[di] || String(di + 1)}</span>
        {d.noiDung}
        {dungDap && <i className="ti ti-check" style={{ marginLeft: 'auto', color: 'var(--success)' }} aria-hidden="true"></i>}
      </div>
    );
  });
}

function KqTraLoiNgan({ c }) {
  const traLoi = (c.traLoiVanBan || '').trim();
  return (
    <>
      <div className={`quiz-option ${c.dungCauNay ? 'correct' : 'wrong'}`} style={khongDoi}>
        <span className="qz-kq-nhan">Bạn trả lời:</span>
        {traLoi ? c.traLoiVanBan : <em>(bỏ trống)</em>}
      </div>
      {!c.dungCauNay && (
        <div className="quiz-option correct" style={khongDoi}>
          <span className="qz-kq-nhan">Đáp án chấp nhận:</span>
          {c.dapAn.map((d) => d.noiDung).join('  ·  ')}
        </div>
      )}
    </>
  );
}

function KqSapXep({ c }) {
  const dung = c.dapAn.map((d) => d.id);
  const cua = c.dsMaDapAnDaChon || [];
  const theoId = new Map(c.dapAn.map((d) => [d.id, d]));
  return (
    <>
      <div className="qz-tip">Thứ tự của bạn:</div>
      {cua.length === 0 && <div className="quiz-option wrong" style={khongDoi}><em>(bỏ trống)</em></div>}
      {cua.map((id, i) => (
        <div key={`${id}-${i}`} className={`quiz-option ${id === dung[i] ? 'correct' : 'wrong'}`} style={khongDoi}>
          <span className="opt-letter">{i + 1}</span>
          {theoId.get(id)?.noiDung}
        </div>
      ))}
      {!c.dungCauNay && (
        <>
          <div className="qz-tip">Thứ tự đúng:</div>
          {c.dapAn.map((d, i) => (
            <div key={d.id} className="quiz-option correct" style={khongDoi}>
              <span className="opt-letter">{i + 1}</span>
              {d.noiDung}
            </div>
          ))}
        </>
      )}
    </>
  );
}

function KqGhepCap({ c }) {
  const daNoi = new Map((c.capGhepDaNoi || []).map((g) => [g.maDapAn, g.noiDungGhep]));
  return c.dapAn.map((d) => {
    const chon = daNoi.get(d.id);
    const dung = !!chon && chuanHoaVanBan(chon) === chuanHoaVanBan(d.noiDungGhep);
    return (
      <div key={d.id} className={`quiz-option ${dung ? 'correct' : 'wrong'}`} style={khongDoi}>
        <span>{d.noiDung}</span>
        <i className="ti ti-arrow-right" aria-hidden="true"></i>
        <span>{chon || <em>(chưa nối)</em>}</span>
        {!dung && <span className="qz-kq-dung">Đúng: {d.noiDungGhep}</span>}
      </div>
    );
  });
}
