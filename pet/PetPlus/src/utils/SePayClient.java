package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SePayClient {
    private static final String URL_GIAO_DICH = "https://userapi.sepay.vn/v2/transactions";

    public static final class GiaoDich {
        public final String id;
        public final String noiDung;
        public final long soTienVao;

        GiaoDich(String id, long soTienVao, String noiDung) {
            this.id = id;
            this.soTienVao = soTienVao;
            this.noiDung = noiDung == null ? "" : noiDung;
        }
    }

    // ================== CHẾ ĐỘ DEMO ==================
    public static final boolean DEMO = "true".equalsIgnoreCase(System.getenv("SEPAY_DEMO"))
            || Boolean.getBoolean("sepay.demo");

    private static final List<GiaoDich> GIAO_DICH_GIA = new CopyOnWriteArrayList<>();

    public static void moPhongChuyenKhoan(String noiDung, long soTien) {
        GIAO_DICH_GIA.add(0, new GiaoDich("DEMO-" + System.currentTimeMillis(), soTien, noiDung));
    }

    // ================== GỌI API THẬT ==================
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public List<GiaoDich> layGiaoDichGanDay(int limit) throws Exception {
        if (DEMO)
            return new ArrayList<>(GIAO_DICH_GIA);

        String token = System.getenv("SEPAY_API_TOKEN");
        if (token == null || token.trim().isEmpty())
            throw new IllegalStateException("Chưa cấu hình biến môi trường SEPAY_API_TOKEN");

        int soLuong = Math.max(1, Math.min(100, limit));
        String url = URL_GIAO_DICH + "?transfer_type=in&transaction_date_sort=desc&per_page=" + soLuong;

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + token.trim())
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        int ma = res.statusCode();
        if (ma == 401 || ma == 403)
            throw new IOException("SePay từ chối token (HTTP " + ma + "). Kiểm tra lại SEPAY_API_TOKEN.");
        if (ma == 429)
            throw new IOException("Gọi SePay quá dày (HTTP 429). Hãy tăng chu kỳ kiểm tra.");
        if (ma != 200)
            throw new IOException("SePay trả về HTTP " + ma);

        return docDanhSach(res.body());
    }

    static List<GiaoDich> docDanhSach(String json) throws IOException {
        JsonElement goc = JsonParser.parseString(json);
        if (!goc.isJsonObject())
            throw new IOException("Dữ liệu SePay không đúng định dạng.");
        JsonObject o = goc.getAsJsonObject();

        String trangThai = chuoi(o, "status");
        if (!trangThai.isEmpty() && !"success".equalsIgnoreCase(trangThai))
            throw new IOException("SePay báo lỗi: " + trangThai);

        JsonElement data = o.get("data");
        JsonArray arr = (data != null && data.isJsonArray()) ? data.getAsJsonArray() : new JsonArray();

        List<GiaoDich> ds = new ArrayList<>();
        for (JsonElement e : arr) {
            if (!e.isJsonObject())
                continue;
            JsonObject gd = e.getAsJsonObject();
            String id = chuoi(gd, "id");
            if (id.isEmpty())
                continue;
            String loai = chuoi(gd, "transfer_type");
            if (!loai.isEmpty() && !"in".equalsIgnoreCase(loai))
                continue; // chỉ lấy tiền vào
            String tien = chuoi(gd, "amount_in");
            long soTien = tien.isEmpty() ? 0L : new BigDecimal(tien).longValue();
            ds.add(new GiaoDich(id, soTien, chuoi(gd, "transaction_content")));
        }
        return ds;
    }

    private static String chuoi(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? "" : e.getAsString();
    }

    /** Giao dịch tiền vào có chứa mã chuyển khoản và đủ số tiền. */
    public static List<GiaoDich> locKhop(List<GiaoDich> ds, String maCK, long soTien) {
        List<GiaoDich> kq = new ArrayList<>();
        String ma = maCK.toUpperCase(Locale.ROOT);
        for (GiaoDich g : ds) {
            // Ngân hàng có thể bỏ dấu cách/gạch trong nội dung nên chỉ so sánh chữ và số.
            String nd = g.noiDung.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
            if (g.soTienVao >= soTien && nd.contains(ma))
                kq.add(g);
        }
        return kq;
    }
}