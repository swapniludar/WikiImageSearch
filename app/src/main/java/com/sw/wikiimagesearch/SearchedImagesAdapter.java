package com.sw.wikiimagesearch;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by swapnil on 5/31/16.
 */
public class SearchedImagesAdapter extends RecyclerView.Adapter<SearchedImagesAdapter.SearchedImageHolder> {

    private static final String DEFAULT_IMAGE = "https://placeholdit.imgix.net/~text?txtsize=19&w=200&h=200";

    private final List<SearchResult> sSearchResults;
    private final ImageLoader sImageLoader;

    public SearchedImagesAdapter(List<SearchResult> searchResults, ImageLoader imageLoader) {
        super();
        this.sSearchResults = searchResults;
        this.sImageLoader = imageLoader;
    }

    @Override
    public SearchedImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result, parent, false);
        return new SearchedImageHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchedImageHolder holder, int position) {
        SearchResult searchResult = (sSearchResults.get(position));
        // when image source url is included in the result
        if (searchResult.getImageSource() != null) {
            holder.sSearchedImageView.setImageUrl(searchResult.getImageSource(), sImageLoader);
        } else {
            // load dummy image
            try {
                holder.sSearchedImageView.setImageUrl(
                        appendImageTextParam(DEFAULT_IMAGE, searchResult.getTitle()), sImageLoader);
            } catch (UnsupportedEncodingException uee) {
                // Load image without text
                holder.sSearchedImageView.setImageUrl(DEFAULT_IMAGE, sImageLoader);
            }
        }
    }

    private String appendImageTextParam(String url, String title) throws UnsupportedEncodingException {
        return url + "&txt=" + URLEncoder.encode(title, "UTF-8");
    }

    @Override
    public int getItemCount() {
        return sSearchResults.size();
    }

    public static class SearchedImageHolder extends RecyclerView.ViewHolder {

        private NetworkImageView sSearchedImageView;


        public SearchedImageHolder(View view) {
            super(view);
            this.sSearchedImageView = (NetworkImageView) view.findViewById(R.id.searched_image);
        }
    }
}