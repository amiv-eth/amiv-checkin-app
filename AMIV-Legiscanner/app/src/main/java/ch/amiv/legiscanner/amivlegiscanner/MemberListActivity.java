package ch.amiv.legiscanner.amivlegiscanner;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MemberListActivity extends Activity {
    private static int REFRESH_LIST_DELAY = 5000;

    private ListView mListview;
    CustomListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

        UpdateList();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {    //Refresh stats every x seconds
            @Override
            public void run() {
                ServerRequests.UpdateMemberDB(getApplicationContext(), new ServerRequests.MemberDBUpdatedCallback() {
                    @Override
                    public void OnMDBUpdated() {
                        UpdateList();
                    }
                });
                handler.postDelayed(this, REFRESH_LIST_DELAY);
            }
        }, REFRESH_LIST_DELAY);
    }

    public void InitialiseListView ()
    {
        if(MemberDatabase.instance.members == null) {
            MemberDatabase.instance.members.add(new Member("0", false, "0", "First Name", "Last Name", "0", "-", "-"));
        }

        adapter = new CustomListAdapter(this, MemberDatabase.instance.members);

        mListview = (ListView) findViewById(R.id.listView);
        mListview.setAdapter(adapter);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPos = position;
                //String itemValue = (String) mListview.getItemAtPosition(itemPos);
            }
        });
    }

    public void UpdateList()
    {
        if(mListview == null)
            InitialiseListView();

        adapter.notifyDataSetChanged();
    }
}
