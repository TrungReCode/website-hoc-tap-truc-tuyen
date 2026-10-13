package com.elearning.choKhoaHoc.haTang.boPhanNgoai;

/**
 * Kết quả trả về sau khi tải video bài giảng lên Cloudinary. Public vì
 * Service (package ungDung.impl) là package khác trong cùng module cần
 * dùng kiểu trả về này — "package-private" trong tài liệu kiến trúc nghĩa
 * là "không cho MODULE khác import", không phải giới hạn package Java
 * thuần (giống lý do Entity/Repository luôn phải public).
 */
public record KetQuaTaiLenVideo(
        String url,
        String dinhDang,
        Integer thoiLuongGiay,
        Integer chieuRong,
        Integer chieuCao
) {
}
