/* ============================================================
   DU LIEU MAU PETPLUS - SQL SERVER
   Chay SAU KHI da chay file tao CSDL PETPLUS va tao TK01/NV01.
   Du lieu demo phuc vu kiem thu cac chuc nang quan ly.
   ============================================================ */
USE PETPLUS;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;

/* 1. Tai khoan va nhan vien (NV01/TK01 admin da duoc tao boi script goc) */
INSERT INTO TaiKhoan (maTK, tenDangNhap, matKhau, vaiTro, trangThai) VALUES
('TK02', N'nhanvien01', N'Demo@123', N'NHAN_VIEN', N'Hoạt động'),
('TK03', N'nhanvien02', N'Demo@123', N'NHAN_VIEN', N'Hoạt động'),
('TK04', N'nhanvien03', N'Demo@123', N'NHAN_VIEN', N'Hoạt động');

INSERT INTO NhanVien (maNV, tenNV, sdt, cccd, diaChi, email, chucVu, maTK) VALUES
('NV02', N'Nguyễn Minh Anh', '0901000002', '079201000002', N'Quận 3, TP. Hồ Chí Minh', 'minhanh@petplus.demo', N'Nhân viên bán hàng', 'TK02'),
('NV03', N'Trần Quốc Bảo', '0901000003', '079201000003', N'Gò Vấp, TP. Hồ Chí Minh', 'quocbao@petplus.demo', N'Nhân viên chăm sóc', 'TK03'),
('NV04', N'Lê Ngọc Hà', '0901000004', '079201000004', N'Thủ Đức, TP. Hồ Chí Minh', 'ngocha@petplus.demo', N'Nhân viên kho', 'TK04');

/* 2. Ca làm và lịch làm việc */
INSERT INTO CaLamViec (maCa, gioBatDau, gioKetThuc, tenCa) VALUES
('CA01', '08:00', '12:00', N'Ca sáng'),
('CA02', '13:00', '17:00', N'Ca chiều'),
('CA03', '17:00', '21:00', N'Ca tối');

INSERT INTO LichLamViec (maLichLamViec, ngayLamViec, maNV, maCa) VALUES
('LLV01', '2026-10-12', 'NV01', 'CA01'),
('LLV02', '2026-10-12', 'NV02', 'CA02'),
('LLV03', '2026-10-12', 'NV03', 'CA03'),
('LLV04', '2026-10-13', 'NV02', 'CA01'),
('LLV05', '2026-10-13', 'NV04', 'CA02'),
('LLV06', '2026-10-14', 'NV01', 'CA02');

/* 3. Khach hang va thu cung */
INSERT INTO KhachHang (maKH, sdt, diaChi, hangThanhVien, diemTichLuy) VALUES
('KH01', '0912000001', N'Quận 1, TP. Hồ Chí Minh', 2, 120),
('KH02', '0912000002', N'Quận 7, TP. Hồ Chí Minh', 1, 45),
('KH03', '0912000003', N'Bình Thạnh, TP. Hồ Chí Minh', 3, 260),
('KH04', '0912000004', N'Thủ Đức, TP. Hồ Chí Minh', 1, 20),
('KH05', '0912000005', N'Tân Bình, TP. Hồ Chí Minh', 2, 95);

INSERT INTO ThuCung (maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, ngaySinh, tinhTrangSK, tiemChung, maKH) VALUES
('TC01', N'Milo', N'Chó', N'Poodle', N'Đực', 4.20, '2023-04-10', N'Khỏe mạnh', N'Đã tiêm 5 bệnh, dại', 'KH01'),
('TC02', N'Bông', N'Mèo', N'Anh lông ngắn', N'Cái', 3.60, '2022-11-20', N'Khỏe mạnh', N'Đã tiêm phòng cơ bản', 'KH02'),
('TC03', N'Đậu', N'Chó', N'Golden Retriever', N'Đực', 18.50, '2021-06-15', N'Cần kiểm tra da định kỳ', N'Đã tiêm 5 bệnh, dại', 'KH03'),
('TC04', N'Miu', N'Mèo', N'Mèo ta', N'Cái', 3.10, '2024-02-01', N'Khỏe mạnh', N'Đang cập nhật lịch tiêm', 'KH04'),
('TC05', N'Cookie', N'Chó', N'Corgi', N'Cái', 9.80, '2022-08-09', N'Khỏe mạnh', N'Đã tiêm 5 bệnh, dại', 'KH05'),
('TC06', N'Bắp', N'Thỏ', N'Holland Lop', N'Đực', 1.45, '2024-05-12', N'Khỏe mạnh', N'Theo dõi lịch khám định kỳ', 'KH01');

