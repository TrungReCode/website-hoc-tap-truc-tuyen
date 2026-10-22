import { describe, it, expect } from 'vitest';
import { daTraLoi, taoYeuCauTraLoi, kiemTraSoanThao, taoPayloadCauHoi, dapAnMacDinh, cauHoiMoi, traLoiTrong, chuanHoaVanBan } from '../utils/cauHoi';

const cauGhep = { maCauHoi: 7, loaiCauHoi: 'GHEP_CAP', dapAn: [{ id: 1, noiDung: 'Java' }, { id: 2, noiDung: 'Spring' }] };
const cauXep = { maCauHoi: 8, loaiCauHoi: 'SAP_XEP', dapAn: [{ id: 3, noiDung: 'x' }, { id: 1, noiDung: 'y' }, { id: 2, noiDung: 'z' }] };

describe('daTraLoi / taoYeuCauTraLoi', () => {
  it('chọn phương án', () => {
    const c = { maCauHoi: 1, loaiCauHoi: 'MOT_DAP_AN', dapAn: [{ id: 1 }, { id: 2 }] };
    expect(daTraLoi(c, undefined)).toBe(false);
    expect(daTraLoi(c, traLoiTrong())).toBe(false);
    expect(daTraLoi(c, { ids: [2] })).toBe(true);
    expect(taoYeuCauTraLoi(c, { ids: [2] })).toEqual({ maCauHoi: 1, dsMaDapAnDaChon: [2] });
    expect(taoYeuCauTraLoi(c, undefined)).toEqual({ maCauHoi: 1, dsMaDapAnDaChon: [] });
  });
  it('trả lời ngắn', () => {
    const c = { maCauHoi: 2, loaiCauHoi: 'TRA_LOI_NGAN', dapAn: [] };
    expect(daTraLoi(c, { vanBan: '   ' })).toBe(false);
    expect(daTraLoi(c, { vanBan: 'Hà Nội' })).toBe(true);
    expect(taoYeuCauTraLoi(c, { vanBan: 'Hà Nội' })).toEqual({ maCauHoi: 2, dsMaDapAnDaChon: [], traLoiVanBan: 'Hà Nội' });
  });
  it('sắp xếp: chỉ tính đã trả lời khi đủ mục', () => {
    expect(daTraLoi(cauXep, { ids: [3, 1] })).toBe(false);
    expect(daTraLoi(cauXep, { ids: [3, 1, 2] })).toBe(true);
    expect(taoYeuCauTraLoi(cauXep, { ids: [3, 1, 2] }).dsMaDapAnDaChon).toEqual([3, 1, 2]);
  });
  it('ghép cặp: chỉ tính đã trả lời khi nối đủ, và dựng đúng danh sách cặp', () => {
    expect(daTraLoi(cauGhep, { capGhep: { 1: 'Ngôn ngữ' } })).toBe(false);
    expect(daTraLoi(cauGhep, { capGhep: { 1: 'Ngôn ngữ', 2: 'Framework' } })).toBe(true);
    expect(taoYeuCauTraLoi(cauGhep, { capGhep: { 2: 'Framework', 1: 'Ngôn ngữ' } })).toEqual({
      maCauHoi: 7, dsMaDapAnDaChon: [],
      capGhep: [{ maDapAn: 1, noiDungGhep: 'Ngôn ngữ' }, { maDapAn: 2, noiDungGhep: 'Framework' }],
    });
  });
});

