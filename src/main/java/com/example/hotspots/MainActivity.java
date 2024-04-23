package com.example.hotspots;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText name_editText,address_editText;
    private Button submit,click_to_rate;
    private RatingBar avg_rating;

    final float[] result = { 0 };

    SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setTheme(R.style.Theme_AppCompat);

        sp = this.getSharedPreferences("com.example.hotspots",MODE_PRIVATE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        init(); // initialize all the class variables

        avg_rating.setRating(getAverageRating(db));



        click_to_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_editText.getText().toString();
                String address = address_editText.getText().toString();
                if (name.isEmpty() || address.isEmpty() || name.equals(" ") || address.equals(" ")){
                    Toast.makeText(MainActivity.this, "Check fields", Toast.LENGTH_SHORT).show();

                }else{
                    showCustomDialog(v);
                }

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_editText.getText().toString();
                String address = address_editText.getText().toString();
                if (name.isEmpty() || address.isEmpty() || name.equals(" ") || address.equals(" ")){
                    Toast.makeText(MainActivity.this, "Check fields", Toast.LENGTH_SHORT).show();

                }else{
                    //upload to Database!
                    uploadToDatabase(db,name,address);
                }
            }
        });


    }

    private float getAverageRating(FirebaseFirestore db) {
        final float[] sum = {0};

        final int[] count = {0};
        db.collection("User")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            float b = Float.parseFloat(doc.get("Beer_rating").toString());
                            float w = Float.parseFloat(doc.get("Wine_rating").toString());
                            float m = Float.parseFloat(doc.get("Music_rating").toString());
                            sum[0] = b+w+m;
                            count[0]++;

                        }
                        result[0] = sum[0] /count[0];

                        sp.edit().putFloat("result",result[0]).apply();


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });

        return sp.getFloat("result",0f);
    }

    private void uploadToDatabase(FirebaseFirestore db, String name, String address) {
        Map<String,Object> map = new HashMap<>();
        map.put("Name",name);
        map.put("Address",address);
        map.put("Beer_rating", String.valueOf(sp.getFloat("beer", (float) 0.0)));
        map.put("Wine_rating", String.valueOf(sp.getFloat("wine", (float) 0.0)));
        map.put("Music_rating", String.valueOf(sp.getFloat("music", (float) 0.0)));
        db.collection("User").document(name)
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Data Uploaded!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Upload Failure!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showCustomDialog(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        ViewGroup vg = findViewById(android.R.id.content);
        View dialog_view = LayoutInflater.from(v.getContext()).inflate(R.layout.custom_dialog,vg,false);
        RatingBar beer_rate = (RatingBar) dialog_view.findViewById(R.id.ratingBar_beer);
        RatingBar wine_rate = (RatingBar) dialog_view.findViewById(R.id.ratingBar_wine);
        RatingBar music_rate = (RatingBar) dialog_view.findViewById(R.id.ratingBar_music);
        builder.setView(dialog_view);
        AlertDialog alert = builder.create();
        builder.setTitle("Custom Dialog");

        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float r1 = beer_rate.getRating();
                float r2 = wine_rate.getRating();
                float r3 = music_rate.getRating();

                sp.edit().putFloat("beer",r1).apply();
                sp.edit().putFloat("wine",r2).apply();
                sp.edit().putFloat("music",r3).apply();

                submit.setVisibility(View.VISIBLE);
                click_to_rate.setVisibility(View.INVISIBLE);

            }
        });

        alert.show();
    }

    private void init() {

        name_editText = (TextInputEditText) findViewById(R.id.name_editText_id);
        address_editText = (TextInputEditText)findViewById(R.id.address_editText_id);

        submit = (Button) findViewById(R.id.submit_btn_id);
        click_to_rate = (Button) findViewById(R.id.clickRate_btn_id);

        avg_rating = (RatingBar) findViewById(R.id.avg_rating_id);
    }
}