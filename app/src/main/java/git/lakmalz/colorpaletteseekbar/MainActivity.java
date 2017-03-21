package git.lakmalz.colorpaletteseekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


/**
 * Created by Lakmal Weerasekara on 20/3/17.
 */

public class MainActivity extends AppCompatActivity {
    private ColorSeekBar mColorSeekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mColorSeekBar = (ColorSeekBar) findViewById(R.id.colorSlider);
        final TextView textView = (TextView) findViewById(R.id.textView);

//        mColorSeekBar.setAlphaBarPosition(10);
//        mColorSeekBar.setBarMargin(10);
        mColorSeekBar.setBarHeight(12);
//        mColorSeekBar.setColor(0xffffff);
//        mColorSeekBar.setColorBarPosition(0xffffff);
        mColorSeekBar.setColorSeeds(R.array.material_colors);
//        mColorSeekBar.setMaxPosition(100);
//        mColorSeekBar.setColorBarPosition(10);
//        mColorSeekBar.setShowAlphaBar(true);
        mColorSeekBar.setThumbHeight(10);
//        mColorSeekBar.setColorSeeds(R.array.material_colors);

        mColorSeekBar.setMaxPosition(100);
        mColorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int color, boolean isAlphaBar) {
                if (isAlphaBar) {
                    textView.setTextColor(mColorSeekBar.getColor());
                } else {
                    textView.setTextColor(mColorSeekBar.getOpacityColor());
                }
            }
        });

        mColorSeekBar.setShowAlphaBar(true);
        mColorSeekBar.setShowBrightnessBar(true);
    }
}
