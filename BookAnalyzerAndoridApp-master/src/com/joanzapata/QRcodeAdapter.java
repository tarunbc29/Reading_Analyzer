package com.joanzapata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.pdfview.sample.R;

public class QRcodeAdapter extends BaseAdapter {

	public ArrayList<Details> listItems = new ArrayList<Details>();
	LayoutInflater vi;
	private Context mContext;

	public QRcodeAdapter(Context context, ArrayList<Details> detailInfoList) {
		mContext = context;
		listItems = detailInfoList;
		vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			convertView = vi.inflate(R.layout.item_barcode_view, null);
			holder = new ViewHolder();
			convertView.setId(position);
			holder.imageView = (ImageView) convertView.findViewById(R.id.barcode_image);
			holder.bookName = (TextView) convertView.findViewById(R.id.subject_name);
			holder.pageNumber = (TextView) convertView.findViewById(R.id.page_number);
			holder.delete = (Button) convertView.findViewById(R.id.delete_button);
			holder.getInfo = (Button) convertView.findViewById(R.id.getInfo_button);
			holder.delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					(new DeleteQr()).execute(
							"http://myapplications.net46.net/delete.php?sno=" + listItems.get(position).getSno());

				}
			});

			holder.getInfo.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					createDoilogForGetInfo((Details) getItem(position));

				}
			});
			holder.imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					createDoilog(convertStringToImageView(((Details) getItem(position)).getQrimage()));
				}
			});

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (listItems.get(position).getQrimage() != null) {
			holder.imageView.setImageBitmap(convertStringToImageView(listItems.get(position).getQrimage()));
			holder.bookName.setText(listItems.get(position).getBook());
			holder.pageNumber.setText(" Pages " + listItems.get(position).getPages());
		}
		return convertView;
	}

	public static class ViewHolder {
		ImageView imageView;
		TextView bookName;
		TextView pageNumber;
		Button delete;
		Button getInfo;

	}

	public Bitmap convertStringToImageView(String strBase64) {
		byte[] decodedString = Base64.decode(strBase64, Base64.DEFAULT);
		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		return decodedByte;
	}

	protected void createDoilog(Bitmap bitmap) {
		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialog.setContentView(R.layout.doilog_custom);
		ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView1);

		imageView.setImageBitmap(bitmap);

		dialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		dialog.show();

	}

	protected void createDoilogForGetInfo(Details details) {
		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialog.setContentView(R.layout.doilog_get_info);
		TextView book = (TextView) dialog.findViewById(R.id.book_name);
		TextView subject = (TextView) dialog.findViewById(R.id.subject_name);
		TextView article = (TextView) dialog.findViewById(R.id.article_name);
		TextView words = (TextView) dialog.findViewById(R.id.words_name);
		TextView avgWords = (TextView) dialog.findViewById(R.id.avg_words_name);
		TextView sentense = (TextView) dialog.findViewById(R.id.sentenses_name);
		TextView avgSentense = (TextView) dialog.findViewById(R.id.avg_senteses_name);
		TextView ri = (TextView) dialog.findViewById(R.id.ri_name);
		TextView pages = (TextView) dialog.findViewById(R.id.pages_name);
		TextView reader = (TextView) dialog.findViewById(R.id.reader_name);
		reader.setText(details.getReader());
		book.setText(details.getBook());
		subject.setText(details.getSubject());
		article.setText(details.getArticle());
		words.setText(details.getWords());
		avgWords.setText(details.getAvgwords());
		sentense.setText(details.getSentenses());
		avgSentense.setText(details.getAvgsentenses());
		ri.setText(details.getRi());
		pages.setText(details.getPages());

		words.setText(details.getWords());
		avgWords.setText(details.getAvgwords());
		sentense.setText(details.getSentenses());
		avgSentense.setText(details.getAvgsentenses());
		ri.setText(details.getRi());
		pages.setText(details.getPages());

		dialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		dialog.show();

	}

	public class DeleteQr extends AsyncTask<String, Void, Void> {
		ProgressDialog dialog = new ProgressDialog(mContext);

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
		protected Void doInBackground(String... params) {
			String resultResponse = Delete(params[0]);
			Log.v("Response :", resultResponse.toString());
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dialog.cancel();
			((ViewActivity) mContext).getDetails();
		}
	}

	public String Delete(String url) {
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

	private String convertInputStreamToString(InputStream inputStream) throws IOException {

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
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
