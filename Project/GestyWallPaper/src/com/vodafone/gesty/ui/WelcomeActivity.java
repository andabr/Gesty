package com.vodafone.gesty.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.vodafone.gesty.R;

public class WelcomeActivity extends Activity {

	private TypedArray mPages;
	private int mCurrentPageIndex = FIRST_PAGE - 1;
	private FrameLayout mPageContainer;
	private ImageView mBackImage;
	private ImageView mForwardImage;
	private Button mSkipButton;
	
	private static final int DEFAULT_PAGE_INDEX = 0;
	private static final int FIRST_PAGE = 0;
	
    private OnClickListener mBackImageListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showPreviousPage();
		}
    };
    
    private OnClickListener mForwardImageListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showNextPage();
		}
    };
    
    private OnClickListener mSkipButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			launchTermsAndConditions();
		}
    };
    
    private void launchTermsAndConditions() {
		Intent launchIntent = new Intent();
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	launchIntent.setClassName(getPackageName(),
                GestureListActivity.class.getName()); 
//    	launchIntent.putExtra(Constants.SKIP, true);
    	startActivity(launchIntent);
    	finish();
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);      

		setup();
	}
	
	private void setup() {
		setContentView(R.layout.welcome);
        mPageContainer = (FrameLayout) findViewById(R.id.page_container);
        mBackImage = (ImageView) findViewById(R.id.back);
        mBackImage.setOnClickListener(mBackImageListener);
        mForwardImage = (ImageView) findViewById(R.id.forward);
        mForwardImage.setOnClickListener(mForwardImageListener);
        mSkipButton = (Button) findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(mSkipButtonListener);
		loadPageArray();
		hideBackImage();
		showNextPage();
	}
	
	private void loadPageArray() {
		Resources res = getResources();
		mPages = res.obtainTypedArray(R.array.intro_pages);
	}
		
	private void showPage(int index) {
		LayoutInflater inflater = getLayoutInflater();
        final View pageView = 
        	(View) inflater.inflate(mPages.getResourceId(index, DEFAULT_PAGE_INDEX), null);
        mPageContainer.removeAllViews();
        mPageContainer.addView(pageView);
	}
	
	private boolean isFirstPage() {
		return mCurrentPageIndex == FIRST_PAGE;
	}
	
	private boolean isLastPage() {
		return mCurrentPageIndex == mPages.length() - 1;
	}
	
	private void showPreviousPage() {
		if(isLastPage()) {
			showForwardImage();
			setRegularPageSkipButton();
		}
		
		showPage(--mCurrentPageIndex);
		
		if(isFirstPage()) {
			hideBackImage();
		}
	}
	
	private void showNextPage() {
		if(isFirstPage()) {
			showBackImage();
		}
		
		showPage(++mCurrentPageIndex);
		
		if(isLastPage()) {
			hideForwardImage();
			setLastPageSkipButton();
		}
	}
	
	private void setRegularPageSkipButton() {
		mSkipButton.setText(R.string.welcome_skip_intro);
//		setSkipButtonWidth(R.dimen.welcome_skip_button_width);		
	}
	
	private void setLastPageSkipButton() {
		mSkipButton.setText("OK");
//		setSkipButtonWidth(R.dimen.welcome_ok_button_width);
	}
	
//	private void setSkipButtonWidth(int dimensionId) {
//		Resources resources = getResources();
//		int pixels = resources.getDimensionPixelSize(dimensionId); 
//		mSkipButton.setWidth(pixels);
//	}
		
	private void hideBackImage() {
		hideImage(mBackImage);
	}
	
	private void hideForwardImage() {
		hideImage(mForwardImage);
	}
	
	private void hideImage(ImageView image) {
		image.setVisibility(View.INVISIBLE);
	}
	
	private void showBackImage() {
		showImage(mBackImage);
	}
	
	private void showForwardImage() {
		showImage(mForwardImage);
	}
	
	private void showImage(ImageView image) {
		image.setVisibility(View.VISIBLE);
	}
}