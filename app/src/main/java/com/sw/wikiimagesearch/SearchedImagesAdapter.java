package com.sw.wikiimagesearch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

/**
 * Created by swapnil on 5/31/16.
 */
public class SearchedImagesAdapter extends RecyclerView.Adapter<SearchedImagesAdapter.SearchedImageHolder> {

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
        if(searchResult.getThumbnailSource() != null) {
            holder.sSearchedImageView.setImageUrl(searchResult.getThumbnailSource(), sImageLoader);
        }
    }

    @Override
    public int getItemCount() {
        return sSearchResults.size();
    }

    public static class SearchedImageHolder extends  RecyclerView.ViewHolder {

        private NetworkImageView sSearchedImageView;


        public SearchedImageHolder(View view) {
            super(view);
            this.sSearchedImageView = (NetworkImageView) view.findViewById(R.id.searched_image);
        }
    }
}