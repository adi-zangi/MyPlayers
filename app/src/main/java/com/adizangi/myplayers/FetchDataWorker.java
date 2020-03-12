package com.adizangi.myplayers;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class FetchDataWorker extends Worker {

    private Document mRankings;
    private Document wRankings;
    private Document tSchedule;
    private Document ySchedule;

    public FetchDataWorker(@NonNull Context context,
                           @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            getHTMLDocuments();
            TotalPlayersFetcher playersFetcher =
                    new TotalPlayersFetcher(mRankings, wRankings);
            PlayerStatsFetcher statsFetcher =
                    new PlayerStatsFetcher(mRankings, wRankings);
            NotificationFetcher notifFetcher =
                    new NotificationFetcher(tSchedule, ySchedule);
            List<String> totalPlayers = playersFetcher.getTotalPlayersList();
            Map<String, PlayerStats> stats = statsFetcher.getPlayerDetailsMap();
            List<String> notificationList = notifFetcher.getNotificationList();
            FileManager fileManager = new FileManager(getApplicationContext());
            fileManager.storeTotalPlayers(totalPlayers);
            fileManager.storePlayerStats(stats);
            fileManager.storeNotificationList(notificationList);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void getHTMLDocuments() throws IOException {
        TimeZone timeZone = TimeZone.getTimeZone("US/Eastern");
        Calendar calendar = Calendar.getInstance(timeZone, Locale.US);
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyyMMdd", Locale.US);
        String dateOfYesterday = dateFormat.format(calendar.getTime());
        mRankings = Jsoup.connect
                ("https://www.espn.com/tennis/rankings").get();
        wRankings = Jsoup.connect
                ("https://www.espn.com/tennis/rankings/_/type/wta").get();
        tSchedule = Jsoup.connect
                ("http://www.espn.com/tennis/dailyResults").get();
        ySchedule = Jsoup.connect
                ("http://www.espn.com/tennis/dailyResults?date=" +
                        dateOfYesterday).get();
    }

}
