package mao.bytecode;

import android.widget.EditText;
import android.os.Bundle;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.util.Log;
import android.text.method.KeyListener;
import android.text.ClipboardManager;
import android.text.Selection;
import android.text.Spannable;
import android.text.NoCopySpan;
import android.text.Editable;
import android.text.method.MetaKeyKeyListener;
import android.text.method.TransformationMethod;

import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;

import org.jf.dexlib.Util.Utf8Utils;

public class MyEdit extends EditText{
    public static final String TAG="MyEdit";
    public static  final int INSERT=0;
    public static  final int NORMAL=1;
    public static  final int VISUAL=2;

    private int mod=NORMAL;
    
    private static final int ID_SELECT_ALL = android.R.id.selectAll;
    private static final int ID_START_SELECTING_TEXT = android.R.id.startSelectingText;
    private static final int ID_STOP_SELECTING_TEXT = android.R.id.stopSelectingText;
    private static final int ID_CUT = android.R.id.cut;
    private static final int ID_COPY = android.R.id.copy;
    private static final int ID_PASTE = android.R.id.paste;
    private static final int ID_COPY_URL = android.R.id.copyUrl;
    private static final int ID_SWITCH_INPUT_METHOD = android.R.id.switchInputMethod;
    private static final int ID_ADD_TO_DICTIONARY = android.R.id.addToDictionary;



    public MyEdit(Context context){
        this(context,null);
    }

    public MyEdit(Context context,AttributeSet attr){
        super(context,attr);
    }

    public MyEdit(Context context,AttributeSet attr,int a){
        super(context,attr,a);
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        Log.e(TAG,"onKeyDown "+keyCode);

        if(keyCode==KeyEvent.KEYCODE_DEL&&handlerDel()){
            return true;
        }
        if(mod==NORMAL&&handleKey(keyCode)){
            return true;
        }

        return super.onKeyDown(keyCode,event);
    }


    private boolean handlerDel(){
        CharSequence charSeq=getText();
        int start=getSelectionStart();
        int end=getSelectionEnd();
        if(start==end&&charSeq.length()!=0){
            char c=charSeq.charAt(start>0?start-1:start);
            if(c=='\n')
                return true;
        }

        CharSequence subSeq=charSeq.subSequence(Math.min(start,end),Math.max(start,end));
        if(subSeq.toString().indexOf('\n')!=-1){
            return true;
        }
        return false;
    }

