package com.hac.android.helper.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter that manages items by its Ids.
 * this manage will make it more robust than ever.
 * Learn this method from the speaking :
 *https://www.youtube.com/playlist?list=PLWz5rJ2EKKc_XOgcRukSoKKjewFJZrKV0
 *
 * Created by ThaoHQSE60963 on 1/11/14.
 */
public class StableArrayAdapter extends ArrayAdapter<String> {

    Map<String, Integer> mIdMap = new HashMap<String, Integer>();
    View.OnTouchListener mTouchListener;

    public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects, View.OnTouchListener listener) {
        super(context, textViewResourceId, objects);
        mTouchListener = listener;
        for (int i = 0; i < objects.size(); i++) {
            mIdMap.put(objects.get(i), i);
        }
    }

    @Override
    public long getItemId(int position) {
        String item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (view != convertView) {
            // Add touch listener to every new view to track swipe motion
            view.setOnTouchListener(mTouchListener);
        }
        return view;
    }
}
