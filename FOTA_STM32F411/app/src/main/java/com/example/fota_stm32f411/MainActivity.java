package com.example.fota_stm32f411;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // bluetooth
    BluetoothSupport bluetoothSupport;
    ProgressBar progressBar,progressSend;
    TextView tvTrangthai;
    ImageView imgSpeech;
    Button button,button2,Cre,Disconnect,Connect;
    public static int Request_open_bluetooth = 101;
    static  int PREMISSION_REQUEST_STORAGE=1000;
    static  int PREMISSION_REQUEST_STORAGE_WRITE=2000;
    private static final String LOG_TAG = "ExternalStorageDemo";

    static  String path ;
    static  String pathSub;
    static int SizeLine;
    static String fileContent="";
    static String rawdata="";
    static  int pushes=0;
    static String fileout= "";
    TextToSpeech toSpeech;

    // File
    Button buttonopenDailog, send,buttonUp;
    TextView OutputDataFile;
    TextView dataPath;
    File root, fileroot, curFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.progressBar);
        progressSend = findViewById(R.id.progressSend);
        tvTrangthai = findViewById(R.id.tvTrangthai);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        Cre = findViewById(R.id.Cre);
        Disconnect = findViewById(R.id.Disconnect);
        Connect = findViewById(R.id.Connect);
        imgSpeech = findViewById(R.id.imgSpeech);
        progressBar.setVisibility(View.INVISIBLE);
        progressSend.setVisibility(View.INVISIBLE);
        bluetoothSupport = new BluetoothSupport(this);
        dataPath=findViewById(R.id.dataPath);
        buttonopenDailog= findViewById(R.id.buttonopenDailog);
        send=findViewById(R.id.send);
        OutputDataFile = findViewById(R.id.OutputDataFile);
        OutputDataFile.setMovementMethod(new ScrollingMovementMethod());

        // request permission
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PREMISSION_REQUEST_STORAGE);
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PREMISSION_REQUEST_STORAGE_WRITE);
        }

        buttonopenDailog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myfileintent = new Intent(Intent.ACTION_GET_CONTENT);
                myfileintent.addCategory(Intent.CATEGORY_OPENABLE);
                myfileintent.setType("*/*");
                startActivityForResult(myfileintent,100);
            }
        });

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        final int BUFFER_SIZE = 16 * 1024; // 16KB

            send.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                public void onClick(View v) {
                        int Sizeof = fileContent.length();  int pack = Sizeof/128+1; int push;
                        OutputDataFile.setText("");
                        rawdata = fileContent.substring(0,3228);
                        bluetoothSupport.write(rawdata);
                        writeFile(rawdata);
                        OutputDataFile.append("Send Byte "+ String.valueOf(Sizeof)+"\n" + rawdata+"\n");
                            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                            dialog.setTitle("Update Firmware");
                            dialog.setMessage("Run New Application");
                            dialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bluetoothSupport.write("!");
                            }
                        });
                            dialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                        dialog.create();
                        dialog.show();


                }
            });



        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        imgSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSpeechInput();
            }
        });

        Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListBluetoothActivity.class);
                startActivityForResult(intent, Request_open_bluetooth);
            }
        });
        Disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothSupport.isConnect()){
                    Toast.makeText(MainActivity.this,"Connect bluetooth",Toast.LENGTH_LONG).show();
                }else {
                    bluetoothSupport.disConnect();
                    tvTrangthai.setText("DISCONNECT");
                    Connect.setEnabled(true);

                }
            }
        });

        Cre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("About");
                dialog.setMessage("Tuấn Đoàn \n" + "Cơ điện tử K61 \n" + "04/17/2020 ");
                dialog.setNegativeButton("Ừ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.create();
                dialog.show();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if(!bluetoothSupport.isConnect()){
                    Toast.makeText(MainActivity.this,"Connect bluetooth",Toast.LENGTH_LONG).show();
                }else {
                    bluetoothSupport.write("on");
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothSupport.isConnect()){
                    Toast.makeText(MainActivity.this,"Connect bluetooth",Toast.LENGTH_LONG).show();
                }else {
                    bluetoothSupport.write("off");
                }
            }
        });

        bluetoothSupport.setOnConnectListener(new BluetoothSupport.OnConnect() {
            @Override
            public void connected() {
                bluetoothSupport.isConnect();
                tvTrangthai.setText("CONNECTED");
                tvTrangthai.setTextColor(Color.GREEN);
                progressBar.setVisibility(View.INVISIBLE);
                //imageView.setImageResource(R.drawable.ic_bluetooth_black_24dp);
            }

            @Override
            public void error() {
                tvTrangthai.setText("DISCONNECT");
                tvTrangthai.setTextColor(Color.RED);
                progressBar.setVisibility(View.INVISIBLE);
                //imageView.setImageResource(R.drawable.ic_bluetooth_disabled_black_24dp);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



//    public String toHex(String arg) {
//        return String.format("%040x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getSpeechInput() {

        if(bluetoothSupport.isConnect()){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            if(intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 10);
            } else {
                Toast.makeText(this, "aaaa", Toast.LENGTH_LONG);
            }
        } else Toast.makeText(this, "Connect bluetooth", Toast.LENGTH_SHORT).show();

    }
    public File getAppExternalFilesDir()  {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            // /storage/emulated/0/Android/data/file
            return this.getExternalFilesDir(null);
        } else {
            // @Deprecated in API 29.
            // /storage/emulated/0
            return Environment.getExternalStorageDirectory();
        }
    }


    private void writeFile(String output) {
        try {
            File extStore = this.getAppExternalFilesDir( );

            boolean canWrite = extStore.canWrite();
            Log.i(LOG_TAG, "Can write: " + extStore.getAbsolutePath()+" : " + canWrite);

            // ==> /storage/emulated/0/note.txt  (API < 29)
            // ==> /storage/emulated/0/Android/data/files/note.txt (API >=29)
            String path2 = extStore.getAbsolutePath() + "/" + "output.txt";
            Log.i(LOG_TAG, "Save to: " + path2);

            String data = output;
            Log.i(LOG_TAG, "Data: " + data);


            File myFile = new File(path2);
            FileOutputStream fOut = new FileOutputStream(myFile);
            fOut.write(data.getBytes("ISO_8859_1"));
            fOut.close();

            Toast.makeText(getApplicationContext(), "output.txt" + " saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Write Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "Write Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void readFile(String input) {

        File extStore = this.getAppExternalFilesDir();
        // ==> /storage/emulated/0/note.txt  (API < 29)
        // ==> /storage/emulated/0/Android/data/files (API >=29)
        String path1 = extStore.getAbsolutePath() + input;//"/" + "Download/App1.bin";
        Log.i(LOG_TAG, "Read file: " + path1);
        String s = "";
        String c ="";
        fileContent = "";
        try {
            File myFile = new File(path1);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn, StandardCharsets.ISO_8859_1));
            while ((s = myReader.readLine()) != null) {
//                pushes++;
//                if(pushes==5||pushes==9||pushes==11||pushes==12)
//                {
//                    fileContent += s +"\r";
//                }
                fileContent += s +"\n";
            }
            Toast.makeText(MainActivity.this,String.valueOf(pushes),Toast.LENGTH_LONG).show();
            myReader.close();
            this.OutputDataFile.setText(fileContent);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Read Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "Read Error: " + e.getMessage());
            e.printStackTrace();
        }
        //Toast.makeText(getApplicationContext(), "Download/App1.bin", Toast.LENGTH_LONG).show();
    }


    private  String textReadFile (String input)
    {
        File file = new File(Environment.getExternalStorageDirectory(),input);
        StringBuilder text = new StringBuilder();

        try
        {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line ;
            while ((line = bufferedReader.readLine()) != null )
            {
                    text.append(line);
                    text.append("\n");
            }
            bufferedReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return text.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static BufferedReader openFile(String fileName)
            throws IOException {
        // Don't forget to add buffering to have better performance!
        return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_16));
    }


    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Request_open_bluetooth && resultCode == RESULT_CANCELED) {
            Toast.makeText(MainActivity.this, "Ban nen mo", Toast.LENGTH_LONG).show();
        } else if (requestCode == Request_open_bluetooth && resultCode == RESULT_OK) {
            String diachi = data.getStringExtra("addr");
            String ten = data.getStringExtra("ten");
            bluetoothSupport.connect(diachi);
            progressBar.setVisibility(View.VISIBLE);
            tvTrangthai.setText("Connecting ...");
            tvTrangthai.setTextColor(Color.BLUE);
            Connect.setEnabled(false);
        }

        switch (requestCode){
            case  10:
                if(resultCode == RESULT_OK &&  data !=null){
                    final ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //tvResult.setText(result.get(0));
                    toSpeech= new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onInit(int status) {
                            toSpeech.setLanguage(Locale.forLanguageTag(RecognizerIntent.EXTRA_LANGUAGE_MODEL));
                            toSpeech.speak( result.get(0),TextToSpeech.QUEUE_FLUSH,null);
                        }
                    });
                    bluetoothSupport.write(result.get(0));
                }
                break;

            case 100:
                if(resultCode==RESULT_OK && data != null)
                {
                    Uri uri =  data.getData();
                    path = data.getData().getPath();
                    dataPath.setText(path);
                    pathSub=path.substring(path.indexOf("0")+1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        readFile(pathSub);
                    }
                    //OutputDataFile.setText(textReadFile(pathSub));
                    writeFile(fileContent);
                    Toast.makeText(MainActivity.this,pathSub,Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
