package ua.kulku.parsehtml;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

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
        mApi.getArticle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Article>() {
                    @Override
                    public void call(Article article) {
                        String content = article.content;
                        visualizeArticleBody(content);
                    }
                });
    }

    private void visualizeArticleBody(String content) {
        visualizeAsTextView(content);
        visualizeAsParsedContent(content);
    }

    private void visualizeAsParsedContent(String content) {
        mCreatable = (LinearLayout) getView().findViewById(R.id.parsed_content_article);

        //get all tags of p -> TextView (handles bold and italic)

        //get image srcs -> ImageView + Picasso

        //get youtube video id -> Youtube library

        mInflater = LayoutInflater.from(getActivity());
        Document doc = Jsoup.parse(content);
        final Element root = doc.child(0).child(1);
        callThem(root);
    }

    private void appendTextBlock(Element element) {
        TextView textView = (TextView) mInflater.inflate(R.layout.part_text_block, mCreatable, false);
        textView.setText(Html.fromHtml(element.html()));
        mCreatable.addView(textView);
    }

    private void visualizeAsTextView(String content) {
        TextView textView = (TextView) getView().findViewById(R.id.text_view_content_article);
        textView.setText(Html.fromHtml(content));
    }

    private void callThem(Node node) {
        if (node instanceof Element && "p".equals(((Element) node).tagName())) { //todo extract constant
            appendTextBlock((Element) node);
        }
        for (Node element : node.childNodes()) {
            callThem(element);
        }
    }

}
