package com.example.chillcrawler;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.example.chillcrawler.JoystickView;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;


public class ControlActivity extends AppCompatActivity implements JoystickView.JoystickListener {

    EditText editText;
    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;
    String res;
    RecognitionProgressView recognitionProgressView;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;




    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
        //REQUEST RECORD AUDIO PERMISSION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS");
        setContentView(R.layout.activity_control);
        ControlActivity.this.new ConnectBT().execute();
        int[] colors = {
                ContextCompat.getColor(this, R.color.colorGreen),
                ContextCompat.getColor(this, android.R.color.holo_blue_light),
                ContextCompat.getColor(this, R.color.colorGreen),
                ContextCompat.getColor(this, android.R.color.holo_blue_light),
                ContextCompat.getColor(this, R.color.colorGreen)
        };
        int[] heights = {60, 76, 58, 80, 55};


        //SpeechRecognizer

        editText = findViewById(R.id.editText);
        editText.setClickable(false);
        editText.setEnabled(false);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);

        //Audio visualizer
        recognitionProgressView = findViewById(R.id.recognition_view);
        recognitionProgressView.setSpeechRecognizer(mSpeechRecognizer);
        recognitionProgressView.setColors(colors);
        recognitionProgressView.setBarMaxHeightsInDp(heights);
        recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onResults(Bundle results) {
                showResults(results);
                recognitionProgressView.stop();
                recognitionProgressView.play();
            }
        });

        recognitionProgressView.setSpacingInDp(2);
        recognitionProgressView.setIdleStateAmplitudeInDp(2);
        recognitionProgressView.play();


        findViewById(R.id.microphone).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:

                        mSpeechRecognizer.stopListening();
                        recognitionProgressView.stop();
                        recognitionProgressView.play();



                        break;

                    case MotionEvent.ACTION_DOWN:

                        startRecognition();
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        recognitionProgressView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startRecognition();
                            }
                        }, 50);


                        editText.setText("");




                        break;
                }
                return false;
            }

        });


//BOOM MENU
        BoomMenuButton bmb = findViewById(R.id.bmb);


        TextInsideCircleButton.Builder builder = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_octicons_arrow_up)
                .normalTextRes(R.string.stand)
                .rippleEffect(true)
                .imagePadding(new Rect(0, 0, 0, 10))
                .normalColorRes(R.color.colorGreen)
                .pieceColor(Color.BLACK)
                .highlightedTextColor(Color.BLACK)
                .highlightedColor(R.color.colorGreen)
                .normalTextColor(Color.BLACK)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        // When the boom-button corresponding this builder is clicked.
                        //Toast.makeText(ControlActivity.this, "Clicked " + index, Toast.LENGTH_SHORT).show();
                        sendStand();
                    }
                });
        bmb.addBuilder(builder);
        TextInsideCircleButton.Builder builder1 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_octicons_arrow_down)
                .normalTextRes(R.string.sit)
                .rippleEffect(true)
                .normalColorRes(R.color.colorGreen)
                .highlightedColor(R.color.colorGreen)
                .normalTextColor(Color.BLACK)
                .pieceColor(Color.BLACK)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        // When the boom-button corresponding this builder is clicked.
                        //Toast.makeText(ControlActivity.this, "Clicked " + index, Toast.LENGTH_SHORT).show();
                        sendSit();
                    }
                });

        bmb.addBuilder(builder1);
        TextInsideCircleButton.Builder builder2 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_waving_hand)
                .normalColorRes(R.color.colorGreen)
                .highlightedColor(R.color.colorGreen)
                .rippleEffect(true)
                .highlightedTextColor(Color.BLACK)
                .imagePadding(new Rect(10, 5, 0, 0))
                .pieceColor(Color.BLACK)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        // When the boom-button corresponding this builder is clicked.
                        //Toast.makeText(ControlActivity.this, "Clicked " + index, Toast.LENGTH_SHORT).show();
                        sendWave();
                    }
                });
        bmb.addBuilder(builder2);
