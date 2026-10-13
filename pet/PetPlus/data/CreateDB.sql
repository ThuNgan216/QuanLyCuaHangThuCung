/* ============================================================
   PETPLUS DATABASE
   HE THONG QUAN LY CUA HANG THU CUNG

   19 THUC THE + DU LIEU MAU
   SQL Server

   LUU Y: Script nay XOA database PETPLUS cu va tao lai tu dau.
   Hay sao luu du lieu cu truoc khi chay neu can giu lai.
   ============================================================ */

USE master;
GO

/* 1. XOA DATABASE CU NEU TON TAI */
IF DB_ID(N'PETPLUS') IS NOT NULL
BEGIN
    ALTER DATABASE PETPLUS SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PETPLUS;
END;
GO

/* 2. TAO DATABASE MOI */
CREATE DATABASE PETPLUS;
GO
USE PETPLUS;
GO

/* ============================================================
   1. TAI KHOAN
   ============================================================ */
CREATE TABLE TaiKhoan
(
    maTK        VARCHAR(20)    NOT NULL,
    tenDangNhap NVARCHAR(50)   NOT NULL,
    matKhau     NVARCHAR(255)  NOT NULL,
    vaiTro      NVARCHAR(50)   NOT NULL,
    trangThai   NVARCHAR(50)   NOT NULL,
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (maTK),
    CONSTRAINT UQ_TaiKhoan_TenDangNhap UNIQUE (tenDangNhap)
);
GO

/* ============================================================
   2. NHAN VIEN
   TaiKhoan 1 ----- 1 NhanVien
   ============================================================ */
CREATE TABLE NhanVien
(
    maNV    VARCHAR(20)    NOT NULL,
    tenNV   NVARCHAR(100)  NOT NULL,
    sdt     VARCHAR(15)    NULL,
    cccd    VARCHAR(20)    NULL,
    diaChi  NVARCHAR(255)  NULL,
    email   VARCHAR(100)   NULL,
    chucVu  NVARCHAR(100)  NULL,
    maTK    VARCHAR(20)    NOT NULL,
    CONSTRAINT PK_NhanVien PRIMARY KEY (maNV),
    CONSTRAINT UQ_NhanVien_CCCD UNIQUE (cccd),
    CONSTRAINT UQ_NhanVien_MaTK UNIQUE (maTK),
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK)
);
GO

/* ============================================================
   3. CA LAM VIEC
   ============================================================ */
CREATE TABLE CaLamViec
(
    maCa        VARCHAR(20)    NOT NULL,
    gioBatDau   TIME           NOT NULL,
    gioKetThuc  TIME           NOT NULL,
    tenCa       NVARCHAR(100)  NOT NULL,
    CONSTRAINT PK_CaLamViec PRIMARY KEY (maCa),
    CONSTRAINT CK_CaLamViec_ThoiGian CHECK (gioKetThuc > gioBatDau)
);
GO

/* ============================================================
   4. LICH LAM VIEC
   ============================================================ */
CREATE TABLE LichLamViec
(
    maLichLamViec VARCHAR(20) NOT NULL,
    ngayLamViec   DATE        NOT NULL,
    maNV          VARCHAR(20) NOT NULL,
    maCa          VARCHAR(20) NOT NULL,
    CONSTRAINT PK_LichLamViec PRIMARY KEY (maLichLamViec),
    CONSTRAINT FK_LichLamViec_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_LichLamViec_CaLamViec FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa),
    CONSTRAINT UQ_LichLamViec UNIQUE (ngayLamViec, maNV, maCa)
);
GO

/* ============================================================
   5. KHACH HANG
   Da khai bao tenKH truc tiep trong cau truc bang.
   ============================================================ */
CREATE TABLE KhachHang
(
    maKH           VARCHAR(20)    NOT NULL,
    tenKH          NVARCHAR(100)  NOT NULL,
    sdt            VARCHAR(15)    NULL,
    diaChi         NVARCHAR(255)  NULL,
    hangThanhVien  INT            NULL,
    diemTichLuy    INT            NOT NULL CONSTRAINT DF_KhachHang_Diem DEFAULT 0,
    CONSTRAINT PK_KhachHang PRIMARY KEY (maKH),
    CONSTRAINT CK_KhachHang_Diem CHECK (diemTichLuy >= 0)
);
GO

/* ============================================================
   6. THU CUNG
   ============================================================ */
