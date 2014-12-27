package pl.brightinventions.lazyapk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import pl.brightinventions.lazyapk.drawable.TextDrawable;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class CalligraphyAwareMenu implements Menu {
    private final Menu menu;
    private final Context context;
    private final Typeface typeface;

    public CalligraphyAwareMenu(Menu menu, Context context) {
        this.menu = menu;
        this.context = context;
        final AssetManager assetManager = context.getAssets();
        this.typeface = TypefaceUtils.load(assetManager, "FontAwesome.otf");
    }

    @Override
    public MenuItem add(CharSequence title) {
        return updateTitleIfOneLetter(menu.add(title), title);
    }

    private MenuItem updateTitleIfOneLetter(MenuItem add, CharSequence title) {
        if (isOneLetter(title) && add.getIcon() == null) {
            add.setTitle(null);
            TextDrawable icon = new TextDrawable(context);
            icon.setTypeface(typeface);
            icon.setText(title);
            icon.setTextColor(Color.WHITE);
            add.setIcon(icon);
        }
        return add;
    }

    private boolean isOneLetter(CharSequence title) {
        return !TextUtils.isEmpty(title) && title.length() == 1;
    }

    @Override
    public MenuItem add(int titleRes) {
        return updateTitleIfOneLetter(menu.add(titleRes), context.getString(titleRes));
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        return updateTitleIfOneLetter(menu.add(groupId, itemId, order, title), title);
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return updateTitleIfOneLetter(menu.add(groupId, itemId, order, titleRes), context.getString(titleRes));
    }

    @Override
    public SubMenu addSubMenu(CharSequence title) {
        return updateTitleIfOneLetter(menu.addSubMenu(title), title);
    }

    private SubMenu updateTitleIfOneLetter(SubMenu subMenu, CharSequence title) {
        if (isOneLetter(title) ) {
            subMenu.setHeaderTitle(CalligraphyUtils.applyTypefaceSpan(title, typeface));
        }
        return subMenu;
    }

    @Override
    public SubMenu addSubMenu(int titleRes) {
        return updateTitleIfOneLetter(menu.addSubMenu(titleRes), context.getString(titleRes));
    }

    @Override
    public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        return updateTitleIfOneLetter(menu.addSubMenu(groupId, itemId, order, title), title);
    }

    @Override
    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        return updateTitleIfOneLetter(menu.addSubMenu(groupId, itemId, order, titleRes), context.getString(titleRes));
    }

    @Override
    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        return menu.addIntentOptions(groupId, itemId, order, caller, specifics, intent, flags, outSpecificItems);
    }

    @Override
    public void removeItem(int id) {
        menu.removeItem(id);
    }

    @Override
    public void removeGroup(int groupId) {
        menu.removeGroup(groupId);
    }

    @Override
    public void clear() {
        menu.clear();
    }

    @Override
    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
        menu.setGroupCheckable(group, checkable, exclusive);
    }

    @Override
    public void setGroupVisible(int group, boolean visible) {
        menu.setGroupVisible(group, visible);
    }

    @Override
    public void setGroupEnabled(int group, boolean enabled) {
        menu.setGroupEnabled(group, enabled);
    }

    @Override
    public boolean hasVisibleItems() {
        return menu.hasVisibleItems();
    }

    @Override
    public MenuItem findItem(int id) {
        return menu.findItem(id);
    }

    @Override
    public int size() {
        return menu.size();
    }

    @Override
    public MenuItem getItem(int index) {
        return menu.getItem(index);
    }

    @Override
    public void close() {
        menu.close();
    }

    @Override
    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        return menu.performShortcut(keyCode, event, flags);
    }

    @Override
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return menu.isShortcutKey(keyCode, event);
    }

    @Override
    public boolean performIdentifierAction(int id, int flags) {
        return menu.performIdentifierAction(id, flags);
    }

    @Override
    public void setQwertyMode(boolean isQwerty) {
        menu.setQwertyMode(isQwerty);
    }
}
