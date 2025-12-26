package io.github.mohmk10.changeloghub.notification.channel;

import io.github.mohmk10.changeloghub.notification.exception.NotificationException;
import io.github.mohmk10.changeloghub.notification.formatter.EmailMessageFormatter;
import io.github.mohmk10.changeloghub.notification.model.ChannelConfig;
import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationResult;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import io.github.mohmk10.changeloghub.notification.util.NotificationConstants;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Email notification channel using Jakarta Mail.
 */
public class EmailNotifier extends AbstractNotificationChannel {

    private Session mailSession;
    private List<String> recipients = new ArrayList<>();
    private List<String> ccRecipients = new ArrayList<>();
    private List<String> bccRecipients = new ArrayList<>();

    public EmailNotifier() {
        this.formatter = new EmailMessageFormatter();
    }

    public EmailNotifier(ChannelConfig config) {
        super(config);
        this.formatter = new EmailMessageFormatter();
        initMailSession();
    }

    private void initMailSession() {
        if (config == null || !config.hasSmtpConfig()) {
            return;
        }

        ChannelConfig.SmtpConfig smtpConfig = config.getSmtpConfig();
        Properties props = new Properties();

        props.put("mail.smtp.host", smtpConfig.getHost());
        props.put("mail.smtp.port", String.valueOf(smtpConfig.getPort()));

        if (smtpConfig.isSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        if (smtpConfig.isStartTls()) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        if (smtpConfig.hasAuth()) {
            props.put("mail.smtp.auth", "true");
            mailSession = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        smtpConfig.getUsername(),
                        smtpConfig.getPassword()
                    );
                }
            });
        } else {
            mailSession = Session.getInstance(props);
        }
    }

    @Override
    public ChannelType getType() {
        return ChannelType.EMAIL;
    }

    @Override
    public void configure(ChannelConfig config) {
        super.configure(config);
        initMailSession();
    }

    @Override
    protected NotificationResult doSend(Notification notification, String formattedMessage) {
        if (recipients.isEmpty()) {
            throw NotificationException.invalidConfiguration(getType(), "No recipients configured");
        }

        try {
            ChannelConfig.SmtpConfig smtpConfig = config.getSmtpConfig();

            MimeMessage message = new MimeMessage(mailSession);

            // From
            String fromAddress = smtpConfig.getFromAddress() != null
                ? smtpConfig.getFromAddress()
                : NotificationConstants.EMAIL_DEFAULT_FROM;
            String fromName = smtpConfig.getFromName() != null
                ? smtpConfig.getFromName()
                : "ChangelogHub";

            message.setFrom(new InternetAddress(fromAddress, fromName));

            // To
            for (String recipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }

            // CC
            for (String cc : ccRecipients) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
            }

            // BCC
            for (String bcc : bccRecipients) {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
            }

            // Subject
            String subject = notification.getTitle() != null
                ? notification.getTitle()
                : "ChangelogHub Notification";
            message.setSubject(subject);

            // Body (HTML)
            MimeMultipart multipart = new MimeMultipart("alternative");

            // Plain text version
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(notification.getMessage() != null
                ? notification.getMessage()
                : "See HTML version for details");

            // HTML version
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(formattedMessage, NotificationConstants.EMAIL_MIME_TYPE_HTML);

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            // Send
            Transport.send(message);

            logger.info("Email notification sent to {} recipients", recipients.size());
            return NotificationResult.success(notification.getId(), getType(),
                message.getMessageID());

        } catch (Exception e) {
            logger.error("Failed to send email notification: {}", e.getMessage());
            throw NotificationException.sendFailed(getType(), e);
        }
    }

    @Override
    protected void validateConfiguration() {
        super.validateConfiguration();
        if (!config.hasSmtpConfig()) {
            throw NotificationException.invalidConfiguration(getType(),
                "SMTP configuration is required");
        }
    }

    @Override
    public boolean testConnection() {
        if (!isConfigured()) {
            return false;
        }

        try {
            ChannelConfig.SmtpConfig smtpConfig = config.getSmtpConfig();
            Transport transport = mailSession.getTransport("smtp");

            if (smtpConfig.hasAuth()) {
                transport.connect(smtpConfig.getHost(), smtpConfig.getPort(),
                    smtpConfig.getUsername(), smtpConfig.getPassword());
            } else {
                transport.connect();
            }

            transport.close();
            return true;

        } catch (Exception e) {
            logger.warn("Email connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public EmailNotifier addRecipient(String email) {
        this.recipients.add(email);
        return this;
    }

    public EmailNotifier addRecipients(List<String> emails) {
        this.recipients.addAll(emails);
        return this;
    }

    public EmailNotifier addCc(String email) {
        this.ccRecipients.add(email);
        return this;
    }

    public EmailNotifier addBcc(String email) {
        this.bccRecipients.add(email);
        return this;
    }

    public EmailNotifier clearRecipients() {
        this.recipients.clear();
        this.ccRecipients.clear();
        this.bccRecipients.clear();
        return this;
    }

    public List<String> getRecipients() {
        return new ArrayList<>(recipients);
    }

    /**
     * Create a configured EmailNotifier.
     */
    public static EmailNotifier create(ChannelConfig.SmtpConfig smtpConfig) {
        ChannelConfig config = ChannelConfig.email(smtpConfig).build();
        return new EmailNotifier(config);
    }

    // For testing
    void setMailSession(Session session) {
        this.mailSession = session;
    }
}
