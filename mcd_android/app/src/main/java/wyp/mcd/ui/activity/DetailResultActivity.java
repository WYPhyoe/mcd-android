/*
 * Copyright 2019 Wai Yan (TechBase Software). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wyp.mcd.ui.activity;

import android.annotation.SuppressLint;

import androidx.lifecycle.ViewModelProviders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.view.View;
import android.widget.ScrollView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import wyp.mcd.R;
import wyp.mcd.component.ui.BasicActivity;
import wyp.mcd.component.util.Logger;
import wyp.mcd.component.util.Rabbit;
import wyp.mcd.component.util.SystemFontChecker;
import wyp.mcd.component.util.WebViewUtil;
import wyp.mcd.infrastructure.entities.BookmarksEntity;
import wyp.mcd.component.ui.Alerter;
import wyp.mcd.component.ui.ExpandableTextView;
import wyp.mcd.component.ui.PopupMenu;
import wyp.mcd.component.ui.ToastView;
import wyp.mcd.viewmodel.BookmarksViewModel;

@SuppressWarnings("deprecation")
public class DetailResultActivity extends BasicActivity
        implements TextToSpeech.OnInitListener, PopupMenu.PopupMenuListener {

    private static final String APP_LINK = "\nhttps://goo.gl/NPRPgx";

    @BindView(R.id.btnPronunciation)
    AppCompatImageButton btnPronunciation;

    @BindView(R.id.fabGoogleTranslate)
    FloatingActionButton btnGoogleTranslate;

    @BindView(R.id.toggleImageBtn)
    AppCompatImageButton toggleBtnBookmark;

    @BindView(R.id.lblVocabulary)
    AppCompatTextView lblVocabulary;

    @BindView(R.id.lblMeaning)
    ExpandableTextView lblMeaning;

    private boolean internetAvailableWatcher;
    private String vocabulary;
    private String meaning;
    private boolean bookmarksWatcher;
    private TextToSpeech textToSpeech;
    private PopupMenu popupMenu = null;
    private BookmarksViewModel bookmarksViewModel;

    @Override
    public void onInternetAvailable() {
        internetAvailableWatcher = true;
    }

    @Override
    public void onInternetUnavailable() {
        internetAvailableWatcher = false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Implement View Model */
        bookmarksViewModel = ViewModelProviders.of(this).get(BookmarksViewModel.class);

        this.initUI();

        this.floatingButtonVisibility();

        /* Implement text to speech */
        this.textToSpeech = new TextToSpeech(this, this);
        /* Implement popup menu */
        this.popupMenu = new PopupMenu(this, this);
    }

    @Override
    public int getLayoutFileId() {
        return R.layout.activity_detail_result;
    }

    @Override
    public String getScreenTitleAtStart() {
        return null;
    }

    @Override
    public Class<? extends BasicActivity> getDefaultBackScreenIfRoot() {
        return null;
    }

    @Override
    public Bundle getDefaultBackScreenExtraIfRoot() {
        return null;
    }

    @OnClick(R.id.toggleImageBtn)
    public void clickToggleBtnBookmark() {

        if (bookmarksWatcher) {
            bookmarksViewModel.deleteByBookmarkWord(vocabulary);
            ToastView.showToast(this, "Bookmark removed.");
            toggleBtnBookmark.setImageResource(R.drawable.ic_bookmark_border);
            bookmarksWatcher = false;
        } else {
            bookmarksViewModel.insertBookmark(new BookmarksEntity(vocabulary));
            ToastView.showToast(this, "Bookmark added.");
            toggleBtnBookmark.setImageResource(R.drawable.ic_bookmarked);
            bookmarksWatcher = true;
        }
    }

    @OnClick(R.id.fabGoogleTranslate)
    public void goGoogleTranslate() {
        if (internetAvailableWatcher) {
            this.translateInGoogleTranslate(vocabulary);
        } else {
            noInternetConnectionAlerter();
        }

    }

    private void translateInGoogleTranslate(String translate) {
        WebViewUtil.show(this, "https://translate.google.com/?source=gtx_m#en/my/" + translate);
    }

    @OnClick(R.id.btnPronunciation)
    public void pronunciation() {
        speakOut();
    }

    public void copyContent(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Computer Dictionary",
                vocabulary + "\n\n" + meaning + "\n" + APP_LINK);
        assert clipboard != null;
        Alerter.showAlerter(this, "Copied to clipboard.", R.color.green_700, R.drawable.ic_success);
        clipboard.setPrimaryClip(clip);
    }

    public void showMoreActions(View view) {
        popupMenu.showPopupMenu(view);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = textToSpeech.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Logger.log("This Language is not supported");
            } else {
                btnPronunciation.setEnabled(true);
            }
        } else {
            Logger.log("Text to speech Initialization Failed!");
        }
    }

    @Override
    public void onDestroy() {
        /* Don't forget to shutdown textToSpeech! */
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void speakOut() {
        textToSpeech.speak(vocabulary, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void noInternetConnectionAlerter() {
        Alerter.showAlerter(this, "No internet connection.", R.color.red_A700,
                R.drawable.ic_no_internet);
    }

    /* Get The String from previous fragment */
    private void initUI() {

        Intent intent = getIntent();
        vocabulary = intent.getStringExtra("vocabularyKey");
        meaning = intent.getStringExtra("meaningKey");

        /* Set Text */
        lblVocabulary.setText(vocabulary);
        lblMeaning.setText(meaning);

         /*
         This is goBrowse solve for overwriting of super class's onCreate Method
         */
        final AppCompatTextView lblScreenTitle = this.findViewById(R.id.lblScreenTitle);
        lblScreenTitle.setText(vocabulary);

        /* Need to assign bookmarksWatcher related on db which is already exist in db or not exist */
        if (bookmarksViewModel.getBookmarkCount(vocabulary) != 0) {
            bookmarksWatcher = true;
            toggleBtnBookmark.setImageResource(R.drawable.ic_bookmarked);
        } else {
            toggleBtnBookmark.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    @Override
    public void goToGoogleSearch() {
        if (internetAvailableWatcher) {
            WebViewUtil.show(this, "https://www.google.com.mm/search?q=" + vocabulary);
        } else {
            noInternetConnectionAlerter();
        }
    }

    @Override
    public void goToWikipediaSearch() {
        if (internetAvailableWatcher) {
            WebViewUtil.show(this, "https://en.wikipedia.org/wiki/" + vocabulary);
        } else {
            noInternetConnectionAlerter();
        }
    }

    @Override
    public void shareTranslation() {

        /* Implement fontChecker */
        SystemFontChecker fontChecker = SystemFontChecker.getInstance();

        /*Change font zawgyi to unicode */
        if (fontChecker.isUnicode(this)) {
            meaning = Rabbit.zg2uni(meaning);
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Computer Dictionary");
        shareIntent.putExtra(Intent.EXTRA_TEXT, vocabulary + "\n\n" + meaning + "\n" + APP_LINK);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "Share with"));
    }

    private void floatingButtonVisibility() {
        ScrollView svBody = findViewById(R.id.svBody);
        svBody.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = svBody.getScrollY(); // For Vertical ScrollView
            if (scrollY > 0 && btnGoogleTranslate.getVisibility() == View.VISIBLE)
                btnGoogleTranslate.hide();
            else
                btnGoogleTranslate.show();
        });

    }
}
