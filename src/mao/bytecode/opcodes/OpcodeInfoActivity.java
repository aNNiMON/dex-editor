package mao.bytecode.opcodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mao.bytecode.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

public class OpcodeInfoActivity extends Activity {
    
    private ListView appListView;
    
    private OpcodeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opcodes);
        
        appListView = (ListView) findViewById(android.R.id.list);
        
        final EditText appNameEditText = (EditText) findViewById(R.id.findOpcodeEditText);
        appNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        });
         
        updateProcessList();
    }

    private void updateProcessList() {
        List<String> list = parse(R.xml.opcodes);
        
        adapter = new OpcodeListAdapter(list, getApplicationContext());
        appListView.setAdapter(adapter);
        appListView.setTextFilterEnabled(true);
    }
    
    private List<String> parse(int resource) {
        ArrayList<String> result = new ArrayList<String>();
        
        XmlPullParser parser = getResources().getXml(resource);
        try {
            while (parser.getEventType()!= XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equals("opcode")) {
                    
                    result.add(parser.getAttributeValue(1) + "\n" // name
                             + parser.getAttributeValue(2)); // description
                }
                parser.next();
            }
        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return result;
    }

}
