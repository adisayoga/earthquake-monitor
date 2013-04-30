package com.adisayoga.earthquake.wrapper;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.content.Context;
import android.util.Log;


/**
 * Class bantuan untuk memudahkan dalam mengirim email.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeMail extends Authenticator {
	
	private static final String TAG = "EarthquakeMail";
	
	private String host = "smtp.gmail.com"; // default smtp server
	private String port = "465"; // default smtp port
	private String sport = "465"; // default socketfactory port
	private String user = ""; // username
	private String pass = ""; // password
	
	private String from = ""; // Email dikirim dari
	private String[] to = new String[] {}; 
	private String subject = ""; // Email subjek
	private String body = ""; // email body

	private final boolean auth = true; // smtp authentication - default on
	private final boolean debuggable = false; // debug mode on or off - default off
	private final Multipart multipart = new MimeMultipart();
	
	public EarthquakeMail(Context context) {
		// Ada sesuatu yang salah dengan MailCap, javamail tidak dapat menemukan
		// handler untuk multipart/mixed part, jadi berikut ini perlu ditambahkan.
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap
				.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
		
		Prefs prefs = Prefs.getInstance(context);
		// Load dari preferences atau biarkan dengan nilai defaultnya
		String host = prefs.getMailHost();
		if (!host.equals("")) this.host = host;
		String port = prefs.getMailPort();
		if (!port.equals("")) this.port = port;
		String sport = prefs.getMailSPort();
		if (!sport.equals("")) this.sport = sport;
		this.user = prefs.getMailUsername();
		this.pass = prefs.getMailPass();
	}
	
	public EarthquakeMail(Context context, String user, String pass) {
		this(context);
		
		this.user = user;
		this.pass = pass;
	}

	/**
	 * Kirim email.
	 * 
	 * @return True jika email berhasil dikirim, false sebaliknya.
	 * @throws Exception
	 */
	public boolean send() throws Exception {
		if (user.equals("") || pass.equals("") || to.length == 0
				|| from.equals("") || subject.equals("") || body.equals("")) {
			Log.d(TAG, "Data tidak lengkap");
			return false;
		}
		
		Properties props = getProperties();
		Session session = Session.getInstance(props, this);

		MimeMessage msg = new MimeMessage(session);

		msg.setFrom(new InternetAddress(from));

		InternetAddress[] addressTo = new InternetAddress[to.length];
		for (int i = 0; i < to.length; i++) {
			addressTo[i] = new InternetAddress(to[i]);
		}
		msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

		msg.setSubject(subject);
		msg.setSentDate(new Date());

		// setup message body
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(body);
		multipart.addBodyPart(messageBodyPart);

		// Taruh parts pada message
		msg.setContent(multipart);

		// kirim email
		Transport.send(msg);
		Log.d(TAG, "Pesan telah dikirim ke: " + from.toString());
		Log.i(TAG, body);
		return true;
	}

	private Properties getProperties() {
		Properties props = new Properties();

		props.put("mail.smtp.host", host);

		if (debuggable) props.put("mail.debug", "true");
		if (auth) props.put("mail.smtp.auth", "true");
		
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.socketFactory.port", sport);
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		return props;
	}

	/**
	 * Menambahkan file lampiran.
	 * 
	 * @param filename Nama file lampiran
	 * @throws Exception
	 */
	public void addAttachment(String filename) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);

		multipart.addBodyPart(messageBodyPart);
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, pass);
	}

	// Getters dan setters
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String[] getTo() {
		return to;
	}

	public void setTo(String[] to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSport() {
		return sport;
	}

	public void setSport(String sport) {
		this.sport = sport;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}