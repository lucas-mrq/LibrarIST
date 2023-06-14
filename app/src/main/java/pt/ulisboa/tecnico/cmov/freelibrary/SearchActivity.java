package pt.ulisboa.tecnico.cmov.freelibrary;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.api.BooksCallback;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Library;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ApiService apiService;
    private List<Book> bookList;

    private List<Book> searchList;
    private List<Integer> searchScore;
    ArrayAdapter<String> adapter;
    boolean isSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ThemeManager.isDarkThemeEnabled()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_search_horizontal);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_search);
        }

        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        setLocale(language);

        bookList = new ArrayList<>();

        //Define the book list
        ListView listView = findViewById(R.id.listBooks);
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            Book book = isSearching ? searchList.get(position) : bookList.get(position);
            Intent intent = new Intent(SearchActivity.this, BookInfo.class);
            intent.putExtra("id", book.getId());
            intent.putExtra("title", book.getTitle());
            intent.putExtra("author", book.getAuthor());
            startActivity(intent);
        });

        //Instantiate ApiService
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        fetchAllBooks(new BooksCallback() {
            @Override
            public void onBooksFetched(List<Book> books) {
                List<String> titles =  bookList.stream().map(Book::getTitle).collect(Collectors.toList());
                adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, titles);
                listView.setAdapter(adapter);
            }

            @Override
            public void onFetchFailed() {
            }
        });

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                isSearching = true;
                searchList = new ArrayList<>();
                searchScore = new ArrayList<>();

                for (Book book : bookList) {
                    int scoreTitle = compare(book.title, query);
                    int scoreAuthor = compare(book.author, query);
                    int score = scoreAuthor > scoreTitle ? scoreAuthor : scoreTitle;
                    Book tempBook = book;
                    int nextScore;
                    Book nextBook;

                    for (int i = 0; i < 10; i++) {
                        if (score == 0) break;
                        if (i >= searchList.size()) {
                            searchList.add(tempBook);
                            searchScore.add(score);
                            break;
                        }
                        if (score > searchScore.get(i)) {
                            nextBook = searchList.get(i);
                            nextScore = searchScore.get(i);
                            searchList.set(i, tempBook);
                            searchScore.set(i, score);
                            score = nextScore;
                            tempBook = nextBook;
                        }
                    }
                }

                List<String> titles =  searchList.stream().map(Book::getTitle).collect(Collectors.toList());
                adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, titles);
                listView.setAdapter(adapter);

                return false;
            }
            //We can adapt the code if the text is changed, not implemented for the moment as we didn't link with server
            @Override
            public boolean onQueryTextChange(String newText) {
                /* Nothing for the moment */
                return false;
            }
        });

        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(SearchActivity.this, MainActivity.class);
            intentMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentMap);
        });
    }

    private void fetchAllBooks(final BooksCallback callback) {
        apiService.getAllLibraries().enqueue(new Callback<List<Library>>() {
            @Override
            public void onResponse(Call<List<Library>> call, Response<List<Library>> response) {
                if (response.isSuccessful()) {
                    List<Library> libraries = response.body();
                    int libraryCount = libraries.size();
                    AtomicInteger fetchedCount = new AtomicInteger(0);

                    for (Library library : libraries) {
                        int libraryId = library.getId();
                        apiService.getBooksByLibraryId(libraryId).enqueue(new Callback<List<Book>>() {
                            @Override
                            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                                if (response.isSuccessful()) {
                                    List<Book> libraryBooks = response.body();
                                    for (Book book : libraryBooks) {
                                        boolean bookExists = false;
                                        for (Book existingBook : bookList) {
                                            if (existingBook.getTitle().equals(book.getTitle())) {
                                                bookExists = true;
                                                break;
                                            }
                                        }
                                        if (!bookExists) {
                                            bookList.add(book);
                                        }
                                    }

                                }

                                int count = fetchedCount.incrementAndGet();
                                if (count == libraryCount) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onBooksFetched(bookList);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Book>> call, Throwable t) {
                                t.printStackTrace();
                                int count = fetchedCount.incrementAndGet();
                                if (count == libraryCount) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onBooksFetched(bookList);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Library>> call, Throwable t) {
                t.printStackTrace();
                callback.onFetchFailed();
            }
        });
    }
    private int compare(String str1, String str2) {
        int score = 0;
        String[] bookWords = str1.toLowerCase().split(" ");
        String[] search = str2.toLowerCase().split(" ");

        for(String str : search) {
            for (String word : bookWords) {
                if (word.equals(str)) {
                    score += str.length();
                }
            }
        }

        return score;
    }

    public void setLocale(String language) {
        Locale locale = new Locale(language);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getApplicationContext().createConfigurationContext(config);
        } else {
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }
}