package fr.allanh.yaya.swipe;

import android.view.View;
import fr.allanh.yaya.ui.matches.MatchesFragment;

public class OnSwipeTouchListenerMatch extends OnSwipeTouchListener {

    private MatchesFragment matchesFragment;

    public OnSwipeTouchListenerMatch(View view, MatchesFragment mf) {
        super(view);
        this.matchesFragment = mf;
    }

    @Override
    protected void onSwipeLeft() {
        matchesFragment.setPreTime(matchesFragment.getCalendar().getTime());
        matchesFragment.addCalendar(1);
        matchesFragment.jsonRequest();
    }

    @Override
    protected void onSwipeRight() {
        matchesFragment.setPreTime(matchesFragment.getCalendar().getTime());
        matchesFragment.addCalendar(-1);
        matchesFragment.jsonRequest();
    }
}
