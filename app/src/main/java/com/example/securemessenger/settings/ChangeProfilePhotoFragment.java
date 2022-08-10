package com.example.securemessenger.settings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.securemessenger.databinding.FragmentChangeProfilePhotoBinding;
import com.example.securemessenger.databinding.FragmentSettingsBinding;
import com.example.securemessenger.messages.MessagesFragment;
import com.example.securemessenger.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;

public class ChangeProfilePhotoFragment extends Fragment {

    FragmentChangeProfilePhotoBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private ImageButton imageButton;
    private Button saveButton;
    private String newPhotoPath;
    private Uri photoUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangeProfilePhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageButton = binding.settingsItemImageView;
        saveButton = binding.button;

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Изменить фото профиля");

        firebaseDatabase = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app");
        firebaseStorage = FirebaseStorage.getInstance();

        Picasso.get().load(MessagesFragment.currentUser.getPhotoPath()).into(imageButton);
        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            try {
                startActivityForResult(intent, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        saveButton.setOnClickListener(v -> {
            uploadPhotoToFirebaseStorage();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            photoUri = data.getData();

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), photoUri);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, imageButton.getWidth(), imageButton.getHeight(), true);
            imageButton.setImageBitmap(resized);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void uploadPhotoToFirebaseStorage() {
        String filename = UUID.randomUUID().toString();
        StorageReference firebaseStorageRef = firebaseStorage.getReference("/images/" + filename);

        firebaseStorageRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    firebaseStorageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                newPhotoPath = uri.toString();
                                Log.d("MyLog", newPhotoPath);
                                updateUserInDatabase();
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                            });
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    private void updateUserInDatabase() {
        DatabaseReference databaseReference = firebaseDatabase.getReference("/users/" + MessagesFragment.currentUser.getUid());

        String email = MessagesFragment.currentUser.getEmail();
        String password = MessagesFragment.currentUser.getPassword();
        String name = MessagesFragment.currentUser.getName();
        String uid = MessagesFragment.currentUser.getUid();

        User user = new User(name, password, email, uid, newPhotoPath);

        databaseReference.setValue(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Фото профиля обновлено", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
