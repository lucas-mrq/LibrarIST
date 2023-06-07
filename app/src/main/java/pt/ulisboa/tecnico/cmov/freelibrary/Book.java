package pt.ulisboa.tecnico.cmov.freelibrary;

public class Book {
    private int id;
    private String title;
    private String author;

    private String language;

    public Book(int id, String title, String author, String language) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.language = language;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getAuthor() {
        return author;
    }


}

