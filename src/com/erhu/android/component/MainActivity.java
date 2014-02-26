package com.erhu.android.component;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.erhu.android.component.strongimageview.StrongImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private List<Product> products;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ListView demoLv = (ListView) findViewById(R.id.demo_lv);
        products = new ArrayList<Product>();
        products.add(new Product("iPhone1", "http://photo.meile.com/d07/GJ/23/p_gjekb503376b_460.jpeg"));
        products.add(new Product("iPhone2", "http://photo.meile.com/d76/64/36/p_6447c3793c4c_460.jpeg"));
        products.add(new Product("iPhone3", "http://photo.meile.com/d17/WN/75/p_wndf13670711_460.jpeg"));
        products.add(new Product("iPhone4", "http://photo.meile.com/d04/YW/50/p_ywz557deda68_460.jpeg"));
        products.add(new Product("iPhone5", "http://photo.meile.com/d35/8Q/30/p_8q2846e696eb_460.jpeg"));
        products.add(new Product("iPhone6", "http://photo.meile.com/d78/2Q/60/p_2qrxb6f58cb2_460.jpeg"));
        products.add(new Product("iPhone7", "http://photo.meile.com/d85/LI/61/p_liiud67badb9_460.jpeg"));
        products.add(new Product("iPhone8", "http://photo.meile.com/d13/6Y/38/p_6yd3736f160d_460.jpeg"));
        products.add(new Product("iPhone9", "http://photo.meile.com/d32/XJ/82/p_xjxe1b89d220_460.jpeg"));
        products.add(new Product("iPhone10", "http://photo.meile.com/d01/5P/12/p_5poz079b8465_460.jpeg"));
        products.add(new Product("iPhone11", "http://photo.meile.com/d37/FX/17/p_fxy0f85dd189_460.jpeg"));
        products.add(new Product("iPhone12", "http://photo.meile.com/d42/QY/84/p_qyhsd486b48e_460.jpeg"));
        products.add(new Product("iPhone13", "http://photo.meile.com/d21/QH/21/p_qhx2af3c3d15_460.jpeg"));
        products.add(new Product("iPhone14", "http://photo.meile.com/d22/6W/24/p_6wwv5aad88de_460.jpeg"));
        products.add(new Product("iPhone15", "http://photo.meile.com/d24/V5/32/p_v5xh06de64e0_460.jpeg"));
        products.add(new Product("iPhone16", "http://photo.meile.com/d14/C9/75/p_c9gk57ac2b72_460.jpeg"));
        products.add(new Product("iPhone17", "http://photo.meile.com/d95/RF/68/p_rfcs5007b0c3_460.jpeg"));
        products.add(new Product("iPhone18", "http://photo.meile.com/d65/JM/19/p_jmmt1edbff41_460.jpeg"));
        products.add(new Product("iPhone19", "http://photo.meile.com/d98/F6/26/p_f6uy85854e62_460.jpeg"));
        products.add(new Product("iPhone20", "http://photo.meile.com/d77/M4/84/p_m4boc22e6cb1_460.jpeg"));
        products.add(new Product("iPhone21", "http://photo.meile.com/d16/FC/60/p_fcg037e0d810_460.jpeg"));
        products.add(new Product("iPhone22", "http://photo.meile.com/d39/CZ/47/p_czz89e4a03ef_460.jpeg"));
        products.add(new Product("iPhone23", "http://photo.meile.com/d75/4U/83/p_4u4z138ee74b_460.jpeg"));
        products.add(new Product("iPhone24", "http://photo.meile.com/d63/TB/75/p_tb11009137a3_460.jpeg"));
        products.add(new Product("iPhone25", "http://photo.meile.com/d54/28/01/p_28srfeb86136_460.jpeg"));
        products.add(new Product("iPhone26", "http://photo.meile.com/d44/SB/80/p_sbblf1c7242c_460.jpeg"));
        products.add(new Product("iPhone27", "http://photo.meile.com/d23/U3/18/p_u324b3e43217_460.jpeg"));

        demoLv.setAdapter(new MyAdapter(this, android.R.layout.simple_list_item_1));
    }


    private class MyAdapter extends ArrayAdapter<Product> {

        public MyAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            //if (convertView == null) {
                //Log.d(TAG, "convertView is null, inflate a new one");

                convertView = getLayoutInflater().inflate(R.layout.product_item, null);
                holder = new ViewHolder();
                holder.nameTv = (TextView) convertView.findViewById(R.id.item_name);
                holder.imageIv = (StrongImageView) convertView.findViewById(R.id.item_image);
                convertView.setTag(holder);
            /*} else {
                //Log.d(TAG, "convertView is not null, get UI component from viewHolder.");
                holder = (ViewHolder) convertView.getTag();
            }*/

            holder.nameTv.setText(products.get(position).name);
            //String image_url = "http://images.apple.com/cn/macbook-pro/images/overview_hero.jpg";
            String image_url = products.get(position).url;//"http://192.168.1.105:8000/bs.jpg";
            final StrongImageView item_image = holder.imageIv;
            item_image.loadImage(image_url);
            return convertView;
        }

        @Override
        public int getCount() {
            return products.size();
        }

        private class ViewHolder {
            TextView nameTv;
            StrongImageView imageIv;
        }
    }

}
