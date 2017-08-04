package com.example.tjasz.guessphrase;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
        final int thisPos = position;

        if (convertView == null) {
            newView = inflater.inflate(R.layout.view_category_reference, parent, false);
        }

        Category curr = list.get(position);
        TextView categoryTitleTextView = (TextView) newView.findViewById(R.id.category_title);
        ImageView deleteIcon = (ImageView) newView.findViewById(R.id.delete_category_icon);
        categoryTitleTextView.setText(curr.getName());
        if (curr.getIsCustom()) {
            categoryTitleTextView.setTextColor(Color.rgb(0x60,0x60,0xb0));
            // set onClick of the remove button
            deleteIcon.setVisibility(View.VISIBLE);
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Category cat = list.get(thisPos);
                    cat.deleteFile();
                    list.remove(thisPos);
                    notifyDataSetChanged();
                }
            });
        }
        else {
            categoryTitleTextView.setTextColor(Color.rgb(0x60,0x60,0x60));
            deleteIcon.setVisibility(View.GONE);
        }

        return newView;
    }

    public void add(Category cat) {
        list.add(cat);
    }

    public void addAll(Collection<Category> items) {
        list.addAll(items);
    }

    public void removeAtPosition(int position) {
        list.remove(position);
    }

    public void removeAll() {
        list.clear();
    }
}
