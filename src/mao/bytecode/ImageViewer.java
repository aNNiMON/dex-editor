package mao.bytecode;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;


public class ImageViewer extends Activity {
    
    public static final String DATA_EXTRA = "byte_array_data";
    public static final String FILE_PATH_EXTRA = "file_path_data";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image);
        
        Bitmap bmp;

        byte[] data = getIntent().getByteArrayExtra(DATA_EXTRA);
        if (data == null) {
            String filePath = getIntent().getStringExtra(FILE_PATH_EXTRA);
            bmp = BitmapFactory.decodeFile(filePath);
        } else {
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        
        if (bmp == null) return;
        
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageBitmap(bmp);
    }

}
