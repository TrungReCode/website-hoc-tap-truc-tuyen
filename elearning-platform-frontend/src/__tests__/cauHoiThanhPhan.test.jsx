import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { useState } from 'react';
import CauHoiLamBai from '../components/quiz/CauHoiLamBai';
import CauHoiKetQua from '../components/quiz/CauHoiKetQua';
import DapAnXemTruoc from '../components/quiz/DapAnXemTruoc';
import BoSoanCauHoi from '../components/quiz/BoSoanCauHoi';
import { cauHoiMoi, kiemTraSoanThao, taoPayloadCauHoi } from '../utils/cauHoi';

afterEach(cleanup);

// bọc để có state thật, giống cách 2 trang làm bài dùng component
function LamBai({ cauHoi, bat }) {
  const [tl, setTl] = useState(undefined);
  return <CauHoiLamBai cauHoi={cauHoi} soThuTu={1} traLoi={tl} onChange={(v) => { setTl(v); bat?.(v); }} />;
}

describe('CauHoiLamBai', () => {
  it('1 đáp án: bấm chọn thay thế lựa chọn cũ', () => {
    const bat = vi.fn();
    const c = { maCauHoi: 1, noiDung: 'Q', loaiCauHoi: 'MOT_DAP_AN', dapAn: [{ id: 1, noiDung: 'Aa' }, { id: 2, noiDung: 'Bb' }] };
    render(<LamBai cauHoi={c} bat={bat} />);
    expect(screen.getByText('Chưa trả lời')).toBeTruthy();
    fireEvent.click(screen.getByText('Aa'));
    fireEvent.click(screen.getByText('Bb'));
    expect(bat).toHaveBeenLastCalledWith(expect.objectContaining({ ids: [2] }));
    expect(screen.queryByText('Chưa trả lời')).toBeNull();
  });

  it('nhiều đáp án: bấm lần 2 thì bỏ chọn', () => {
    const bat = vi.fn();
    const c = { maCauHoi: 1, noiDung: 'Q', loaiCauHoi: 'NHIEU_DAP_AN', dapAn: [{ id: 1, noiDung: 'Aa' }, { id: 2, noiDung: 'Bb' }] };
    render(<LamBai cauHoi={c} bat={bat} />);
    fireEvent.click(screen.getByText('Aa'));
    fireEvent.click(screen.getByText('Bb'));
    expect(bat).toHaveBeenLastCalledWith(expect.objectContaining({ ids: [1, 2] }));
    fireEvent.click(screen.getByText('Aa'));
    expect(bat).toHaveBeenLastCalledWith(expect.objectContaining({ ids: [2] }));
  });

  it('trả lời ngắn: gõ chữ cập nhật vanBan', () => {
    const bat = vi.fn();
    const c = { maCauHoi: 2, noiDung: 'Thủ đô?', loaiCauHoi: 'TRA_LOI_NGAN', dapAn: [] };
    render(<LamBai cauHoi={c} bat={bat} />);
    fireEvent.change(screen.getByPlaceholderText('Nhập câu trả lời của bạn'), { target: { value: 'Hà Nội' } });
    expect(bat).toHaveBeenLastCalledWith(expect.objectContaining({ vanBan: 'Hà Nội' }));
    expect(screen.queryByText('Chưa trả lời')).toBeNull();
  });

  it('sắp xếp: chưa đụng thì chưa tính trả lời; nút chuyển đổi chỗ đúng; có thể xác nhận thứ tự hiện tại', () => {
    const bat = vi.fn();
    // thứ tự server gửi (đã xáo): z, x, y với id 2,3,1
    const c = { maCauHoi: 3, noiDung: 'Xếp', loaiCauHoi: 'SAP_XEP', dapAn: [{ id: 2, noiDung: 'z' }, { id: 3, noiDung: 'x' }, { id: 1, noiDung: 'y' }] };
    render(<LamBai cauHoi={c} bat={bat} />);
    expect(screen.getByText('Chưa trả lời')).toBeTruthy();
    // nút lên của mục đầu bị khóa, nút xuống của mục cuối bị khóa
    expect(screen.getAllByLabelText('Chuyển lên')[0].disabled).toBe(true);
    expect(screen.getAllByLabelText('Chuyển xuống')[2].disabled).toBe(true);
    fireEvent.click(screen.getAllByLabelText('Chuyển xuống')[0]); // z xuống -> x, z, y
    expect(bat).toHaveBeenLastCalledWith(expect.objectContaining({ ids: [3, 2, 1] }));
    expect(screen.queryByText('Chưa trả lời')).toBeNull();
    expect(screen.queryByText('Giữ thứ tự hiện tại')).toBeNull();
  });

  it('sắp xếp: "Giữ thứ tự hiện tại" xác nhận đúng thứ tự đang hiển thị', () => {
    const bat = vi.fn();
    const c = { maCauHoi: 3, noiDung: 'Xếp', loaiCauHoi: 'SAP_XEP', dapAn: [{ id: 2, noiDung: 'z' }, { id: 3, noiDung: 'x' }] };
    render(<LamBai cauHoi={c} bat={bat} />);
    fireEvent.click(screen.getByText('Giữ thứ tự hiện tại'));
    expect(bat).toHaveBeenLastCalledWith(expect.objectContaining({ ids: [2, 3] }));
  });

  it('ghép cặp: chọn vế phải; mục đã dùng ở dòng khác bị khóa; chỉ tính đã trả lời khi đủ', () => {
    const bat = vi.fn();
    const c = { maCauHoi: 4, noiDung: 'Ghép', loaiCauHoi: 'GHEP_CAP',
      dapAn: [{ id: 1, noiDung: 'Java' }, { id: 2, noiDung: 'Spring' }], luaChonGhep: ['Framework', 'Ngôn ngữ'] };
    render(<LamBai cauHoi={c} bat={bat} />);
    const o1 = screen.getByLabelText('Ghép với: Java');
    const o2 = screen.getByLabelText('Ghép với: Spring');
    fireEvent.change(o1, { target: { value: 'Ngôn ngữ' } });
    expect(bat).toHaveBeenLastCalledWith(expect.objectContaining({ capGhep: { 1: 'Ngôn ngữ' } }));
    expect(screen.getByText('Chưa trả lời')).toBeTruthy(); // mới nối 1/2
    // ở select của dòng 2, "Ngôn ngữ" đã bị dòng 1 dùng nên khóa
    const opt = [...o2.querySelectorAll('option')].find((o) => o.value === 'Ngôn ngữ');
    expect(opt.disabled).toBe(true);
    fireEvent.change(o2, { target: { value: 'Framework' } });
    expect(bat).toHaveBeenLastCalledWith(expect.objectContaining({ capGhep: { 1: 'Ngôn ngữ', 2: 'Framework' } }));
    expect(screen.queryByText('Chưa trả lời')).toBeNull();
    // bỏ chọn dòng 1
    fireEvent.change(o1, { target: { value: '' } });
    expect(bat).toHaveBeenLastCalledWith(expect.objectContaining({ capGhep: { 2: 'Framework' } }));
  });
});

