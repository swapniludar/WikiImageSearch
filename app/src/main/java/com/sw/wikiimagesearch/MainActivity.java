package com.sw.wikiimagesearch;

import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main activity class.
 *
 * @author Swapnil Udar
 */
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String URL = "https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=%s&pilimit=50&generator=prefixsearch&gpssearch=";
    private static final String REQ_TAG = "WikiImageSearch";
    private static final double IMAGE_RATIO = 0.5;

    private EditText sSearchTextField;
    private TextView sErrorText;
    private TextWatcher sSearchFieldWatcher;

    private RequestQueue sRequestQueue;

    private RecyclerView.Adapter sAdapter;

    private List<SearchResult> sSearchResults = new ArrayList<>();

    private int sImageSize;

    private String sURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sSearchTextField = (EditText) findViewById(R.id.search_text);
        sSearchFieldWatcher = new SearchFieldWatcher();
        sSearchTextField.addTextChangedListener(sSearchFieldWatcher);

        sRequestQueue = Volley.newRequestQueue(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        ImageLoader imageLoader = new ImageLoader(sRequestQueue, new LruBitmapCache(this));
        sImageSize = calcImageSize();
        sAdapter = new SearchedImagesAdapter(sSearchResults, imageLoader, sImageSize);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.images_list_view);
        recyclerView.setAdapter(sAdapter);
        recyclerView.setLayoutManager(layoutManager);

        sURL = updateImageSize();

        sErrorText = (TextView) findViewById(R.id.error_text);
    }

    /**
     * Image size is calculated based on the screen width
     */
    private int calcImageSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int imageSize = (int) Math.ceil(width * IMAGE_RATIO);
        // Round image size in units of 50
        imageSize = imageSize - (imageSize % 50);
        Log.d(LOG_TAG, "Image size (in pixels): " + imageSize);
        return imageSize;
    }

    /**
     * Update image size in URL
     *
     */
    private String updateImageSize() {
        String url = String.format(URL, String.valueOf(sImageSize));
        Log.d(LOG_TAG, "Updated URL: "+url);
        return url;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check if network connectivity is available
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // when not connected
        if(networkInfo == null || !networkInfo.isAvailable() || !networkInfo.isConnected()) {
            sErrorText.setText(getString(R.string.network_error));
        } else {
            sErrorText.setText("");
            // Check on resume if search term is present
            String searchTerm = sSearchTextField.getText().toString();
            if (searchTerm.trim().length() != 0) {
                initNewRequests(searchTerm);
            }
        }
    }

    private void cancelCurrentRequests() {
        // Cancel current requests
        if (sRequestQueue != null) {
            sRequestQueue.cancelAll(REQ_TAG);
        }
        sSearchResults.clear();
        sAdapter.notifyDataSetChanged();
        Log.d(LOG_TAG, "cancelCurrentRequests: Cancelled current requests");
    }

    private void initNewRequests(CharSequence searchQuery) {
        String url;
        try {
            url = sURL + URLEncoder.encode(searchQuery.toString(), "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            Log.d(LOG_TAG, "initNewRequests: " + uee.getLocalizedMessage());
            return;
        }
        Log.d(LOG_TAG, "initNewRequests: final URL " + url);

        JsonObjectRequest req = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG_TAG, "initNewRequests->onResponse: " + response.toString());

                        try {
                            parseResponse(response);
                            sAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.d(LOG_TAG, "initNewRequests->onResponse: Failed to parse JSON reply");
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "initNewRequests->onErrorResponse: " + error.toString());
                    }
                });
        req.setTag(REQ_TAG);
        sRequestQueue.add(req);
    }

    private void parseResponse(JSONObject response) throws JSONException {
        if (response.has("query")
                && response.getJSONObject("query").has("pages")) {
            JSONObject pages = response.getJSONObject("query").getJSONObject(
                    "pages");
            JSONArray pageNames = pages.names();

            JSONObject searchItem;
            int index;
            String title;
            String imageSource;

            for (int i = 0; i < pageNames.length(); i++) {
                searchItem = pages.getJSONObject(pageNames.getString(i));
                index = searchItem.getInt("index");
                title = searchItem.getString("title");
                imageSource = searchItem.has("thumbnail") ? searchItem.getJSONObject("thumbnail")
                        .getString("source") : null;
                sSearchResults.add(new SearchResult(index, title, imageSource));
            }
            Collections.sort(sSearchResults);
            Log.d(LOG_TAG, "parseResponse: " + sSearchResults.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelCurrentRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove text change listener from search field
        sSearchTextField.removeTextChangedListener(sSearchFieldWatcher);

        sRequestQueue = null;
        sSearchFieldWatcher = null;
        sSearchTextField = null;
    }

    private class SearchFieldWatcher implements TextWatcher {

        private CharSequence sPrevSearchTerm = null;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Implementation not required
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Check if search term is same as the previous one
            if (sPrevSearchTerm != null && sPrevSearchTerm.equals(s)) {
                return;
            }
            sPrevSearchTerm = s;

            Log.d(LOG_TAG, "onTextChanged: " + s);
            cancelCurrentRequests();
            initNewRequests(s);
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Implementation not required
        }
    }
}