CREATE TABLE ThuCung
(
    maThuCung    VARCHAR(20)    NOT NULL,
    tenThuCung   NVARCHAR(100)  NOT NULL,
    loai         NVARCHAR(50)   NOT NULL,
    giong        NVARCHAR(100)  NULL,
    gioiTinh     NVARCHAR(20)   NULL,
    canNang      FLOAT          NULL,
    ngaySinh     DATE           NULL,
    tinhTrangSK  NVARCHAR(255)  NULL,
    tiemChung    NVARCHAR(255)  NULL,
    maKH         VARCHAR(20)    NOT NULL,
    CONSTRAINT PK_ThuCung PRIMARY KEY (maThuCung),
    CONSTRAINT FK_ThuCung_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT CK_ThuCung_CanNang CHECK (canNang IS NULL OR canNang >= 0)
);
GO

/* ============================================================
   7. SAN PHAM
   ============================================================ */
CREATE TABLE SanPham
(
    maSP        VARCHAR(20)     NOT NULL,
    tenSP       NVARCHAR(150)   NOT NULL,
    giaBan      FLOAT           NOT NULL,
    soLuongTon  INT             NOT NULL CONSTRAINT DF_SanPham_SoLuong DEFAULT 0,
    CONSTRAINT PK_SanPham PRIMARY KEY (maSP),
    CONSTRAINT CK_SanPham_GiaBan CHECK (giaBan >= 0),
    CONSTRAINT CK_SanPham_SoLuong CHECK (soLuongTon >= 0)
);
GO

/* ============================================================
   8. MA VACH
   ============================================================ */
CREATE TABLE MaVach
(
    maVach VARCHAR(50) NOT NULL,
    maSP   VARCHAR(20) NOT NULL,
    CONSTRAINT PK_MaVach PRIMARY KEY (maVach),
    CONSTRAINT UQ_MaVach_MaSP UNIQUE (maSP),
    CONSTRAINT FK_MaVach_SanPham FOREIGN KEY (maSP) REFERENCES SanPham(maSP)
);
GO

/* ============================================================
   9. NHA CUNG CAP
   ============================================================ */
CREATE TABLE NhaCungCap
(
    maNCC       VARCHAR(20)    NOT NULL,
    tenNCC      NVARCHAR(150)  NOT NULL,
    soDienThoai VARCHAR(15)    NULL,
    diaChi      NVARCHAR(255)  NULL,
    email       VARCHAR(100)   NULL,
    CONSTRAINT PK_NhaCungCap PRIMARY KEY (maNCC)
);
GO

/* ============================================================
   10. PHIEU NHAP
   ============================================================ */
CREATE TABLE PhieuNhap
(
    maPN      VARCHAR(20) NOT NULL,
    ngayNhap  DATE        NOT NULL,
    maNV      VARCHAR(20) NOT NULL,
    maNCC     VARCHAR(20) NOT NULL,
    CONSTRAINT PK_PhieuNhap PRIMARY KEY (maPN),
    CONSTRAINT FK_PhieuNhap_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhieuNhap_NhaCungCap FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC)
);
GO

/* ============================================================
   11. CHI TIET PHIEU NHAP
   ============================================================ */
CREATE TABLE ChiTietPhieuNhap
(
    soLuongNhap  INT             NOT NULL,
    donGiaNhap   DECIMAL(18,2)   NOT NULL,
    hanSuDung    DATE            NULL,
    maPN         VARCHAR(20)     NOT NULL,
    maSP         VARCHAR(20)     NOT NULL,
    CONSTRAINT FK_ChiTietPhieuNhap_PhieuNhap FOREIGN KEY (maPN) REFERENCES PhieuNhap(maPN),
    CONSTRAINT FK_ChiTietPhieuNhap_SanPham FOREIGN KEY (maSP) REFERENCES SanPham(maSP),
    CONSTRAINT UQ_ChiTietPhieuNhap UNIQUE (maPN, maSP),
    CONSTRAINT CK_ChiTietPhieuNhap_SoLuong CHECK (soLuongNhap > 0),
    CONSTRAINT CK_ChiTietPhieuNhap_DonGia CHECK (donGiaNhap >= 0)
);
GO

/* ============================================================
   12. LICH HEN
   ============================================================ */
