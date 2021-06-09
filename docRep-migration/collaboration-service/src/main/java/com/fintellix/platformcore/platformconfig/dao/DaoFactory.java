package com.fintellix.platformcore.platformconfig.dao; 



//import com.fintellix.products.traq.npa.dao.NPAClassificationDao;
//import com.fintellix.products.commons.rulesandgladjustments.dao.ReportDAO;
//import com.fintellix.products.commons.rulesandgladjustments.dao.ReviewProcessDAO;

public abstract class DaoFactory {

	public static final int HIBERNATE = 1;


	// Was already present the NPS DaoFactory .. not required
	//	public abstract NPAClassificationDao 			getNPAClassificationDao();
	
	// Both these moved this to GLAdj daoFactory
	//	public abstract ReportDAO 						getReportDAO();
	//	public abstract ReviewProcessDAO				getReviewProcessDAO();
}