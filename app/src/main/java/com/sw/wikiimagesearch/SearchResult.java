package com.sw.wikiimagesearch;

import java.util.Comparator;

/**
 * Created by Swapnil on 5/31/16.
 */
public class SearchResult implements Comparator<SearchResult> {
    private final int sIndex;
    private final String sTitle;
    private final String sThumbnailURL;

    public SearchResult(int index, String title, String thumbnailURL) {
        this.sIndex = index;
        this.sTitle = title;
        this.sThumbnailURL = thumbnailURL;
    }

    public int getIndex() {
        return sIndex;
    }

    public String getTitle() {
        return sTitle;
    }

    public String getThumbnailURL() {
        return sThumbnailURL;
    }

    @Override
    public int compare(SearchResult lhs, SearchResult rhs) {
        return lhs.getIndex() <= rhs.getIndex() ? 1 : -1;
    }

    @Override
    public String toString() {
        return getIndex() + ", " + getTitle() + ", " + (getThumbnailURL() != null ? getThumbnailURL() : "");
    }
}