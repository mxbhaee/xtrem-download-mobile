

package xtrem.download.mobile.ui.browser.bookmarks;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import xtrem.download.mobile.core.model.data.entity.BrowserBookmark;
import xtrem.download.mobile.core.utils.Utils;
import xtrem.download.mobile.ui.FragmentCallback;

public class EditBookmarkActivity extends AppCompatActivity
        implements FragmentCallback
{
    public static final String TAG_BOOKMARK = "bookmark";
    public static final String TAG_RESULT_ACTION_APPLY_CHANGES = "result_action_apply_changes";
    public static final String TAG_RESULT_ACTION_APPLY_CHANGES_FAILED = "result_action_apply_changes_failed";
    public static final String TAG_RESULT_ACTION_DELETE_BOOKMARK = "result_action_delete_bookmark";
    public static final String TAG_RESULT_ACTION_DELETE_BOOKMARK_FAILED = "result_action_delete_bookmark_failed";
    private static final String TAG_EDIT_BOOKMARK_DIALOG = "edit_bookmark_dialog";

    private EditBookmarkDialog editBookmarkDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        setTheme(Utils.getTranslucentAppTheme(getApplicationContext()));
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        editBookmarkDialog = (EditBookmarkDialog)fm.findFragmentByTag(TAG_EDIT_BOOKMARK_DIALOG);
        if (editBookmarkDialog == null) {
            Intent i = getIntent();
            if (i == null)
                throw new NullPointerException("intent is null");
            BrowserBookmark bookmark = i.getParcelableExtra(TAG_BOOKMARK);
            if (bookmark == null)
                throw new NullPointerException("bookmark is null");
            editBookmarkDialog = EditBookmarkDialog.newInstance(bookmark);
            editBookmarkDialog.show(fm, TAG_EDIT_BOOKMARK_DIALOG);
        }
    }

    @Override
    public void fragmentFinished(Intent intent, ResultCode code)
    {
        int resultCode = (intent.getAction() == null || code != ResultCode.OK ?
                RESULT_CANCELED :
                RESULT_OK);

        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        editBookmarkDialog.onBackPressed();
    }
}
