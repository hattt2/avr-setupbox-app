
package com.example.livestreamstb;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private View errorLayout;
    private TextView errorText;
    private Button retryButton;
    private ExoPlayer player;

    private final String sheetUrl = "https://script.google.com/macros/s/AKfycbzBRA1mpIpMGEJZOTwzMRkxt_vDOVg8ibRpxFkvSfizkn0F2he0F59koTC1w8QtdFOp/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerView = findViewById(R.id.player_view);
        errorLayout = findViewById(R.id.error_layout);
        errorText = findViewById(R.id.error_text);
        retryButton = findViewById(R.id.retry_button);

        retryButton.setOnClickListener(v -> initPlayback());
        initPlayback();
    }

    private void initPlayback() {
        if (!isOnline()) {
            showError("No internet connection");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Scanner scanner = new Scanner(new URL(sheetUrl).openStream());
                final String streamUrl = scanner.useDelimiter("\A").next().trim();
                runOnUiThread(() -> startPlayer(streamUrl));
            } catch (Exception e) {
                runOnUiThread(() -> showError("Failed to fetch stream URL"));
            }
        });
    }

    private void startPlayer(String url) {
        hideError();
        playerView.setVisibility(View.VISIBLE);
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaSource mediaSource = new HlsMediaSource.Factory(new DefaultDataSource.Factory(this))
                .createMediaSource(MediaItem.fromUri(Uri.parse(url)));

        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
    }

    private void showError(String message) {
        playerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }

    private void hideError() {
        errorLayout.setVisibility(View.GONE);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) player.release();
    }
}
