package org.coursera.android.student.zee.modernartui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.app.Activity;

import java.util.Random;


public class MainActivity extends Activity {
    private static String TAG = "org.coursera.android.student.zee.modernartui.DEBUG";
    private static String URL_MOMA = "http://moma.org";
    private static String KEY_INITIAL_COLORS = "key.initital.colors";
    private static String KEY_FINAL_COLORS = "key.final.colors";

    private ColoredBox[] boxes;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int[] ids = new int[]{ R.id.box1, R.id.box2, R.id.box3, R.id.box4, R.id.box5 };
        this.boxes = new ColoredBox[ids.length];

        if (savedInstanceState != null) {
            int[] initialColors = savedInstanceState.getIntArray(KEY_INITIAL_COLORS);
            int[] finalColors = savedInstanceState.getIntArray(KEY_FINAL_COLORS);
            assert initialColors.length == finalColors.length : "Initial & final color array lengths must be equal";

            for(int i = 0; i < initialColors.length; i++) {
                this.boxes[i] = new ColoredBox(ids[i], initialColors[i], finalColors[i]);
            }
        }
        else {
            // Randomize box colors and store the initial and final colors of the box. This allows
            // the seek bar (slider) to change box colors from initial to final and vice versa.
            Random r = new Random();
            for(int i = 0; i < ids.length; i++) {
                this.boxes[i] = new ColoredBox(ids[i], r.nextInt(), r.nextInt());
            }
        }

        this.seekBar = (SeekBar)findViewById(R.id.seekBar);
        assert seekBar != null : "Seekbar missing; it is part of the UI requirements";
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    for(ColoredBox box: MainActivity.this.boxes) {
                        box.transition(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        int percentage = this.seekBar.getProgress();
        for(ColoredBox b: this.boxes) {
            b.transition(percentage);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int[] initialColors = new int[this.boxes.length];
        int[] finalColors = new int[this.boxes.length];
        for(int i = 0; i < initialColors.length; i++) {
            initialColors[i] = this.boxes[i].getInitialColor();
            finalColors[i] = this.boxes[i].getFinalColor();
        }

        outState.putIntArray(KEY_INITIAL_COLORS, initialColors);
        outState.putIntArray(KEY_FINAL_COLORS, finalColors);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_text)
                    .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_MOMA));
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ColoredBox {
        private final ViewGroup view;
        private final int initialColor;
        private final int[] rgbDeltas;

        public ColoredBox(int viewId, int initialColor, int finalColor) {
            this((ViewGroup)MainActivity.this.findViewById(viewId), initialColor, finalColor);
        }

        public ColoredBox(ViewGroup view, int initialColor, int finalColor) {
            this.view = view;
            this.initialColor = initialColor;

            // Deltas won't ever change to cache it.
            this.rgbDeltas = new int[]{
                    (Color.red(finalColor) - Color.red(initialColor)) / 100,
                    (Color.green(finalColor) - Color.green(initialColor)) / 100,
                    (Color.blue(finalColor) - Color.blue(initialColor)) / 100
            };
        }

        public void transition(int percentage) {
            if (percentage < 0 || percentage > 100) {
                throw new IllegalArgumentException("percentage outside range");
            }

            int percentageColor = this.getPercentageColor(percentage);
            this.view.setBackgroundColor(percentageColor);
        }

        public int getInitialColor() {
            return this.initialColor;
        }

        public int getFinalColor() {
            return this.getPercentageColor(100);
        }

        private int getPercentageColor(int percentage) {
            assert percentage >= 0 && percentage <= 100 : "percentage out of bounds!";

            int newRed = Color.red(this.initialColor) + (percentage * this.rgbDeltas[0]);
            int newGreen = Color.green(this.initialColor) + (percentage * this.rgbDeltas[1]);
            int newBlue = Color.blue(this.initialColor) + (percentage * this.rgbDeltas[2]);
            return Color.rgb(newRed, newGreen, newBlue);
        }
    }
}
