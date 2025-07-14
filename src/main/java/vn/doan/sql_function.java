package vn.doan;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

interface sql_manager {
    // xài chung
    void printAllData();
    void insertSampleData();
    boolean checkLoginFromDatabase(String username_s, String password_s, boolean isAdmin);

    //admin
    void CreateAdmin();
    //user
    void CreateUser();
    boolean insertUser(String username_s, String password_s, String email, int age, String occupation,
                       String major, String FavoriteGenres);
    boolean resetPassword(String username_s, String email, String newPassword);
    String getUserInfoAsJson(String username_s);
    String suggestBooksForUser(String username_s);
        //book
    void CreateAndResetDatabase();
    boolean insertBook(
            String title,
            String description,
            String genre,
            String tags,
            String targetAudience,
            String ageRange,
            String difficulty,
            String imageUrl);

    boolean deleteBookById(int id);
    boolean updateBook(
            String id,
            String title,
            String description,
            String genre,
            String tags,
            String targetAudience,
            String ageRange,
            String difficulty,
            String imageUrl);
     int getTotalBooks();// tổng số sách
    }
public class sql_function implements sql_manager{
    private static final String jdbcURL = System.getenv("DB_URL");
    private static final String username = System.getenv("DB_USER");
    private static final String password = System.getenv("DB_PASSWORD");
    function f = new function();

    public void CreateAdmin() {
        String dropbook = "DROP TABLE IF EXISTS admin";
        String createAdmin = """
    CREATE TABLE admin (
        id INT PRIMARY KEY AUTO_INCREMENT,
        username VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        email VARCHAR(255)
    );
    """;

        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropbook);

            System.out.println("Dropped existing tables admin successfully!");
            // Tạo lại bảng
            statement.executeUpdate(createAdmin);
            System.out.println("Created table admin successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e); // Hàm ghi log của bạn
        }
    }

    public void CreateUser() {
        String dropbook = "DROP TABLE IF EXISTS user";
        String createAccount = """
    CREATE TABLE user (
        id INT PRIMARY KEY AUTO_INCREMENT,
        username VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        email VARCHAR(255),
        age INT,
        occupation VARCHAR(255),
        major VARCHAR(255),
        favorite_genres TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );
    """;

        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(dropbook);

            System.out.println("Dropped existing tables user successfully!");

            // Tạo lại bảng
            statement.executeUpdate(createAccount);
            System.out.println("Created table user successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e); // Ghi log lỗi
        }
    }

    public void CreateAndResetDatabase() {
        String dropbook = "DROP TABLE IF EXISTS book";
        String createBooks = """
                CREATE TABLE book (
                    id              INT AUTO_INCREMENT PRIMARY KEY,
                    title           TEXT NOT NULL,
                    description     TEXT,
                    genre           TEXT,               -- Thể loại do AI phân loại
                    tags            TEXT,                       -- Các từ khóa ví dụ: "pháp y, trinh thám, bí ẩn"
                    target_audience VARCHAR(100),               -- Ví dụ: học sinh, sinh viên, người lớn
                    age_range       VARCHAR(20),                -- Ví dụ: "13-17", "18-25"
                    difficulty      VARCHAR(50),                -- Dễ, Trung bình, Nâng cao
                    image_url       VARCHAR(500)               -- URL ảnh bìa sách (link online hoặc từ server)
                );
        """;
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement statement = connection.createStatement()) {

//            // Xóa bảng nếu tồn tại
            statement.executeUpdate(dropbook);
            System.out.println("Dropped existing tables book successfully!");

            // Tạo lại bảng
            statement.executeUpdate(createBooks);
            System.out.println("Created tables book successfully!");

        } catch (SQLException e) {
e.printStackTrace();
            f.logException(e);
        }
    }
