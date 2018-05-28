package com.hongyun.cordova.plugin.encode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Created by Administrator on 2017/7/13.
 */

public class Encode extends CordovaPlugin {

    private CallbackContext currentCallbackContext;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        currentCallbackContext = callbackContext;
        JSONObject jo = args.getJSONObject(0);
        String url= jo.getString("url");
        int width = jo.getInt("width");
        int height = jo.getInt("height");
        String fileName = createCodeImage(this.cordova.getActivity(),url,width,height);
        callbackContext.success(fileName);
        return true;

    }

    //1更具url产生二维码
    public Bitmap create2DCodeOld(String str,int _width,int _height) throws WriterException {
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix = (new MultiFormatWriter()).encode(str, BarcodeFormat.QR_CODE, _width, _height, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for(int bitmap = 0; bitmap < height; ++bitmap) {
            for(int x = 0; x < width; ++x) {
                if(matrix.get(x, bitmap)) {
                    pixels[bitmap * width + x] = -16777216;
                }
            }
        }

        Bitmap var8 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        var8.setPixels(pixels, 0, width, 0, 0, width, height);
        return var8;
    }

    public static Bitmap create2DCode(String str,int _width,int _height) throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, _width, _height);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                }else{
                    pixels[y * width + x] = WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    //2将1产生的二维码生成照片
    public String saveImageToGallery(Context context, Bitmap bmp) throws FileNotFoundException,IOException,FileNotFoundException{
        String deviceVersion = Build.VERSION.RELEASE;
        int check = deviceVersion.compareTo("2.3.3");

        File folder;
			/*
			 * File path = Environment.getExternalStoragePublicDirectory(
			 * Environment.DIRECTORY_PICTURES ); //this throws error in Android
			 * 2.2
			 */
        if (check >= 1) {
            folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            if(!folder.exists()) {
                folder.mkdirs();
            }
        } else {
            folder = Environment.getExternalStorageDirectory();
        }

        Calendar c = Calendar.getInstance();
        String date = "" + c.get(Calendar.DAY_OF_MONTH)
                + c.get(Calendar.MONTH)
                + c.get(Calendar.YEAR)
                + c.get(Calendar.HOUR_OF_DAY)
                + c.get(Calendar.MINUTE)
                + c.get(Calendar.SECOND);


        String fileName = "c2i_" + date.toString() + ".png";
        File file = new File(folder, fileName);

        FileOutputStream fos = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();

        // 其次把文件插入到系统图库
        MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.toString())));

        return file.toString();
    }

    //包裹1,2
    public String createCodeImage(Context context,String str,int width,int height){
        Bitmap temp;
        String fileName ="";
        try {
            temp = create2DCode(str,width,height);
            fileName = saveImageToGallery(context,temp);
        } catch (WriterException e) {
            e.printStackTrace();
            currentCallbackContext.error(e.toString());
        }catch (IOException e) {
            e.printStackTrace();
            currentCallbackContext.error(e.toString());
        }
        currentCallbackContext.success(fileName.toString());
        return fileName;
    }
}
