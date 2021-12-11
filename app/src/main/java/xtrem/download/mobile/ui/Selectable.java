package xtrem.download.mobile.ui;

public interface Selectable<T>
{
    T getItemKey(int position);

    int getItemPosition(T key);
}
