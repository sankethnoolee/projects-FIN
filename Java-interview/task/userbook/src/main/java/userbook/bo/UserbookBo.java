package userbook.bo;

import org.json.JSONObject;

public interface UserbookBo {
	public JSONObject getUserDetails(String userName, String password);
}
