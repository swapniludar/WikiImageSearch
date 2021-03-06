package com.sw.wikiimagesearch;

import android.support.annotation.NonNull;

/**
 * Search result POJO
 * @author Swapnil Udar
 */
public class SearchResult implements Comparable<SearchResult> {
    private final int sIndex;
    private final String sTitle;
    private final String sImageSource;

    public SearchResult(int index, String title, String imageSource) {
        this.sIndex = index;
        this.sTitle = title;
        this.sImageSource = imageSource;
    }

    public int getIndex() {
        return sIndex;
    }

    public String getTitle() {
        return sTitle;
    }

    public String getImageSource() {
        return sImageSource;
    }

    @Override
    public int compareTo(@NonNull  SearchResult another) {
        return getIndex() >= another.getIndex() ? 1 : -1;
    }

    @Override
    public String toString() {
        return getIndex() + ", " + getTitle() + ", " + (getImageSource() != null ? getImageSource() : "");
    }
}