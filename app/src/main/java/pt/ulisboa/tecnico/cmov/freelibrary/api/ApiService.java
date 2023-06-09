package pt.ulisboa.tecnico.cmov.freelibrary.api;

import java.util.List;

import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Library;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @GET("api/books/{id}")
    Call<Book> getBookById(@Path("id") int id);

    @GET("api/books/library/{id}")
    Call<List<Book>> getBooksByLibraryId(@Path("id") int id);

    @GET("api/libraries/{id}")
    Call<Library> getLibraryById(@Path("id") int id);

    @GET("api/libraries/books/{bookId}")
    Call<List<Library>> getAvailableBooksInLibrary(@Path("bookId") int bookId);

    @PUT("api/libraries/{id}")
    Call<Library> updateLibrary(@Path("id") int id, @Body Library library);

    @DELETE("api/libraries/{id}")
    Call<Void> deleteLibrary(@Path("id") int id);

    @GET("/api/libraries")
    Call<List<Library>> getAllLibraries();
}
