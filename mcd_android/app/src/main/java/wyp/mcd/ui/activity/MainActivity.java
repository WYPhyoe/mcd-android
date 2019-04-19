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

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Objects;

import wyp.mcd.R;
import wyp.mcd.component.android.AndroidUtil;
import wyp.mcd.component.util.GetVersionCodeFromPlayStoreUtil;
import wyp.mcd.component.util.Logger;
import wyp.mcd.component.util.TransitionUtil;
import wyp.mcd.ui.behavior.BottomNavigationBehavior;
import wyp.mcd.ui.fragment.AboutFragment;
import wyp.mcd.ui.fragment.BookmarksFragment;
import wyp.mcd.ui.fragment.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        Fragment fragment;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_search:
                    fragment = new SearchFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_bookmarks:
                    fragment = new BookmarksFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_about:
                    fragment = new AboutFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /* Attaching bottom sheet behaviour - hide / show on scroll */
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());

        /* Load the store fragment by default */
        loadFragment(new SearchFragment());

        /* Check Update */
        this.checkAppUpdate();

        /* Firebase Token */
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, instanceIdResult -> {
            String updatedToken = instanceIdResult.getToken();
            Logger.log(String.format("Firebase Updated Token :%s", updatedToken));
        });
    }

    /*
     * loading fragment into FrameLayout
     * @param fragment
     */
    private void loadFragment(Fragment fragment) {
        /* load fragment */
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        /*
         * If we want to allow when user click back button previous fragment will call
         */
        //fragmentTransaction.addToBackStack(null);
        //fragmentTransaction.detach(fragment);
        //fragmentTransaction.attach(fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    private void checkAppUpdate() {
        /* Call App Updater */
        new GetVersionCodeFromPlayStoreUtil(onlineVersion -> {
            Logger.log("Online Version Name is : " + onlineVersion);
            if (onlineVersion != null) {
                if (Double.parseDouble(AndroidUtil.getCurrentVersionName(Objects.requireNonNull(MainActivity.this))) < Double.parseDouble(onlineVersion)) {
                    this.showAppUpdateActivityScreen();
                }
            }
        }, "https://play.google.com/store/apps/details?id=wyp.mmcomputerdictionary").execute();
    }

    private void showAppUpdateActivityScreen() {
        TransitionUtil.showNextActivity(MainActivity.this, AppUpdateActivity.class, R.anim.entering_screen_sliding_up, R.anim.exiting_screen_sliding_down, false);
    }
}
