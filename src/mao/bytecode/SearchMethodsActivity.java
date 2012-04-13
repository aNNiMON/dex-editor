package mao.bytecode;

import android.app.ListActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.content.Intent;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView;
import android.util.Log;
import android.database.DataSetObserver;

import java.text.SimpleDateFormat;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

import org.jf.dexlib.*;
import org.jf.dexlib.ClassDataItem.*;
public class SearchMethodsActivity extends ListActivity {

    private MethodItemAdapter mAdapter;
    private static List<String> methodList;
    private static List<Boolean> isDirectes;
    private static List<Integer> methodIndexes;




    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(methodList ==null){
            methodList=new ArrayList<String>();
        }
        mAdapter=new MethodItemAdapter(getApplication());
        setListAdapter(mAdapter);

    }
    @Override
    public void onListItemClick(ListView list,View v,int position,long id){
       MethodListActivity.setMethodIndex(isDirectes.get(position),methodIndexes.get(position));
       Intent intent=new Intent(this,CodeEditorActivity.class);
       startActivity(intent);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
         
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        clearAll();
    }

    private void clearAll(){
        methodList=null;
        isDirectes=null;
        methodIndexes=null;
        mAdapter=null;
    }

    public static void initMethodList(List<String> list,List<Boolean> isDirect,List<Integer> methodIndex){
        methodList=list;
        isDirectes=isDirect;
        methodIndexes=methodIndex;
    }

    private class MethodItemAdapter extends BaseAdapter {

        protected final Context mContext;
        protected final LayoutInflater mInflater;

        public MethodItemAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return methodList.size();
        }

        public Object getItem(int position) {
            return methodList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }



        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout container;
            if(convertView ==null){
            container= (LinearLayout) mInflater.inflate(R.layout.list_item, null);
            }else{
                container=(LinearLayout)convertView;
            }
            ImageView icon = (ImageView) container.findViewById(R.id.list_item_icon);
            icon.setImageResource(R.drawable.method);
            TextView text = (TextView) container.findViewById(R.id.list_item_title);
            text.setText(methodList.get(position));
            return container;
        }
    }
}
