package vn.doan;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/con")
@CrossOrigin(origins = "https://book-filter.project.io.vn", allowCredentials = "true")
public class manager_controller {
    @PostMapping("/login")
    public String login(@RequestBody Map<String, Object> payload, HttpSession session) {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");
        boolean isAdmin = Boolean.TRUE.equals(payload.get("isAdmin"));

        sql_function sql = new sql_function();
        if (sql.checkLoginFromDatabase(username, password, isAdmin)) {
            // Lưu thông tin người dùng vào session
            session.setAttribute("username", username);
            session.setAttribute("role", isAdmin ? "admin" : "user");
            session.setMaxInactiveInterval(300); // 5 phút

            return isAdmin ? "admin" : "user";
        } else {
            return "fail";
        }
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, Object> payload) {
        try {
            String username = (String) payload.get("username");
            String password = (String) payload.get("password");
            String email = (String) payload.get("email");
            String occupation = (String) payload.get("occupation");
            String major = (String) payload.get("major");
            String favoriteGenres = (String) payload.get("favoriteGenres");

            // Xử lý tuổi
            int age = 0;
            Object ageObj = payload.get("age");
            if (ageObj instanceof Integer) {
                age = (Integer) ageObj;
            } else if (ageObj instanceof Double) {
                age = ((Double) ageObj).intValue();
            } else if (ageObj instanceof String) {
                age = Integer.parseInt((String) ageObj);
            }

            sql_function sql = new sql_function();
            return sql.insertUser(username, password, email, age, occupation, major, favoriteGenres)
                    ? "true"
                    : "false";
        } catch (Exception e) {
            e.printStackTrace();
            return "false";
        }
    }

    @PostMapping("/forgot_password")
    public String forgotPassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String email = payload.get("email");
        String newPassword = payload.get("newPassword");

        sql_function sql = new sql_function();
        if (sql.resetPassword(username, email, newPassword)) {
            return "true";
        } else {
            return "false";
        }
    }

    @GetMapping("/check_session")
    public Map<String, Object> checkSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        if (username != null) {
            response.put("status", "logged_in");
            response.put("username", username);
            response.put("role", role);
        } else {
            response.put("status", "not_logged_in");
        }

        return response;
    }
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa toàn bộ session
        return "logged_out";
    }


}
