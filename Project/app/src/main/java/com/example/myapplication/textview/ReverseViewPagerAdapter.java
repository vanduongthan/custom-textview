package com.example.myapplication.textview;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ReverseViewPagerAdapter extends FragmentStateAdapter {
    private final String TAG = "duongtv";
    private boolean mIsVertical = false; //defaul horizontal textview, LTR viewpager
    private String mText;
    private PageFragment mFragment;

    public ReverseViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public ReverseViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String text) {
        super(fragmentActivity);
        mText = text;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "createFragment: "+ position);
        mFragment = PageFragment.newInstance(position, mText, mIsVertical);
        return mFragment;
    }

    @Override
    public int getItemCount() {
        return 100;
    }

    public int rotate(){
        mIsVertical = !mIsVertical;
        int totalPage = 0;
        try {
            ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
            totalPage = (int) mExecutorService.submit(new CalculatorPageCallable(mFragment.getTextView())).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int page = 0;
        while(page < totalPage - 1 && VTextView.pageIndex[page + 1] <= mFragment.getTextView().getMarkedIndex()){
            page++;
        }

        return page;
    }

    public void setData(String text){
        mText = text;
        notifyDataSetChanged();
    }
}

