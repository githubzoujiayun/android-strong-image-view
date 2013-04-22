package cn.erhu.android.strong.activity;

import android.app.Activity;
import android.os.Bundle;
import cn.erhu.android.strong.R;
import cn.erhu.android.strong.image.StrongImageView;

public class TestActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        StrongImageView world_map_iv = (StrongImageView) findViewById(R.id.bg_3);
        world_map_iv.setImageUrl("http://img.lequshi.com/desk/3729/y_1343358336.jpg");

    }
}
