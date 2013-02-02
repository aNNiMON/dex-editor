package mao.layoutviewer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Stack;

import mao.bytecode.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.*;

public class ViewInflater {

    private Stack<ViewGroup> layoutStack;
    private Hashtable<String, Integer> ids;
    private Context context;
    private int idIndex;

    public ViewInflater(Context context) {
        this.layoutStack = new Stack<ViewGroup>();
        this.ids = new Hashtable<String, Integer>();
        this.context = context;
        this.idIndex = 0;
    }

    public View inflate(XmlPullParser parse) throws XmlPullParserException, IOException {
        layoutStack.clear();
        ids.clear();

        Stack<StringBuffer> data = new Stack<StringBuffer>();
        int evt = parse.getEventType();
        View root = null;
        while (evt != XmlPullParser.END_DOCUMENT) {
            switch (evt) {
                case XmlPullParser.START_DOCUMENT:
                    data.clear();
                    break;
                case XmlPullParser.START_TAG:
                    data.push(new StringBuffer());
                    View v = createView(parse);
                    if (v == null) {
                        evt = parse.next();
                        continue;
                    }
                    if (!(v instanceof Button)) {
                        v.setBackgroundResource(R.drawable.view_background);
                    }
                    if (root == null) {
                        root = v;
                    } else {
                        layoutStack.peek().addView(v);
                    }
                    if (v instanceof ViewGroup) {
                        layoutStack.push((ViewGroup) v);
                    }
                    break;
                case XmlPullParser.TEXT:
                    data.peek().append(parse.getText());
                    break;
                case XmlPullParser.END_TAG:
                    data.pop();
                    if (isLayout(parse.getName())) {
                        layoutStack.pop();
                    }
                    break;

            }
            evt = parse.next();
        }
        return root;
    }

