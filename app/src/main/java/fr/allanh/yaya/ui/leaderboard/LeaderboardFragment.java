package fr.allanh.yaya.ui.leaderboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.allanh.yaya.League;
import fr.allanh.yaya.swipe.OnSwipeTouchListener;
import fr.allanh.yaya.swipe.OnSwipeTouchListenerLeaderboard;
import fr.allanh.yaya.R;
import fr.allanh.yaya.Team;
import fr.allanh.yaya.Utils;

public class LeaderboardFragment extends Fragment {

    private FrameLayout frameLayout;
    private RequestQueue queue;
    private ArrayList<Team> teamList = new ArrayList<Team>();
    private View view;
    private int leagueID;
    private int preLeagueID;
    private TextView leagueName;
    private OnSwipeTouchListener swipeTouchListener;
    private static HashMap<Integer, ArrayList<Team>> keep_leaderboard = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frameLayout = new FrameLayout(getActivity());
        inflater =(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.fragment_leaderboard, container,false);
        frameLayout.addView(view);

        Utils.getFavLeagueLeaderboard();
        leagueName = view.findViewById(R.id.league_leaderboard);
        leagueName.setText(Utils.getCurrentLeague().getName());
        leagueID = Utils.getCurrentLeague().getId();

        queue = Volley.newRequestQueue(getContext());

        swipeTouchListener = new OnSwipeTouchListenerLeaderboard(view.findViewById(R.id.scroll_leaderboard),this);

        Button b1 = view.findViewById(R.id.btnNextLeague);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextLeague();
            }
        });

        Button b2 = view.findViewById(R.id.btnPreviousLeague);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousLeague();
            }
        });

        jsonRequest();

        return frameLayout;
    }

    /**
     * Swipe to previous league
     */
    public void previousLeague() {
        League l = Utils.previousLeague();
        preLeagueID = leagueID;
        leagueID = l.getId();
        leagueName.setText(l.getName());
        jsonRequest();
    }

    /**
     * Swipe to next league
     */
    public void nextLeague() {
        League l = Utils.nextLeague();
        preLeagueID = leagueID;
        leagueID = l.getId();
        leagueName.setText(l.getName());
        jsonRequest();
    }

    /**
     * Generate the position textview
     * @param pos
     * @return
     */
    private TextView getTextViewPosition(int pos) {
        TextView t = new TextView(getActivity());
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setText(String.valueOf(pos));
        t.setLayoutParams(new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        t.setTextSize(14);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(Color.WHITE);
        t.setMinWidth(40);
        t.setId(Utils.generateViewId());
        return t;
    }

    /**
     * Generate the team name textView
     * @param name
     * @return
     */
    private TextView getTextViewName(String name) {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = (int) Math.floor(size.x/3);

        TextView t = new TextView(getActivity());
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setText(name);
        t.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        t.setTextSize(14);
        t.setMaxWidth(width);
        t.setMinWidth(width);
        t.setPadding(10,0,0,0);
        t.setTextColor(Color.WHITE);
        t.setId(Utils.generateViewId());
        return t;
    }

    /**
     * Get the stats relative layout
     * @param team
     * @return
     */
    private RelativeLayout getRelativeLayoutStat(Team team) {
        RelativeLayout box_stat = new RelativeLayout(getActivity());
        //box_stat.setPaddingRelative(0,20,0,0);

        TextView pts = createTeamViewStat(team.getPoints(),-1);
        box_stat.addView(pts);

        TextView lose = createTeamViewStat(team.getLosses(),pts.getId());
        box_stat.addView(lose);

        TextView draw = createTeamViewStat(team.getDraws(),lose.getId());
        box_stat.addView(draw);

        TextView win = createTeamViewStat(team.getWins(),draw.getId());
        box_stat.addView(win);

        TextView played = createTeamViewStat(team.getPlayed(),win.getId());
        box_stat.addView(played);

        return box_stat;
    }

    /**
     * Render all teams
     */
    private void renderTeams() {
        LinearLayout vbox = view.findViewById(R.id.vbo_leaderboard);

        vbox.removeAllViews();

        int position = 0;
        for (Team team : teamList) {
            LinearLayout hbox = new LinearLayout(getActivity());
            position+=1;
            if (position%2 == 0) {
                hbox.setBackgroundColor(Color.rgb(102,102,102));
            }

            hbox.setMinimumHeight(50);
            hbox.setPaddingRelative(0,10,0,10);
            hbox.setGravity(Gravity.CENTER);
            hbox.addView(getTextViewPosition(position));
            hbox.addView(getTextViewName(team.getName()));
            hbox.addView(getRelativeLayoutStat(team));
            vbox.addView(hbox);
        }
    }

    /**
     * Override onPause method
     * Clear the request queue
     */
    @Override
    public void onPause() {
        if (queue != null) {
            queue.cancelAll("LEADERBOARD");
        }
        super.onPause();
    }

    /**
     * Generate stat TextView
     * @param stat
     * @param startOf
     * @return
     */
    private TextView createTeamViewStat(int stat, int startOf) {
        TextView t = new TextView(getActivity());
        t.setText(String.valueOf(stat));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        t.setTextSize(14);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(Color.WHITE);
        int width = (int) Math.floor(Utils.convertDpToPx(34));
        t.setMaxWidth(width);
        t.setMinWidth(width);

        RelativeLayout.LayoutParams layoutParams =(RelativeLayout.LayoutParams) t.getLayoutParams();
        if (startOf != -1) {
            layoutParams.addRule(RelativeLayout.START_OF, startOf);
        }
        else {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        }
        t.setLayoutParams(layoutParams);

        t.setId(Utils.generateViewId());
        return t;
    }

    /**
     * Setup teams from jsonrequest response
     * @param response
     */
    private void setupTeams(JSONObject response) {
        teamList = new ArrayList<>();
        try {
            JSONArray jsonArray = response.getJSONArray("response").getJSONObject(0).getJSONObject("league").getJSONArray("standings").getJSONArray(0);
            for (int i = 0 ; i < jsonArray.length() ; i++) {
                String name = (String) jsonArray.getJSONObject(i).getJSONObject("team").get("name");
                int points = (int) jsonArray.getJSONObject(i).get("points");
                int wins = (int) jsonArray.getJSONObject(i).getJSONObject("all").get("win");
                int losses = (int) jsonArray.getJSONObject(i).getJSONObject("all").get("lose");
                int played = (int) jsonArray.getJSONObject(i).getJSONObject("all").get("played");
                String logoURL = (String) jsonArray.getJSONObject(i).getJSONObject("team").get("logo");
                teamList.add(new Team(name, points, wins, losses, played, logoURL));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Doing the JSON Request to get leaderboard
     */
    private void jsonRequest() {
        String url = "https://api-football-v1.p.rapidapi.com/v3/standings?season=2021&league=" + leagueID;

        if (keep_leaderboard.containsKey(leagueID)) {
            if (!keep_leaderboard.containsKey(preLeagueID)) {
                keep_leaderboard.put(preLeagueID, teamList);
            }
            teamList = keep_leaderboard.get(leagueID);
            renderTeams();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                keep_leaderboard.put(preLeagueID, teamList);
                setupTeams(response);
                renderTeams();
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

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setTag("LEADERBOARD");
        queue.add(jsonObjectRequest);
    }
}