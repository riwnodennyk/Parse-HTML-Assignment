package ua.kulku.parsehtml;

import retrofit.http.GET;
import rx.Observable;

public interface Api {
    @GET("/test.json")
    Observable<Article> getArticle();
}