///
public static String removeAccents(String input) {
    if (input == null) return "";
    String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
    return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
}

    public String suggestBooksForUser(String username_s) {

        String userJson = getUserInfoAsJson(username_s);
        if (userJson == null) {
//            System.out.println("⚠️ Không tìm thấy thông tin người dùng!");
            return "[]";
        }

//        System.out.println("📦 Thông tin người dùng JSON: " + userJson);

        JSONObject user = new JSONObject(userJson);
        int age = user.getInt("age");
        String occupation = removeAccents(user.optString("occupation", ""));
        String major = removeAccents(user.optString("major", ""));
        String favoriteGenresStr = user.optString("favorite_genres", "");
        String[] favoriteGenres = favoriteGenresStr.split("\\s*,\\s*");
        for (int i = 0; i < favoriteGenres.length; i++) {
            favoriteGenres[i] = removeAccents(favoriteGenres[i]);
        }

//        System.out.println("🧠 Tuổi: " + age);
//        System.out.println("🎓 Nghề nghiệp (chuẩn hóa): " + occupation);
//        System.out.println("🏫 Chuyên ngành (chuẩn hóa): " + major);
//        System.out.println("💖 Thể loại yêu thích: " + Arrays.toString(favoriteGenres));

        String sql = "SELECT * FROM book";

        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            JSONArray resultArray = new JSONArray();

//            int totalBooks = 0;
//            int matchedBooks = 0;

            while (rs.next()) {
//                totalBooks++;

                String title = rs.getString("title");
                String ageRange = rs.getString("age_range");
                String rawTargetAudience = rs.getString("target_audience");
                String rawGenre = rs.getString("genre");
                String rawTags = rs.getString("tags");

                String targetAudience = rawTargetAudience == null ? "" : removeAccents(rawTargetAudience);
                String genre = rawGenre == null ? "" : removeAccents(rawGenre);
                String tags = rawTags == null ? "" : removeAccents(rawTags);

//                System.out.println("\n📘 === Kiểm tra sách #" + totalBooks + ": " + title + " ===");
//                System.out.println("🔹 Age Range: " + ageRange);
//                System.out.println("🔹 Genre: " + genre);
//                System.out.println("🔹 Tags: " + tags);
//                System.out.println("🔹 Target Audience: " + targetAudience);

                // --- Kiểm tra độ tuổi
                boolean ageMatched = false;

                if (ageRange == null || ageRange.isEmpty()) {
                    ageMatched = true;
                } else if (ageRange.contains("-")) {
                    // Trường hợp khoảng tuổi: chỉ khớp nếu nằm trong khoảng
                    String[] parts = ageRange.split("-");
                    if (parts.length == 2) {
                        try {
                            int min = Integer.parseInt(parts[0].trim());
                            int max = Integer.parseInt(parts[1].trim());
                            if (age >= min && age <= max) ageMatched = true;
                        } catch (NumberFormatException e) {
                           // System.out.println("⚠️ Không thể parse age range: " + ageRange);
                        }
                    }
                } else {
                    // Trường hợp "18+" hoặc chỉ số như "12"
                    try {
                        String cleaned = ageRange.trim().replace("+", "");
                        int min = Integer.parseInt(cleaned);
                        if (age >= min) ageMatched = true;
                    } catch (NumberFormatException e) {
                       // System.out.println("⚠️ Không thể parse age range kiểu '18+' hoặc '12': " + ageRange);
                    }
                }



               // System.out.println("✅ Độ tuổi khớp: " + ageMatched);

                int optionalCriteriaMatched = 0;

                if (!occupation.isEmpty() && targetAudience.contains(occupation)) {
                    optionalCriteriaMatched++;
                   // System.out.println("✔️ Nghề nghiệp KHỚP.");
                } else {
                  //  System.out.println("❌ Nghề nghiệp KHÔNG khớp.");
                }

                if (!major.isEmpty() && tags.contains(major)) {
                    optionalCriteriaMatched++;
                  //  System.out.println("✔️ Chuyên ngành KHỚP.");
                } else {
                  //  System.out.println("❌ Chuyên ngành KHÔNG khớp.");
                }

                boolean genreMatched = false;
                for (String fav : favoriteGenres) {
                    if (genre.contains(fav) || tags.contains(fav)) {
                        optionalCriteriaMatched++;
                        genreMatched = true;
                     //   System.out.println("✔️ Thể loại yêu thích KHỚP với: " + fav);
                        break;
                    }
                }
                if (!genreMatched) {
                  //  System.out.println("❌ Không khớp thể loại yêu thích.");
                }

              //  System.out.println("🔎 Tổng tiêu chí phụ khớp: " + optionalCriteriaMatched);

                if (ageMatched && optionalCriteriaMatched >= 1) {
                    //matchedBooks++;
                    JSONObject bookJson = new JSONObject();
                    bookJson.put("id", rs.getInt("id"));
                    bookJson.put("title", rs.getString("title"));
                    bookJson.put("description", rs.getString("description"));
                    bookJson.put("genre", rawGenre);
                    bookJson.put("tags", rawTags);
                    bookJson.put("targetAudience", rawTargetAudience);
                    bookJson.put("ageRange", ageRange);
                    bookJson.put("difficulty", rs.getString("difficulty"));
                    bookJson.put("imageUrl", rs.getString("image_url"));
                    resultArray.put(bookJson);

                   // System.out.println("✅ → SÁCH ĐƯỢC GỢI Ý");
                } else {
                   // System.out.println("❌ → KHÔNG GỢI Ý sách này");
                }
            }

           // System.out.println("\n🎯 KẾT QUẢ: Có " + matchedBooks + " / " + totalBooks + " sách được gợi ý.");
            return resultArray.toString();

        } catch (SQLException e) {
           // System.out.println("❌ Lỗi SQL:");
            e.printStackTrace();
            f.logException(e);
            return "[]";
        }
    }


    public void insertSampleData() {
    String insertAdmins = """
        INSERT INTO admin (username, password, email) VALUES
        ('admin1', 'password123', 'admin1@example.com'),
        ('admin2', 'securepass', 'admin2@example.com');
    """;

    String insertUsers = """
        INSERT INTO user (username, password, email, age, occupation, major, favorite_genres) VALUES
        ('user1', 'pass1', 'user1@example.com', 20, 'Học sinh', 'Khoa học máy tính', 'Khoa học, Kỹ năng sống'),
        ('user2', 'pass2', 'user2@example.com', 30, 'kỹ sư', 'Điện - Cơ khí', 'Trinh thám, Hư cấu'),
        ('user3', 'pass3', 'user3@example.com', 25, 'Nhà thiết kế', 'Vẽ và thiết kế', 'Tiểu thuyết, Lịch sử');
    """;

    String insertBooks = """
        INSERT INTO book (title, description, genre, tags, target_audience, age_range, difficulty, image_url) VALUES
        (
            'Sự Im Lặng Của Bầy Cừu',
            'Một tiểu thuyết trinh thám nổi tiếng với nhân vật bác sĩ Hannibal Lecter.',
            'Trinh thám',
            'tội phạm, tâm lý, phá án',
            'người lớn',
            '18-35',
            'Nâng cao',
            'https://down-vn.img.susercontent.com/file/6c3532cfe57832578580394f9136725a'

        ),
        (
            'Lập trình Java cho người mới bắt đầu',
            'Hướng dẫn cơ bản cho lập trình Java từ đầu.',
            'Học thuật / Giáo dục',
            'lập trình, java, cơ bản',
            'sinh viên, học sinh',
            '16-25',
            'Dễ',
            'https://salt.tikicdn.com/cache/w1200/ts/product/5c/1d/ed/ad299b78f8a5289406ab1936faf066f4.jpg'

        );
    """;

    try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
         Statement statement = connection.createStatement()) {

        statement.executeUpdate(insertAdmins);
        statement.executeUpdate(insertUsers);
        statement.executeUpdate(insertBooks);

        System.out.println("Inserted sample data into admin, user, and book tables successfully!");

    } catch (SQLException e) {
        e.printStackTrace();
        f.logException(e);
    }
}

    public void printAllData() {
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement statement = connection.createStatement()) {

            // 1. Admin table
            System.out.println("=== Admin Table ===");
            ResultSet rsAdmin = statement.executeQuery("SELECT * FROM admin");
            while (rsAdmin.next()) {
                System.out.println("ID: " + rsAdmin.getInt("id") +
                        ", Username: " + rsAdmin.getString("username") +
                        ", Password: " + rsAdmin.getString("password") +
                        ", Email: " + rsAdmin.getString("email"));
            }

            // 2. User table
            System.out.println("\n=== User Table ===");
            ResultSet rsUser = statement.executeQuery("SELECT * FROM user");
            while (rsUser.next()) {
                System.out.println("ID: " + rsUser.getInt("id") +
                        ", Username: " + rsUser.getString("username") +
                        ", Password: " + rsUser.getString("password") +
                        ", Email: " + rsUser.getString("email") +
                        ", Age: " + rsUser.getInt("age") +
                        ", Occupation: " + rsUser.getString("occupation") +
                        ", Major: " + rsUser.getString("major") +
                        ", Favorite Genres: " + rsUser.getString("favorite_genres") +
                        ", Created At: " + rsUser.getString("created_at"));
            }

            // 3. Book table
            System.out.println("\n=== Book Table ===");
            ResultSet rsBook = statement.executeQuery("SELECT * FROM book");
            while (rsBook.next()) {
                System.out.println("ID: " + rsBook.getInt("id") +
                        ", Title: " + rsBook.getString("title") +
                        ", Description: " + rsBook.getString("description") +
                        ", Genre: " + rsBook.getString("genre") +
                        ", Tags: " + rsBook.getString("tags") +
                        ", Target Audience: " + rsBook.getString("target_audience") +
                        ", Age Range: " + rsBook.getString("age_range") +
                        ", Difficulty: " + rsBook.getString("difficulty") +
                        ", Image URL: " + rsBook.getString("image_url"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);
        }
    }


    public boolean checkLoginFromDatabase(String username_s, String password_s, boolean isAdmin) {
        String role = isAdmin ? "admin" : "user";
        String sql = "SELECT COUNT(*) FROM " + role + " WHERE username = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(jdbcURL,username, password);
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username_s);
            stmt.setString(2, password_s);

            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;  // ✅ Tài khoản hợp lệ
            }

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);  // ✅ Ghi log nếu bạn có hàm f.logException
        }

        return false;  // ❌ Không tìm thấy
    }

    public boolean insertUser(String username_s,String password_s,String email,int age, String occupation,
                           String major, String FavoriteGenres) {
        String sql = "INSERT INTO user (username, password, email, age, occupation, major, favorite_genres) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(jdbcURL,username, password);
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username_s);
            stmt.setString(2, password_s);
            stmt.setString(3, email);
            stmt.setInt(4, age);
            stmt.setString(5, occupation);
            stmt.setString(6, major);
            stmt.setString(7,FavoriteGenres);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Thêm người dùng thành công!");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);  // ✅ Ghi log nếu bạn có hàm f.logException
        }
        return false;

    }

    public boolean resetPassword(String username_s, String email, String newPassword) {
        String checkSql = "SELECT * FROM user WHERE username = ? AND email = ?";
        String updateSql = "UPDATE user SET password = ? WHERE username = ? AND email = ?";

        try (Connection conn = DriverManager.getConnection(jdbcURL,username, password);
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                PreparedStatement updateStmt = conn.prepareStatement(updateSql)
        ) {
            // Bước 1: Kiểm tra tồn tại người dùng với username và email
            checkStmt.setString(1, username_s);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Bước 2: Nếu tồn tại, cập nhật mật khẩu mới
                updateStmt.setString(1, newPassword);
                updateStmt.setString(2, username_s);
                updateStmt.setString(3, email);

                int rowsUpdated = updateStmt.executeUpdate();
                return rowsUpdated > 0;
            } else {
                return false; // Không tìm thấy người dùng phù hợp
            }

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);  // ✅ Ghi log nếu bạn có hàm f.logException
            return false;
        }
    }
    public String getUserInfoAsJson(String username_s) {
        String sql = """
        SELECT JSON_OBJECT(
            'id', id,
            'username', username,
            'email', email,
            'age', age,
            'occupation', occupation,
            'major', major,
            'favorite_genres', favorite_genres,
            'created_at', created_at
        ) AS user_info
        FROM user
        WHERE username = ?
    """;

        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username_s);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("user_info"); // Trả về JSON chuỗi
            } else {
                return null; // Không tìm thấy người dùng
            }

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);  // Nếu bạn có hàm log
            return null;
        }
    }

    public boolean insertBook(
                                     String title,
                                     String description,
                                     String genre,
                                     String tags,
                                     String targetAudience,
                                     String ageRange,
                                     String difficulty,
                                     String imageUrl) {
        String sql = """
            INSERT INTO book (title, description, genre, tags, target_audience, age_range, difficulty, image_url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DriverManager.getConnection(jdbcURL,username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, genre);
            stmt.setString(4, tags);
            stmt.setString(5, targetAudience);
            stmt.setString(6, ageRange);
            stmt.setString(7, difficulty);
            stmt.setString(8, imageUrl);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);  // ✅ Ghi log nếu bạn có hàm f.logException
            return false;
        }
    }
    public boolean updateBook(
            String id,
            String title,
            String description,
            String genre,
            String tags,
            String targetAudience,
            String ageRange,
            String difficulty,
            String imageUrl) {

        String sql = """
        UPDATE book
        SET title = ?, description = ?, genre = ?, tags = ?, 
            target_audience = ?, age_range = ?, difficulty = ?, image_url = ?
        WHERE id = ?
    """;

        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, genre);
            stmt.setString(4, tags);
            stmt.setString(5, targetAudience);
            stmt.setString(6, ageRange);
            stmt.setString(7, difficulty);
            stmt.setString(8, imageUrl);
            stmt.setString(9, id);  // dùng để xác định bản ghi cần cập nhật

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);  // Nếu bạn có phương thức ghi log
            return false;
        }
    }

    public boolean deleteBookById(int id) {
        String sql = "DELETE FROM book WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcURL,username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);
            return false;
        }
    }


    public int getTotalBooks() {
        String sql = "SELECT COUNT(*) FROM book";
        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);
        }
        return 0;
    }


//    public boolean checkIfTitleExists(String title) {
//        String checkTitleQuery = "SELECT COUNT(*) FROM book WHERE title = ?";
//        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
//             PreparedStatement checkStmt = connection.prepareStatement(checkTitleQuery)) {
//
//            // Check if the title already exists
//            checkStmt.setString(1, title);
//            ResultSet resultSet = checkStmt.executeQuery();
//            resultSet.next();
//            int count = resultSet.getInt(1);
//
//            // Return true if the title exists, false otherwise
//            return count > 0;
//
//        } catch (SQLException e) {
//            f.logException(e);
//            return false; // Return false in case of any error
//        }
//    }

//    public boolean addBook(String json) {
//        try {
//            // Parse JSON
//            JSONObject jsonObject = new JSONObject(json);
//
//            String title = jsonObject.optString("title","");
//            String description = jsonObject.optString("description", "");
//            String image = jsonObject.optString("image", "");
//            if(title.equals("")||description.equals("")||image.equals("")){
//                return false;
//            }
//            String category=moduleAi.classify(title + " "+description);
//
//            if(category.equals("")){
//                return false;
//            }
//            // Check if the title already exists
//            if (checkIfTitleExists(title)) {
//                return false;
//            }
//
//            String insertBookQuery = """
//            INSERT INTO book (title, description, image, category)
//            VALUES (?, ?, ?, ?);
//        """;
//
//            try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
//                 PreparedStatement insertStmt = connection.prepareStatement(insertBookQuery)) {
//
//                insertStmt.setString(1, title);
//                insertStmt.setString(2, description);
//                insertStmt.setString(3, image);
//                insertStmt.setString(4, category);
//
//                int rowsAffected = insertStmt.executeUpdate();
//                return rowsAffected > 0;
//
//            } catch (SQLException e) {
//                f.logException(e);
//                return false;
//            }
//
//        } catch (Exception e) {
//            f.logException(e); // log JSON parsing or other errors
//            return false;
//        }
//
//
//
//}
public String getAllBooks() {
    ArrayList<book> books = new ArrayList<>();
    try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
         PreparedStatement statement = connection.prepareStatement("SELECT * FROM book");
         ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
            String id = String.valueOf(resultSet.getInt("id"));
            String title = resultSet.getString("title");
            String description = resultSet.getString("description");
            String genre = resultSet.getString("genre");
            String tags = resultSet.getString("tags");
            String targetAudience = resultSet.getString("target_audience");
            String ageRange = resultSet.getString("age_range");
            String difficulty = resultSet.getString("difficulty");
            String imageUrl = resultSet.getString("image_url");
            book b = new book();
            b.setId(id);
            b.setTitle(title);
            b.setDescription(description);
            b.setGenre(genre);
            b.setTags(tags);
            b.setTargetAudience(targetAudience);
            b.setAgeRange(ageRange);
            b.setDifficulty(difficulty);
            b.setImageUrl(imageUrl);
            books.add(b);
        }

    } catch (Exception e) {
        e.printStackTrace();
        f.logException(e);
        return "404";
    }
    Gson gson = new Gson();
    return gson.toJson(books);
}

    public String getBooksByGenreCategory() {
        String sql = "SELECT title, genre FROM book";
        JSONArray resultArray = new JSONArray();

        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String title = rs.getString("title");
                String genreRaw = rs.getString("genre") != null ? rs.getString("genre").toLowerCase().trim() : "";

                Set<String> categories = new HashSet<>();

                // Tách các thể loại nếu có nhiều
                String[] genres = genreRaw.split("\\s*,\\s*");
                if (genres.length >= 2) {
                    categories.add("Nhiều thể loại");
                }

                // Xét từng nhóm lớn
                if (genreRaw.contains("kinh dị")) {
                    categories.add("Kinh dị");
                }
                if (genreRaw.contains("trinh thám")) {
                    categories.add("Trinh thám");
                }
                if (genreRaw.contains("tình yêu") || genreRaw.contains("tình cảm")||genreRaw.contains("lãng mạn")) {
                    categories.add("Tình yêu");
                }
                if (genreRaw.contains("lịch sử")) {
                    categories.add("Lịch sử");
                }
                if (genreRaw.contains("khoa học")) {
                    categories.add("Khoa học");
                }
                if (genreRaw.contains("thiếu nhi")) {
                    categories.add("Thiếu nhi");
                }

                // Nếu không khớp thể loại nào thì thêm vào "Thể loại khác"
                if (categories.isEmpty()) {
                    categories.add("Thể loại khác");
                }

                // Thêm từng category riêng biệt
                for (String cat : categories) {
                    JSONObject bookJson = new JSONObject();
                    bookJson.put("title", title);
                    bookJson.put("genre", genreRaw);
                    bookJson.put("category", cat);
                    resultArray.put(bookJson);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            f.logException(e);
        }

        return resultArray.toString();
    }



