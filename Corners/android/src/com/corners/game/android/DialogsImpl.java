/**
 * @author Kristin Fjola Tomasdottir
 * @date 	12.03.15
 * @goal 	Various native Android features
 */
package com.corners.game.android;

import java.io.IOException;
import java.io.InputStream;

import logic.Category;
import screens.Play;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.corners.game.Dialogs;
import com.corners.game.MainActivity;


public class DialogsImpl implements Dialogs {
       Handler uiThread;
       Context appContext;
       AndroidLauncher launcher;
       ProgressDialog progress;

      public DialogsImpl(Context appContext) {
               uiThread = new Handler();
               this.appContext = appContext;
       }

      @Override
      public void showDirections(final String alertBoxTitle, final String alertBoxMessage, 
    		  final String alertBoxButtonText, final Play playScreen) {
		  // onClick
    	  final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {		
    		  @Override
    		  public void onClick(DialogInterface dialog, int whichButton) {
    			  playScreen.resume();
			  }
		  };
		  
		  // onDismiss
		  final DialogInterface.OnDismissListener onDismissListener = new DialogInterface.OnDismissListener() {			
			  @Override
			  public void onDismiss(DialogInterface dialog) {
				  playScreen.resume();
			  }
		  };
    	  
		  LayoutInflater inflater = (LayoutInflater) appContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  final View dialogLayout = inflater.inflate(R.layout.directions_layout, null);
    	  uiThread.post(new Runnable() {
    		  public void run() {
    			  AlertDialog dialog = new AlertDialog.Builder(appContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
    			  	.setIcon(R.drawable.temp)
                    //.setTitle(alertBoxTitle)
    			  	.setMessage(alertBoxMessage)
    			  	.setNeutralButton(alertBoxButtonText, onClickListener)
    			  	//.setView(dialogLayout)
    			  	.create();
    			  dialog.setOnDismissListener(onDismissListener);
    			  dialog.show();
    		  }
    	  });
      }
      
      @Override
      public void showToast(final CharSequence toastMessage, int toastDuration) {
    	  uiThread.post(new Runnable() {
    		  public void run() {
    			  Toast.makeText(appContext, toastMessage, Toast.LENGTH_LONG).show();
    		  }
    	  });
      }
	
      @Override
      public void showProgressBar() {
    	  LayoutInflater inflater = (LayoutInflater) appContext.getApplicationContext()
    			  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	  inflater.inflate(R.layout.directions_layout, null);
    	  uiThread.post(new Runnable() {
    		  public void run() {
    			  //show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, DialogInterface.OnCancelListener cancelListener)
    			  progress = new ProgressDialog(appContext);
    			  progress.setMessage("Loading friends... ");
    			  progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    			  progress.setIndeterminate(true);
    			  progress.show();
    		  }
    	  });
      }
	
      @Override
      public void hideProgressBar() {
    	  uiThread.post(new Runnable() {
    		  public void run() {
    			  progress.dismiss();
    		  }
    	  });
      }
      
      @Override
      public void showEndLevelDialog(final String title, final String starsImgDir, final String charImgDir,
    		  final String message, final MainActivity main, final Category cat) {
    	  final DialogInterface.OnClickListener popupClickListener = new DialogInterface.OnClickListener() {		
    		  @Override
    		  public void onClick(DialogInterface dialog, int whichButton) {
    			  //main.setScreen(new Levels(main, cat));
    		  }
    	  };
    	  
    	  //LayoutInflater inflater = (LayoutInflater) appContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  //final View dialogLayout = inflater.inflate(R.layout.directions_layout, null);
    	  uiThread.post(new Runnable() {
    		  public void run() {
    			  AlertDialog.Builder dialog = new AlertDialog.Builder(appContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
    			  dialog.setTitle(title);
    			  
    			  LayoutInflater factory = LayoutInflater.from(appContext);
    			  final View view = factory.inflate(R.layout.popup_layout, null);
    			  
    			  if(!starsImgDir.equals("")) {
	    			  try {
	    				  InputStream stream = appContext.getAssets().open(starsImgDir);
	    				  Drawable d = Drawable.createFromStream(stream, null);
	        			  ImageView image = (ImageView) view.findViewById(R.id.starsImg);
	        			  image.setImageDrawable(d);
	    			  } catch (IOException e) {
	    				  e.printStackTrace();
	    			  }
    			  }
    			  
    			  try {
    				  InputStream stream = appContext.getAssets().open(charImgDir);
    				  Drawable d = Drawable.createFromStream(stream, null);
        			  ImageView image = (ImageView) view.findViewById(R.id.charImg);
        			  image.setImageDrawable(d);
    			  } catch (IOException e) {
    				  e.printStackTrace();
    			  }
    			  
    			  TextView text = (TextView) view.findViewById(R.id.msgFromChar);
    			  text.setText(message);

    			  dialog.setView(view);
    			  dialog.setNeutralButton("Ok", popupClickListener);
    			  
    			  //dialog.setOnDismissListener(onDismissListener);
    			  dialog.show();
    		  }
    	  });
      }
}
