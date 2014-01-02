package com.hqt.hac.helper.adapter;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import static com.hqt.hac.utils.LogUtils.LOGE;
import static com.hqt.hac.utils.LogUtils.makeLogTag;

public abstract class ChordViewAdapter extends BaseAdapter implements SectionIndexer, IChordView {

    public static String TAG = makeLogTag(ChordViewAdapter.class);

    protected Context mContext;

    /** List all chords that adapter contains */
    protected String[] chords;

    /** currently index of chord */
    protected int[] index;

    /** String that using for SectionIndexer */
    protected static String sectionStr = "A,Am,B,Bm,C,Cm";
    protected String[] sections = sectionStr.split(",");

    public ChordViewAdapter(Context mContext, String[] chords) {
        this.mContext = mContext.getApplicationContext();
        this.chords = chords;
        index = new int[chords.length];
    }

    public void setChordList(String[] chords) {
        this.chords = chords;
        index = new int[chords.length];
    }

    @Override
    public int getCount() {
        return chords.length;
    }

    @Override
    public Object getItem(int position) {
        return chords[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        for (int i=0; i < chords.length; i++) {
            String item = chords[i];
            if (item.length() >= 2) item = item.substring(0, 2);
            LOGE(TAG, "SubString: " + item);
            if (item.toLowerCase().equals(sections[section].toLowerCase())) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }
}
