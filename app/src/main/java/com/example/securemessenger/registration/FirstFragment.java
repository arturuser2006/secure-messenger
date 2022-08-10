package com.example.securemessenger.registration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.securemessenger.R;
import com.example.securemessenger.databinding.FragmentFirstBinding;
import com.example.securemessenger.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import java.util.UUID;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirstFragment extends Fragment {

    FragmentFirstBinding binding;
    private EditText name;
    private EditText password;
    private EditText email;
    private Button regButton;
    private TextView accountExistsTextView;
    private Button setPhotoButton;
    private Uri photoUri;
    private CircleImageView circleImageView;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = binding.name;
        password = binding.password;
        email = binding.email;
        regButton = binding.registrationButton;
        accountExistsTextView = binding.alreadyHaveAccountTextView;
        setPhotoButton = binding.photoButton;
        circleImageView = binding.photoCircle;
        progressBar = binding.progressBar;

        regButton.setOnClickListener(v -> {
            try {
                registerUser();
                progressBar.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        accountExistsTextView.setOnClickListener(v -> {
            NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_firstFragment_to_secondFragment);
        });

        setPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            try {
                startActivityForResult(intent, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Регистрация");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            photoUri = data.getData();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), photoUri);
            setPhotoButton.setAlpha(0f); //делает кнопку выбора фото прозрачной
            circleImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    private void registerUser() {
        if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Заполните пустые поля", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnSuccessListener((OnSuccessListener) o -> {
                    Toast.makeText(requireContext(), "Регистрация прошла успешно: " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
                    if (photoUri != null) {
                        uploadPhotoToFirebaseStorage();
                    } else {
                        uploadUserToFirebaseDatabase(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void uploadPhotoToFirebaseStorage() {

        String filename = UUID.randomUUID().toString();
        StorageReference ref = FirebaseStorage.getInstance().getReference("/images/" + filename);
        ref.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(requireContext(), "Фото профиля добавлено успешно", Toast.LENGTH_LONG).show();
                    ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            uploadUserToFirebaseDatabase(uri.toString());
                            Log.d("MyLog", uri.toString());
                        })
                        .addOnFailureListener(e -> {
                            Log.d("MyLog", e.getMessage());
                        });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Ошибка при загрузке фото", Toast.LENGTH_SHORT).show();
                    Log.d("MyLog", e.getMessage());
                });
    }

    private void uploadUserToFirebaseDatabase(String photoPath) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/" + uid);

        if (photoPath == null) {
            photoPath = "https://firebasestorage.googleapis.com/v0/b/messenger-project-1ab76.appspot.com/o/images%2F1c89a0fd-07d7-4001-844e-f788a50868e6?alt=media&token=e523145c-d603-43cb-8db6-f7d73d7590f8";
        }

        User user = new User(name.getText().toString().trim(), password.getText().toString().trim(), email.getText().toString().trim(), uid, photoPath);

        ref.setValue(user)
                .addOnSuccessListener(unused -> {
                    Log.d("MyLog", "Пользователь добавлен успешно в базу данных");
                    NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_firstFragment_to_messagesFragment);
                })
                .addOnFailureListener(e -> {
                        Log.d("MyLog", "Ошибка при добавлении пользователя в базу данных: " + e.getMessage());
                });
    }
}