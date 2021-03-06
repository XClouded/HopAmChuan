package com.hac.android.helper.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hac.android.config.PrefStore;
import com.hac.android.guitarchord.LoginActivity;
import com.hac.android.guitarchord.MainActivity;
import com.hac.android.guitarchord.fragment.PlaylistManagerFragment;
import com.hac.android.guitarchord.popup.ProfilePopup;
import com.hac.android.model.Playlist;
import com.hac.android.model.dal.PlaylistDataAccessLayer;
import com.hac.android.utils.EncodingUtils;
import com.hac.android.utils.HacUtils;
import com.hac.android.utils.LogUtils;
import com.hac.android.guitarchord.R;

import java.util.List;

public class NavigationDrawerAdapter {

    private static String TAG = LogUtils.makeLogTag(NavigationDrawerAdapter.class);


    public static class HeaderAdapter extends BaseAdapter {

        private static String TAG = LogUtils.makeLogTag(HeaderAdapter.class);

        Context mContext;
        Activity activity;
        IHeaderDelegate delegate;

        public HeaderAdapter(Activity activity) {
            this.activity = activity;
            this.mContext = activity.getApplicationContext();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderHeader holder = null;
            View row = convertView;

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (row == null) {
                row = inflater.inflate(R.layout.list_item_header_1, null);
                holder = new ViewHolderHeader();
                holder.txtName = (TextView) row.findViewById(R.id.name);
                holder.txtMail = (TextView) row.findViewById(R.id.mail);
                holder.imgAvatar = (ImageView) row.findViewById(R.id.imageView);
                row.setTag(holder);
            }
            else {
                holder = (ViewHolderHeader) row.getTag();
            }
            String username = PrefStore.getLoginUsername();
            String email = PrefStore.getEmail();

            if (username.isEmpty()) username = activity.getString(R.string.login_account);
            if (email.isEmpty()) email = activity.getString(R.string.login_account_description);

            holder.txtName.setText(username);
            holder.txtMail.setText(email);

            // Makes the marquee running
            holder.txtMail.setSelected(true);
            holder.txtName.setSelected(true);

            Bitmap imageAvatar = EncodingUtils.decodeByteToBitmap(PrefStore.getUserImage());
            if (imageAvatar != null) {
                holder.imgAvatar.setImageBitmap(imageAvatar);
            } else {
                holder.imgAvatar.setImageResource(R.drawable.default_avatar);
            }

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TrungDQ: if you user has logged in, then display Logout popup
                    if (!HacUtils.isLoggedIn()) {
                        // start Login Activity
                        Intent intent = new Intent(activity, LoginActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    } else {
                        // Start logout mActivity or popup here.
                        ProfilePopup profilePopup = new ProfilePopup(activity);
                        profilePopup.show();
                    }
                }
            });