//BOOM MENU END


        final Button flex = findViewById(R.id.flex);

        flex.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                    sendflex();


            }
        });


        final Button tips = findViewById(R.id.tips);
        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.tips_popup, null, false);


                // create the popup window
//
               popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int width = popupView.getMeasuredWidth();
                int height = popupView.getMeasuredHeight();

                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                //tips.setVisibility(View.INVISIBLE);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                       //tips.setVisibility(View.VISIBLE);
                        popupWindow.dismiss();

                        return true;
                    }
                });
            }
        });

    }

//for toasts
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

//COMMANDS TO ARDUINO
    private void sendStand() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("STAND UP".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void sendVoice(String voice) {
        if (btSocket != null) {
            try {
                voice = voice.toUpperCase();
                btSocket.getOutputStream().write(voice.getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void sendSit() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("SIT DOWN".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void sendWave() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("WAVE".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void showResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (matches != null) {
            //Log.d("TAG", matches.get(0));
            // msg(matches.get(0));
            res = matches.get(0);
            Log.d("TAG", res);
            editText.setText(res);
            sendVoice(res);
        }

    }

    private void sendflex() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("FLEX".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }




    //TOOLBAR MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    protected void onDestroy() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
        super.onDestroy();
    }

    private void startRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        mSpeechRecognizer.startListening(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {

            case R.id.action_voice:

                //REQUEST RECORD_AUDIO permission
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

                item.setChecked(!item.isChecked());

                LinearLayout dim_layout = findViewById(R.id.dim_layout);  //overlay layout
                LinearLayout voice_layout = findViewById(R.id.voice_layout);
                if (item.isChecked()) {
                    dim_layout.setVisibility(View.VISIBLE);
                    voice_layout.setVisibility(View.VISIBLE);
                } else if (!item.isChecked()) {
                    dim_layout.setVisibility(View.INVISIBLE);
                    voice_layout.setVisibility(View.INVISIBLE);
                }
                //do stuff
                editText.setText("");

                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(getApplicationContext()));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });


                // Toast.makeText(ControlActivity.this, "test1 clicked", Toast.LENGTH_SHORT).show();

                break;
//            case R.id.action_exit:
//                finish();
//                //System.exit(0);
//                return true;
            //Toast.makeText(MainActivity.this, "smthing", Toast.LENGTH_SHORT).show();
//            case R.id.action_delete:
//
//                Toast.makeText(MainActivity.this, "smthing", Toast.LENGTH_SHORT).show();
//
//                break;
//            case R.id.action_logout:
//
//                Toast.makeText(MainActivity.this, "smthing", Toast.LENGTH_SHORT).show();
//
//                break;
//            case R.id.action_refresh:
//
//                Toast.makeText(MainActivity.this, "smthing", Toast.LENGTH_SHORT).show();
//
//                break;
//


        }

        return super.onOptionsItemSelected(item);
    }

//WHEN JOYSTICK TOUCHED

    @Override
    public void onJoystickMoved(float xPercent, float yPercent, int id) {
        Log.d("Main Mehtod", "X percent: " + xPercent + "Y percent: " + yPercent);   //JOYSTICK


        if (xPercent > 0.90) {

            try {
                btSocket.getOutputStream().write("TURN RIGHT\0".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
            Log.d("Main Mehtod", "RIGHT SENT");
        } else if (xPercent < -0.90) {

            try {
                btSocket.getOutputStream().write("TURN LEFT\0".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
            Log.d("Main Mehtod", "LEFT SENT");
        }

        if (yPercent < -0.90) {

            try {
                btSocket.getOutputStream().write("MOVE FORWARD\0".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
            Log.d("Main Mehtod", "FORWARD SENT");
        } else if (yPercent > 0.90) {

            try {
                btSocket.getOutputStream().write("MOVE BACK\0".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
            Log.d("Main Mehtod", "BACK SENT");
        }

//        if (buff != "STANDBY") {
//            try {
//                btSocket.getOutputStream().write(buff.toString().getBytes());
//            } catch (IOException e) {
//                msg("Error");
//            }
//        }

    }


    //BT CONNECT
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ControlActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}



