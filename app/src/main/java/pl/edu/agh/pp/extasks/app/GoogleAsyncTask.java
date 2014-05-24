package pl.edu.agh.pp.extasks.app;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Kuba on 11.05.14.
 */
public class GoogleAsyncTask extends AsyncTask<String, String, String> {
    String result;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected String doInBackground(String... params) {
        //https://www.googleapis.com/tasks/v1/lists/MDg1MTIwOTEyNzA0NTYwMjY2ODU6MDow/tasks
        //String url = "https://www.googleapis.com/tasks/v1/lists/{list}/tasks";
        String authUrl = "https://www.googleapis.com/auth/tasks";
        String username = "129983892196-ahd​em6mt5pau5sufj9o​c1rocngqmr4e6.ap​ps.googleusercon​tent.com";
        String password = "AIzaSyAN01Uou4oW​pt6EB57Vr4eof4Wb​DZ3RJs4";
        HttpAuthentication authHeader = new HttpBasicAuthentication(username, password);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAuthorization(authHeader);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);


        String url = "https://www.googleapis.com/tasks/v1/users/@me/lists";
        String taskList = "MDg1MTIwOTEyNzA0NTYwMjY2ODU6MDow";
        Log.d(TAG, "1");
        RestTemplate restTemplate = new RestTemplate();
        Log.d(TAG, "1");
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        try {
            ResponseEntity<String> response = restTemplate.exchange(authUrl, HttpMethod.GET, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        Log.d(TAG, "1");
        String result = restTemplate.getForObject(url, String.class, taskList);
        Log.d(TAG, "1");
        this.result = result;
        Log.d(TAG, "1");
        return result;

        /*String url = "https://ajax.googleapis.com/ajax/services/search/web?v=1.0&q={query}";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String result = restTemplate.getForObject(url, String.class, "SpringSource");
        this.result = result;
        return result;*/


    }

    public String getResult() {
        return this.result;
    }
}
