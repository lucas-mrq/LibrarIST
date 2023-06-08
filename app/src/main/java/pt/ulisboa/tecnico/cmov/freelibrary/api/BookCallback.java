package pt.ulisboa.tecnico.cmov.freelibrary.api;

import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;

public interface BookCallback {
    void onBookFetched(Book book);
    void onFetchFailed();
}
