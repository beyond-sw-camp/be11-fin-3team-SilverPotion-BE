package silverpotion.postserver.post.dtos;

public class PostCategoryDto {
    private String type;
    private String label;

    public PostCategoryDto(String type, String label) {
        this.type = type;
        this.label = label;
    }
    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

}
