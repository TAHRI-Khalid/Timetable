package tk.tahri.timetable.tools;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.image.WritableImage;
import tk.tahri.timetable.Entities.Etablissement;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.FormateurTimeTablePrint;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class MailService extends Service<Formateur> {

    private final Etablissement etablissement;
    private final Map<Formateur, WritableImage> map;
    private final String mailSubject;
    private final String mailMessage;

    public MailService(Map<Formateur, WritableImage> map, String subject, String message, Etablissement etablissement) {
        this.map = map;
        this.mailSubject = subject;
        this.mailMessage = message;
        this.etablissement = etablissement;
    }

    @Override
    protected Task<Formateur> createTask() {
        return new Task<Formateur>() {
            @Override
            protected Formateur call() throws Exception {

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.office365.com");
                props.put("mail.smtp.port", "587");

                String tmpdir = System.getProperty("java.io.tmpdir");

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(etablissement.getUsername(), etablissement.getPassword());
                    }
                });

                int i = 0;
                for (var entry : map.entrySet()) {

                    try {
                        var file = new File(tmpdir + "/" + entry.getKey() + ".png");
                        ImageIO.write(SwingFXUtils.fromFXImage(entry.getValue(), null), "PNG", file);

                        Message message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(etablissement.getUsername()));
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(entry.getKey().getMail()));
                        message.setSubject(mailSubject);
                        message.setText(mailMessage);

                        BodyPart messageBodyPart = new MimeBodyPart();
                        messageBodyPart.setText(mailMessage);

                        Multipart multipart = new MimeMultipart("related");
                        multipart.addBodyPart(messageBodyPart);

                        MimeBodyPart imgPart = new MimeBodyPart();
                        imgPart.attachFile(file);
                        multipart.addBodyPart(imgPart);
                        message.setContent(multipart);
                        Transport.send(message);
                        i++;
                        updateProgress(i, map.size());
                        updateValue(entry.getKey());

                    } catch (MessagingException | IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
    }

}