            return row;
        }

        public void setDelegate(IHeaderDelegate delegate) {
            this.delegate = delegate;
        }
    }

    public static class ItemAdapter extends BaseAdapter {

        private static String TAG = LogUtils.makeLogTag(ItemAdapter.class);

        public enum TYPE {
            HOME,
            SONGS,
            MYPLAYLIST,
            FAVORITE,
            FIND_BY_CHORD,
            SEARCH_CHORD,
            SETTING
        }

        Context mContext;
        String[] categories;
        IItemDelegate mDelegate;

        public ItemAdapter(Context context) {
            this.mContext = context.getApplicationContext();
            categories = context.getResources().getStringArray(R.array.navigation_drawer_default_items);
        }

        @Override
        public int getCount() {
            return categories.length;
        }

        @Override
        public Object getItem(int position) {
            return categories[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderItem holder = null;
            View row = convertView;

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (row == null) {
                row = inflater.inflate(R.layout.list_item_navigation_drawer_1, null);
                holder = new ViewHolderItem();
                holder.txtView = (TextView) row.findViewById(R.id.text);
                holder.imageView = (ImageView) row.findViewById(R.id.icon);

                row.setTag(holder);
            }
            else {
                holder = (ViewHolderItem) row.getTag();
            }

            // assign value to holder
            TYPE type = null;
            try {
                holder.txtView.setText(categories[position]);
            }
            catch (Exception e) {
                if (holder == null) Log.e("Huynh Quang Thao", "Silly Error");
            }
            if (holder != null) {
                switch(position) {
                    case 0:
                        // Trang chu
                        holder.imageView.setImageResource(R.drawable.home_icon);
                        type = TYPE.HOME;
                        break;
                    case 1:
                        // Bài hát
                        holder.imageView.setImageResource(R.drawable.songs_icon);
                        type = TYPE.SONGS;
                        break;
                    case 2:
                        // Playlist cua toi
                        holder.imageView.setImageResource(R.drawable.playlist_icon);
                        type = TYPE.MYPLAYLIST;
                        break;
                    case 3:
                        // Yeu Thich
                        holder.imageView.setImageResource(R.drawable.favorite_icon);
                        type = TYPE.FAVORITE;
                        break;
                    case 4:
                        // Tim Theo Hop Am
                        holder.imageView.setImageResource(R.drawable.search_icon);
                        type = TYPE.FIND_BY_CHORD;
                        break;
                    case 5:
                        // Tra cuu hop am
                        holder.imageView.setImageResource(R.drawable.chord_icon);
                        type = TYPE.SEARCH_CHORD;
                        break;
                    case 6:
                        // Cai dat
                        holder.imageView.setImageResource(R.drawable.ic_action_settings);
                        type = TYPE.SETTING;
                        break;
                }
            }
            final TYPE finalType = type;
            final int finalPosition = position;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDelegate.gotoCategoryPage(finalType, finalPosition);
                }
            });
            return row;
        }

        public void setDelegate(IItemDelegate mDelegate) {
            this.mDelegate = mDelegate;
        }
    }

    public static class PlaylistHeaderAdapter extends BaseAdapter {

        private static String TAG = LogUtils.makeLogTag(PlaylistHeaderAdapter.class);

        private Context mContext;
        IPlaylistHeaderDelegate delegate;

        public PlaylistHeaderAdapter(Context context) {
            this.mContext = context.getApplicationContext();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderPlaylistHeader holder = null;
            View row = convertView;

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (row == null) {
                row = inflater.inflate(R.layout.list_item_header_2, null);
                holder = new ViewHolderPlaylistHeader();
                holder.txtHeader = (TextView) row.findViewById(R.id.playlist_header);
                row.setTag(holder);
            }
            else {
                holder = (ViewHolderPlaylistHeader) row.getTag();
            }

            // assign value to view
            holder.txtHeader.setText(R.string.my_playlists_drawer_title);

            return row;
        }

        public void setDelegate(IPlaylistHeaderDelegate delegate) {
            this.delegate = delegate;
        }
    }

    public static class PlaylistItemAdapter extends BaseAdapter {

        private static String TAG = LogUtils.makeLogTag(PlaylistItemAdapter.class);

        Activity activity;
        IPlaylistItemDelegate delegate;
        public List<Playlist> playlists;

        public PlaylistItemAdapter(Activity activity) {
            this.activity = activity;
            // load all playlist
            playlists = PlaylistDataAccessLayer.getAllPlayLists(activity.getApplicationContext());
        }

        /** use this constructor for performance */
        public PlaylistItemAdapter(Activity activity, List<Playlist> playlists) {
            this.activity = activity;
            this.playlists = playlists;
        }

        @Override
        public int getCount() {
            if (playlists.size() == 0) return 1;
            return playlists.size();
        }

        @Override
        public Object getItem(int position) {
            return playlists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolderPlaylistItem holder = null;
            View row = convertView;
            LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (playlists.size() == 0) {
                row = inflater.inflate(R.layout.list_item_navigation_drawer_empty, null);
                if (row != null) {
                    row.findViewById(R.id.createPlaylistLayout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PlaylistManagerFragment fragment = new PlaylistManagerFragment();
                            Bundle arguments = new Bundle();
                            arguments.putBoolean("createPlaylist", true);
                            fragment.setArguments(arguments);
                            ((MainActivity) activity).switchFragmentClearStack(fragment);
                        }
                    });
                }
            } else {
                if (row != null && !(row.getTag() instanceof ViewHolderPlaylistItem)) row = null;
                if (row == null) {
                    row = inflater.inflate(R.layout.list_item_navigation_drawer_2, null);
                    holder = new ViewHolderPlaylistItem();
                    holder.txtTitle = (TextView) row.findViewById(R.id.title);
                    holder.txtDescription = (TextView) row.findViewById(R.id.description);
                    holder.txtNumberOfSong = (TextView) row.findViewById(R.id.countSongText);
                    row.setTag(holder);
                }
                else {
                    holder = (ViewHolderPlaylistItem) row.getTag();
                }

                // assign value to view
                Playlist p = playlists.get(position);
                holder.txtTitle.setText(p.playlistName);
                holder.txtDescription.setText(p.playlistDescription);
                holder.txtNumberOfSong.setText(p.numberOfSongs + "");

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delegate.gotoPlayList(position);
                    }
                });
            }

            return row;
        }

        public void setDelegate(IPlaylistItemDelegate delegate) {
            this.delegate = delegate;
        }
    }

    private static class ViewHolderItem {
        ImageView imageView;
        TextView txtView;
    }

    private static class ViewHolderPlaylistItem {
        TextView txtTitle;
        TextView txtDescription;
        TextView txtNumberOfSong;
    }

    private static class ViewHolderHeader {
        TextView txtName;
        TextView txtMail;
        ImageView imgAvatar;
    }

    private static class ViewHolderPlaylistHeader {
        TextView txtHeader;
    }

    /**
     * Interface acts as Callback
     * for NavigationDrawer decide actions
     * this stimulate Delegate Design Pattern often use in C#
     * @author Huynh Quang Thao
     */

    public static interface IHeaderDelegate {

    }

    public static interface IItemDelegate {
        void gotoCategoryPage(ItemAdapter.TYPE type, int position);

    }

    public static interface IPlaylistHeaderDelegate {

    }

    public static interface IPlaylistItemDelegate {
        void gotoPlayList(int playlistId);

    }


}

