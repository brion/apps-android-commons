package org.wikimedia.commons;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class WelcomeActivity extends Activity {
    private ViewPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        pager = (ViewPager)findViewById(R.id.welcomePager);
        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(View view, Object o) {
                return (view == o);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                int[] items = new int[] {
                        R.layout.welcome_wikipedia,
                        R.layout.welcome_copyright,
                        R.layout.welcome_final
                };
                View view = getLayoutInflater().inflate(items[position], container);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object obj) {
                container.removeAllViews();
            }
        });
    }
}