describe('CauHoiKetQua', () => {
  const base = { maCauHoi: 1, noiDung: 'Q' };
  it('trả lời ngắn sai: hiện câu của học viên và đáp án chấp nhận', () => {
    render(<CauHoiKetQua soThuTu={1} c={{ ...base, loaiCauHoi: 'TRA_LOI_NGAN', dungCauNay: false, traLoiVanBan: 'Huế',
      dapAn: [{ id: 1, noiDung: 'Hà Nội' }, { id: 2, noiDung: 'Ha Noi' }], dsMaDapAnDaChon: [] }} />);
    expect(screen.getByText('Huế')).toBeTruthy();
    expect(screen.getByText(/Hà Nội\s+·\s+Ha Noi/)).toBeTruthy();
  });
  it('trả lời ngắn bỏ trống', () => {
    render(<CauHoiKetQua soThuTu={1} c={{ ...base, loaiCauHoi: 'TRA_LOI_NGAN', dungCauNay: false, traLoiVanBan: '  ',
      dapAn: [{ id: 1, noiDung: 'A' }], dsMaDapAnDaChon: [] }} />);
    expect(screen.getByText('(bỏ trống)')).toBeTruthy();
  });
  it('sắp xếp sai: hiện thứ tự của bạn và thứ tự đúng', () => {
    const { container } = render(<CauHoiKetQua soThuTu={1} c={{ ...base, loaiCauHoi: 'SAP_XEP', dungCauNay: false,
      dapAn: [{ id: 1, noiDung: 'một' }, { id: 2, noiDung: 'hai' }], dsMaDapAnDaChon: [2, 1] }} />);
    expect(screen.getByText('Thứ tự đúng:')).toBeTruthy();
    expect(container.querySelectorAll('.quiz-option.wrong').length).toBe(2);
    expect(container.querySelectorAll('.quiz-option.correct').length).toBe(2);
  });
  it('sắp xếp đúng: không hiện khối thứ tự đúng', () => {
    render(<CauHoiKetQua soThuTu={1} c={{ ...base, loaiCauHoi: 'SAP_XEP', dungCauNay: true,
      dapAn: [{ id: 1, noiDung: 'một' }, { id: 2, noiDung: 'hai' }], dsMaDapAnDaChon: [1, 2] }} />);
    expect(screen.queryByText('Thứ tự đúng:')).toBeNull();
  });
  it('ghép cặp: tô đúng/sai từng cặp và hiện vế đúng cho cặp sai', () => {
    const { container } = render(<CauHoiKetQua soThuTu={1} c={{ ...base, loaiCauHoi: 'GHEP_CAP', dungCauNay: false,
      dapAn: [{ id: 1, noiDung: 'Java', noiDungGhep: 'Ngôn ngữ' }, { id: 2, noiDung: 'Spring', noiDungGhep: 'Framework' }],
      dsMaDapAnDaChon: [], capGhepDaNoi: [{ maDapAn: 1, noiDungGhep: ' ngôn ngữ ' }, { maDapAn: 2, noiDungGhep: 'Ngôn ngữ' }] }} />);
    expect(container.querySelectorAll('.quiz-option.correct').length).toBe(1);
    expect(container.querySelectorAll('.quiz-option.wrong').length).toBe(1);
    expect(screen.getByText('Đúng: Framework')).toBeTruthy();
  });
  it('dạng chọn (và kết quả cũ không có loaiCauHoi) vẫn hiển thị như trước', () => {
    const { container } = render(<CauHoiKetQua soThuTu={2} c={{ ...base, loaiCauHoi: null, dungCauNay: false,
      dapAn: [{ id: 1, noiDung: 'A', dapAnDung: true }, { id: 2, noiDung: 'B', dapAnDung: false }], dsMaDapAnDaChon: [2] }} />);
    expect(container.querySelectorAll('.quiz-option.wrong').length).toBe(1);
    expect(container.querySelectorAll('.quiz-option.correct').length).toBe(1);
  });
});

