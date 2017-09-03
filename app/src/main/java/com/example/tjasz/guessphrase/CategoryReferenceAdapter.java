package com.example.tjasz.guessphrase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

class CategoryReferenceAdapter extends BaseAdapter {
    private Context myContext;
    private static LayoutInflater inflater = null;
    private ArrayList<Category> list;

    CategoryReferenceAdapter(Context context) {
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
                    final Category cat = list.get(thisPos);
                    // confirm deletion with a dialog
                    AlertDialog alertDialog = new AlertDialog.Builder(myContext).create();
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.setTitle(R.string.confirm_delete_dialog_title);
                    String message = myContext.getResources().getString(R.string.confirm_delete_dialog_head) +
                            cat.getName() +
                            myContext.getResources().getString(R.string.confirm_delete_dialog_tail);
                    // if game save file exists and uses this category
                    // append a warning to the confirmation message
                    // and delete game save file if deletion of category is confirmed
                    if (cat.isInSavedGame()) {
                        message += "\n\n" + myContext.getResources().getString(R.string.warning_category_in_saved_game);
                        // this positive button will delete the category file and the game save file
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, myContext.getResources().getString(R.string.okay),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // delete file
                                        File game_save_file = myContext.getFileStreamPath(myContext.getResources()
                                                .getString(R.string.game_save_file_name));
                                        if (!(game_save_file.delete())) {
                                            throw new RuntimeException("Failed to delete file " + game_save_file.getPath());
                                        }
                                        cat.deleteFile();
                                        list.remove(thisPos);
                                        notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                });
                    }
                    // otherwise, only delete category if confirmed
                    else {
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, myContext.getResources().getString(R.string.okay),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // delete file
                                        cat.deleteFile();
                                        list.remove(thisPos);
                                        notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                });
                    }
                    alertDialog.setMessage(message);
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, myContext.getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
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

    void addAll(Collection<Category> items) {
        list.addAll(items);
    }

    public void removeAtPosition(int position) {
        list.remove(position);
    }

    void removeAll() {
        list.clear();
    }
}
