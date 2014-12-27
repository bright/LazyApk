package pl.brightinventions.lazyapk.drawable;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.widget.TextView;

import pl.brightinventions.lazyapk.R;
import pl.brightinventions.lazyapk.sources.SourcesActivity;

public class Icons {
    public static FontAwesomeDrawable navigationUp(Context context) {
        FontAwesomeDrawable drawable = FontAwesomeDrawable.arrowLeft(context);
        standardSizeAndColor(context, drawable);
        return drawable;
    }

    public static FontAwesomeDrawable plus(Context context) {
        FontAwesomeDrawable drawable = FontAwesomeDrawable.plus(context);
        standardSizeAndColor(context, drawable);
        return drawable;
    }

    public static FontAwesomeDrawable save(Context context) {
        FontAwesomeDrawable drawable = FontAwesomeDrawable.save(context);
        standardSizeAndColor(context, drawable);
        return drawable;
    }
    public static FontAwesomeDrawable cancel(Context context) {
        FontAwesomeDrawable drawable = FontAwesomeDrawable.cancel(context);
        standardSizeAndColor(context, drawable);
        return drawable;
    }

    public static FontAwesomeDrawable trash(Context context) {
        FontAwesomeDrawable drawable = FontAwesomeDrawable.trash(context);
        standardSizeAndColor(context, drawable);
        return drawable;
    }

    private static void standardSizeAndColor(Context context, FontAwesomeDrawable drawable) {
        drawable.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen(context, R.dimen.toolbar_icon_size));
        drawable.setTextColor(getColor(context, R.color.text_primary));
    }

    private static ColorStateList getColor(Context context, int colorResId) {
        int color = context.getResources().getColor(colorResId);
        return ColorStateList.valueOf(color);
    }

    private static float getDimen(Context context, int dimenResId) {
        return context.getResources().getDimension(dimenResId);
    }

    public static void left(TextView textView, FontAwesomeDrawable awesomeDrawable) {
        awesomeDrawable.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize());
//        textView.setCompoundDrawablePadding(R.dimen.icon_padding);
        textView.setCompoundDrawablesWithIntrinsicBounds(awesomeDrawable, null, null, null);
    }
}
