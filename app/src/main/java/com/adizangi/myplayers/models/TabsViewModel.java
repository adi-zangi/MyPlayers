/*
   ViewModel for the views in MainActivity's tabs
 */

package com.adizangi.myplayers.models;

import android.app.Application;

import com.adizangi.myplayers.utils_data.FileManager;
import com.adizangi.myplayers.utils_data.PlayerStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class TabsViewModel extends AndroidViewModel {

    private final List<String> totalPlayers;
    private final Map<String, PlayerStats> statsMap;
    private List<String> myPlayers;
    private List<PlayerStats> myPlayersStats;
    private MutableLiveData<String> addedPlayer;
    private MutableLiveData<String> removedPlayer;
    private FileManager fileManager;

    /*
       Constructs a TabsViewModel with the given Application reference
       Retrieves saved data and initialized the data for the views
       Initializes the observable data to empty values
     */
    public TabsViewModel(@NonNull Application application) {
        super(application);
        fileManager = new FileManager(application);
        totalPlayers = fileManager.readTotalPlayers();
        statsMap = fileManager.readPlayerStats();
        myPlayers = fileManager.readMyPlayers();
        myPlayersStats = new ArrayList<>();
        for (String player : myPlayers) {
            PlayerStats playerStats = statsMap.get(player);
            myPlayersStats.add(playerStats);
        }
        Collections.sort(myPlayersStats, Collections.reverseOrder());
        addedPlayer = new MutableLiveData<>();
        removedPlayer = new MutableLiveData<>();
    }

    /*
       Returns a list of the user's selected players
     */
    public List<String> getMyPlayers() {
        return myPlayers;
    }

    /*
       Returns a list of all the players the user can add
     */
    public List<String> getTotalPlayers() {
        return totalPlayers;
    }

    /*
       Returns a list of PlayerStats objects corresponding to each of the
       user's selected players
       The list is sorted in descending order
     */
    public List<PlayerStats> getMyPlayersStats() {
        return myPlayersStats;
    }

    /*
       Returns a MutableLiveData containing the player that was added
     */
    public MutableLiveData<String> getAddedPlayer() {
        return addedPlayer;
    }

    /*
       Returns a MutableLiveData containing the player that was removed
     */
    public MutableLiveData<String> getRemovedPlayer() {
        return removedPlayer;
    }

    /*
       Adds the given player to the list of the user's players and saves the
       list
       Sets the value of addedPlayer to the player so the change will be
       observed
     */
    public void addPlayer(String player) {
        addedPlayer.setValue(player);
        myPlayers.add(player);
        fileManager.storeMyPlayers(myPlayers);
    }

    /*
       Adds a PlayerStats object that corresponds to the given player into the
       PlayerStats list
       The list remains sorted
     */
    public void addPlayerStats(String player) {
        PlayerStats playerStats = statsMap.get(player);
        myPlayersStats.add(playerStats);
        Collections.sort(myPlayersStats, Collections.reverseOrder());
    }

    /*
       Removes the given player from the list of the user's players
       Sets the value of removedPlayer to the player so the change will be
       observed
     */
    public void removePlayer(String player) {
        removedPlayer.setValue(player);
        myPlayers.remove(player);
        fileManager.storeMyPlayers(myPlayers);
    }

    /*
       Removes the PlayerStats object that corresponds to the given player from
       the PlayerStats list
       The list remains sorted
     */
    public void removePlayerStats(String player) {
        int size = myPlayersStats.size();
        for (int i = 0; i < size; i++) {
            PlayerStats playerStats = myPlayersStats.get(i);
            String name = playerStats.getName();
            String fullRanking = playerStats.getRanking();
            String ranking = fullRanking.substring(fullRanking.indexOf(":") + 2);
            String playerAtIndex = name + " (" + ranking + ")";
            if (playerAtIndex.equals(player)) {
                myPlayersStats.remove(i);
                break;
            }
        }
    }

}
