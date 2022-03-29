package fr.allanh.yaya;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.DragAndDropPermissions;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static String actualLanguage = "fr";

    private static int actual_league;

    private static ArrayList<League> fav_leagues;
    private static ArrayList<League> leagues;
    private static HashMap<Integer, League> all_leagues = new HashMap<>();

    private static Activity activity;
    private static Resources resources;
    private static Context context;

    private static boolean initialize = false;

    /**
     * Get the bitmap from an url
     * @param url
     * @return
     */
    public static void setLogo(ImageView view, String url, int width) {
        Picasso.get().load(url).resize(width, 0).noFade().into(view);
    }

    /**
     * Initialise utils class
     * Only one use
     * @param a
     * @param r
     * @param t
     */
    public static void initialisation(Activity a, Resources r, Context t) {
        if (!initialize) {
            activity = a;
            resources = r;
            context = t;

            getAllLeagues();
            initLanguage();
            checkFirstStart();
            getFavoriteLeagues();
        }
    }

    /**
     * Get all leagues
     */
    public static void getAllLeagues() {
        try {
            JSONObject jsonObject = new JSONObject(Utils.JsonDataFromAsset("leagues.json"));
            JSONArray jsonArray = jsonObject.getJSONArray("response");
            for (int i = 0 ; i < jsonArray.length() ; i++) {
                String leagueName = jsonArray.getJSONObject(i).getJSONObject("league").getString("name");
                boolean isCup = jsonArray.getJSONObject(i).getJSONObject("league").getString("type").equalsIgnoreCase("cup");
                int leagueID = jsonArray.getJSONObject(i).getJSONObject("league").getInt("id");
                String leagueLogo = jsonArray.getJSONObject(i).getJSONObject("league").getString("logo");
                all_leagues.put(leagueID, new League(leagueName, leagueID, isCup, leagueLogo));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get actual language
     * @return
     */
    public static String getActualLanguage() {
        return actualLanguage;
    }

    /**
     * Add a league to our favorite league list
     * @param a
     * @param league
     */
    public static void addFavoriteLeague(Activity a, League league) {
        fav_leagues.add(league);

        Set<String> set = new HashSet<>();
        for (League t : fav_leagues) {
            set.add(String.valueOf(t.getId()));
        }
        SharedPreferences s_lang = a.getSharedPreferences("yaya",0);
        SharedPreferences.Editor e_lang = s_lang.edit();
        e_lang.putStringSet("fav_leagues", set);
        e_lang.commit();
        a.recreate();
    }

    /**
     * Remove favorite league
     * @param a
     * @param id
     */
    public static void removeFavoriteLeague(Activity a, int id) {
        for (League l : fav_leagues) {
            if (l.getId() == id) {
                fav_leagues.remove(l);
                break;
            }
        }

        Set<String> set = new HashSet<>();
        for (League t : fav_leagues) {
            set.add(String.valueOf(t.getId()));
        }
        SharedPreferences s_lang = a.getSharedPreferences("yaya",0);
        SharedPreferences.Editor e_lang = s_lang.edit();
        e_lang.putStringSet("fav_leagues", set);
        e_lang.commit();
        a.recreate();

    }

    /**
     * Convert dp to px
     * @param dp
     * @return
     */
    public static float convertDpToPx(float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    /**
     * Check if that is the first start of the application. If it is then generate fav leagues
     * @return
     */
    public static boolean checkFirstStart() {
        SharedPreferences prefs = activity.getSharedPreferences("yaya",0);
        boolean firstStart = prefs.getBoolean("firststart", true);
        if (firstStart) {
            fav_leagues = new ArrayList<>();
            fav_leagues.add(getLeagueFromName("Ligue 1"));
            fav_leagues.add(getLeagueFromName("Premier League"));
            fav_leagues.add(getLeagueFromId(135));
            fav_leagues.add(getLeagueFromName("UEFA Champions League"));
            fav_leagues.add(getLeagueFromName("UEFA Europa League"));
            fav_leagues.add(getLeagueFromName("Bundesliga 1"));
            fav_leagues.add(getLeagueFromName("La Liga"));

            Set<String> set = new HashSet<>();
            for (League t : fav_leagues) {
                set.add(String.valueOf(t.getId()));
            }
            SharedPreferences.Editor e_lang = prefs.edit();
            e_lang.putStringSet("fav_leagues", set);
            e_lang.commit();

            SharedPreferences.Editor e_first = prefs.edit();
            e_first.putBoolean("firststart", false);
            e_first.commit();

            activity.recreate();
        }
        return firstStart;
    }

    /**
     * Get the current league
     * @return
     */
    public static League getCurrentLeague() {
        return leagues.get(actual_league);
    }

    /**
     * Get the next league
     * @return
     */
    public static League nextLeague() {
        actual_league++;
        if (actual_league >= leagues.size()) {
            actual_league = 0;
        }
        return leagues.get(actual_league);
    }

    /**
     * Get the previous league
     * @return
     */
    public static League previousLeague() {
        actual_league--;
        if (actual_league < 0) {
            actual_league = leagues.size()-1;
        }
        return leagues.get(actual_league);
    }

    /**
     * Get all favorite leagues and cup
     * @return
     */
    public static ArrayList<League> getFavoriteLeagues() {
        Set<String> set = new HashSet<>();
        fav_leagues = new ArrayList<>();
        SharedPreferences settings_language = activity.getSharedPreferences("yaya",0);
        set = settings_language.getStringSet("fav_leagues", set);
        for (String s : set) {
            fav_leagues.add(Utils.getLeagueFromId(Integer.valueOf(s)));
        }
        Collections.sort(fav_leagues);
        return fav_leagues;
    }

    /**
     * Get all favorite leagues for the leaderboard (without cups)
     * @return
     */
    public static ArrayList<League> getFavLeagueLeaderboard() {
        leagues = new ArrayList<>();
        for (League l : fav_leagues) {
            if(!l.getIsCup()) {
                leagues.add(l);
            }
        }
        Collections.sort(leagues);
        actual_league = (int) Math.floor(leagues.size()/2);
        return leagues;
    }

    /**
     * Update language
     * @param lang
     */
    public static void updateLanguage(String lang) {
        setLocale(lang);
        SharedPreferences s_lang = activity.getSharedPreferences("yaya",0);
        SharedPreferences.Editor e_lang = s_lang.edit();
        e_lang.putString("lang",lang);
        e_lang.commit();
        activity.recreate();
    }

    /**
     * Change language
     * @param language
     */
    @SuppressWarnings("deprecation")
    private static void setLocale(String language) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(language));
        resources.updateConfiguration(configuration, metrics);
        activity.onConfigurationChanged(configuration);
    }

    /**
     * Initialize language from the cookies
     */
    public static void initLanguage() {
        SharedPreferences settings_language = activity.getSharedPreferences("yaya",0);
        actualLanguage = settings_language.getString("lang", actualLanguage);
        setLocale(actualLanguage);
    }

    /**
     * Get League from its id
     * @param id
     * @return
     */
    public static League getLeagueFromId(int id) {
        return all_leagues.get(id);
    }

    /**
     * Get League from its name
     * @param name
     * @return
     */
    public static League getLeagueFromName(String name) {
        for (int i : all_leagues.keySet()) {
            League l = all_leagues.get(i);
            if (l.getName().equalsIgnoreCase(name)) {
                return l;
            }
        }
        return null;
    }

    /**
     * Get json data from a file
     * @param fileName
     * @return
     */
    public static String JsonDataFromAsset(String fileName) {
        try {
            InputStream inputStream = activity.getAssets().open(fileName);
            int sizeOfFile = inputStream.available();
            byte[] bufferData = new byte[sizeOfFile];
            inputStream.read(bufferData);
            inputStream.close();
            return new String(bufferData, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate UUID
     * @return
     */
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
}

