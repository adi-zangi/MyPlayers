/*
   Gets statistics about each player from the list that TotalPlayersFetcher returns
   Information is taken from the ESPN website
 */

package com.adizangi.tennisplayerstracker.network_calls;

import com.adizangi.tennisplayerstracker.utils_data.PlayerStats;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PlayerStatsFetcher {

    private Document mRankings;
    private Document wRankings;

    /*
       Constructs a PlayerStatsFetcher with the given HTML documents of the
       men's tennis rankings and women's tennis rankings from ESPN
     */
    public PlayerStatsFetcher(Document mRankings, Document wRankings) {
        this.mRankings = mRankings;
        this.wRankings = wRankings;
    }

    /*
       Returns a map from each player to a PlayerStats object
       The ESPN website doesn't have player information when a new year
       starts and there have not been any tennis tournaments in the new year
       In this case, returns an empty map
       May throw IOException
     */
    public Map<String, PlayerStats> getPlayerStatsMap() throws IOException {
        Map<String, PlayerStats> stats = new HashMap<>();
        Element mRankingsTable = mRankings.selectFirst("table");
        if (mRankingsTable == null) {
            return new HashMap<>();
        }
        Elements mRows = mRankingsTable.select("tr");
        Element wRankingsTable = wRankings.selectFirst("table");
        Elements wRows = wRankingsTable.select("tr");
        int mNumOfRows = mRows.size();
        int wNumOfRows = wRows.size();
        for (int rowIndex = 1; rowIndex < 101; rowIndex++) {
            if (rowIndex < mNumOfRows) {  // Check is needed due to bug in the website
                Elements mColumns = mRows.get(rowIndex).select("td");
                String playerName = mColumns.get(1).text();
                String playerRanking = mColumns.get(0).text();
                Element playerNameLink = mColumns.get(1).selectFirst("a");
                String playerURL = playerNameLink.attr("abs:href");
                Document playerDocument = Jsoup.connect(playerURL).get();
                String playerKey = playerName + " (" + playerRanking + ")";
                PlayerStats playerStats = getPlayerStats(playerDocument,
                        playerName, playerRanking);
                stats.put(playerKey, playerStats);
            }
            if (rowIndex < wNumOfRows) {
                Elements wColumns = wRows.get(rowIndex).select("td");
                String playerName = wColumns.get(1).text();
                String playerRanking = wColumns.get(0).text();
                Element playerNameLink = wColumns.get(1).selectFirst("a");
                String playerURL = playerNameLink.attr("abs:href");
                Document playerDocument = Jsoup.connect(playerURL).get();
                String playerKey = playerName + " (" + playerRanking + ")";
                PlayerStats playerStats = getPlayerStats(playerDocument,
                        playerName, playerRanking);
                stats.put(playerKey, playerStats);
            }
        }
        return stats;
    }

    /*
       Returns a PlayerStats object for the player whose information is in
       the given document
     */
    private PlayerStats getPlayerStats(Document playerDocument,
                                       String name, String rankingNumber) {
        String ranking = "Current ranking: " + rankingNumber;
        String titles = getTitles(playerDocument);
        int latestResultIndex = getLatestResultIndex(playerDocument);
        String standing = getTournamentStanding
                (playerDocument, latestResultIndex);
        String currentTournament = "";
        String latestMatchResult = "";
        if (!standing.equals("not playing")) {
            currentTournament = getCurrentTournament(playerDocument);
            latestMatchResult = getLatestMatchResult
                    (playerDocument, latestResultIndex);
        }
        String upcomingMatch = "";
        if (standing.contains("advanced")) {
            upcomingMatch = getUpcomingMatch
                    (playerDocument, latestResultIndex);
        }
        return new PlayerStats(
                name,
                ranking,
                titles,
                standing,
                currentTournament,
                latestMatchResult,
                upcomingMatch);
    }

    /*
       Returns the name of the player whose information is in the given document
     */
    private String getName(Document playerDocument) {
        return playerDocument.selectFirst("h1").text();
    }

    /*
       Returns the ranking of the player whose information is in the given document
     */
    private String getRanking(Document playerDocument) {
        Element detailsList = playerDocument.select("ul").get(1);
        Elements listItems = detailsList.select("li");
        String rankingFullText = listItems.get(0).text();
        return rankingFullText.substring(rankingFullText.indexOf("#") + 1);
    }

    /*
       Returns the number of singles titles of the player whose information is
       in the given document
     */
    private String getTitles(Document playerDocument) {
        Element playerStatsDiv = playerDocument.selectFirst("div.player-stats");
        if (playerStatsDiv.select("p").isEmpty()) { // check needed due to bug in website
            return "Singles titles: unknown";
        }
        String statsFullTitle = playerStatsDiv.selectFirst("p").text();
        String year = statsFullTitle.substring(0, statsFullTitle.indexOf(" "));
        Element statsTable = playerStatsDiv.selectFirst("table");
        String singlesTitles = statsTable.select("tr").get(1)
                .selectFirst("td").text();
        return year + " singles titles: " + singlesTitles;
    }

    /*
       Returns the index of the row that contains the latest singles match result
       of the player whose information is in the given document
       Returns -1 if the player is not currently playing singles in a tournament
     */
    private int getLatestResultIndex(Document playerDocument) {
        Element latestTournamentDiv =
                playerDocument.selectFirst("#my-players-table");
        String latestTournamentTitle = latestTournamentDiv.selectFirst("h4")
                .text();
        if (!latestTournamentTitle.equals("CURRENT TOURNAMENT")) {
            return -1;
        }
        Element latestTournamentTable =
                latestTournamentDiv.select("table").get(1);
        Elements rows = latestTournamentTable.select("tr");
        String tournamentType = rows.get(1).text();
        if (!tournamentType.contains("Singles")) {
            return -1;
        }
        int numOfRows = rows.size();
        int row = 2;
        while (row < numOfRows) {
            Elements columns = rows.get(row).select("td");
            if (columns.size() < 4) {
                break;
            }
            row++;
        }
        return row - 1;
    }

    /*
       Returns the tournament standing of the player whose information is
       in the given document, using the given index of the latest result row
       The tournament standing tells whether the player advanced to the next round,
       is out of the tournament, or is not playing in a tournament
     */
    private String getTournamentStanding(Document playerDocument,
                                         int latestResultIndex) {
        if (latestResultIndex == -1) {
            return "not playing";
        }
        Element latestTournamentDiv =
                playerDocument.selectFirst("#my-players-table");
        Element latestTournamentTable =
                latestTournamentDiv.select("table").get(1);
        Elements rows = latestTournamentTable.select("tr");
        Element latestResultRow = rows.get(latestResultIndex);
        Elements columns = latestResultRow.select("td");
        String matchResult = columns.get(2).text();
        if (matchResult.equals("-")) {
            String roundNumber = columns.get(0).text();
            return "advanced to " + roundNumber;
        } else if (!matchResult.equals("W")) {
            return "out";
        }
        return "winner";
    }

    /*
       Returns the name of the tournament that the player whose information is
       in the given document is playing in
       Only safe to call if the player is currently in a tournament, which is
       given in the return value of getTournamentStanding()
     */
    private String getCurrentTournament(Document playerDocument) {
        Element latestTournamentDiv =
                playerDocument.selectFirst("#my-players-table");
        return latestTournamentDiv.selectFirst("a").text();
    }

    /*
       Returns the latest match result of the player whose information is
       in the given document, using the given index of the latest result row
       Only safe to call if the player is currently in a tournament, which is
       given in the return value of getTournamentStanding()
     */
    private String getLatestMatchResult(Document playerDocument,
                                        int latestResultIndex) {
        Element latestTournamentDiv =
                playerDocument.selectFirst("#my-players-table");
        Element latestTournamentTable =
                latestTournamentDiv.select("table").get(1);
        Elements rows = latestTournamentTable.select("tr");
        Element latestResultRow = rows.get(latestResultIndex);
        Elements columns = latestResultRow.select("td");
        String matchResult = columns.get(2).text();
        if (matchResult.equals("-")) {
            if (latestResultIndex == 2) {
                return "";
            }
            latestResultRow = rows.get(latestResultIndex - 1);
            columns = latestResultRow.select("td");
        }
        String round = columns.get(0).text();
        String opponent = columns.get(1).text();
        if (opponent.isEmpty()) {
            return round + "- automatically advanced";
        }
        String score = columns.get(3).text();
        return round + "- " + opponent + " " + score;
    }

    /*
       Returns the upcoming match of the player whose information is
       in the given document, using the given index of the latest result row
       Returns an empty string if the player does not have an upcoming match today
       Only safe to call if the player advanced to the next round, which is
       given in the return value of getTournamentStanding()
     */
    private String getUpcomingMatch(Document playerDocument,
                                    int latestResultIndex) {
        Element latestTournamentDiv =
                playerDocument.selectFirst("#my-players-table");
        Element latestTournamentTable =
                latestTournamentDiv.select("table").get(1);
        Elements rows = latestTournamentTable.select("tr");
        Element upcomingMatchRow = rows.get(latestResultIndex);
        Elements columns = upcomingMatchRow.select("td");
        String upcomingMatchDetails = columns.get(3).text();
        if (upcomingMatchDetails.contains("ET")) {
            int secondSpaceIndex = upcomingMatchDetails
                    .indexOf(" ", upcomingMatchDetails.indexOf(" ") + 1);
            String upcomingMatchDate =
                    upcomingMatchDetails.substring(0, secondSpaceIndex);
            String pattern = "MMMMM d";
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
            String date = dateFormat.format(new Date());
            if (date.equals(upcomingMatchDate)) {
                String upcomingMatchTime =
                        upcomingMatchDetails.substring(secondSpaceIndex + 1);
                String playerName = playerDocument.selectFirst("h1").text();
                return playerName + " " + upcomingMatchTime;
            }
        }
        return "";
    }

}
