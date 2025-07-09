package vn.doan;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

interface functon_manager{
    void logException(Exception e); // ghi file exception chỉ lấy dòng đầu
    void logException1(String ma,Exception e);// ghi thêm mã và exception
    String getFirstStackTraceLine(Exception e);// chỉ lấy dòng đâu exception
    String normalizeIfHasUppercase(String input);// thường hết
}

public class function implements functon_manager{
    static String duongdanlog = "/var/log/log_system.txt";
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{4,}$";
    private void ensureLogFileExists() {
        File logFile = new File(duongdanlog);
        File parentDir = logFile.getParentFile();

        try {
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {  // Tạo thư mục nếu chưa có
                   System.err.println("❌ Không thể tạo thư mục log: " + parentDir.getAbsolutePath());
                    return; // Dừng lại nếu không tạo được thư mục
                }
            }

            if (!logFile.exists()) {
                if (!logFile.createNewFile()) {
                    System.err.println("❌ Không thể tạo file log: " + duongdanlog);
                }
            }
        } catch (Exception e) {

        }
    }

    public void logException(Exception e) {
        ensureLogFileExists();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        synchronized (this) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(duongdanlog, true))) {
                writer.print("Time: " + timeStamp);
                writer.print(" | Exception:");
                writer.println(getFirstStackTraceLine(e));
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
        }

    public void log(String messagee) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (PrintWriter writer = new PrintWriter(new FileWriter(duongdanlog, true))) {
            writer.print("Time: " + timeStamp+"-");
            writer.println(messagee);
        } catch (Exception ex) {
            //ex.printStackTrace();
        }}
    public void logException1(String ma,Exception e) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (PrintWriter writer = new PrintWriter(new FileWriter(duongdanlog, true))) {
            writer.print("Time: " + timeStamp);
            writer.print(" " +ma+" | Exception:");
            writer.println(getFirstStackTraceLine(e));
        } catch (Exception ex) {
        }}
    //
    public String getFirstStackTraceLine(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String[] lines = sw.toString().split("\n");
        return lines.length > 0 ? lines[0].trim() : ""; // Sử dụng trim() để loại bỏ khoảng trắng dư thừa
    }
    public String normalizeIfHasUppercase(String input) {
        if (input == null) return null;

        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return input.toLowerCase(); // Có chữ hoa → chuyển hết sang thường
            }
        }
        return input; // Không có chữ hoa → giữ nguyên
    }

}
