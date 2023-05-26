package pt.ulisboa.tecnico.cmov.freelibrary;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;

    public Book(int id, String title, String author, String isbn) {
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

