package com.example.imagetotext;


import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity  {

    EditText mResultEdit;
    ImageView mPreview;

    private static final int CAMERA_REQ_CODE=200;
    private  static final int STORAGE_REQ_CODE=400;
    private static final int IMAGE_PICK_GALLERY=1000;
    private static final int IMAGE_PICK_CAMERA=1001;

    String cameraPermission[];
    String storagePermission[];

    private String inputLanguage,outputLanguage;
    Uri image_uri;

    private TextView mSourceLang;
    private Button mTranslateBtn;
    private TextView mTranslatedText;

    private String sourceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerLang);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.langArray, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                outputLanguage=(String)adapterView.getItemAtPosition(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ActionBar actionBar=getSupportActionBar();
        actionBar.setSubtitle("Eklemek için ikona basınız ->");

        mResultEdit=findViewById(R.id.resultEdit);
        mPreview=findViewById(R.id.imagevw);

        mSourceLang=findViewById(R.id.sourceLang);
        mTranslateBtn=findViewById(R.id.translate);
        mTranslatedText=findViewById(R.id.translatedText);

        mTranslateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                identifyLanguage();



            }
        });

        cameraPermission=new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


    }

    private void identifyLanguage() {
        sourceText = mResultEdit.getText().toString();

        FirebaseLanguageIdentification identifier = FirebaseNaturalLanguage.getInstance()
                .getLanguageIdentification();

        mSourceLang.setText("Algılanıyor..");
        identifier.identifyLanguage(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s.equals("und")){
                    Toast.makeText(getApplicationContext(),"Dil tanımlanamadı !",Toast.LENGTH_SHORT).show();

                }
                else {
                    getLanguageCode(s);
                }
            }
        });
    }
    private void getLanguageCode(String language) {
        int langCode;
        switch (language){
            case "tr":
                langCode = FirebaseTranslateLanguage.TR;
                mSourceLang.setText("Türkçe");
                break;
            case "de":
                langCode = FirebaseTranslateLanguage.DE;
                mSourceLang.setText("German");

                break;

            case "en":
                langCode = FirebaseTranslateLanguage.EN;
                mSourceLang.setText("English");
                break;

            default:
                langCode = 0;
        }

        if (outputLanguage.equals("Türkçe")){
            translateTr(langCode);}
        else if (outputLanguage.equals("English")){
            translateEng(langCode);
        }
    }

    private void translateEng(int langCode) {

        mTranslatedText.setText("Translating..");


        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                //from language
                .setSourceLanguage(langCode)
                // to language
                .setTargetLanguage(FirebaseTranslateLanguage.EN)
                .build();

        final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                .getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                 .build();


        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        mTranslatedText.setText(s);
                    }
                });
            }
        });
    }

    private void translateTr(int langCode) {

        mTranslatedText.setText("Translating..");


        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                //from language
                .setSourceLanguage(langCode)
                // to language
                .setTargetLanguage(FirebaseTranslateLanguage.TR)
                .build();

        final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                .getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();


        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        mTranslatedText.setText(s);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.addImage){
            showImageImportDiaolog();
        }

        if (id==R.id.settings){

            Toast.makeText(this, "Ayarlar", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDiaolog() {
        String[] items={" Kamera", " Galeri"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Seç");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i ==0){
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickCamera();
                    }
                }

                if (i ==1 ){
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private void pickGallery() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY);
    }

    private void pickCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image To Text");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQ_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result=ContextCompat.checkSelfPermission(this,

                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQ_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result= ContextCompat.checkSelfPermission(this,

                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1=ContextCompat.checkSelfPermission(this,

                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return  result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQ_CODE:
                if (grantResults.length<0){
                    boolean cameraAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }else{
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQ_CODE:
                if (grantResults.length<0){

                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted){
                        pickGallery();
                    }else{
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode ==RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY){
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON)
                .start(this);

            }
            if (requestCode == IMAGE_PICK_CAMERA){

                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                Uri resultUri=result.getUri();
                mPreview.setImageURI(resultUri);

                BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreview.getDrawable();
                Bitmap bitmap=bitmapDrawable.getBitmap();

                TextRecognizer recognizer=new TextRecognizer.Builder(getApplicationContext()).build();
                if (!recognizer.isOperational()){
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }else{
                    Frame frame=new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items=recognizer.detect(frame);
                    StringBuilder stringBuilder=new StringBuilder();

                    for (int i =0; i<items.size();i++){
                        TextBlock myitem=items.valueAt(i);
                        stringBuilder.append(myitem.getValue());
                        stringBuilder.append("\n");

                    }

                    mResultEdit.setText(stringBuilder.toString());
                }
            }
            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error=result.getError();
                Toast.makeText(this, "Hata"+error, Toast.LENGTH_SHORT).show();

            }
        }
    }


}
