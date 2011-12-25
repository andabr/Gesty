package com.vodafone.gesty.ui;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vodafone.gesty.R;

public class HelpActivity extends ListActivity {
	   
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);      
        setContentView(R.layout.help);

        // Use our own list adapter
        addFooterView();
        setListAdapter(new SpeechListAdapter(this));

    }
       
    private void addFooterView() {
    	View footerView = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.help_list_footer, null, false);
    	getListView().addFooterView(footerView);
    }
    
    public void takeIntro(View v) {
    	Intent launchIntent = new Intent();
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		launchIntent.setClassName(getPackageName(),
		           WelcomeActivity.class.getName());
		startActivity(launchIntent);
		finish();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {    
       ((SpeechListAdapter)getListAdapter()).toggle(position);
    }
    
    /**
     * A sample ListAdapter that presents content
     * from arrays of speeches and text.
     *
     */
    private class SpeechListAdapter extends BaseAdapter {
        public SpeechListAdapter(Context context)
        {
            mContext = context;
        }

        
        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         * 
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return mQuestions.length();
        }

        /**
         * Since the data comes from an array, just returning
         * the index is sufficent to get at the data. If we
         * were using a more complex data structure, we
         * would return whatever object represents one 
         * row in the list.
         * 
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a SpeechView to hold each row.
         * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            SpeechView sv;
            int questionResourceId = mQuestions.getResourceId(position, 0);
            int answerResourceId = mAnswers.getResourceId(position, 0);
            if (convertView == null) {
                sv = new SpeechView(mContext, questionResourceId, answerResourceId, mExpanded[position]);
            } else {
                sv = (SpeechView)convertView;
                sv.setTitle(questionResourceId);
                sv.setDialogue(answerResourceId);
                sv.setExpanded(mExpanded[position]);
            }
            
            return sv;
        }

        public void toggle(int position) {
            mExpanded[position] = !mExpanded[position];
            notifyDataSetChanged();
        }
        
        /**
         * Remember our context so we can use it when constructing views.
         */
        private Context mContext;
        
        /**
         * Our data, part 1.
         */
        private TypedArray mQuestions = getResources().obtainTypedArray(R.array.help_questions);
        
        /**
         * Our data, part 2.
         */
        private TypedArray mAnswers = getResources().obtainTypedArray(R.array.help_answers);
        
        /**
         * Our data, part 3.
         */
        private boolean[] mExpanded = 
        {
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false   
        };
    }
    
    /**
     * We will use a SpeechView to display each speech. It's just a LinearLayout
     * with two text fields.
     *
     */
    private class SpeechView extends LinearLayout {
        public SpeechView(Context context, int resourceIdQuestion, int resourceIdAnswer, boolean expanded) {
            super(context);
            
            this.setOrientation(VERTICAL);
            
            // Here we build the child views in code. They could also have
            // been specified in an XML file.
            
            mTitle = new TextView(context);
            mTitle.setBackgroundResource(R.drawable.help_list_item_selector);
            mTitle.setPadding(10, 10, 10, 10);
            mTitle.setGravity(Gravity.CENTER_VERTICAL);
            mTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.icon_help), null, null, null);  
            mTitle.setCompoundDrawablePadding(4);
            mTitle.setTextAppearance(HelpActivity.this, R.style.TextMediumBold);
            mTitle.setText(getString(resourceIdQuestion));
            addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            
            mDialogue = new TextView(context);
            mDialogue.setBackgroundResource(R.drawable.help_list_item_selector);
            mDialogue.setPadding(20, 10, 10, 10);
            mDialogue.setTextAppearance(HelpActivity.this, R.style.TextMedium20sp);
            mDialogue.setText(getString(resourceIdAnswer));
            addView(mDialogue, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            
            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
        }
        
        /**
         * Convenience method to set the title of a SpeechView
         */
        public void setTitle(int questionResId) {
            mTitle.setText(getString(questionResId));
        }
        
        /**
         * Convenience method to set the dialogue of a SpeechView
         */
        public void setDialogue(int answerResId) {
            mDialogue.setText(getString(answerResId));
        }
        
        /**
         * Convenience method to expand or hide the dialogue
         */
        public void setExpanded(boolean expanded) {
            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
        }
        
        private TextView mTitle;
        private TextView mDialogue;
    }
}
