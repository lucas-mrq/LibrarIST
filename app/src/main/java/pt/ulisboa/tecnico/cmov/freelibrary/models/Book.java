package pt.ulisboa.tecnico.cmov.freelibrary.models;

public class Book {
    public int id;
    public String title;
    public String author;
    public String isbn;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
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

    public String getAuthor() {
        return author;
    }
}
