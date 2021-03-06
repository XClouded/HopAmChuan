package com.hac.android.guitarchord.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.hac.android.config.Config;
import com.hac.android.guitarchord.BunnyApplication;
import com.hac.android.guitarchord.MainActivity;
import com.hac.android.helper.adapter.IContextMenu;
import com.hac.android.helper.adapter.SongListAdapter;
import com.hac.android.helper.widget.InfinityListView;
import com.hac.android.helper.widget.SongListRightMenuHandler;
import com.hac.android.model.Artist;
import com.hac.android.model.Song;
import com.hac.android.model.dal.ArtistDataAccessLayer;
import com.hac.android.utils.DialogUtils;
import com.hac.android.guitarchord.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fragment to show Artist Song's
 * Created by ThaoHQSE60963 on 1/9/14.
 */
public class ArtistViewFragment extends CustomFragment implements InfinityListView.ILoaderContent {

    /** Activity running this fragment */
    MainActivity activity;

    List<Song> songs;
    SongListAdapter songlistAdapter;
    private InfinityListView mListView;
    private PopupWindow popupWindow;
    private Artist artist;

    public ArtistViewFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;

        // get artist from arguments of main mActivity
        Bundle arguments = getArguments();
        if ((arguments.get("artist") != null)) {
            artist = arguments.getParcelable("artist");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_view, container, false);

        songs = new ArrayList<Song>();
        songlistAdapter = new SongListAdapter(BunnyApplication.getAppContext(), songs);

        /** ListView Configure */
        mListView = (InfinityListView) rootView.findViewById(R.id.mListView);
        /** config mode for this ListView.
         *  this ListView is full rich function. See document for more detail
         */
        InfinityListView.ListViewProperty property = new InfinityListView.ListViewProperty();
        property.Loader(this).Adapter(songlistAdapter).FirstProcessLoading(true).LoadingView(R.layout.list_item_loading)
                .NumPerLoading(Config.DEFAULT_SONG_NUM_PER_LOAD).RunningBackground(true);
        mListView.setListViewProperty(property);
        mListView.resetListView(songlistAdapter);
        mListView.setEmptyView(rootView.findViewById(R.id.empty));

        // Event for right menu click
        popupWindow = DialogUtils.createPopup(inflater, R.layout.popup_songlist_menu);
        SongListRightMenuHandler.setRightMenuEvents(getActivity(), popupWindow);

        // Event received from mAdapter.
        songlistAdapter.contextMenuDelegate = new IContextMenu() {
            @Override
            public void onMenuClick(View view, Song song, ImageView theStar) {
                // Show the popup menu and set selectedSong, theStar
                SongListRightMenuHandler.openPopupMenu(view, song, theStar);
            }
        };

        // Event for Item Click on ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SongDetailFragment fragment = new SongDetailFragment();
                Bundle arguments = new Bundle();
                arguments.putParcelable("song", songs.get(position));
                fragment.setArguments(arguments);
                activity.switchFragmentNormal(fragment);
                activity.changeTitleBar(songs.get(position).title);
            }
        });
        return rootView;

    }

    @Override
    public Collection load(int offset, int count) {
        return ArtistDataAccessLayer.searchSongByArtist(artist.artistName, offset, count);
    }

    @Override
    public int getTitle() {
        return 0;
    }
}
