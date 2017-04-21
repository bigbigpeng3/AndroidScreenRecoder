package com.example.dw.screenrecord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dw.screenrecord.utils.CommonString;
import com.example.dw.screenrecord.utils.PrefConfigUtil;
import com.example.dw.screenrecord.utils.ShareUtils;
import com.example.dw.screenrecord.utils.TimeUtils;
import com.example.dw.screenrecord.videopath.Picture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {


    public static final String TAG = "MainActivity";
    public static final int AUDIO_RECORD_REPERMISSION_CODE = 11;
    public static final int STORAGE_WRITE_PERMISSION_CODE = 12;
    public static final int REQUEST_CODE = 15;


    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnOpenFile;

    private boolean recording = true;

    private String mPathFile;

    //Android 5.0 后提供公开的api实现屏幕截屏和录制
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;    //截屏使用
    private MediaRecorder mediaRecorder;   //屏幕录制使用


    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private int mVideoRate;
    private int mVideoSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolBar();


        mBtnStart = (Button) findViewById(R.id.btn_start_record);
        mBtnStop = (Button) findViewById(R.id.btn_stop_record);
        mBtnOpenFile = (Button) findViewById(R.id.btn_open_file);

        listView = (ListView) findViewById(R.id.lv_show);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        //1.获取ProjectionManager
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mediaRecorder = new MediaRecorder();  //创建MediaRecorder

        mPathFile = getsaveDirectory();

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();  //开始屏幕录制
                Toast.makeText(MainActivity.this, "屏幕录制中.....", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();  //停止屏幕录制
                Toast.makeText(MainActivity.this, "已停止录制", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnOpenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(Uri.fromFile(new File(mPathFile)), "video/*");
                startActivity(intent);
            }
        });


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mScreenDensity = metrics.densityDpi;
        mVideoSize = mScreenWidth * mScreenHeight;
        mVideoRate = 24;


//        mScreenWidth = 720;
//        mScreenHeight = 1280;
//        mScreenDensity = 1;


        cur_path = mPathFile;
        loadVaule();


    }

    private void initToolBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.base_toolbar_menu);//设置右上角的填充菜单

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int menuItemId = item.getItemId();

                if (menuItemId == R.id.action_setting) {
                    Toast.makeText(MainActivity.this, "设置", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this,SettingActivity.class));

                }
                return true;
            }
        });

    }


    //android 6.0 动态申请相关权限
    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_RECORD_REPERMISSION_CODE);
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_WRITE_PERMISSION_CODE);
            }
        }
    }


    //开始屏幕录制
    public void startRecord() {
        requestPermission();
        //2.开始屏幕录制
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_CODE);
    }


    public void stopRecord() {
        //必须先要预先判断，否则调用stop方法是会抛出异常
        if (isRecording()) {
            mediaRecorder.stop();
        }
        mediaRecorder.reset();
        virtualDisplay.release();
        mediaProjection.stop();

        // 刷新 视频列表
        loadVaule();

    }

    public void createVirturalDisplay() {
        //3.获取虚拟屏幕  关键是
        virtualDisplay = mediaProjection.createVirtualDisplay(TAG, mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
    }


    public void initMediaRecoder(int bitRate) {


        //注意顺序！！！
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);   //设置音频源，从麦克风中获取
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(mPathFile + TimeUtils.millis2String(System.currentTimeMillis()) + "_" + mScreenWidth + "x" + mScreenHeight + "_" + mScreenDensity + "_"+mVideoRate + "fps" + ".mp4");

        mediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);  //after setVideoSource(), setOutFormat()
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //after setOutputFormat()
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);  //after setOutputFormat()

        mediaRecorder.setVideoEncodingBitRate(bitRate);

        mediaRecorder.setVideoFrameRate(mVideoRate);

        try {
            mediaRecorder.prepare();   //千万别忘了prepare
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void initMediaRecoderOld() {
        //注意顺序！！！
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);   //设置音频源，从麦克风中获取
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);    //设置视频源，从Surface中获取
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);   //设置视频输出格式
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);   //设置视频输出格式
        mediaRecorder.setOutputFile(mPathFile + TimeUtils.millis2String(System.currentTimeMillis()) + ".mp4");  //设置文件的输出路径，之前就掉进这个坑了
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  //设置音频编码器
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);   //设置视频编码器

        mediaRecorder.setVideoSize(480, 720);   //设置视频文件的分辨率或者说是大小
