package com.redstar.gifttime;

import android.util.Base64;
import android.util.Log;

import com.redstar.gifttime.HTTPServerHelper.HTTPAnswer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class HTTPServer {

    private HTTPServerHelper helper;

    public HTTPServer(String email, String password) {
        helper = new HTTPServerHelper(email, password);
    }

    public HTTPServer() {
        helper = new HTTPServerHelper();
    }

    public JSONObject tryGetUserInfo(String userId) {
        try {
            HTTPAnswer answer = helper.doGetQuery("/api/users/" + userId);
            if (answer == null)
                return null;

            String sAnswer = answer.getAnswerBody();

            if (sAnswer == null)
                return null;

            return new JSONObject(sAnswer);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject tryLogIn(String email, String password) {
        try {
            String query = "{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }";

            HTTPAnswer answer = helper.doPostQuery("/api/auth/signin", query);

            if (answer == null) {
                return null;
            }
            String sAnswer = answer.getAnswerBody();

            return new JSONObject(sAnswer);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject trySignUp(String name, String email, String password) {
        try {
            String query = "{ \"email\": \"" + email + "\", \"name\": \"" + name + "\", \"password\": \"" + password + "\" }";

            HTTPAnswer answer = helper.doPostQuery("/api/auth/signup", query);
            if (answer == null)
                return null;

            String sAnswer = answer.getAnswerBody();

            return new JSONObject(sAnswer);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray tryGetCards(String userId) {
        try {
            HTTPAnswer answer = helper.doGetQuery("/api/users/" + userId + "/cards/");
            if (answer == null)
                return null;

            if (answer.getResponseCode() != 200) {
                return null;
            }
            String sAnswer = answer.getAnswerBody();
            return new JSONArray(sAnswer);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public JSONObject tryAddCard(String userId, SaleCard card) {
        try {
            String query = "{ \"organizationName\": \"" + card.companyName + "\", \"description\": \"" +
                    card.cardDescription + "\", \"frontPhoto\": \"" +
                    Base64.encodeToString(card.cardCodePhoto, 0) + "\", \"barCodePhoto\": \"" +
                    Base64.encodeToString(card.cardPhoto, 0) + "\" }";

            HTTPAnswer answer = helper.doPutQuery("/api/users/" + userId + "/addCard", query);
            if (answer == null)
                return null;

            if (answer.getResponseCode() != 200) {
                return null;
            }

            return new JSONObject(answer.getAnswerBody());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int tryDeleteCard(String userId, String cardId) {
        HTTPAnswer answer = helper.doDeleteQuery("/api/users/" + userId + "/archiveCard/" + cardId);
        if (answer == null)
            return -1;

        return answer.getResponseCode();
    }
}
