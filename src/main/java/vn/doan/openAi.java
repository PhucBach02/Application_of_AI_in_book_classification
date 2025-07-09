package vn.doan;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class openAi {
    private static final String API_KEY = System.getenv("API_KEY");
    private static final String API_URL = System.getenv("API_URL");
public static String classifyBook(String bookDescription) {
    try {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String prompt = "Dựa trên nội dung mô tả dưới đây, hãy xác định thể loại phù hợp nhất của cuốn sách. "
                + "Không giới hạn vào danh sách thể loại cố định. "
                + "Chỉ trả về duy nhất tên thể loại ngắn gọn, không thêm giải thích.\n"
                + "Mô tả sách: " + bookDescription;

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", "Bạn là AI phân loại sách."));
        messages.put(new JSONObject().put("role", "user").put("content", prompt));

        requestBody.put("messages", messages);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray choices = jsonResponse.getJSONArray("choices");
        String genre = choices.getJSONObject(0).getJSONObject("message").getString("content").trim();

        return genre;

    } catch (Exception e) {
        e.printStackTrace();
        return "Lỗi khi gọi API: " + e.getMessage();
    }
}
    public static String suggestTags(String bookDescription) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String prompt = "Dựa trên mô tả sau, hãy liệt kê các từ khóa (tags) liên quan đến nội dung và thể loại của sách. "
                    + "Chỉ trả về danh sách tags, phân tách bằng dấu phẩy, không giải thích thêm.\n"
                    + "Mô tả sách: " + bookDescription;

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", "Bạn là AI tạo từ khóa cho sách."));
            messages.put(new JSONObject().put("role", "user").put("content", prompt));

            requestBody.put("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray choices = jsonResponse.getJSONArray("choices");
            String tags = choices.getJSONObject(0).getJSONObject("message").getString("content").trim();

            return tags.replaceAll("\\.$", ""); // xóa dấu chấm cuối nếu có

        } catch (Exception e) {
            e.printStackTrace();

            return "Lỗi khi gọi API: " + e.getMessage();
        }
    }


}