//        mediaRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);   //设置视频编码帧率
        mediaRecorder.setVideoEncodingBitRate(3 * 480 * 720);// 设置帧频率，然后就清晰了

        mediaRecorder.setVideoFrameRate(24);  //设置视频的帧率

        try {
            mediaRecorder.prepare();   //千万别忘了prepare
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void createImageReader() {
        imageReader = ImageReader.newInstance(480, 720, PixelFormat.RGBA_8888, 2);
    }


    public boolean isRecording() {
        return recording;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection == null) {
                return;
            }
            initMediaRecoder(mVideoSize);
            createVirturalDisplay();
            mediaRecorder.start();
            recording = true;

        }
    }

    public String getsaveDirectory() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";

            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }
            return rootDir;
        } else {
            return null;
        }
    }

    private String cur_path;
    private List<Picture> listPictures;
    ListView listView;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            if (msg.what == 0) {
                List<Picture> listPictures = (List<Picture>) msg.obj;
//				Toast.makeText(getApplicationContext(), "handle"+listPictures.size(), 1000).show();
                MyAdapter adapter = new MyAdapter(listPictures);
                listView.setAdapter(adapter);
            }
        }

    };

    File file;

    private void loadVaule() {


        if (file == null) {
            file = new File(cur_path);
        }

        File[] files = file.listFiles();

        if (listPictures == null) {
            listPictures = new ArrayList<>();
        }

        listPictures.clear();


        if (files == null) {
//            Log.e(" zp ", "file.getAbsolutePath() = " + file.getAbsolutePath());
//            Log.e(" zp ", "files == null");
            return;
        }

        for (int i = 0; i < files.length; i++) {
            Picture picture = new Picture();
            picture.setBitmap(getVideoThumbnail(files[i].getPath(), 200, 200, MediaStore.Images.Thumbnails.MICRO_KIND));
            picture.setPath(files[i].getPath());
            listPictures.add(picture);

        }


        sendMessage();

    }

    private void sendMessage() {
        Message msg = new Message();
        msg.what = 0;
        msg.obj = listPictures;

        handler.sendMessage(msg);
    }


    //获取视频的缩略图
    private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
//		        System.out.println("w"+bitmap.getWidth());
//		        System.out.println("h"+bitmap.getHeight());
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }


    public class MyAdapter extends BaseAdapter {
        private List<Picture> listPictures;

        public MyAdapter(List<Picture> listPictures) {
            super();
            this.listPictures = listPictures;

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return listPictures.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return listPictures.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup arg2) {
            // TODO Auto-generated method stu
            View view = getLayoutInflater().inflate(R.layout.item_video, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.iv_show);
            TextView textView = (TextView) view.findViewById(R.id.tv_show);

            imageView.setImageBitmap(listPictures.get(position).getBitmap());


            int index = listPictures.get(position).getPath().lastIndexOf("/");
            String realName = listPictures.get(position).getPath().substring(index + 1);

            textView.setText(realName);

            return view;

        }
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

//        Toast.makeText(getApplicationContext(), "点击了" + arg2, Toast.LENGTH_SHORT).show();
        playVideo(listPictures.get(arg2).getPath());
        Log.e("path", listPictures.get(arg2).getPath());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        File file = new File(listPictures.get(position).getPath());
        ShareUtils.shareFile(this, Uri.fromFile(file));


        return true;
    }


    //调用系统播放器   播放视频
    private void playVideo(String videoPath) {
//					   Intent intent = new Intent(Intent.ACTION_VIEW);
//					   String strend="";
//					   if(videoPath.toLowerCase().endsWith(".mp4")){
//						   strend="mp4";
//					   }
//					   else if(videoPath.toLowerCase().endsWith(".3gp")){
//						   strend="3gp";
//					   }
//					   else if(videoPath.toLowerCase().endsWith(".mov")){
//						   strend="mov";
//					   }
//					   else if(videoPath.toLowerCase().endsWith(".avi")){
//						   strend="avi";
//					   }
//					   intent.setDataAndType(Uri.parse(videoPath), "video/*");
//					   startActivity(intent);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(videoPath);
        intent.setDataAndType(Uri.fromFile(file), "video/*");
        startActivity(intent);
    }


    public void setSetting() {

        mScreenWidth = PrefConfigUtil.getInt(this, CommonString.VEIDO_WIDTH,mScreenWidth);
        mScreenHeight = PrefConfigUtil.getInt(this, CommonString.VEIDO_HEIGHT,mScreenHeight);
        mVideoSize = mScreenWidth * mScreenHeight;
        mVideoRate = PrefConfigUtil.getInt(this, CommonString.VEIDO_FRAME_RATE,mVideoRate);

        Log.e(TAG,"mScreenWidth = " + mScreenWidth);
        Log.e(TAG,"mScreenHeight = " + mScreenHeight);
        Log.e(TAG,"mVideoSize = " + mVideoSize);
        Log.e(TAG,"mVideoRate = " + mVideoRate);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setSetting();
    }


}
