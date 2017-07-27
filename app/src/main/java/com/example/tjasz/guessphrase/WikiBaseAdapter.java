package com.example.tjasz.guessphrase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class WikiBaseAdapter extends BaseAdapter {

    private Context myContext;
    private static LayoutInflater inflater;
    private ArrayList<String> arrayList;

    public WikiBaseAdapter(Context context) {
        myContext = context;
        inflater = LayoutInflater.from(myContext);
        arrayList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public String getItem(int position) {
        return arrayList.get(position);
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
            newView = inflater.inflate(R.layout.view_wiki_base, parent, false);
        }

        TextView wikiBaseTextView = (TextView) newView.findViewById(R.id.wiki_base_text_view);
        // set the value of the DelayAutoCompleteTextView if necessary
        String curr = arrayList.get(position);
        wikiBaseTextView.setText(curr);
        // set onClick of the remove button
        Button removeButton = (Button) newView.findViewById(R.id.remove_wiki_base_button);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayList.remove(thisPos);
                notifyDataSetChanged();
            }
        });

        return newView;
    }

    public void add(String str) {
        arrayList.add(str);
        notifyDataSetChanged();
    }

}
