package ch.amiv.legiscanner.amivlegiscanner;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Calendar;

public class MemberListActivity extends Activity {
    //private static int REFRESH_LIST_DELAY = 5000;
    //private Handler handler = new Handler();    //Delayed call to only allow submission of another legi in x seconds

    private ListView mListview;
    CustomListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

        UpdateList();
    }

    public void InitialiseListView ()
    {
        if(ScanActivity.memberDatabase.members == null)  //XXX fetch the data and wait until we have the data from ther server to create the list view. Some async stuff here...
        {
            ScanActivity.memberDatabase.members.add(new Member("0", false, "0", "First Name", "Last Name", "0", "-", "-"));
        }


        adapter = new CustomListAdapter(this, ScanActivity.memberDatabase.members);

        mListview = (ListView) findViewById(R.id.listView);
        mListview.setAdapter(adapter);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPos = position;
                String itemValue = (String) mListview.getItemAtPosition(itemPos);
            }
        });
    }
/*
    Runnable refreshList = new Runnable() {    //Creates delay call to only allow scanning again after x seconds

        @Override
        public void run() {
            UpdateList();
            handler.postDelayed(refreshList, REFRESH_LIST_DELAY);
        }
    };
*/
    public void UpdateList()
    {
        if(mListview == null)
            InitialiseListView();

        adapter.notifyDataSetChanged();
    }
}
