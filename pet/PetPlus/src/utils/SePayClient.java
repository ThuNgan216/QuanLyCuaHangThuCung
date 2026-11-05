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

/**
 * Lấy danh sách giao dịch tiền vào từ SePay (polling) để tự động xác nhận
 * chuyển khoản.
 *
 * Cấu hình:
 * - Chạy thật : đặt biến môi trường SEPAY_API_TOKEN.
 * - Chạy demo : đặt biến môi trường SEPAY_DEMO=true (hoặc VM option
 * -Dsepay.demo=true),
 * không cần token, dùng nút "Giả lập" trong dialog QR.
 */
public final class SePayClient {

    // TODO: đối chiếu với tài liệu SePay API v2 mục "API Giao dịch" (đường dẫn +
    // tên trường)
    private static final String URL_GIAO_DICH = "https://my.sepay.vn/userapi/transactions/list";
    private static final String KEY_DANH_SACH = "transactions";
    private static final String KEY_ID = "id";
    private static final String KEY_SO_TIEN_VAO = "amount_in";
    private static final String KEY_NOI_DUNG = "transaction_content";

    /** Một giao dịch tiền vào. */
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

    /** Giả lập khách vừa chuyển khoản đúng nội dung và số tiền. */
    public static void moPhongChuyenKhoan(String noiDung, long soTien) {
        // id theo thời gian để không trùng giữa các lần chạy (cột maGiaoDichNH có
        // unique index)
        GIAO_DICH_GIA.add(0, new GiaoDich("DEMO-" + System.currentTimeMillis(), soTien, noiDung));
    }

    // ================== GỌI API THẬT ==================
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public List<GiaoDich> layGiaoDichGanDay(int limit) throws Exception {
        if (DEMO)
            return new ArrayList<>(GIAO_DICH_GIA);

        String token = System.getenv("SEPAY_API_TOKEN");
        if (token == null || token.trim().isEmpty())
            throw new IllegalStateException("Chưa cấu hình biến môi trường SEPAY_API_TOKEN");

        HttpRequest req = HttpRequest.newBuilder(URI.create(URL_GIAO_DICH + "?limit=" + limit))
                .timeout(Duration.ofSeconds(8))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200)
            throw new IOException("SePay trả về HTTP " + res.statusCode());

        JsonObject goc = JsonParser.parseString(res.body()).getAsJsonObject();
        JsonArray arr = goc.has(KEY_DANH_SACH) && goc.get(KEY_DANH_SACH).isJsonArray()
                ? goc.getAsJsonArray(KEY_DANH_SACH)
                : new JsonArray();

        List<GiaoDich> ds = new ArrayList<>();
        for (JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            String id = chuoi(o, KEY_ID);
            if (id.isEmpty())
                continue;
            String tien = chuoi(o, KEY_SO_TIEN_VAO);
            long soTien = tien.isEmpty() ? 0L : new BigDecimal(tien).longValue();
            ds.add(new GiaoDich(id, soTien, chuoi(o, KEY_NOI_DUNG)));
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
            String nd = g.noiDung.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
            if (g.soTienVao >= soTien && nd.contains(ma))
                kq.add(g);
        }
        return kq;
    }
}