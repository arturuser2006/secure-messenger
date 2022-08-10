package com.example.securemessenger.messages;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.securemessenger.R;
import com.example.securemessenger.databinding.FragmentMessagesBinding;
import com.example.securemessenger.models.Message;
import com.example.securemessenger.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.Group;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesFragment extends Fragment {

    FragmentMessagesBinding binding;
    public static User currentUser;
    private RecyclerView recyclerView;
    private HashMap<String, Message> map;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    private ProgressBar progressBar;
    private FloatingActionButton floatingActionButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = binding.progressBar;
        recyclerView = binding.recyclerView;
        drawerLayout = getActivity().findViewById(R.id.drawerLayout);
        floatingActionButton = binding.floatingBtn;
        map = new HashMap<>();

        verifyUserIsLoggedIn();
        fetchCurrentUser();
        fetchUserMessages();

        navigationView = (NavigationView) getActivity().findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(item -> {
            //выбор фрагмента
            switch (item.getItemId()) {
                case R.id.settings:
                    try {
                        NavHostFragment.findNavController(MessagesFragment.this).navigate(R.id.action_messagesFragment_to_settingsFragment);
                    } catch (Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(), "Вернитесь во фрагмент \"Сообщения\"", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    drawerLayout.closeDrawers();
                    break;
                case R.id.changePassword:
                    try {
                        NavHostFragment.findNavController(MessagesFragment.this).navigate(R.id.action_messagesFragment_to_changePasswordFragment);
                    } catch (Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(), "Вернитесь во фрагмент \"Сообщения\"", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    drawerLayout.closeDrawers();
                    break;
                case R.id.changeUsername:
                    try {
                        NavHostFragment.findNavController(MessagesFragment.this).navigate(R.id.action_messagesFragment_to_changeUsernameFragment);
                    } catch (Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(), "Вернитесь во фрагмент \"Сообщения\"", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    drawerLayout.closeDrawers();
                    break;
                case R.id.signOut:
                    try {
                        NavHostFragment.findNavController(MessagesFragment.this).navigate(R.id.action_messagesFragment_to_firstFragment);
                        FirebaseAuth.getInstance().signOut();
                    } catch (Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(), "Вернитесь во фрагмент \"Сообщения\"", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    drawerLayout.closeDrawers();
                    break;
            }
            return true;
        });

        floatingActionButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MessagesFragment.this).navigate(R.id.action_messagesFragment_to_selectUserFragment);
        });

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Сообщения");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.custom_menu, menu);
        
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void verifyUserIsLoggedIn() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            NavHostFragment.findNavController(MessagesFragment.this).navigate(R.id.action_messagesFragment_to_firstFragment);
        }
    }

    private void fetchCurrentUser() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/users/" + uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);

                View headerView = navigationView.getHeaderView(0);
                ((TextView) headerView.findViewById(R.id.usernameSideBar)).setText(currentUser.getName());
                Picasso.get().load(currentUser.getPhotoPath()).into(((CircleImageView) headerView.findViewById(R.id.profileImageSideBar)));
                ((TextView) headerView.findViewById(R.id.emailSideBar)).setText(currentUser.getEmail());

                Log.d("MyLog", "Current user is " + currentUser.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchUserMessages() {
        GroupAdapter<GroupieViewHolder> adapter = new GroupAdapter<>();

        DatabaseReference ref = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/latest-messages/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);

                map.put(snapshot.getKey(), message);
                refreshAdapter(adapter);
                sortByVerity(adapter);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);

                map.put(snapshot.getKey(), message);
                refreshAdapter(adapter);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adapter.setOnItemClickListener((item, view) -> {

            RecentMessage recentMessage = (RecentMessage) item;
            Message message = recentMessage.message;
            String partnerUid;

            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderUid())) {
                partnerUid = message.getReceiverUid();
            } else {
                partnerUid = message.getSenderUid();
            }

            DatabaseReference ref1 = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/users");

            ref1.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);

                    if (partnerUid.equals(user.getUid())) {
                        progressBar.setVisibility(View.VISIBLE);
                        requireActivity().getIntent().putExtra("KEY", user);
                        NavHostFragment.findNavController(MessagesFragment.this).navigate(R.id.action_messagesFragment_to_chatFragment);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        recyclerView.setAdapter(adapter);
    }

    private void refreshAdapter(GroupAdapter<GroupieViewHolder> adapter) {
        adapter.clear();

        for (Message m : map.values()) {
            adapter.add(new RecentMessage(m));
        }
    }

    private void sortByVerity(GroupAdapter<GroupieViewHolder> adapter) {
        ArrayList<RecentMessage> items = new ArrayList<>();
        boolean isSorted = false;

        for (int i=0; i<adapter.getItemCount(); i++) {
            items.add(i, (RecentMessage) adapter.getItem(i));
        }
        Log.d("MyLog", Integer.toString(items.size()));
        adapter.clear();

        //Log.d("MyLog", Boolean.toString(items.get(0).message.getTimestamp().after(items.get(1).message.getTimestamp())));

        while (!isSorted) {
            isSorted = true;

            for (int i=1; i<items.size(); i++) {
                if (items.get(i-1).message.isReadReceiver() == true && items.get(i).message.isReadReceiver() == false) {
                    RecentMessage temp = items.get(i-1);
                    items.set(i-1, items.get(i));
                    items.set(i, temp);
                    isSorted = false;
                }
            }
        }

        for (int i=0; i< items.size(); i++) {
            Log.d("MyLog", Boolean.toString(items.get(i).message.isReadReceiver()));
            adapter.add(i, (Group) items.get(i));
        }
    }

    class RecentMessage extends Item<GroupieViewHolder> {

        private final Message message;
        private boolean isRead = false;

        public RecentMessage(Message message) {
            this.message = message;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView username = viewHolder.itemView.findViewById(R.id.usernameTextView);
            TextView messageText = viewHolder.itemView.findViewById(R.id.messageText);
            CircleImageView circleImageView = viewHolder.itemView.findViewById(R.id.imageView);
            TextView time = viewHolder.itemView.findViewById(R.id.timestamp);
            Button newMessageSign = viewHolder.itemView.findViewById(R.id.newMessageImage);

            DatabaseReference ref = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/users");

            String comradeUid;

            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderUid())) {
                comradeUid = message.getReceiverUid();
            } else {
                comradeUid = message.getSenderUid();
            }

            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);

                    if (comradeUid.equals(user.getUid())) {

                        if (message.getSenderUid().equals(FirebaseAuth.getInstance().getUid())) {
                            messageText.setText("Вы: " + message.getText());
                        } else {
                            messageText.setText(user.getName() + ": " + message.getText());
                        }
                        username.setText(user.getName());

                        try {
                            Picasso.get().load(user.getPhotoPath()).into(circleImageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        time.setText(message.getTimestamp().toString());

                        if (message.isReadReceiver() == false && message.isReadSender() == false) {
                            newMessageSign.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @Override
        public int getLayout() {
            return R.layout.user_item_messages_fragment;
        }
    }
}