package com.hac.android.guitarchord.test;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.hac.android.guitarchord.fragment.ChordViewFragment;
import com.hac.android.guitarchord.fragment.FavoriteManagerFragment;
import com.hac.android.guitarchord.fragment.PlaylistDetailFragment;
import com.hac.android.guitarchord.fragment.PlaylistManagerFragment;
import com.hac.android.guitarchord.fragment.SearchChordFragment;
import com.hac.android.guitarchord.fragment.SongListFragment;
import com.hac.android.guitarchord.fragment.WelcomeFragment;
import com.hac.android.helper.adapter.MergeAdapter;
import com.hac.android.helper.adapter.NavigationDrawerAdapter;
import com.hac.android.helper.widget.SlidingMenuActionBarActivity;
import com.hac.android.model.Playlist;
import com.hac.android.model.dal.PlaylistDataAccessLayer;
import com.hac.android.utils.LogUtils;
import com.hac.android.utils.UIUtils;
import com.hac.android.guitarchord.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.List;

public class MainActivityTest extends SlidingMenuActionBarActivity
        implements NavigationDrawerAdapter.IHeaderDelegate, NavigationDrawerAdapter.IItemDelegate,
        NavigationDrawerAdapter.IPlaylistHeaderDelegate, NavigationDrawerAdapter.IPlaylistItemDelegate {

    private static final String TAG = LogUtils.makeLogTag(MainActivityTest.class);

    /** SearchView widget */
    SearchView mSearchView;

    /** ListView contains all item categories */
    ListView mDrawerListView;

    /** SlidingMenu : use for slide to see like NavigationDrawer*/
    SlidingMenu slidingMenu;

    /** MainLayout of this mActivity */
    FrameLayout mainLayout;

    View sideBarLayout;

    /** Helper component that ties the action bar to the navigation drawer */
    private ActionBarDrawerToggle mDrawerToggle;

    /** All Adapter for Navigation Drawer */
    MergeAdapter mergeAdapter;
    NavigationDrawerAdapter.HeaderAdapter headerAdapter;
    NavigationDrawerAdapter.ItemAdapter itemAdapter;
    NavigationDrawerAdapter.PlaylistHeaderAdapter playlistHeaderAdapter;
    NavigationDrawerAdapter.PlaylistItemAdapter playlistItemAdapter;

    /**
     * All playlist currently on system
     * loading first for performance
     */
    List<Playlist> playlistList;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /////////////////////////////////////////////////////////////////
    ////////////////// LIFE CYCLE ACTIVITY METHOD ///////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // delete all database
        // HopAmChuanDatabase.deleteDatabase(getApplicationContext());

        // create sample database
        // DatabaseTest.prepareLocalDatabaseByHand(getApplicationContext());

        // set Main View
        setContentView(R.layout.activity_main_frame);

        // set navigation drawer View
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        sideBarLayout = inflater.inflate(R.layout.activity_navigation_drawer, null);
        mDrawerListView = (ListView) sideBarLayout.findViewById(R.id.navigation_drawer);
        setBehindContentView(sideBarLayout);

        /** load all playlist here */
        playlistList = PlaylistDataAccessLayer.getAllPlayLists(getApplicationContext());

        mTitle = getTitle();

        // Set up the actionbar
        setUpActionBar();

        // Set up SlidingMenu
        setUpSlidingMenu();

        // set up the ListView
        setUpListView();

        // Load default fragment
        WelcomeFragment fragment = new WelcomeFragment();
        switchFragment(fragment);

    }

    @Override
    protected void onResume() {
        super.onResume();
        headerAdapter.setDelegate(this);
        itemAdapter.setDelegate(this);
        playlistHeaderAdapter.setDelegate(this);
        playlistItemAdapter.setDelegate(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        headerAdapter.setDelegate(null);
        itemAdapter.setDelegate(null);
        playlistHeaderAdapter.setDelegate(null);
        playlistItemAdapter.setDelegate(null);
    }

    //////////////////////////////////////////////////////////////////////
    ////////////////////// CONFIGURATION METHOD /////////////////////////

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    /**
     * Create ActionBar items
     * Items of Activity will be created before
     * Items of Fragment will be created after that
     * always try to call super method
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // because SearchView just exist in HoneyComb 3.0 to above
        // we should check version of users here
        // if version is lower. We use SearchDialog instead
        // TODO: search google. Find SearchWidget library for APIUtils Lower than 11
        MenuItem searchItem = menu.findItem(R.id.search_bar);
        if (searchItem != null && UIUtils.hasHoneycomb()) {
            // Get the SearchView and set the Search Configuration
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            // Assumes current mActivity is the searchable mActivity
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            // Do not icon the widget. expand it.
            mSearchView.setIconifiedByDefault(false);
        }

        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent mActivity in AndroidManifest.xml.
        int id = item.getItemId();

        if(mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * when search dialog is active. mActivity will lost input focus
     * so. do some stuffs (such as saving state) before search
     * Unless passing search context data.
     * should end method by calling super class implementation
     */
    @Override
    public boolean onSearchRequested() {
        // doing some stuff before here
        return super.onSearchRequested();
    }

    /**
     * set up navigation drawer view
     */
    public void setUpActionBar() {

        // set a custom shadow that overlays the main content when the drawer opens
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

    }

    public void setUpSlidingMenu() {
        slidingMenu = getSlidingMenu();

        // customize look for SlidingMenu
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);

        // set custom action for SlidingMenu
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        // slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
    }

    /**
     * set up adapter for list view
     * include add all views and adapters to currently ListView
     * @return
     */
    public void setUpListView() {

        /**
         * setCacheColorHint : color background when scroll
         */
        mDrawerListView.setSelector(android.R.color.transparent);
        mDrawerListView.setCacheColorHint(Color.TRANSPARENT);

        /**
         * Using MergeAdapter for complex view
         * just create as many type of view in list as we want and Add to this Adapter
         */
        mergeAdapter = new MergeAdapter();

        /** setup adapter
         * create all adapters as we want
         * set delegate in onResume()
         * remove delegate on onPause()
         */
        // create all adapters as we want
        headerAdapter = new NavigationDrawerAdapter.HeaderAdapter(this);
        itemAdapter = new NavigationDrawerAdapter.ItemAdapter(getApplicationContext());
        playlistHeaderAdapter = new NavigationDrawerAdapter.PlaylistHeaderAdapter(getApplicationContext());
        playlistItemAdapter = new NavigationDrawerAdapter.PlaylistItemAdapter(this, playlistList);

        /** assign each adapter to this composite adapter */
        mergeAdapter.addAdapter(headerAdapter);
        mergeAdapter.addAdapter(itemAdapter);
        mergeAdapter.addAdapter(playlistHeaderAdapter);
        mergeAdapter.addAdapter(playlistItemAdapter);

        /** assign this complex adapter to navigation drawer list*/
        mDrawerListView.setAdapter(mergeAdapter);

    }


    ////////////////////////////////////////////////////////////////////////
    //////////////////// SIMPLE HELPER METHOD //////////////////////////////

    public void switchFragment(Fragment fragment) {
        if (fragment == null) return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }


    ////////////////////////////////////////////////////////////////
    //////////////// METHOD OVERRIDE USE FOR ADAPTER //////////////

    @Override
    public void gotoCategoryPage(NavigationDrawerAdapter.ItemAdapter.TYPE pageType, int position) {
        Log.e("DEBUG", "category: " + pageType);
        Fragment fragment = null;
        Bundle arguments = new Bundle();
        switch (pageType) {
            case HOME:
                fragment = new SongListFragment();
                break;
            case MYPLAYLIST:
                fragment = new PlaylistManagerFragment();
                break;
            case FAVORITE:
                fragment = new FavoriteManagerFragment();
                break;
            case FIND_BY_CHORD:
                fragment = new SearchChordFragment();
                break;
            case SEARCH_CHORD:
                fragment = new ChordViewFragment();
                break;
        }
        // Open Custom Fragment
        if (fragment != null) fragment.setArguments(arguments);
        switchFragment(fragment);
    }

    @Override
    public void gotoPlayList(int playlistId) {
        Playlist playlist = playlistList.get(playlistId);
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();

        // setting parameters
        Bundle arguments = new Bundle();
        // TODO : can change to parcelable later for performance
        arguments.putParcelable("playlist", playlist);
        fragment.setArguments(arguments);

        // setting for Drawer List View
        if (mDrawerListView != null) {
            //mDrawerListView.setItemChecked(position, true);
        }
        switchFragment(fragment);
    }

    /////////////////// HQT //////////////////////////////////////////
    //////////// NOTES ABOUT CALLBACK ///////////////////////////////
    /**
     * Callbacks interface that send data back from fragment to mActivity
     * Activity will use this function to determine what should to do
     * All activities using this fragment must implement this interface.
     * See this link for more detail:
     * @link{http://stackoverflow.com/questions/14213947/onattach-callback-from-fragment-to-activity}
     *
     * STEP :
     *  1. create interface
     *  2. assign mActivity to mCallback on onAttach() method
     *      (can do this because mActivity must be implements this method)
     *  on NavigationDrawer, when event arises. use mCallback
     */
}
