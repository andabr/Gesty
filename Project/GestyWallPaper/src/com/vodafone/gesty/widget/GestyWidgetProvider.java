package com.vodafone.gesty.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.vodafone.gesty.R;
import com.vodafone.gesty.ui.GestureDetectorActivity;

public class GestyWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		if (appWidgetIds == null) {
			return;
		}
		int len = appWidgetIds.length;
		for (int i = 0; i < len; i++) {
			int widgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        intent.setClass(context, GestureDetectorActivity.class);
	        
	        PendingIntent pendingIntent =  PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			views.setOnClickPendingIntent(R.id.GestyButton, pendingIntent);

			appWidgetManager.updateAppWidget(widgetId, views);
		}
	}
}
