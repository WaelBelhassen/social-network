package com.example.polina.socialnetwork;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;


import com.amazonaws.org.apache.http.client.utils.URLEncodedUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by polina on 04.06.15.
 */
public class ServerAPI implements API {
    public static String HOST = "https://socialnetwork-core-rest.herokuapp.com/";
    private String LOG_IN = HOST + "user/login";
    private String SIGN_UP = HOST + "user/register";
    private String USER_INFO = HOST + "/user/me";
    private String GET_USER = HOST + "/user/";
    private String POST_USER = HOST + "/post";
    private String GET_POSTS = HOST + "/timeline/";
    private String GET_POSTS_LOAD = "?limit=%d&before=%s";
    private final String MAIL = "email";
    private final String PASSWORD = "password";
    private String POST_GET_COMMENT = HOST + "post/%s/comment";
    private String GET_POST = HOST + "/post/%s";
    private String GET_LIKE = HOST + "/post/%s/like";
    private String DELETE_EDIT_POST = HOST + "/user/me/post/%s";
    private String GET_SEARCH_USERS = HOST + "user/find?";
    private String GET_FOLLOWERS = HOST + "user/%s/followers?";
    private String GET_FOLLOWING = HOST + "user/%s/following?";
    private String GET_FEED = HOST + "feed";
    private String GET_FEED_LOAD = HOST + "feed?limit=10&before=%s";


    @Override
    public JSONObject logIn(String email, String password) {
        return logInSignUp(email, password, LOG_IN);
    }

    @Override
    public JSONObject signUp(String email, String password) {
        return logInSignUp(email, password, SIGN_UP);
    }

