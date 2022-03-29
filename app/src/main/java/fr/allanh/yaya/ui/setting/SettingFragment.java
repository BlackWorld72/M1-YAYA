package fr.allanh.yaya.ui.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import fr.allanh.yaya.League;
import fr.allanh.yaya.MainActivity;
import fr.allanh.yaya.R;
import fr.allanh.yaya.Team;
import fr.allanh.yaya.TeamSimple;
import fr.allanh.yaya.Utils;

public class SettingFragment extends Fragment {

    private enum AddType{LEAGUE,TEAM}

    private FrameLayout frameLayout;
    private View view;
    private ArrayList<League> fav_leagues = new ArrayList<>();

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private String[] text_search_bar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frameLayout = new FrameLayout(getActivity());
        inflater =(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.fragment_setting, container,false);
        frameLayout.addView(view);

        setupLeagues();
        setupFavoriteLeagues();

        Spinner spinner = view.findViewById(R.id.spinner_language);
        switch (Utils.getActualLanguage()) {
            case "en": spinner.setSelection(0); break;
            case "fr": spinner.setSelection(1); break;
            default: break;
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = adapterView.getItemAtPosition(i).toString();
                if (text.equalsIgnoreCase(getResources().getString(R.string.french)) && !Utils.getActualLanguage().equalsIgnoreCase("fr")) {
                    Utils.updateLanguage("fr");
                }
                else if (text.equalsIgnoreCase(getResources().getString(R.string.english)) && !Utils.getActualLanguage().equalsIgnoreCase("en")) {
                    Utils.updateLanguage("en");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return frameLayout;
    }

    /**
     * Setup all favorite leagues
     */
    private void setupFavoriteLeagues() {
        LinearLayout vbox_fav_leagues = view.findViewById(R.id.vbox_fav_leagues);
        LinearLayout hbox = new LinearLayout(getActivity());
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x-100;

        for (int i = 0 ; i < fav_leagues.size() ; i++) {
            final int val = i;
            if (i % 5 == 0) {
                if (i != 0) {
                    vbox_fav_leagues.addView(hbox);
                }
                hbox = new LinearLayout(getActivity());
                hbox.setGravity(Gravity.CENTER);
            }

            ImageView logo = new ImageView(getActivity());
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width/5, width/5);
            parms.setMargins(10,10,10,10);
            logo.setLayoutParams(parms);
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.remove);
                    builder.setMessage(getContext().getResources().getString(R.string.confirm_remove) + " " + Utils.getLeagueFromId(fav_leagues.get(val).getId()).getName() + " " + getContext().getResources().getString(R.string.confirm_remove_end));
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Utils.removeFavoriteLeague(getActivity(), fav_leagues.get(val).getId() );
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            Utils.setLogo(logo, fav_leagues.get(i).getLogo(),width/5);
            hbox.addView(logo);
        }
        if (hbox.getChildCount() > 0) {
            vbox_fav_leagues.addView(hbox);
        }

        MaterialButton mb = new MaterialButton(getActivity());
        mb.setText(R.string.add);
        mb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPopUp(AddType.LEAGUE);
            }
        });
        vbox_fav_leagues.addView(mb);
    }

    /**
     * Create Pop Up to select a league
     * @param addType
     */
    private void createPopUp(AddType addType) {
        AlertDialog.Builder dialogBuilder;
        AlertDialog dialog;

        dialogBuilder = new AlertDialog.Builder(getContext());
        View popUpView = getLayoutInflater().inflate(R.layout.popup_settings, null);

        listView = popUpView.findViewById(R.id.listViewSearch);
        listView.setVisibility(View.INVISIBLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long l) {
                String item = (String) adapter.getItemAtPosition(position);
                switch (addType) {
                    case LEAGUE:
                        League league = Utils.getLeagueFromName(item);
                        Utils.addFavoriteLeague(getActivity(), league);
                        break;
                    default: return;
                }
            }
        });

        setSearchBar(addType);

        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, text_search_bar);
        listView.setAdapter(arrayAdapter);

        SearchView searchView = popUpView.findViewById(R.id.nav_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.equals("")) {
                    listView.setVisibility(View.INVISIBLE);
                }
                else {
                    arrayAdapter.getFilter().filter(s);
                    listView.setVisibility(View.VISIBLE);
                }

                return false;
            }
        });

        dialogBuilder.setView(popUpView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * Setup fav_leagues
     */
    private void setupLeagues() {
        fav_leagues = Utils.getFavoriteLeagues();
    }

    /**
     * Set searchbar for a specific AddType
     * @param addType
     */
    private void setSearchBar(AddType addType) {
        switch (addType) {
            case LEAGUE:
                ArrayList<League> leagues = getLeagues();
                text_search_bar = new String[leagues.size()];
                for (int i = 0 ; i < leagues.size() ; i++) {
                    text_search_bar[i] = leagues.get(i).getName();
                }
                return;
            default: return;
        }
    }


    /**
     * Get all leagues
     * @return
     */
    private ArrayList<League> getLeagues() {
        ArrayList<League> leagues = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(Utils.JsonDataFromAsset("leagues.json"));
            JSONArray jsonArray = jsonObject.getJSONArray("response");
            for (int i = 0 ; i < jsonArray.length() ; i++) {
                String leagueName = jsonArray.getJSONObject(i).getJSONObject("league").getString("name");
                if (!leaguesContain(leagues, leagueName) && !leaguesContain(fav_leagues, leagueName)) {
                    boolean isCup = jsonArray.getJSONObject(i).getJSONObject("league").getString("type").equalsIgnoreCase("cup");
                    int leagueID = jsonArray.getJSONObject(i).getJSONObject("league").getInt("id");
                    String leagueLogo = jsonArray.getJSONObject(i).getJSONObject("league").getString("logo");
                    leagues.add(new League(leagueName, leagueID, isCup, leagueLogo));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return leagues;
    }

    /**
     * Check if league's array contain league with this name
     * @param leagues
     * @param name
     * @return
     */
    private boolean leaguesContain(ArrayList<League> leagues, String name) {
        for (League l : leagues) {
            if (l.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}