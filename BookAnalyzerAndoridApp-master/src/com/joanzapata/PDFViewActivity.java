/**
 * Copyright 2014 Joan Zapata
 *
 * This file is part of Android-pdfview.
 *
 * Android-pdfview is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Android-pdfview is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android-pdfview.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.joanzapata;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.abbyy.ocrsdk.Contents;
import com.abbyy.ocrsdk.QRCodeEncoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.joanzapata.pdfview.sample.R;

public class PDFViewActivity extends Activity implements OnPageChangeListener {

	public static final String SAMPLE_FILE = "sample.pdf";

	public static final String ABOUT_FILE = "about.pdf";
	private static final int PICKFILE_REQUEST_CODE = 100;
	PDFView pdfView;
	int pageNumber = 1;
	private Button save;
	private Button choose;
	private Button view;
	private int noOfWords, noOfSentenses, noOfLetters;
	private String bitmapQrcode = "";
	private String filePathDetails;
	private TextView pageCountTx;
	private EditText etSubject, etArticle, etBookName,etReaderName;
	private int startPage;
	private boolean firstPage = true;
	private String totalInfo,totalContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pdfView = (PDFView) findViewById(R.id.pdfView);
		openPDF(filePathDetails);
		save = (Button) findViewById(R.id.save_button);
		view = (Button) findViewById(R.id.view_button);
		pageCountTx = (TextView) findViewById(R.id.page_count);
		etSubject = (EditText) findViewById(R.id.et_subject_area);
		etArticle = (EditText) findViewById(R.id.et_article);
		etReaderName= (EditText) findViewById(R.id.et_reader_name);
		etBookName = (EditText) findViewById(R.id.et_book_name);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PDFViewActivity.this,
						ViewActivity.class);
				startActivity(intent);
			}
		});
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// (new
				// AsyncProcessTask(getBitmapFromView(pdfView),PDFViewActivity.this)).execute();
				(new Upload()).execute("");
			}

		});
		choose = (Button) findViewById(R.id.choose_button);
		choose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent1 = new Intent(PDFViewActivity.this,
						FileChooser.class);
				startActivityForResult(intent1, PICKFILE_REQUEST_CODE);

			}
		});

	}

	private void openPDF(String filePathDetails) {
		if (filePathDetails != null)
			pdfView.fromFile(new File(filePathDetails)).defaultPage(pageNumber)
					.onPageChange(this).load();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK) {
			String filePath = data.getStringExtra("GetPath");
			String fileName = data.getStringExtra("GetFileName");
			filePathDetails = filePath + "/" + fileName;
			openPDF(filePathDetails);
		}

	}

	@Override
	public void onPageChanged(int page, int pageCount) {
		// TODO Auto-generated method stub
		if (firstPage) {
			firstPage = false;
			startPage = page;
		}

		pageNumber = page;
		pageCountTx.setText(pageNumber + "");
	}

	public static int wordCount(String s) {
		if (s == null)
			return 0;
		return s.trim().split("\\s+").length;
	}

	public byte[] getBitmapFromView(View view) {
		// Define a bitmap with the same size as the view
		Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(),
				view.getHeight(), Bitmap.Config.ARGB_8888);
		// Bind a canvas to it
		Canvas canvas = new Canvas(returnedBitmap);
		// Get the view's background
		Drawable bgDrawable = view.getBackground();
		if (bgDrawable != null)
			// has background drawable, then draw it on the canvas
			bgDrawable.draw(canvas);
		else
			// does not have background drawable, then draw white background on
			// the canvas
			canvas.drawColor(Color.WHITE);
		// draw the view on the canvas
		view.draw(canvas);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		returnedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;

	}

	public class Upload extends AsyncTask<String, Void, String> {
		ProgressDialog dialog = new ProgressDialog(PDFViewActivity.this);

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
		protected String doInBackground(String... params) {
			try {
				parsePdf(filePathDetails, "hi");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return submitTheDetails();
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dialog.cancel();
			etArticle.setText("");
			etSubject.setText("");
			etBookName.setText("");

			try {
				JSONObject jsonObject = new JSONObject(result);
				Toast.makeText(getApplicationContext(),
						jsonObject.getString("message"), Toast.LENGTH_LONG)
						.show();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected String submitTheDetails() {
		// TODO Auto-generated method stub
		// getBitmapFromView(pdfView);
		String result = null;
		String url = "http://myapplications.net46.net/setData.php";
		JSONObject jsonObject = new JSONObject();
		List<NameValuePair> paraList = new ArrayList<NameValuePair>();
		paraList.add(new BasicNameValuePair("qrimage", bitmapQrcode));
		paraList.add(new BasicNameValuePair("subject", etSubject.getText()
				.toString()));
		paraList.add(new BasicNameValuePair("reader", etReaderName.getText()
				.toString()));
		paraList.add(new BasicNameValuePair("article", etArticle.getText()
				.toString()));
		paraList.add(new BasicNameValuePair("book", etBookName.getText()
				.toString()));

		paraList.add(new BasicNameValuePair("words", wordCount(totalContent)+""));
		paraList.add(new BasicNameValuePair("avgwords", averageWords(totalContent)+""));
		paraList.add(new BasicNameValuePair("sentenses", sentenseCount(totalContent)+""));
		paraList.add(new BasicNameValuePair("avgsentenses", averageSentense(totalContent)+""));
		
		paraList.add(new BasicNameValuePair("pages", startPage + " to "
				+ pageNumber));
		paraList.add(new BasicNameValuePair("ri", getReadability(totalContent)));
		paraList.add(new BasicNameValuePair("totalInfo", totalInfo));
		Log.v("Result..", paraList.toString());
		HttpParams httpParameters = new BasicHttpParams();
		// set timeout for requests

		try {

			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(paraList));
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

	public void displayMessage(String message) {
		bitmapQrcode = message;
		convertQrCode(message);
	}

	public void convertQrCode(String _message) {
		// TODO Auto-generated method stub
		// Find screen size
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		int width = point.x;
		int height = point.y;
		int smallerDimension = width < height ? width : height;
		smallerDimension = smallerDimension * 3 / 4;

		// Encode with a QR Code image
		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(_message, null,
				Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
				smallerDimension);
		try {
			Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100,
					byteArrayOutputStream);
			byte[] byteArray = byteArrayOutputStream.toByteArray();
			bitmapQrcode = Base64.encodeToString(byteArray, Base64.DEFAULT);

		} catch (WriterException e) {
			e.printStackTrace();
		}

	}

	public void parsePdf(String pdf2, String txt) throws IOException {
		// String
		// pdf1=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+
		// File.separator + "about.pdf";
		PdfReader reader = new PdfReader(pdf2);
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);

		File file = getFileStreamPath("test.txt");

		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream writer = openFileOutput(file.getName(),
				Context.MODE_PRIVATE);

		// PrintWriter out = new PrintWriter(new FileOutputStream(txt));
		TextExtractionStrategy strategy;
		for (int i = 1; i <= pageNumber; i++) {
			strategy = parser.processContent(i,
					new SimpleTextExtractionStrategy());
			writer.write(strategy.getResultantText().getBytes());
			writer.flush();
		}
		writer.close();
		reader.close();
		String totalString = readFromFile();
		System.out.println(totalString);
		totalContent=totalString;
		String word = "Number of words  " + wordCount(totalString) + "\n";
		String averageWords = "Average length of words  " + " "
				+ averageWords(totalString) + "\n";
		String sentenseCount = "Number of sentences  "
				+ sentenseCount(totalString) + "\n";
		String averageSentenses = "Average length of sentences  "
				+ averageSentense(totalString) + "\n";
		// String complexity=
		String readability = " Readability Index  " + getReadability(totalString)
				+ "\n";

		String subject = "Subject Area  " + etSubject.getText().toString()
				+ "\n";
		String article = "Type of the Article : "
				+ etArticle.getText().toString() + "\n";
		String pages = " Pages  " + startPage + " to " + pageNumber + "\n";
		String book = "book name  " + etBookName.getText().toString() + "\n";
		String readername = "reader name  " + etReaderName.getText().toString() + "\n";
		totalInfo = readername+book+subject+"\n"+article+ pages+word + averageWords + sentenseCount + averageSentenses
				+ readability ;
		displayMessage(totalInfo);

	}

	private String getReadability(String totalString) {
		// TODO Auto-generated method stub
		double readabilityScore = (4.71 * (getCharacter(totalString) / wordCount(totalString)))
				+ (0.5 * (wordCount(totalString) / sentenseCount(totalString)))
				- 21.43;
		return new DecimalFormat("##.##").format(readabilityScore)+ "";
	}

	private int getCharacter(String totalString) {

		String[] word = totalString.split(" ");

		Integer characters = 0;
		for (String w : word) {
			characters += w.length();
		}
		return characters;
	}

	private int averageWords(String str) {
		String words[] = str.split(" ");
		int numWords = words.length;

		return getCharacter(str) / numWords;
	}

	private int sentenseCount(String myFile) {
		int sentenceCount = 0;
		String SENTENCE_DELIMETERS = ".,::?!";
		for (int i = 0; i < myFile.length() - 1; i++) {
			for (int j = 0; j < SENTENCE_DELIMETERS.length(); j++) {
				if (myFile.charAt(i) == SENTENCE_DELIMETERS.charAt(j)) {
					if (myFile.charAt(i + 1) == SENTENCE_DELIMETERS.charAt(j)) {
						sentenceCount--;
					}
					sentenceCount++;
				}
			}
		}
		return sentenceCount;
	}

	private int averageSentense(String str) {
		int total = 0;
		try {

			int averageWords = str.split(" ").length;
			int sentensecount = sentenseCount(str);

			total = averageWords / sentensecount;

		} catch (ArithmeticException e) {
			// TODO: handle exception
		}
		return total;
	}

	private String readFromFile() {

		String ret = "";

		try {
			InputStream inputStream = openFileInput("test.txt");

			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ((receiveString = bufferedReader.readLine()) != null) {
					stringBuilder.append(receiveString);
				}

				inputStream.close();
				ret = stringBuilder.toString();
			}
		} catch (FileNotFoundException e) {
			Log.e("", "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e("", "Can not read file: " + e.toString());
		}

		return ret;
	}
}