    private boolean handleKey(int keyCode){
        switch(keyCode){
            case 'h':
                KeyEvent left=new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_LEFT);
                super.onKeyDown(left.getKeyCode(),left);
                return true;
            case 'j':
            case KeyEvent.KEYCODE_ENTER:
                KeyEvent down=new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN);
                super.onKeyDown(down.getKeyCode(),down);
                return true;
            case 'k':
                KeyEvent up=new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP);
                super.onKeyDown(up.getKeyCode(),up);
                return true;
            case 'l':
                KeyEvent right=new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT);
                super.onKeyDown(right.getKeyCode(),right);
                return true;
        }
        return false;
    }
    
    @Override
    public boolean onKeyUp(int keyCode,KeyEvent event){
        Log.e(TAG,"onKeyUp "+keyCode);

        return super.onKeyUp(keyCode,event);
    }
    public void setMod(int mod){
        this.mod=mod;
    }
    @Override
    public InputConnection onCreateInputConnection(EditorInfo edit){
        final InputConnection  ic=super.onCreateInputConnection(edit);
        return new InputConnection(){
            public boolean beginBatchEdit() {
                Log.w(TAG, "beginBatchEdit");
                return ic.beginBatchEdit();
            }
            public boolean clearMetaKeyStates(int arg0) {
                Log.w(TAG, "clearMetaKeyStates " + arg0);
                return ic.clearMetaKeyStates(arg0);
            }

            public boolean commitCompletion(CompletionInfo arg0) {
                Log.w(TAG, "commitCompletion " + arg0);
                return ic.commitCompletion(arg0);
            }

            public boolean endBatchEdit() {
                Log.w(TAG, "endBatchEdit");
                return ic.endBatchEdit();
            }

            public boolean finishComposingText() {
                Log.w(TAG, "finishComposingText");
                return ic.finishComposingText();
            }

            public int getCursorCapsMode(int arg0) {
                Log.w(TAG, "getCursorCapsMode(" + arg0 + ")");
                return ic.getCursorCapsMode(arg0);
            }

            public ExtractedText getExtractedText(ExtractedTextRequest arg0,
                    int arg1) {
                Log.w(TAG, "getExtractedText" + arg0 + "," + arg1);
                return ic.getExtractedText(arg0,arg1);
            }

            public CharSequence getTextAfterCursor(int n, int flags) {
                Log.w(TAG, "getTextAfterCursor(" + n + "," + flags + ")");
                return ic.getTextAfterCursor(n,flags);
            }

            public CharSequence getTextBeforeCursor(int n, int flags) {
                Log.w(TAG, "getTextBeforeCursor(" + n + "," + flags + ")");
                return ic.getTextBeforeCursor(n,flags);
            }

            public boolean performContextMenuAction(int arg0) {
                Log.w(TAG, "performContextMenuAction" + arg0);
                return ic.performContextMenuAction(arg0);
            }

            public boolean performPrivateCommand(String arg0, Bundle arg1) {
                Log.w(TAG, "performPrivateCommand" + arg0 + "," + arg1);
                return ic.performPrivateCommand(arg0,arg1);
            }

            public boolean reportFullscreenMode(boolean arg0) {
                Log.w(TAG, "reportFullscreenMode" + arg0);
                return ic.reportFullscreenMode(arg0);
            }

            public boolean commitText(CharSequence text, int newCursorPosition) {
                Log.w(TAG, "commitText(\"" + text + "\", " + newCursorPosition + ")");
                CharSequence charSeq=getText();
                int start=getSelectionStart();
                int end=getSelectionEnd();
                if(start != end){

                    CharSequence subSeq=charSeq.subSequence(Math.min(start,end),Math.max(start,end));
                    if(subSeq.toString().indexOf('\n')!=-1){
                        return true;
                    }
                }

                return ic.commitText(text,newCursorPosition);
            }

            public boolean deleteSurroundingText(int leftLength, int rightLength) {
                Log.w(TAG, "deleteSurroundingText(" + leftLength +
                        "," + rightLength + ")");
                return ic.deleteSurroundingText(leftLength,rightLength);
            }

            public boolean performEditorAction(int actionCode) {
                Log.w(TAG, "performEditorAction(" + actionCode + ")");
                return ic.performEditorAction(actionCode);
            }

            public boolean sendKeyEvent(KeyEvent event) {
                Log.w(TAG, "sendKeyEvent(" + event + ")");
                return ic.sendKeyEvent(event);
            }

            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                Log.w(TAG, "setComposingText(\"" + text + "\", " + newCursorPosition + ")");
                return ic.setComposingText(text,newCursorPosition);
            }

            public boolean setSelection(int start, int end) {
                Log.w(TAG, "setSelection" + start + "," + end);
                return ic.setSelection(start,end);
            }
            
            /*
            public boolean setComposingRegion(int a, int b) {
                Log.w(TAG, "setComposingRegion(\"" + "\", " +  ")");
                return true;
            }
            */

        };
    }

    @Override
    public boolean onTextContextMenuItem(int id) {

        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();
        CharSequence text=getText();

        if (!isFocused()) {
            selStart = 0;
            selEnd = text.length();
        }

        int min = Math.min(selStart, selEnd);
        int max = Math.max(selStart, selEnd);

        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }

        ClipboardManager clip = (ClipboardManager)getContext()
            .getSystemService(Context.CLIPBOARD_SERVICE);
        Object SELECTING=new NoCopySpan.Concrete();

        switch (id) {
            case ID_SELECT_ALL:
                super.onTextContextMenuItem(ID_SELECT_ALL);
                return true;

            case ID_START_SELECTING_TEXT:
                super.onTextContextMenuItem(ID_START_SELECTING_TEXT);
                return true;

            case ID_STOP_SELECTING_TEXT:
                super.onTextContextMenuItem(ID_STOP_SELECTING_TEXT);
                return true;

            case ID_CUT:
                ((Spannable) text).removeSpan(SELECTING);

                if (min == max) {
                    min = 0;
                    max = text.length();
                }

                TransformationMethod transformation=getTransformationMethod();
                CharSequence transformed;
                if(transformation==null){
                    transformed=text;
                }else{
                    transformed=transformation.getTransformation(text,this);
                }
                if(transformed.subSequence(min,max).toString().indexOf('\n') ==-1){
                    clip.setText(transformed.subSequence(min, max));
                    ((Editable) text).delete(min, max);
                }
                return true;

            case ID_COPY:
                super.onTextContextMenuItem(ID_COPY);
                return true;

            case ID_PASTE:
                ((Spannable) text).removeSpan(SELECTING);

                CharSequence paste = clip.getText();

                if (paste != null) {
                    Selection.setSelection((Spannable) text, max);
                    ((Editable) text).replace(min, max, Utf8Utils.escapeString(paste.toString()));
                }

                return true;

            case ID_COPY_URL:
                super.onTextContextMenuItem(ID_COPY_URL);
                return true;

            case ID_SWITCH_INPUT_METHOD:
                super.onTextContextMenuItem(ID_SWITCH_INPUT_METHOD);
                return true;

            case ID_ADD_TO_DICTIONARY:
                super.onTextContextMenuItem(ID_ADD_TO_DICTIONARY);
                return true;
        }

        return false;
    }

  
}
