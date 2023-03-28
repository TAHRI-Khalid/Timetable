package tk.tahri.timetable.tools;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import tk.tahri.timetable.Entities.Etablissement;
import tk.tahri.timetable.Entities.Formateur;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Map;
import java.util.Properties;

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
                props.put("mail.debug", "true");

                String tmpdir = System.getProperty("java.io.tmpdir");

                Session session = Session.getInstance(props);

                int i = 0;
                for (var entry : map.entrySet()) {

                    var file = new File(tmpdir + "/" + entry.getKey() + ".png");
                    ImageIO.write(SwingFXUtils.fromFXImage(entry.getValue(), null), "PNG", file);

                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(etablissement.getUsername()));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(entry.getKey().getMail()));
                    message.setSubject(mailSubject);
                    message.setText(mailMessage);

                    var messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setText(mailMessage);

                    var multipart = new MimeMultipart("related");
                    multipart.addBodyPart(messageBodyPart);

                    var imgPart = new MimeBodyPart();
                    imgPart.attachFile(file);
                    multipart.addBodyPart(imgPart);
                    message.setContent(multipart);
                    Transport.send(message,etablissement.getUsername(), etablissement.getPassword());

                    i++;
                    updateProgress(i, map.size());
                    updateValue(entry.getKey());
                }
                return null;
            }
        };
    }

}
