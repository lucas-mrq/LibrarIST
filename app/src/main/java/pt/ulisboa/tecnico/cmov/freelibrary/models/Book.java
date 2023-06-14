package pt.ulisboa.tecnico.cmov.freelibrary.models;

public class Book {
    public int id;
    public String title;
    public String author;
    public String isbn;

    private int coverId;

    public Book(int id, String title, String author, int coverId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverId = coverId;
    }

    public int getImage() { return coverId;}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
