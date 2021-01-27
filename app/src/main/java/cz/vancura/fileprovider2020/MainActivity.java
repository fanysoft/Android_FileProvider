package cz.vancura.fileprovider2020;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "myTAG";

    int MY_PERMISSIONS_REQUEST = 0;
    Button button;
    private static Uri uri;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "activity started");
        context = this;

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Permission check
                // minimal API 21 (Android 5) = permission hardcoded
                if (Build.VERSION.SDK_INT < 23) {
                    // ok
                    Log.d(TAG, "Permission API < 23 - granted already");
                } else {
                    // target API 23 (Android 6 and higher)
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // must ask for permision
                        Log.d(TAG, "Permission API =>23 - not granted so far - requesting permition");

                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);

                    } else{
                        // ok
                        Log.d(TAG, "Permission API =>23 - granted already");
                        // do some work
                        doWork();
                    }
                }

            }
        });

    }

    private void doWork() {
        // work here

        // Create file - in internal dir - data/data/cz.vancura.fileprovider2020/files
        String filename = "myfile.txt";
        String fileContents = "Hello world!";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // File to share
        File parentDir = this.getFilesDir();
        File file = new File(parentDir, filename);
        Log.d(TAG, "file=" + file); // file=/data/user/0/cz.vancura.fileprovider2020/files/myfile.txt

        // Fileprovider
        uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);
        Log.d(TAG, "uri="+uri); // uri=content://cz.vancura.fileprovider2020/files/myfile.txt

        // Intent
        Intent intent = ShareCompat.IntentBuilder.from(MainActivity.this)
                .setType("application/txt")
                .setStream(uri)
                .setChooserTitle("Choose bar")
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PackageManager pm = MainActivity.this.getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent);
        }else{
            Log.e(TAG, "menu_export() - Device can not open this file");
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {

            case 0 : { // MY_PERMISSIONS_REQUEST

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Log.d(TAG, "Permission granted in runtime by user");
                    // do some work
                    doWork();
                } else {
                    // permission denied, boo!
                    Log.d(TAG, "Permission denied in runtime by user");
                }
                return;
            }
        }
    }

}