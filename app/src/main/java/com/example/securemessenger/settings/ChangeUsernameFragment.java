package com.example.securemessenger.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.securemessenger.databinding.FragmentChangeUsernameBinding;
import com.example.securemessenger.messages.MessagesFragment;
import com.example.securemessenger.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeUsernameFragment extends Fragment {

    FragmentChangeUsernameBinding binding;
    private FirebaseDatabase database;
    private EditText editText;
    private Button saveBtn;
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangeUsernameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editText = binding.editText;
        textView = binding.textView;
        saveBtn = binding.saveBtn;
        database = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app");

        textView.setText(MessagesFragment.currentUser.getName());

        saveBtn.setOnClickListener(v -> {
            updateUserInDatabase();
        });

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Сменить имя пользователя");
    }

    private void updateUserInDatabase() {
        DatabaseReference ref = database.getReference("/users/" + MessagesFragment.currentUser.getUid());

        String name = editText.getText().toString().trim();
        String email = MessagesFragment.currentUser.getEmail();
        String password = MessagesFragment.currentUser.getPassword();
        String photoPath = MessagesFragment.currentUser.getPhotoPath();
        String uid = MessagesFragment.currentUser.getUid();

        User user = new User(name, password, email, uid, photoPath);

        ref.setValue(user)
                .addOnSuccessListener(unused -> {
                    textView.setText(user.getName());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
