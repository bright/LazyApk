package pl.brightinventions.lazyapk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ViewUtils.class.getSimpleName());

    public static void clearFocusAndKeyboard(View newSourceForm) {
        View focus = newSourceForm.findFocus();
        if(focus != null){
            InputMethodManager imm =  (InputMethodManager) focus.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            focus.clearFocus();
        }

    }

    public static int getDisplayHeight(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        LOG.trace("Display size {}", size);
        return size.y;
    }
}
