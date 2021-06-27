package de.marcel.servermonitoring.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ApplicationScoped
public class WebSocketObserver implements Serializable
{
	@Inject @Push private PushContext channelUser;

	public void sendMessage() {
			channelUser.send("channelUser");
	}
}
