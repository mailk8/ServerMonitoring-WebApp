package de.marcel.servermonitoring.httpClient;


import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.Serializable;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Logger;


public class HttpClientConfig implements Serializable
{
	// Executor wird als Resource vom Java EE Server bezogen
	@Resource private ManagedExecutorService executor;

	// Timeout für HttpClient
	private  Duration timeout = Duration.ofSeconds(1);

	/*
	Erzeugt einen HttpClient mit hier definierten Einstellungen. Dabei ist SSL aktiviert, die Einstellungen fordern jedoch keine
	weitere Sicherheit von der Verbindung! Daher nicht ohne Weiteres für Production-Usage geeignet!
	 */
	protected HttpClient getHttpClient()
	{
		try
		{
			// Schaltet Host Name Verification aus
			final Properties props = System.getProperties();
			props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());


			// Schaltet certificate validation aus
			SSLContext sslContext = null;
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
			{

				@Override public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}

				@Override public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{
				}

				@Override public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{
				}

			} };

			// Schaltet Client Auth aus
			SSLParameters sslParam = new SSLParameters();
			sslParam.setNeedClientAuth(false);

			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, null);

			HttpClient client=  java.net.http.HttpClient.newBuilder()
							.connectTimeout(timeout)
							//.executor(executor)
							.followRedirects(HttpClient.Redirect.NEVER)
							.sslContext(sslContext)
							.sslParameters(sslParam)
							//.version(HttpClient.Version.HTTP_2)
							//.authenticator(Authenticator.getDefault())
							.build();

			return client;
		}
		catch (Exception e)
		{
			Logger.getLogger(HttpClientConfig.class.getSimpleName()).severe("+# Exception creating client " + e  );
			e.printStackTrace();
		}

		return null;
	}
}
