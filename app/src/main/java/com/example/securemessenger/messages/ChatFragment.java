package com.example.securemessenger.messages;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.securemessenger.R;
import com.example.securemessenger.databinding.FragmentChatBinding;
import com.example.securemessenger.models.Message;
import com.example.securemessenger.models.User;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.Group;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    FragmentChatBinding binding;
    private User comradeUser;
    private RecyclerView recyclerView;
    private ImageButton sendButton;
    private EditText editText;
    private String senderUid;
    private String receiverUid;
    private DatabaseReference refFromLatest;
    private DatabaseReference refToLatest;
    private ImageView imageToSend;
    private ImageButton attachImage;
    private Uri photoUri;
    private String photoPath;
    private Message message;
    private String id;
    private ProgressBar progressBar;
    private ImageButton deleteImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        comradeUser = requireActivity().getIntent().getParcelableExtra("KEY");
        recyclerView = binding.recyclerView;
        sendButton = binding.sendBtn;
        editText = binding.editText;
        senderUid = MessagesFragment.currentUser.getUid();
        receiverUid = comradeUser.getUid();
        attachImage = binding.attachFile;
        imageToSend = binding.imageToSend;
        progressBar = binding.progressBar;
        deleteImage = binding.deleteImage;
        //messages will update because they are saved to the same id
        refFromLatest = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/latest-messages/" + senderUid + "/" + receiverUid);
        refToLatest = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/latest-messages/" + receiverUid + "/" + senderUid);

        sendButton.setOnClickListener(v -> {

            progressBar.setVisibility(View.VISIBLE);

            id = UUID.randomUUID().toString();

            DatabaseReference refFrom = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/user-messages/" + senderUid + "/" + receiverUid + "/" + id);
            DatabaseReference refTo = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/user-messages/" + receiverUid + "/" + senderUid + "/" + id);

            if (photoUri != null) {
                String filename = UUID.randomUUID().toString();
                StorageReference ref = FirebaseStorage.getInstance().getReference("/images/" + filename);

                ref.putFile(photoUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            ref.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        photoPath = uri.toString();
                                        message = new Message(senderUid, receiverUid, editText.getText().toString(),
                                                id, Calendar.getInstance().getTime(), true, false, photoPath);

                                        refFrom.setValue(message)
                                                .addOnSuccessListener(unused -> {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(requireContext(), "Отправлено", Toast.LENGTH_SHORT).show();
                                                    editText.setText("");
                                                    imageToSend.setImageBitmap(null);
                                                    photoUri = null;
                                                    deleteImage.getLayoutParams().width = 0;
                                                    deleteImage.getLayoutParams().height = 0;
                                                })
                                                .addOnFailureListener(e -> {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });

                                        refTo.setValue(message);
                                        refFromLatest.setValue(message);
                                        refToLatest.setValue(message);
                                    })
                                    .addOnFailureListener(e -> {

                                    });
                        })
                        .addOnFailureListener(e -> {

                        });
            } else {
                message = new Message(senderUid, receiverUid, editText.getText().toString(),
                        id, Calendar.getInstance().getTime(), true, false, null);

                refFrom.setValue(message)
                        .addOnSuccessListener(unused -> {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(requireContext(), "Отправлено", Toast.LENGTH_SHORT).show();
                            editText.setText("");
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                refTo.setValue(message);
                refFromLatest.setValue(message);
                refToLatest.setValue(message);
            }
        });

        attachImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            try {
                startActivityForResult(intent, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        deleteImage.setOnClickListener(v -> {
            imageToSend.setImageBitmap(null);
            photoUri = null;
            deleteImage.getLayoutParams().width = 0;
            deleteImage.getLayoutParams().height = 0;
        });

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(comradeUser.getName());
        fetchAdapterItems();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            photoUri = data.getData();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), photoUri);
            imageToSend.setImageBitmap(bitmap);
            deleteImage.getLayoutParams().height = 70;
            deleteImage.getLayoutParams().width = 70;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void fetchAdapterItems() {
        GroupAdapter<GroupieViewHolder> adapter = new GroupAdapter<>();

        DatabaseReference ref = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/user-messages/" + senderUid + "/" + receiverUid);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);

                if (message.getSenderUid().equals(senderUid)) {
                    adapter.add(new MessageItemYou(message));
                    sortByDate(adapter);
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                } else {
                    adapter.add(new MessageItemComrade(message));
                    sortByDate(adapter);
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }

                if (message.getReceiverUid().equals(MessagesFragment.currentUser.getUid()) && message.isReadReceiver() == false) {
                    Message updatedMessage = new Message(message.getSenderUid(), message.getReceiverUid(), message.getText(), message.getId(), message.getTimestamp(), message.isReadSender(), true, message.getPhotoPath());

                    DatabaseReference refFromSpecified = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                            .getReference("/user-messages/" + senderUid + "/" + receiverUid + "/" + message.getId());
                    DatabaseReference refToSpecified = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                            .getReference("/user-messages/" + receiverUid + "/" + senderUid + "/" + message.getId());

                    refFromSpecified.setValue(updatedMessage);
                    refToSpecified.setValue(updatedMessage);
                    refFromLatest.setValue(updatedMessage);
                    refToLatest.setValue(updatedMessage);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Message message = snapshot.getValue(Message.class);
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

        recyclerView.setAdapter(adapter);
    }

    private void sortByDate(GroupAdapter<GroupieViewHolder> adapter) {
        ArrayList<MessageItem> items = new ArrayList<>();
        boolean isSorted = false;

        for (int i=0; i<adapter.getItemCount(); i++) {
            items.add(i, (MessageItem) adapter.getItem(i));
        }

        adapter.clear();
        //Log.d("MyLog", Integer.toString(adapter.getItemCount()));
        //Log.d("MyLog", Boolean.toString(items.get(0).message.getTimestamp().after(items.get(1).message.getTimestamp())));

        while (!isSorted) {
            isSorted = true;

            for (int i=1; i<items.size(); i++) {
                if (items.get(i-1).getMessage().getTimestamp().after(items.get(i).getMessage().getTimestamp())) {
                    MessageItem temp = items.get(i-1);
                    items.set(i-1, items.get(i));
                    items.set(i, temp);
                    isSorted = false;
                }
            }
        }

        for (int i=0; i< items.size(); i++) {
            adapter.add(i, (Group) items.get(i));
        }
    }

    private void uploadImageToStorage(Uri photoUri, String text) {
        String filename = UUID.randomUUID().toString();
        StorageReference ref = FirebaseStorage.getInstance().getReference("/images/" + filename);

        ref.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                photoPath = uri.toString();
                                message = new Message(senderUid, receiverUid, text, id, Calendar.getInstance().getTime(), true, false, photoPath);
                            })
                            .addOnFailureListener(e -> {

                            });
                })
                .addOnFailureListener(e -> {

                });
    }

    class MessageItemYou extends Item<GroupieViewHolder> implements MessageItem {

        private Message message;

        public MessageItemYou(Message message) {
            this.message = message;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView textView = viewHolder.itemView.findViewById(R.id.yourTextView);
            textView.setText(message.getText());

            Picasso.get()
                    .load(MessagesFragment.currentUser.getPhotoPath())
                    .into((CircleImageView)viewHolder.itemView.findViewById(R.id.yourImageView));

            TextView timestamp = viewHolder.itemView.findViewById(R.id.yourTimeStamp);
            timestamp.setText(message.getTimestamp().toString());

            if (message.getPhotoPath() != null) {
                ImageView imageView = viewHolder.itemView.findViewById(R.id.attachedImageYou);
                Picasso.get()
                        .load(message.getPhotoPath())
                        .placeholder(R.drawable.rotate_animation)
                        .into(imageView);
                imageView.setAdjustViewBounds(true);
            }
        }

        @Override
        public int getLayout() {
            return R.layout.chat_item_you;
        }

        @Override
        public void setMessage(Message message) {
            this.message = message;
        }

        @Override
        public Message getMessage() {
            return message;
        }
    }

    class MessageItemComrade extends Item<GroupieViewHolder> implements MessageItem {

        private Message message;

        public MessageItemComrade(Message message) {
            this.message = message;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView textView = viewHolder.itemView.findViewById(R.id.mateTextView);
            textView.setText(message.getText());

            Picasso.get()
                    .load(comradeUser.getPhotoPath())
                    .into((CircleImageView)viewHolder.itemView.findViewById(R.id.mateImageView));

            TextView timestamp = viewHolder.itemView.findViewById(R.id.mateTimeStamp);
            timestamp.setText(message.getTimestamp().toString());

            if (message.getPhotoPath() != null) {
                ImageView imageView = viewHolder.itemView.findViewById(R.id.attachedImageMate);
                Picasso.get()
                        .load(message.getPhotoPath())
                        .placeholder(R.drawable.rotate_animation)
                        .into(imageView);
                imageView.setAdjustViewBounds(true);
            }
        }

        @Override
        public int getLayout() {
            return R.layout.chat_item_mate;
        }

        @Override
        public void setMessage(Message message) {
            this.message = message;
        }

        @Override
        public Message getMessage() {
            return message;
        }
    }

    interface MessageItem {

        void setMessage(Message message);

        Message getMessage();
    }
}
