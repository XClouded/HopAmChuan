package com.hac.android.helper.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.hac.android.config.Config;
import com.hac.android.helper.adapter.NavigationDrawerAdapter;
import com.hac.android.helper.adapter.PlaylistListAdapter;
import com.hac.android.helper.adapter.PlaylistManagerAdapter;
import com.hac.android.helper.adapter.SongListAdapter;
import com.hac.android.model.Playlist;
import com.hac.android.model.Song;
import com.hac.android.model.dal.FavoriteDataAccessLayer;
import com.hac.android.model.dal.PlaylistDataAccessLayer;
import com.hac.android.model.dal.PlaylistSongDataAccessLayer;
import com.hac.android.utils.DialogUtils;
import com.hac.android.guitarchord.R;

import java.util.Date;
import java.util.List;

/**
 * Control action when click popup menu on Songlist Fragment
 */
public class SongListRightMenuHandler {
    /**
     * Current selected song for song lists (favorite fragment, song list fragment,
     * playlist detail fragment **/
    public static Song selectedSong = null;

    /*** The ImageView for visual feedback when user like/unlike a song */
    public static ImageView theStar;

    /*** The list view control for Playlist */
    public static ListView mListView;

    /*** Playlist list for "Add to playlist" dialog */
    public static List<Playlist> playlists;

    /*** Activity for dialogs */
    public static Activity activity;

    /*** The popup window */
    private static PopupWindow popupWindow;

    /*** The popup window's controls */
    private static Button favoriteBtn;
    private static Button playlistBtn;


    /*** Dialog for playlist list */
    private static Dialog playlistListDialog;

    /*** Dialog for new playlist */
    public static Dialog newPlaylistDialog;

    /*** New playlist dialog controls */
    public static EditText txtNewPlaylistName;
    public static EditText txtNewPlaylistDescription;

    /*** Adapter for playlist list */
    private static PlaylistListAdapter playlistAdapter;

    /** Adapter for PlaylistManagerFragment (for feedback effect when data changed) **/
    public static PlaylistManagerAdapter playlistManagerAdapter;

    /** Adapter in NavigationDrawerFragment to update changes of playlists **/
    public static NavigationDrawerAdapter.PlaylistItemAdapter navDrawerPlaylistItemAdapter;


