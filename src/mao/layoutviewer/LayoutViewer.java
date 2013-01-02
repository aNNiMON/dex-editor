package mao.layoutviewer;

import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class LayoutViewer extends Activity {
    
    public static final String DATA_EXTRA = "byte_array_data";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String data;
        try {
            AndroidXml androidXml = AndroidXml.readFromArray(getIntent().getByteArrayExtra(DATA_EXTRA));
            data = androidXml.getText();
        } catch (IOException ex) {
            data = null;
        }
        
        if (data == null) return;
        
        ViewInflater inflater = new ViewInflater(this);
        XmlPullParser parse;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parse = factory.newPullParser();
            parse.setInput(new StringReader(data));
            View v = inflater.inflate(parse);
            setContentView(v);
        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