CREATE TABLE LichHen
(
    maLichHen    VARCHAR(20)   NOT NULL,
    thoiGianHen  DATETIME2     NOT NULL,
    trangThai    NVARCHAR(50)  NOT NULL,
    maNV         VARCHAR(20)   NOT NULL,
    maKH         VARCHAR(20)   NOT NULL,
    maThuCung    VARCHAR(20)   NOT NULL,
    CONSTRAINT PK_LichHen PRIMARY KEY (maLichHen),
    CONSTRAINT FK_LichHen_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_LichHen_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung)
);
GO

/* ============================================================
   13. DICH VU
   ============================================================ */
CREATE TABLE DichVu
(
    maDichVu   VARCHAR(20)     NOT NULL,
    tenDichVu  NVARCHAR(150)   NOT NULL,
    donGia     DECIMAL(18,2)   NOT NULL,
    moTa       NVARCHAR(500)   NULL,
    trangThai  NVARCHAR(20)    NOT NULL,
    maLichHen  VARCHAR(20)     NOT NULL,
    CONSTRAINT PK_DichVu PRIMARY KEY (maDichVu),
    CONSTRAINT FK_DichVu_LichHen FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen),
    CONSTRAINT CK_DichVu_DonGia CHECK (donGia >= 0)
);
GO

/* ============================================================
   14. KHUYEN MAI
   ============================================================ */
CREATE TABLE KhuyenMai
(
    maKhuyenMai     VARCHAR(20)    NOT NULL,
    tenKhuyenMai    NVARCHAR(150)  NOT NULL,
    phanTramGiam    FLOAT          NOT NULL,
    dieuKienApDung  NVARCHAR(500)  NULL,
    ngayBatDau      DATE           NOT NULL,
    ngayKetThuc     DATE           NOT NULL,
    CONSTRAINT PK_KhuyenMai PRIMARY KEY (maKhuyenMai),
    CONSTRAINT CK_KhuyenMai_PhanTram CHECK (phanTramGiam >= 0 AND phanTramGiam <= 100),
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (ngayKetThuc >= ngayBatDau)
);
GO

/* ============================================================
   15. PHUONG THUC THANH TOAN
   ============================================================ */
CREATE TABLE PhuongThucThanhToan
(
    maPTTT   VARCHAR(20)    NOT NULL,
    tenPTTT  NVARCHAR(100)  NOT NULL,
    CONSTRAINT PK_PhuongThucThanhToan PRIMARY KEY (maPTTT),
    CONSTRAINT UQ_PhuongThucThanhToan UNIQUE (tenPTTT)
);
GO

/* ============================================================
   16. HOA DON
   ============================================================ */
CREATE TABLE HoaDon
(
    maHoaDon     VARCHAR(20) NOT NULL,
    ngayLapHD    DATE        NOT NULL,
    maNV         VARCHAR(20) NOT NULL,
    maKH         VARCHAR(20) NOT NULL,
    maKhuyenMai  VARCHAR(20) NOT NULL,
    maPTTT       VARCHAR(20) NOT NULL,
    CONSTRAINT PK_HoaDon PRIMARY KEY (maHoaDon),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai),
    CONSTRAINT FK_HoaDon_PhuongThucThanhToan FOREIGN KEY (maPTTT) REFERENCES PhuongThucThanhToan(maPTTT)
);
GO

/* ============================================================
   17. CHI TIET HOA DON
   ============================================================ */
CREATE TABLE ChiTietHoaDon
(
    maHoaDon  VARCHAR(20)     NOT NULL,
    maDichVu  VARCHAR(20)     NOT NULL,
    soLuong   INT             NOT NULL,
    donGia    DECIMAL(18,2)   NOT NULL,
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maHoaDon, maDichVu),
    CONSTRAINT FK_ChiTietHoaDon_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_ChiTietHoaDon_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),
    CONSTRAINT CK_ChiTietHoaDon_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_ChiTietHoaDon_DonGia CHECK (donGia >= 0)
);
GO

/* ============================================================
   18. KHUYEN MAI SAN PHAM
   ============================================================ */
