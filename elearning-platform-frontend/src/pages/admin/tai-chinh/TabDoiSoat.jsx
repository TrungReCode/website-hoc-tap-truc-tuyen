import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { layThongBaoLoi } from '../../../utils/layThongBaoLoi';

import {
    layDanhSachPhieuDoiSoat,
    capNhatPhieuDoiSoat,
    capNhatHangLoatPhieuDoiSoat,
    layGiaoDichTheoDoiSoat,
    taiXuongCsvDoiSoat,
    layTongHopDoiSoatTheoGiangVien,
    layChiTietDoiSoatTheoGiangVien,
} from '../../../services/taiChinhService';

import NganKeoChiTiet from '../../../components/NganKeoChiTiet';
import BoLocThoiGianNhanh from '../../../components/BoLocThoiGianNhanh';

const dinhDangTien = (n) =>
    new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
    }).format(n || 0);

function formatNgay(value) {
    if (!value) return 'N/A';

    return new Intl.DateTimeFormat('vi-VN', {
        dateStyle: 'short',
        timeStyle: 'short',
    }).format(new Date(value));
}

function dichTrangThaiDoiSoat(trangThai) {
    const banDo = {
        CHO_XAC_NHAN: 'Chờ xác nhận',
        DANG_XU_LY: 'Đang xử lý',
        HOAN_TAT: 'Hoàn tất',
        THAT_BAI: 'Thất bại',
    };

    return banDo[trangThai] || trangThai || 'Không rõ';
}

function layMauTrangThaiDoiSoat(trangThai) {
    switch (trangThai) {
        case 'HOAN_TAT':
            return 'green';
        case 'DANG_XU_LY':
        case 'CHO_XAC_NHAN':
            return 'amber';
        case 'THAT_BAI':
            return 'red';
        default:
            return 'gray';
    }
}

// Yêu cầu: chỉ hiện phiếu đối soát có giá trị, ẩn hẳn phiếu giá 0 (thường do
// giao dịch khuyến mãi/tặng miễn phí không phát sinh doanh thu thật để đối
// soát) — dùng CHUNG ở cả bảng hiển thị, checkbox "chọn tất cả" và xuất CSV,
// để 3 nơi luôn nhất quán (không chọn/xuất được phiếu đang bị ẩn trên bảng).
function locPhieuCoGia(danhSachPhieu) {
    return (danhSachPhieu || []).filter((p) => Number(p.tongDoanhThu) > 0);
}
function locGiangVienCoGia(danhSach) {
    return (danhSach || []).filter(
        (gv) => Number(gv.tongDoanhThu) > 0
    );
}

