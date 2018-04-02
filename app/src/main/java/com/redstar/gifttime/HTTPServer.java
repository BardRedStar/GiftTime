package com.redstar.gifttime;

import android.util.Base64;
import android.util.Log;

import com.redstar.gifttime.HTTPServerHelper.HTTPAnswer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class HTTPServer {

    private HTTPServerHelper helper;

    /**
     * Constructor of HTTP-connection client (with email and password).
     * Creates a helper for HTTP-requests.
     *
     * @param email user's email
     * @param password user's password
     */
    public HTTPServer(String email, String password) {
        helper = new HTTPServerHelper(email, password);
    }

    /**
     * Another constructor of HTTP-connection client (without inner data).
     * Creates a helper for HTTP-requests.
     */
    public HTTPServer() {
        helper = new HTTPServerHelper();
    }

    /**
     * Creates request to server to get user data by userID
     *
     * @param userId identifier of user
     * @return {@link JSONObject JSON object} with user's data or null if request was failed.
     */
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

    /**
     * Creates an auth request with email and password.
     *
     * @param email user's email address
     * @param password user's password
     * @return {@link JSONObject JSON object} with user's data or null, if request was failed
     */
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

    /**
     * Creates an register request with username, email and password.
     *
     * @param name user's username
     * @param email user's email address
     * @param password user's password
     * @return {@link JSONObject JSON object} with new user's data or null, if request was failed
     */
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

    /**
     * Creates a request to get user's cards
     *
     * @param userId user's identifier
     * @return {@link JSONArray JSON array} with information about user's cards
     */
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

    /**
     * Creates a request to add {@link SaleCard card} to user's cards
     *
     * @param userId user's identifier
     * @param card card to add
     * @return {@link JSONObject JSON object} with updated user's data
     */
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

    /**
     * Creates a request to delete {@link SaleCard card} from user's cards
     *
     * @param userId user's identifier
     * @param cardId card identifier
     * @return request response code
     */
    public int tryDeleteCard(String userId, String cardId) {
        HTTPAnswer answer = helper.doDeleteQuery("/api/users/" + userId + "/archiveCard/" + cardId);
        if (answer == null)
            return -1;

        return answer.getResponseCode();
    }
}