CREATE TABLE KhuyenMaiSanPham
(
    maKhuyenMaiMaSanPham VARCHAR(50)    NOT NULL,
    mucGiam              DECIMAL(18,2)  NOT NULL,
    maSP                 VARCHAR(20)    NOT NULL,
    maKhuyenMai          VARCHAR(20)    NOT NULL,
    maHoaDon             VARCHAR(20)    NOT NULL,
    CONSTRAINT PK_KhuyenMaiSanPham PRIMARY KEY (maKhuyenMaiMaSanPham),
    CONSTRAINT FK_KhuyenMaiSanPham_SanPham FOREIGN KEY (maSP) REFERENCES SanPham(maSP),
    CONSTRAINT FK_KhuyenMaiSanPham_KhuyenMai FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai),
    CONSTRAINT FK_KhuyenMaiSanPham_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT CK_KhuyenMaiSanPham_MucGiam CHECK (mucGiam >= 0)
);
GO

/* ============================================================
   19. TICH DIEM
   ============================================================ */
CREATE TABLE TichDiem
(
    maTichDiem   VARCHAR(20) NOT NULL,
    diemTichLuy  INT         NOT NULL,
    ngayCapNhat  DATE        NOT NULL CONSTRAINT DF_TichDiem_NgayCapNhat DEFAULT (CONVERT(DATE, SYSDATETIME())),
    maKH         VARCHAR(20) NOT NULL,
    maHoaDon     VARCHAR(20) NOT NULL,
    CONSTRAINT PK_TichDiem PRIMARY KEY (maTichDiem),
    CONSTRAINT FK_TichDiem_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_TichDiem_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT UQ_TichDiem_HoaDon UNIQUE (maHoaDon),
    CONSTRAINT CK_TichDiem_Diem CHECK (diemTichLuy >= 0)
);
GO

/* ============================================================
   INDEX
   ============================================================ */
CREATE INDEX IX_LichLamViec_MaNV ON LichLamViec(maNV);
CREATE INDEX IX_LichLamViec_Ngay ON LichLamViec(ngayLamViec);
CREATE INDEX IX_ThuCung_MaKH ON ThuCung(maKH);
CREATE INDEX IX_PhieuNhap_MaNV ON PhieuNhap(maNV);
CREATE INDEX IX_PhieuNhap_MaNCC ON PhieuNhap(maNCC);
CREATE INDEX IX_PhieuNhap_NgayNhap ON PhieuNhap(ngayNhap);
CREATE INDEX IX_LichHen_MaKH ON LichHen(maKH);
CREATE INDEX IX_LichHen_MaThuCung ON LichHen(maThuCung);
CREATE INDEX IX_LichHen_MaNV ON LichHen(maNV);
CREATE INDEX IX_LichHen_ThoiGianHen ON LichHen(thoiGianHen);
CREATE INDEX IX_HoaDon_MaKH ON HoaDon(maKH);
CREATE INDEX IX_HoaDon_MaNV ON HoaDon(maNV);
CREATE INDEX IX_HoaDon_NgayLapHD ON HoaDon(ngayLapHD);
GO

/* ============================================================
   20. DU LIEU MAU
   Du lieu duoc them theo thu tu phu thuoc khoa ngoai.
   ============================================================ */

/* 20.1 Tai khoan */
INSERT INTO TaiKhoan (maTK, tenDangNhap, matKhau, vaiTro, trangThai) VALUES
('TK01', N'admin',  N'Admin@123',     N'QUAN_LY',  N'Hoạt động'),
('TK02', N'nv.tuan',N'NhanVien@123', N'NHAN_VIEN',N'Hoạt động'),
('TK03', N'nv.mai', N'NhanVien@123', N'NHAN_VIEN',N'Hoạt động'),
('TK04', N'nv.huy', N'NhanVien@123', N'NHAN_VIEN',N'Hoạt động');
GO

/* 20.2 Nhan vien */
INSERT INTO NhanVien (maNV, tenNV, sdt, cccd, diaChi, email, chucVu, maTK) VALUES
('NV01', N'Quản lý hệ thống', '0901000001', '079206000001', N'Quận 1, TP. Hồ Chí Minh', 'admin@petplus.vn', N'Quản lý', 'TK01'),
('NV02', N'Nguyễn Minh Tuấn', '0901000002', '079206000002', N'Quận 3, TP. Hồ Chí Minh', 'tuan@petplus.vn', N'Nhân viên chăm sóc', 'TK02'),
('NV03', N'Trần Thị Mai',      '0901000003', '079206000003', N'Quận 7, TP. Hồ Chí Minh', 'mai@petplus.vn', N'Nhân viên thu ngân', 'TK03'),
('NV04', N'Lê Quốc Huy',       '0901000004', '079206000004', N'Thủ Đức, TP. Hồ Chí Minh', 'huy@petplus.vn', N'Nhân viên chăm sóc', 'TK04');
GO

