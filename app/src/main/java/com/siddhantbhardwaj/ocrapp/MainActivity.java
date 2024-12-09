package com.siddhantbhardwaj.ocrapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 123;
    ImageView imageView;
    TextView textView;
    Button imageBtn, speechBtn;

    InputImage inputImage;
    TextRecognizer textRecognizer;
    TextToSpeech textToSpeech;

    public Bitmap textImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        imageBtn = findViewById(R.id.choose_image);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.text);
        speechBtn = findViewById(R.id.speech);

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        speechBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(textView.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
            }
        });

    }

    private void openGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/");

        Intent chooseIntent = Intent.createChooser(getIntent,"Select an Image");
        chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooseIntent,PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE){
            if(data != null){
                byte[] byteArray = new byte[0];
                String filePath = null;

                try {
                    inputImage = InputImage.fromFilePath(this,data.getData());
                    Bitmap resultUri = inputImage.getBitmapInternal();
                    Glide.with(MainActivity.this)
                            .load(resultUri)
                            .into(imageView);

                    Task<Text> result = textRecognizer.process(inputImage)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text text) {
                                    processTextBlock(text);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_LONG).show();
                                }
                            });

                }catch (final IOException e){

                }

            }
        }

    }

    private void processTextBlock(Text text) {
        String resultText = text.getText();
        for(Text.TextBlock block : text.getTextBlocks()){
            String blockText = block.getText();
            textView.append("\n");
            Point[] blockCode = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for(Text.Line line : block.getLines()){
                String lineText = line.getText();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for(Text.Element element : line.getElements()){
                    textView.append(" ");
                    String elementText  = element.getText();
                    textView.append(elementText);

                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();

                }
            }
        }
    }

    @Override
    protected void onPause() {
        if(!textToSpeech.isSpeaking()){
            super.onPause();
        }
    }
}