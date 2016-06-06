package com.sw.wikiimagesearch;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Adapter for recycler view
 *
 * @author Swapnil Udar
 */
public class SearchedImagesAdapter extends RecyclerView.Adapter<SearchedImagesAdapter.SearchedImageHolder> {
    private static final String LOG_TAG = SearchedImagesAdapter.class.getSimpleName();

    private static final String DEFAULT_IMAGE = "https://placeholdit.imgix.net/~text?txtsize=%d&w=%d&h=%d";


    private final List<SearchResult> sSearchResults;
    private final ImageLoader sImageLoader;
    private final int sImageSize;
    private final String sModifiedDefImageURL;

    public SearchedImagesAdapter(List<SearchResult> searchResults, ImageLoader imageLoader, int imageSize) {
        super();
        this.sSearchResults = searchResults;
        this.sImageLoader = imageLoader;
        this.sImageSize = imageSize;
        this.sModifiedDefImageURL = updateDefImageParams();
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
                        updateImageText(searchResult.getTitle()), sImageLoader);
            } catch (UnsupportedEncodingException uee) {
                // Load image without text
                holder.sSearchedImageView.setImageUrl(sModifiedDefImageURL, sImageLoader);
            }
        }
        // update image view width and height
        ViewGroup.LayoutParams layoutParams = holder.sSearchedImageView.getLayoutParams();
        layoutParams.width = layoutParams.height = sImageSize;
        holder.sSearchedImageView.setLayoutParams(layoutParams);
    }

    // Set font size over image
    private String updateDefImageParams() {
        int textSize = sImageSize / 10;
        return String.format(DEFAULT_IMAGE, textSize, sImageSize, sImageSize);
    }

    private String updateImageText(String title) throws UnsupportedEncodingException {
        String url = sModifiedDefImageURL + "&txt=" + URLEncoder.encode(title, "UTF-8");
        Log.d(LOG_TAG, "Default image URL: " + url);
        return url;
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