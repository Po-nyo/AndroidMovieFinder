package com.example.startproject2;


import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static com.example.startproject2.MainActivity.handler;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    MovieAdapter adapter;
    RecyclerView recyclerView;

    BufferedReader br;

    String uriString = "content://com.example.startproject2.movieprovider/movie";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        adapter = new MovieAdapter();

        recyclerView = rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        SearchView searchView = rootView.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                clearMovie();   // 데이터베이스 clear
                searchNaver(query); // 입력받은 query(제목) 검색수행, 데이터베이스에 입력
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        File file = new File(getContext().getDatabasePath("movie.db").toString());
        if(file.exists())   // 데이터베이스 파일이 존재하면
            queryMovie();   // 목록을 불러오는 쿼리실행
        else
            Log.d("test", "No Database File");

    }

    private void queryMovie() {
        Uri uri = new Uri.Builder().build().parse(uriString);   // uri 설정
        String[] columns = new String[] {"title", "director", "actor", "link", "rating", "image", "pubDate"};   // column 설정
        // 커서 생성
        Cursor cursor = getActivity().getContentResolver().query(uri, columns, null, null,"name DESC");
        MovieList movieList = new MovieList();  // 새로운 MovieList 객체 생성

        while(cursor.moveToNext()) {    // 데이터베이스 내용 끝까지 읽기
            Movie movie = new Movie();  // 새로운 Movie 객체
            // 커서를 이용하여 Movie 객체의 필드에 데이터 입력
            movie.title = cursor.getString(cursor.getColumnIndex(columns[0]));
            movie.director = cursor.getString(cursor.getColumnIndex(columns[1]));
            movie.actor = cursor.getString(cursor.getColumnIndex(columns[2]));
            movie.link = cursor.getString(cursor.getColumnIndex(columns[3]));
            movie.userRating = cursor.getFloat(cursor.getColumnIndex(columns[4]));
            movie.image = cursor.getString(cursor.getColumnIndex(columns[5]));
            movie.pubDate = cursor.getString(cursor.getColumnIndex(columns[6]));

            movieList.items.add(movie); // MovieList 객체의 item에 생성한 Movie 객체 추가
        }

        adapter.items = movieList.items;    // 어댑터에 추가
        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                // 데이터 바뀐 것 감지
            }
        });
    }

    private void searchNaver(final String searchObject) {
        final String clientId = "eldcp9gzaLzoLz8tm7Us"; // 클라이언트 아이디
        final String clientSecret = "zYLyHCreof"; // 클라이언트 비밀번호
        final int display = 5;  // 개수

        new Thread() {
            @Override
            public void run() {
                try {
                    String text = URLEncoder.encode(searchObject, "UTF-8");
                    String apiURL = "https://openapi.naver.com/v1/search/movie.json?query=" + text + "&display=" + display + "&";
                    // json 으로 결과요청

                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("X-Naver-Client-Id", clientId);
                    con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                    con.setDoInput(true);   // inputStream 으로 서버에게 응답 받음

                    int responseCode = con.getResponseCode();

                    String line;
                    StringBuilder sb = new StringBuilder();

                    if (responseCode == 200) { // 정상 호출
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        while ((line = br.readLine()) != null)
                            sb.append(line + "\n");
                        br.close();
                    } else
                        return;

                    Gson gson = new Gson(); // json 파싱할 gson 객체 생성
                    // 데이터 입력
                    MovieList movieList = gson.fromJson(String.valueOf(sb), MovieList.class);

                    for (int i = 0; i < movieList.items.size(); i++) {
                        // 데이터 가공처리
                        String temp = movieList.items.get(i).title;
                        // 제목에서 볼드 태그 없앰
                        movieList.items.get(i).title = temp.replaceAll("(<b>)|(</b>)", "");

                        temp = movieList.items.get(i).director;
                        // 감독에서 | 제거
                        movieList.items.get(i).director = temp.replace("|", ", ");

                        temp = movieList.items.get(i).actor;
                        // 배우들에서 | 제거
                        movieList.items.get(i).actor = temp.replace("|", " ");
                    }
                    // 어댑터에 추가
                    adapter.items = movieList.items;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            // 데이터 바뀐 것 감지
                        }
                    });
                    insertMovie(movieList); // 검색내용이 저장된 movieList 객체의 내용을 데이터베이스에 저장
                } catch (Exception e) {

                }
            }
        }.start();
    }

    private void insertMovie(MovieList movieList) {
        Log.d("test", "insert");
        Uri uri = new Uri.Builder().build().parse(uriString);   // uri 설정

        if(movieList.items.size() !=0) {
            for(int i=0; i<movieList.items.size(); i++) {   // movieList의 items의 Movie 객체들에 대하여 실행
                Movie movie = movieList.items.get(i);   // i번째 Movie객체 불러옴
                // 데이터들을 ContentValues 객체에 저장
                ContentValues values = new ContentValues();
                values.put("title", movie.title);
                values.put("director", movie.director);
                values.put("actor", movie.actor);
                values.put("link", movie.link);
                values.put("rating", movie.userRating);
                values.put("image", movie.image);
                values.put("pubDate", movie.pubDate);
                // 저장한 ContentValues 데이터베이스에 삽입
                uri = getActivity().getContentResolver().insert(uri, values);
            }
        }
    }

    private void clearMovie() {
        Log.d("test", "clear");
        Uri uri = new Uri.Builder().build().parse(uriString); // uri 설정
        // 데이터베이스 clear
        int count = getActivity().getContentResolver().delete(uri, null, null);
    }
}
