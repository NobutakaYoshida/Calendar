package com.example.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

@Controller
@RequestMapping("/calendar")
public class CalendarQuickstartController {
  private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = GsonFactory
      .getDefaultInstance();
  private static final String CREDENTIALS_FOLDER = "credentials";

  /**
   * Global instance of the scopes required by this quickstart. If modifying
   * these scopes, delete your previously saved credentials/ folder.
   */
  private static final List<String> SCOPES = Collections
      .singletonList(CalendarScopes.CALENDAR_READONLY);
  private static final String CLIENT_SECRET_DIR = "client_secret.json";

  /**
   * Creates an authorized Credential object.
   * 
   * @param HTTP_TRANSPORT
   *          The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException
   *           If there is no client_secret.
   */
  private static Credential getCredentials(
      final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    // Load client secrets.
    URL url = ClassLoader.getSystemResource(CLIENT_SECRET_DIR);
    URI uri = URI.create(url.toString());
    Path path = Paths.get(uri);
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
        new InputStreamReader(new FileInputStream(path.toString())));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(
                new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
            .setAccessType("offline").build();
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
        .authorize("user");
  }


  @RequestMapping("")
  public String calendar(Model model)
      throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport
        .newTrustedTransport();
    Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME)
            .build();

    // List the next 10 events from the primary calendar.
    DateTime now = new DateTime(System.currentTimeMillis());
    Events events = service.events().list("primary").setMaxResults(10)
        .setTimeMin(now).setOrderBy("startTime").setSingleEvents(true)
        .execute();
    System.out.println(events);
    List<Event> items = events.getItems();
    List<String> eventList = new ArrayList<>();
    if (items.isEmpty()) {
      System.out.println("No upcoming events found.");
    } else {
      System.out.println("Upcoming events");
      for (Event event : items) {
        DateTime start = event.getStart().getDateTime();
        if (start == null) {
          start = event.getStart().getDate();
        }
        System.out.printf("%s (%s)\n", event.getSummary(), start);
        eventList.add(event.getSummary());
      }
    }
    System.out.println(eventList.get(0));
    model.addAttribute("NoscheduleMessage", "No upcoming events found.");
    model.addAttribute("eventList", eventList);
    return "/calendar";
  }
}