//
//    public boolean deleteBook(String title) {
//        String query = "DELETE FROM book WHERE title = ?";
//        try (
//                Connection connection = DriverManager.getConnection(jdbcURL, username, password);
//                PreparedStatement statement = connection.prepareStatement(query)
//        ) {
//            statement.setString(1, title);
//            int rowsAffected = statement.executeUpdate();
//            return (rowsAffected > 0) ? true : false;
//        } catch (Exception e) {
//            f.logException(e);
//            return false;
//        }
//    }
//    public boolean updateByTitle(String jsonInput) {
//        try {
//            JsonObject jsonObject = JsonParser.parseString(jsonInput).getAsJsonObject();
//
//            String title = jsonObject.has("title") ? jsonObject.get("title").getAsString() : null;
//            String description = jsonObject.has("description") ? jsonObject.get("description").getAsString() : null;
//            String image = jsonObject.has("image") ? jsonObject.get("image").getAsString() : null;
//            String category = jsonObject.has("category") ? jsonObject.get("category").getAsString() : null;
//
//            if (title == null || title.isEmpty()) {
//                return false;
//            }
//
//            StringBuilder sql = new StringBuilder("UPDATE book SET ");
//            boolean hasUpdate = false;
//
//            if (description != null) {
//                sql.append("description = ?, ");
//                hasUpdate = true;
//            }
//            if (image != null) {
//                sql.append("image = ?, ");
//                hasUpdate = true;
//            }
//            if (category != null) {
//                sql.append("category = ?, ");
//                hasUpdate = true;
//            }
//
//            if (!hasUpdate) {
//                return false;
//            }
//
//            sql.setLength(sql.length() - 2);
//            sql.append(" WHERE title = ?");
//
//            try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
//                 PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
//
//                int index = 1;
//                if (description != null) {
//                    stmt.setString(index++, description);
//                }
//                if (image != null) {
//                    stmt.setString(index++, image);
//                }
//                if (category != null) {
//                    stmt.setString(index++, category);
//                }
//                stmt.setString(index, title);
//
//                int rowsUpdated = stmt.executeUpdate();
//                return rowsUpdated > 0;
//            }
//        } catch (Exception e) {
//            f.logException(e);
//        }
//        return false;
//    }
//    public String filterBooksByCategory(String category) {
//        List<Book> books = new ArrayList<>();
//
//        if (category == null || category.trim().isEmpty()) {
//            return "[]"; // Trả về mảng rỗng nếu category không hợp lệ
//        }
//
//        String sql = "SELECT * FROM book WHERE category = ?";
//
//        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setString(1, category);
//            ResultSet rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                Book book = new Book(
//                        rs.getInt("id"),
//                        rs.getString("title"),
//                        rs.getString("description"),
//                        rs.getString("image"),
//                        rs.getString("category"),
//                        rs.getString("createdAt")
//                );
//                books.add(book);
//            }
//
//        } catch (Exception e) {
//            f.logException(e);
//        }
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        return gson.toJson(books);
//    }

}