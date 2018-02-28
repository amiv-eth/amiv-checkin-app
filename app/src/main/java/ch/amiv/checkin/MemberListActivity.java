package ch.amiv.checkin;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * This activity is for displaying the list of signed in members, similar to what is seen in the checkin website in the other amiv checkin project.
 * Mostly handles updating the data by fetching from the server and then updating the listview. Note the list view is customised, ie the individual item, this is what the CustomListAdapter class and listview_item.xml are for
 */

public class MemberListActivity extends AppCompatActivity {

    private ListView mListview;
    private CustomListAdapter adapter;
    private final Handler handler = new Handler();  //similar as in scanActivity, to keep refreshing the data
    private Runnable refreshMemberDB = new Runnable() {    //Refresh stats every x seconds
        @Override
        public void run() {
            ServerRequests.UpdateMemberDB(getApplicationContext(), new ServerRequests.MemberDBUpdatedCallback() {
                @Override
                public void OnMDBUpdated() {
                    UpdateList();
                }
            });
            if(SettingsActivity.GetAutoRefresh(getApplicationContext()))
                handler.postDelayed(this, SettingsActivity.GetRefreshFrequency(getApplicationContext()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

        UpdateList();
    }

    private void InitialiseListView()
    {
        if(MemberDatabase.instance.members == null) {
            MemberDatabase.instance.members.add(new Member("0", false, "0", "First Name", "Last Name", "0", "-", "-"));
        }

        adapter = new CustomListAdapter(this, MemberDatabase.instance.members);

        mListview = findViewById(R.id.listView);
        mListview.setAdapter(adapter);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //int itemPos = position;
                //String itemValue = (String) mListview.getItemAtPosition(itemPos);
            }
        });
    }

    @Override
    protected void onPause() {  //stop refreshing the data when the app is not active
        super.onPause();
        handler.removeCallbacks(refreshMemberDB);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(refreshMemberDB, 0);
    }

    private void UpdateList()
    {
        if(mListview == null)
            InitialiseListView();

        adapter.notifyDataSetChanged();
    }
}
