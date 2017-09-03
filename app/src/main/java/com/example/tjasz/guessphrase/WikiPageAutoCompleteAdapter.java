package com.example.tjasz.guessphrase;

// a custom adapter for use in auto-complete text views
// http://makovkastar.github.io/blog/2014/04/12/android-autocompletetextview-with-suggestions-from-a-web-service/

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public class WikiPageAutoCompleteAdapter extends BaseAdapter implements Filterable {
    private static final int MAX_RESULTS = 10;
    private Context myContext;
    private List<String> resultList = new ArrayList<>();

    public WikiPageAutoCompleteAdapter(Context context) {
        myContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView result = new TextView(myContext);
        result.setText(getItem(position));
        return result;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            // note: invoked in a worker thread automatically
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<String> pages = new ArrayList<>(Wikipedia.search(constraint.toString(), MAX_RESULTS));
                    // assign the data to the FilterResults
                    filterResults.values = pages;
                    filterResults.count = pages.size();
                }
                return filterResults;
            }

            @Override
            // note: takes place in UI thread
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<String>) results.values;
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
