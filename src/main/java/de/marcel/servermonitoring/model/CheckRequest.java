package de.marcel.servermonitoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Data
@NoArgsConstructor
public class CheckRequest
{
	private String server;
	private String status;
	private ZonedDateTime lastResult;

	public CheckRequest(String server, String status) {
		this.server = appendHttp(server);
		this.status = status;
	}

	public void setServer(String stringValue) {
		server = appendHttp(stringValue);
		status = "";
		lastResult = null;
	}

	private String appendHttp(String stringValue) {
		stringValue = stringValue.trim();
		if(!stringValue.startsWith("http"))
			stringValue = "http://" + stringValue;
		return stringValue;
	}

	public String getLastResultFormatted() {
		Locale.setDefault(Locale.UK);
		return lastResult != null ? lastResult.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)) : "";
	}


}
