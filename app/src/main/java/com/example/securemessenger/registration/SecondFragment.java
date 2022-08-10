package com.example.securemessenger.registration;

import android.os.Bundle;
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
import com.example.securemessenger.databinding.FragmentSecondBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SecondFragment extends Fragment {

    FragmentSecondBinding binding;
    private TextView backToRegistration;
    private EditText email;
    private EditText password;
    private Button loginButton;
    ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backToRegistration = binding.regTextView;
        email = binding.email;
        password = binding.password;
        loginButton = binding.loginBtn;
        progressBar = binding.progressBar;;

        backToRegistration.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this).navigate(R.id.action_secondFragment_to_firstFragment);
        });

        loginButton.setOnClickListener(v -> {

            progressBar.setVisibility(View.VISIBLE);

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(requireContext(), "Вы вошли как " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(SecondFragment.this).navigate(R.id.action_secondFragment_to_messagesFragment);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Войти");
    }
}