/**
 * @author Kristin Fjola Tomasdottir
 * @date 	05.03.2015
 * @goal 	Functionality for everything regarding the user's Facebook account
 */
package com.corners.game.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.corners.game.FacebookService;
import com.corners.game.FacebookUser;

import com.facebook.HttpMethod;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

public class FacebookServiceImpl implements FacebookService{
    private final AndroidLauncher androidLauncher;
    private final UiLifecycleHelper uiHelper;
    private FacebookUser user = new FacebookUser();
   
    public FacebookServiceImpl(AndroidLauncher androidLauncher) {
        this.androidLauncher = androidLauncher;
        this.uiHelper = new UiLifecycleHelper(androidLauncher, new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
			}
        });
    }

	@Override
	public boolean isLoggedIn() {
		boolean isLoggedIn = Session.getActiveSession() != null && Session.getActiveSession().isOpened();
		return isLoggedIn;
	}

	@Override
	public FacebookUser logIn() {
		Log.i("facebook", "logging in");
		List<String> permissions = new ArrayList<String>();
        permissions.add("user_friends");
        permissions.add("user_games_activity");
        permissions.add("friends_games_activity");
        permissions.add("public_profile");
        permissions.add("email");

        Session.openActiveSession(
        		androidLauncher,
                true,
                permissions,
                new Session.StatusCallback() {
                    // callback when session changes state
                    @Override
                    public void call(Session session, SessionState state, Exception exception) {
                    	Log.i("facebook", "session state change");
                        if (session.isOpened()) {
                        	Log.i("facebook", "session isOpened");
                            if(!session.getPermissions().contains("publish_actions") && !user.isTriedGettingPublishPermission()) {
                            	 Log.i("facebook", "getting permission for publish_actions");
                            	 Session.NewPermissionsRequest newPermissionsRequest = new Session
                                         .NewPermissionsRequest(androidLauncher, Arrays.asList("publish_actions"));
                                 session.requestNewPublishPermissions(newPermissionsRequest);
                                 user.setTriedGettingPublishPermission(true);
                            }
                            if (!session.getPermissions().contains("user_friends")) {
                                Session.NewPermissionsRequest newPermissionsRequest = new Session
                                        .NewPermissionsRequest(androidLauncher, Arrays.asList("user_friends"));
                                session.requestNewReadPermissions(newPermissionsRequest);
                            }
                            new GetFacebookUserTask().execute(session);
                        } else {
                        	Log.i("facebook", "session was open");
                        }
                        Log.i("facebook", "finished logging into facebook");
                    }
                }
        );
        return user;
	}

	@Override
	public void logOut() {
		 if (isLoggedIn()) {
			Log.i("facebook", "logging out");
            Session.getActiveSession().closeAndClearTokenInformation();
            removeUser();
        }
	}
	
	@Override
	public void showFacebookUser(){
		if(isLoggedIn()){
			Session session = Session.getActiveSession();
			new GetFacebookUserTask().execute(session);
		}
	}
	
	/**
	 * removing information from Facebook about the user 
	 */
	public void removeUser(){
		user.setFirstName(null);
		user.setFullName(null);
		user.setId(null);
		user.setTriedGettingPublishPermission(false);
		new SetProfilePicTask().execute(user);
	}
    
	public void onCreate(Bundle savedInstanceState) {
        uiHelper.onCreate(savedInstanceState);
    }

    public void onResume() {
        uiHelper.onResume();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    public void onSaveInstanceState(Bundle outState) {
        uiHelper.onSaveInstanceState(outState);
    }

    public void onPause() {
        uiHelper.onPause();
    }

    public void onStop() {
        uiHelper.onStop();
    }

    public void onDestroy() {
        uiHelper.onDestroy();
    }
    
    /**
     * @param session - current Facebook session
     * fetches information about the user's Facebook account
     */
    public void getFacebookUser(Session session){
		Request request = Request.newGraphPathRequest(session, "me", null);
		Response response = Request.executeAndWait(request);
		try{
			JSONObject JSONuser = response.getGraphObject().getInnerJSONObject();
			setUser(JSONuser);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("facebook", "could not read JSONuser");
		}
    }
    
    /**
     * @param jo - JSON object representing the user's Facebook account
     * @return FacebookUser - information about the user's Facebook account
     */
    public void setUser(JSONObject jo){
    	try {
			user.setId(jo.get("id").toString());
			user.setFirstName(jo.get("first_name").toString());
			user.setFullName(jo.get("name").toString());
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("facebook", "could not create user");
			removeUser();
		}  	
    }
    
    @Override
    public String getUserName() {
    	return (this.user.getFullName()) != null ? this.user.getFullName() : "";
    }
    
    /**
     * @author Kristin Fjola Tomasdottir
     * @date 	19.03.2015
     * @goal 	Fetches information about the user's Facebook account
     */
    public class GetFacebookUserTask extends AsyncTask<Session, Void, Session> {
    	public GetFacebookUserTask() {}
		protected Session doInBackground(Session... sessions) {
			Session session = sessions[0];
			getFacebookUser(session);
			return session;
		}
    	protected void onPostExecute(Session session) {
    		new SetProfilePicTask().execute(user);
    	}
    }
    
    /**
     * @author Kristin Fjola Tomasdottir
     * @date 	19.03.2015
     * @goal 	Displays the user's Facebook photo
     */
    public class SetProfilePicTask extends AsyncTask<FacebookUser, Void, FacebookUser> {
    	public SetProfilePicTask() {}
    	protected FacebookUser doInBackground(FacebookUser... users) {
    		FacebookUser user = users[0];
    		return user;
    	}
    	protected void onPostExecute(FacebookUser user) {
    		androidLauncher.setProfilePicture(user);
    	}
    }
    
    @Override
    public List<String> getFriendsList() {
    	System.out.println("getting friends list");
    	System.out.println("getting session");
    	Session session = Session.getActiveSession();
    	System.out.println("getting request");
		Request request = Request.newGraphPathRequest(session, "me/friends", null);
		System.out.println("getting response");
		Response response = Request.executeAndWait(request);
		List<String> friends = new ArrayList<String>();
		try {
			JSONArray data = (JSONArray) response.getGraphObject().getInnerJSONObject().get("data");
			System.out.println("DATA: " + data);
			for(int i = 0; i < data.length(); i++) {
				String name = (String) data.getJSONObject(i).get("name");
				friends.add(name);
				System.out.println("Adding to friendslist: " + name);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("fb friends list", "JSONException retrieving fb friends list");
		}
		System.out.println("returning friends list");
		return friends;
    }
    
    /**
     * used in getScores method - to get score from id
     * 	 (the list is in the same order as the list from getFriendsList)
     * 
	 * @return list of ids of facebook friends
	 */
    public List<String> getFriendsListIds() {
    	Session session = Session.getActiveSession();
		Request request = Request.newGraphPathRequest(session, "me/friends", null);
		Response response = Request.executeAndWait(request);
		List<String> friends = new ArrayList<String>();
		try {
			JSONArray data = (JSONArray) response.getGraphObject().getInnerJSONObject().get("data");
			for(int i = 0; i < data.length(); i++) {
				String id = (String) data.getJSONObject(i).get("id");
				friends.add(id);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return friends;
    }
    
    @Override
    public List<Integer> getScores() {
    	System.out.println("getting scores from friends");
    	Session session = Session.getActiveSession();
    	List<String> friends = getFriendsListIds();
    	List<Integer> scores = new ArrayList<Integer>();
    	Integer score = 0;
    	
    	for(int i = 0; i < friends.size(); i++) {
    		String id = friends.get(i);
    		boolean is_score = false;
    		Request request = Request.newGraphPathRequest(session, id+"/scores", null);
    		Response response = Request.executeAndWait(request);
    		try {
    			JSONArray data = (JSONArray) response.getGraphObject().getInnerJSONObject().get("data");
    			if(data.length() != 0) {
    				for(int j = 0; j < data.length(); j++) {
    					JSONObject app = (JSONObject) data.getJSONObject(j).get("application");
    					String app_name = (String) app.get("name");
    					if(app_name.equals("Corners")) {
    						is_score = true;
    						score = (Integer) data.getJSONObject(j).get("score");
    					}
        			}
    			}
    			if(is_score) {
    				scores.add(score);
    			} else {
    				scores.add(-1); //no permission to see score
    			}
    			
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	return scores;
    }
    
    @Override
    public Integer getMyScore() {
    	Integer score = 0;
    	Session session = Session.getActiveSession();
    	Request request = Request.newGraphPathRequest(session, "me/scores", null);
		Response response = Request.executeAndWait(request);
		try {
			JSONArray data = (JSONArray) response.getGraphObject().getInnerJSONObject().get("data");
			if(data.length() != 0) {
				for(int j = 0; j < data.length(); j++) {
					JSONObject app = (JSONObject) data.getJSONObject(j).get("application");
					String app_name = (String) app.get("name");
					if(app_name.equals("Corners")) {
						score = (Integer) data.getJSONObject(j).get("score");
					}
    			}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return score;
    }
    
    @Override
    public void updateScore(String score) {
    	System.out.println("update score: "+ score);
    	Session session = Session.getActiveSession();
    	Bundle params = new Bundle();
    	params.putString("score", score);
    	Request updateScores = new Request(
    	    session,
    	    "/me/scores",
    	    params,
    	    HttpMethod.POST,
    	    new Request.Callback() {
    	        public void onCompleted(Response response) {
    	        	Log.i("facebook", "updating score");
    	        }
    	    }
    	);
    	Response response = updateScores.executeAndWait();
    }
    
    /**
	 * writes permessions for facebook user in console
	 */
    public void checkPermission() {
    	Session session = Session.getActiveSession();
		Request request = Request.newGraphPathRequest(session, "me/permissions", null);
		Response response = Request.executeAndWait(request);
		Log.i("facebook", "permission: "+ response);
    }
}
