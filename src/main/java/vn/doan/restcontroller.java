package vn.doan;

import org.springframework.web.bind.annotation.*;

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
}
