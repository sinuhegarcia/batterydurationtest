package com.example.webserviceimagen;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	Button btcamara;
	Button btgaleria;
	TextView tv1;
	private String foto;
	private static int TAKE_PICTURE=1;
	private static int TAKE_PICTURE_FROM_GALERY=2;
	double aleatorio=0;
	boolean repetir = false;
	
	private static String URL_SERVICE="http://sinugama.meximas.com/upload/upload_image.php";
	//private static String URL_SERVICE="http://132.248.51.251:8081/upload/upload_image.php";
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tv1 = (TextView) findViewById(R.id.textView1);
        
        aleatorio = (new Double(Math.random()*100)).intValue();
        foto = Environment.getExternalStorageDirectory() + "/imagen" + aleatorio + ".jpg";
        
        btcamara = (Button) findViewById(R.id.button1);
        btcamara.setOnClickListener(
        		new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
						if(!repetir){
							repetir=true;
							btcamara.setText("Parar");
							subirFotoCiclo();							
						}else{
							repetir=false;
							btcamara.setText("Repetir");
						}
						
						
						//Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				        //Uri output = Uri.fromFile(new File(foto));
				        //intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
				        //startActivityForResult(intent, TAKE_PICTURE);
				       
					}
        			
        		}
        		
        );
        
        btgaleria = (Button) findViewById(R.id.button2);
        btgaleria.setOnClickListener(
        		new OnClickListener(){
        			

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(i, TAKE_PICTURE_FROM_GALERY);
					}
        			
        		}
        		
        );
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	
    	if (requestCode == TAKE_PICTURE_FROM_GALERY && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
    
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
    
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            foto = picturePath;
            cursor.close();
                         
            ImageView imageView = (ImageView) findViewById(R.id.imageView1);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            
            File file = new File(foto);
        	if (file.exists()) {
        		subirFoto(foto);       	
        	}
        	
        	//Toast.makeText(getApplicationContext(), "Archivo = "+foto, Toast.LENGTH_LONG).show();
            // String picturePath contains the path of selected Image
        }else if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK && null != data){        	
        	ImageView iv = (ImageView) findViewById(R.id.imageView1);
        	iv.setImageBitmap(BitmapFactory.decodeFile(foto));
        	
        	File file = new File(foto);
        	if (file.exists()) {
        		subirFoto(foto);       	
        	}
        	
        	
        	//Toast.makeText(getApplicationContext(), "Archivo = "+foto, Toast.LENGTH_LONG).show();
        }
    	else
            Toast.makeText(getApplicationContext(), "No se ha realizado la foto", Toast.LENGTH_SHORT).show();
    }
    
    public void subirFoto(String fotoPath){
    	uploaderFoto(fotoPath);	
    }
    
    public void subirFotoCiclo(){
    	Runnable runnable = new Runnable(){
    		String responseMessage="";
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(repetir){
					File file = new File(foto);
					tv1.setText(foto);
		        	if (file.exists()) {
		        		try{
		    				HttpClient httpclient = new DefaultHttpClient();
		                    httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		                    HttpPost httppost = new HttpPost(URL_SERVICE);
		                    MultipartEntity mpEntity = new MultipartEntity();
		                    ContentBody foto = new FileBody(file, "image/jpeg");
		                    mpEntity.addPart("fotoUp", foto);
		                    mpEntity.addPart("date", new StringBody(getDate()));
		                    mpEntity.addPart("level", new StringBody(getBatteryLevelString()));
		                    
		                    httppost.setEntity(mpEntity);
		                    HttpResponse response = httpclient.execute(httppost);
		                    httpclient.getConnectionManager().shutdown();
		                    
		                    InputStream inputStream = response.getEntity().getContent();
		    				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

		                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		                    StringBuilder stringBuilder = new StringBuilder();

		                    String bufferedStrChunk = null;
		                    
		                    

		                    while((bufferedStrChunk = bufferedReader.readLine()) != null){
		                        stringBuilder.append(bufferedStrChunk);
		                    }
		                    
		                    responseMessage = new String(stringBuilder.toString());
		                    
		                    tv1.setText(responseMessage);
		    			}catch(Exception e){
		    				e.printStackTrace();
		    			}       	
		        	}
		        	
		        	try{
		        		Thread.sleep(60000);
		        	}catch(InterruptedException e){
		        		
		        	}
				}
			}
    		
    	};
    	
    	(new Thread(runnable)).start();
    	
    }
    
    public void uploaderFoto(String fotoPath){
    	UploaderFoto nuevaTarea = new UploaderFoto();
        nuevaTarea.execute(fotoPath);
    }
    
    public String getDate(){
    	Calendar c = Calendar.getInstance();
    	SimpleDateFormat formatDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    	String fechaFormateada = formatDate.format(c.getTime());
    	Toast.makeText(getApplicationContext(), fechaFormateada, Toast.LENGTH_LONG).show();
    	return fechaFormateada;
    }
    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f; 
    }
    public String getBatteryLevelString(){
    	String batteryLevel = Float.toString(getBatteryLevel());
    	return batteryLevel;
    }
    
    class UploaderFoto extends AsyncTask<String,Void,Void>{
    	ProgressDialog pDialog;
    	String miFoto="";
    	String responseMessage="";
    	
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			miFoto=params[0];
			try{
				HttpClient httpclient = new DefaultHttpClient();
                httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpPost httppost = new HttpPost(URL_SERVICE);
                File file = new File(miFoto);
                MultipartEntity mpEntity = new MultipartEntity();
                ContentBody foto = new FileBody(file, "image/jpeg");
                mpEntity.addPart("fotoUp", foto);
                mpEntity.addPart("date", new StringBody(getDate()));
                mpEntity.addPart("level", new StringBody(getBatteryLevelString()));
                
                httppost.setEntity(mpEntity);
                HttpResponse response = httpclient.execute(httppost);
                httpclient.getConnectionManager().shutdown();
                
                InputStream inputStream = response.getEntity().getContent();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();

                String bufferedStrChunk = null;
                
                

                while((bufferedStrChunk = bufferedReader.readLine()) != null){
                    stringBuilder.append(bufferedStrChunk);
                }
                
                responseMessage = new String(stringBuilder.toString());
                
                
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPreExecute(){
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Subiendo la imagen, espere." );
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(true);
            pDialog.show();
		}
		
		protected void onPostExecute(Void result){
			super.onPostExecute(result);
			Toast.makeText(getApplicationContext(), responseMessage, Toast.LENGTH_LONG).show();
			tv1.setText(responseMessage);
            pDialog.dismiss();
		}
		
		public String getDate(){
	    	Calendar c = Calendar.getInstance();
	    	SimpleDateFormat formatDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
	    	String fechaFormateada = formatDate.format(c.getTime());
	    	return fechaFormateada;
	    }
	    public float getBatteryLevel() {
	        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

	        // Error checking that probably isn't needed but I added just in case.
	        if(level == -1 || scale == -1) {
	            return 50.0f;
	        }

	        return ((float)level / (float)scale) * 100.0f; 
	    }
	    
	    public String getBatteryLevelString(){
	    	String batteryLevel = Float.toString(getBatteryLevel());
	    	return batteryLevel;
	    }
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
}
