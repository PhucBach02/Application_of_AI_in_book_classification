package vn.doan;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
@RequestMapping("/res")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class restcontroller {
    @GetMapping("/cre")
    public void create(){
        sql_function sql=new sql_function();
        sql.CreateAndResetDatabase();
        sql.CreateUser();
        sql.CreateAdmin();
        sql.insertSampleData();
    }
    @GetMapping("/in")
    public void in(){
        sql_function sql=new sql_function();
         sql.printAllData();
    }
    @GetMapping("/a")
    public String fin(){
        return "thanh cong";
    }


    @PostMapping("/suggest_genre")
    public String suggestGenre(@RequestBody Map<String, String> payload) {
        String description = payload.get("description");
//        System.out.println(description);
//        String genre =description;
        String genre = openAi.classifyBook(description);
        return genre.replaceAll("\\.$", "");
    }
    @PostMapping("/suggest_tags")
    public String suggest_tags(@RequestBody Map<String, String> payload) {
        String description = payload.get("description");
        String tags = openAi.suggestTags(description);
        return tags;
    }
    @PostMapping("/addbook")
    public String addBook(@RequestBody Map<String, String> payload) {
        function fun=new function();
        sql_function sql=new sql_function();
        String title = payload.get("title");
        String description = payload.get("description");
        String genre = fun.normalizeIfHasUppercase(payload.get("genre"));
        String tags = fun.normalizeIfHasUppercase(payload.get("tags"));
        String targetAudience = payload.get("targetAudience");
        String ageRangeStr = payload.get("ageRange"); // cần ép kiểu
        String difficulty = payload.get("difficulty");
        String imageUrl = payload.get("imageUrl");
        if(sql.insertBook(title,description,genre,tags,targetAudience,ageRangeStr,difficulty,imageUrl)){
            return "true";
        }else{
            return "false";
        }
    }
    @GetMapping("/getbook")
    public String getBook() {
        sql_function sql = new sql_function();
     //   System.out.println(sql.getAllBooks());
    return sql.getAllBooks();
    }

    @PostMapping("/upload")
    public String uploadCsvFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "false01";// lỗi file rỗng
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            return "false02";// chỉ nhận csv
        }

        sql_function sql=new sql_function();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    // Bỏ qua dòng tiêu đề
                    isFirstLine = false;
                    continue;
                }
                // Tách dòng theo dấu phẩy, giả sử CSV chuẩn
                String[] fields = line.split(",", -1); // -1 để giữ các trường trống

                // Kiểm tra số lượng tối thiểu 2 trường: title và description
                if (fields.length < 2 || fields[0].trim().isEmpty() || fields[1].trim().isEmpty()) {
                    continue; // Bỏ qua dòng không hợp lệ
                }

                // Lấy các trường, có thể bị thiếu nên phải kiểm tra
                String title = fields[0].trim();
                String description = fields[1].trim();
                String genre;
                if (fields.length > 2 && !fields[2].trim().isEmpty()) {
                    genre = fields[2].trim();  // Nếu trường genre có dữ liệu hợp lệ
                } else {
                    genre = openAi.classifyBook(description);  // Nếu không, gọi API để phân loại genre
                }
                String tags;
                if (fields.length > 3 && !fields[3].trim().isEmpty()) {
                    tags = fields[3].trim();  // Nếu trường tags có dữ liệu hợp lệ
                } else {
                    tags = openAi.suggestTags(description);  // Nếu không, gọi API để gợi ý tags
                }
                String targetAudience = fields.length > 4 ? fields[4].trim() : "";
                String ageRange = fields.length > 5 ? fields[5].trim() : "";
                String difficulty = fields.length > 6 ? fields[6].trim() : "";
                String imageUrl = fields.length > 7 ? fields[7].trim() : "";

                sql.insertBook(title,description,genre,tags,targetAudience,ageRange,difficulty,imageUrl);
            }

        } catch (Exception e) {
            return "false";
        }

        return "true";
    }

    @PostMapping("/getuser")
    public String getuser(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        sql_function sql = new sql_function();
        return sql.getUserInfoAsJson(username);
    }

    @PostMapping("/getuserbook")
    public String getuserbook(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        sql_function sql = new sql_function();
       // String username_info=sql.getUserInfoAsJson(username);
        return sql.suggestBooksForUser(username);
    }


    @GetMapping("/checkb")
    public String checkb(@RequestParam(value = "username", defaultValue = "default")  String username) {
        sql_function sql = new sql_function();
      //  System.out.println("thông tin sách:"+sql.suggestBooksForUser(username));
        return sql.suggestBooksForUser(username);
    }

    @PostMapping("/deletebook")
    public String deleteBook(@RequestBody Map<String, Object> payload) {
        Object idObj = payload.get("id");
        int id;

        if (idObj instanceof Integer) {
            id = (Integer) idObj;
        } else if (idObj instanceof String) {
            try {
                id = Integer.parseInt((String) idObj);
            } catch (NumberFormatException e) {
                return "false"; // hoặc xử lý lỗi tùy ý
            }
        } else {
            return "false"; // kiểu dữ liệu không hợp lệ
        }        sql_function sql = new sql_function();
        boolean success = sql.deleteBookById(id);
        return success ? "true" : "false";
    }

    @PostMapping("/updatebook")
    public String updatebook(@RequestBody Map<String, String> payload){
        function fun=new function();
        sql_function sql=new sql_function();
        String id=payload.get("id");
        String title = payload.get("title");
        String description = payload.get("description");
        String genre = fun.normalizeIfHasUppercase(payload.get("genre"));
        String tags = fun.normalizeIfHasUppercase(payload.get("tags"));
        String targetAudience = fun.normalizeIfHasUppercase(payload.get("targetAudience"));
        String ageRangeStr = payload.get("ageRange"); // cần ép kiểu
        String difficulty = payload.get("difficulty");
        String imageUrl = payload.get("imageUrl");
        if(sql.updateBook(id,title,description,genre,tags,targetAudience,ageRangeStr,difficulty,imageUrl)){
            return "true";
        }else{
            return "false";
        }

    }

    @GetMapping("/getquantitybook")
    public int getquantitybook() {
        sql_function sql = new sql_function();
        //  System.out.println("thông tin sách:"+sql.suggestBooksForUser(username));
        return sql.getTotalBooks();
    }
    @PostMapping("/getgenreBooklist")
    public String getgenreBooklist() {
        sql_function sql = new sql_function();
        //  System.out.println("thông tin sách:"+sql.suggestBooksForUser(username));
        return sql.getBooksByGenreCategory();
    }
}
