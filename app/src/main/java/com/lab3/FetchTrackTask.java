package com.lab3;

import android.os.AsyncTask;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class FetchTrackTask extends AsyncTask<Void, Void, String> {
    private DatabaseHelper dbHelper;
    private TextView statusTextView;

    public FetchTrackTask(DatabaseHelper dbHelper, TextView statusTextView) {
        this.dbHelper = dbHelper;
        this.statusTextView = statusTextView;
    }

    @Override
    protected String doInBackground(Void... voids) {
        StringBuilder resultBuilder = new StringBuilder();

        try {
            Document doc = Jsoup.connect("https://www.loveradio.ru/player/history")
                    .header("locale", "ru_RU")
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, как Gecko) Chrome/103.0.5060.53 Safari/537.36")
                    .get();

            Elements trackElements = doc.select("li[data-v-f7ae7722]");

            for (Element trackElement : trackElements) {
                String artist = trackElement.select(".playlist-item-card__artist-name").text();
                String title = trackElement.select(".playlist-item-card__artist-song").text();
                String timestamp = trackElement.select(".playlist-item-card__artist-time").text();

                if (!artist.isEmpty() && !title.isEmpty() && !timestamp.isEmpty()) {
                    // Проверка уникальности перед вставкой в базу данных

                    dbHelper.insertTrack(artist, title, timestamp);
                    resultBuilder.append(artist).append(" - ").append(title).append(" (").append(timestamp).append(")\n");

                }
            }

            return resultBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка подключения: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка обработки данных.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            statusTextView.setText("Треки из истории:\n" + result);
        } else {
            statusTextView.setText("Не удалось получить данные о треках.");
        }
    }
}