    @Override
    public JSONObject saveProfile(String name, String birthday, String sex, String imageUrl, String imageMiniUrl) {
        JSONObject o = new JSONObject();
        try {
            o.put(Utils.NAME, name);
            o.put(Utils.BIRTHDAY, birthday);
            o.put(Utils.SEX, sex);
            o.put(Utils.PROF_URL, imageUrl);
            o.put(Utils.MINI_PROF_URL, imageMiniUrl);
            return postRequest(o, USER_INFO);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private JSONObject deleteRequest(String path) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpDelete delete = new HttpDelete(path);
            CookieManager cookieManager = CookieManager.getInstance();
            delete.addHeader("Cookie", cookieManager.getCookie(path));
            HttpResponse resp = client.execute(delete);
            if (resp.getStatusLine().getStatusCode() < 400) {
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject getRequest( String path) {
        System.err.println("REQUEST PATH: " + path);
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(path);
            CookieManager cookieManager = CookieManager.getInstance();
            get.addHeader("Cookie", cookieManager.getCookie(path));
            HttpResponse resp = client.execute(get);
            if (resp.getStatusLine().getStatusCode() < 400) {
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject getProfile() {
        return getRequest(USER_INFO);
    }

    @Override
    public JSONObject getUser(String userId) {
        return getRequest(GET_USER +userId);
    }

    @Override
    public JSONObject getLoadPosts( String idUser, String idPost) {
        if(idPost.isEmpty()){
            return getRequest( GET_POSTS + idUser);
        }
        return getRequest( GET_POSTS + idUser + String.format(GET_POSTS_LOAD, 10, idPost));
    }

    @Override
    public JSONObject sendComment( String idPost, String comment) {
        JSONObject o = new JSONObject();
        try {
            o.put(Utils.COMMENT, comment);
            postRequest( o, String.format(POST_GET_COMMENT, idPost) );

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JSONObject getPost( String idPost) {
        return getRequest(String.format(GET_POST, idPost));
    }

    @Override
    public JSONObject deletePost(String postId) {
        return deleteRequest(String.format(DELETE_EDIT_POST, postId));
    }

    @Override
    public JSONObject getLike(String idPost) {
        return getRequest(String.format(GET_LIKE, idPost));
    }

    @Override
    public JSONObject getComments(String idPost) {
        return getRequest(String.format(POST_GET_COMMENT, idPost));
    }



    @Override
    public JSONObject editComment( String idPost, String idComment, String comment) throws JSONException {
        JSONObject o = new JSONObject();
        o.put(Utils.COMMENT, comment);
        return putRequest(o, String.format(POST_GET_COMMENT, idPost) + "/" + idComment);
    }

    @Override
    public JSONObject deleteComment( String idPost, String idComment) {
        return deleteRequest(String.format(POST_GET_COMMENT, idPost) + "/" + idComment);
    }

    @Override
    public JSONObject findUsers(String name, int offset) {
        List<com.amazonaws.org.apache.http.NameValuePair> params = new LinkedList<>();
        params.add(new com.amazonaws.org.apache.http.message.BasicNameValuePair("q", name));
        params.add(new com.amazonaws.org.apache.http.message.BasicNameValuePair("offset", "" + offset));

        String paramString = URLEncodedUtils.format(params, "UTF-8");
        System.err.println(" find URL " + GET_SEARCH_USERS + paramString);
        return getRequest(GET_SEARCH_USERS + paramString);
    }

    @Override
    public JSONObject getFollowers(String id, int offset) {
        List<com.amazonaws.org.apache.http.NameValuePair> params = new LinkedList<>();
        params.add(new com.amazonaws.org.apache.http.message.BasicNameValuePair("offset", "" + offset));
        String paramString = URLEncodedUtils.format(params, "UTF-8");
        return getRequest(String.format(GET_FOLLOWERS, id) + paramString);
    }

    @Override
    public JSONObject getFollowing(String id, int offset) {
        List<com.amazonaws.org.apache.http.NameValuePair> params = new LinkedList<>();
        params.add(new com.amazonaws.org.apache.http.message.BasicNameValuePair("offset", "" + offset));
        String paramString = URLEncodedUtils.format(params, "UTF-8");
        return getRequest(String.format(GET_FOLLOWING, id) + paramString);
    }

    @Override
    public JSONObject getFeed(String postId) {
        if(postId.isEmpty()){
            return getRequest(GET_FEED);
        }
        return getRequest(String.format(GET_FEED_LOAD, postId));
    }

    private JSONObject logInSignUp(String email, String password, String path) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);
            List<NameValuePair> nameValuePairs = new ArrayList(2);
            nameValuePairs.add(new BasicNameValuePair(MAIL, email));
            nameValuePairs.add(new BasicNameValuePair(PASSWORD, password));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() < 400) {
                List<Cookie> cookies = client.getCookieStore().getCookies();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.removeAllCookie();
                for (Cookie sessionCookie : cookies)
                    if (sessionCookie != null) {
                        String cookieString = sessionCookie.getName() + "=" + sessionCookie.getValue() + "; domain=" + sessionCookie.getDomain();
                        cookieManager.setCookie(sessionCookie.getDomain(), cookieString);
                    }
                CookieSyncManager.getInstance().sync();
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else {
                System.err.println("ERROR: " + resp.getStatusLine());
                return null;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }

    private JSONObject postRequest( JSONObject o, String path) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);
            CookieManager cookieManager = CookieManager.getInstance();
            post.addHeader("Cookie", cookieManager.getCookie(path));
            String data = o.toString();
            post.setEntity(new StringEntity(data, HTTP.UTF_8));
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() < 400) {
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject putRequest(JSONObject o, String path) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPut post = new HttpPut(path);
            CookieManager cookieManager = CookieManager.getInstance();
            post.addHeader("Cookie", cookieManager.getCookie(path));
            String data = o.toString();
            post.setEntity(new StringEntity(data, HTTP.UTF_8));
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() < 400) {
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public JSONObject newPost( String massage, JSONObject location, String image, String account) {
        JSONObject o = new JSONObject();
        try {
            o.put(Utils.POST_MASSAGE, massage);
            o.put(Utils.POST_LOCATION, location);
            o.put(Utils.POST_IMAGE, image);
            o.put(Utils.POST_ACCOUNT, account);
            return postRequest( o, POST_USER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
