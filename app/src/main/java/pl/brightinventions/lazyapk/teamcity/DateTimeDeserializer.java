package pl.brightinventions.lazyapk.teamcity;

import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.Locale;

public class DateTimeDeserializer implements JsonDeserializer {
    private final DateTimeFormatter dateTimeFormat;

    public DateTimeDeserializer(Locale locale) {
        dateTimeFormat = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ").withLocale(locale);
    }

    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String rawValue = json.getAsString();
        if (TextUtils.isEmpty(rawValue)) {
            return null;
        }
        DateTime dateTime = dateTimeFormat.parseDateTime(rawValue);
        return dateTime;
    }
}
