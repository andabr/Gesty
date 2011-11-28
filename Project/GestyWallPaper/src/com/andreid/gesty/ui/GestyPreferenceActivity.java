package com.andreid.gesty.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreid.gesty.R;
import com.andreid.gesty.data.IntentsData;

public class GestyPreferenceActivity extends Activity {
	private final int REQ_CODE_PICK_IMAGE = 123;
	 private SharedPreferences mSettings;

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mSettings = getSharedPreferences(IntentsData.PREFS_NAME, 0);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
	        
        
        TextView text = new TextView(this);
        text.setText(R.string.preference_button_description);
        layout.addView(text);
        Button chooseBtn = new Button(this);
        chooseBtn.setText(R.string.preference_button_text);
        layout.addView(chooseBtn);
	        
        chooseBtn.setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE); 
			}
		});
        
        
        
        setContentView(layout);
    }

    
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

        switch(requestCode) { 
        case REQ_CODE_PICK_IMAGE:
            if (resultCode == RESULT_OK){  
                Uri selectedImage = imageReturnedIntent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                Editor editor = mSettings.edit();
                editor.putString("backgroundpicture", filePath);
                editor.commit();
                
//                Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
            }
        }
    }
      
    
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {}

}