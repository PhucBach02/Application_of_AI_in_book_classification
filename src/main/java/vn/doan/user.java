package vn.doan;


public class user {
    private int id;
    private String username;
    private String password;
    private String email;
    private int age;
    private String occupation;
    private String major;
    private String favorite_genres;
    private String createdAt;

    // Constructor không tham số
    public user() {
    }

    // Constructor đầy đủ
    public user(int id, String username, String password, String email, int age, String occupation, String major, String favoriteGenres, String createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.age = age;
        this.occupation = occupation;
        this.major = major;
        this.favorite_genres = favoriteGenres;
        this.createdAt = createdAt;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getFavoriteGenres() {
        return favorite_genres;
    }

    public void setFavoriteGenres(String favoriteGenres) {
        this.favorite_genres = favoriteGenres;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
