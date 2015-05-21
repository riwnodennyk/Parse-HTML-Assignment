package ua.kulku.parsehtml;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArticleFragment extends Fragment {

    private final Api mApi = new RestAdapter.Builder()
            .setEndpoint("http://api.naij.com")
//            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build()
            .create(Api.class);
    private LayoutInflater mInflater;
    private LinearLayout mCreatable;

    public ArticleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCreatable = (LinearLayout) getView().findViewById(R.id.parsed_content_article);
        mInflater = LayoutInflater.from(getActivity());

        mApi.getArticle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Article>() {
                    @Override
                    public void call(Article article) {
                        ((TextView) getView().findViewById(R.id.title_article)).setText(article.title);

                        String createdDate = SimpleDateFormat.getDateInstance(DateFormat.SHORT).format(article.created * 1000);
                        ((TextView) getView().findViewById(R.id.date_article)).setText(createdDate);

                        Document doc = Jsoup.parse(article.content);
                        final Element root = doc.child(0).child(1);
                        addViewBlocksForChildren(root);
                    }
                });
    }

    private TextView textBlock(Element element) {
        TextView textView = (TextView) mInflater.inflate(R.layout.part_text_block, mCreatable, false);
        textView.setText(Html.fromHtml(element.html()));
        return textView;
    }

    private void addViewBlocksForChildren(Element root) {
        View view = viewBlock(root);
        if (view != null)
            mCreatable.addView(view);

        for (Element element : root.children()) {
            addViewBlocksForChildren(element);
        }
    }

    private View viewBlock(Element element) {
        if (element == null) {
            return null;
        } //todo extract constants "p" "a" "img"

        if ("p".equals(element.tagName())) {
            if (element.children().size() == 1) {
                Element presumableA = element.child(0);
                if (presumableA.tagName().equals("a")) {
                    if (presumableA.children().size() == 1) {
                        Element presumableImg = presumableA.children().get(0);
                        if (presumableImg.tagName().equals("img")) {
                            return imageViewBlock(presumableImg);
                        }
                    }
                }
            }

            return textBlock(element);
        }

        //todo get youtube video id -> Youtube library

        return null;
    }

    private ImageView imageViewBlock(Element img) {
        ImageView imageView = (ImageView) mInflater.inflate(R.layout.part_image_view_block, mCreatable, false);
        String src = img.attr("src");
        Picasso.with(getActivity())
                .load(src)
//                .centerCrop()
//                .fit()
                .into(imageView);
        return imageView;
    }

}