/* 4. San pham va ma vach */
INSERT INTO SanPham (maSP, tenSP, giaBan, soLuongTon) VALUES
('SP01', N'Hạt chó trưởng thành 2kg', 185000, 35),
('SP02', N'Hạt mèo vị cá hồi 1.5kg', 165000, 28),
('SP03', N'Sữa tắm thú cưng 500ml', 95000, 18),
('SP04', N'Vòng cổ chó mèo', 55000, 42),
('SP05', N'Đồ chơi bóng cao su', 35000, 50),
('SP06', N'Cát vệ sinh mèo 5L', 78000, 24),
('SP07', N'Pate chó lon 400g', 42000, 60),
('SP08', N'Bàn chải lông thú cưng', 65000, 15),
('SP09', N'Bình nước du lịch cho chó', 89000, 12),
('SP10', N'Thức ăn cho thỏ 1kg', 72000, 20);

INSERT INTO MaVach (maVach, maSP) VALUES
('8938501000011', 'SP01'), ('8938501000028', 'SP02'),
('8938501000035', 'SP03'), ('8938501000042', 'SP04'),
('8938501000059', 'SP05'), ('8938501000066', 'SP06'),
('8938501000073', 'SP07'), ('8938501000080', 'SP08'),
('8938501000097', 'SP09'), ('8938501000103', 'SP10');

/* 5. Nha cung cap va phieu nhap */
INSERT INTO NhaCungCap (maNCC, tenNCC, soDienThoai, diaChi, email) VALUES
('NCC01', N'Công ty Thức ăn PetFood', '02873000001', N'Quận Bình Tân, TP. Hồ Chí Minh', 'sales@petfood.demo'),
('NCC02', N'Phân phối PetCare Việt', '02873000002', N'Quận 12, TP. Hồ Chí Minh', 'contact@petcare.demo'),
('NCC03', N'Đồ dùng thú cưng HappyPaws', '02873000003', N'Thủ Đức, TP. Hồ Chí Minh', 'hello@happypaws.demo');

INSERT INTO PhieuNhap (maPN, ngayNhap, maNV, maNCC) VALUES
('PN01', '2026-10-01', 'NV04', 'NCC01'),
('PN02', '2026-10-05', 'NV04', 'NCC02'),
('PN03', '2026-10-10', 'NV01', 'NCC03');

INSERT INTO ChiTietPhieuNhap (soLuongNhap, donGiaNhap, hanSuDung, maPN, maSP) VALUES
(20, 135000, '2027-10-01', 'PN01', 'SP01'),
(25, 118000, '2027-09-01', 'PN01', 'SP02'),
(30, 28000, '2027-12-31', 'PN01', 'SP07'),
(15, 62000, '2028-06-30', 'PN02', 'SP03'),
(20, 48000, NULL, 'PN02', 'SP04'),
(30, 60000, '2028-01-31', 'PN02', 'SP06'),
(25, 22000, NULL, 'PN03', 'SP05'),
(10, 43000, NULL, 'PN03', 'SP08'),
(10, 61000, NULL, 'PN03', 'SP09');

/* 6. Lich hen va dich vu */
INSERT INTO LichHen (maLichHen, thoiGianHen, trangThai, maNV, maKH, maThuCung) VALUES
('LH01', '2026-10-12T09:00:00', N'Hoàn thành', 'NV03', 'KH01', 'TC01'),
('LH02', '2026-10-12T14:00:00', N'Đã xác nhận', 'NV03', 'KH02', 'TC02'),
('LH03', '2026-10-13T10:30:00', N'Chờ xác nhận', 'NV02', 'KH03', 'TC03'),
('LH04', '2026-10-14T15:00:00', N'Đã xác nhận', 'NV03', 'KH04', 'TC04'),
('LH05', '2026-10-15T16:00:00', N'Đã hủy', 'NV02', 'KH05', 'TC05'),
('LH06', '2026-10-16T09:30:00', N'Đã xác nhận', 'NV03', 'KH01', 'TC06');

INSERT INTO DichVu (maDichVu, tenDichVu, donGia, moTa, trangThai, maLichHen) VALUES
('DV01', N'Tắm và sấy chó nhỏ', 150000, N'Tắm, sấy và vệ sinh cơ bản', N'Hoàn thành', 'LH01'),
('DV02', N'Vệ sinh tai và cắt móng', 80000, N'Vệ sinh tai, cắt móng an toàn', N'Đang thực hiện', 'LH01'),
('DV03', N'Tắm mèo', 180000, N'Tắm chuyên dụng cho mèo', N'Đã đặt', 'LH02'),
('DV04', N'Khám sức khỏe tổng quát', 200000, N'Kiểm tra thể trạng và tư vấn', N'Chưa thực hiện', 'LH03'),
('DV05', N'Cắt tỉa lông', 220000, N'Cắt tỉa theo yêu cầu', N'Đã đặt', 'LH04'),
('DV06', N'Chăm sóc thỏ cơ bản', 100000, N'Kiểm tra lông, móng và tai', N'Đã đặt', 'LH06');

