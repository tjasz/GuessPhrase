package com.example.tjasz.guessphrase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

public class CategoryReferenceAdapter extends BaseAdapter {
    private Context myContext;
    private static LayoutInflater inflater = null;
    private ArrayList<Category> list;

    public CategoryReferenceAdapter(Context context) {
        myContext = context;
        inflater = LayoutInflater.from(myContext);
        list = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Category getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View newView = convertView;

        if (convertView == null) {
            newView = inflater.inflate(R.layout.category_reference_view, parent, false);
        }

        Category curr = list.get(position);
        TextView categoryTitleTextView = (TextView) newView.findViewById(R.id.category_title);
        categoryTitleTextView.setText(curr.getName());

        return newView;
    }

    public void add(Category cat) {
        list.add(cat);
    }

    public void addAll(Collection<Category> items) {
        list.addAll(items);
    }

    public void removeAll() {
        list.clear();
    }
}
