

package xtrem.download.mobile.ui.browser.bookmarks;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import xtrem.download.mobile.R;
import xtrem.download.mobile.core.model.data.entity.BrowserBookmark;
import xtrem.download.mobile.databinding.DialogEditBookmarkBinding;
import xtrem.download.mobile.ui.FragmentCallback;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static xtrem.download.mobile.ui.browser.bookmarks.EditBookmarkActivity.TAG_BOOKMARK;
import static xtrem.download.mobile.ui.browser.bookmarks.EditBookmarkActivity.TAG_RESULT_ACTION_APPLY_CHANGES;
import static xtrem.download.mobile.ui.browser.bookmarks.EditBookmarkActivity.TAG_RESULT_ACTION_APPLY_CHANGES_FAILED;
import static xtrem.download.mobile.ui.browser.bookmarks.EditBookmarkActivity.TAG_RESULT_ACTION_DELETE_BOOKMARK;
import static xtrem.download.mobile.ui.browser.bookmarks.EditBookmarkActivity.TAG_RESULT_ACTION_DELETE_BOOKMARK_FAILED;

public class EditBookmarkDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = EditBookmarkDialog.class.getSimpleName();

    private AlertDialog alert;
    private AppCompatActivity activity;
    private DialogEditBookmarkBinding binding;
    private EditBookmarkViewModel viewModel;
    private CompositeDisposable disposables = new CompositeDisposable();

    public static EditBookmarkDialog newInstance(@NonNull BrowserBookmark bookmark)
    {
        EditBookmarkDialog frag = new EditBookmarkDialog();

        Bundle args = new Bundle();
        args.putParcelable(TAG_BOOKMARK, bookmark);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        if (context instanceof AppCompatActivity)
            activity = (AppCompatActivity)context;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        /* Back button handle */
        getDialog().setOnKeyListener((DialogInterface dialog, int keyCode, KeyEvent event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return true;
                } else {
                    onBackPressed();
                    return true;
                }
            } else {
                return false;
            }
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();

        disposables.clear();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (activity == null)
            activity = (AppCompatActivity)getActivity();

        viewModel = new ViewModelProvider(this).get(EditBookmarkViewModel.class);

        LayoutInflater i = LayoutInflater.from(activity);
        binding = DataBindingUtil.inflate(i, R.layout.dialog_edit_bookmark, null, false);

        BrowserBookmark bookmark = getArguments().getParcelable(TAG_BOOKMARK);
        if (bookmark != null) {
            binding.name.setText(bookmark.name);
            binding.url.setText(bookmark.url);
        }
        binding.url.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                binding.layoutUrl.setErrorEnabled(false);
                binding.layoutUrl.setError(null);
            }
        });
        binding.name.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                binding.layoutName.setErrorEnabled(false);
                binding.layoutName.setError(null);
            }
        });

        alert = new AlertDialog.Builder(activity)
                .setTitle(R.string.browser_edit_bookmark)
                .setPositiveButton(R.string.apply, null)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.delete, null)
                .setView(binding.getRoot())
                .create();
        alert.setCanceledOnTouchOutside(false);

        alert.setOnShowListener((dialog) -> {
            Button addUpdateButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            Button cancelButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
            Button deleteButton = alert.getButton(AlertDialog.BUTTON_NEUTRAL);
            addUpdateButton.setOnClickListener((__) -> applyChanges());
            cancelButton.setOnClickListener((__) -> finish(new Intent(), FragmentCallback.ResultCode.CANCEL));
            deleteButton.setOnClickListener((__) -> deleteBookmark());
        });

        return alert;
    }

    private boolean checkUrlField()
    {
        if (TextUtils.isEmpty(binding.url.getText())) {
            binding.layoutUrl.setErrorEnabled(true);
            binding.layoutUrl.setError(getString(R.string.browser_bookmark_error_empty_url));
            binding.layoutUrl.requestFocus();

            return false;
        }

        binding.layoutUrl.setErrorEnabled(false);
        binding.layoutUrl.setError(null);

        return true;
    }

    private boolean checkNameField()
    {
        if (TextUtils.isEmpty(binding.name.getText())) {
            binding.layoutName.setErrorEnabled(true);
            binding.layoutName.setError(getString(R.string.browser_bookmark_error_empty_name));
            binding.layoutName.requestFocus();

            return false;
        }

        binding.layoutName.setErrorEnabled(false);
        binding.layoutName.setError(null);

        return true;
    }

    private void applyChanges()
    {
        if (!(checkNameField() & checkUrlField()))
            return;

        BrowserBookmark oldBookmark = getArguments().getParcelable(TAG_BOOKMARK);
        if (oldBookmark == null)
            return;
        BrowserBookmark bookmark = new BrowserBookmark(oldBookmark);
        Editable name = binding.name.getText();
        if (name != null)
            bookmark.name = name.toString();
        Editable url = binding.url.getText();
        if (url != null)
            bookmark.url = url.toString();

        disposables.add(viewModel.applyChanges(oldBookmark, bookmark)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> onChangesApplied(bookmark),
                        (e) -> onApplyChangesFailed(bookmark, e))
        );
    }

    private void onChangesApplied(BrowserBookmark bookmark)
    {
        Intent i = new Intent(TAG_RESULT_ACTION_APPLY_CHANGES);
        i.putExtra(TAG_BOOKMARK, bookmark);
        finish(i, FragmentCallback.ResultCode.OK);
    }

    private void onApplyChangesFailed(BrowserBookmark bookmark, Throwable e)
    {
        Log.e(TAG, Log.getStackTraceString(e));

        Intent i = new Intent(TAG_RESULT_ACTION_APPLY_CHANGES_FAILED);
        i.putExtra(TAG_BOOKMARK, bookmark);
        finish(i, FragmentCallback.ResultCode.OK);
    }

    private void deleteBookmark()
    {
        BrowserBookmark bookmark = getArguments().getParcelable(TAG_BOOKMARK);
        if (bookmark == null)
            return;

        disposables.add(viewModel.deleteBookmark(bookmark)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((__) -> onBookmarkDeleted(bookmark),
                        (e) -> onBookmarkDeleteFailed(bookmark, e))
        );
    }

    private void onBookmarkDeleted(BrowserBookmark bookmark)
    {
        Intent i = new Intent(TAG_RESULT_ACTION_DELETE_BOOKMARK);
        i.putExtra(TAG_BOOKMARK, bookmark);
        finish(i, FragmentCallback.ResultCode.OK);
    }

    private void onBookmarkDeleteFailed(BrowserBookmark bookmark, Throwable e)
    {
        Log.e(TAG, Log.getStackTraceString(e));

        Intent i = new Intent(TAG_RESULT_ACTION_DELETE_BOOKMARK_FAILED);
        i.putExtra(TAG_BOOKMARK, bookmark);
        finish(i, FragmentCallback.ResultCode.OK);
    }

    public void onBackPressed()
    {
        finish(new Intent(), FragmentCallback.ResultCode.BACK);
    }

    private void finish(Intent intent, FragmentCallback.ResultCode code)
    {
        alert.dismiss();
        ((FragmentCallback)activity).fragmentFinished(intent, code);
    }
}
