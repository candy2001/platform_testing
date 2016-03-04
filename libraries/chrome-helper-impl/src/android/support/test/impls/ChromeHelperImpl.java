/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.support.test.helpers;

import android.app.Instrumentation;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.Until;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ListView;

import java.io.IOException;

import junit.framework.Assert;

public class ChromeHelperImpl extends AbstractChromeHelper {
    private static final String LOG_TAG = AbstractChromeHelper.class.getSimpleName();

    private static final String UI_MENU_BUTTON_ID = "menu_button";
    private static final String UI_SEARCH_BOX_ID = "search_box_text";
    private static final String UI_URL_BAR_ID = "url_bar";
    private static final String UI_VIEW_HOLDER_ID = "compositor_view_holder";

    private static final long APP_INIT_WAIT = 10000;
    private static final long MAX_DIALOG_TRANSITION = 5000;
    private static final long PAGE_LOAD_TIMEOUT = 30 * 1000;

    private String mPackageName;
    private String mLauncherName;

    public ChromeHelperImpl(Instrumentation instr) {
        super(instr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        if (mPackageName == null) {
            String prop = null;
            try {
                mDevice.executeShellCommand("getprop dev.chrome.package");
            } catch (IOException ioe) {
                // log but ignore
                Log.e(LOG_TAG, "IOException while getprop", ioe);
            }
            if (prop == null || prop.isEmpty()) {
                prop = "com.android.chrome";
            }
            mPackageName = prop;
        }
        return mPackageName;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        if (mLauncherName == null) {
            String prop = null;
            try {
                mDevice.executeShellCommand("getprop dev.chrome.name");
            } catch (IOException ioe) {
                // log but ignore
                Log.e(LOG_TAG, "IOException while getprop", ioe);
            }
            if (prop == null || prop.isEmpty()) {
                prop = "Chrome";
            }
            mLauncherName = prop;
        }
        return mLauncherName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {
        // Terms of Service
        UiObject2 tos = mDevice.wait(Until.findObject(By.res(getPackage(), "terms_accept")),
                APP_INIT_WAIT);
        if (tos != null) {
            tos.click();
        }

        if (mDevice.wait(Until.hasObject(By.textStartsWith("Add an account")),
                MAX_DIALOG_TRANSITION)) {
            // Device has no accounts registered that Chrome recognizes
            // Select negative button to skip setup wizard sign in
            UiObject2 negative = mDevice.wait(Until.findObject(
                    By.res(getPackage(), "negative_button")), MAX_DIALOG_TRANSITION);
            if (negative != null) {
                negative.click();
            }
        } else {
            // Device has an account registered that Chrome recognizes
            // Press positive buttons until through setup wizard
            for (int i = 0; i < 4; i++) {
                if (!isInSetupWizard()) {
                    break;
                }

                UiObject2 positive = mDevice.wait(Until.findObject(
                        By.res(getPackage(), "positive_button")), MAX_DIALOG_TRANSITION);
                if (positive != null) {
                    positive.click();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openUrl(String url) {
        UiObject2 urlBar = getUrlBar();
        Assert.assertNotNull("Failed to detect a URL bar", urlBar);
        mDevice.waitForIdle();
        urlBar.setText(url);
        mDevice.pressEnter();
        waitForPageLoad();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flingPage(Direction dir) {
        UiObject2 page = getWebPage();
        // TODO: Change this to be non-constant
        page.setGestureMargin(500);
        page.fling(dir);
        // Block until the fling is complete
        mDevice.waitForIdle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openMenu() {
        UiObject2 menuButton = null;
        for (int retries = 2; retries > 0; retries--) {
            menuButton = mDevice.findObject(By.desc("More options"));
            if (menuButton == null) {
                flingPage(Direction.UP);
            } else {
                break;
            }
        }
        Assert.assertNotNull("Unable to find menu button.", menuButton);
        menuButton.clickAndWait(Until.newWindow(), 5000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeTabs() {
        openSettings();
        mDevice.findObject(By.text("Merge tabs and apps")).click();
        if (mDevice.findObject(By.text("On")) != null) {
            // Merge tabs is already on
            mDevice.pressBack();
            mDevice.pressBack();
        } else {
            mDevice.findObject(By.res(getPackage(), "switch_widget")).click();
            mDevice.findObject(By.text("OK")).click();
        }
        SystemClock.sleep(5000);
        waitForPageLoad();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unmergeTabs() {
        openSettings();
        mDevice.findObject(By.text("Merge tabs and apps")).click();
        if (mDevice.findObject(By.text("Off")) != null) {
            // Merge tabs is already off
            mDevice.pressBack();
            mDevice.pressBack();
        } else {
            mDevice.findObject(By.res(getPackage(), "switch_widget")).click();
            mDevice.findObject(By.text("OK")).click();
        }
        SystemClock.sleep(5000);
        waitForPageLoad();
    }

    private void openSettings() {
        openMenu();
        UiObject2 menu = getMenu();
        // TODO: Change this to be non-constant
        menu.setGestureMargin(500);
        menu.scroll(Direction.DOWN, 1.0f);
        // Open the Settings menu
        mDevice.findObject(By.desc("Settings")).clickAndWait(Until.newWindow(), 3000);
    }

    private UiObject2 getWebPage() {
        mDevice.waitForIdle();

        UiObject2 webView = mDevice.findObject(By.clazz(WebView.class));
        if (webView != null) {
            return webView;
        }

        UiObject2 viewHolder = mDevice.findObject(
                By.res(getPackage(), UI_VIEW_HOLDER_ID));
        if (viewHolder != null) {
            return viewHolder;
        }

        Assert.fail("Unable to select web page.");
        return null;
    }

    private UiObject2 getUrlBar() {
        // First time, URL bar is has id SEARCH_BOX_ID
        UiObject2 urlLoc = mDevice.findObject(By.res(getPackage(), UI_SEARCH_BOX_ID));
        if (urlLoc != null) {
            urlLoc.click();
        }
        // Afterwards, URL bar has id URL_BAR_ID
        for (int retries = 2; retries > 0; retries--) {
            urlLoc = mDevice.findObject(By.res(getPackage(), UI_URL_BAR_ID));
            if (urlLoc == null) {
                flingPage(Direction.UP);
            } else {
                break;
            }
        }

        urlLoc.click();
        return urlLoc;
    }

    private UiObject2 getMenu() {
        return mDevice.findObject(By.clazz(ListView.class).pkg(getPackage()));
    }

    private void waitForPageLoad() {
        mDevice.waitForIdle();
        if (mDevice.hasObject(By.desc("Stop page loading"))) {
            mDevice.wait(Until.gone(By.desc("Stop page loading")), PAGE_LOAD_TIMEOUT);
        } else if (mDevice.hasObject(By.res(getPackage(), "progress"))) {
            mDevice.wait(Until.gone(By.res(getPackage(), "progress")), PAGE_LOAD_TIMEOUT);
        }
    }

    private boolean isInSetupWizard() {
        return mDevice.hasObject(By.res(getPackage(), "fre_pager"));
    }
}