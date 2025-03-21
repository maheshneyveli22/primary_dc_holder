

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.List;

import org.apache.log4j.Logger;

import com.caucho.hessian.client.HessianProxyFactory;
import com.db.gpf.csw.coveragemanager.hessian.service.ICoverageManagerHessianService;
import com.db.gpf.csw.java.common.model.Coverage;

/**
 * Hello world!
 * 
 */
public class App {
	private static Logger log = Logger.getLogger(App.class);

	public static void main(String[] args) {
		HessianProxyFactory factory = new HessianProxyFactory();
		String internetTestUrl ="http://msgm2202.gslb.db.com";
		String devcoverageUrl = "http://apcswd1.us.db.com:8968/coveragemanager_webapp/hessian/CoverageManagerService";
		String localCoverageURL = "http://localhost:8080/coveragemanager_webapp/hessian/CoverageManagerService";
		
	
		String paymentsTaskSummaryUrl= "http://apcswd1.us.db.com:8968/new_globalpayments_webapp/hessian/PaymentsTaskSummaryService";
		


		
		String ar[]={internetTestUrl,devcoverageUrl,paymentsTaskSummaryUrl,localCoverageURL};
		try {
			ICoverageManagerHessianService svc = (ICoverageManagerHessianService) factory.create(ICoverageManagerHessianService.class,
					devcoverageUrl);
			System.out.println("App.main(svc)"+svc);
			URL  url ;
			//check the connection
	for(String urlString:ar)
	{
		System.out.println("Testing url "+urlString);
		url = new URL(urlString);
	    HttpURLConnection con = (HttpURLConnection)url.openConnection();
	    int resCode= con.getResponseCode();
System.out.println("The url: -->"+urlString+"<-- has response code value("+resCode+")");
	}
			

			 
			     
		/*	 	Certificate[] certs = con.getServerCertificates();
				for(Certificate cert : certs){
				   System.out.println("Cert Type : " + cert.getType());
				   System.out.println("Cert Hash Code : " + cert.hashCode());
				   System.out.println("Cert Public Key Algorithm : " 
			                                    + cert.getPublicKey().getAlgorithm());
				   System.out.println("Cert Public Key Format : " 
			                                    + cert.getPublicKey().getFormat());
				   System.out.println("\n");
				}*/
			     

	
			List<Coverage> covlist= svc.getCoverageFilterContents("praveen.patil@db.com");
			System.out.println("App.main(covlist)"+covlist);
		
					
		} catch (Exception e) {
		System.out.println("Exception name "+e);
		}
	}
}