describe('DapAnXemTruoc (chống lộ khi đáp án bị che)', () => {
  it('trả lời ngắn: học viên không thấy đáp án; giảng viên thấy', () => {
    const { container, rerender } = render(<DapAnXemTruoc cauHoi={{ loaiCauHoi: 'TRA_LOI_NGAN', dapAn: [] }} />);
    expect(container.textContent).toContain('Học viên tự gõ');
    rerender(<DapAnXemTruoc cauHoi={{ loaiCauHoi: 'TRA_LOI_NGAN', dapAn: [{ id: 1, noiDung: 'Hà Nội', dapAnDung: true }] }} />);
    expect(container.textContent).toContain('Đáp án chấp nhận: Hà Nội');
  });
  it('sắp xếp: bị che thì không đánh số thứ tự; lộ thì đánh số', () => {
    const { container, rerender } = render(<DapAnXemTruoc cauHoi={{ loaiCauHoi: 'SAP_XEP', dapAn: [{ id: 2, noiDung: 'b', dapAnDung: null }, { id: 1, noiDung: 'a', dapAnDung: null }] }} />);
    expect(container.textContent).not.toMatch(/1\.\s*b/);
    rerender(<DapAnXemTruoc cauHoi={{ loaiCauHoi: 'SAP_XEP', dapAn: [{ id: 1, noiDung: 'a', dapAnDung: true }, { id: 2, noiDung: 'b', dapAnDung: true }] }} />);
    expect(container.textContent).toContain('1. a');
    expect(container.textContent).toContain('2. b');
  });
  it('ghép cặp: bị che chỉ thấy vế trái và danh sách vế phải riêng; lộ thì thấy cặp', () => {
    const { container, rerender } = render(<DapAnXemTruoc cauHoi={{ loaiCauHoi: 'GHEP_CAP', luaChonGhep: ['Y', 'X'], dapAn: [{ id: 1, noiDung: 'A', dapAnDung: null }] }} />);
    expect(container.textContent).toContain('Vế phải: Y');
    expect(container.querySelectorAll('.correct').length).toBe(0);
    rerender(<DapAnXemTruoc cauHoi={{ loaiCauHoi: 'GHEP_CAP', dapAn: [{ id: 1, noiDung: 'A', noiDungGhep: 'X', dapAnDung: true }] }} />);
    expect(container.querySelectorAll('.correct').length).toBe(1);
  });
  it('dạng chọn: đánh dấu đáp án đúng khi lộ, không đánh dấu khi bị che', () => {
    const { container, rerender } = render(<DapAnXemTruoc cauHoi={{ dapAn: [{ id: 1, noiDung: 'A', dapAnDung: true }, { id: 2, noiDung: 'B', dapAnDung: false }] }} />);
    expect(container.querySelectorAll('.correct').length).toBe(1);
    rerender(<DapAnXemTruoc cauHoi={{ dapAn: [{ id: 1, noiDung: 'A', dapAnDung: null }, { id: 2, noiDung: 'B', dapAnDung: null }] }} />);
    expect(container.querySelectorAll('.correct').length).toBe(0);
  });
});

