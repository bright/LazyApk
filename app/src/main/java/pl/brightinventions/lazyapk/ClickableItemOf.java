package pl.brightinventions.lazyapk;

import rx.Observable;
import rx.functions.Action1;

public interface ClickableItemOf<T> {
    Observable<T> onClick();

    public static class Helper {
        @SuppressWarnings("unchecked")
        public static <T> void ifClickable(Object object, Action1<ClickableItemOf<T>> clickableItem){
            if(object instanceof ClickableItemOf<?>){
                ClickableItemOf<T> item = (ClickableItemOf<T>) object;
                clickableItem.call(item);
            }
        }
    }
}
