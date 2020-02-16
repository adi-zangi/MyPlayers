/*
   Gets a list of all the players the user can add
   The players in the list are based on professional tennis player rankings in
   the ESPN website
 */

package com.adizangi.myplayers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class PlayerChoicesGetter {

    private Document mRankings;
    private Document wRankings;

    /*
       Constructs a PlayerChoicesGetter with the given HTML documents of the
       men's tennis rankings and women's tennis rankings from ESPN
     */
    public PlayerChoicesGetter(Document mRankings, Document wRankings) {
        this.mRankings = mRankings;
        this.wRankings = wRankings;
    }

    /*
       Returns the player choices list
       The list contains the top 100 men tennis players and the top 100 women
       tennis players
       It is sorted by ranking in descending order, and with each men tennis
       player followed by a woman tennis player with the same ranking
     */
    public List<String> getPlayerChoicesList() {
        List<String> playerChoices = new ArrayList<>();
        Element mRankingsTable = mRankings.selectFirst("table");
        Elements mRows = mRankingsTable.select("tr");
        Element wRankingsTable = wRankings.selectFirst("table");
        Elements wRows = wRankingsTable.select("tr");
        int mNumOfRows = mRows.size();
        int wNumOfRows = wRows.size();
        for (int rowIndex = 1; rowIndex < 101; rowIndex++) {
            if (rowIndex < mNumOfRows) {  // Check is needed due to bug in the website
                Elements mColumns = mRows.get(rowIndex).select("td");
                String playerName = mColumns.get(2).text();
                String playerRanking = mColumns.get(0).text();
                playerChoices.add(playerName + " (" + playerRanking + ")");
            }
            if (rowIndex < wNumOfRows) {
                Elements wColumns = wRows.get(rowIndex).select("td");
                String playerName = wColumns.get(2).text();
                String playerRanking = wColumns.get(0).text();
                playerChoices.add(playerName + " (" + playerRanking + ")");
            }
        }
        return playerChoices;
    }

}