function Soan({ khoiTao, ra }) {
  const [v, setV] = useState(khoiTao || cauHoiMoi());
  ra.hienTai = v;
  return <BoSoanCauHoi value={v} onChange={setV} />;
}

describe('BoSoanCauHoi', () => {
  it('soạn câu ghép cặp hoàn chỉnh rồi dựng payload gửi backend', () => {
    const ra = {};
    render(<Soan ra={ra} />);
    fireEvent.change(screen.getByPlaceholderText('Nhập câu hỏi'), { target: { value: 'Nối công nghệ' } });
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'GHEP_CAP' } });
    expect(ra.hienTai.dapAn).toHaveLength(3);
    const trai = screen.getAllByPlaceholderText('Vế trái');
    const phai = screen.getAllByPlaceholderText('Vế phải');
    ['Java', 'Spring', 'Postgres'].forEach((t, i) => fireEvent.change(trai[i], { target: { value: t } }));
    ['Ngôn ngữ', 'Framework', 'CSDL'].forEach((t, i) => fireEvent.change(phai[i], { target: { value: t } }));
    expect(kiemTraSoanThao(ra.hienTai)).toBe('');
    const p = taoPayloadCauHoi(ra.hienTai, 1);
    expect(p.loaiCauHoi).toBe('GHEP_CAP');
    expect(p.dapAn.map((d) => [d.noiDung, d.noiDungGhep])).toEqual([['Java', 'Ngôn ngữ'], ['Spring', 'Framework'], ['Postgres', 'CSDL']]);
    // thêm / xóa cặp
    fireEvent.click(screen.getByText('Thêm cặp'));
    expect(ra.hienTai.dapAn).toHaveLength(4);
    fireEvent.click(screen.getAllByLabelText('Xóa cặp')[3]);
    expect(ra.hienTai.dapAn).toHaveLength(3);
  });

  it('soạn câu sắp xếp: thứ tự nhập = thứ tự đúng, đổi chỗ bằng nút', () => {
    const ra = {};
    render(<Soan ra={ra} khoiTao={cauHoiMoi('SAP_XEP')} />);
    fireEvent.change(screen.getByPlaceholderText('Nhập câu hỏi'), { target: { value: 'Xếp' } });
    ['một', 'hai', 'ba'].forEach((t, i) => fireEvent.change(screen.getByPlaceholderText(`Mục thứ ${i + 1}`), { target: { value: t } }));
    fireEvent.click(screen.getAllByLabelText('Chuyển xuống')[0]); // hai, một, ba
    expect(ra.hienTai.dapAn.map((d) => d.noiDung)).toEqual(['hai', 'một', 'ba']);
    expect(taoPayloadCauHoi(ra.hienTai, 1).dapAn.map((d) => d.id)).toEqual([1, 2, 3]);
  });

  it('chuyển 1 đáp án <-> nhiều đáp án giữ nội dung; sang 1 đáp án chỉ giữ đáp án đúng đầu tiên', () => {
    const ra = {};
    render(<Soan ra={ra} khoiTao={{ noiDung: 'Q', loaiCauHoi: 'NHIEU_DAP_AN', dapAn: [
      { noiDung: 'a', dapAnDung: true }, { noiDung: 'b', dapAnDung: true }, { noiDung: 'c', dapAnDung: false }] }} />);
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'MOT_DAP_AN' } });
    expect(ra.hienTai.dapAn.map((d) => d.noiDung)).toEqual(['a', 'b', 'c']);
    expect(ra.hienTai.dapAn.map((d) => d.dapAnDung)).toEqual([true, false, false]);
    expect(kiemTraSoanThao(ra.hienTai)).toBe('');
  });

  it('chuyển sang dạng khác thì làm lại danh sách mặc định; Đúng/Sai có sẵn 2 dòng cố định', () => {
    const ra = {};
    render(<Soan ra={ra} />);
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'DUNG_SAI' } });
    expect(ra.hienTai.dapAn.map((d) => d.noiDung)).toEqual(['Đúng', 'Sai']);
    expect(ra.hienTai.dapAn.filter((d) => d.dapAnDung)).toHaveLength(1);
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'TRA_LOI_NGAN' } });
    expect(ra.hienTai.dapAn).toHaveLength(1);
  });

  it('trả lời ngắn: thêm cách viết khác, không cho xóa đáp án cuối cùng', () => {
    const ra = {};
    render(<Soan ra={ra} khoiTao={cauHoiMoi('TRA_LOI_NGAN')} />);
    expect(screen.queryByLabelText('Xóa đáp án')).toBeNull();
    fireEvent.click(screen.getByText('Thêm cách viết khác'));
    expect(ra.hienTai.dapAn).toHaveLength(2);
    expect(screen.getAllByLabelText('Xóa đáp án')).toHaveLength(2);
  });
});