    @SuppressWarnings("deprecation")
    private View createView(XmlPullParser parse) {
        String name = parse.getName();
        View result = null;
        AttributeSet atts = Xml.asAttributeSet(parse);
        if (name.equals("LinearLayout")) {
            result = new LinearLayout(context);
        } else if (name.equals("RelativeLayout")) {
            result = new RelativeLayout(context);
        } else if (name.equals("AbsoluteLayout")) {
            result = new AbsoluteLayout(context);
        } else if (name.equals("FrameLayout")) {
            result = new FrameLayout(context);
        } else if (name.equals("GridView")) {
            result = new GridView(context);
        } else if (name.endsWith("ListView")) {
            result = new ListView(context);
        } else if (name.equals("RadioGroup")) {
            result = new RadioGroup(context);
        } else if (name.equals("ScrollView")) {
            result = new ScrollView(context);
        } else if (name.equals("TableRow")) {
            result = new TableRow(context);
        } else if (name.equals("TableLayout")) {
            result = new TableLayout(context);
        } else if (name.equals("TabHost")) {
            result = new TabHost(context);
        }
        
        else if (name.equals("TextView")) {
            result = new TextView(context);
        } else if (name.equals("AutoCompleteTextView")) {
            result = new AutoCompleteTextView(context);
        } else if (name.equals("AnalogClock")) {
            result = new AnalogClock(context);
        } else if (name.equals("Button")) {
            result = new Button(context);
        } else if (name.equals("CheckBox")) {
            result = new CheckBox(context);
        } else if (name.equals("CheckedTextView")) {
            result = new CheckedTextView(context);
        } else if (name.equals("Chronometer")) {
            result = new Chronometer(context);
        } else if (name.equals("CompoundButton")) {
            result = new CompoundButton(context) {};
        } else if (name.equals("DatePicker")) {
            result = new DatePicker(context);
        } else if (name.equals("DigitalClock")) {
            result = new DigitalClock(context);
        } else if (name.equals("EditText")) {
            result = new EditText(context);
        } else if (name.equals("Gallery")) {
            result = new Gallery(context);
        } else if (name.equals("ImageButton")) {
            result = new ImageButton(context);
        } else if (name.equals("ImageView")) {
            result = new ImageView(context);
        } else if (name.equals("HorizontalScrollView")) {
            result = new HorizontalScrollView(context);
        } else if (name.equals("ProgressBar")) {
            result = new ProgressBar(context);
        } else if (name.equals("RadioButton")) {
            result = new RadioButton(context);
        } else if (name.equals("RatingBar")) {
            result = new RatingBar(context);
        } else if (name.equals("SeekBar")) {
            result = new SeekBar(context);
        } else if (name.equals("Spinner")) {
            result = new Spinner(context);
        } else if (name.equals("TimePicker")) {
            result = new TimePicker(context);
        } else if (name.equals("WebView")) {
            result = new WebView(context);
        } else {
            Toast.makeText(context, "Unhandled tag:" + name, Toast.LENGTH_SHORT).show();
        }

        if (result == null) return null;
        
        String id = findAttribute(atts, "android:id");
        if (id != null) {
            int idNumber = lookupId(id);
            if (idNumber > -1) {
                result.setId(idNumber);
            }
        }

        
        if (result instanceof CompoundButton) {
            CompoundButton cb = (CompoundButton) result;
            String checked = findAttribute(atts, "android:checked");
            cb.setChecked("true".equals(checked));
        }
        
        if (result instanceof Chronometer) {
            Chronometer cr = (Chronometer) result;
            String format = findAttribute(atts, "android:format");
            if (format != null) {
                cr.setFormat(format);
            }
        }
        
/*        if (result instanceof DatePicker) {
            DatePicker dp = (DatePicker) result;
            
            String calendarViewShown = findAttribute(atts, "android:calendarViewShown");
            dp.setCalendarViewShown("true".equals(calendarViewShown));
            
            String spinnersShown = findAttribute(atts, "android:spinnersShown");
            dp.setSpinnersShown("true".equals(spinnersShown));
        }
*/        
        if (result instanceof ImageView) {
            ImageView iv = (ImageView) result;
            
            String adjustViewBounds = findAttribute(atts, "android:adjustViewBounds");
            iv.setAdjustViewBounds("true".equals(adjustViewBounds));
            
            String maxWidth = findAttribute(atts, "android:maxWidth");
            if (maxWidth != null) {
                iv.setMaxWidth(readSize(maxWidth));
            }
            
            String maxHeight = findAttribute(atts, "android:maxHeight");
            if (maxHeight != null) {
                iv.setMaxHeight(readSize(maxHeight));
            }
            
            iv.setImageResource(R.drawable.android);
        }
        
        if (result instanceof RatingBar) {
            RatingBar rb = (RatingBar) result;
            
            String isIndicator = findAttribute(atts, "android:isIndicator");
            rb.setIsIndicator("true".equals(isIndicator));
            
            String numStars = findAttribute(atts, "android:numStars");
            if (numStars != null) {
                rb.setNumStars(Integer.parseInt(numStars));
            }
            
            String rating = findAttribute(atts, "android:rating");
            if (rating != null) {
                rb.setRating(Float.parseFloat(rating));
            }
            
            String stepSize = findAttribute(atts, "android:stepSize");
            if (stepSize != null) {
                rb.setStepSize(Float.parseFloat(stepSize));
            }
        }

        if (result instanceof TextView) {
            TextView tv = (TextView) result;
            
            String gravity = findAttribute(atts, "android:gravity");
            if (gravity != null) {
                tv.setGravity(parseGravity(gravity));
            }
            
            String hint = findAttribute(atts, "android:hint");
            if (hint != null) {
                hint = hint.replace("\\n", "\n");
                tv.setHint(hint);
            }
            
            String password = findAttribute(atts, "android:password");
            if ("true".equals(password)) {
                tv.setTransformationMethod(new PasswordTransformationMethod());
            }
            
            String text = findAttribute(atts, "android:text");
            if (text != null) {
                text = text.replace("\\n", "\n");
                tv.setText(text);
            } else tv.setText("text");
            
            String textColor = findAttribute(atts, "android:textColor");
            if ( (textColor != null) && (textColor.startsWith("#")) ) {
                textColor = textColor.substring(1);
                int value;
                try {
                    value = (int)Long.parseLong(textColor, 16);
                } catch (NumberFormatException nfe) {
                    value = Integer.parseInt(textColor);
                }
                tv.setBackgroundColor(value);
            }
            
        }

        if (result instanceof ProgressBar) {
            ProgressBar pb = (ProgressBar) result;
            
            String indet = findAttribute(atts, "android:indeterminate");
            if (indet != null) {
                pb.setIndeterminate("true".equals(indet));
            }
            
            String max = findAttribute(atts, "android:max");
            if (max != null) {
                pb.setMax(parseInt(max));
            }
            
            String progress = findAttribute(atts, "android:progress");
            if (progress != null) {
                pb.setProgress(parseInt(progress));
            }
        }
        
        if (result instanceof GridView) {
            GridView gv = (GridView) result;
            
            String columnWidth = findAttribute(atts, "android:columnWidth");
            if (columnWidth != null) {
                gv.setColumnWidth(readSize(columnWidth));
            }
            
            String numColumns = findAttribute(atts, "android:numColumns");
            if (numColumns != null) {
                int value;
                try {
                    value = Integer.parseInt(numColumns);
                } catch (NumberFormatException nfe) {
                    value = -1; // auto_fit
                }
                gv.setNumColumns(value);
            }
            
            String stretchMode = findAttribute(atts, "android:stretchMode");
            if (stretchMode != null) {
                int value;
                try {
                    value = Integer.parseInt(stretchMode);
                    gv.setStretchMode(value);
                } catch (NumberFormatException nfe) {
                    // do_nothing
                }
            }
        }

        if (result instanceof LinearLayout) {
            LinearLayout ll = (LinearLayout) result;
            String orient = findAttribute(atts, "android:orientation");
            if (orient != null) {
                if (orient.equals("horizontal"))
                    ll.setOrientation(LinearLayout.HORIZONTAL);
                else if (orient.equals("vertical"))
                    ll.setOrientation(LinearLayout.VERTICAL);
            }
        }

        if (result instanceof RadioGroup) {
            RadioGroup rg = (RadioGroup) result;
            String cid = findAttribute(atts, "android:checkedButton");
            if (cid != null) {
                rg.check(parseInt(cid));
            }
        }

        if (result instanceof View) {
            View view = result;
            /* API 11 
              String alpha = findAttribute(atts, "android:alpha");
              if (alpha != null) v.setAlpha(Float.parseFloat(alpha));
            */
            maybeSetBoolean(view, "setClickable", atts, "android:clickable");
            maybeSetBoolean(view, "setFocusable", atts, "android:focusable");
            maybeSetBoolean(view, "setHapticFeedbackEnabled", atts, "android:hapticFeedbackEnabled");
            
            String background = findAttribute(atts, "android:background");
            if ( (background != null) && (background.startsWith("#")) ) {
                background = background.substring(1);
                int value;
                try {
                    value = (int)Long.parseLong(background, 16);
                } catch (NumberFormatException nfe) {
                    value = Integer.parseInt(background);
                }
                view.setBackgroundColor(value);
            }
            
            String minWidth = findAttribute(atts, "android:minWidth");
            if (minWidth != null) {
                view.setMinimumWidth(readSize(minWidth));
            }
            
            String minHeight = findAttribute(atts, "android:minHeight");
            if (minHeight != null) {
                view.setMinimumHeight(readSize(minHeight));
            }

            String visibility = findAttribute(atts, "android:visibility");
            if (visibility != null) {
                int code = -1;
                if ("visible".equals(visibility)) {
                    code = 0;
                } else if ("invisible".equals(visibility)) {
                    code = 1;
                } else if ("gone".equals(visibility)) {
                    code = 2;
                }
                if (code != -1) {
                    view.setVisibility(code);
                }
            }
        }

        if (layoutStack.size() > 0) {
            result.setLayoutParams(loadLayoutParams(atts, layoutStack.peek()));
        }
        return result;
    }

