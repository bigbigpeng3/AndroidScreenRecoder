package com.example.dw.screenrecord.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by zp on 4/21/17.
 */

public class ShareUtils {

    /**
     * 分享视频
     */
    public static void shareFile(Context context, Uri uri) {
        // File file = new File("\sdcard\android123.cwj"); //附件文件地址

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("subject", ""); //
        intent.putExtra("body", ""); // 正文
        intent.putExtra(Intent.EXTRA_STREAM, uri); // 添加附件，附件为file对象
        if (uri.toString().endsWith(".gz")) {
            intent.setType("application/x-gzip"); // 如果是gz使用gzip的mime
        } else if (uri.toString().endsWith(".txt")) {
            intent.setType("text/plain"); // 纯文本则用text/plain的mime
        } else {
            intent.setType("application/octet-stream"); // 其他的均使用流当做二进制数据来发送
        }
        context.startActivity(intent); // 调用系统的mail客户端进行发送
    }

}
