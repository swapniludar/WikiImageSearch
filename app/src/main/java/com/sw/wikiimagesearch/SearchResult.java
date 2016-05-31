package com.sw.wikiimagesearch;

import java.util.Comparator;

/**
 * Created by Swapnil on 5/31/16.
 */
public class SearchResult implements Comparable<SearchResult> {
    private final int sIndex;
    private final String sTitle;
    private final String sThumbnailSource;

    public SearchResult(int index, String title, String thumbnailSource) {
        this.sIndex = index;
        this.sTitle = title;
        this.sThumbnailSource = thumbnailSource;
    }

    public int getIndex() {
        return sIndex;
    }

    public String getTitle() {
        return sTitle;
    }

    public String getThumbnailSource() {
        return sThumbnailSource;
    }

    @Override
    public int compareTo(SearchResult another) {
        return getIndex() >= another.getIndex() ? 1 : -1;
    }

    @Override
    public String toString() {
        return getIndex() + ", " + getTitle() + ", " + (getThumbnailSource() != null ? getThumbnailSource() : "");
    }
}