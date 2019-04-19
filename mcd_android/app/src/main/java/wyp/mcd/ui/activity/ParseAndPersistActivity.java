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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;

import java.util.List;

import butterknife.BindView;
import wyp.mcd.R;
import wyp.mcd.component.android.AndroidUtil;
import wyp.mcd.component.ui.BasicActivity;
import wyp.mcd.component.android.JsonParser;
import wyp.mcd.component.sharedpreferences.AppInfoStorage;
import wyp.mcd.component.util.ClearApplicationData;
import wyp.mcd.component.util.Logger;
import wyp.mcd.infrastructure.dao.EngToEngDao;
import wyp.mcd.infrastructure.dao.EngToMmDao;
import wyp.mcd.infrastructure.entities.EngToEngEntity;
import wyp.mcd.infrastructure.entities.EngToMmEntity;
import wyp.mcd.infrastructure.pesistance.McdRoomDatabase;

@SuppressWarnings("unchecked")
public class ParseAndPersistActivity extends BasicActivity {

    @BindView(R.id.lottie_animation_view)
    LottieAnimationView mLottieAnimationView;
    private EngToMmDao engToMmDao;
    private EngToEngDao engToEngDao;
    private JsonParser jsonParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        McdRoomDatabase mcdRoomDatabase = McdRoomDatabase.getDatabase(getApplicationContext());
        engToMmDao = mcdRoomDatabase.engToMmDao();
        engToEngDao = mcdRoomDatabase.engToEngDao();

        jsonParser = new JsonParser(getApplicationContext());

        mLottieAnimationView.setAnimation(R.raw.moving_bus);
        mLottieAnimationView.playAnimation();

        /* Calling parse and persist */
        parseAndPersistData();
    }

    @Override
    protected int getLayoutFileId() {
        return R.layout.activity_parse_and_persist;
    }

    @Override
    protected String getScreenTitleAtStart() {
        return null;
    }

    @Override
    protected Class<? extends BasicActivity> getDefaultBackScreenIfRoot() {
        return null;
    }

    @Override
    protected Bundle getDefaultBackScreenExtraIfRoot() {
        return null;
    }

    private void parseAndPersistData() {

        if (AppInfoStorage.getInstance().getVersionCode() != AndroidUtil.getCurrentVersionCode(ParseAndPersistActivity.this)) {

            /* Clean Current database and SharedPreferences */
            ClearApplicationData.clearApplicationData(ParseAndPersistActivity.this);
            AppInfoStorage.getInstance().cleanUpAll();

            new InsertEngToMmAsyncTask(engToMmDao).execute((List<EngToMmEntity>) jsonParser.parseEngToMmJson());
        }
    }

    private void showNextScreen() {
        if (AppInfoStorage.getInstance().getVersionCode() == AndroidUtil.getCurrentVersionCode(ParseAndPersistActivity.this)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class InsertEngToMmAsyncTask extends AsyncTask<List<EngToMmEntity>, Void, Void> {

        private EngToMmDao engToMmDao;

        InsertEngToMmAsyncTask(EngToMmDao dao) {
            engToMmDao = dao;
        }

        @Override
        protected final Void doInBackground(List<EngToMmEntity>... params) {

            for (EngToMmEntity engToMmEntity : params[0]) {
                engToMmDao.insert(engToMmEntity);
                Logger.d(ParseAndPersistActivity.class, "Eng to Mm:" + engToMmEntity.getVocabulary());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new InsertEngToEngAsyncTask(engToEngDao).execute((List<EngToEngEntity>) jsonParser.parseEngToEngJson());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class InsertEngToEngAsyncTask extends AsyncTask<List<EngToEngEntity>, Void, Void> {

        private EngToEngDao engToEngDao;

        InsertEngToEngAsyncTask(EngToEngDao engToEngDao) {
            this.engToEngDao = engToEngDao;
        }

        @Override
        protected Void doInBackground(List<EngToEngEntity>... params) {
            for (EngToEngEntity engToEngEntity : params[0]) {
                engToEngDao.insert(engToEngEntity);
                Logger.d(ParseAndPersistActivity.class, "Eng to Eng:" + engToEngEntity.getVocabulary());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            /* Marked version code for next data upgrade &
             db is already persisted*/
            AppInfoStorage.getInstance().saveVersionCode(AndroidUtil.getCurrentVersionCode(ParseAndPersistActivity.this));
            showNextScreen();
        }
    }
}
