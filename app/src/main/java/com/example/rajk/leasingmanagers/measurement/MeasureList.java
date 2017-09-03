package com.example.rajk.leasingmanagers.measurement;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.rajk.leasingmanagers.MainViews.TaskDetail;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.listener.ClickListener;
import com.example.rajk.leasingmanagers.listener.RecyclerTouchListener;
import com.example.rajk.leasingmanagers.model.measurement;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;


public class MeasureList extends AppCompatActivity {


    RecyclerView recyclerView;
    MyAdapter adapter;
    DatabaseReference dbRef;
    private List<measurement> listItems = new ArrayList<>();
    String task_id, id;
    ChildEventListener ch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_list);

        task_id = TaskDetail.task_id;

        dbRef = DBREF.child("Task").child(task_id).child("Measurement").getRef();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        registerForContextMenu(recyclerView);

        adapter = new MyAdapter(listItems, this);

        recyclerView.setAdapter(adapter);


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Action for on click event


                String width, height, unit, fleximage,tag;


                measurement temp = listItems.get(position);
                width = temp.getWidth();
                height = temp.getHeight();
                unit = temp.getUnit();
                fleximage = temp.getFleximage();
                tag = temp.getTag();
                id = temp.getId();

                Intent i = new Intent(getApplicationContext(), dialogue.class);
                i.putExtra("width", width);
                i.putExtra("height", height);
                i.putExtra("unit", unit);
                i.putExtra("fleximage", fleximage);
                i.putExtra("tag", tag);
                i.putExtra("id", id);

                startActivityForResult(i, 100);

            }

            @Override
            public void onLongClick(View view, final int position) {
                PopupMenu popupMenu = new PopupMenu(MeasureList.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.context_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        remove(listItems.get(position).getId(),position);
                        return false;
                    }
                });
                popupMenu.show();
            }
        }));
    }

    void remove(String rm_id,int pos){
        DatabaseReference db = dbRef.child(rm_id);
        StorageReference st = FirebaseStorage.getInstance().getReference()
                .child("MeasurementImages").child(TaskDetail.task_id).child(rm_id+".jpeg");
        st.delete();
        db.removeValue();

        listItems.remove(pos);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(this, dialogue.class);
        startActivityForResult(i, 100);
        return true;

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // For Menu
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {

                // get String data from Intent
                String width = data.getStringExtra("width");
                String height = data.getStringExtra("height");
                String unit = data.getStringExtra("unit");
                String fleximage = data.getStringExtra("fleximage");
                String id = data.getStringExtra("id");
                String tag = data.getStringExtra("tag");
                measurement m = new measurement(tag, width, height, fleximage, unit, id);

                dbRef.child(id).setValue(m);

            }
        }
    }

    class net extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            ch = dbRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.exists()) {
                        measurement temp = dataSnapshot.getValue(measurement.class);
                        listItems.add(temp);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    measurement temp = dataSnapshot.getValue(measurement.class);
                    listItems.remove(temp);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            return null;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        dbRef.removeEventListener(ch);
    }

    @Override
    public void onResume() {
        super.onResume();

        listItems.clear();
        adapter.notifyDataSetChanged();
        new net().execute();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(MeasureList.this,TaskDetail.class);
        i.putExtra("task_id",TaskDetail.task_id);
        startActivity(i);
        finish();

    }
}
