package info.holliston.high.app.pager.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import info.holliston.high.app.detail.NewsDetailFragment;
import info.holliston.high.app.datamodel.Article;

public class NewsPagerAdapter extends FragmentStatePagerAdapter {
    private List<Article> articles;
    public NewsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setArticles(List<Article> list) {
        this.articles = list;
        notifyDataSetChanged();
    }
    @Override
    public Fragment getItem(int i) {

        Article article = articles.get(i);

        NewsDetailFragment newFragment = new NewsDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("detail_article", article);
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    public int getCount() {
        int count=0;
        if (articles!=null) {
            count = articles.size();
        }
        return count;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return articles.get(position).title;
    }


}
