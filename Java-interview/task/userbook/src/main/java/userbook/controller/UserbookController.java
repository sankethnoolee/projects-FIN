package userbook.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import userbook.bo.UserbookBo;


@Controller
public class UserbookController {

	@Autowired
	private UserbookBo userbookBo;


	@RequestMapping("/login")
	public ModelAndView getLogin(HttpServletRequest request,HttpServletResponse responses) {
		System.out.println("user login page");
		return new ModelAndView("WEB-INF/JSP/login.jsp");
	}

	@RequestMapping(value="/getuserdetails", method = RequestMethod.GET,produces="application/json")
	@ResponseBody
	public String getUserInfo(HttpServletRequest request,HttpServletResponse responses) {
		JSONObject data =new JSONObject();
		try {
			System.out.println("user logged in");
			String userName=request.getParameter("userName");
			String password=request.getParameter("password");
			data = userbookBo.getUserDetails(userName, password);
		} catch (Exception e) {
		}
		return data.toString();
	}
	@RequestMapping("/sendfriendrequest")
	public ModelAndView sendFriendRequest(HttpServletRequest request,HttpServletResponse responses) {
		System.out.println("sendfriendrequest");
		return new ModelAndView("null");
	}
	@RequestMapping("/getmutualfriends")
	public ModelAndView getMutualFriends(HttpServletRequest request,HttpServletResponse responses) {
		System.out.println("getMutualFriends");
		return new ModelAndView("null");
	}
}
