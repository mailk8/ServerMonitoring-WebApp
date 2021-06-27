package de.marcel.servermonitoring.controller;

import de.marcel.servermonitoring.httpClient.Scheduler;
import de.marcel.servermonitoring.model.CheckRequest;
import lombok.Data;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Data
@Named
@SessionScoped
@ManagedBean
public class BackingBean implements Serializable
{
	private List<CheckRequest> requests = new ArrayList<>();
	@Inject private Instance<Scheduler> scheduler; // weld cdi workaround
	private Boolean running = false;


	@PostConstruct
	private void prepare() {
		requests.add(new CheckRequest("https://172.217.0.0", "initial State"));
		requests.add(new CheckRequest("8.8.8.8", "initial State"));
		requests.add(new CheckRequest("localhost:80", "initial State"));
	}

	/*
	Wird von Startbutton aufgerufen und stößt das initiale Starten oder Neustarten
	von Monitoring-Jobs an.
	 */
	public void start() {

		Logger.getLogger(this.getClass().getSimpleName()).severe("+# start " );
		try
		{
			running = scheduler.get().startRequests(requests);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/*
	Wird von Stoppbutton aufgerufen und stößt das initiale Starten oder Neustarten
	von Monitoring-Jobs an.
	 */
	public void stop() {
		running =  Boolean.valueOf(!scheduler.get().stopRequests(requests));
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# stop " );
	}

	/*
	Erzeugt zusätzlichen Server-Eintrag als Monitoring-Auftrag
	 */
	public void addNewRow() {
		requests.add(new CheckRequest("", "initial State"));
	}

	/*
	Löscht Server-Eintrag
	 */
	public void deleteRow(CheckRequest req) {
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# deleteRow" );
		try
		{
			requests.remove(req);
		}
		catch (Exception e)
		{
			Logger.getLogger(this.getClass().getSimpleName()).severe("+# Fehler bei DeleteRow!" );
			e.printStackTrace();
		}
	}

}
