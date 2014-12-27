package pl.brightinventions.lazyapk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import rx.Observable;
import rx.Subscriber;

public class ViewUtils {

    public static int getWindowHeight(View view){
        Rect outRect = new Rect();
        view.getWindowVisibleDisplayFrame(outRect);
        return outRect.height();
    }

    public static void translateViewOffScreenToBottom(View view) {
//        view.setVisibility(View.INVISIBLE);
        int windowHeight = getWindowHeight(view);
        view.setTranslationY(windowHeight);
//        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                int windowHeight = getWindowHeight(view);
//                if (windowHeight > 0) {
//                    view.setTranslationY(windowHeight - top);
//                    view.setVisibility(View.VISIBLE);
//                    view.removeOnLayoutChangeListener(this);
//                }
//            }
//        });
    }

    public static void clearFocusAndKeyboard(View newSourceForm) {
        View focus = newSourceForm.findFocus();
        if(focus != null){
            InputMethodManager imm =  (InputMethodManager) focus.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            focus.clearFocus();
        }

    }

    public static Observable<Integer> getViewHeight(final View view) {
        int currentHeight = view.getHeight();
        if(currentHeight <= 0){
            return Observable.create(new Observable.OnSubscribe<Integer>() {
                @Override
                public void call(final Subscriber<? super Integer> subscriber) {
                    view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            int height = bottom - top;
                            if(height > 0){
                                view.removeOnLayoutChangeListener(this);
                                subscriber.onNext(height);
                                subscriber.onCompleted();
                            }
                        }
                    });
                }
            });
        } else {
            return Observable.just(currentHeight);
        }
    }

    public static int getDisplayHeight(Context context) {
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
    public static int getDisplayHeight(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
}
