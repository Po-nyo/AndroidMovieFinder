package com.example.startproject2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.startproject2.MainActivity.handler;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{

    ArrayList<Movie> items = new ArrayList<Movie>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.movie_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Movie item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Movie item){
        items.add(item);
    }

    public void setItems(ArrayList<Movie> items){
        this.items = items;
    }

    public Movie getItem(int position) {
        return items.get(position);
    }

    public void clearItems() {
        this.items.clear();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView, textView2, textView3, textView4;
        ImageView imageView;
        RatingBar ratingBar;

        public final View layout;

        Bitmap bitmap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView);
            textView2 = itemView.findViewById(R.id.textView2);
            textView3 = itemView.findViewById(R.id.textView3);
            textView4 = itemView.findViewById(R.id.textView4);
            imageView = itemView.findViewById(R.id.imageView);
            ratingBar = itemView.findViewById(R.id.ratingBar);

            layout = itemView;
        }

        public void setItem(final Movie item) {
            textView.setText(item.title);
            textView2.setText("감독: " + item.director);
            textView3.setText("출연: " + item.actor);

            if(item.userRating != 0) {
                textView4.setText(item.userRating + " ");
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setRating(item.userRating / 2);
            } else {
                textView4.setText("평점 없음");
                ratingBar.setVisibility(View.GONE);
            }

            // 영화 레이아웃을 누르면 인터넷창으로 전환
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.link));
                    v.getContext().startActivity(intent);
                }
            });

            // image url을 가져와서 영화 image 띄움
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        String image = item.image; // image의 url
                        URL url = new URL(image);
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), conn.getContentLength());
                        bitmap = BitmapFactory.decodeStream(bis); // bitmap 에 저장

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap); // 이미지 띄움
                            }
                        });
                    } catch (Exception e) { }
                }
            }.start();

        }
    }
}
