package pt.ulisboa.tecnico.cmov.freelibrary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Book {
    private int id;
    private String title;
    private String author;

    private String language;

    private int coverId;

    public Book(int id, String title, String author, String language, int coverId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.language = language;
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

    public String getLanguage() {
        return language;
    }

    public String getAuthor() {
        return author;
    }


}

