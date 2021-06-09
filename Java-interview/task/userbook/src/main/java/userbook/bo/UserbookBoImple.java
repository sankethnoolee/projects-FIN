package userbook.bo;

import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class UserbookBoImple implements UserbookBo {

	public JSONObject getUserDetails(String userName, String password) {
		JSONObject obj = new JSONObject();
		obj.put("firstName", "Abhishek");
		obj.put("secondName", "Suvarna");
		obj.put("noOfFriends", "20");
		obj.put("signedIn", "20-12-2017");
		return obj;
	}

}
