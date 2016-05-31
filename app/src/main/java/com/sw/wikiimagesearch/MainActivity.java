package com.sw.wikiimagesearch;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // TODO: Move it to external property
    private static final String URL = "https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=50&pilimit=50&generator=prefixsearch&gpssearch=";
    private static final String REQ_TAG = "WikiImageSearch";

    private EditText sSearchTextField;
    private TextWatcher sSearchFieldWatcher;
    private RequestQueue sRequestQueue;
    private ImageLoader sImageLoader;

    private RecyclerView sRecyclerView;
    private RecyclerView.Adapter sAdapter;
    private RecyclerView.LayoutManager sLayoutManager;

    private List<SearchResult> sSearchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        sSearchTextField = (EditText) findViewById(R.id.search_text);
        sSearchFieldWatcher = new SearchFieldWatcher();
        sSearchTextField.addTextChangedListener(sSearchFieldWatcher);

        sRequestQueue = Volley.newRequestQueue(this);
        sImageLoader = new ImageLoader(sRequestQueue, new LruBitmapCache(this));

        sRecyclerView = (RecyclerView) findViewById(R.id.images_list_view);

        sLayoutManager = new LinearLayoutManager(this);
        sRecyclerView.setLayoutManager(sLayoutManager);

        sAdapter = new SearchedImagesAdapter(sSearchResults, sImageLoader);
        sRecyclerView.setAdapter(sAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void cancelCurrentRequests() {
        // Cancel current requests
        if(sRequestQueue != null) {
            sRequestQueue.cancelAll(REQ_TAG);
        }
        sSearchResults.clear();
        sAdapter.notifyDataSetChanged();
        Log.d(LOG_TAG, "cancelCurrentRequests: Cancelled current requests");
    }

    private void initNewRequests(CharSequence searchQuery) {
        String url = URL + searchQuery;
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

            JSONObject searchItem = null;
            int index;
            String title = null;
            String thumbnailSource = null;

            for (int i = 0; i < pageNames.length(); i++) {
                searchItem = pages.getJSONObject(pageNames.getString(i));
                index = searchItem.getInt("index");
                title = searchItem.getString("title");
                thumbnailSource = searchItem.has("thumbnail") ? searchItem.getJSONObject("thumbnail")
                        .getString("source") : null;
                sSearchResults.add(new SearchResult(index, title, thumbnailSource));
            }
            Collections.sort(sSearchResults);
            Log.d(LOG_TAG, "parseResponse: " + sSearchResults.toString());
        }
    }

    private void loadImages() {

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
}