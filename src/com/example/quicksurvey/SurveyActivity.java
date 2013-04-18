package com.example.quicksurvey;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SurveyActivity extends Activity {
	
	LinearLayout lLayout;
	Button submitButton;
	String questionToken;
	String surveyToken;
	
	String urlType;
	static final String ASK = "ask";
	static final String ANSWER = "answer";
	
	static final String EXTRA_TOKEN = "token";

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();

		//setContentView(R.layout.activity_survey);
		//lLayout = (LinearLayout) this.findViewById(R.id.linear_layout);
        
		lLayout = new LinearLayout(this);
		lLayout.setOrientation(LinearLayout.VERTICAL);

		submitButton = new Button(this);
		
		surveyToken = getIntent().getStringExtra(EXTRA_TOKEN);
		urlType = ASK;
		new GetJSONFromUrl().execute("http://quicksurvey.herokuapp.com/api/surveys/"+surveyToken+"/ask.json");
		
		setContentView(lLayout);
		
	}
	
	public void setupQuestion(JSONObject json) {
		Log.i("QuickSurvey app", "in setupQuestion()");
		//TextView titleView = (TextView) this.findViewById(R.id.survey_title);
		TextView titleView = new TextView(this);
		try {
			JSONObject data = json.getJSONObject("data");
			titleView.setText(data.getString("title"));
			lLayout.addView(titleView);
			
			JSONArray questions = data.getJSONArray("questions");
			
			JSONObject question = questions.getJSONObject(0);
			questionToken = question.getString("token");
			TextView questionView = new TextView(this);
			questionView.setText(question.getString("title"));
			lLayout.addView(questionView);
			
			if (question.getString("kind").equals("multiple-choice")) {
				
				final RadioGroup rg = new RadioGroup(this);
				rg.setOrientation(RadioGroup.VERTICAL);
				
				JSONArray options = question.getJSONArray("options");
				for(int j = 0; j < options.length(); j++) {
					JSONObject option = options.getJSONObject(j);
					RadioButton rb = new RadioButton(this);
					rb.setText(option.getString("content"));
					rb.setTag(option.getString("token"));
					rb.setId(j);
					rg.addView(rb);
				}
				lLayout.addView(rg);
				submitButton.setOnClickListener(new View.OnClickListener() {
				    @Override
				    public void onClick(View v) {
				    	int optionID = rg.getCheckedRadioButtonId();
				    	if (optionID == -1) {
				    		Toast.makeText(SurveyActivity.this, "You haven't selected a response", Toast.LENGTH_SHORT).show();
				    	}
				    	else {
					        RadioButton response = (RadioButton) findViewById(optionID);
					        sendResponse(questionToken, (String) response.getTag());
				    	}
				    }
				});
			}
			else if (question.getString("kind").equals("fill-in")) {
				final EditText textResponse = new EditText(this);
				lLayout.addView(textResponse);
				submitButton.setOnClickListener(new View.OnClickListener() {
				    @Override
				    public void onClick(View v) {	
				    	sendResponse(questionToken, textResponse.getText().toString());
				    }
				});
			}
			submitButton.setText("Submit");
			lLayout.addView(submitButton);

		}
		catch (Exception e) {
		    e.printStackTrace();
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Error fetching survey");
		    builder.setCancelable(false);
		    builder.setPositiveButton("Retry",
		    	new DialogInterface.OnClickListener() {
		    		public void onClick(DialogInterface dialog, int id) {
		    			new GetJSONFromUrl().execute("http://quicksurvey.herokuapp.com/api/surveys/"+surveyToken+"/ask.json");
		    		}
		    	}
		    );
		    builder.setNegativeButton("Return to main menu",
		    	new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int id) {
			    		finish();
			    	}
			 	}
			);
			builder.show();
		}
		
	}
	
	protected void sendResponse(String questionToken, String response) {
		Log.i("QuickSurvey app", "Question token: "+questionToken+" response: "+response);
		urlType = ANSWER;
		try {
			new GetJSONFromUrl().execute("http://quicksurvey.herokuapp.com/api/questions/"+questionToken+"/answer.json?value="+URLEncoder.encode(response, "UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}	
	}
	
	public void confirmSubmission(JSONObject json) {
		Log.i("QuickSurvey app", "in confirmSubmission()");
		try {
			String status = json.getString("status");
			String message = json.getString("message");
			Log.i("QuickSurvey app", "status: "+status+" message: "+message);
			
			if (status.compareTo("success") == 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(message)
			    		.setCancelable(false)
			    		.setPositiveButton("Return to main menu", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int id) {
			                finish();
			           }
			       });
				builder.show();
			}
			else {
				Toast.makeText(this, "Submission failed", Toast.LENGTH_SHORT).show();
			}
		}
		catch (JSONException e) {
		    e.printStackTrace();
		    Toast.makeText(this, "Submission failed", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.survey, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class GetJSONFromUrl extends AsyncTask<String, Void, JSONObject> {
	    ProgressDialog progressDialog;

	    @Override
	    protected void onPreExecute() {
	        progressDialog = ProgressDialog.show(SurveyActivity.this, "", "Loading...");
	        super.onPreExecute();
	    }
	    
	    protected JSONObject doInBackground(String... urls) {
	    	Log.i("QuickSurvey app", "in doInBackground()");
	    	JSONObject jObj = null;
	        try {
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            Log.i("QuickSurvey app", "1");
	            HttpPost httpPost = new HttpPost(urls[0]);
	            Log.i("QuickSurvey app", "2");
	            HttpResponse httpResponse = httpClient.execute(httpPost);
	            Log.i("QuickSurvey app", "3");
	            HttpEntity httpEntity = httpResponse.getEntity();
	            String json = EntityUtils.toString(httpEntity);
	            jObj = new JSONObject(json);
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (ParseException e) {
	        	Log.e("Buffer Error", "Error converting result " + e.toString());
	        } catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
	        }
	        return jObj;
			
	    }

	    protected void onPostExecute(JSONObject jObj) {
	    	Log.i("QuickSurvey app", "in onPostExecute()");
	    	super.onPostExecute(jObj);
	    	
	    	if (progressDialog.isShowing()) {
	    		progressDialog.dismiss();
	    	}
	        
	        if (urlType.compareTo(ASK) == 0) {
        		setupQuestion(jObj);
        	}
        	else if (urlType.compareTo(ANSWER) == 0) {
        		confirmSubmission(jObj);
        	}
       
	        
	    }
	}

}
