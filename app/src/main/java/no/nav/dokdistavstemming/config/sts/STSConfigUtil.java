package no.nav.dokdistavstemming.config.sts;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;

import java.util.HashMap;

public class STSConfigUtil {

	private STSConfigUtil() {
	}

	static void configureStsRequestToken(Client client, String stsUrl, String username, String password) {
		STSClient stsClient= new STSClient(client.getBus());
		configureSTSClient(stsClient,stsUrl,username,password);
		client.getRequestContext().put(SecurityConstants.STS_CLIENT,stsClient);
	}
	private static void configureSTSClient(STSClient stsClient, String location, String username, String password){
		stsClient.setEnableAppliesTo(false);
		stsClient.setAllowRenewing(false);
		stsClient.setLocation(location);

		HashMap<String, Object> properties = new HashMap<>();
		properties.put(SecurityConstants.USERNAME,username);
		properties.put(SecurityConstants.PASSWORD,password);

		stsClient.setProperties(properties);

	}
}
