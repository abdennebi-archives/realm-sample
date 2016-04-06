package io.realm.examples.realmadapters;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;



import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.examples.realmadapters.models.TimeStamp;


public class AdapterExampleActivity extends Activity {

    private Realm realm;
    private WorkerThread workerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
        Realm.deleteRealm(realmConfig);
        // Set the default Realm configuration at the beginning.
        Realm.setDefaultConfiguration(realmConfig);
        realm = Realm.getDefaultInstance();

        RealmResults<TimeStamp> timeStamps = realm.where(TimeStamp.class).findAll();
        final MyAdapter adapter = new MyAdapter(this, R.id.listView, timeStamps, true);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TimeStamp timeStamp = adapter.getRealmResults().get(i);
                Message message = buildMessage(WorkerHandler.REMOVE_TIMESTAMP, timeStamp.getTimeStamp());

                workerThread.workerHandler.sendMessage(message);
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        workerThread.workerHandler.getLooper().quit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        workerThread = new WorkerThread();
        workerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Message message = buildMessage(WorkerHandler.ADD_TIMESTAMP, Long.toString(System.currentTimeMillis()));
            workerThread.workerHandler.sendMessage(message);
        }
        return true;
    }

    private static Message buildMessage(int action, String timeStamp) {
        Bundle bundle = new Bundle(2);
        bundle.putInt(WorkerHandler.ACTION, action);
        bundle.putString(WorkerHandler.TIMESTAMP, timeStamp);
        Message message = new Message();
        message.setData(bundle);
        return message;
    }
}