describe('kiemTraSoanThao', () => {
  const ok = (cau) => expect(kiemTraSoanThao(cau)).toBe('');
  const loi = (cau) => expect(kiemTraSoanThao(cau)).not.toBe('');
  it('chấp nhận câu hợp lệ cả 6 dạng', () => {
    ok({ noiDung: 'Q', loaiCauHoi: 'MOT_DAP_AN', dapAn: [{ noiDung: 'a', dapAnDung: true }, { noiDung: 'b', dapAnDung: false }] });
    ok({ noiDung: 'Q', loaiCauHoi: 'NHIEU_DAP_AN', dapAn: [{ noiDung: 'a', dapAnDung: true }, { noiDung: 'b', dapAnDung: true }] });
    ok({ noiDung: 'Q', loaiCauHoi: 'DUNG_SAI', dapAn: dapAnMacDinh('DUNG_SAI') });
    ok({ noiDung: 'Q', loaiCauHoi: 'TRA_LOI_NGAN', dapAn: [{ noiDung: 'x', dapAnDung: true }] });
    ok({ noiDung: 'Q', loaiCauHoi: 'SAP_XEP', dapAn: [{ noiDung: 'a' }, { noiDung: 'b' }] });
    ok({ noiDung: 'Q', loaiCauHoi: 'GHEP_CAP', dapAn: [{ noiDung: 'a', noiDungGhep: '1' }, { noiDung: 'b', noiDungGhep: '2' }] });
  });
  it('từ chối câu không hợp lệ', () => {
    loi({ noiDung: ' ', loaiCauHoi: 'MOT_DAP_AN', dapAn: [{ noiDung: 'a', dapAnDung: true }, { noiDung: 'b' }] });
    loi({ noiDung: 'Q', loaiCauHoi: 'MOT_DAP_AN', dapAn: [{ noiDung: 'a', dapAnDung: true }, { noiDung: 'b', dapAnDung: true }] });
    loi({ noiDung: 'Q', loaiCauHoi: 'MOT_DAP_AN', dapAn: [{ noiDung: 'a' }, { noiDung: 'b' }] });
    loi({ noiDung: 'Q', loaiCauHoi: 'DUNG_SAI', dapAn: [{ noiDung: 'Đúng', dapAnDung: true }, { noiDung: 'Sai', dapAnDung: false }, { noiDung: 'Không chắc', dapAnDung: false }] });
    loi({ noiDung: 'Q', loaiCauHoi: 'NHIEU_DAP_AN', dapAn: [{ noiDung: 'a' }, { noiDung: 'b' }] });
    loi({ noiDung: 'Q', loaiCauHoi: 'TRA_LOI_NGAN', dapAn: [{ noiDung: '', dapAnDung: true }] });
    loi({ noiDung: 'Q', loaiCauHoi: 'SAP_XEP', dapAn: [{ noiDung: 'a' }, { noiDung: ' A ' }] });
    loi({ noiDung: 'Q', loaiCauHoi: 'GHEP_CAP', dapAn: [{ noiDung: 'a', noiDungGhep: '1' }, { noiDung: 'b', noiDungGhep: '1' }] });
    loi({ noiDung: 'Q', loaiCauHoi: 'GHEP_CAP', dapAn: [{ noiDung: 'a', noiDungGhep: '' }, { noiDung: 'b', noiDungGhep: '2' }] });
    loi({ noiDung: 'Q', loaiCauHoi: 'GHEP_CAP', dapAn: [{ noiDung: 'a', noiDungGhep: '1' }] });
  });
});

describe('taoPayloadCauHoi', () => {
  it('đánh lại id 1..n và cờ đúng theo dạng', () => {
    const p = taoPayloadCauHoi({ noiDung: ' Q ', loaiCauHoi: 'SAP_XEP', dapAn: [{ noiDung: ' b ', dapAnDung: false }, { noiDung: 'c' }] }, 3);
    expect(p).toEqual({ noiDung: 'Q', viTri: 3, loaiCauHoi: 'SAP_XEP', dapAn: [{ id: 1, noiDung: 'b', dapAnDung: true }, { id: 2, noiDung: 'c', dapAnDung: true }] });
  });
  it('ghép cặp có noiDungGhep, chọn phương án thì không', () => {
    const g = taoPayloadCauHoi({ noiDung: 'Q', loaiCauHoi: 'GHEP_CAP', dapAn: [{ noiDung: 'a', noiDungGhep: ' 1 ' }, { noiDung: 'b', noiDungGhep: '2' }] }, 1);
    expect(g.dapAn[0]).toEqual({ id: 1, noiDung: 'a', dapAnDung: true, noiDungGhep: '1' });
    const m = taoPayloadCauHoi({ noiDung: 'Q', loaiCauHoi: 'MOT_DAP_AN', dapAn: [{ noiDung: 'a', dapAnDung: true }, { noiDung: 'b', dapAnDung: false }] }, 1);
    expect('noiDungGhep' in m.dapAn[0]).toBe(false);
    expect(m.dapAn.map((d) => d.dapAnDung)).toEqual([true, false]);
  });
  it('khởi tạo mặc định hợp lệ về cấu trúc', () => {
    expect(cauHoiMoi().loaiCauHoi).toBe('MOT_DAP_AN');
    expect(dapAnMacDinh('GHEP_CAP')).toHaveLength(3);
  });
  it('chuẩn hóa văn bản khớp backend', () => {
    expect(chuanHoaVanBan('  Hà   NỘI ')).toBe('hà nội');
    expect(chuanHoaVanBan('Ha\u0300')).toBe(chuanHoaVanBan('Hà'));
  });
});
