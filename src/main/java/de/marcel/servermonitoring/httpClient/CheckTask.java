package de.marcel.servermonitoring.httpClient;

import de.marcel.servermonitoring.controller.WebSocketObserver;
import de.marcel.servermonitoring.model.CheckRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CheckTask implements Runnable {

	private HttpClient client;
	private Map<String, List<CheckRequest>> map;
	private WebSocketObserver websocket;

	public CheckTask(HttpClient client, Map<String, List<CheckRequest>> map, WebSocketObserver update)
	{
		this.websocket = update;
		this.client = client;
		this.map = map;
	}

	/*
	Methode iteriert eine Map, die vom Scheduler übergeben wird und kontaktiert mit HttpClient per GET das Server-Ziel.
	Ergebnis wird in alle Monitoring-Aufträge eingetragen, bei denen eine Überachung dieses Servers beauftragt wurde.

	Ist der Server nicht erreichbar oder wurde eine invalide Addresse eingegeben, wirft der HttpClient eine TimeOutException
	oder es entsteht eine URISyntaxException. In dem Fall erhalten alle an diesem Server interessierten Monitoring-Aufträge
	die Notiz "Error or not reachable".

	Nach dem abarbeiten jew. einer Serveraddresse erfolgt ein Update per Websocket an alle Sessions (ApplicationScoped)
	 */
	@Override public void run()
	{
		Set<Map.Entry<String, List<CheckRequest>>> entries = map.entrySet();

		for( Map.Entry<String, List<CheckRequest>> e : entries ) {

			try
			{
				Logger.getLogger(this.getClass().getSimpleName()).severe("+# CheckTask für " + e.getKey() );

				URI uri = new URI(e.getKey());
				HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).timeout(Duration.ofSeconds(2)).build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				e.getValue().stream().forEach(el -> {
					el.setStatus(response.statusCode() + " - " + HttpStatusCode.getByValue(response.statusCode()));
					el.setLastResult(ZonedDateTime.now());
				});
				Logger.getLogger(this.getClass().getSimpleName()).severe("+# Response war " + response.statusCode() + "Response Objekte zum Server " +e.getKey() + " sind " + e.getValue());

			}
			catch (Exception ex)
			{
				Logger.getLogger(this.getClass().getSimpleName()).severe("Timeout oder Exception bei http Anfrage: " + ex);
				e.getValue().stream().forEach(el -> {
					el.setStatus("Error or not reachable");
					el.setLastResult(ZonedDateTime.now());
				});
				Logger.getLogger(this.getClass().getSimpleName()).severe("+# Response Objekte zum Server " +e.getKey() + " sind " + e.getValue());
				//ex.printStackTrace();
			}
			finally
			{
				websocket.sendMessage();
			}
		}
	}
}
