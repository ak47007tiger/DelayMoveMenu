package com.delaymove.app;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import widget.TouchDelayMoveLayout;


public class MainActivity extends Activity {
    TouchDelayMoveLayout mTouchDelayMoveLayout;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String msg = "null";
            switch (v.getId()){
                case R.id.item0:
                    msg = "item0";
                    break;
                case R.id.item1:
                    msg = "item1";
                    break;
                case R.id.item2:
                    msg = "item2";
                    break;
                case R.id.item3:
                    msg = "item3";
                    break;
                case R.id.item4:
                    msg = "item4";
                    break;
                case R.id.item5:
                    msg = "item5";
                    break;
            }
            Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
            mTouchDelayMoveLayout.switchToTouchModel();
            mTouchDelayMoveLayout.letChildrenBack();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTouchDelayMoveLayout = (TouchDelayMoveLayout) findViewById(R.id.touchDelayMoveL);
        findViewById(R.id.item0).setOnClickListener(mOnClickListener);
        findViewById(R.id.item1).setOnClickListener(mOnClickListener);
        findViewById(R.id.item2).setOnClickListener(mOnClickListener);
        findViewById(R.id.item3).setOnClickListener(mOnClickListener);
        findViewById(R.id.item4).setOnClickListener(mOnClickListener);
        findViewById(R.id.item5).setOnClickListener(mOnClickListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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
}