    private boolean maybeSetBoolean(View v, String method, AttributeSet atts, String arg) {
        return maybeSetBoolean(v, method, findAttribute(atts, arg));
    }

    private static boolean isLayout(String name) {
        return name.endsWith("Layout")
                || name.equals("RadioGroup")
                || name.equals("TableRow")
                || name.equals("TabHost")
                || name.equals("GridView")
                || name.equals("Gallery")
                || name.endsWith("ScrollView")
                || name.endsWith("ListView");
    }

    private int lookupId(String id) {
        int ix = id.indexOf('/');
        if (ix != -1) {
            String idName = id.substring(ix + 1);
            Integer n = ids.get(idName);
            if (n == null && id.startsWith("@+")) {
                n = Integer.valueOf(idIndex++);
                ids.put(idName, n);
            }
            if (n != null) return n.intValue();
        }
        return -1;
    }

    private String findAttribute(AttributeSet atts, String id) {
        for (int i = 0; i < atts.getAttributeCount(); i++) {
            if (atts.getAttributeName(i).equals(id)) {
                return atts.getAttributeValue(i);
            }
        }
        int ix = id.indexOf(':');
        if (ix != -1) {
            return atts.getAttributeValue("http://schemas.android.com/apk/res/android", id.substring(ix + 1));
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private ViewGroup.LayoutParams loadLayoutParams(AttributeSet atts, ViewGroup vg) {
        ViewGroup.LayoutParams lps = null;

        String width = findAttribute(atts, "android:layout_width");
        String height = findAttribute(atts, "android:layout_height");
        int w = readSize(width);
        int h = readSize(height);

        if (vg instanceof RadioGroup) {
            lps = new RadioGroup.LayoutParams(w, h);
        } else if (vg instanceof TableRow) {
            lps = new TableRow.LayoutParams();
        } else if (vg instanceof TableLayout) {
            lps = new TableLayout.LayoutParams();
        } else if (vg instanceof LinearLayout) {
            lps = new LinearLayout.LayoutParams(w, h);
        } else if (vg instanceof AbsoluteLayout) {
            String x = findAttribute(atts, "android:layout_x");
            String y = findAttribute(atts, "android:layout_y");

            lps = new AbsoluteLayout.LayoutParams(w, h, readSize(x), readSize(y));
        } else if (vg instanceof RelativeLayout) {
            lps = new RelativeLayout.LayoutParams(w, h);
        } else if (vg instanceof ScrollView) {
            lps = new ScrollView.LayoutParams(w, h);
        } else if (vg instanceof ListView) {
            lps = new ListView.LayoutParams(w, h);
        } else if (vg instanceof FrameLayout) {
            lps = new FrameLayout.LayoutParams(w, h);
        }

        if (lps instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams l = (LinearLayout.LayoutParams) lps;
            String gravity = findAttribute(atts, "android:layout_gravity");
            if (gravity != null) {
                l.gravity = parseGravity(gravity);
                //l.gravity = Integer.parseInt(gravity);
            }

            String weight = findAttribute(atts, "android:layout_weight");
            if (weight != null) {
                l.weight = Float.parseFloat(weight);
            }
            lps = l;
        }

        if (lps instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams) lps;
            for (int i = 0; i < relative_strings.length; i++) {
                String id = findAttribute(atts, relative_strings[i]);
                if (id != null) {
                    int idN = lookupId(id);
                    l.addRule(relative_verbs[i], idN);
                }
            }
            // Margin handling
            // Contributed by Vishal Choudhary - Thanks!
            String bottom = findAttribute(atts, "android:layout_marginBottom");
            String left = findAttribute(atts, "android:layout_marginLeft");
            String right = findAttribute(atts, "android:layout_marginRight");
            String top = findAttribute(atts, "android:layout_marginTop");
            int bottomInt = 0, leftInt = 0, rightInt = 0, topInt = 0;
            if (bottom != null) bottomInt = readSize(bottom);
            if (left != null) leftInt = readSize(left);
            if (right != null) rightInt = readSize(right);
            if (top != null) topInt = readSize(top);

            l.setMargins(leftInt, topInt, rightInt, bottomInt);
        }

        return lps;
    }

    private int readSize(String val) {
        if ("wrap_content".equals(val)) {
            return ViewGroup.LayoutParams.WRAP_CONTENT;
        } else if ("fill_parent".equals(val)) {
            return ViewGroup.LayoutParams.MATCH_PARENT;
        } else if ("match_parent".equals(val)) {
            return ViewGroup.LayoutParams.MATCH_PARENT;
        } else if (val != null) {
            int ix = val.indexOf("px");
            if (ix != -1) return parseInt(val.substring(0, ix));
            
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            
            ix = val.indexOf("dp");
            if (ix != -1) {
                return (int) (parseInt(val.substring(0, ix)) * dm.density + 0.5); 
            }
            
            ix = val.indexOf("sp");
            if (ix != -1) {
                return (int) (parseInt(val.substring(0, ix)) * dm.density + 0.5); 
            }
            
            ix = val.indexOf("pt");
            if (ix != -1) {
                int dp = parseInt(val.substring(0, ix)) * 96 / 72;
                return (int) (dp * dm.density + 0.5); 
            }
            
            return parseInt(val);
        } else {
            return ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    private boolean maybeSetBoolean(View view, String method, String value) {
        if (value == null) {
            return false;
        }
        value = value.toLowerCase();
        Boolean boolValue;
        if ("true".equals(value)) {
            boolValue = Boolean.TRUE;
        } else if ("false".equals(value)) {
            boolValue = Boolean.FALSE;
        } else {
            return false;
        }
        try {
            Method m = View.class.getMethod(method, boolean.class);
            m.invoke(view, boolValue);
            return true;
        } catch (NoSuchMethodException ex) {
            Log.e("ViewInflater", "No such method: " + method, ex);
        } catch (IllegalArgumentException e) {
            Log.e("ViewInflater", "Call", e);
        } catch (IllegalAccessException e) {
            Log.e("ViewInflater", "Call", e);
        } catch (InvocationTargetException e) {
            Log.e("ViewInflater", "Call", e);
        }
        return false;
    }
    
    private int parseGravity(String gravity) {
        int grav = Gravity.NO_GRAVITY;
        if (gravity.indexOf("top") != -1) grav |= Gravity.TOP;
        else if (gravity.indexOf("bottom") != -1) grav |= Gravity.BOTTOM;
        else if (gravity.indexOf("left") != -1) grav |= Gravity.LEFT;
        else if (gravity.indexOf("right") != -1) grav |= Gravity.RIGHT;
        else if (gravity.indexOf("center_vertical") != -1) grav |= Gravity.CENTER_VERTICAL;
        else if (gravity.indexOf("center_horizontal") != -1) grav |= Gravity.CENTER_HORIZONTAL;
        else if (gravity.indexOf("clip_vertical") != -1) grav |= Gravity.CLIP_VERTICAL;
        else if (gravity.indexOf("clip_horizontal") != -1) grav |= Gravity.CLIP_HORIZONTAL;
        else if (gravity.indexOf("fill_vertical") != -1) grav |= Gravity.FILL_VERTICAL;
        else if (gravity.indexOf("fill_horizontal") != -1) grav |= Gravity.FILL_HORIZONTAL;
//        else if (gravity.indexOf("start") != -1) grav |= Gravity.START;
//        else if (gravity.indexOf("end") != -1) grav |= Gravity.END;
        else {
             if ( (gravity.indexOf("center_") == -1) && (gravity.indexOf("center") != -1) ) grav |= Gravity.CENTER;
             if ( (gravity.indexOf("fill_") == -1) && (gravity.indexOf("fill") != -1) ) grav |= Gravity.FILL;
        }
        return grav;
    }
    
    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException numberFormatException) {
            return (int) Float.parseFloat(str);
        }
    }
    
    private static final String[] relative_strings = {
        "android:layout_above",
        "android:layout_alignBaseline",
        "android:layout_alignBottom",
        "android:layout_alignLeft",
        "android:layout_alignParentBottom",
        "android:layout_alignParentLeft",
        "android:layout_alignParentRight",
        "android:layout_alignParentTop",
        "android:layout_alignRight",
        "android:layout_alignTop",
        "android:layout_below",
        "android:layout_centerHorizontal",
        "android:layout_centerInParent",
        "android:layout_centerVertical",
        "android:layout_toLeft",
        "android:layout_toRight"
    };
    
    private static final int[] relative_verbs = {
        RelativeLayout.ABOVE,
        RelativeLayout.ALIGN_BASELINE,
        RelativeLayout.ALIGN_BOTTOM,
        RelativeLayout.ALIGN_LEFT,
        RelativeLayout.ALIGN_PARENT_BOTTOM,
        RelativeLayout.ALIGN_PARENT_LEFT,
        RelativeLayout.ALIGN_PARENT_RIGHT,
        RelativeLayout.ALIGN_PARENT_TOP,
        RelativeLayout.ALIGN_RIGHT,
        RelativeLayout.ALIGN_TOP,
        RelativeLayout.BELOW,
        RelativeLayout.CENTER_HORIZONTAL,
        RelativeLayout.CENTER_IN_PARENT,
        RelativeLayout.CENTER_VERTICAL,
        RelativeLayout.LEFT_OF,
        RelativeLayout.RIGHT_OF
    };

}
