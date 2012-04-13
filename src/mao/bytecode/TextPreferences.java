package mao.bytecode;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class TextPreferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
