
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
//import org.apache.http.HttpHeaders;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.message.BasicNameValuePair;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;

public class Patient {

	public String ID;
	public ArrayList<Double> bmi;
	public ArrayList<Double> whitebloodCellCounts; 
	public ArrayList<Double> glucoseLevels;
	public ArrayList<Double> glucosePostMeal; 
	public ArrayList<Double> AIC;
	public ArrayList<Double> pulse;
	public ArrayList<Double> hdl;
	public ArrayList<Double> ldl;
	public ArrayList<Double> triglyceride;
	
	
	public Patient(String ID,ArrayList<Double> bmi, ArrayList<Double> whitebloodCellCounts, ArrayList<Double> glucoseLevels, ArrayList<Double> glucosePostMeal, ArrayList<Double> AIC, ArrayList<Double>pulse, ArrayList<Double> hdl, ArrayList<Double> ldl, ArrayList<Double> triglyceride) {
		this.ID = ID;
		this.bmi= bmi;
		this.whitebloodCellCounts = whitebloodCellCounts;
		this.glucoseLevels = glucoseLevels;
		this.glucosePostMeal = glucosePostMeal;
		this.AIC = AIC;
		this.pulse = pulse;
		this.hdl = hdl;
		this.ldl = ldl;
		this.triglyceride = triglyceride;
	}
}
