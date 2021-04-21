package com.example.myapplication.textview;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class PageFragment extends Fragment {
    final String TAG = "duongtv";

    public static String PAGE = "page";
    public static String CONTENT = "content";
    public static String VERTICAL = "vertical";

    private int mPage;
    private String mContent;
    private VTextView mTextView;
    private boolean mIsVertical = false;

    public PageFragment() {
    }

    public static PageFragment newInstance(int page, String content, boolean isVertical) {
        PageFragment instance = new PageFragment();
        Bundle args = new Bundle();
        args.putInt(PAGE, page);
        args.putString(CONTENT, content);
        args.putBoolean(VERTICAL, isVertical);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            Bundle args = getArguments();
            mPage = args.getInt(PAGE);
            mContent = args.getString(CONTENT);
            mIsVertical = args.getBoolean(VERTICAL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: page: "+ mPage);
        mTextView = view.findViewById(R.id.vTextView);
        mTextView.setVertical(mIsVertical);
        mTextView.setText(mContent);
        mTextView.setPage(mPage);
    }

    public VTextView getTextView() {
        return mTextView;
    }
}
