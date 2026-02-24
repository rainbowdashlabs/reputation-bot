/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.logutil.marker.LogNotify;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.Mailing;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dao.access.user.sub.MailEntry;
import de.chojo.repbot.dao.access.user.sub.MailSource;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.service.mailservice.FailureReason;
import de.chojo.repbot.service.mailservice.Mail;
import de.chojo.repbot.util.Result;
import de.chojo.repbot.util.Retry;
import jakarta.activation.DataHandler;
import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.validator.routines.EmailValidator;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class MailService {
    public MailService(Configuration configuration, UserRepository userRepository, Threading threading) {
        this.configuration = configuration;
        this.userRepository = userRepository;
        threading.repBotWorker().scheduleAtFixedRate(this::cleanupExpiredMails, 30, 60, TimeUnit.MINUTES);
    }

    private static final Pattern MAIL_SHORTER = Pattern.compile("(.{2}).+?@.+?(.{2}\\..+)");
    private static final Logger log = getLogger(MailService.class);

    Configuration configuration;
    UserRepository userRepository;

    public Result<MailEntry, FailureReason> registerAndPromptVerify(long user, String mail, MailSource source) {
        String hash = mailHash(mail);
        Optional<RepUser> repUser = userRepository.byMailHash(hash);
        MailEntry mailEntry;
        if (repUser.isPresent()) {
            mailEntry = repUser.get().mails().getMail(hash).get();
            if (mailEntry.verified()) {
                return Result.failure(FailureReason.ALREADY_REGISTERED);
            }
            mailEntry.updateUser(user);
        } else {
            Result<MailEntry, FailureReason> newEntry = createMailEntry(user, mail, source);
            if (newEntry.isFailure()) return newEntry;
            mailEntry = newEntry.result();
            userRepository.byId(user).mails().addMail(mailEntry);
        }

        sendMail(Mail.accountConfirmation(mail, configuration.api().url(), hash, mailEntry.verificationCode()));

        return Result.success(mailEntry);
    }

    /**
     * Attempts to verify the mail entry with the given verification code.
     *
     * @param user             user that owns that mail entry
     * @param mailHash         hash of the mail entry
     * @param verificationCode verification code to verify with
     * @return a failure reason if the verification failed, otherwise null
     */
    @Nullable
    public FailureReason verifyMail(long user, String mailHash, String verificationCode) {
        Optional<RepUser> repUser = userRepository.byMailHash(mailHash);
        if (repUser.isEmpty()) return FailureReason.UNKNOWN_ADDRESS;
        if (repUser.get().id() != user) return FailureReason.WRONG_USER;
        MailEntry mail = repUser.get().mails().getMail(mailHash).get();
        if (!mail.verificationCode().equals(verificationCode)) return FailureReason.INVALID_CODE;
        if (mail.verificationRequested().isBefore(Instant.now().minus(1, ChronoUnit.HOURS)))
            return FailureReason.CODE_EXPIRED;
        mail.verify();
        return null;
    }

    public Result<MailEntry, FailureReason> registerVerifiedMail(long user, String mail, MailSource source) {
        Optional<RepUser> optUser = userRepository.byMailHash(mailHash(mail));
        if (optUser.isPresent()) {
            // This is still considered a succcess, because the mail adress is already registered and present in the
            // result.
            MailEntry mailEntry = optUser.get()
                                         .mails()
                                         .getMail(configuration.mailing().mailHash(mail))
                                         .get();
            mailEntry.updateUser(user);
            // If the mail entry is not verified, verify it.
            if (!mailEntry.verified()) {
                mailEntry.verify();
            }
            return new Result<>(mailEntry, FailureReason.ALREADY_REGISTERED, true);
        }

        Result<MailEntry, FailureReason> result = createMailEntry(user, mail, source);
        if (!result.isSuccess()) return result;
        MailEntry mailEntry = result.result();
        RepUser repUser = userRepository.byId(user);
        repUser.mails().addMail(mailEntry);
        return Result.success(mailEntry);
    }

    public Result<MailEntry, FailureReason> createMailEntry(long user, String mail, MailSource source) {
        boolean valid = EmailValidator.getInstance().isValid(mail);
        if (!valid) {
            return Result.failure(FailureReason.INVALID_FORMAT);
        }

        Matcher matcher = MAIL_SHORTER.matcher(mail);

        if (!matcher.matches()) {
            return Result.failure(FailureReason.INVALID_FORMAT);
        }

        var mailShort = "%s***@***%s".formatted(matcher.group(1), matcher.group(2));

        return Result.success(new MailEntry(
                user,
                source,
                mailHash(mail),
                mailShort,
                false,
                Instant.now(),
                UUID.randomUUID().toString()));
    }

    public String mailHash(String mail) {
        return configuration.mailing().mailHash(mail);
    }

    private Session createSession() {
        log.debug("Creating new mail session");
        Properties props = System.getProperties();
        Mailing mailing = configuration.mailing();
        props.putAll(mailing.properties());
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailing.user(), mailing.password());
            }
        });
    }

    private IMAPStore createImapStore(Session session) {
        log.debug("Creating imap store");
        IMAPStore imapStore = null;
        try {
            imapStore = (IMAPStore) session.getStore("imap");
            imapStore.connect();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return imapStore;
    }

    private MimeMessage buildMessage(Session session, Mail mail) throws MessagingException {
        var message = new MimeMessage(session);
        message.addFrom(
                new Address[]{new InternetAddress(configuration.mailing().user())});
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(mail.address(), false));
        message.setDataHandler(new DataHandler(mail.text(), "text/html; charset=UTF-8"));
        message.setSubject(mail.subject());
        message.setHeader("X-Mailer", "Reputation Bot");
        message.setSentDate(new Date());
        return message;
    }

    public void sendMail(Mail mail) {
        Session session = createSession();
        MimeMessage mimeMessage;
        try {
            mimeMessage = buildMessage(session, mail);
        } catch (MessagingException e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Could not build mail", e);
            return;
        }

        Optional<Boolean> sendResult = Retry.retryAndReturn(3, () -> sendMessage(mimeMessage), err -> {
            log.error(LogNotify.NOTIFY_ADMIN, "Could not sent mail", err);
            sendMail(mail);
        });

        if (sendResult.isEmpty()) {
            log.error(LogNotify.NOTIFY_ADMIN, "Retries exceeded. Aborting.");
            return;
        }

        try (IMAPStore imapStore = createImapStore(session)) {
            Optional<Boolean> result = Retry.retryAndReturn(3, () -> storeMessage(imapStore, mimeMessage), err -> {
                log.error(LogNotify.NOTIFY_ADMIN, "Could not store mail");
                sendMail(mail);
            });

            if (result.isPresent() && result.get()) {
                log.debug("Mail stored");
            } else {
                log.error(LogNotify.NOTIFY_ADMIN, "Retries exceeded. Aborting.");
            }
        } catch (MessagingException e) {
            log.error("Error occurred while sending a mail", e);
        }
    }

    private boolean storeMessage(IMAPStore store, MimeMessage message) throws MessagingException {
        store.getFolder("inbox");
        Folder sent = getInbox(store).getFolder("Sent");
        if (!sent.exists()) {
            sent.create(Folder.HOLDS_MESSAGES);
        }
        sent.appendMessages(new Message[]{message});
        return true;
    }

    private IMAPFolder getInbox(IMAPStore store) {
        return getFolder(store, "inbox");
    }

    private IMAPFolder getFolder(IMAPStore store, String name) {
        return Retry.retryAndReturn(
                            3,
                            () -> {
                                log.debug("Connecting to folder {}", name);
                                IMAPFolder folder = (IMAPFolder) store.getFolder(name);
                                folder.open(Folder.READ_WRITE);
                                return folder;
                            },
                            err -> {
                                log.error(LogNotify.NOTIFY_ADMIN, "Could not connect to folder. Retrying.");
                                getFolder(store, name);
                            })
                    .orElseThrow(() -> new RuntimeException("Reconnecting to folder failed."));
    }

    private boolean sendMessage(MimeMessage message) throws MessagingException {
        log.info("Sending mail to {}", ((InternetAddress) message.getAllRecipients()[0]).getAddress());
        Transport.send(
                message, configuration.mailing().user(), configuration.mailing().password());
        log.info("Mail sent.");
        return true;
    }

    private void cleanupExpiredMails() {
        userRepository.cleanupExpiredMails();
    }
}
