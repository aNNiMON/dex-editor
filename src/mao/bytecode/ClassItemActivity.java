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
public class ClassItemActivity extends ListActivity {

    private ClassItemAdapter mAdapter;
    private List<String> classList;




    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            classList=new ArrayList<String>();

            classList.add("ClassInfo");
            classList.add("Fields");
            classList.add("Methods");
            mAdapter=new ClassItemAdapter(getApplication());
            setListAdapter(mAdapter);
            registerForContextMenu(getListView());

        }
    @Override
        public void onListItemClick(ListView list,View v,int position,long id){
           if( position==1){
               Intent intent=new Intent(this,FieldListActivity.class);
               startActivity(intent);
           }
           else if( position==2){
               Intent intent=new Intent(this,MethodListActivity.class);
               startActivity(intent);
           }
           else if( position==0){
               Intent intent=new Intent(this,ClassInfoEditorActivity.class);
               startActivity(intent);
           }

        }


            
/*
    @Override
        public boolean onCreateOptionsMenu(Menu m){
            MenuInflater in=getMenuInflater();
            in.inflate(R.menu.zip_editor_menu,m);
            return true;
        }
        */


    @Override
        public boolean onOptionsItemSelected(MenuItem mi){
            int id=mi.getItemId();
            switch(id){
                case R.id.add_entry:
                    break;
                case R.id.save_file:


            }
            return true;
        }
    @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

           // menu.add(Menu.NONE, R, Menu.NONE, "Remove");
           // menu.add(Menu.NONE, EXTRACT, Menu.NONE, "Extract");
        }

    @Override
        protected Dialog onCreateDialog(int id) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("abcd");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            return dialog;
        }



    @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo info;
            try {
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            } catch (ClassCastException e) {
                Log.e(e.toString(),"Bad menuInfo");
                return false;
            }
            int id=item.getItemId();
            return true;
        }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
         
            return super.onKeyDown(keyCode,event);
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
            String file=classList.get(position);

            LinearLayout container = (LinearLayout) mInflater.inflate(R.layout.list_item, null);
            ImageView icon = (ImageView) container.findViewById(R.id.list_item_icon);
            switch(position){
                case 0:
                    icon.setImageResource(R.drawable.file);
                    break;
                case 1:
                    icon.setImageResource(R.drawable.field);
                    break;
                case 2:
                    icon.setImageResource(R.drawable.method);
                    break;
            }

            TextView text = (TextView) container.findViewById(R.id.list_item_title);
            text.setText(file);
            return container;
        }
    }
    


}
