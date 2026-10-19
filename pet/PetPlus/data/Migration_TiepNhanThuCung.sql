/*
   Migration bo sung phieu tiep nhan thu cung cho database PETPLUS dang co.
   Chi tao bang va index; khong xoa hay sua du lieu hien tai.
*/
USE PETPLUS;
GO

IF OBJECT_ID(N'dbo.TiepNhanThuCung', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TiepNhanThuCung
    (
        maTiepNhan       VARCHAR(20)    NOT NULL,
        maLichHen        VARCHAR(20)    NULL,
        thoiGianTiepNhan DATETIME2      NOT NULL,
        gioHenTra        DATETIME2      NOT NULL,
        maNV             VARCHAR(20)    NOT NULL,
        maKH             VARCHAR(20)    NOT NULL,
        maThuCung        VARCHAR(20)    NOT NULL,
        dichVuYeuCau     NVARCHAR(500)  NOT NULL,
        tinhTrangBanGiao NVARCHAR(1000) NULL,
        ghiChu           NVARCHAR(1000) NULL,
        phiDuKien        DECIMAL(18,2)  NULL,
        trangThai        NVARCHAR(50)   NOT NULL
            CONSTRAINT DF_TiepNhanThuCung_TrangThai DEFAULT N'Đã tiếp nhận',
        CONSTRAINT PK_TiepNhanThuCung PRIMARY KEY (maTiepNhan),
        CONSTRAINT FK_TiepNhanThuCung_LichHen FOREIGN KEY (maLichHen) REFERENCES dbo.LichHen(maLichHen),
        CONSTRAINT FK_TiepNhanThuCung_NhanVien FOREIGN KEY (maNV) REFERENCES dbo.NhanVien(maNV),
        CONSTRAINT FK_TiepNhanThuCung_KhachHang FOREIGN KEY (maKH) REFERENCES dbo.KhachHang(maKH),
        CONSTRAINT FK_TiepNhanThuCung_ThuCung FOREIGN KEY (maThuCung) REFERENCES dbo.ThuCung(maThuCung),
        CONSTRAINT CK_TiepNhanThuCung_Phi CHECK (phiDuKien IS NULL OR phiDuKien >= 0),
        CONSTRAINT CK_TiepNhanThuCung_ThoiGian CHECK (gioHenTra > thoiGianTiepNhan)
    );
END;
GO

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.TiepNhanThuCung')
      AND name = N'UX_TiepNhanThuCung_LichHen'
)
BEGIN
    CREATE UNIQUE INDEX UX_TiepNhanThuCung_LichHen
        ON dbo.TiepNhanThuCung(maLichHen) WHERE maLichHen IS NOT NULL;
END;
GO
