package pt.ulisboa.tecnico.cmov.freelibrary.api;

import java.util.List;

import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;

public interface BooksCallback {
    void onBooksFetched(List<Book> books);
    void onFetchFailed();
}
