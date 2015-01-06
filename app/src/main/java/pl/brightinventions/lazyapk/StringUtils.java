package pl.brightinventions.lazyapk;

public class StringUtils {
    public static String appendSlashIfNotLast(String address) {
        if (address.endsWith("/")) {
            return address;
        }
        return address + "/";
    }

    public static String removeFirstCharIfSlash(String href) {
        if (href.startsWith("/")) {
            return href.substring(1);
        }
        return href;
    }
}