    public static void setRightMenuEvents(final Activity _activity, final PopupWindow _pw) {

        activity = _activity;
        popupWindow = _pw;

        // Popup menu item
        favoriteBtn = (Button) popupWindow.getContentView().findViewById(R.id.song_list_menu_addtofavorite);
        playlistBtn = (Button) popupWindow.getContentView().findViewById(R.id.song_list_menu_addtoplaylist);
        Button shareBtn = (Button) popupWindow.getContentView().findViewById(R.id.song_list_menu_share);

        // "Add to Favorite" button
        favoriteBtn.setOnClickListener(new ToggleFavorite());

        // "Add to playlist" dialog
        playlistListDialog = DialogUtils.createDialog(activity, R.string.title_add_to_playlist_dialog,
                activity.getLayoutInflater(), R.layout.dialog_addtoplaylist);

        mListView = (ListView) playlistListDialog.findViewById(R.id.playlist_list);

        playlists = PlaylistDataAccessLayer.getAllPlayLists(activity.getApplicationContext());

        playlistAdapter = new PlaylistListAdapter(activity, playlists);
        mListView.setAdapter(playlistAdapter);

        // Add click event item for this ListView
        mListView.setOnItemClickListener(new AddToPlaylistOnClick());

        /***** New playlist dialog *****/
        newPlaylistDialog = DialogUtils.createDialog(activity, R.string.new_playlist,
                activity.getLayoutInflater(), R.layout.dialog_newplaylist);

        Button createPlaylistBtn = (Button) newPlaylistDialog.findViewById(R.id.btnCreatePlaylist);

        createPlaylistBtn.setOnClickListener(new NewPlaylistOnClick());
        /**************/

        // Event to add new playlist
        LinearLayout addNewPlaylistBtn = (LinearLayout) playlistListDialog.findViewById(R.id.playlist_list_header);

        txtNewPlaylistName = (EditText) newPlaylistDialog.findViewById(R.id.txtNewPlaylistName);
        txtNewPlaylistDescription = (EditText) newPlaylistDialog.findViewById(R.id.txtNewPlaylistDescription);

        addNewPlaylistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtNewPlaylistName.setText("");
                txtNewPlaylistDescription.setText("");
                txtNewPlaylistName.requestFocus();
                newPlaylistDialog.show();
            }
        });

        // Playlist button
        playlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                playlistListDialog.setTitle(R.string.title_add_to_playlist_dialog);

                // Refresh playlists
                playlistManagerAdapter = null;
                playlistAdapter.setPlaylists(PlaylistDataAccessLayer.getAllPlayLists(activity.getApplicationContext()));
                playlistListDialog.show();
            }
        });

        // Share button
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareText = String.format(activity.getString(R.string.share_text_template),
                        selectedSong.title,
                        selectedSong.songId + "/" + selectedSong.titleAscii.replaceAll(" ", "-"),
                        Config.GOOGLE_PLAY_REF_LINK + activity.getApplicationContext().getPackageName());

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                sendIntent.setType("text/plain");
                activity.startActivity(Intent.createChooser(sendIntent, activity.getString(R.string.send_to)));
                popupWindow.dismiss();
            }
        });
    }

    public static void openPopupMenu(View _view, Song _song, ImageView _theStar) {
        /** Store the song that user clicked on the right menu **/
        selectedSong = _song;
        /** The Image View for visual feedback as user selection **/
        theStar = _theStar;

        /** Set label for favorite button **/
        if (_song.isFavorite == 0) {
            favoriteBtn.setText(R.string.song_detail_menu_favorite);
        } else {
            favoriteBtn.setText(R.string.song_detail_menu_unfavorite);
        }

        popupWindow.showAsDropDown(_view);
    }

    public static void openPopupMenu(View _view, final Song _song, ImageView _theStar,
                                     final int playlistId, final SongListAdapter mAdapter) {
        playlistBtn.setText(R.string.remove_from_playlist);

        // Override add playlist button
        playlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();

                // Remove from playlist
                PlaylistSongDataAccessLayer.removePlaylist_Song(
                        activity.getApplicationContext(), playlistId, _song.songId);

                // Update UI
                mAdapter.remove(_song.songId);
                updateNavDrawerPlaylistList(PlaylistDataAccessLayer.getAllPlayLists(activity.getApplicationContext()));

            }
        });

        openPopupMenu(_view, _song, _theStar);
    }

    private static class AddToPlaylistOnClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Insert
            PlaylistSongDataAccessLayer.insertPlaylist_Song(
                    activity.getApplicationContext(),
                    playlists.get(position).playlistId, selectedSong.songId);

            // Show message
            Toast msg = Toast.makeText(activity.getApplicationContext(),
                    activity.getString(R.string.add_to_playlist_success)
                            + " " + playlists.get(position).playlistName,
                    Toast.LENGTH_LONG);
            msg.show();

            updateNavDrawerPlaylistList(PlaylistDataAccessLayer.getAllPlayLists(activity.getApplicationContext()));

            // Close dialog
            playlistListDialog.dismiss();
        }
    }

    public static class NewPlaylistOnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (txtNewPlaylistName.getText().toString().isEmpty()) {
                Toast msg = Toast.makeText(activity.getApplicationContext(),
                        activity.getString(R.string.please_enter_playlist_name),
                        Toast.LENGTH_LONG);
                msg.show();
            } else {
                // Add new playlist
                Playlist newPlaylist = new Playlist(Config.DEFAULT_PLAYLIST_ID_INSERTED_BY_USER,
                        txtNewPlaylistName.getText().toString(),
                        txtNewPlaylistDescription.getText().toString(),
                        new Date(),
                        1);

                PlaylistDataAccessLayer.insertPlaylist(activity.getApplicationContext(), newPlaylist);

                Toast msg = Toast.makeText(activity.getApplicationContext(),
                        activity.getString(R.string.create_playlist_success) + " " +newPlaylist.playlistName,
                        Toast.LENGTH_LONG);
                msg.show();

                // Refresh playlist list
                // We have to re-set the adapter for onItemClick event.
                playlists = PlaylistDataAccessLayer.getAllPlayLists(activity.getApplicationContext());

                // If the dialog is called in Playlist Manager, then reset the right adapter
                if (playlistManagerAdapter != null) {
                    playlistManagerAdapter.playLists = playlists;
                    playlistManagerAdapter.notifyDataSetChanged();
                } else {
                    // If the dialog is called in Add to playlist popup. Then reset the adapter.
                    playlistAdapter = new PlaylistListAdapter(activity, playlists);
                    mListView.setAdapter(playlistAdapter);
                    mListView.setOnItemClickListener(new AddToPlaylistOnClick());
                }

                // Update item on navigation drawer adapter
                updateNavDrawerPlaylistList(playlists);

                // Close dialog
                newPlaylistDialog.dismiss();
            }
        }
    }

    private static class ToggleFavorite implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // If already in favorite
            if (FavoriteDataAccessLayer.isInFavorite(
                    activity.getApplicationContext(), selectedSong.songId) > 0) {
                FavoriteDataAccessLayer.removeSongFromFavorite(
                        activity.getApplicationContext(), selectedSong.songId);

                Toast msg = Toast.makeText(activity.getApplicationContext(),
                        activity.getString(R.string.removed_from_favorite),
                        Toast.LENGTH_LONG);
                msg.show();

                // Update UI
                theStar.setImageResource(R.drawable.star);
                selectedSong.isFavorite = 0;
            }
            // If not in favorite
            else {
                FavoriteDataAccessLayer.addSongToFavorite(activity.getApplicationContext(),
                        selectedSong.songId);
                Toast msg = Toast.makeText(activity.getApplicationContext(),
                        activity.getString(R.string.added_to_favorite),
                        Toast.LENGTH_LONG);
                msg.show();

                // Update UI
                theStar.setImageResource(R.drawable.star_liked);
                selectedSong.isFavorite = (new Date()).getTime();
            }

            popupWindow.dismiss();
        }
    }

    public static void updateNavDrawerPlaylistList(List<Playlist> playlists) {
        // LOGE("TRUNGDQ", "nav: " + navDrawerPlaylistItemAdapter);
        if (navDrawerPlaylistItemAdapter != null) {
            navDrawerPlaylistItemAdapter.playlists = playlists;
            navDrawerPlaylistItemAdapter.notifyDataSetChanged();
        }
    }
}
