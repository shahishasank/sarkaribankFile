package com.sarkaribank.Activities;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.Window;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.sarkaribank.webview.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class BottomActivity extends AppCompatActivity {

    WebView edlwe;
    SwipeRefreshLayout mySwipeRefreshLayout;
    File imagePath;
    LinearLayout offlinelayout;
    Button exitap;

    private BottomActivity getThis(){ return this; }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String URLToLoad="https://app.sarkaribank.com/";

        //get URL from notification
        try {
            Bundle bundle = getIntent().getExtras();

            if (bundle != null && bundle.getString("openURL") != null) {
                URLToLoad = bundle.getString("openURL");
            }
        }catch(Exception e){
            //Log.d("intent error in bottom",e.toString());
        }

        edlwe = findViewById(R.id.mainwebview_bottom);
        edlwe.setVisibility(View.VISIBLE);
        offlinelayout = findViewById(R.id.offlinelayout);
        exitap = findViewById(R.id.exitap);
        exitap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BottomActivity.this,BottomActivity.class));
                finish();
            }
        });
        mySwipeRefreshLayout = findViewById(R.id.swipeContainer);
        edlwe.clearCache(true);
        edlwe.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        edlwe.getSettings().setJavaScriptEnabled(true);

        edlwe.addJavascriptInterface(BottomActivity.this,"Android");
        //edlwe.setWebChromeClient(new WebChromeClient());
        edlwe.setWebViewClient(new MyWebViewClient(this));
        edlwe.loadUrl(URLToLoad);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mySwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mySwipeRefreshLayout.setRefreshing(false);
                                edlwe.reload();

                            }
                        }, 3000);
                    }
                }
        );
        /*FloatingActionButton fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("making","image");
                Bitmap bitmap = takeScreenshot();
                //adding watermark

                saveBitmap(bitmap);
                shareIt();
            }
        });*/

    }
    /*public Bitmap takeScreenshot() {

        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap b=rootView.getDrawingCache();
        rootView.setDrawingCacheEnabled(false);
        return b;
    }*/
    private int getNavWidthHeight(){
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;

    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void saveBitmap(Bitmap bitmap) {
        imagePath = new File(Environment.getExternalStorageDirectory() + "/screenshot.png");
        Log.v("saving","image");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
    }


    /*public void showAppExitDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please confirm");
        builder.setMessage("No back history found, want to exit the app?");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                BottomActivity.super.finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Toast.makeText(BottomActivity.this, "Thank You", Toast.LENGTH_SHORT).show();

            }
        });

        builder.create().show();
    }*/

    public void onBackPressed() {
        if (edlwe.canGoBack()) {
            edlwe.goBack();
        } else if(!edlwe.getUrl().equals("https://app.sarkaribank.com/")) {
            startActivity(new Intent(getApplicationContext(),BottomActivity.class));
            finish();
        }
        else {
            startActivity(new Intent(BottomActivity.this,BackActivity.class));
            finish();
        }
    }

    @JavascriptInterface
    public void shareButtonClicked(final String text, final String link, String status) {

        if(status.equals("0")){
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, text+"\n"+link);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, text+"\n"+link));


        }else {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {

                    View rootView = findViewById(android.R.id.content).getRootView();
                    rootView.setDrawingCacheEnabled(false);
                    rootView.setDrawingCacheEnabled(true);
                    Bitmap ss=rootView.getDrawingCache();

                    int navBarHeight=getNavWidthHeight();
                    int statusBarHeight=getStatusBarHeight();

                    //adding ss and shadow
                    Bitmap bitmap=Bitmap.createBitmap(ss,0,statusBarHeight,ss.getWidth(),ss.getHeight()-navBarHeight-statusBarHeight);

                    final int canvasWidth=1920;
                    final int canvasHeight=1920;
                    Bitmap result=Bitmap.createBitmap(canvasWidth,canvasHeight,bitmap.getConfig());

                    Canvas canvas=new Canvas(result);
                    canvas.drawColor(Color.WHITE);
                    Paint shadowPaint=new Paint();
                    shadowPaint.setColor(Color.parseColor("#00cbff"));

                    Paint border=new Paint();
                    border.setColor(Color.parseColor("#095baf"));
                    border.setStyle(Paint.Style.STROKE);
                    border.setStrokeWidth(10);

                    canvas.drawRect(new Rect(0,0,canvasWidth,canvasHeight),border);

                    border.setStrokeWidth(5);

                    int nh = (int) ( bitmap.getHeight() * (768.0 / bitmap.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 768, nh, true);

                    Rect shadow=new Rect(0,0,scaled.getWidth(),scaled.getHeight());
                    float sd=96*scaled.getWidth()/scaled.getHeight();
                    float translateX=(canvasWidth/2-scaled.getWidth())/2;
                    float translateY=(canvasHeight-scaled.getHeight())/2;
                    canvas.save();
                    canvas.translate(translateX+sd,translateY+sd);
                    canvas.drawRect(shadow,shadowPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.translate(translateX,translateY);
                    canvas.drawBitmap(scaled,0,0,null);
                    canvas.drawRect(shadow,border);
                    canvas.restore();

                    //add icon
                    int left=canvasWidth/2;
                    int right=canvasWidth;
                    int blockHeight=canvasHeight/12;

                    Rect iconRect=new Rect(left,3*blockHeight,right,5*blockHeight);
                    Bitmap icon=BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.logo);

                    int iconX=(iconRect.left+(iconRect.width()/2))-blockHeight;
                    int iconY=(iconRect.top+(iconRect.height()/2))-blockHeight;
                    iconRect.left=iconX;
                    iconRect.top=iconY;
                    iconRect.right=iconX+2*blockHeight;
                    iconRect.bottom=iconY+2*blockHeight;
                    canvas.drawBitmap(icon,null,iconRect,null);

                    //add text
                    String t1="SarkariBank.com",
                            t2="These details are shared by sarkaribank mobile app. You can download it from playstore.",
                            t3="Go on playstore and search sarkaribank then install it.",
                            t4="Thanks for sharing!";

                    TextPaint headingPaint=new TextPaint();
                    headingPaint.setTextSize(blockHeight/2);
                    TextPaint textPaint = new TextPaint();
                    textPaint.setTextSize(blockHeight/4);
                    headingPaint.setColor(Color.parseColor("#095baf"));

                    Rect t1Rect=new Rect(left,5*blockHeight,right,6*blockHeight);
                    Rect t2Rect=new Rect(left+(int)translateX,6*blockHeight,right-(int)translateX,7*blockHeight);
                    Rect t3Rect=new Rect(left+(int)translateX,7*blockHeight,right-(int)translateX,8*blockHeight);
                    Rect t4Rect=new Rect(left,8*blockHeight,right,9*blockHeight);

                    drawText(canvas,t1Rect,t1,headingPaint);
                    drawText(canvas,t2Rect,t2,textPaint);
                    drawText(canvas,t3Rect,t3,textPaint);
                    drawText(canvas,t4Rect,t4,headingPaint);

                    saveBitmap(result);

                    Uri uri = Uri.fromFile(imagePath);
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("image/*");
                    String shareBody = text + "\n" + link;
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
                /**
                 * @return text height
                 */
                private float getTextHeight(String text, Paint paint) {

                    Rect rect = new Rect();
                    paint.getTextBounds(text, 0, text.length(), rect);
                    return rect.height();
                }

                private void drawText(Canvas canvas, Rect rect, String textOnCanvas, TextPaint textPaint){
                    StaticLayout sl = new StaticLayout(textOnCanvas, textPaint, rect.width(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);

                    canvas.save();
                    //float textHeight = getTextHeight(textOnCanvas,textPaint);
                    //int numberOfTextLines = sl.getLineCount();

                    float textYCoordinate=rect.top;
                    float textXCoordinate = rect.left;
                    //rect.bottom=rect.top+(int)textHeight*numberOfTextLines;
                    canvas.translate(textXCoordinate, textYCoordinate);
                    sl.draw(canvas);
                    canvas.restore();
                }


                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    Toast.makeText(BottomActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                }


            };
            TedPermission.with(this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();
            //Get the string value to process
//        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

//                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
//                        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
//                        sendIntent.setType("text/plain");
//                        startActivity(Intent.createChooser(sendIntent, text));
        }
    }



    public class MyWebViewClient extends WebViewClient {
        private Activity activity = null;

        private MyWebViewClient(Activity activity2) {
            this.activity = activity2;
        }

        public boolean shouldOverrideUrlLoading(WebView webView, String str) {
            if (str.indexOf("app.sarkaribank.com") > -1) {
                return false;
            }
            this.activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mySwipeRefreshLayout.setRefreshing(false);
            edlwe.loadUrl("javascript:window.Android.processHTML( (function (){var metas = document.getElementsByTagName('meta'); \n" +
                    "\n" +
                    "   for (var i=0; i<metas.length; i++) { \n" +
                    "      if (metas[i].getAttribute(\"name\") == \"theme-color\") { \n" +
                    "         return metas[i].getAttribute(\"content\"); \n" +
                    "      } \n" +
                    "   } \n" +
                    "\n" +
                    "    return  \"\";})() );");
            super.onPageFinished(view, url);

        }


        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Toast.makeText(BottomActivity.this, "Error occured, please check newtwork connectivity", Toast.LENGTH_SHORT).show();
            if (view.canGoBack()) {
                view.goBack();
            }
            //edlwe.loadUrl("file:///android_asset/No Internet Available.html");

            offlinelayout.setVisibility(View.VISIBLE);
            edlwe.setVisibility(View.GONE);

            // Stuff that updates the UI
            Window window = getThis().getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            window.setStatusBarColor(Color.parseColor("#ffffff"));
        }
    }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(final String content) {
            try {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        ConnectivityManager cm = (ConnectivityManager)  getSystemService(Context.CONNECTIVITY_SERVICE);

                        // Get Network Info from connectivity Manager
                        NetworkInfo networkInfo = cm != null ? cm.getActiveNetworkInfo() : null;

                        // Stuff that updates the UI
                        Window window = getThis().getWindow();

                        // clear FLAG_TRANSLUCENT_STATUS flag:
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        // finally change the color
                        if(content.isEmpty()||networkInfo==null)
                            window.setStatusBarColor(Color.parseColor("#ffffff"));
                        else
                            window.setStatusBarColor(Color.parseColor(content));

                    }
                });
            }catch (Exception e){
                //Log.v("error",e.toString());
            }

        }
}
