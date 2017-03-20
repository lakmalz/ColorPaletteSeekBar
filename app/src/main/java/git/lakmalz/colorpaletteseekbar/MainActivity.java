package git.lakmalz.colorpaletteseekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ColorSeekBar mColorSeekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mColorSeekBar = (ColorSeekBar) findViewById(R.id.colorSlider);
        final TextView textView = (TextView) findViewById(R.id.textView);

        mColorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int color, boolean isAlphabar) {
                if (isAlphabar) {
                    textView.setTextColor(mColorSeekBar.getColor());
                } else {
                    textView.setTextColor(mColorSeekBar.getOpacityColor());
                }
            }
        });

        mColorSeekBar.setBarHeight((float) 12);
        mColorSeekBar.setShowAlphaBar(true);
        mColorSeekBar.setShowOpacityBar(true);
    }
}
