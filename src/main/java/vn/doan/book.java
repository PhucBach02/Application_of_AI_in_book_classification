package vn.doan;
public class book {
    private String id;
    private String title;
    private String description;
    private String genre;
    private String tags;
    private String targetAudience;
    private String ageRange;
    private String difficulty;
    private String imageUrl;

    // Constructor không tham số
    public book() {}

    // Constructor đầy đủ
    public book(String id, String title, String description, String genre, String tags,
                String targetAudience, String ageRange, String difficulty,
                String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.tags = tags;
        this.targetAudience = targetAudience;
        this.ageRange = ageRange;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
    }

    // Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
