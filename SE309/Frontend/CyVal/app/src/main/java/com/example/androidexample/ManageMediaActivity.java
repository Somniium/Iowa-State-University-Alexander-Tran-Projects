package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Edwin Cepeda
 */
public class ManageMediaActivity extends AppCompatActivity {

    // Declare button variables
    private Button queryAddBtn, queryManageBtn, addBtn, updateBtn, deleteBtn, returnBtn;
    private ImageButton addSearchBtn, searchBtn;

    // Declare layout variables
    private LinearLayout queryInterface, addInterface, manageInterface;

    //Declare edit variables
    private EditText searchEdt, addEdt, titleEdt, authorEdt, idEdt, otherEdt, releaseDateEdt;

    //Declare textview variables
    private TextView searchAsShowBtn, mediaAddName, mediaAddID, mediaTypeText, titleText, authorText, idText, otherText, releaseDateText;

    private JSONObject mediaData;
    private int mediaID = -1;
    private int adminID = -1;
    private int userID = -1;
    private String mediaType = null;

    //Used solely to differentiate between searching for movies and shows
    private boolean searchAsShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_media); // Set the UI layout for the activity

        //Initialize every component in queryInterface
        queryInterface = findViewById(R.id.queryInterface);
        queryAddBtn = findViewById(R.id.queryAddBtn);
        queryManageBtn = findViewById(R.id.queryManageBtn);

        //Initialize every component in addInterface
        addInterface = findViewById(R.id.addInterface);
        searchAsShowBtn = findViewById(R.id.searchAsShowBtn);
        addEdt = findViewById(R.id.AddEdt);
        addSearchBtn = findViewById(R.id.addSearchBtn);
        mediaAddName = findViewById(R.id.AddName);
        mediaAddID = findViewById(R.id.AddID);
        addBtn = findViewById(R.id.addBtn);

        //Initialize every component in manageInterface
        manageInterface = findViewById(R.id.manageInterface);
        searchEdt = findViewById(R.id.searchEdt);
        searchBtn = findViewById(R.id.searchBtn);
        mediaTypeText = findViewById(R.id.media_type_text);
        titleText = findViewById(R.id.title_text);
        titleEdt = findViewById(R.id.titleEdt);
        authorText = findViewById(R.id.author_text);
        authorEdt = findViewById(R.id.authorEdt);
        idText = findViewById(R.id.id_text);
        idEdt = findViewById(R.id.idEdt);
        otherText = findViewById(R.id.other_text);
        otherEdt = findViewById(R.id.otherEdt);
        releaseDateText = findViewById(R.id.release_date_text);
        releaseDateEdt = findViewById(R.id.releaseDateEdt);
        updateBtn = findViewById(R.id.updateBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        returnBtn = findViewById(R.id.returnBtn);
        returnBtn.setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            adminID = extras.getInt("ADMIN_ID", -1);
            userID = extras.getInt("USER_ID", -1);
            mediaType = extras.getString("MEDIA_TYPE", null);
        }

        updateElements();

        //Click Listeners for queryInterface
        queryAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryInterface.setVisibility(View.GONE);
                addInterface.setVisibility(View.VISIBLE);
            }
        });

        queryManageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryInterface.setVisibility(View.GONE);
                manageInterface.setVisibility(View.VISIBLE);
            }
        });


        //Click listeners for addInterface
        searchAsShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchAsShow) {
                    searchAsShow = true;
                    searchAsShowBtn.setText("Searching for SHOWS\n(Click to toggle)");
                    return;
                } else {
                    searchAsShow = false;
                    searchAsShowBtn.setText("Searching for MOVIES\n(Click to toggle)");
                    return;
                }
            }
        });

        addSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSearch();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMedia();
            }
        });


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchMediaInDB();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMediaInDB();
            }
        });

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageMediaActivity.this, AdminPanelActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                updateMediaInDB();
            }
        });

    }

    private void updateElements() {
        switch (mediaType){
            case "moviesOrShows":
                searchAsShowBtn.setVisibility(View.VISIBLE);
                titleText.setText("Title: ");
                titleEdt.setHint("Title here");
                authorText.setText("Director: ");
                authorEdt.setHint("Director here");
                idText.setText("ID (TMDB): ");
                idEdt.setHint("ID here");
                otherText.setText("Genre: ");
                otherEdt.setHint("Genre here");
                releaseDateText.setText("Release Date: ");
                releaseDateEdt.setHint("Release Date here");
                break;
            case "games":
                titleText.setText("Title: ");
                titleEdt.setHint("Title here");
                authorText.setText("Developer: ");
                authorEdt.setHint("Developer here");
                idText.setText("ID (RAWG): ");
                idEdt.setHint("ID here");
                otherText.setText("Genre: ");
                otherEdt.setHint("Genre here");
                releaseDateText.setText("Release Date: ");
                releaseDateEdt.setHint("Release Date here");
                break;
            case "books":
                titleText.setText("Title: ");
                titleEdt.setHint("Title here");
                authorText.setText("Author: ");
                authorEdt.setHint("Author here");
                idText.setText("ID (Hardcover API): ");
                idEdt.setHint("ID here");
                otherText.setVisibility(View.GONE);
                otherEdt.setVisibility(View.GONE);
                releaseDateText.setText("Year Published: ");
                releaseDateEdt.setHint("Year Published here");
                break;
            case "albums":
                titleText.setText("Title: ");
                titleEdt.setHint("Title here");
                authorText.setVisibility(View.GONE);
                authorEdt.setVisibility(View.GONE);
                idText.setText("ID (Spotify): ");
                idEdt.setHint("ID here");
                otherText.setText("Genre: ");
                otherEdt.setHint("Genre here");
                releaseDateText.setText("Release Date: ");
                releaseDateEdt.setHint("Release Date here");
                break;
            case "artists":
                addEdt.setHint("Search artist by NAME");
                titleText.setVisibility(View.GONE);
                titleEdt.setVisibility(View.GONE);
                authorText.setText("Name: ");
                authorEdt.setHint("Name here");
                idText.setText("ID (Spotify): ");
                idEdt.setHint("ID here");
                otherText.setText("Genre: ");
                otherEdt.setHint("Genre here");
                releaseDateText.setVisibility(View.GONE);
                releaseDateEdt.setVisibility(View.GONE);
                break;
            default:
                Toast.makeText(ManageMediaActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
        }


    }

    private void addSearch() {
        String searchEndpoint = "";
        switch (mediaType){
            case "moviesOrShows":
                if (!searchAsShow) {
                    searchEndpoint = "/search-movies?q=" + addEdt.getText().toString();
                } else {
                    searchEndpoint = "/search-shows?q=" + addEdt.getText().toString();
                }
                break;
            case "games":
                searchEndpoint = "/search-games?q=" + addEdt.getText().toString();
                break;
            case "books":
                searchEndpoint = "/search-books?q=" + addEdt.getText().toString();
                break;
            case "albums":
                searchEndpoint = "http://coms-3090-017.class.las.iastate.edu:8080/search-albums-and-save?album_name=" + addEdt.getText().toString();
                break;
            case "artists":
                searchEndpoint = "http://coms-3090-017.class.las.iastate.edu:8080/search-artist-and-save?artist_name=" + addEdt.getText();
                break;
            default:
                Toast.makeText(ManageMediaActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
        }
        Log.d("test endpoint", searchEndpoint);

        //Music related things automatically save.
        if (mediaType.equals("albums") || mediaType.equals("artists")) {
            StringRequest findAlbumOrArtist = new StringRequest(Request.Method.GET,
                    searchEndpoint,
                    response -> {
                        queryInterface.setVisibility(View.VISIBLE);
                        addInterface.setVisibility(View.GONE);
                        Toast.makeText(ManageMediaActivity.this, "Musical media auto-saved.", Toast.LENGTH_LONG).show();
                    },

                    error -> {
                        Toast.makeText(ManageMediaActivity.this, "Could not find album or artist.", Toast.LENGTH_SHORT).show();
                    }
            );

            // Adding request to the Volley request queue
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(findAlbumOrArtist);
            return;
        }

        ApiClient.getArray(ManageMediaActivity.this, searchEndpoint, new Api_Array_Interface() {

            @Override
            public void onSuccess(JSONArray response) {
                JSONObject selected = null;
                try {
                    selected = response.getJSONObject(0);
                    Log.d("Response test", response.toString());
                    switch (mediaType){
                        case "moviesOrShows":
                            String rawMediaID = selected.getString("tmdbId");
                            String[] splitId = rawMediaID.split("-");
                            mediaID = Integer.parseInt(splitId[1]);
                            mediaAddName.setText("Title: " + selected.getString("title"));
                            mediaAddID.setText("External ID: " + mediaID);
                            break;
                        case "games":
                            mediaID = selected.getInt("rawgId");
                            mediaAddName.setText("Title: " + selected.getString("title"));
                            mediaAddID.setText("External ID (RAWG): " + mediaID);
                            break;
                        case "books":
                            mediaID = selected.getInt("volumeId");
                            mediaAddName.setText("Title: " + selected.getString("title"));
                            mediaAddID.setText("External ID (Hardcover API): " + mediaID);
                            break;
                        default:
                            Toast.makeText(ManageMediaActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Log.e("addSearch error", message);
            }
        });
    }


    private void saveMedia() {
        if (mediaID == -1) {
            Toast.makeText(ManageMediaActivity.this, "Search for something first.", Toast.LENGTH_LONG).show();
            return;
        }
        String saveEndpoint = "";
        switch (mediaType){
            case "moviesOrShows":
                if (!searchAsShow) {
                    saveEndpoint = "/movies/save?tmdbId=" + mediaID + "&type=movie";
                } else {
                    saveEndpoint = "/movies/save?tmdbId=" + mediaID + "&type=tv";
                }
                break;
            case "games":
                saveEndpoint = "/games/save?rawgId=" + mediaID;
                break;
            case "books":
                saveEndpoint = "/books/save?hardcoverId=" + mediaID;
                break;
            default:
                Toast.makeText(ManageMediaActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
        }

        //Music related media auto-saves due to backend structure
        if (mediaType.equals("albums") || mediaType.equals("artists")) {
            Toast.makeText(ManageMediaActivity.this, "Music related media auto-saves.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiClient.post(ManageMediaActivity.this, saveEndpoint, null, new Api_Interface() {

            @Override
            public void onSuccess(JSONObject response) {
                switch (mediaType){
                    case "moviesOrShows":
                        Toast.makeText(ManageMediaActivity.this, "Movie/Show added.", Toast.LENGTH_SHORT).show();
                        break;
                    case "games":
                        Toast.makeText(ManageMediaActivity.this, "Game added.", Toast.LENGTH_SHORT).show();
                        break;
                    case "books":
                        Toast.makeText(ManageMediaActivity.this, "Book added.", Toast.LENGTH_SHORT).show();
                        break;
                    case "artists":
                        Toast.makeText(ManageMediaActivity.this, "Artist added.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(ManageMediaActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
                }

                addInterface.setVisibility(View.GONE);
                queryInterface.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(String message) {
                Log.e("addSearch error", message);
            }
        });
    }

    private void searchMediaInDB() {
        String searchEndpoint = "";
        switch (mediaType){
            case "moviesOrShows":
                searchEndpoint = "/movies/" + searchEdt.getText().toString();
                break;
            case "games":
                searchEndpoint = "/games/" + searchEdt.getText().toString();
                break;
            case "books":
                searchEndpoint = "/books/" + searchEdt.getText().toString();
                break;
            case "albums":
                searchEndpoint = "/album/" + searchEdt.getText().toString();
                break;
            case "artists":
                searchEndpoint = "/artist/get-artist/" + searchEdt.getText().toString();
                break;
            default:
                Toast.makeText(ManageMediaActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
        }
        Log.d("test endpoint", searchEndpoint);

        ApiClient.get(ManageMediaActivity.this, searchEndpoint, new Api_Interface() {

            @Override
            public void onSuccess(JSONObject response) {
                JSONObject selected = null;
                try {
                    Log.d("Response test", response.toString());
                    String mediaDataString = response.toString();
                    mediaData = new JSONObject(mediaDataString);
                    switch (mediaType){
                        case "moviesOrShows":
                            mediaID = response.getInt("id");
                            String movieOrShow = response.getString("mediaType");
                            if (movieOrShow.equals("SHOW")) {
                                mediaTypeText.setText("(SHOW)");
                            } else {
                                mediaTypeText.setText("(MOVIE)");
                            }
                            mediaTypeText.setVisibility(View.VISIBLE);
                            titleEdt.setText(response.getString("title"));
                            authorEdt.setText(response.getString("director"));
                            idEdt.setText(response.getString("tmdbId"));
                            otherEdt.setText(response.getString("genre"));
                            releaseDateEdt.setText(response.getString("releaseDate"));
                            break;
                        case "games":
                            try {
                                mediaID = response.getInt("id");
                                titleEdt.setText(response.getString("title"));
                                authorEdt.setText(response.getString("developer"));
                                idEdt.setText(String.valueOf(response.getInt("rawgId")));
                                otherEdt.setText(response.getString("genre"));
                                releaseDateEdt.setText(response.getString("releaseDate"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "books":
                            try {
                                mediaID = response.getInt("id");
                                titleEdt.setText(response.getString("title"));
                                authorEdt.setText(response.getString("authors"));
                                idEdt.setText(response.getString("volumeId"));
                                releaseDateEdt.setText(response.getString("publishedDate"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "albums":
                            mediaID = response.getInt("albumId");
                            titleEdt.setText(response.getString("name"));
                            idEdt.setText(response.getString("spotifyId"));
                            otherEdt.setText(response.getString("genre"));
                            releaseDateEdt.setText(response.getString("releaseDate"));
                            break;
                        case "artists":
                            mediaID = response.getInt("artistId");
                            authorEdt.setText(response.getString("name"));
                            idEdt.setText(response.getString("spotifyId"));
                            otherEdt.setText(response.getString("genre"));
                            break;
                        default:
                            Toast.makeText(ManageMediaActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Log.e("addSearch error", message);
            }
        });
    }

    private void updateMediaInDB() {
        String updateEndpoint = "";
        switch (mediaType){
            case "moviesOrShows":
                updateEndpoint = "http://coms-3090-017.class.las.iastate.edu:8080/movies/" + mediaID;
                try {
                    mediaData.put("title", titleEdt.getText().toString());
                    mediaData.put("director", authorEdt.getText().toString());
                    mediaData.put("tmdbId", idEdt.getText().toString());
                    mediaData.put("genre", otherEdt.getText().toString());
                    mediaData.put("releaseDate", releaseDateEdt.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "games":
                updateEndpoint = "http://coms-3090-017.class.las.iastate.edu:8080/games/" + mediaID;
                try {
                    mediaData.put("title", titleEdt.getText().toString());
                    mediaData.put("developer", authorEdt.getText().toString());
                    mediaData.put("rawgId", Integer.parseInt(idEdt.getText().toString()));
                    mediaData.put("genre", otherEdt.getText().toString());
                    mediaData.put("releaseDate", releaseDateEdt.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "books":
                updateEndpoint = "http://coms-3090-017.class.las.iastate.edu:8080/books/" + mediaID;
                try {
                    mediaData.put("title", titleEdt.getText().toString());
                    mediaData.put("authors", authorEdt.getText().toString());
                    mediaData.put("volumeId", idEdt.getText().toString());
                    mediaData.put("publishedDate", releaseDateEdt.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "albums":
                updateEndpoint = "http://coms-3090-017.class.las.iastate.edu:8080/album/" + mediaID;
                try {
                    mediaData.put("name", titleEdt.getText().toString());
                    mediaData.put("spotifyId", idEdt.getText().toString());
                    mediaData.put("genre", otherEdt.getText().toString());
                    mediaData.put("releaseDate", releaseDateEdt.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "artists":
                updateEndpoint = "http://coms-3090-017.class.las.iastate.edu:8080/artist/update/" + mediaID;
                try {
                    mediaData.put("name", authorEdt.getText().toString());
                    mediaData.put("spotifyId", idEdt.getText().toString());
                    mediaData.put("genre", otherEdt.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                Toast.makeText(ManageMediaActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
        }

        Log.d("Test updated mediaData", mediaData.toString());
        JsonObjectRequest update = new JsonObjectRequest(Request.Method.PUT,
                updateEndpoint,
                mediaData,
                response -> {
                    Toast.makeText(ManageMediaActivity.this, "Media (ID: " + mediaID + ") data updated.", Toast.LENGTH_SHORT).show();
                },

                error -> {
                    Toast.makeText(ManageMediaActivity.this, "Could not edit media data.", Toast.LENGTH_SHORT).show();
                }
        );

        // Adding request to the Volley request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(update);
    }

    private void deleteMediaInDB() {
        String deleteEndpoint = "";
        switch (mediaType){
            case "moviesOrShows":
                deleteEndpoint = "/movies/" + mediaID;
                break;
            case "games":
                deleteEndpoint = "/games/" + mediaID;
                break;
            case "books":
                deleteEndpoint = "/books/" + mediaID;
                break;
            case "albums":
                deleteEndpoint = "/album/" + mediaID;
                break;
            case "artists":
                deleteEndpoint = "/artist/delete-artist/id/" + mediaID;
                break;
            default:
                Toast.makeText(ManageMediaActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
        }

        ApiClient.deleteString(ManageMediaActivity.this, deleteEndpoint, new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ManageMediaActivity.this, response, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManageMediaActivity.this, AdminPanelActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManageMediaActivity.this, "Could not delete item.", Toast.LENGTH_SHORT).show();
                Log.e("Delete error", message);
            }
        });

    }

}
