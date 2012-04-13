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
public class SearchClassesActivity extends ListActivity {

    private ClassItemAdapter mAdapter;
    public static List<String> classList;




    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(classList ==null){
            classList=new ArrayList<String>();
        }
        mAdapter=new ClassItemAdapter(getApplication());
        setListAdapter(mAdapter);

    }
    @Override
    public void onListItemClick(ListView list,View v,int position,long id){
        ClassListActivity.setCurrnetClass(classList.get(position));
       Intent intent=new Intent(this,ClassItemActivity.class);
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
        classList=null;
        mAdapter=null;
    }

    public static void initClassList(List<String> list){
        classList=list;
    }

    private class ClassItemAdapter extends BaseAdapter {

        protected final Context mContext;
        protected final LayoutInflater mInflater;

        public ClassItemAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return classList.size();
        }

        public Object getItem(int position) {
            return classList.get(position);
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
            icon.setImageResource(R.drawable.clazz);
            TextView text = (TextView) container.findViewById(R.id.list_item_title);
            text.setText(classList.get(position));
            return container;
        }
    }
}
