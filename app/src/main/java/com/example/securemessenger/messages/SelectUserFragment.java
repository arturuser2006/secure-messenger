package com.example.securemessenger.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.securemessenger.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.securemessenger.databinding.FragmentSelectUserBinding;
import com.example.securemessenger.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class SelectUserFragment extends Fragment {

    FragmentSelectUserBinding binding;
    private RecyclerView recyclerView;
    private ImageButton findButton;
    private EditText editText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSelectUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = binding.recyclerView;
        findButton = binding.findByEmailButton;
        editText = binding.fragmentSelectUserEditText;

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editText.getText().toString().trim();

                fetchUserItems(email);
            }
        });

        setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Выберите пользователя");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.custom_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onContextItemSelected(item);
    }

    private void fetchUserItems(String email) {
        GroupAdapter<GroupieViewHolder> adapter = new GroupAdapter<>();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/users");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);

                if (user.getEmail().equals(email)) {
                    adapter.add(new UserItem(user));
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

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                UserItem userItem = (UserItem) item;
                requireActivity().getIntent().putExtra("KEY", userItem.user);
                NavHostFragment.findNavController(SelectUserFragment.this).navigate(R.id.action_selectUserFragment_to_chatFragment);
            }
        });

        recyclerView.setAdapter(adapter);
    }



    class UserItem extends Item<GroupieViewHolder> {

        User user;

        public UserItem(User user) {
            this.user = user;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView textView = viewHolder.itemView.findViewById(R.id.username);
            textView.setText(user.getName());
            Picasso.get().load(user.getPhotoPath()).into((CircleImageView) viewHolder.itemView.findViewById(R.id.profileImage));
        }

        @Override
        public int getLayout() {
            return R.layout.user_item;
        }
    }
}
