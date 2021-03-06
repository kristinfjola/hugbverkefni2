/**
 * @author Kristin Fjola Tomasdottir
 * @date 	12.03.15
 * @goal 	Various pop-up dialogs and toasts to display
 */
package com.corners.game.android;

import java.io.IOException;
import java.io.InputStream;

import screens.Play;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.corners.game.Dialogs;
import com.corners.game.MainActivity;


public class DialogsImpl implements Dialogs {
       Handler uiThread;
       Context appContext;
       AndroidLauncher launcher;
       ProgressDialog progress;
       Toast backToast;

       /**
        * Constructor, creates and instance of this class
        * @param appContext
        */
       public DialogsImpl(Context appContext) {
    	   uiThread = new Handler();
    	   this.appContext = appContext;
       }

      @Override
      public void showDirections(final String alertBoxTitle, final String alertBoxMessage, final Play playScreen) {
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
    	  
		  uiThread.post(new Runnable() {
    		  public void run() {
    			  AlertDialog dialog = new AlertDialog.Builder(getStyle())
	  			  	.setMessage(alertBoxMessage)
	  			  	.setNeutralButton("Got it!", onClickListener)
	  			  	.create();
	  			  dialog.setOnDismissListener(onDismissListener);
	  			  dialog.show();
    		  }
    	  });
      }
      
      @Override
      public void showBackToast(final CharSequence toastMessage) {
    	  uiThread.post(new Runnable() {
    		  public void run() {
    			  backToast = Toast.makeText(appContext, toastMessage, Toast.LENGTH_LONG);
    			  backToast.show();
    		  }
    	  });
      }
      
      @Override
      public void removeBackToast() {
    	  backToast.cancel();
      }
	
      @Override
      public void showProgressBar() {
    	  LayoutInflater inflater = (LayoutInflater) appContext.getApplicationContext()
    			  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	  inflater.inflate(R.layout.directions_layout, null);
    	  uiThread.post(new Runnable() {
    		  public void run() {
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
      
      /**
       * Displays the dialog that allows the user to change the characters name
       */
      public void showCharNameDialog(final String title, final MainActivity main, final Label label) {
    	  uiThread.post(new Runnable() {
    		  public void run() {
    			  AlertDialog.Builder dialog = new AlertDialog.Builder(getStyle());
    			  dialog.setTitle(title);
    			  
    			  LayoutInflater factory = LayoutInflater.from(appContext);
    			  final View view = factory.inflate(R.layout.name_input_layout, null);
 
    			  dialog.setView(view);
    			  dialog.setNeutralButton("Save", getNamePopupListener(view, main, label));    			  
    			  setTitleDividerLineColor(dialog.show());
    		  }
    	  });
      }
      
      /**
       * @param view
       * @param main
       * @param label
       * @return listener that gets the text in the views editText and saves it to
       * main.data and adds it to the label label
       */
      private DialogInterface.OnClickListener getNamePopupListener(final View view, final MainActivity main,
    		  final Label label) {
    	  DialogInterface.OnClickListener popupClickListener = new DialogInterface.OnClickListener() {		
    		  @Override
    		  public void onClick(DialogInterface dialog, int whichButton) {
    			  TextView text = (TextView) view.findViewById(R.id.nameInput);
    			  String newName = text.getText().toString();
    			  if(!newName.equals("") && !newName.equals(null)) {
    				  main.data.setName(newName);
        			  label.setText(newName);
    			  }
    		  }
    	  };
    	  return popupClickListener;
      }
      
      @Override
      public void showEndLevelDialog(final String title, final String starsImgDir[], final String charImgDir,
    		  final String[] messages) {
    	  uiThread.post(new Runnable() {
    		  public void run() {
    			  AlertDialog.Builder dialog = new AlertDialog.Builder(getStyle());
    			  dialog.setTitle(title);
    			  
    			  LayoutInflater factory = LayoutInflater.from(appContext);
    			  final View view = factory.inflate(R.layout.popup_layout, null);
    			  
    			  try {
    				  InputStream stream0 = appContext.getAssets().open(starsImgDir[0]);
    				  Drawable d0 = Drawable.createFromStream(stream0, null);
        			  ImageView image0 = (ImageView) view.findViewById(R.id.starImg0);
        			  image0.setImageDrawable(d0);
        			  
        			  InputStream stream1 = appContext.getAssets().open(starsImgDir[1]);
    				  Drawable d1 = Drawable.createFromStream(stream1, null);
        			  ImageView image1 = (ImageView) view.findViewById(R.id.starImg1);
        			  image1.setImageDrawable(d1);
        			  
        			  InputStream stream2 = appContext.getAssets().open(starsImgDir[2]);
    				  Drawable d2 = Drawable.createFromStream(stream2, null);
        			  ImageView image2 = (ImageView) view.findViewById(R.id.starImg2);
        			  image2.setImageDrawable(d2); 
    			  } catch (IOException e) {
    				  ((LinearLayout) view.findViewById(R.id.starTable)).getLayoutParams().height = 0;
    			  }
    			  
    			  try {
    				  InputStream stream = appContext.getAssets().open(charImgDir);
    				  Drawable d = Drawable.createFromStream(stream, null);
        			  ImageView image = (ImageView) view.findViewById(R.id.charImg);
        			  image.setImageDrawable(d);
    			  } catch (IOException e) {
    				  e.printStackTrace();
    			  }
    			  
    			  TextView text0 = (TextView) view.findViewById(R.id.message0);
    			  text0.setText(messages[0]);
    			  TextView text1 = (TextView) view.findViewById(R.id.message1);
    			  text1.setText(messages[1]);
    			  TextView text2 = (TextView) view.findViewById(R.id.message2);
    			  if(messages[2].equals("")) text2.getLayoutParams().height = 0;
    			  else text2.setText(messages[2]);

    			  dialog.setView(view);
    			  dialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {				
    					}
    			  	});
    			  
    			  setTitleDividerLineColor(dialog.show());
    		  }
    	  });
      }
      
      /**
       * @return theme for the apps dialogs
       */
      public ContextThemeWrapper getStyle() {
    	  return new ContextThemeWrapper(appContext, R.style.AlertDialogCustom);
      }
      
      /**
       * Changes the titleDividers color in the dialog dialog to the apps brown color
       * @param dialog
       */
      public void setTitleDividerLineColor(AlertDialog dialog) {
    	  int titleDividerId = appContext.getResources().getIdentifier("titleDivider", "id", "android");
		  View titleDivider = dialog.findViewById(titleDividerId);
		  if (titleDivider != null)
			  titleDivider.setBackgroundColor(appContext.getResources().getColor(R.color.dialog_line_color));  
      }

	@Override
	public void showNotConnectedToast() {
		uiThread.post(new Runnable() {
  		  public void run() {
  			  backToast = Toast.makeText(appContext, "You're not connected to the internet", Toast.LENGTH_LONG);
  			  backToast.show();
  		  }
  	  });
	}
}
