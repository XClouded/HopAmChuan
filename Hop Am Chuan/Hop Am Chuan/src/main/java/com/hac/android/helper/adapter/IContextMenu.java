package com.hac.android.helper.adapter;

/**
 * Created by Dinh Quang Trung on 1/3/14.
 */

import android.view.View;
import android.widget.ImageView;

import com.hac.android.model.Song;

/** interface */
public interface IContextMenu {
    public void onMenuClick(View view, Song song, ImageView theStar);
}
