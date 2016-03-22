package com.eggheadgames.siren;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

class SirenHelper {

    protected static String getPackageName(Context context) {
        return context.getPackageName();
    }

    protected static int getDaysSinceLastCheck(Context context) {
        long lastCheckTimestamp = getLastVerificationDate(context);

        if (lastCheckTimestamp > 0) {
            return (int) (TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis()) - TimeUnit.MILLISECONDS.toDays(lastCheckTimestamp));
        } else {
            return 0;
        }
    }

    protected static int getVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(getPackageName(context), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected static boolean isVersionSkippedByUser(Context context, String minAppVersion) {
        String skippedVersion = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREFERENCES_SKIPPED_VERSION, "");
        return skippedVersion.equals(minAppVersion);
    }

    protected static void setLastVerificationDate(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(Constants.PREFERENCES_LAST_CHECK_DATE, Calendar.getInstance().getTimeInMillis())
                .commit();
    }

    protected static long getLastVerificationDate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(Constants.PREFERENCES_LAST_CHECK_DATE, 0);
    }

    protected static String getAlertMessage(Context context, String minAppVersion, SirenSupportedLocales locale) {
        try {
            if (context.getApplicationInfo().labelRes != 0) {
                return String.format(getLocalizedString(context, R.string.update_alert_message, locale), getLocalizedString(context, context.getApplicationInfo().labelRes, locale), minAppVersion);
            } else {
                return String.format(getLocalizedString(context, R.string.update_alert_message, locale), getLocalizedString(context, R.string.fallback_app_name, locale), minAppVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.update_alert_message, context.getString(R.string.fallback_app_name), minAppVersion);
        }
    }

    protected static String getLocalizedString(Context context, int stringResource, SirenSupportedLocales locale) {
        if (locale == null) {
            return context.getString(stringResource);
        } else {
            Resources standardResources = context.getResources();
            AssetManager assets = standardResources.getAssets();
            DisplayMetrics metrics = standardResources.getDisplayMetrics();
            Configuration defaultConfiguration = standardResources.getConfiguration();
            Configuration newConfiguration = new Configuration(defaultConfiguration);
            newConfiguration.locale = new Locale(locale.getLocale());
            String string = new Resources(assets, metrics, newConfiguration).getString(stringResource);

            //need to turn back the default locale
            new Resources(assets, metrics, defaultConfiguration);
            return string;

        }
    }


    protected static void openGooglePlay(Activity activity) {
        final String appPackageName = getPackageName(activity);
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    protected static void setVersionSkippedByUser(Context context, String skippedVersion) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(Constants.PREFERENCES_SKIPPED_VERSION, skippedVersion)
                .commit();
    }

    protected static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(getPackageName(context), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    protected static boolean isGreater(String first, String second) {
        if (TextUtils.isDigitsOnly(first) && TextUtils.isDigitsOnly(second)) {
            return Integer.parseInt(first) > Integer.parseInt(second);
        }
        return false;
    }
}
