package com.sungshin.biking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

public class Adapter extends PagerAdapter {
    private int[] images = {R.drawable.menual, R.drawable.start, R.drawable.gps, R.drawable.search,
            R.drawable.exit, R.drawable.gauge, R.drawable.explain, R.drawable.end};
    private LayoutInflater inflater;
    private Context context;

    public Adapter (Context context){
        this.context = context;
    }
    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // object를 LinearLayout 형태로 형변환했을 때 view와 같은지 여부를 반환
//        return view == ((LinearLayout)object); 으로 했을때 오류 나서 View 로 바꿈..
        return view == object;
    }

    // 각각의 item을 인스턴스 화
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //초기화
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.activity_explanation, container, false);
        ImageView imageView = (ImageView)v.findViewById(R.id.explanation);
        imageView.setImageResource(images[position]);
        container.addView(v);
        return v;
    }

    //할당을 해제
    @Override
    public void destroyItem(ViewGroup container,
                            int position, Object object) {
        container.invalidate();
//        super.destroyItem(container, position, object);
    }
}
