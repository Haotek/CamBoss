package appteam;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpGetTool extends AsyncTask<String, Void, String> 
{
	private Context context;
	private String from;
	private boolean isConnect;
	
	public HttpGetTool(Context context,String from)
	{
		this.context=context;
		this.from=from;
		this.isConnect=false;
	}
	
	@Override
    protected void onPreExecute() 
	{
        super.onPreExecute();
        
        ConnectivityManager connManager =
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
			
		if (info == null || !info.isConnected()) 
		{
			//Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
		} 
		else 
		{
		   if (!info.isAvailable())
		   {
			  // Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
		   } 
		   else 
		   {
			   isConnect=true;
		   }
		}
    }
	
	
	@Override
	protected String doInBackground(String... urls)
	{
		// TODO Auto-generated method stub
		String response=null;
		if(isConnect)
		{
			if(urls!=null)
			{
				if(urls.length!=0)
				{
					DefaultHttpClient httpClient =new DefaultHttpClient();
				    HttpGet httpGet = new HttpGet(urls[0]);
				    httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
				    ResponseHandler<String> responseHandler = new BasicResponseHandler();
			    
					try 
					{
						response = httpClient.execute(httpGet, responseHandler);
					}
					catch (Exception e)
					{
						response=null;
						System.out.println("error");
					}

			httpClient.getConnectionManager().shutdown();
		}
			}
		}
		if(!isCancelled())
		{
			if(response!=null)
			{
			Intent intent=new Intent(from);
			intent.putExtra("result", response);
			context.sendBroadcast(intent);
		}
		}
		if(response!=null)
		{
			System.out.println(response);
		}
		return response;
	}     
	
	protected void onPostExecute (String response) 
	{
	}
}
