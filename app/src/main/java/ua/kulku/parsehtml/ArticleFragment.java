package ua.kulku.parsehtml;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArticleFragment extends Fragment {

    private final Api mApi = new RestAdapter.Builder()
            .setEndpoint("http://api.naij.com")
            .build()
            .create(Api.class);
    private LayoutInflater mInflater;
    private LinearLayout mCreatable;
    private LinkedList<ImageView> mImageViews = new LinkedList<>();

    public ArticleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCreatable = (LinearLayout) getView().findViewById(R.id.parsed_content_article);
        mInflater = LayoutInflater.from(getActivity());
        final ScrollView scrollView = (ScrollView) getView();
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        scrollView.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        boolean shouldShowActionBar = !isAnyImageViewVisibleOnScreen();
                        @SuppressWarnings("ConstantConditions") boolean showing = actionBar.isShowing();
                        if (shouldShowActionBar && !showing) {
                            actionBar.show();
                        } else if (!shouldShowActionBar && showing) {
                            actionBar.hide();
                        }
                    }
                });

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

    private boolean isAnyImageViewVisibleOnScreen() {
        for (ImageView imageView : mImageViews) {
            boolean isVisibleOnScreen = imageView.getGlobalVisibleRect(new Rect(0, 0, imageView.getWidth(), imageView.getHeight()));
            if (isVisibleOnScreen) return true;
        }
        return false;
    }

    private TextView generateTextView(Element element) {
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
        }

        if ("p".equals(element.tagName())) {
            if (element.children().size() == 1) {
                Element presumableA = element.child(0);
                if ("a".equals(presumableA.tagName())) {
                    if (presumableA.children().size() == 1) {
                        Element presumableImg = presumableA.children().get(0);
                        if ("img".equals(presumableImg.tagName())) {
                            return generateImageView(presumableImg);
                        }
                    }
                }
            }

            return generateTextView(element);
        }

//        if ("iframe".equals(element.tagName())) {
//            String videoId = "www.youtube.com/embed/yF0yPOhcHIk";
//     todo      create YouTubePlayerView
//        }

        return null;
    }

    private ImageView generateImageView(Element img) {
        ImageView imageView = (ImageView) mInflater.inflate(R.layout.part_image_view_block, mCreatable, false);
        String src = img.attr("src");
        Picasso.with(getActivity())
                .load(src)
                .into(imageView);
        mImageViews.add(imageView);
        return imageView;
    }

}