/* 20.3 Ca lam viec */
INSERT INTO CaLamViec (maCa, gioBatDau, gioKetThuc, tenCa) VALUES
('CA01', '07:30:00', '12:00:00', N'Ca sáng'),
('CA02', '13:00:00', '17:30:00', N'Ca chiều'),
('CA03', '17:30:00', '21:30:00', N'Ca tối');
GO

/* 20.4 Lich lam viec */
INSERT INTO LichLamViec (maLichLamViec, ngayLamViec, maNV, maCa) VALUES
('LLV01', '2026-10-12', 'NV01', 'CA01'),
('LLV02', '2026-10-12', 'NV02', 'CA02'),
('LLV03', '2026-10-12', 'NV03', 'CA01'),
('LLV04', '2026-10-12', 'NV04', 'CA03'),
('LLV05', '2026-10-13', 'NV01', 'CA02'),
('LLV06', '2026-10-13', 'NV02', 'CA01');
GO

/* 20.5 Khach hang: co tenKH de DAO co the truy van ten khach hang */
INSERT INTO KhachHang (maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy) VALUES
('KH01', N'Nguyễn Minh Châu', '0912000001', N'Quận 1, TP. Hồ Chí Minh', 2, 120),
('KH02', N'Trần Gia Hân',     '0912000002', N'Quận 7, TP. Hồ Chí Minh', 1, 45),
('KH03', N'Lê Quốc Bảo',     '0912000003', N'Bình Thạnh, TP. Hồ Chí Minh', 3, 260),
('KH04', N'Phạm Ngọc Anh',   '0912000004', N'Tân Bình, TP. Hồ Chí Minh', 1, 20),
('KH05', N'Võ Hoàng Nam',    '0912000005', N'Thủ Đức, TP. Hồ Chí Minh', 2, 95),
('KH06', N'Đặng Thu Hà',     '0912000006', N'Gò Vấp, TP. Hồ Chí Minh', 1, 0);
GO

/* 20.6 Thu cung */
INSERT INTO ThuCung (maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, ngaySinh, tinhTrangSK, tiemChung, maKH) VALUES
('TC01', N'Bông',   N'Chó', N'Poodle',  N'Cái', 4.80, '2022-01-15', N'Khỏe mạnh', N'Đã tiêm phòng dại và 5 bệnh', 'KH01'),
('TC02', N'Miu',    N'Mèo', N'Anh lông ngắn', N'Cái', 3.60, '2023-04-20', N'Khỏe mạnh', N'Đã tiêm phòng cơ bản', 'KH01'),
('TC03', N'Đốm',    N'Chó', N'Phốc sóc', N'Đực', 5.20, '2021-09-10', N'Cần kiểm tra da', N'Đã tiêm phòng dại', 'KH02'),
('TC04', N'Lucky',  N'Thỏ', N'Thỏ Hà Lan', N'Đực', 1.90, '2024-02-05', N'Khỏe mạnh', N'Chưa cập nhật', 'KH03'),
('TC05', N'Corgi',  N'Chó', N'Corgi', N'Đực', 10.50, '2020-11-12', N'Khỏe mạnh', N'Đã tiêm phòng đầy đủ', 'KH04'),
('TC06', N'Cam',    N'Mèo', N'Mèo ta', N'Cái', 3.10, '2023-08-01', N'Khỏe mạnh', N'Đã tiêm phòng cơ bản', 'KH05');
GO

/* 20.7 San pham */
INSERT INTO SanPham (maSP, tenSP, giaBan, soLuongTon) VALUES
('SP01', N'Hạt dinh dưỡng cho chó 2kg', 185000, 45),
('SP02', N'Hạt dinh dưỡng cho mèo 1.5kg', 165000, 38),
('SP03', N'Cát vệ sinh cho mèo 5L', 75000, 60),
('SP04', N'Bình sữa và bộ chăm sóc thú cưng', 95000, 24),
('SP05', N'Sữa tắm chó mèo 500ml', 120000, 30),
('SP06', N'Dây dắt chó loại tiêu chuẩn', 80000, 20),
('SP07', N'Đồ chơi bóng cao su', 35000, 55),
('SP08', N'Pate cho mèo 400g', 42000, 70);
GO