export default function TabDoiSoat({ onThayDoi }) {
    // FIX: đọc trạng thái ban đầu từ query string (?trangThai=...) — cho phép
    // trang Tổng quan điều hướng thẳng tới đúng bộ lọc (VD: "Phiếu đối soát
    // chờ" → tab=doi-soat&trangThai=CHO_XAC_NHAN).
    const [searchParams] = useSearchParams();
    const [danhSach, setDanhSach] = useState(null);
    const [dangTai, setDangTai] = useState(true);
    const [loi, setLoi] = useState('');

    const [trangThaiLoc, setTrangThaiLoc] = useState(searchParams.get('trangThai') || '');
    const [oTimKiem, setOTimKiem] = useState('');
    const [timKiem, setTimKiem] = useState('');

    const [tuNgayLoc, setTuNgayLoc] = useState('');
    const [denNgayLoc, setDenNgayLoc] = useState('');

    const [trang, setTrang] = useState(0);

    const [dsChon, setDsChon] = useState(new Set());

    const [dangXuLyHangLoat, setDangXuLyHangLoat] = useState(false);
    const [dangXuatCsvToanBo, setDangXuatCsvToanBo] = useState(false);

    const [phieuXemChiTiet, setPhieuXemChiTiet] = useState(null);

    // ===== Chế độ xem =====
    const [cheDoXem, setCheDoXem] = useState('phieu');

    const [tongHopGV, setTongHopGV] = useState([]);
    const [dangTaiTongHop, setDangTaiTongHop] = useState(false);

    const [maGiangVienDangXuLy, setMaGiangVienDangXuLy] =
        useState(null);

    const [giangVienXemChiTiet, setGiangVienXemChiTiet] =
        useState(null);

    // Reset trang khi thay đổi bộ lọc
    useEffect(() => {
        setTrang(0);
    }, [trangThaiLoc, timKiem, tuNgayLoc, denNgayLoc]);

    // ===== Tải danh sách phiếu =====
    useEffect(() => {
        let daHuy = false;

        setDangTai(true);

        layDanhSachPhieuDoiSoat({
            trangThai: trangThaiLoc,
            timKiem,
            tuNgay: tuNgayLoc,
            denNgay: denNgayLoc,
            page: trang,
            size: 10,
        })
            .then((d) => {
                if (!daHuy) {
                    setDanhSach(d);
                    setDsChon(new Set());
                }
            })
            .catch((err) => {
                if (!daHuy) {
                    setLoi(layThongBaoLoi(err));
                }
            })
            .finally(() => {
                if (!daHuy) {
                    setDangTai(false);
                }
            });

        return () => {
            daHuy = true;
        };
    }, [trang, trangThaiLoc, timKiem, tuNgayLoc, denNgayLoc]);

    // ===== Tải báo cáo tổng hợp giảng viên =====
    useEffect(() => {
        if (cheDoXem !== 'giang-vien') return;

        let daHuy = false;

        setDangTaiTongHop(true);

        layTongHopDoiSoatTheoGiangVien({
            trangThai: trangThaiLoc,
            timKiem,
            tuNgay: tuNgayLoc,
            denNgay: denNgayLoc,
        })
            .then((d) => {
                if (!daHuy) {
                    setTongHopGV(locGiangVienCoGia(d));
                }
            })
            .catch((err) => {
                if (!daHuy) {
                    setLoi(layThongBaoLoi(err));
                }
            })
            .finally(() => {
                if (!daHuy) {
                    setDangTaiTongHop(false);
                }
            });

        return () => {
            daHuy = true;
        };
    }, [
        cheDoXem,
        trangThaiLoc,
        timKiem,
        tuNgayLoc,
        denNgayLoc,
    ]);

    async function taiLai() {
        const d = await layDanhSachPhieuDoiSoat({
            trangThai: trangThaiLoc,
            timKiem,
            tuNgay: tuNgayLoc,
            denNgay: denNgayLoc,
            page: trang,
            size: 10,
        });

        setDanhSach(d);
    }

    async function taiLaiTongHopGV() {
        const d = await layTongHopDoiSoatTheoGiangVien({
            trangThai: trangThaiLoc,
            timKiem,
            tuNgay: tuNgayLoc,
            denNgay: denNgayLoc,
        });

        const dsCoGia = locGiangVienCoGia(d);

        setTongHopGV(dsCoGia);

        return dsCoGia;
    }

    function xuLyTimKiem(e) {
        e.preventDefault();
        setTimKiem(oTimKiem.trim());
    }

    // ===== Xuất CSV =====
    async function xuLyXuatCsvToanBo() {
        setDangXuatCsvToanBo(true);
        setLoi('');

        try {
            await taiXuongCsvDoiSoat({
                trangThai: trangThaiLoc,
                timKiem,
                tuNgay: tuNgayLoc,
                denNgay: denNgayLoc,
            });
        } catch (err) {
            setLoi(layThongBaoLoi(err));
        } finally {
            setDangXuatCsvToanBo(false);
        }
    }

    // ===== Cập nhật 1 phiếu =====
    async function handleCapNhat(phieu, trangThaiMoi) {
        let maGiaoDichNganHang =
            phieu.maGiaoDichNganHang || '';

        if (trangThaiMoi === 'HOAN_TAT') {
            const nhap = window.prompt(
                'Nhập mã giao dịch ngân hàng',
                maGiaoDichNganHang,
            );

            if (nhap === null) return;

            maGiaoDichNganHang = nhap.trim();
        }

        if (
            !window.confirm(
                `Cập nhật phiếu đối soát #${phieu.maDoiSoat} sang trạng thái ${trangThaiMoi}?`,
            )
        ) {
            return;
        }

        try {
            await capNhatPhieuDoiSoat(phieu.maDoiSoat, {
                trangThai: trangThaiMoi,
                maGiaoDichNganHang,
            });

            await taiLai();

            onThayDoi?.();
        } catch (err) {
            setLoi(layThongBaoLoi(err));
        }
    }

    // ===== Chọn hàng =====
    function toggleChon(maDoiSoat) {
        setDsChon((prev) => {
            const moi = new Set(prev);

            if (moi.has(maDoiSoat)) {
                moi.delete(maDoiSoat);
            } else {
                moi.add(maDoiSoat);
            }

            return moi;
        });
    }

    function toggleChonTatCa() {
        const dsHienTai = locPhieuCoGia(danhSach?.content);

        setDsChon((prev) =>
            prev.size === dsHienTai.length
                ? new Set()
                : new Set(dsHienTai.map((p) => p.maDoiSoat)),
        );
    }

    // ===== Xử lý hàng loạt =====
    async function xuLyHangLoat(trangThaiMoi) {
        if (dsChon.size === 0) return;

        if (
            !window.confirm(
                `Cập nhật ${dsChon.size} phiếu đối soát đã chọn sang trạng thái "${dichTrangThaiDoiSoat(
                    trangThaiMoi,
                )}"?`,
            )
        ) {
            return;
        }

        setDangXuLyHangLoat(true);
        setLoi('');

        try {
            await capNhatHangLoatPhieuDoiSoat(
                Array.from(dsChon),
                { trangThai: trangThaiMoi },
            );

            await taiLai();

            setDsChon(new Set());

            onThayDoi?.();
        } catch (err) {
            setLoi(layThongBaoLoi(err));
        } finally {
            setDangXuLyHangLoat(false);
        }
    }

    // ===== Xuất CSV đã chọn =====
    function xuatCsvDaChon() {
        const dsHienTai = locPhieuCoGia(danhSach?.content);

        const dsDaChon = dsHienTai.filter((p) =>
            dsChon.has(p.maDoiSoat),
        );

        if (dsDaChon.length === 0) return;

        const dong = (v) =>
            `"${String(v ?? '').replace(/"/g, '""')}"`;

        const dauDe = [
            'Mã đối soát',
            'Giảng viên',
            'Số tài khoản',
            'Ngân hàng',
            'Doanh thu',
            'Thực nhận',
            'Trạng thái',
            'Ngày tạo',
        ]
            .map(dong)
            .join(',');

        const dsDong = dsDaChon.map((p) =>
            [
                p.maDoiSoat,
                p.tenGiangVien,
                p.soTaiKhoan,
                p.tenNganHang,
                p.tongDoanhThu,
                p.soTienThucTe,
                dichTrangThaiDoiSoat(p.trangThai),
                formatNgay(p.ngayTao),
            ]
                .map(dong)
                .join(','),
        );

        const noiDung =
            '\ufeff' + [dauDe, ...dsDong].join('\n');

        const blob = new Blob([noiDung], {
            type: 'text/csv;charset=utf-8',
        });

        const url = window.URL.createObjectURL(blob);

        const a = document.createElement('a');

        a.href = url;
        a.download = 'doi-soat-da-chon.csv';

        document.body.appendChild(a);
        a.click();
        a.remove();

        window.URL.revokeObjectURL(url);
    }

    const tongSoTrang = danhSach?.totalPages || 0;
    const dsHienTai = locPhieuCoGia(danhSach?.content);

    return (
        <div className="card tc-card">
            <div className="tc-card-header">
                <div>
                    <div className="tc-card-title">
                        <i
                            className="ti ti-receipt-2"
                            aria-hidden="true"
                        />
                        Đối soát giảng viên
                    </div>

                    <div className="tc-card-subtitle">
                        Theo dõi các phiếu đối soát và thực hiện giải ngân cho
                        giảng viên
                    </div>
                </div>

                <div
                    style={{
                        display: 'flex',
                        gap: 8,
                        flexWrap: 'wrap',
                    }}
                >
                    <div
                        style={{
                            display: 'flex',
                            border: '1px solid var(--border)',
                            borderRadius: 10,
                            overflow: 'hidden',
                        }}
                    >
                        <button
                            className="btn btn-sm"
                            style={{
                                borderRadius: 0,
                                background:
                                    cheDoXem === 'phieu'
                                        ? 'var(--primary)'
                                        : 'var(--white)',
                                color:
                                    cheDoXem === 'phieu' ? '#fff' : 'inherit',
                                border: 'none',
                            }}
                            onClick={() => setCheDoXem('phieu')}
                        >
                            Theo phiếu
                        </button>

                        <button
                            className="btn btn-sm"
                            style={{
                                borderRadius: 0,
                                background:
                                    cheDoXem === 'giang-vien'
                                        ? 'var(--primary)'
                                        : 'var(--white)',
                                color:
                                    cheDoXem === 'giang-vien'
                                        ? '#fff'
                                        : 'inherit',
                                border: 'none',
                            }}
                            onClick={() => setCheDoXem('giang-vien')}
                        >
                            <i
                                className="ti ti-report-money"
                                aria-hidden="true"
                            />
                            Báo cáo GV
                        </button>
                    </div>

                    {cheDoXem === 'phieu' ? (
                        <button
                            className="btn btn-secondary btn-sm"
                            disabled={dangXuatCsvToanBo}
                            onClick={xuLyXuatCsvToanBo}
                        >
                            <i
                                className="ti ti-file-spreadsheet"
                                aria-hidden="true"
                            />

                            {dangXuatCsvToanBo
                                ? 'Đang xuất...'
                                : 'Xuất CSV'}
                        </button>
                    ) : (
                        <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => {
                                const dong = (v) =>
                                    `"${String(v ?? '').replace(
                                        /"/g,
                                        '""',
                                    )}"`;

                                const dauDe = [
                                    'Giảng viên',
                                    'Số tài khoản',
                                    'Ngân hàng',
                                    'Số phiếu',
                                    'Tổng doanh thu',
                                    'Tổng thực nhận',
                                    'Số phiếu chưa hoàn tất',
                                    'Tổng cần chuyển',
                                ]
                                    .map(dong)
                                    .join(',');

                                const dsDong = tongHopGV.map((d) =>
                                    [
                                        d.tenGiangVien ||
                                        `Giảng viên #${d.maGiangVien}`,
                                        d.soTaiKhoan,
                                        d.tenNganHang,
                                        d.soLuongPhieu,
                                        d.tongDoanhThu,
                                        d.tongSoTienThucTe,
                                        d.soLuongChuaHoanTat,
                                        d.tongCanChuyen,
                                    ]
                                        .map(dong)
                                        .join(','),
                                );

                                const noiDung =
                                    '\ufeff' + [dauDe, ...dsDong].join('\n');

                                const blob = new Blob([noiDung], {
                                    type: 'text/csv;charset=utf-8',
                                });

                                const url =
                                    window.URL.createObjectURL(blob);

                                const a = document.createElement('a');

                                a.href = url;
                                a.download =
                                    'doi-soat-theo-giang-vien.csv';

                                document.body.appendChild(a);
                                a.click();
                                a.remove();

                                window.URL.revokeObjectURL(url);
                            }}
                        >
                            <i
                                className="ti ti-file-spreadsheet"
                                aria-hidden="true"
                            />
                            Xuất CSV tổng hợp
                        </button>
                    )}
                </div>
            </div>

            {loi && <div className="alert alert-danger">{loi}</div>}

            <BoLocThoiGianNhanh
                tuNgay={tuNgayLoc}
                denNgay={denNgayLoc}
                onDoiKhoang={(t, d) => {
                    setTuNgayLoc(t);
                    setDenNgayLoc(d);
                }}
            />

            <div className="tc-filter-grid">
                <form
                    onSubmit={xuLyTimKiem}
                    className="tc-filter-group"
                >
                    <label>
                        Tìm kiếm (mã phiếu, giảng viên, tài khoản ngân hàng...)
                    </label>

                    <div className="tc-filter-search">
                        <input
                            className="form-input"
                            placeholder="Nhập từ khóa..."
                            value={oTimKiem}
                            onChange={(e) => setOTimKiem(e.target.value)}
                        />

                        <button
                            type="submit"
                            className="btn btn-secondary btn-sm"
                        >
                            <i
                                className="ti ti-search"
                                aria-hidden="true"
                            />
                        </button>
                    </div>
                </form>

                <div className="tc-filter-group">
                    <label>Trạng thái đối soát</label>

                    <select
                        className="form-input"
                        value={trangThaiLoc}
                        onChange={(e) =>
                            setTrangThaiLoc(e.target.value)
                        }
                    >
                        <option value="">Tất cả</option>
                        <option value="CHO_XAC_NHAN">
                            Chờ xác nhận
                        </option>
                        <option value="DANG_XU_LY">
                            Đang xử lý
                        </option>
                        <option value="HOAN_TAT">
                            Hoàn tất
                        </option>
                        <option value="THAT_BAI">
                            Thất bại
                        </option>
                    </select>
                </div>
            </div>

            {cheDoXem === 'giang-vien' ? (
                <BangTongHopTheoGiangVien
                    dsTongHop={tongHopGV}
                    dangTai={dangTaiTongHop}
                    maGiangVienDangXuLy={maGiangVienDangXuLy}
                    onHoanTatHangLoat={async (dong) => {
                        if (dong.soLuongChuaHoanTat === 0) return;

                        const ten =
                            dong.tenGiangVien ||
                            `Giảng viên #${dong.maGiangVien}`;

                        const maGiaoDichNganHang = window.prompt(
                            `Nhập mã giao dịch ngân hàng dùng chung cho ${dong.soLuongChuaHoanTat} phiếu của ${ten}\nTổng số tiền cần chuyển: ${dinhDangTien(
                                dong.tongCanChuyen,
                            )}`,
                            '',
                        );

                        if (maGiaoDichNganHang === null) return;

                        if (!maGiaoDichNganHang.trim()) {
                            setLoi(
                                'Vui lòng nhập mã giao dịch ngân hàng trước khi xác nhận.',
                            );
                            return;
                        }

                        if (
                            !window.confirm(
                                `Xác nhận đã chuyển ${dinhDangTien(
                                    dong.tongCanChuyen,
                                )} cho ${ten}?`,
                            )
                        ) {
                            return;
                        }

                        setMaGiangVienDangXuLy(dong.maGiangVien);

                        try {
                            await capNhatHangLoatPhieuDoiSoat(
                                dong.dsMaDoiSoatChuaHoanTat,
                                {
                                    trangThai: 'HOAN_TAT',
                                    maGiaoDichNganHang:
                                        maGiaoDichNganHang.trim(),
                                },
                            );

                            await taiLaiTongHopGV();

                            onThayDoi?.();
                        } catch (err) {
                            setLoi(layThongBaoLoi(err));
                        } finally {
                            setMaGiangVienDangXuLy(null);
                        }
                    }}
                    onXemChiTiet={(dong) =>
                        setGiangVienXemChiTiet(dong)
                    }
                />
            ) : (
                <>
                    {dsChon.size > 0 && (
                        <div className="tc-bulkbar">
                            <strong>{dsChon.size} phiếu đã chọn</strong>

                            <button
                                className="btn btn-success btn-sm"
                                disabled={dangXuLyHangLoat}
                                onClick={() => xuLyHangLoat('DANG_XU_LY')}
                            >
                                Chuyển sang Đang xử lý
                            </button>

                            <button
                                className="btn btn-danger btn-sm"
                                disabled={dangXuLyHangLoat}
                                onClick={() => xuLyHangLoat('THAT_BAI')}
                            >
                                Đánh dấu Thất bại
                            </button>

                            <button
                                className="btn btn-secondary btn-sm"
                                onClick={xuatCsvDaChon}
                            >
                                <i
                                    className="ti ti-file-spreadsheet"
                                    aria-hidden="true"
                                />
                                Xuất CSV đã chọn
                            </button>

                            <div className="tc-bulkbar-spacer" />

                            <button
                                className="btn btn-secondary btn-sm"
                                onClick={() => setDsChon(new Set())}
                            >
                                Bỏ chọn
                            </button>
                        </div>
                    )}

                    <div className="tc-table-wrap">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 36 }}>
                                        <input
                                            type="checkbox"
                                            checked={
                                                dsHienTai.length > 0 &&
                                                dsChon.size === dsHienTai.length
                                            }
                                            onChange={toggleChonTatCa}
                                        />
                                    </th>

                                    <th>Mã đối soát</th>
                                    <th>Giảng viên</th>
                                    <th>Tài khoản NH</th>
                                    <th>Doanh thu</th>
                                    <th>Thực nhận</th>
                                    <th>Chênh lệch</th>
                                    <th>Trạng thái</th>
                                    <th>Ngày tạo</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>

                            <tbody>
                                {!dangTai && dsHienTai.length === 0 && (
                                    <tr>
                                        <td colSpan={10} className="tc-empty">
                                            Không có phiếu đối soát nào khớp bộ lọc
                                        </td>
                                    </tr>
                                )}

                                {dsHienTai.map((phieu) => {
                                    const daChon = dsChon.has(phieu.maDoiSoat);

                                    return (
                                        <tr
                                            key={phieu.maDoiSoat}
                                            style={{
                                                background: daChon
                                                    ? 'var(--primary-light)'
                                                    : undefined,
                                            }}
                                        >
                                            <td>
                                                <input
                                                    type="checkbox"
                                                    checked={daChon}
                                                    onChange={() =>
                                                        toggleChon(phieu.maDoiSoat)
                                                    }
                                                />
                                            </td>

                                            <td>
                                                <span
                                                    className="tc-table-link"
                                                    onClick={() =>
                                                        setPhieuXemChiTiet(phieu)
                                                    }
                                                >
                                                    #{phieu.maDoiSoat}
                                                </span>
                                            </td>

                                            <td>
                                                <div style={{ fontWeight: 600 }}>
                                                    {phieu.tenGiangVien ||
                                                        `Giảng viên #${phieu.maGiangVien ?? 'N/A'}`}
                                                </div>

                                                <div
                                                    style={{
                                                        fontSize: 12,
                                                        color: 'var(--text-3)',
                                                    }}
                                                >
                                                    {phieu.tenChuTaiKhoan ||
                                                        'Chưa có tên tài khoản'}
                                                </div>
                                            </td>

                                            <td>
                                                <div style={{ fontWeight: 600 }}>
                                                    {phieu.soTaiKhoan || 'N/A'}
                                                </div>

                                                <div
                                                    style={{
                                                        fontSize: 12,
                                                        color: 'var(--text-3)',
                                                    }}
                                                >
                                                    {phieu.tenNganHang || 'N/A'}
                                                </div>
                                            </td>

                                            <td>
                                                {dinhDangTien(phieu.tongDoanhThu)}
                                            </td>

                                            <td>
                                                {dinhDangTien(phieu.soTienThucTe)}
                                            </td>

                                            <td
                                                style={{
                                                    color:
                                                        phieu.chenhLech > 0
                                                            ? 'var(--danger)'
                                                            : 'var(--success)',
                                                    fontWeight: 600,
                                                }}
                                            >
                                                {dinhDangTien(phieu.chenhLech)}
                                            </td>

                                            <td>
                                                <span
                                                    className={`badge badge-${layMauTrangThaiDoiSoat(
                                                        phieu.trangThai,
                                                    )} tc-status-badge`}
                                                >
                                                    {dichTrangThaiDoiSoat(
                                                        phieu.trangThai,
                                                    )}
                                                </span>
                                            </td>

                                            <td
                                                style={{
                                                    fontSize: 12.5,
                                                    color: 'var(--text-3)',
                                                }}
                                            >
                                                {formatNgay(phieu.ngayTao)}
                                            </td>

                                            <td>
                                                <div
                                                    style={{
                                                        display: 'flex',
                                                        gap: 8,
                                                        flexWrap: 'wrap',
                                                    }}
                                                >
                                                    {phieu.trangThai ===
                                                        'CHO_XAC_NHAN' && (
                                                            <>
                                                                <button
                                                                    className="btn btn-secondary btn-sm"
                                                                    onClick={() =>
                                                                        handleCapNhat(
                                                                            phieu,
                                                                            'DANG_XU_LY',
                                                                        )
                                                                    }
                                                                >
                                                                    Đang xử lý
                                                                </button>

                                                                <button
                                                                    className="btn btn-danger btn-sm"
                                                                    onClick={() =>
                                                                        handleCapNhat(
                                                                            phieu,
                                                                            'THAT_BAI',
                                                                        )
                                                                    }
                                                                >
                                                                    Từ chối
                                                                </button>
                                                            </>
                                                        )}

                                                    {phieu.trangThai ===
                                                        'DANG_XU_LY' && (
                                                            <>
                                                                <button
                                                                    className="btn btn-success btn-sm"
                                                                    onClick={() =>
                                                                        handleCapNhat(
                                                                            phieu,
                                                                            'HOAN_TAT',
                                                                        )
                                                                    }
                                                                >
                                                                    Hoàn tất
                                                                </button>

                                                                <button
                                                                    className="btn btn-danger btn-sm"
                                                                    onClick={() =>
                                                                        handleCapNhat(
                                                                            phieu,
                                                                            'THAT_BAI',
                                                                        )
                                                                    }
                                                                >
                                                                    Từ chối
                                                                </button>
                                                            </>
                                                        )}

                                                    {phieu.trangThai === 'HOAN_TAT' && (
                                                        <span
                                                            style={{
                                                                fontSize: 12,
                                                                color: 'var(--success)',
                                                            }}
                                                        >
                                                            Đã hoàn tất
                                                        </span>
                                                    )}

                                                    {phieu.trangThai === 'THAT_BAI' && (
                                                        <span
                                                            style={{
                                                                fontSize: 12,
                                                                color: 'var(--danger)',
                                                            }}
                                                        >
                                                            Giao dịch thất bại
                                                        </span>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>

                    {tongSoTrang > 1 && (
                        <div className="tc-pagination">
                            <div className="tc-pagination-info">
                                Trang {trang + 1} / {tongSoTrang}
                            </div>

                            <div className="tc-pagination-actions">
                                <button
                                    className="btn btn-secondary btn-sm"
                                    disabled={trang <= 0}
                                    onClick={() =>
                                        setTrang((t) => Math.max(0, t - 1))
                                    }
                                >
                                    Trước
                                </button>

                                <button
                                    className="btn btn-secondary btn-sm"
                                    disabled={trang >= tongSoTrang - 1}
                                    onClick={() =>
                                        setTrang((t) =>
                                            Math.min(tongSoTrang - 1, t + 1),
                                        )
                                    }
                                >
                                    Sau
                                </button>
                            </div>
                        </div>
                    )}
                </>
            )}

            <NganKeoChiTiet
                mo={!!phieuXemChiTiet}
                onDong={() => setPhieuXemChiTiet(null)}
                tieuDe={
                    phieuXemChiTiet
                        ? `Chi tiết phiếu đối soát #${phieuXemChiTiet.maDoiSoat}`
                        : ''
                }
                zIndex={1500}
            >
                {phieuXemChiTiet && (
                    <NoiDungChiTietDoiSoat phieu={phieuXemChiTiet} />
                )}
            </NganKeoChiTiet>

            <NganKeoChiTiet
                mo={!!giangVienXemChiTiet}
                onDong={() => setGiangVienXemChiTiet(null)}
                tieuDe={
                    giangVienXemChiTiet
                        ? `Các phiếu đối soát — ${giangVienXemChiTiet.tenGiangVien ||
                        `Giảng viên #${giangVienXemChiTiet.maGiangVien}`
                        }`
                        : ''
                }
                rongPx={560}
                zIndex={1200}
            >
                {giangVienXemChiTiet && (
                    <NoiDungChiTietDoiSoatTheoGiangVien
                        giangVien={giangVienXemChiTiet}
                        trangThai={trangThaiLoc}
                        tuNgay={tuNgayLoc}
                        denNgay={denNgayLoc}
                        onXemPhieu={(phieu) => {
                            setPhieuXemChiTiet(phieu);
                        }}
                    />
                )}
            </NganKeoChiTiet>
        </div>
    );
}

// =========================================================
// Báo cáo tổng hợp theo giảng viên
// =========================================================

function BangTongHopTheoGiangVien({
    dsTongHop,
    dangTai,
    maGiangVienDangXuLy,
    onHoanTatHangLoat,
    onXemChiTiet,
}) {
    if (dangTai) {
        return (
            <p className="tc-loading">
                Đang tổng hợp báo cáo...
            </p>
        );
    }

    if (dsTongHop.length === 0) {
        return (
            <div className="tc-empty">
                Không có dữ liệu khớp bộ lọc
            </div>
        );
    }

    const tongCanChuyenCaKy = dsTongHop.reduce(
        (s, d) => s + Number(d.tongCanChuyen || 0),
        0,
    );

    const soGiangVienCanChuyen = dsTongHop.filter(
        (d) => d.soLuongChuaHoanTat > 0,
    ).length;

    return (
        <div>
            <div className="tc-summary-strip">
                <div className="tc-summary-item">
                    <div className="tc-summary-label">
                        Tổng cần chuyển
                    </div>

                    <div className="tc-summary-value primary">
                        {dinhDangTien(tongCanChuyenCaKy)}
                    </div>
                </div>

                <div className="tc-summary-item">
                    <div className="tc-summary-label">
                        Giảng viên còn tiền cần chuyển
                    </div>

                    <div className="tc-summary-value">
                        {soGiangVienCanChuyen}
                    </div>
                </div>
            </div>

            <div className="tc-table-wrap">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Giảng viên</th>
                            <th>Tài khoản NH</th>
                            <th>Số phiếu</th>
                            <th>Tổng doanh thu</th>
                            <th>Tổng thực nhận</th>
                            <th>Cần chuyển</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>

                    <tbody>
                        {dsTongHop.map((dong) => (
                            <tr key={dong.maGiangVien}>
                                <td>
                                    <span
                                        className="tc-table-link"
                                        onClick={() => onXemChiTiet?.(dong)}
                                    >
                                        {dong.tenGiangVien ||
                                            `Giảng viên #${dong.maGiangVien}`}
                                    </span>
                                </td>

                                <td>
                                    <div style={{ fontWeight: 600 }}>
                                        {dong.soTaiKhoan || 'N/A'}
                                    </div>

                                    <div
                                        style={{
                                            fontSize: 12,
                                            color: 'var(--text-3)',
                                        }}
                                    >
                                        {dong.tenNganHang || 'N/A'}
                                    </div>
                                </td>

                                <td>{dong.soLuongPhieu}</td>

                                <td>
                                    {dinhDangTien(dong.tongDoanhThu)}
                                </td>

                                <td>
                                    {dinhDangTien(dong.tongSoTienThucTe)}
                                </td>

                                <td>
                                    <div
                                        style={{
                                            fontWeight: 700,
                                            color:
                                                dong.soLuongChuaHoanTat > 0
                                                    ? 'var(--primary)'
                                                    : 'var(--success)',
                                        }}
                                    >
                                        {dinhDangTien(dong.tongCanChuyen)}
                                    </div>

                                    {dong.soLuongChuaHoanTat > 0 && (
                                        <div
                                            style={{
                                                fontSize: 11,
                                                color: 'var(--text-3)',
                                            }}
                                        >
                                            {dong.soLuongChuaHoanTat} phiếu chưa hoàn tất
                                        </div>
                                    )}
                                </td>

                                <td>
                                    <div
                                        style={{
                                            display: 'flex',
                                            gap: 8,
                                            flexWrap: 'wrap',
                                        }}
                                    >
                                        <button
                                            className="btn btn-secondary btn-sm"
                                            onClick={() => onXemChiTiet?.(dong)}
                                        >
                                            <i
                                                className="ti ti-list-details"
                                                aria-hidden="true"
                                            />
                                            Chi tiết
                                        </button>

                                        {dong.soLuongChuaHoanTat > 0 ? (
                                            <button
                                                className="btn btn-success btn-sm"
                                                disabled={
                                                    maGiangVienDangXuLy ===
                                                    dong.maGiangVien
                                                }
                                                onClick={() =>
                                                    onHoanTatHangLoat(dong)
                                                }
                                            >
                                                {maGiangVienDangXuLy ===
                                                    dong.maGiangVien
                                                    ? 'Đang xử lý...'
                                                    : 'Hoàn tất hàng loạt'}
                                            </button>
                                        ) : (
                                            <span
                                                style={{
                                                    fontSize: 12,
                                                    color: 'var(--success)',
                                                }}
                                            >
                                                Đã hoàn tất
                                            </span>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

// =========================================================
// Chi tiết 1 phiếu đối soát
// =========================================================

function NoiDungChiTietDoiSoat({ phieu }) {
    const [giaoDich, setGiaoDich] = useState(null);
    const [loi, setLoi] = useState('');

    useEffect(() => {
        let daHuy = false;

        setGiaoDich(null);

        layGiaoDichTheoDoiSoat(phieu.maDoiSoat)
            .then((d) => {
                if (!daHuy) {
                    setGiaoDich(d);
                }
            })
            .catch((err) => {
                if (!daHuy) {
                    setLoi(layThongBaoLoi(err));
                }
            });

        return () => {
            daHuy = true;
        };
    }, [phieu.maDoiSoat]);

    return (
        <div>
            <div className="tc-detail-box">
                <div>
                    <strong>Giảng viên:</strong>{' '}
                    {phieu.tenGiangVien || `#${phieu.maGiangVien}`}
                </div>

                <div>
                    <strong>Tài khoản:</strong>{' '}
                    {phieu.soTaiKhoan || 'N/A'} —{' '}
                    {phieu.tenNganHang || 'N/A'}
                </div>

                <div>
                    <strong>Chủ tài khoản:</strong>{' '}
                    {phieu.tenChuTaiKhoan || 'N/A'}
                </div>

                <div>
                    <strong>Doanh thu gộp:</strong>{' '}
                    {dinhDangTien(phieu.tongDoanhThu)}
                </div>

                <div>
                    <strong>Thực nhận:</strong>{' '}
                    {dinhDangTien(phieu.soTienThucTe)}
                </div>

                <div>
                    <strong>Trạng thái:</strong>{' '}
                    <span
                        className={`badge badge-${layMauTrangThaiDoiSoat(
                            phieu.trangThai,
                        )}`}
                    >
                        {dichTrangThaiDoiSoat(phieu.trangThai)}
                    </span>
                </div>

                {phieu.maGiaoDichNganHang && (
                    <div>
                        <strong>Mã GD ngân hàng:</strong>{' '}
                        {phieu.maGiaoDichNganHang}
                    </div>
                )}

                <div>
                    <strong>Ngày tạo:</strong>{' '}
                    {formatNgay(phieu.ngayTao)}
                </div>
            </div>

            <div className="tc-overview-title">
                Các giao dịch gộp trong phiếu này (
                {giaoDich?.length || 0})
            </div>

            {loi && <div className="alert alert-danger">{loi}</div>}

            {!giaoDich ? (
                <p className="tc-loading">Đang tải...</p>
            ) : giaoDich.length === 0 ? (
                <div className="tc-empty">
                    Không tìm thấy giao dịch gốc nào.
                </div>
            ) : (
                <div className="tc-detail-list">
                    {giaoDich.map((gd) => (
                        <div
                            key={`${gd.loai}-${gd.maPhieu}`}
                            className="tc-detail-item"
                        >
                            <div className="tc-detail-item-header">
                                <div className="tc-detail-item-title">
                                    {gd.loai === 'KHOA_HOC'
                                        ? 'Mua khóa học'
                                        : 'Phí lớp học'}
                                </div>

                                <div style={{ fontWeight: 700 }}>
                                    {dinhDangTien(gd.soTien)}
                                </div>
                            </div>

                            <div className="tc-detail-item-meta">
                                <span>{gd.tenKhoaHoc}</span>

                                {gd.maLop && (
                                    <span>Lớp: {gd.maLop}</span>
                                )}
                            </div>

                            <div className="tc-detail-item-date">
                                {formatNgay(gd.ngayGiaoDich)}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

// =========================================================
// Chi tiết các phiếu của 1 giảng viên
// =========================================================

function NoiDungChiTietDoiSoatTheoGiangVien({
    giangVien,
    trangThai,
    tuNgay,
    denNgay,
    onXemPhieu,
}) {
    const [dsPhieu, setDsPhieu] = useState(null);
    const [loi, setLoi] = useState('');

    useEffect(() => {
        let daHuy = false;

        setDsPhieu(null);
        setLoi('');

        layChiTietDoiSoatTheoGiangVien(
            giangVien.maGiangVien,
            {
                trangThai,
                tuNgay,
                denNgay,
            },
        )
            .then((d) => {
                if (!daHuy) {
                    setDsPhieu(locPhieuCoGia(d));
                }
            })
            .catch((err) => {
                if (!daHuy) {
                    setLoi(layThongBaoLoi(err));
                }
            });

        return () => {
            daHuy = true;
        };
    }, [
        giangVien.maGiangVien,
        trangThai,
        tuNgay,
        denNgay,
    ]);

    return (
        <div>
            <div className="tc-detail-box">
                <div>
                    <strong>Giảng viên:</strong>{' '}
                    {giangVien.tenGiangVien ||
                        `#${giangVien.maGiangVien}`}
                </div>

                <div>
                    <strong>Tài khoản:</strong>{' '}
                    {giangVien.soTaiKhoan || 'N/A'} —{' '}
                    {giangVien.tenNganHang || 'N/A'}
                </div>

                <div>
                    <strong>Tổng số phiếu:</strong>{' '}
                    {giangVien.soLuongPhieu ?? 0}
                </div>

                <div>
                    <strong>Tổng doanh thu:</strong>{' '}
                    {dinhDangTien(giangVien.tongDoanhThu)}
                </div>

                <div>
                    <strong>Tổng thực nhận:</strong>{' '}
                    {dinhDangTien(giangVien.tongSoTienThucTe)}
                </div>

                <div>
                    <strong>Còn cần chuyển:</strong>{' '}
                    <span
                        style={{
                            fontWeight: 700,
                            color:
                                giangVien.soLuongChuaHoanTat > 0
                                    ? 'var(--primary)'
                                    : 'var(--success)',
                        }}
                    >
                        {dinhDangTien(giangVien.tongCanChuyen)}
                    </span>
                </div>
            </div>

            <div className="tc-overview-title">
                Danh sách phiếu đối soát ({dsPhieu?.length || 0})
            </div>

            {loi && <div className="alert alert-danger">{loi}</div>}

            {!dsPhieu ? (
                <p className="tc-loading">Đang tải...</p>
            ) : dsPhieu.length === 0 ? (
                <div className="tc-empty">
                    Không có phiếu đối soát nào khớp bộ lọc.
                </div>
            ) : (
                <div className="tc-detail-list">
                    {dsPhieu.map((p) => (
                        <div
                            key={p.maDoiSoat}
                            className="tc-detail-item clickable"
                            onClick={() => onXemPhieu?.(p)}
                        >
                            <div className="tc-detail-item-header">
                                <div
                                    className="tc-detail-item-title"
                                    style={{ color: 'var(--primary)' }}
                                >
                                    #{p.maDoiSoat}
                                </div>

                                <span
                                    className={`badge badge-${layMauTrangThaiDoiSoat(
                                        p.trangThai,
                                    )}`}
                                >
                                    {dichTrangThaiDoiSoat(p.trangThai)}
                                </span>
                            </div>

                            <div className="tc-detail-item-meta">
                                <span>
                                    Doanh thu: {dinhDangTien(p.tongDoanhThu)}
                                </span>

                                <span>
                                    Thực nhận:{' '}
                                    {dinhDangTien(p.soTienThucTe)}
                                </span>
                            </div>

                            {p.maGiaoDichNganHang && (
                                <div
                                    style={{
                                        fontSize: 12,
                                        color: 'var(--text-3)',
                                        marginTop: 4,
                                    }}
                                >
                                    Mã GD ngân hàng: {p.maGiaoDichNganHang}
                                </div>
                            )}

                            <div className="tc-detail-item-date">
                                {formatNgay(p.ngayTao)}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}