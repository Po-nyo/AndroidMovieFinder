package com.example.startproject2;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {
    EditText name, birthday, email, password;
    RadioGroup gender;
    ImageView imageView;
    FrameLayout frameLayout;
    Button button;
    File file, signatureFile;
    PaintBoard paintBoard;

    public MyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_my, container, false);

        name = rootView.findViewById(R.id.editText);
        email = rootView.findViewById(R.id.editText3);
        password = rootView.findViewById(R.id.editText4);
        birthday = rootView.findViewById(R.id.editText2);

        gender = rootView.findViewById(R.id.radioGroup);

        SharedPreferences sp = getActivity().getPreferences(getContext().MODE_PRIVATE);

        // 프래그먼트가 띄워질 때 저장된 정보가 있으면 각각 그것으로 설정, default는 공백
        name.setText(sp.getString("name", ""));
        email.setText(sp.getString("email", ""));
        password.setText(sp.getString("password", ""));
        birthday.setText(sp.getString("birthday", ""));
        gender.check(sp.getInt("gender", -1));


        // 생년월일 란을 클릭하면 실행
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DatePickerDialog 클래스를 이용
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // 선택한 날짜에 따라 text 설정
                        birthday.setText(year+"년 "+(month+1)+"월 "+dayOfMonth+"일");
                    }
                }, 2019, 10, 29);
                dialog.show();
            }
        });

        // 프로필사진 란을 누르면 takePicture() 가 실행되도록 onClickListener 설정
        imageView = rootView.findViewById(R.id.imageView4);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        frameLayout = rootView.findViewById(R.id.signature);

        // 지우기버튼을 누르면 서명란이 clear 되도록 설정
        button = rootView.findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintBoard.clear();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 이전 프로필 사진파일이 존재하면 프로필사진에 설정
        file = createFile();
        if(file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), null);
            imageView.setImageBitmap(bitmap);
        }

        // frameLayout에 paintBoard 뷰 설정,
        // 만약 이전 서명을 저장한 파일이 존재하면 불러와서
        // paintBoard 객체에 넘겨준 후 수정가능하도록 서명란에 표시
        signatureFile = createFile2();
        paintBoard = new PaintBoard(getContext());
        frameLayout.addView(paintBoard);
        if(signatureFile.exists()) {
            Bitmap oldSignature = BitmapFactory.decodeFile(signatureFile.getAbsolutePath(), null);
            paintBoard.changeBitmap(oldSignature);
            Log.d("signature", "yes");
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences sp = getActivity().getPreferences(getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        // 어플을 끌 때 정보들을 각각 저장함
        editor.putString("name", name.getText().toString());
        editor.putString("email", email.getText().toString());
        editor.putString("password", password.getText().toString());
        editor.putString("birthday", birthday.getText().toString());
        editor.putInt("gender", gender.getCheckedRadioButtonId());

        editor.commit();

        // 어플을 끌 때 서명란의 사진을 png 파일로 저장
        signatureFile = new File(getActivity().getExternalFilesDir(null), "signature.png");
        Bitmap bitmap = paintBoard.mBitmap; // 생성된 paintBoard 객체에서 현재 서명 불러옴
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos); // png 포맷으로 설정

        try {
            // 파일 저장
            FileOutputStream fos = new FileOutputStream(signatureFile);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e ) {}
    }

    private void takePicture() {  // 프로필 사진을 눌렀을 때 실행
        if(file == null)    // 파일 생성
            file = createFile();

        // uri 설정
        Uri fileUri = FileProvider.getUriForFile(getContext(), "com.example.startproject2.fileprovider", file);
        // 사진을 찍어서 저장하는 동작을 설정
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // 카메라 어플이 존재하면 실행
        if(intent.resolveActivity(getContext().getPackageManager()) != null)
            startActivityForResult(intent, 101);
        else
            Log.d("camera", "failed");
    }

    private File createFile() { // 프로필 사진 파일
        String name = "capture.jpg";
        File storage = getContext().getExternalFilesDir(null);
        File outFile = new File(storage, name);

        return outFile;
    }

    private File createFile2() {    // 서명 사진 파일
        String name = "signature.png";
        File storage = getContext().getExternalFilesDir(null);
        File outFile = new File(storage, name);

        return outFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 사진을 찍고나서 imageView 에 찍은 사진 설정
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101 && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            imageView.setImageBitmap(bitmap);
        }
    }
}
