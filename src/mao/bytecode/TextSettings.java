package mao.bytecode;

import android.content.SharedPreferences;

/**
 * Terminal emulator settings
 */
public class TextSettings {
    private SharedPreferences mPrefs;

    public boolean mLineWrap;
    public int mFontSize = 14;
    public int mFontColor=0xFF000000;
    public int mBgColor = 0xffcccccc;
    public String mFontType="Sans Serif";
    private static final String LINEWRAP = "linewrap";
    private static final String FONTSIZE = "fontsize";
    private static final String FONTTYPE = "fonttype";

    private static final String FONTCOLOR = "fontcolor";
    private static final String BGCOLOR = "bgcolor";

   public TextSettings(SharedPreferences prefs) {
        mPrefs = prefs;
    }

    public void readPrefs(SharedPreferences mPrefs) {
        mLineWrap=readBooleanPref(LINEWRAP,false);

        mFontType=readStringPref(FONTTYPE,mFontType);
        mFontSize = readIntPref(FONTSIZE, mFontSize, 24);
        mFontColor = mPrefs.getInt(FONTCOLOR,mFontColor);
        mBgColor = mPrefs.getInt(BGCOLOR,mBgColor);
    }

    private int readIntPref(String key, int defaultValue, int maxValue) {
        int val;
        try {
            val = Integer.parseInt(
                mPrefs.getString(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            val = defaultValue;
        }
        val = Math.max(0, Math.min(val, maxValue));
        return val;
    }

    
    private String readStringPref(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }

    private boolean readBooleanPref(String key, boolean defaultValue) {
        return mPrefs.getBoolean(key, defaultValue);
    }


    public int getFontSize() {
        return mFontSize;
    }




}
