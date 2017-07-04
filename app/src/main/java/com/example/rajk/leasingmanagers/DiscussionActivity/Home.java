package com.example.rajk.leasingmanagers.DiscussionActivity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.rajk.leasingmanagers.MainActivity;
import com.example.rajk.leasingmanagers.NewTopic;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.topicAdapter;
import com.example.rajk.leasingmanagers.model.Discussions;
import com.example.rajk.leasingmanagers.session;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Home extends AppCompatActivity implements topicAdapter.TopicAdapterListener
{

    RecyclerView Topic_list;
    DatabaseReference dbTopic;
    LinearLayoutManager linearLayoutManager;
    private ArrayList<Discussions>  TopicList= new ArrayList<>();
    private RecyclerView.Adapter mAdapter;
    session se ;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        se = new session(getApplicationContext());
        Topic_list = (RecyclerView) findViewById(R.id.Topic_List);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Topic");

        dbTopic = FirebaseDatabase.getInstance().getReference().child("MeChat").child("Topic").child(se.getPlace_id()).getRef();
        LoadData();
        mAdapter = new topicAdapter(TopicList,getApplicationContext(),this);
        linearLayoutManager=new LinearLayoutManager(this);
        Topic_list.setLayoutManager(linearLayoutManager);
        Topic_list.setItemAnimator(new DefaultItemAnimator());
        Topic_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        Topic_list.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this,NewTopic.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.signout:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("SIGN_OUT","SIGN_OUT");
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void LoadData()
    {

        dbTopic.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Discussions topic = new Discussions(se.getPlace_id(),dataSnapshot.getKey());
                topic.setColor(getRandomMaterialColor("400"));
                TopicList.add(topic);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    @Override
    public void onTopicRowClicked(int position) {
        Intent intent = new Intent(Home.this,Comment.class);
        Discussions topic = TopicList.get(position);
        intent.putExtra("topic_id",topic.getName());
        startActivity(intent);
    }
}