/* 7. Khuyen mai va phuong thuc thanh toan */
INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, phanTramGiam, dieuKienApDung, ngayBatDau, ngayKetThuc) VALUES
('KM01', N'Ưu đãi khách hàng mới', 10, N'Áp dụng cho hóa đơn từ 200.000đ', '2026-01-01', '2026-12-31'),
('KM02', N'Tuần lễ thú cưng', 15, N'Áp dụng dịch vụ chăm sóc thú cưng', '2026-10-01', '2026-10-31'),
('KM03', N'Thành viên thân thiết', 5, N'Áp dụng cho thành viên có tích điểm', '2026-01-01', '2027-01-01'),
('KM04', N'Không áp dụng khuyến mãi', 0, N'Giá bán thông thường', '2020-01-01', '2030-12-31');

INSERT INTO PhuongThucThanhToan (maPTTT, tenPTTT) VALUES
('PT01', N'Tiền mặt'), ('PT02', N'Chuyển khoản'), ('PT03', N'Ví điện tử');

/* 8. Hoa don, chi tiet hoa don, khuyen mai san pham, tich diem */
INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, maKhuyenMai, maPTTT) VALUES
('HD01', '2026-10-02', 'NV02', 'KH01', 'KM01', 'PT01'),
('HD02', '2026-10-06', 'NV02', 'KH02', 'KM02', 'PT02'),
('HD03', '2026-10-08', 'NV01', 'KH03', 'KM03', 'PT03'),
('HD04', '2026-10-10', 'NV02', 'KH04', 'KM04', 'PT01'),
('HD05', '2026-10-12', 'NV01', 'KH05', 'KM01', 'PT02');

INSERT INTO ChiTietHoaDon (maHoaDon, maDichVu, soLuong, donGia) VALUES
('HD01', 'DV01', 1, 150000), ('HD01', 'DV02', 1, 80000),
('HD02', 'DV03', 1, 180000), ('HD03', 'DV04', 1, 200000),
('HD04', 'DV05', 1, 220000), ('HD05', 'DV06', 1, 100000);

INSERT INTO KhuyenMaiSanPham (maKhuyenMaiMaSanPham, mucGiam, maSP, maKhuyenMai, maHoaDon) VALUES
('KMSP01', 18500, 'SP01', 'KM01', 'HD01'),
('KMSP02', 27000, 'SP02', 'KM02', 'HD02'),
('KMSP03', 3900, 'SP06', 'KM03', 'HD03'),
('KMSP04', 0, 'SP05', 'KM04', 'HD04'),
('KMSP05', 9500, 'SP03', 'KM01', 'HD05');

INSERT INTO TichDiem (maTichDiem, diemTichLuy, ngayCapNhat, maKH, maHoaDon) VALUES
('TD01', 23, '2026-10-02', 'KH01', 'HD01'),
('TD02', 18, '2026-10-06', 'KH02', 'HD02'),
('TD03', 20, '2026-10-08', 'KH03', 'HD03'),
('TD04', 22, '2026-10-10', 'KH04', 'HD04'),
('TD05', 10, '2026-10-12', 'KH05', 'HD05');

COMMIT TRANSACTION;
GO

/* Kiem tra nhanh so luong du lieu */
SELECT N'TaiKhoan' AS TenBang, COUNT(*) AS SoDong FROM TaiKhoan UNION ALL
SELECT N'NhanVien', COUNT(*) FROM NhanVien UNION ALL
SELECT N'CaLamViec', COUNT(*) FROM CaLamViec UNION ALL
SELECT N'LichLamViec', COUNT(*) FROM LichLamViec UNION ALL
SELECT N'KhachHang', COUNT(*) FROM KhachHang UNION ALL
SELECT N'ThuCung', COUNT(*) FROM ThuCung UNION ALL
SELECT N'SanPham', COUNT(*) FROM SanPham UNION ALL
SELECT N'MaVach', COUNT(*) FROM MaVach UNION ALL
SELECT N'NhaCungCap', COUNT(*) FROM NhaCungCap UNION ALL
SELECT N'PhieuNhap', COUNT(*) FROM PhieuNhap UNION ALL
SELECT N'ChiTietPhieuNhap', COUNT(*) FROM ChiTietPhieuNhap UNION ALL
SELECT N'LichHen', COUNT(*) FROM LichHen UNION ALL
SELECT N'DichVu', COUNT(*) FROM DichVu UNION ALL
SELECT N'KhuyenMai', COUNT(*) FROM KhuyenMai UNION ALL
SELECT N'PhuongThucThanhToan', COUNT(*) FROM PhuongThucThanhToan UNION ALL
SELECT N'HoaDon', COUNT(*) FROM HoaDon UNION ALL
SELECT N'ChiTietHoaDon', COUNT(*) FROM ChiTietHoaDon UNION ALL
SELECT N'KhuyenMaiSanPham', COUNT(*) FROM KhuyenMaiSanPham UNION ALL
SELECT N'TichDiem', COUNT(*) FROM TichDiem
ORDER BY TenBang;
GO
