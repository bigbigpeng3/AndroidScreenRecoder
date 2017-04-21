package com.example.dw.screenrecord;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.dw.screenrecord.utils.CommonString;
import com.example.dw.screenrecord.utils.PrefConfigUtil;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {


    private RelativeLayout rl_video_size;
    private RelativeLayout rl_video_qulity;
    private RelativeLayout rl_video_rate;

    private TextView tv_video_size;
    private TextView tv_video_qulity;
    private TextView tv_video_rate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);


        rl_video_size = (RelativeLayout) findViewById(R.id.rl_video_size);
        rl_video_qulity = (RelativeLayout) findViewById(R.id.rl_video_qulity);
        rl_video_rate = (RelativeLayout) findViewById(R.id.rl_video_rate);

        rl_video_size.setOnClickListener(this);
        rl_video_qulity.setOnClickListener(this);
        rl_video_rate.setOnClickListener(this);

        tv_video_size = (TextView) findViewById(R.id.tv_video_size);
        tv_video_qulity = (TextView) findViewById(R.id.tv_video_qulity);
        tv_video_rate = (TextView) findViewById(R.id.tv_video_rate);


//        initTextView();


    }

    private void initTextView() {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mScreenWidth = metrics.widthPixels;
        int mScreenHeight = metrics.heightPixels;
        int mScreenDensity = metrics.densityDpi;

        int mVideoSize = mScreenWidth * mScreenHeight;
        int mVideoRate = 30;

        tv_video_size.setText(PrefConfigUtil.getInt(this, CommonString.VEIDO_HEIGHT, mScreenHeight) + "x" + PrefConfigUtil.getInt(this, CommonString.VEIDO_WIDTH, mScreenWidth));

        String qulity = (PrefConfigUtil.getInt(this, CommonString.VEIDO_BIT_RATE, mVideoSize) / 1024) / 1024 + "mbps";

        tv_video_qulity.setText(qulity);

        String frameRate = PrefConfigUtil.getInt(this, CommonString.VEIDO_FRAME_RATE, mVideoRate) + "fps";

        tv_video_rate.setText(frameRate);
    }

    @Override
    public void onClick(View v) {

        if (v == rl_video_size) {//选择 视频大小
            sizeDialog();

        } else if (v == rl_video_qulity) { //选择 视频质量
            bitRateDialog();
        } else if (v == rl_video_rate) { // 选择 视频帧率
            frameDialog();
        }

    }


    private void sizeDialog() {

        String[] items = {"680x480", "1280x720", "1920x1080"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        int index = getSizeIndex();

        builder.setSingleChoiceItems(items, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(SettingActivity.this, "你选择的ID为：" + which, Toast.LENGTH_SHORT).show();
                // TODO Auto-generated method stub
                if (which == 0) {
                    PrefConfigUtil.putInt(SettingActivity.this, CommonString.VEIDO_HEIGHT, 640);
                    PrefConfigUtil.putInt(SettingActivity.this, CommonString.VEIDO_WIDTH, 480);
                }

                if (which == 1) {
                    PrefConfigUtil.putInt(SettingActivity.this, CommonString.VEIDO_HEIGHT, 1280);
                    PrefConfigUtil.putInt(SettingActivity.this, CommonString.VEIDO_WIDTH, 720);
                }

                if (which == 2) {
                    PrefConfigUtil.putInt(SettingActivity.this, CommonString.VEIDO_HEIGHT, 1920);
                    PrefConfigUtil.putInt(SettingActivity.this, CommonString.VEIDO_WIDTH, 1080);
                }

                initTextView();

            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    public int getSizeIndex() {

        if (PrefConfigUtil.getInt(this, CommonString.VEIDO_HEIGHT, 1920) == 1920) {
            return 2;
        } else if (PrefConfigUtil.getInt(this, CommonString.VEIDO_HEIGHT, 1920) == 1280) {
            return 1;
        } else if (PrefConfigUtil.getInt(this, CommonString.VEIDO_HEIGHT, 1920) == 680) {
            return 0;
        }

        return 2;
    }


    private void bitRateDialog() {

        String[] items = {"1mbps", "4mbps", "8mbps", "16mbps", "24mbps", "32mbps", "50mbps", "100mbps", "200mbps"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("");

        int index = getBitIndex();

        builder.setSingleChoiceItems(items, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(SettingActivity.this, "你选择的ID为：" + which, Toast.LENGTH_SHORT).show();
                // TODO Auto-generated method stub
                if (which == 0) {
                    setRatePre(1);
                }

                if (which == 1) {
                    setRatePre(4);
                }

                if (which == 2) {
                    setRatePre(8);
                }

                if (which == 3) {
                    setRatePre(16);
                }
                if (which == 4) {
                    setRatePre(24);
                }
                if (which == 5) {
                    setRatePre(32);
                }

                if (which == 6) {
                    setRatePre(50);
                }
                if (which == 7) {
                    setRatePre(100);
                }

                if (which == 8) {
                    setRatePre(200);
                }
                initTextView();

            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    public int getBitIndex() {

        int bit = PrefConfigUtil.getInt(this, CommonString.VEIDO_BIT_RATE, 1024*1024);

        bit = bit >> 10 >> 10;

        // TODO Auto-generated method stub
        if (bit == 1) {
            return 0;
        }

        if (bit == 4) {
            return 1;
        }

        if (bit == 8) {
            return 2;
        }

        if (bit == 16) {
            return 3;
        }

        if (bit == 24) {
            return 4;
        }

        if (bit == 32) {
            return 5;
        }

        if (bit == 50) {
            return 6;
        }

        if (bit == 100) {
            return 7;
        }

        if (bit == 200) {
            return 8;
        }


        return 0;
    }

    public void setRatePre(int rate) {

        int realRate = rate << 10 << 10; // = rate * 1024 * 1024

        PrefConfigUtil.putInt(SettingActivity.this, CommonString.VEIDO_BIT_RATE, realRate);
    }


    private void frameDialog() {

        String[] items = {"24fps", "30fps", "48fps","60fps"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        int index = getFrameIndex();

        builder.setSingleChoiceItems(items, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(SettingActivity.this, "你选择的ID为：" + which, Toast.LENGTH_SHORT).show();
                // TODO Auto-generated method stub
                if (which == 0) {
                    setFramePre(24);
                }

                if (which == 1) {
                    setFramePre(30);
                }

                if (which == 2) {
                    setFramePre(48);
                }

                if (which == 3) {
                    setFramePre(60);
                }

                initTextView();

            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    public int getFrameIndex() {

        int frame = PrefConfigUtil.getInt(this,CommonString.VEIDO_FRAME_RATE,24);

        if (frame == 24){
            return 0;
        }
        if (frame == 30){
            return 1;
        }
        if (frame == 48){
            return 2;
        }

        if (frame == 60){
            return 3;
        }

        return 0;
    }

    private void setFramePre(int frame) {
        PrefConfigUtil.putInt(SettingActivity.this, CommonString.VEIDO_FRAME_RATE, frame);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTextView();
    }
}
