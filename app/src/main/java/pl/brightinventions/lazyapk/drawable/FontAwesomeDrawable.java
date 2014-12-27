package pl.brightinventions.lazyapk.drawable;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class FontAwesomeDrawable extends TextDrawable {
    public FontAwesomeDrawable(Context context) {
        super(context);
        final AssetManager assetManager = context.getAssets();
        final Typeface typeface = TypefaceUtils.load(assetManager, "FontAwesome.otf");
        setTypeface(typeface);
    }

    public static FontAwesomeDrawable chevronLeft(Context context) {
        return fromText(context, "");
    }
    public static FontAwesomeDrawable arrowLeft(Context context) {
        return fromText(context, "");
    }
    public static FontAwesomeDrawable plus(Context context) {
        return fromText(context, "");
    }
    public static FontAwesomeDrawable save(Context context) {
        return fromText(context, "");
    }
    public static FontAwesomeDrawable cancel(Context context) {
        return fromText(context, "");
    }
    public static FontAwesomeDrawable trash(Context context) {
        return fromText(context, "");
    }

    private static FontAwesomeDrawable fromText(Context context, String text) {
        FontAwesomeDrawable textDrawable = new FontAwesomeDrawable(context);
        textDrawable.setText(text);
        return textDrawable;
    }
}
