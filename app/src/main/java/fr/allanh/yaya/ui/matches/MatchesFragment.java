package fr.allanh.yaya.ui.matches;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import fr.allanh.yaya.League;
import fr.allanh.yaya.Match;
import fr.allanh.yaya.swipe.OnSwipeTouchListener;
import fr.allanh.yaya.swipe.OnSwipeTouchListenerMatch;
import fr.allanh.yaya.R;
import fr.allanh.yaya.Utils;

public class MatchesFragment extends Fragment {

    private HashMap<League, ArrayList<Match>> matches = new HashMap<>();
    private HashMap<String, HashMap<League, ArrayList<Match>>> keep_matches = new HashMap<>();
    private HashMap<LinearLayout, ArrayList<LinearLayout>> renderedMatches = new HashMap<>();
    private Calendar calendar, pre;
    private FrameLayout frameLayout;
    private View view;
    private TextView tv_match_day;
    private RequestQueue queue;

    private OnSwipeTouchListener swipeListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frameLayout = new FrameLayout(getActivity());
        inflater =(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.fragment_matches, container,false);
        frameLayout.addView(view);

        calendar = Calendar.getInstance();
        pre = Calendar.getInstance();
        calendar.setTime(new Date());

        swipeListener = new OnSwipeTouchListenerMatch(view.findViewById(R.id.scroll_match), this);

        queue = Volley.newRequestQueue(getContext());

        tv_match_day = view.findViewById(R.id.match_day);

