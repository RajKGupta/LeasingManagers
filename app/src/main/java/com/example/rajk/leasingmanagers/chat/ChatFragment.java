package com.example.rajk.leasingmanagers.chat;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.rajk.leasingmanagers.CoordinatorLogin.CoordinatorSession;
import com.example.rajk.leasingmanagers.R;
import com.example.rajk.leasingmanagers.adapter.chatListAdapter;
import com.example.rajk.leasingmanagers.helper.DividerItemDecoration;
import com.example.rajk.leasingmanagers.model.ChatListModel;
import com.example.rajk.leasingmanagers.model.NameAndStatus;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import static com.example.rajk.leasingmanagers.LeasingManagers.DBREF;

public class ChatFragment extends Fragment implements chatListAdapter.chatListAdapterListener {
    private View myFragmentView;
    FragmentManager fmm;
    private ArrayList<ChatListModel> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private DatabaseReference dbChatList;
    private String mykey;
    private chatListAdapter mAdapter;
    private HashMap<DatabaseReference, ValueEventListener> dbLastMessageHashMap = new HashMap<>();
    private ChildEventListener dbChatCHE;
    private HashMap<DatabaseReference, ValueEventListener> dbProfileRefHashMap = new HashMap<>();

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    public static ChatFragment newInstance(Bundle args) {
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_chats, container, false);
        return myFragmentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//      setupUI(view.findViewById(R.id.relcity));
        fmm = getFragmentManager();

        CoordinatorSession coordinatorSession = new CoordinatorSession(getActivity());
        mykey = coordinatorSession.getUsername();
        dbChatList = DBREF.child("Users").child("Userchats").child(mykey).getRef();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mAdapter = new chatListAdapter(list, getActivity(), this);
        recyclerView.setAdapter(mAdapter);
        LoadData();
    }

    private void LoadData() {

        dbChatCHE = dbChatList.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    final String dbTablekey = dataSnapshot.getValue(String.class);
                    final String otheruserkey = dataSnapshot.getKey();
                    final DatabaseReference dbLastMsg = DBREF.child("Chats").child(dbTablekey).child("lastMsg").getRef();

                    ValueEventListener dbLastMsgChildEventListener = dbLastMsg.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                boolean alreadyexists = false;
                                for (ChatListModel chatListModel : list) {
                                    if (chatListModel.getDbTableKey().equals(dbTablekey)) {
                                        list.remove(chatListModel);
                                        chatListModel.setLastMsg(dataSnapshot.getValue(Long.class));
                                        list.add(chatListModel);
                                        sortChatList();
                                        alreadyexists = true;
                                        mAdapter.notifyDataSetChanged();
                                        break;
                                    }
                                }
                                if (!alreadyexists) {
                                    final Long lastMsgId = dataSnapshot.getValue(Long.class);
                                    DatabaseReference dbProfileRef = DBREF.child("Users").child("Usersessions").child(otheruserkey).getRef();

                                    ValueEventListener valueEventListener = dbProfileRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                boolean alreadyexists = false;
                                                NameAndStatus user = dataSnapshot.getValue(NameAndStatus.class);
                                                for (ChatListModel chatListModel : list) {
                                                    if (chatListModel.getUserkey().equals(dataSnapshot.getKey())) {
                                                        alreadyexists = true;
                                                        break;
                                                    }

                                                }
                                                if (!alreadyexists) {
                                                    ChatListModel chatListModel = new ChatListModel(user.getName(), otheruserkey, dbTablekey, getRandomMaterialColor("400"), lastMsgId);
                                                    list.add(chatListModel);
                                                    sortChatList();
                                                    mAdapter.notifyDataSetChanged();

                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    dbProfileRefHashMap.put(dbProfileRef, valueEventListener);
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    dbLastMessageHashMap.put(dbLastMsg, dbLastMsgChildEventListener);

                }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Iterator<HashMap.Entry<DatabaseReference, ValueEventListener>> iterator = dbLastMessageHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<DatabaseReference, ValueEventListener> entry = (HashMap.Entry<DatabaseReference, ValueEventListener>) iterator.next();
            if (entry.getValue() != null)
                entry.getKey().removeEventListener(entry.getValue());
        }
        Iterator<HashMap.Entry<DatabaseReference, ValueEventListener>> iterator2 = dbProfileRefHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<DatabaseReference, ValueEventListener> entry = (HashMap.Entry<DatabaseReference, ValueEventListener>) iterator2.next();
            if (entry.getValue() != null) entry.getKey().removeEventListener(entry.getValue());
        }
        if (dbChatCHE != null)
            dbChatList.removeEventListener(dbChatCHE);
        mAdapter.removeListeners();

    }

    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getActivity().getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    @Override
    public void onChatRowClicked(int position) {
        final Intent intent = new Intent(getActivity(), ChatActivity.class);
        ChatListModel topic = list.get(position);
        intent.putExtra("dbTableKey", topic.getDbTableKey());
        intent.putExtra("otheruserkey", topic.getUserkey());
        startActivity(intent);
    }

    private void sortChatList() {
        Collections.sort(list, new Comparator<ChatListModel>() {
            @Override
            public int compare(ChatListModel o1, ChatListModel o2) {
                return o1.getLastMsg() > o2.getLastMsg() ? -1 : 0;
            }
        });
    }
}