package com.example.gismeteo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class SplashScreen extends Activity {
	
	private ProgressBar progress;
	private TextView noty;
    private String region = new String();
    private LoadTask lt;
    private ArrayList<Weather> forecast = new ArrayList<Weather>();
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
		if (getIntent().getBooleanExtra("EXIT", false)) {
			finish();
		}
		noty = (TextView) findViewById(R.id.noty);
		noty.setText(this.getString(R.string.pd_message);
		progress = (ProgressBar) findViewById(R.id.progress);

    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        region = data.getStringExtra("region");
        showForecast();
    }
    @Override
    protected void onResume() {
        super.onResume();
        showForecast();
    }

	private void showForecast(){
	    lt = new LoadTask(this, region);
        lt.execute();
	}
	
	protected void gpsAlertBox(String mymessage) {
        final Context context = this;
        AlertDialog.Builder ad;
        ad = new AlertDialog.Builder(this);	
        ad.setMessage(mymessage);
        ad.setPositiveButton(this.getString(R.string.GPS_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        ad.setNegativeButton(this.getString(R.string.listreg_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
				startActivityForResult(new Intent(((Dialog) dialog).getContext(),RegionList.class),1);
				return;
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                finish();
                return;
            }
        });
        ad.show();
    }
	
	public void alert(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(this.getString(R.string.close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    class LoadTask extends AsyncTask<Void, String, ArrayList<Weather>> {
        private Context thisContext;
		private String region;
        private ProgressDialog progressDialog;
        private XmlParse gismeteo;
		private GetLocation gl;
        
		public LoadTask(Context context, String region) {
            thisContext = context;
			this.region = region;
            gl = new GetLocation(thisContext);
        }
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
		
        @Override
        protected ArrayList<Weather> doInBackground(Void... params) {
            try {
				
                if(region.length() == 0){
					while(gl.getNoLocation()){}
					// synchronized ("getLoc") {
					// try {
						// "getLoc".wait();
					// } catch (InterruptedException e) {e.printStackTrace();}
					gl.checkRegion();
					region = gl.getRegion();
					if (region == null) {
						return null;
					}
				}
				publishProgress(thisContext.getString(R.string.pd_forecast));
                gismeteo = new XmlParse(thisContext, region);
                return gismeteo.getForecast();
            } catch (IOException e) {
                progressDialog.dismiss();
                alert(thisContext.getString(R.string.error));
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                progressDialog.dismiss();
                alert(thisContext.getString(R.string.error));
                e.printStackTrace();
            }
            return null;
        }
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			noty.setText(values[0]);
		}
		
        @Override
        protected void onPostExecute(ArrayList<Weather> result) {
            super.onPostExecute(result);
            forecast = result;
			if(region == null) {
				gpsAlertBox(thisContext.getString(R.string.GPS_error));
			} else if(forecast == null) {
				alert(thisContext.getString(R.string.error));
			} else {
				Intent intent = new Intent(thisContext,MainActivity.class);
				intent.putExtra("forecast",forecast);
				startActivity(intent);
                finish();
			}

        }
    }
}