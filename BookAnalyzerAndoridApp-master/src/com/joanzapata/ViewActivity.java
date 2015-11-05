package com.joanzapata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.ListView;

import com.joanzapata.pdfview.sample.R;

public class ViewActivity extends Activity {
	private String url = "http://myapplications.net46.net/getData.php";
	ArrayList<Details> detailsList = new ArrayList<Details>();
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);
		listView = (ListView) findViewById(R.id.gridView);
		(new Upload()).execute("");

	}
	public void getDetails()
	{
		(new Upload()).execute("");
	}

	public class Upload extends AsyncTask<String, Void, ArrayList<Details>> {
		ProgressDialog dialog = new ProgressDialog(ViewActivity.this);

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			// TODO Auto-generated method stub
			dialog.setMessage("loading.........");
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected ArrayList<Details> doInBackground(String... params) {
			String resultResponse = submitTheDetails();
			detailsList.clear();
			try {

				JSONObject json = new JSONObject(resultResponse);
				JSONArray jsonArray = json.optJSONArray("ReadAnalyzer");
				for (int i = 0; i < jsonArray.length(); i++) {
					Details detail = new Details();
					JSONObject childeJson = jsonArray.getJSONObject(i);
					detail.setSno(childeJson.optString("sno"));
					detail.setQrimage(childeJson.optString("qrimage"));
					detail.setReader(childeJson.optString("reader"));
					detail.setWords(childeJson.optString("words"));
					detail.setAvgwords(childeJson.optString("avgwords"));
					detail.setSentenses(childeJson.optString("sentenses"));
					detail.setAvgsentenses(childeJson.optString("avgsentenses"));
					detail.setRi(childeJson.optString("ri"));
					detail.setBook(childeJson.optString("book"));
					detail.setPages(childeJson.optString("pages"));
					detail.setArticle(childeJson.optString("article"));
					detail.setSubject(childeJson.optString("subject"));
					detail.setTotalInfo(childeJson.optString("totalInfo"));
					
					detailsList.add(detail);

				}
			} catch (JSONException e) {
				// TODO: handle exception
			}

			return detailsList;
		}

		@Override
		protected void onPostExecute(ArrayList<Details> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dialog.cancel();
			QRcodeAdapter barcodeAdapter = new QRcodeAdapter(
					ViewActivity.this, result);
			listView.setAdapter(barcodeAdapter);

		}
	}

	protected String submitTheDetails() {
		String result = null;

		HttpParams httpParameters = new BasicHttpParams();
		// set timeout for requests

		try {

			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(url);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			InputStream inputStream = httpResponse.getEntity().getContent();
			if (inputStream != null) {
				try {
					result = convertInputStreamToString(inputStream);
					Log.v("Response :", result.toString());

					/*
					 * Toast.makeText(getApplicationContext(),
					 * "Uploaded Successfully", Toast.LENGTH_LONG).show();
					 */
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result.toString();

	}

	private String convertInputStreamToString(InputStream inputStream)
			throws IOException {

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		try {
			while ((line = bufferedReader.readLine()) != null)
				result += line;
			inputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

}