/* 20.8 Ma vach */
INSERT INTO MaVach (maVach, maSP) VALUES
('8938501000001', 'SP01'),
('8938501000002', 'SP02'),
('8938501000003', 'SP03'),
('8938501000004', 'SP04'),
('8938501000005', 'SP05'),
('8938501000006', 'SP06'),
('8938501000007', 'SP07'),
('8938501000008', 'SP08');
GO

/* 20.9 Nha cung cap */
INSERT INTO NhaCungCap (maNCC, tenNCC, soDienThoai, diaChi, email) VALUES
('NCC01', N'Công ty Thức ăn thú cưng An Phát', '02839000001', N'Quận 5, TP. Hồ Chí Minh', 'sales@anphat.example'),
('NCC02', N'Nhà phân phối Pet World', '02839000002', N'Quận 10, TP. Hồ Chí Minh', 'contact@petworld.example'),
('NCC03', N'Công ty Phụ kiện thú cưng Việt', '02839000003', N'Bình Tân, TP. Hồ Chí Minh', 'orders@petviet.example');
GO

/* 20.10 Phieu nhap */
INSERT INTO PhieuNhap (maPN, ngayNhap, maNV, maNCC) VALUES
('PN01', '2026-09-20', 'NV02', 'NCC01'),
('PN02', '2026-10-01', 'NV01', 'NCC02'),
('PN03', '2026-10-08', 'NV04', 'NCC03');
GO

/* 20.11 Chi tiet phieu nhap */
INSERT INTO ChiTietPhieuNhap (soLuongNhap, donGiaNhap, hanSuDung, maPN, maSP) VALUES
(30, 135000, '2027-09-20', 'PN01', 'SP01'),
(25, 120000, '2027-09-20', 'PN01', 'SP02'),
(50, 48000,  '2027-09-20', 'PN01', 'SP03'),
(20, 68000,  NULL,         'PN02', 'SP04'),
(25, 78000,  '2028-10-01', 'PN02', 'SP05'),
(15, 50000,  NULL,         'PN03', 'SP06'),
(40, 18000,  NULL,         'PN03', 'SP07'),
(60, 22000,  '2027-10-08', 'PN03', 'SP08');
GO

/* 20.12 Lich hen */
INSERT INTO LichHen (maLichHen, thoiGianHen, trangThai, maNV, maKH, maThuCung) VALUES
('LH01', '2026-10-10T09:00:00', N'Hoàn thành', 'NV02', 'KH01', 'TC01'),
('LH02', '2026-10-10T10:30:00', N'Hoàn thành', 'NV03', 'KH02', 'TC03'),
('LH03', '2026-10-11T14:00:00', N'Hoàn thành', 'NV02', 'KH03', 'TC04'),
('LH04', '2026-10-12T09:30:00', N'Đang thực hiện', 'NV04', 'KH04', 'TC05'),
('LH05', '2026-10-12T14:00:00', N'Đã xác nhận', 'NV02', 'KH05', 'TC06'),
('LH06', '2026-10-13T10:00:00', N'Đã xác nhận', 'NV03', 'KH01', 'TC02'),
('LH07', '2026-10-13T15:00:00', N'Chờ xác nhận', 'NV04', 'KH01', 'TC01');
GO

/* 20.13 Dich vu */
INSERT INTO DichVu (maDichVu, tenDichVu, donGia, moTa, trangThai, maLichHen) VALUES
('DV01', N'Tắm và vệ sinh cơ bản', 80000, N'Tắm, sấy và vệ sinh tai chân', N'Hoạt động', 'LH01'),
('DV02', N'Cắt tỉa lông', 120000, N'Cắt tỉa lông theo yêu cầu', N'Hoạt động', 'LH01'),
('DV03', N'Kiểm tra da và lông', 150000, N'Kiểm tra tình trạng da và tư vấn chăm sóc', N'Hoạt động', 'LH02'),
('DV04', N'Chăm sóc thỏ', 90000, N'Vệ sinh và kiểm tra tổng quát', N'Hoạt động', 'LH03'),
('DV05', N'Tắm và cắt tỉa', 130000, N'Tắm sấy và tỉa lông', N'Hoạt động', 'LH04'),
('DV06', N'Vệ sinh răng miệng', 180000, N'Vệ sinh răng miệng cho thú cưng', N'Hoạt động', 'LH05'),
('DV07', N'Tắm mèo', 110000, N'Tắm và sấy lông mèo', N'Hoạt động', 'LH06'),
('DV08', N'Tư vấn chăm sóc thú cưng', 90000, N'Tư vấn dinh dưỡng và chăm sóc', N'Hoạt động', 'LH07');
GO

