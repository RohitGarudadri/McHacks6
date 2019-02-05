//copyright Selima Cheriff, Rohit Garudadri, Isabel Hadziomerovic, Alex Gass.

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TactioTest {
    
    public static String token = "";
    
    public static void main(String[] args) throws JSONException, IOException {
        TactioTest b = new TactioTest();
        token = b.getToken();
        System.out.println(token);
        List<String> patientIds = b.getPatients();
        double i = 0.0;
        FileWriter infectionWriter = new FileWriter("infection.csv");
        FileWriter obesityWriter = new FileWriter("obesity.csv");
        FileWriter diabetesWriter = new FileWriter("diabetehypertension.csv");
        FileWriter dyslipWriter = new FileWriter("dyslipidemia.csv");
        infectionWriter.append("patient id");
        infectionWriter.append("\n");
        obesityWriter.append("patient id");
        obesityWriter.append("\n");
        diabetesWriter.append("patient id");
        diabetesWriter.append("\n");
        dyslipWriter.append("patient id");
        dyslipWriter.append("\n");
        for (String patientId: patientIds) {
            i++;
            if(i>=432){
                List<JSONObject> obs =  b.getObservations(patientId);
                /*a)here the observations for one patient have been gathered
                 * we are now going to scrape that observation for information that will lead to
                 * the classification
                 * b) create patient
                 * c) classify patient
                 * d) add patient id to appropriate array list
                 * */
                ArrayList<Double> bmi = b.helperGetValue(obs, "snomed", "60621009");
                ArrayList<Double> wbcc = b.helperGetValue(obs, "loinc", "26464-8");
                ArrayList<Double> glucoseLevels = b.helperGetValue(obs, "snomed", "434912009");
                ArrayList<Double> glucosePostMeal = b.helperGetValue(obs, "snomed", "302788006");
                ArrayList<Double> AIC = b.helperGetValue(obs, "snomed", "733829007");
                ArrayList<Double> pulse = b.helperGetValue(obs, "snomed", "78564009");
                ArrayList<Double> hdl = b.helperGetValue(obs, "snomed", "102737005");
                ArrayList<Double> ldl = b.helperGetValue(obs, "snomed", "102739008");
                ArrayList<Double> triglyceride = b.helperGetValue(obs, "loinc", "70218-3");
                Patient p = new Patient(patientId, bmi,wbcc,glucoseLevels, glucosePostMeal,AIC,pulse,hdl,ldl,triglyceride);
                boolean infected = false;
                boolean obese = false;
                boolean diabetic = false;
                boolean dyslipidemiac = false;
                infected = checkInfected(p);
                obese = checkObese(p);
                diabetic = checkDiabetic(p);
                dyslipidemiac = checkDyslipidemia(p);
                if (infected) {
                    infectionWriter.append(p.ID);
                    infectionWriter.append("\n");
                }
                if (obese) {
                    obesityWriter.append(p.ID);
                    obesityWriter.append("\n");
                }
                if (diabetic) {
                    diabetesWriter.append(p.ID);
                    diabetesWriter.append("\n");
                }
                if (dyslipidemiac) {
                    dyslipWriter.append(p.ID);
                    dyslipWriter.append("\n");
                }
                System.out.println("At patient "+i);
                System.out.println("Getting observations " + i/patientIds.size()*100.0 + "% complete ... ");
            }
        } 
        infectionWriter.flush();
        infectionWriter.close();
        obesityWriter.flush();
        obesityWriter.close();
        diabetesWriter.flush();
        diabetesWriter.close();
        dyslipWriter.flush();
        dyslipWriter.close();
    }
    
    
    public static boolean checkObese(Patient p) {
        ArrayList<Double> bmi = p.bmi;
        double avgBMI = calcAvg(bmi);
        if (avgBMI < 30) {
            return false;
        }
        return true;
    }
    
    public static boolean checkInfected(Patient p) {
        
        //this field could be obtained from the patient
        ArrayList<Double> WBCcount = p.whitebloodCellCounts;
        
        double average = calcAvg(WBCcount);
        double stDev = calcStDev(WBCcount);
        
        //checking criteria for infection
        if (WBCcount.size() < 2) {
            return false;
        }
        if (average < 5500) {
            return false;
        }

        if (stDev < 1000) {
            return false;
        }
        //if nothing returned false, the patient is infected
        return true;
        
    }
    public static boolean checkDiabetic(Patient p) {
        
        //these fields could preferrably just be obtained from the patient, eg. glucose = p.glucose;
        ArrayList<Double> glucose = p.glucoseLevels;
        ArrayList<Double> glucosePM = p.glucosePostMeal;
        ArrayList<Double> A1C = p.AIC;
        ArrayList<Double> pulserate = p.pulse;
        
        //checking criteria for diabetics
        double avgGlucose = calcAvg(glucose);
        if (avgGlucose < 5.5) {
            return false;
        }
        double glucoseStDev = calcStDev(glucose);
        if (glucoseStDev > 0.6) {
            return false;
        }
        
        double avgGlucosePM = calcAvg(glucosePM);
        if (avgGlucosePM < 6.9) {
            return false;
        }
        double glucosePMStDev = calcStDev(glucosePM);
        if (glucosePMStDev > 0.9) {
            return false;
        }
        
        double avgA1C = calcAvg(A1C);
        if (avgA1C < 6.0) {
            return false;
        }
        double A1CStDev = calcStDev(A1C);
        if (A1CStDev > 0.4) {
            return false;
        }
        

        double avgPulserate= calcAvg(pulserate);
        if (avgPulserate < 70.0) {
            return false;
        }
        double pulserateStDev = calcStDev(pulserate);
        if (pulserateStDev > 10.0) {
            return false;
        }
        //if nothing returned false then patient is a diabetic
        return true;
        
    }
    public static boolean checkDyslipidemia(Patient p) {

        //fields obtained from the patient
        ArrayList<Double> hdl = p.hdl;
        ArrayList<Double> ldl = p.ldl;
        ArrayList<Double> triglyceride = p.triglyceride;
        
        //checking criteria for dyslipidemia
        double avgHdl = calcAvg(hdl);
        if (avgHdl < 1.6) {
            return false;
        }
        double hdlStDev = calcStDev(hdl);
        if (hdlStDev > 0.15) {
            return false;
        }
        
        double avgLdl = calcAvg(ldl);
        if (avgLdl > 3.1) {
            return false;
        }
        double ldlStDev = calcStDev(ldl);
        if (ldlStDev > 0.3) {
            return false;
        }
        
        double avgTriglyc = calcAvg(triglyceride);
        if (avgTriglyc < 1.13) {
            return false;
        }
        double triglycStDev = calcStDev(triglyceride);
        if (triglycStDev > 0.1) {
            return false;
        }
        
        //if nothing returned false, then patient is a dyslipidemiac
        return true;
        
    }
    public static double calcStDev(ArrayList<Double> values) {
        //calculate average of the given values
        double average = calcAvg(values);
        
        //calculate the square differences for the values
        ArrayList<Double> sqrDiffs = new ArrayList<Double>();
        for (double v : values) {

            double diff = v - average;
            diff = Math.pow(diff, 2);
            sqrDiffs.add(diff);
        }
        
        //calculate the average square difference
        double averageSqrDiff = calcAvg(sqrDiffs);
        
        double stDev = Math.sqrt(averageSqrDiff); 
        return stDev;
    }
    
    public static double calcAvg(ArrayList<Double> values) {
        double avg = 0;
        for (Double d : values) {
            avg += d;
        }
        avg /= values.size();
        return avg;
    }
    
    
    public ArrayList<Double> helperGetValue(List<JSONObject> obs, String standard, String code) throws JSONException{
        ArrayList<Double> values= new ArrayList<Double>();
        for(int i = 0; i<obs.size(); i++) {
            JSONObject resources = obs.get(i).getJSONObject("resource");
            JSONArray jsonArray= resources.getJSONObject("code").getJSONArray("coding");
            for(int j = 0; j<jsonArray.length(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                if (jsonObject.getString("system").contains(standard)) {
                    if (jsonObject.getString("code").contains(code)) {
                        JSONObject tmp = resources.getJSONObject("valueQuantity");
                        values.add(Double.parseDouble(tmp.getString("value")));
                    }
                }
            }
        }
        return values;
    }
/*  public ArrayList<Double> helperGetValueComponent(List<JSONObject> obs, String standard, String code) throws JSONException{
        ArrayList<Double> values= new ArrayList<Double>();
        for(int i = 0; i<obs.size(); i++) {
            JSONObject component = obs.get(i).getJSONObject("resource").optJSONObject("component");
            if(component!=null) {
                JSONArray jsonArray= component.getJSONObject("code").getJSONArray("coding");
                for(int j = 0; j<jsonArray.length(); j++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    if (jsonObject.getString("system").contains(standard)) {
                        if (jsonObject.getString("code").contains(code)) {
                            JSONObject tmp = component.getJSONObject("valueQuantity");
                            values.add(Double.parseDouble(tmp.getString("value")));
                        }
                    }
                }
            }
        }
        return values;
    }*/
    
    public List<String> getPatients() throws ClientProtocolException, IOException, JSONException {
        int pageIndex = 1;
        boolean hasNextPage = true;
        
        List<String> pats = new ArrayList<String>();
        
        while (hasNextPage) {
            String response = getPatientPage(pageIndex);
            
            pageIndex += 1;
            JSONObject respObj = new  JSONObject(response);
            
            JSONArray entries = respObj.optJSONArray("entry");
            if(entries!=null) {
                for(int i = 0; i<entries.length(); i++) {
                    JSONObject entry = entries.getJSONObject(i);
                    JSONObject res   = entry.getJSONObject("resource");
                    String id = res.getString("id");
                    pats.add(id);
                }
            }

            JSONArray links = respObj.getJSONArray("link");
            hasNextPage = false;
            if(links!=null) {
                for(int i = 0; i<links.length(); i++) {
                    JSONObject link = links.getJSONObject(i);
                    if (link.getString("relation").equals("next")) {
                        hasNextPage  = true;
                    }
                }
            }       
        }
        return pats;
    }
    
    public String getPatientPage(int pageNumber) throws ClientProtocolException, IOException {
        String url = "https://sandbox86.tactiorpm7000.com/tactio-clinician-api/1.1.4/Patient" +
                "?page=" + Integer.toString(pageNumber);
        
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer "+ this.token);
        HttpResponse response = client.execute(request);
        
        return readResponse(response);
    }
    
    public String readResponse(HttpResponse resp) throws UnsupportedOperationException, IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        line = rd.readLine();
        while (line != null) {
            result.append(line);
            line = rd.readLine();
        }
        return result.toString();
    }
    
    //given a patient id, fetch all the observations
    public List<JSONObject> getObservations(String patientId) throws ClientProtocolException, IOException, JSONException {
        int pageIndex = 1;
        boolean hasNextPage = true;
        
        List<JSONObject> obs = new ArrayList<JSONObject>();
        
        while (hasNextPage) {
            String response = getObservationPage(patientId, pageIndex);
            pageIndex += 1;
            JSONObject respObj = new  JSONObject(response);
            
            JSONArray entries = respObj.optJSONArray("entry");
            if(entries!=null) {
                for(int i = 0; i<entries.length(); i++) {
                    JSONObject entry = entries.getJSONObject(i);
                    obs.add(entry);
                }
            }

            JSONArray links = respObj.optJSONArray("link");
            if (links != null) {
                hasNextPage = false;
                for (int i = 0; i < links.length(); i++) {
                    JSONObject link = links.getJSONObject(i);
                    if (link.getString("relation").equals("next")) {
                        hasNextPage = true;
                    }
                }
            }
        }
        return obs;
    }
    
    
    public String getObservationPage(String patientId, int pageNumber) throws ClientProtocolException, IOException {
        String url = "https://sandbox86.tactiorpm7000.com/tactio-clinician-api/1.1.4/Observation" +
                "?subject=" +patientId +
                "&page=" + Integer.toString(pageNumber);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer "+ this.token);
        HttpResponse response = client.execute(request);
        
        return readResponse(response);
    }
    
    public void getObservation()  {
        try {
            String url = "https://sandbox86.tactiorpm7000.com/tactio-clinician-api/1.1.4/Observation";
            JSONObject obj = new JSONObject();
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer "+ this.token);
            HttpResponse response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            int counter = 1;
            boolean idk = true;
            while (idk) {
                idk = false;
                counter++;
                request = new HttpGet("https://sandbox86.tactiorpm7000.com/tactio-clinician-api/1.1.4/Observation?page="+Integer.toString(counter));
                result = new StringBuffer();
                request.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
                request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer "+ this.token);
                response = client.execute(request);
                rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                line = rd.readLine();
                while (line != null) {
                    if(line.contains("next")) {
                        idk = true;
                    }
                    
                    if(line.contains("Patient")) {
                        String requiredString = line.substring(line.indexOf("Patient") + 9, line.indexOf("identifier")-3);
                        request = new HttpGet("https://sandbox86.tactiorpm7000.com/tactio-clinician-api/1.1.4/Observation?subject="+requiredString);
                        result = new StringBuffer();
                        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
                        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer "+ this.token);
                        response = client.execute(request);
                        rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        line = "";
                        line = rd.readLine();
                        while (line != null) {
                            result.append(line);
                            line = rd.readLine();
                        }
                    }
                    result.append(line);
                    line = rd.readLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Haha error bruv");
        }
    }
    
    public String getToken()  throws ClientProtocolException, JSONException{
        JSONObject obj = new JSONObject();
        obj.put("client_id", "083e9a44a763473fbeb62fbf90b74551");
        obj.put("grant_type", "password");
        obj.put("client_secret", "ba09798f0921456e8b4e5e4588ea536d");
        obj.put("username", "tactioClinician");
        obj.put("password", "tactio");
        try {
        HttpClient hc = HttpClientBuilder.create().build();
        
        String url = "https://sandbox86.tactiorpm7000.com/token.php";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);

        StringEntity entity = new StringEntity(obj.toString(),ContentType.APPLICATION_JSON);
        request.setEntity(entity);
        
        // add request header
        //request.addHeader("User-Agent", );
        HttpResponse response = client.execute(request);
                
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        JSONObject responseResult = new JSONObject(result.toString());
        return responseResult.getString("access_token");
        }catch( IOException e){
            System.out.println("Haha error bruv");
        }
        return null;
    }
}