        Button btn_next = view.findViewById(R.id.btnNextDay);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {  updateMatchDay(1); }
        });

        Button btn_previous = view.findViewById(R.id.btnPreviousDay);
        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { updateMatchDay(-1); }
        });

        jsonRequest();

        return frameLayout;
    }

    /**
     * Update the day
     * @param dayChange
     */
    private void updateMatchDay(int dayChange) {
        pre.setTime(calendar.getTime());
        calendar.add(Calendar.DATE, dayChange);
        jsonRequest();
    }


    /**
     * Update the date at the top
     */
    private String refreshDate() {
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        if (c.get(Calendar.DATE) == calendar.get(Calendar.DATE) && c.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && c.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
            tv_match_day.setText(getResources().getString(R.string.today));
            return getResources().getString(R.string.today);
        }
        else {
            c.add(Calendar.DATE, 1);
            if (c.get(Calendar.DATE) == calendar.get(Calendar.DATE) && c.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && c.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                tv_match_day.setText(getResources().getString(R.string.tomorrow));
                return getResources().getString(R.string.tomorrow);
            }
            else {
                c.add(Calendar.DATE, -2);
                if (c.get(Calendar.DATE) == calendar.get(Calendar.DATE) && c.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && c.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                    tv_match_day.setText(getResources().getString(R.string.yesterday));
                    return getResources().getString(R.string.yesterday);
                }
                else {
                    tv_match_day.setText(calendar.get(Calendar.DATE) + "-" + String.valueOf(calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.YEAR));
                    return calendar.get(Calendar.DATE) + "-" + String.valueOf(calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.YEAR);
                }
            }
        }
    }

    /**
     * Add top leagues to selected teams to render
     */
    private void setupIdTeams() {
        ArrayList<League> leagues = Utils.getFavoriteLeagues();
        for (League l : leagues) {
            matches.put(l,new ArrayList<>());
        }
    }

    public void setPreTime(Date t) {
        this.pre.setTime(t);
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    public void addCalendar(int i) {
        calendar.add(Calendar.DATE, i);
    }

    /**
     * Get date at API format from calendar calendar
     * @return
     */
    private String getDateCalendar() {
        String res = "";

        res += calendar.get(Calendar.YEAR) + "-";

        int m = calendar.get(Calendar.MONTH)+1;
        if (m < 10) {
            res += "0";
        }
        res += m + "-";

        int d = calendar.get(Calendar.DATE);
        if (d < 10) {
            res += "0";
        }
        res += d;


        return res;
    }

    /**
     * Get date at API format from pre calendar
     * @return
     */
    private String getDatePre() {
        String res = "";

        res += pre.get(Calendar.YEAR) + "-";

        int m = pre.get(Calendar.MONTH)+1;
        if (m < 10) {
            res += "0";
        }
        res += m + "-";

        int d = pre.get(Calendar.DATE);
        if (d < 10) {
            res += "0";
        }
        res += d;


        return res;
    }

    /**
     * Doing the JSON Request to refresh matches
     */
    public void jsonRequest() {
        String url = "https://api-football-v1.p.rapidapi.com/v3/fixtures?date=" + getDateCalendar();

        if (keep_matches.containsKey(getDateCalendar())) {
            if (!keep_matches.containsKey(getDatePre())) {
                keep_matches.put(getDatePre(), matches);
            }
            matches = keep_matches.get(getDateCalendar());
            renderMatches();
            refreshDate();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                keep_matches.put(getDatePre(), matches);
                setupMatches(response);
                renderMatches();
                refreshDate();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error : " + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "api-football-v1.p.rapidapi.com");
                params.put("x-rapidapi-key", "1c7d63f81bmsh9ece0d263344f55p19b4d9jsncadd110c060d");
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        jsonObjectRequest.setTag("MATCHS");
        queue.add(jsonObjectRequest);
    }

    /**
     * Is the team in a selected id
     * @param id
     * @return
     */
    private League isInSelectedTeams(int id) {
        for (League l : matches.keySet()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    /**
     * Get league hbox
     * @param league
     * @return
     */
    private LinearLayout getHboxLeague(League league) {
        LinearLayout hbox = new LinearLayout(getActivity());
        //hbox.setBackgroundColor(getResources().getColor(R.color.purple_500));
        hbox.setBackgroundColor(Color.rgb(100,90,90));
        hbox.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        hbox.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams layoutParams =(RelativeLayout.LayoutParams) hbox.getLayoutParams();
        layoutParams.setMargins(20,20,20,20);
        hbox.setPadding(10,10,10,10);
        hbox.setLayoutParams(layoutParams);
        hbox.setGravity(Gravity.CENTER);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x-30;

        ImageView l_team_home = new ImageView(getActivity());
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width/5,width/5);
        l_team_home.setLayoutParams(parms);
        Utils.setLogo(l_team_home, league.getLogo(), width/5);
        hbox.addView(l_team_home);

        TextView t = new TextView(getActivity());
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setText(league.getName());
        t.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        t.setTextColor(Color.WHITE);
        t.setId(Utils.generateViewId());
        t.setTextSize(25);
        RelativeLayout.LayoutParams params =(RelativeLayout.LayoutParams) t.getLayoutParams();
        params.setMargins(20,0,0,0);
        t.setLayoutParams(params);
        t.setGravity(Gravity.START);
        hbox.addView(t);

        return hbox;
    }

    /**
     * Create the match horizontal box
     * @param match
     * @return
     */
    private LinearLayout getHboxMatch(Match match) {
        LinearLayout hbox = new LinearLayout(getActivity());
        hbox.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        hbox.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams layoutParams =(RelativeLayout.LayoutParams) hbox.getLayoutParams();
        layoutParams.setMargins(10,10,10,10);
        hbox.setLayoutParams(layoutParams);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x-20;

        ImageView l_team_home = new ImageView(getActivity());
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width/7,width/7);
        l_team_home.setLayoutParams(parms);
        Utils.setLogo(l_team_home, match.getLogo_home(), width/7);
        hbox.addView(l_team_home);

        TextView t_team_home = new TextView(getActivity());
        t_team_home.setText(match.getTeam_home());
        t_team_home.setGravity(Gravity.CENTER);
        t_team_home.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        t_team_home.setTextSize(16);
        t_team_home.setWidth((2*width)/7);
        t_team_home.setPadding(5,0,5,0);
        t_team_home.setTextColor(Color.WHITE);
        t_team_home.setId(Utils.generateViewId());
        hbox.addView(t_team_home);

        String txt = " - ";
        if (match.getScore_home() != -1) {
            if (match.getScore_home() > match.getScore_away()) {
                txt = "<font color=#489F0B>" + match.getScore_home() + "</font>" + txt + "<font color=#cc0029>" + match.getScore_away() + "</font>";
            }
            else if (match.getScore_home() < match.getScore_away()) {
                txt = "<font color=#cc0029>" + match.getScore_home() + "</font>" + txt + "<font color=#489F0B>" + match.getScore_away() + "</font>";
            }
            else {
                txt = match.getScore_home() + txt + match.getScore_away();
            }
        }

        TextView t = new TextView(getActivity());
        t.setText(Html.fromHtml(txt, 0));
        t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        t.setTextSize(16);
        t.setWidth(width/7);
        t.setPadding(0,0,0,0);
        t.setTextColor(Color.WHITE);
        t.setId(Utils.generateViewId());
        hbox.addView(t);

        TextView t_team_away = new TextView(getActivity());
        t_team_away.setText(match.getTeam_away());
        t_team_away.setGravity(Gravity.CENTER);
        t_team_away.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        t_team_away.setTextSize(16);
        t_team_away.setWidth((2*width)/7);
        t_team_away.setTextColor(Color.WHITE);
        t_team_away.setId(Utils.generateViewId());
        hbox.addView(t_team_away);

        ImageView l_team_away = new ImageView(getActivity());
        LinearLayout.LayoutParams parms2 = new LinearLayout.LayoutParams(width/7,width/7);
        l_team_away.setLayoutParams(parms2);
        Utils.setLogo(l_team_away, match.getLogo_away(), width/7);
        hbox.addView(l_team_away);
        hbox.setVisibility(View.GONE);
        return hbox;
    }

    @Override
    public void onPause() {
        if (queue != null) {
            queue.cancelAll("MATCHS");
        }
        super.onPause();
    }

    /**
     * Render matches
     */
    private void renderMatches() {
        LinearLayout vbox_matches = view.findViewById(R.id.vbox_matches);
        boolean noMatch = true;

        while (vbox_matches.getChildCount() != 1) {
            vbox_matches.removeViewAt(1);
        }

        Map sortedMap = new TreeMap(matches);
        Set set2 = sortedMap.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()) {
            Map.Entry me2 = (Map.Entry)iterator2.next();
            League league = (League) me2.getKey();
            ArrayList<Match> match = (ArrayList<Match>) me2.getValue();

            if (match.size() > 0) {
                noMatch = false;
                LinearLayout hboxLeague = getHboxLeague(league);
                vbox_matches.addView(hboxLeague);
                ArrayList<LinearLayout> listMatches = new ArrayList<>();
                for (Match m : match) {
                    LinearLayout hboxMatch = getHboxMatch(m);
                    listMatches.add(hboxMatch);
                    vbox_matches.addView(hboxMatch);
                }
                hboxLeague.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (LinearLayout l : listMatches) {
                            if (l.getVisibility() == View.VISIBLE) {
                                l.setVisibility(View.GONE);
                            }
                            else {
                                goneAllMatch(hboxLeague);
                                l.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
                renderedMatches.put(hboxLeague, listMatches);
            }
        }

        if (noMatch) {
            renderNoMatch();
        }
    }

    /**
     * Render no match message
     */
    private void renderNoMatch() {
        LinearLayout vbox_matches = view.findViewById(R.id.vbox_matches);

        TextView t = new TextView(getActivity());
        t.setText(getResources().getString(R.string.no_match));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        t.setTextColor(Color.WHITE);
        t.setId(Utils.generateViewId());
        t.setTextSize(25);

        vbox_matches.addView(t);
    }

    /**
     * Gone all matchs from hbox
     * @param hbox
     */
    private void goneAllMatch(LinearLayout hbox) {
        for (LinearLayout l : renderedMatches.keySet()) {
            if (!l.equals(hbox)) {
                ArrayList<LinearLayout> list = renderedMatches.get(l);
                for (LinearLayout match : list) {
                    match.setVisibility(View.GONE);
                }
            }

        }
    }

    /**
     * Setup matches (extract matches from json)
     * @param jsonObject
     */
    private void setupMatches(JSONObject jsonObject) {
        matches = new HashMap<>();
        setupIdTeams();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("response");
            for (int i = 0 ; i < jsonArray.length() ; i++) {
                int teamID = (int) jsonArray.getJSONObject(i).getJSONObject("league").get("id");
                String teamHome = (String) jsonArray.getJSONObject(i).getJSONObject("teams").getJSONObject("home").get("name");
                String teamAway = (String) jsonArray.getJSONObject(i).getJSONObject("teams").getJSONObject("away").get("name");
                String teamHome_logo = (String) jsonArray.getJSONObject(i).getJSONObject("teams").getJSONObject("home").get("logo");
                String teamAway_logo = (String) jsonArray.getJSONObject(i).getJSONObject("teams").getJSONObject("away").get("logo");
                int id_teamAway = (int) jsonArray.getJSONObject(i).getJSONObject("teams").getJSONObject("away").get("id");
                int id_teamHome = (int) jsonArray.getJSONObject(i).getJSONObject("teams").getJSONObject("home").get("id");
                int goals_home = -1;
                int goals_away = -1;

                Object obj = jsonArray.getJSONObject(i).getJSONObject("goals").get("home");
                if (!obj.equals(null)) {
                    goals_home = (int) obj;
                }

                obj = jsonArray.getJSONObject(i).getJSONObject("goals").get("away");
                if (!obj.equals(null)) {
                    goals_away = (int) obj;
                }

                League league = isInSelectedTeams(teamID);
                if (league != null) {
                    matches.get(league).add(new Match(teamHome, teamAway, goals_home, goals_away, teamHome_logo, teamAway_logo));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}