/* 20.14 Khuyen mai */
INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, phanTramGiam, dieuKienApDung, ngayBatDau, ngayKetThuc) VALUES
('KM01', N'Ưu đãi khách hàng thân thiết', 10, N'Áp dụng cho hóa đơn dịch vụ đủ điều kiện', '2026-01-01', '2026-12-31'),
('KM02', N'Ưu đãi khách hàng mới', 5, N'Áp dụng cho khách hàng mới', '2026-01-01', '2026-12-31'),
('KM03', N'Khuyến mãi đặc biệt', 15, N'Áp dụng trong chương trình khuyến mãi', '2026-09-01', '2026-10-31');
GO

/* 20.15 Phuong thuc thanh toan */
INSERT INTO PhuongThucThanhToan (maPTTT, tenPTTT) VALUES
('PT01', N'Tiền mặt'),
('PT02', N'Chuyển khoản'),
('PT03', N'Thẻ ngân hàng'),
('PT04', N'Ví điện tử');
GO

/* 20.16 Hoa don */
INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, maKhuyenMai, maPTTT) VALUES
('HD01', '2026-10-10', 'NV03', 'KH01', 'KM01', 'PT01'),
('HD02', '2026-10-10', 'NV03', 'KH02', 'KM02', 'PT02'),
('HD03', '2026-10-11', 'NV01', 'KH03', 'KM01', 'PT01'),
('HD04', '2026-10-11', 'NV03', 'KH04', 'KM03', 'PT03'),
('HD05', '2026-10-12', 'NV01', 'KH05', 'KM02', 'PT04'),
('HD06', '2026-10-12', 'NV03', 'KH01', 'KM01', 'PT02');
GO

/* 20.17 Chi tiet hoa don */
INSERT INTO ChiTietHoaDon (maHoaDon, maDichVu, soLuong, donGia) VALUES
('HD01', 'DV01', 1, 80000),
('HD01', 'DV02', 1, 120000),
('HD02', 'DV03', 1, 150000),
('HD03', 'DV04', 1, 90000),
('HD04', 'DV05', 1, 130000),
('HD05', 'DV06', 1, 180000),
('HD06', 'DV07', 1, 110000),
('HD06', 'DV08', 1, 90000);
GO

/* 20.18 Khuyen mai san pham */
INSERT INTO KhuyenMaiSanPham (maKhuyenMaiMaSanPham, mucGiam, maSP, maKhuyenMai, maHoaDon) VALUES
('KMSP01', 10, 'SP01', 'KM01', 'HD01'),
('KMSP02', 5,  'SP04', 'KM02', 'HD02'),
('KMSP03', 15, 'SP05', 'KM03', 'HD04');
GO

/* 20.19 Tich diem */
INSERT INTO TichDiem (maTichDiem, diemTichLuy, ngayCapNhat, maKH, maHoaDon) VALUES
('TD01', 20, '2026-10-10', 'KH01', 'HD01'),
('TD02', 15, '2026-10-10', 'KH02', 'HD02'),
('TD03', 10, '2026-10-11', 'KH03', 'HD03'),
('TD04', 25, '2026-10-12', 'KH05', 'HD05');
GO

/* ============================================================
   KIEM TRA SO BANG (MONG DOI: 19)
   ============================================================ */
SELECT COUNT(*) AS SoLuongBang
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE';

SELECT TABLE_NAME AS TenBang
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
GO

/* KIEM TRA COT tenKH */
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'KhachHang'
ORDER BY ORDINAL_POSITION;
GO

/* KIEM TRA CAC BANG DU LIEU MAU */
SELECT * FROM KhachHang ORDER BY maKH;
SELECT * FROM HoaDon ORDER BY ngayLapHD, maHoaDon;
SELECT * FROM ChiTietHoaDon ORDER BY maHoaDon, maDichVu;
GO

PRINT N'================================================';
PRINT N'PETPLUS DA DUOC TAO THANH CONG';
PRINT N'19 BANG - DA KHAI BAO tenKH VA DU LIEU MAU';
PRINT N'TAI KHOAN QUAN LY MAC DINH: admin / Admin@123';
PRINT N'================================================';
GO
