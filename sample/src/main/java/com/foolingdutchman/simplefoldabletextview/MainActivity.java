package com.foolingdutchman.simplefoldabletextview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;



public class MainActivity extends AppCompatActivity {
     private  SimpleFoldableTextView mFoldableTextView1;
     private  SimpleFoldableTextView mFoldableTextView2;
     private  SimpleFoldableTextView mFoldableTextView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        showView();
    }

    private void showView() {
        mFoldableTextView1.setText(getResources().getString(R.string.poem1));
        mFoldableTextView2.setText(getResources().getString(R.string.poem2));
        mFoldableTextView3.setText(getResources().getString(R.string.poem3));
        mFoldableTextView3.setToExpandHint("展开压一压");
        mFoldableTextView3.setToFoldHint("收起啊一啊");
        mFoldableTextView3.setExpandHintRelativeSize(0.5f);
        mFoldableTextView3.setFoldHintRelativeSize(0.5f);
    }

    private void initView() {
        mFoldableTextView1= (SimpleFoldableTextView) findViewById(R.id.sftv_item1);
        mFoldableTextView2= (SimpleFoldableTextView) findViewById(R.id.sftv_item2);
        mFoldableTextView3= (SimpleFoldableTextView) findViewById(R.id.sftv_item3);

    }
}
