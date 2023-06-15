package pt.ulisboa.tecnico.cmov.freelibrary.api;

import java.util.List;

import okhttp3.RequestBody;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Library;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @GET("/api/libraries")
    Call<List<Library>> getAllLibraries();

    @GET("api/libraries/{id}")
    Call<Library> getLibraryById(@Path("id") int id);

    @Headers("Content-Type: application/json")
    @POST("/api/libraries")
    Call<Library> createLibrary(@Body Library library);

    @PUT("api/libraries/{id}")
    Call<Library> updateLibrary(@Path("id") int id, @Body Library library);


    @DELETE("api/libraries/{id}")
    Call<Void> deleteLibrary(@Path("id") int id);

    @GET("api/books/{id}")
    Call<Book> getBookById(@Path("id") int id);

    @GET("api/books/isbn/{isbn}")
    Call<Book> getBookByIsbn(@Path("isbn") String isbn);

    @GET("api/books/library/{id}")
    Call<List<Book>> getBooksByLibraryId(@Path("id") int id);

    @GET("api/libraries/books/{bookId}")
    Call<List<Library>> getAvailableBooksInLibrary(@Path("bookId") int bookId);

    @Multipart
    @POST("/api/books")
    Call<Book> createBook(@Part("title") RequestBody title,
                          @Part("author") RequestBody author,
                          @Part("isbn") RequestBody isbn);

    @POST("/api/libraries/{libraryId}/books/{bookId}/checkin")
    Call<Void> checkInBook(@Path("libraryId") int libraryId, @Path("bookId") int bookId);
}
