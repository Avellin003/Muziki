//In charge of reading storage, Searching for mp3 and wav files and also search filtering.

package com.example.muziki;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.karumi.dexter.Dexter;

import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;//Definition of variables to use
    ArrayList<File> mySongs;
    boolean permissionGranted = false;
    CustomAdapter adapter;
    ArrayList<File> originalSongs; // to store original list of songs before search
    ArrayList<File> currentSongs; // to store current list of songs after search

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//calls the skeleton display of page"activity_main.xml"

        listView = findViewById(R.id.listViewSong);//calls the skelton display of each song "list_item.xml"
        final SearchView searchView = findViewById(R.id.searchView);//links the to the search bar icon
        originalSongs = new ArrayList<>();
        currentSongs = new ArrayList<>();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }//makes sure to erase all the query questions in search filtering
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    if (newText.isEmpty()) {
                        currentSongs = new ArrayList<>(originalSongs);
                        updateListView();
                    } else {
                        searchSongs(newText);
                    }
                }
                return true;
            }
        });//performs search filtering

        if (!permissionGranted) {
            runtimePermission();
        } else {
            displaySongs();
        }//checks if the permission was granted before this allows us not to ask permissions when we are already granted them
    }

    private void searchSongs(String query) {
        currentSongs = new ArrayList<>();
        for (File song : originalSongs) {
            if (song.getName().toLowerCase().contains(query.toLowerCase())) {
                currentSongs.add(song);
            }
        }
        updateListView();
    }//in charge of searching songs

    public void runtimePermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        permissionGranted = true;
                        displaySongs();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }//Asks permission and defines what happens when the permission is ignored or refused

    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();

        File[] files = file.listFiles();
        if (files != null) {
            for (File singlefile : files) {
                if (singlefile.isDirectory() && !singlefile.isHidden()) {
                    arrayList.addAll(findSong(singlefile));
                } else {
                    if (singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")) {
                        arrayList.add(singlefile);
                    }
                }
            }
        }
        return arrayList;
    }//after reading all the file the app now seeks specific files namely mp3 and wav files that is what this function does

    void displaySongs() {
        mySongs = findSong(Environment.getExternalStorageDirectory());
        originalSongs.addAll(mySongs);
        currentSongs.addAll(mySongs);
        updateListView();
    }//displays the songs for both search filtering and the initial list of songs

    void updateListView() {
        adapter = new CustomAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = ((TextView) view.findViewById(R.id.txtsongname)).getText().toString();
                ArrayList<File> songsToPlay = currentSongs.isEmpty() ? originalSongs : mySongs;
                int positionInOriginal = originalSongs.indexOf(currentSongs.isEmpty() ? originalSongs.get(i) : currentSongs.get(i));
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs", songsToPlay)
                        .putExtra("songname", songName)
                        .putExtra("position", positionInOriginal));
            }
        });
    }//this one is charge of passing the songs infos to the PlayerActivity.java files for being played

    class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return currentSongs.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textsong = myView.findViewById(R.id.txtsongname);
            textsong.setSelected(true);
            textsong.setText(currentSongs.get(i).getName().replace(".mp3", "").replace(".wav", ""));

            return myView;
        }
    }
}