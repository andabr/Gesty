package com.vodafone.gesty.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

import com.vodafone.gesty.R;
import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.data.Contact;
import com.vodafone.gesty.data.GestureDetail;
import com.vodafone.gesty.data.NativeContacts;

public class ContactListActivity extends BasicMenuActivity implements TextWatcher, OnEditorActionListener {
	
	private boolean mModeDirect; 
	
	private ToggleButton mAllToggle;
    private ToggleButton mFavsToggle;

    private ContactListAdapter mAdapter;
    private int mShow;
    
    private EditText mSearchEditText;
    private String mInitialFilter;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		super.onCreate(savedInstanceState);
        setContentView(R.layout.contactslist);
        
        Intent intent = getIntent();
        mModeDirect = intent.getBooleanExtra(Constants.MODE_DIRECT, false);
        
		setupListAdapter();
		setupButtons();
		setupSearchView();
	}
	
	private void setupListAdapter() {
		mAdapter = new ContactListAdapter(this, getGestyApp().getContactPhotoLoader());
		mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			
			@Override
			public Cursor runQuery(CharSequence constraint) {
				return NativeContacts.getSearchContactsCursor(getContentResolver(),
						mShow == ContactListAdapter.MODE_FAVS, constraint.toString());
			}
		});
		setListAdapter(mAdapter);
	}
	
	private void setupButtons() {
		mAllToggle = (ToggleButton) findViewById(R.id.toggle_tab_all_contacts);
        mAllToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateButtons(ContactListAdapter.MODE_ALL);
				mAdapter.setMode(mShow);
			}
		});
        mFavsToggle = (ToggleButton) findViewById(R.id.toggle_tab_fav_contacts);
        mFavsToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateButtons(ContactListAdapter.MODE_FAVS);
				mAdapter.setMode(mShow);
			}
		});
        updateButtons(ContactListAdapter.MODE_ALL);
		
	}
	
	private void updateButtons(int showGestures) {
	   	mShow = showGestures;
	    mFavsToggle.setChecked(mShow == ContactListAdapter.MODE_FAVS);
	    mAllToggle.setChecked(mShow == ContactListAdapter.MODE_ALL);
	}
	 
	private GestyApp getGestyApp() {
	    return (GestyApp)getApplication();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	    super.onListItemClick(l, v, position, id);
	    Contact contact = mAdapter.getItem(position);
        if (contact != null) {
        	Intent intent = null;
        	if (mModeDirect) {
    	        intent = new Intent(this, ContactViewActivity.class);
    	        intent.putExtra(Constants.CONTACT_ID, contact.getId());
    	    } else {
    	    	intent = new Intent(this, CreateGestureActivity.class);
    	        intent.putExtra(Constants.CONTACT_DETAIL_VALUE, contact.getDisplayName());
    	        intent.putExtra(Constants.CONTACT_ID, contact.getId());
    	        intent.putExtra(Constants.CONTACT_NAME, contact.getDisplayName());
    	        intent.putExtra(Constants.ACTION_ID, GestureDetail.ACTION_VIEW_CONTACT);
    	        intent.putExtra(Constants.PHOTO_ID, contact.getPhotoId());
    	        intent.putExtra(Constants.CONTACT_NAME, contact.getDisplayName());

    	    }	
        	startActivityForResult(intent, GestyApp.DIALOG_CODE);
        }
	}
	
	@Override
	protected void onPause() {
	    getGestyApp().getContactPhotoLoader().pause();
	    super.onPause();
	}
	
	@Override
	protected void onResume() {
	    super.onStart();
	    if (mAdapter != null) {
	    	mAdapter.refresh();
		    getGestyApp().getContactPhotoLoader().resume();
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (GestyApp.DIALOG_CODE == requestCode && (resultCode == Activity.RESULT_OK)) {
			setResult(resultCode);
			finish();
		}
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(Constants.MODE, mShow);
        outState.putBoolean(Constants.MODE_DIRECT, mModeDirect);
        
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mShow = savedInstanceState.getInt(Constants.MODE);
        mModeDirect = savedInstanceState.getBoolean(Constants.MODE_DIRECT);
        updateButtons(mShow);
    }

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
        mAdapter.getFilter().filter(s);
	}
	
	@Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } 
    }

    @Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideSoftKeyboard();
            if (TextUtils.isEmpty(getTextFilter())) {
                finish();
            }
            return true;
        }
        return false;
	}

    protected String getTextFilter() {
        if (mSearchEditText != null) {
            return mSearchEditText.getText().toString();
        }
        return null;
    }
    
    private void hideSoftKeyboard() {
        // Hide soft keyboard, if visible
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        ListView listView = getListView();
        inputMethodManager.hideSoftInputFromWindow(listView.getWindowToken(), 0);
    }
    
    /**
     * Configures search UI.
     */
    private void setupSearchView() {
        mSearchEditText = (EditText)findViewById(R.id.search_src_text);
        mSearchEditText.addTextChangedListener(this);
        mSearchEditText.setOnEditorActionListener(this);
        mSearchEditText.setText(mInitialFilter);
        final InputMethodManager inputMethodManager = (InputMethodManager)
        	getSystemService(Context.INPUT_METHOD_SERVICE);

		mSearchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		        	inputMethodManager.showSoftInput(mSearchEditText, WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		        }
		    }
		});
    }
}
