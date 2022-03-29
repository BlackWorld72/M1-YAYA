package fr.allanh.yaya.swipe;

import android.view.View;
import fr.allanh.yaya.ui.leaderboard.LeaderboardFragment;

public class OnSwipeTouchListenerLeaderboard extends OnSwipeTouchListener {

    private LeaderboardFragment leaderboardFragment;

    public OnSwipeTouchListenerLeaderboard(View view, LeaderboardFragment mf) {
        super(view);
        this.leaderboardFragment = mf;
    }

    @Override
    protected void onSwipeLeft() {
        leaderboardFragment.nextLeague();
    }

    @Override
    protected void onSwipeRight() {
        leaderboardFragment.previousLeague();
    }
}
