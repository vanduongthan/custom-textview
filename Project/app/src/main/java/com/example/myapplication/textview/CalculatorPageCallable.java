package com.example.myapplication.textview;

import android.util.Log;

import java.util.concurrent.Callable;

public class CalculatorPageCallable implements Callable {

    private VTextView mVTextView;

    public CalculatorPageCallable(VTextView vTextView){
        mVTextView = vTextView;
    }

    @Override
    public Object call() {
        int totalPage = 0;
        Log.d("page",totalPage+"");
        while( !mVTextView.textDraw( null , totalPage , false, null) ){
            totalPage++;
        }
        return totalPage;
    }
}
