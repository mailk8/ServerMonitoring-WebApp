package de.marcel.servermonitoring.httpClient;

import de.marcel.servermonitoring.controller.WebSocketObserver;
import de.marcel.servermonitoring.model.CheckRequest;

import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class Scheduler implements Serializable
{
	private java.net.http.HttpClient client = new HttpClientConfig().getHttpClient();

	@Resource private ManagedScheduledExecutorService scheduler;
	private ScheduledFuture future;

	private int time = 2; private TimeUnit unit = TimeUnit.SECONDS; // Interval der Abfragen

	@Inject private WebSocketObserver websocket;
	private ConcurrentMap<String, List<CheckRequest>> pendingOperations = new ConcurrentHashMap<>();

	/*
	Stoppt laufenden Job, Monitoringaufträge der aufrufenden UI-Session werden zusätzlich in der Map pendingOperations
	registriert und der Job neu gestartet.
	 */
	public boolean startRequests(List<CheckRequest> startables) throws Exception {
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# startRequests " );
		// Neue Aufträge registrieren, Scheduler neustarten
		safeCancel();

		Map<String, List<CheckRequest>> map = startables.stream().collect(Collectors.groupingBy(e -> e.getServer(), HashMap::new, Collectors.toList()));

		map.entrySet().stream().forEach(entry -> pendingOperations.merge(
						entry.getKey(),
						entry.getValue(),
						(old, ne) -> { entry.getValue().addAll(old); return entry.getValue(); }
						));

		future = scheduler.scheduleAtFixedRate(new CheckTask(client, pendingOperations, websocket), 0, time, unit);
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# started with pendings size" + pendingOperations.size() );
		return true;
	}

	/*
	Stoppt laufenden Job, Monitoringaufträge der aufrufenden UI-Session werden entfernt,
	falls es weitere Aufträge aus anderen Sessions gibt, wird der Job neu gestartet.
	 */
	public boolean stopRequests(List<CheckRequest> stoppables) {
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# stopRequests begin, stoppables size " + stoppables.size() );
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# stopRequests begin, pendingOperations size " + pendingOperations.size() );

		safeCancel();

		stoppables.stream().forEach(request -> pendingOperations.computeIfPresent(request.getServer(),
						(key, value) -> {
							List li = new ArrayList(pendingOperations.get(key));
							li.remove(request);
							return li.isEmpty() ?  null : li; // Eintrag wird enfernt bei null
						} ));

		Logger.getLogger(this.getClass().getSimpleName()).severe("+# stoppables entfernt, pendings size " + pendingOperations.size() );

		if(!pendingOperations.isEmpty())
		{
			Logger.getLogger(this.getClass().getSimpleName()).severe("+# restarting requests, size " + pendingOperations.size() );
			scheduler.scheduleAtFixedRate(new CheckTask(client, pendingOperations, websocket), 0, time, unit);

		}

		return true;
	}

	private void safeCancel()
	{
		if(! (future == null || future.isDone()) )
		{
			Logger.getLogger(this.getClass().getSimpleName()).severe("+# safeCancel " );
			future.cancel(true);
		}
	}
}
