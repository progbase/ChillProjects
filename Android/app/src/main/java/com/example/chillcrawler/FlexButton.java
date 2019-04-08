package com.example.chillcrawler;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class FlexButton extends ControlActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);




    }




}

//public class FlexButton implements View.OnClickListener {
//    public FlexButton(Button button) {
//        Button flex = button;
//
//
//    }
//
//    private int BUTTON_STATE = 0;
//    private int BUTTON_STATE_ONCE = 0;
//    private int BUTTON_STATE_TWICE = 1;
//
//    @Override
//    public void onClick(View view) {
//        if (BUTTON_STATE == BUTTON_STATE_ONCE) {
//            flex.setTextColor(getResources().getColor(R.color.colorBlue));
//            BUTTON_STATE = BUTTON_STATE_TWICE;
//        } else if (BUTTON_STATE == BUTTON_STATE_TWICE) {
//            flex.setTextColor(getResources().getColor(R.color.colorGrey));
//            BUTTON_STATE = BUTTON_STATE_ONCE;
//        }
//    }
//});
//
//
//        flex.setOnClickListener(new View.OnClickListener(){
//
//
//        }
//
//
//        }
